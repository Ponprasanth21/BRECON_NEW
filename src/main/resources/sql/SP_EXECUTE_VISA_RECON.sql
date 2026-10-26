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

-- Ensure AUTH_ID column exists in CBS_TXN
-- ALTER TABLE BRECON.CBS_TXN ADD (AUTH_ID VARCHAR2(100));

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
    -- Using records from SWITCH_POS (AUTHX_APPRV_CDE) and SWITCH_ATM (AUTHX_AUTH_ID_RESP)
    --------------------------------------------------------------------------------
    MERGE INTO BRECON.CBS_TXN c
    USING (
        SELECT TRIM(AUTHX_SEQ_NUM) AS RRN, MAX(TRIM(AUTHX_APPRV_CDE)) AS AUTH_ID 
        FROM BRECON.SWITCH_POS 
        WHERE AUTHX_APPRV_CDE IS NOT NULL AND TRIM(AUTHX_APPRV_CDE) IS NOT NULL
          AND AUTHX_SEQ_NUM IS NOT NULL
        GROUP BY TRIM(AUTHX_SEQ_NUM)
        UNION
        SELECT TRIM(COALESCE(NULLIF(AUTHX_ORIG_OSEQ_NUM, '0'), AUTHX_SEQ_NUM)) AS RRN, MAX(TRIM(AUTHX_AUTH_ID_RESP)) AS AUTH_ID 
        FROM BRECON.SWITCH_ATM 
        WHERE AUTHX_AUTH_ID_RESP IS NOT NULL AND TRIM(AUTHX_AUTH_ID_RESP) IS NOT NULL
          AND (AUTHX_SEQ_NUM IS NOT NULL OR AUTHX_ORIG_OSEQ_NUM IS NOT NULL)
        GROUP BY TRIM(COALESCE(NULLIF(AUTHX_ORIG_OSEQ_NUM, '0'), AUTHX_SEQ_NUM))
    ) s
    ON (c.PSTD_USER_ID = s.RRN OR c.REF_NUM = s.RRN)
    WHEN MATCHED THEN
    UPDATE SET c.AUTH_ID = s.AUTH_ID 
    WHERE c.AUTH_ID IS NULL;

    --------------------------------------------------------------------------------
    -- STEP 2 & 3: Insert Consolidated & Enriched Data into RECON_TXN with RECON_KEY
    -- Composite key: Card Number + RRN + Auth ID + Amount + Transaction Date
    -- Deduplicated by composite key to satisfy UK_RECON_TXN_COMPOSITE
    --------------------------------------------------------------------------------
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
                ORDER BY CASE WHEN AUTHX_TYP = '420' THEN 1 ELSE 2 END,
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

    --------------------------------------------------------------------------------
    -- STEP 4: Settlement Matching Logic
    -- Match records across: VISA (EPIN + EPIN745) AND CBS AND SWITCH (POS + ATM)
    --------------------------------------------------------------------------------
    -- 4.1 Match with CBS
    UPDATE BRECON.RECON_TXN r
    SET r.SOURCE_CBS = 'Y'
    WHERE EXISTS (
        SELECT 1 FROM BRECON.CBS_TXN c
        WHERE (c.PSTD_USER_ID = r.RRN OR c.REF_NUM = r.RRN)
          AND (
              c.AUTH_ID IS NULL 
              OR r.AUTH_ID IS NULL 
              OR c.AUTH_ID = r.AUTH_ID
          )
    );

    -- 4.2 Match with VISA (Consolidated VISA_EPIN + VISA_EPIN745)
    UPDATE BRECON.RECON_TXN r
    SET r.SOURCE_VISA = 'Y'
    WHERE EXISTS (
        SELECT 1 FROM BRECON.VISA_EPIN745 v
        WHERE v.RET_REF_NUM = r.RRN
        UNION ALL
        SELECT 1 FROM BRECON.VISA_EPIN e
        WHERE e.AUTH_CODE = r.AUTH_ID AND e.AUTH_CODE IS NOT NULL
    );

    -- 4.3 Mark Settled where matching record exists in all three sources
    UPDATE BRECON.RECON_TXN
    SET RECON_STATUS = 'SETTLED',
        REASON = 'Settled - Matched across VISA, CBS, and Switch'
    WHERE SOURCE_VISA = 'Y'
      AND SOURCE_CBS = 'Y'
      AND SOURCE_SWITCH = 'Y';

    --------------------------------------------------------------------------------
    -- STEP 5: Exception / Failed Transaction Logic
    -- Condition: RECON_STATUS <> 'SETTLED' AND authx_resp_cde NOT IN ('00','01') AND authx_typ = '420'
    --------------------------------------------------------------------------------
    UPDATE BRECON.RECON_TXN r
    SET r.RECON_STATUS = 'FAILED_REFUND',
        r.REASON = 'Reversal Not Received'
    WHERE r.RECON_STATUS <> 'SETTLED'
      AND r.AUTHX_TYP = '420'
      AND (r.AUTHX_RESP_CDE NOT IN ('00', '01') OR r.AUTHX_RESP_CDE IS NULL);

    -- Pending Investigation for remaining unmatched
    UPDATE BRECON.RECON_TXN
    SET RECON_STATUS = 'PENDING_INVESTIGATION',
        REASON = CASE 
            WHEN SOURCE_CBS = 'N' AND SOURCE_VISA = 'Y' THEN 'Missing in CBS'
            WHEN SOURCE_VISA = 'N' AND SOURCE_CBS = 'Y' THEN 'Missing in VISA'
            WHEN SOURCE_VISA = 'N' AND SOURCE_CBS = 'N' THEN 'Missing in CBS and VISA'
            ELSE 'Pending Investigation'
        END
    WHERE RECON_STATUS = 'PENDING';

    SELECT COUNT(*) INTO V_COUNT FROM BRECON.RECON_TXN;
    O_PROCESSED_COUNT := V_COUNT;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'Error in SP_EXECUTE_VISA_RECON: ' || SQLERRM);
END SP_EXECUTE_VISA_RECON;
