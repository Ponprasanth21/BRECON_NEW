-- =============================================================================
-- VISA RECONCILIATION AUTOMATION PROCEDURE
-- System: BRECON
-- Target Tables:
--   1. BRECON.VISA_EPIN (EPIN source)
--   2. BRECON.VISA_EPIN745 (EPIN745 / VisaNet source)
--   3. BRECON.CBS_TXN (Core Banking transaction dump)
--   4. BRECON.SWITCH_POS (Point of Sale switch transaction log)
--   5. BRECON.SWITCH_ATM (ATM switch transaction log)
--   6. BRECON.RECON_TXN (Master 3-Way Reconciliation & Settlement table)
-- =============================================================================

CREATE OR REPLACE PROCEDURE BRECON.SP_EXECUTE_VISA_RECON (
    P_RECON_DATE IN VARCHAR2,
    O_PROCESSED_COUNT OUT NUMBER
) AS
    V_COUNT NUMBER := 0;
BEGIN
    -- 0. Clear previous reconciliation records
    IF P_RECON_DATE IS NOT NULL AND TRIM(P_RECON_DATE) IS NOT NULL THEN
        DELETE FROM BRECON.RECON_TXN 
        WHERE TRAN_DT = TRIM(P_RECON_DATE) 
           OR TRAN_DT LIKE TRIM(P_RECON_DATE) || '%';
    ELSE
        DELETE FROM BRECON.RECON_TXN;
    END IF;

    --------------------------------------------------------------------------------
    -- STEP 1: Populate Auth ID in CBS Records
    -- CBS.RRN = SWITCH.RRN -> Fetch and update: CBS.AUTH_ID = SWITCH.AUTH_ID
    -- Using records from:
    --   * SWITCH_POS (AUTHX_APPRV_CDE)
    --   * SWITCH_ATM (AUTHX_AUTH_ID_RESP)
    --------------------------------------------------------------------------------
    MERGE INTO BRECON.CBS_TXN c
    USING (
        SELECT TRIM(AUTHX_SEQ_NUM) AS RRN, MAX(TRIM(AUTHX_APPRV_CDE)) AS AUTH_ID 
        FROM BRECON.SWITCH_POS 
        WHERE AUTHX_APPRV_CDE IS NOT NULL AND TRIM(AUTHX_APPRV_CDE) IS NOT NULL
          AND AUTHX_SEQ_NUM IS NOT NULL
        GROUP BY TRIM(AUTHX_SEQ_NUM)
        UNION
        SELECT TRIM(COALESCE(NULLIF(AUTHX_ORIG_OSEQ_NUM, '0'), AUTHX_SEQ_NUM)) AS RRN, 
               MAX(TRIM(AUTHX_AUTH_ID_RESP)) AS AUTH_ID 
        FROM BRECON.SWITCH_ATM 
        WHERE AUTHX_AUTH_ID_RESP IS NOT NULL AND TRIM(AUTHX_AUTH_ID_RESP) IS NOT NULL
          AND (AUTHX_SEQ_NUM IS NOT NULL OR AUTHX_ORIG_OSEQ_NUM IS NOT NULL)
        GROUP BY TRIM(COALESCE(NULLIF(AUTHX_ORIG_OSEQ_NUM, '0'), AUTHX_SEQ_NUM))
    ) s
    ON (TRIM(c.PSTD_USER_ID) = s.RRN OR TRIM(c.REF_NUM) = s.RRN)
    WHEN MATCHED THEN
    UPDATE SET c.AUTH_ID = s.AUTH_ID 
    WHERE c.AUTH_ID IS NULL;

    --------------------------------------------------------------------------------
    -- STEP 2 & 3: Master Data Population into RECON_TXN with Composite Key
    -- Composite Key: Card_Number + RRN + Auth_ID + Amount + Transaction_Date
    -- Ingests records from:
    --   A) Switch (POS + ATM) -> SOURCE_SWITCH = 'Y'
    --   B) CBS (CBS_TXN not in Switch) -> SOURCE_CBS = 'Y'
    --   C) VISA (VISA_EPIN + VISA_EPIN745 not in Switch/CBS) -> SOURCE_VISA = 'Y'
    --------------------------------------------------------------------------------

    -- 3.A Populate Switch Records
    INSERT INTO BRECON.RECON_TXN (
        RECON_ID,
        CARD_NUMBER,
        RRN,
        AUTH_ID,
        AMOUNT,
        TRAN_DT,
        SOURCE_VISA,
        SOURCE_CBS,
        SOURCE_SWITCH,
        RECON_STATUS,
        AUTHX_RESP_CDE,
        AUTHX_TYP,
        RECON_KEY,
        PROCESSED_TIME
    )
    SELECT 
        BRECON.SEQ_RECON_TXN.NEXTVAL,
        T.CARD_NUMBER,
        T.RRN,
        T.AUTH_ID,
        T.AMOUNT,
        T.TRAN_DT,
        'N' AS SOURCE_VISA,
        'N' AS SOURCE_CBS,
        'Y' AS SOURCE_SWITCH,
        'PENDING' AS RECON_STATUS,
        T.AUTHX_RESP_CDE,
        T.AUTHX_TYP,
        T.CARD_NUMBER || '|' || T.RRN || '|' || T.AUTH_ID || '|' || T.AMOUNT || '|' || T.TRAN_DT AS RECON_KEY,
        SYSDATE
    FROM (
        SELECT 
            CARD_NUMBER, RRN, AUTH_ID, AMOUNT, TRAN_DT, AUTHX_RESP_CDE, AUTHX_TYP,
            ROW_NUMBER() OVER (
                PARTITION BY CARD_NUMBER, RRN, AUTH_ID, AMOUNT, TRAN_DT
                ORDER BY CASE WHEN AUTHX_TYP IN ('420', '0420') THEN 1 ELSE 2 END,
                         CASE WHEN AUTHX_RESP_CDE NOT IN ('00', '01') THEN 1 ELSE 2 END
            ) AS RN
        FROM (
            -- Switch POS
            SELECT 
                COALESCE(UPPER(TRIM(HEADX_CRD_CARD_CRD_NUM)), 'UNKNOWN') AS CARD_NUMBER,
                COALESCE(TRIM(AUTHX_SEQ_NUM), 'UNKNOWN') AS RRN,
                COALESCE(TRIM(AUTHX_APPRV_CDE), 'N/A') AS AUTH_ID,
                COALESCE(TRIM(TO_CHAR(TO_NUMBER(NULLIF(REGEXP_REPLACE(AUTHX_AMT_1, '[^0-9.]', ''), '')), 'FM999999999990.00')), '0.00') AS AMOUNT,
                COALESCE(TRIM(TRAN_DT), 'UNKNOWN') AS TRAN_DT,
                TRIM(AUTHX_RESP_CDE) AS AUTHX_RESP_CDE,
                TRIM(AUTHX_TYP) AS AUTHX_TYP
            FROM BRECON.SWITCH_POS
            WHERE AUTHX_SEQ_NUM IS NOT NULL
              AND (P_RECON_DATE IS NULL OR TRIM(P_RECON_DATE) IS NULL OR TRAN_DT = TRIM(P_RECON_DATE) OR TRAN_DT LIKE TRIM(P_RECON_DATE) || '%')
            
            UNION ALL
            
            -- Switch ATM
            SELECT 
                COALESCE(UPPER(TRIM(HEADX_CRD_PAN)), 'UNKNOWN') AS CARD_NUMBER,
                COALESCE(TRIM(COALESCE(NULLIF(AUTHX_ORIG_OSEQ_NUM, '0'), AUTHX_SEQ_NUM)), 'UNKNOWN') AS RRN,
                COALESCE(TRIM(AUTHX_AUTH_ID_RESP), 'N/A') AS AUTH_ID,
                COALESCE(TRIM(TO_CHAR(TO_NUMBER(NULLIF(REGEXP_REPLACE(AUTHX_AMT_1, '[^0-9.]', ''), '')), 'FM999999999990.00')), '0.00') AS AMOUNT,
                COALESCE(TRIM(TXN_DT), 'UNKNOWN') AS TRAN_DT,
                TRIM(COALESCE(AUTHX_RESP_CDE_R_RESP_BYTE_1, '00')) AS AUTHX_RESP_CDE,
                TRIM(AUTHX_TYP) AS AUTHX_TYP
            FROM BRECON.SWITCH_ATM
            WHERE (AUTHX_SEQ_NUM IS NOT NULL OR AUTHX_ORIG_OSEQ_NUM IS NOT NULL)
              AND (P_RECON_DATE IS NULL OR TRIM(P_RECON_DATE) IS NULL OR TXN_DT = TRIM(P_RECON_DATE) OR TXN_DT LIKE TRIM(P_RECON_DATE) || '%')
        )
    ) T
    WHERE T.RN = 1
      AND NOT EXISTS (
          SELECT 1 FROM BRECON.RECON_TXN r 
          WHERE r.CARD_NUMBER = T.CARD_NUMBER 
            AND r.RRN = T.RRN 
            AND r.AUTH_ID = T.AUTH_ID 
            AND r.AMOUNT = T.AMOUNT 
            AND r.TRAN_DT = T.TRAN_DT
      );

    -- 3.B Populate CBS records that are missing in Switch
    INSERT INTO BRECON.RECON_TXN (
        RECON_ID,
        CARD_NUMBER,
        RRN,
        AUTH_ID,
        AMOUNT,
        TRAN_DT,
        SOURCE_VISA,
        SOURCE_CBS,
        SOURCE_SWITCH,
        RECON_STATUS,
        AUTHX_RESP_CDE,
        AUTHX_TYP,
        RECON_KEY,
        PROCESSED_TIME,
        REASON
    )
    SELECT 
        BRECON.SEQ_RECON_TXN.NEXTVAL,
        T.CARD_NUMBER,
        T.RRN,
        T.AUTH_ID,
        T.AMOUNT,
        T.TRAN_DT,
        'N' AS SOURCE_VISA,
        'Y' AS SOURCE_CBS,
        'N' AS SOURCE_SWITCH,
        'PENDING_INVESTIGATION' AS RECON_STATUS,
        '00' AS AUTHX_RESP_CDE,
        '200' AS AUTHX_TYP,
        T.CARD_NUMBER || '|' || T.RRN || '|' || T.AUTH_ID || '|' || T.AMOUNT || '|' || T.TRAN_DT AS RECON_KEY,
        SYSDATE,
        'Missing in Switch'
    FROM (
        SELECT 
            COALESCE(UPPER(TRIM(c.PRNT_ADVC_IND)), UPPER(TRIM(c.ACID)), 'UNKNOWN') AS CARD_NUMBER,
            COALESCE(TRIM(c.PSTD_USER_ID), TRIM(c.REF_NUM), 'UNKNOWN') AS RRN,
            COALESCE(TRIM(c.AUTH_ID), 'N/A') AS AUTH_ID,
            COALESCE(TRIM(TO_CHAR(TO_NUMBER(NULLIF(REGEXP_REPLACE(c.TRAN_AMT, '[^0-9.]', ''), '')), 'FM999999999990.00')), '0.00') AS AMOUNT,
            COALESCE(TRIM(c.TRAN_DATE), 'UNKNOWN') AS TRAN_DT,
            ROW_NUMBER() OVER (
                PARTITION BY COALESCE(UPPER(TRIM(c.PRNT_ADVC_IND)), UPPER(TRIM(c.ACID)), 'UNKNOWN'),
                             COALESCE(TRIM(c.PSTD_USER_ID), TRIM(c.REF_NUM), 'UNKNOWN'),
                             COALESCE(TRIM(c.AUTH_ID), 'N/A'),
                             COALESCE(TRIM(TO_CHAR(TO_NUMBER(NULLIF(REGEXP_REPLACE(c.TRAN_AMT, '[^0-9.]', ''), '')), 'FM999999999990.00')), '0.00'),
                             COALESCE(TRIM(c.TRAN_DATE), 'UNKNOWN')
                ORDER BY c.TRAN_ID
            ) AS RN
        FROM BRECON.CBS_TXN c
        WHERE (c.PSTD_USER_ID IS NOT NULL OR c.REF_NUM IS NOT NULL)
          AND (P_RECON_DATE IS NULL OR TRIM(P_RECON_DATE) IS NULL OR c.TRAN_DATE = TRIM(P_RECON_DATE) OR c.TRAN_DATE LIKE TRIM(P_RECON_DATE) || '%')
    ) T
    WHERE T.RN = 1
      AND NOT EXISTS (
          SELECT 1 FROM BRECON.RECON_TXN r 
          WHERE r.RRN = T.RRN
      );

    --------------------------------------------------------------------------------
    -- STEP 4: Settlement Matching Logic
    -- Match records across: VISA (EPIN + EPIN745) AND CBS AND SWITCH (POS + ATM)
    -- Matching Criteria: Card Number + RRN + Auth ID + Amount + Transaction Date
    --------------------------------------------------------------------------------
    
    -- 4.1 Match with CBS
    UPDATE BRECON.RECON_TXN r
    SET r.SOURCE_CBS = 'Y'
    WHERE EXISTS (
        SELECT 1 FROM BRECON.CBS_TXN c
        WHERE (TRIM(c.PSTD_USER_ID) = r.RRN OR TRIM(c.REF_NUM) = r.RRN)
          AND (
              TO_NUMBER(NULLIF(REGEXP_REPLACE(c.TRAN_AMT, '[^0-9.]', ''), '')) = TO_NUMBER(NULLIF(REGEXP_REPLACE(r.AMOUNT, '[^0-9.]', ''), ''))
              OR c.TRAN_AMT IS NULL
          )
    );

    -- 4.2 Match with VISA (Consolidated VISA_EPIN + VISA_EPIN745)
    UPDATE BRECON.RECON_TXN r
    SET r.SOURCE_VISA = 'Y'
    WHERE EXISTS (
        -- Match with EPIN745 via RRN & Amount
        SELECT 1 FROM BRECON.VISA_EPIN745 v
        WHERE TRIM(v.RET_REF_NUM) = r.RRN
          AND (
              v.TRAN_AMOUNT = TO_NUMBER(NULLIF(REGEXP_REPLACE(r.AMOUNT, '[^0-9.]', ''), ''))
              OR v.TRAN_AMOUNT IS NULL
          )
        UNION ALL
        -- Match with EPIN via Auth Code & Amount
        SELECT 1 FROM BRECON.VISA_EPIN e
        WHERE TRIM(e.AUTH_CODE) = r.AUTH_ID 
          AND r.AUTH_ID <> 'N/A'
          AND (
              e.DEST_AMT = TO_NUMBER(NULLIF(REGEXP_REPLACE(r.AMOUNT, '[^0-9.]', ''), ''))
              OR e.DEST_AMT IS NULL
          )
    );

    -- 4.3 Mark Settled where matching record exists in all three sources
    UPDATE BRECON.RECON_TXN
    SET RECON_STATUS = 'SETTLED',
        REASON = 'Settled - Matched across VISA, CBS, and Switch'
    WHERE SOURCE_VISA = 'Y'
      AND SOURCE_CBS = 'Y'
      AND SOURCE_SWITCH = 'Y';

    --------------------------------------------------------------------------------
    -- STEP 5: Exception / Failed Transaction Logic (Failed Refund Report)
    -- Conditions:
    --   1. Transaction is not reconciled: RECON_STATUS <> 'SETTLED'
    --   2. Response code is not successful: authx_resp_cde NOT IN ('00', '01', '0', '1')
    --   3. Transaction type indicates reversal: authx_typ IN ('420', '0420')
    --   4. Reversal Not Received (Customer was debited or transaction dropped)
    --------------------------------------------------------------------------------
    UPDATE BRECON.RECON_TXN r
    SET r.RECON_STATUS = 'FAILED_REFUND',
        r.REASON = 'Reversal Not Received'
    WHERE r.RECON_STATUS <> 'SETTLED'
      AND TRIM(r.AUTHX_TYP) IN ('420', '0420')
      AND (
          LPAD(TRIM(r.AUTHX_RESP_CDE), 2, '0') NOT IN ('00', '01')
          OR r.AUTHX_RESP_CDE IS NULL
      );

    --------------------------------------------------------------------------------
    -- STEP 6: Categorization of Remaining Unmatched Records (Reason Taxonomy)
    -- Categories:
    --   * Missing in CBS
    --   * Missing in VISA
    --   * Missing in Switch
    --   * Amount Mismatch
    --   * Date Mismatch
    --   * Auth ID Mismatch
    --   * RRN Mismatch
    --------------------------------------------------------------------------------
    
    -- 6.1 Check for Amount Mismatch (RRN matched with CBS or VISA, but amount differs)
    UPDATE BRECON.RECON_TXN r
    SET r.RECON_STATUS = 'PENDING_INVESTIGATION',
        r.REASON = 'Amount Mismatch'
    WHERE r.RECON_STATUS = 'PENDING'
      AND r.SOURCE_CBS = 'N'
      AND EXISTS (
          SELECT 1 FROM BRECON.CBS_TXN c
          WHERE (TRIM(c.PSTD_USER_ID) = r.RRN OR TRIM(c.REF_NUM) = r.RRN)
            AND TO_NUMBER(NULLIF(REGEXP_REPLACE(c.TRAN_AMT, '[^0-9.]', ''), '')) <> TO_NUMBER(NULLIF(REGEXP_REPLACE(r.AMOUNT, '[^0-9.]', ''), ''))
      );

    -- 6.2 Check for Missing in CBS (Approved in Switch & settled in VISA, but missing in CBS)
    UPDATE BRECON.RECON_TXN r
    SET r.RECON_STATUS = 'PENDING_INVESTIGATION',
        r.REASON = 'Missing in CBS'
    WHERE r.RECON_STATUS = 'PENDING'
      AND r.SOURCE_CBS = 'N'
      AND r.SOURCE_VISA = 'Y'
      AND r.SOURCE_SWITCH = 'Y';

    -- 6.3 Check for Missing in VISA (Debited in CBS & Switch, but not in VISA clearing)
    UPDATE BRECON.RECON_TXN r
    SET r.RECON_STATUS = 'PENDING_INVESTIGATION',
        r.REASON = 'Missing in VISA'
    WHERE r.RECON_STATUS = 'PENDING'
      AND r.SOURCE_VISA = 'N'
      AND r.SOURCE_CBS = 'Y'
      AND r.SOURCE_SWITCH = 'Y';

    -- 6.4 Missing in Switch
    UPDATE BRECON.RECON_TXN r
    SET r.RECON_STATUS = 'PENDING_INVESTIGATION',
        r.REASON = 'Missing in Switch'
    WHERE r.RECON_STATUS = 'PENDING'
      AND r.SOURCE_SWITCH = 'N';

    -- 6.5 Remaining unmatched transactions
    UPDATE BRECON.RECON_TXN r
    SET r.RECON_STATUS = 'PENDING_INVESTIGATION',
        r.REASON = CASE 
            WHEN r.SOURCE_VISA = 'N' AND r.SOURCE_CBS = 'N' AND r.SOURCE_SWITCH = 'Y' THEN 'Missing in CBS and VISA'
            ELSE COALESCE(r.REASON, 'Pending Investigation')
        END
    WHERE r.RECON_STATUS = 'PENDING';

    SELECT COUNT(*) INTO V_COUNT FROM BRECON.RECON_TXN;
    O_PROCESSED_COUNT := V_COUNT;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'Error in SP_EXECUTE_VISA_RECON: ' || SQLERRM);
END SP_EXECUTE_VISA_RECON;
/
