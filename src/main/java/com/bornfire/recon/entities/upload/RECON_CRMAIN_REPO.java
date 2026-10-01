package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface RECON_CRMAIN_REPO extends CrudRepository<RECON_CRMAIN_ENTITY,BigDecimal> {
    @Query(value = "SELECT * FROM BRECON_CRMAIN_TABLE WHERE DEL_FLG ='N' AND TRUNC(s_recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
    List<RECON_CRMAIN_ENTITY> getlist();
    
	@Query(value = "SELECT A.* , TO_CHAR(S_TRXN_DATE, 'DD-MM-YY') as tran_date,\r\n"
			+ "TO_CHAR(S_POSTING_DATE, 'DD-MM-YY') as post_date\r\n" + "FROM BRECON_CRMAIN_TABLE  A\r\n"
			+ "WHERE DEL_FLG ='N' \r\n"
			+ "AND TRUNC(S_RECON_PROCESS_DATE) = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getMainListByDate(@Param("recon_process_date") String recon_process_date);
    
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO BRECON_CRMAIN_TABLE (\r\n" + 
    		"    S_ARN, S_TR_HIS_ID, S_TRXN_DATE, S_TRXN_TIME, S_POSTING_DATE, S_CARD_NUMBER,\r\n" + 
    		"    S_ACCT_CURRENCY, S_SOURCE_CURR, S_SOURCE_AMT, S_BILL_CURR, S_MC_BILL_AMT,\r\n" + 
    		"    S_SV_BILL_AMT, S_SETT_CURR, S_SETT_AMT, S_MARKUP_AMNT_ICCR, S_TRANSACTION_IND,\r\n" + 
    		"    S_TRANSACTION_FEE, S_I_FEE_IND, S_SIGN_IFEE, S_TERM_TYPE, S_TRANSACTION_TYPE,\r\n" + 
    		"    S_TRANSACTION_DESCRIPTION, S_TYPE_OF_CARD, S_MERCHANT_NAME, S_MERCHANT_COUNTRY,\r\n" + 
    		"    S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_FLG,\r\n" + 
    		"    D_BO_UTRNNO, D_TAA_DATE, D_TRXN_DATE, D_TRXN_TIME, D_POST_DATE, D_CARD_NUMBER,\r\n" + 
    		"    D_TRXN_TYPE, D_ACCT_CURRENCY, D_SOURCE_CURR, D_SOURCE_AMT, D_SETT_CURR,\r\n" + 
    		"    D_SETT_AMT, D_BILL_CURR, D_MC_BILL_AMT, D_SV_BILL_AMT, D_TRXN_FEE,\r\n" + 
    		"    D_TRANSACTION_IND, D_I_FEE, D_I_FEE_IND, D_AUTH_CODE, D_MERCHANT_NAME,\r\n" + 
    		"    D_EXCEPTION, D_ARN, D_CYCLE_NUMBER, D_MCC, D_COUNTRY, D_REFNUM,\r\n" + 
    		"    D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_FLG,\r\n" + 
    		"    SRL_NO, ENTRY_TIME, ENTRY_USER,  DEL_FLG, D_CCARD_NUM,\r\n" + 
    		"    S_ABS_ACCT_NUMBER,S_RECON_PROCESS_DATE,D_RECON_PROCESS_DATE\r\n" + 
    		")\r\n" + 
    		"WITH source_dedup AS (\r\n" + 
    		"    SELECT * FROM (\r\n" + 
    		"        SELECT S.*, ROW_NUMBER() OVER (PARTITION BY ARN ORDER BY TRXN_DATE DESC) AS rn\r\n" + 
    		"        FROM BRECON_CRFILE_SOURCE_TABLE S\r\n" + 
    		"    ) WHERE rn = 1\r\n" + 
    		"),\r\n" + 
    		"destination_dedup AS (\r\n" + 
    		"    SELECT * FROM (\r\n" + 
    		"        SELECT D.*, ROW_NUMBER() OVER (PARTITION BY ARN ORDER BY TRXN_DATE DESC) AS rn\r\n" + 
    		"        FROM BRECON_CRFILE_DESTINATION_TABLE D\r\n" + 
    		"    ) WHERE rn = 1\r\n" + 
    		")\r\n" + 
    		"SELECT\r\n" + 
    		"    S.ARN, S.TR_HIS_ID, S.TRXN_DATE, S.TRXN_TIME, S.POSTING_DATE, S.CARD_NUMBER,\r\n" + 
    		"    S.ACCT_CURRENCY, S.SOURCE_CURR, S.SOURCE_AMT, S.BILL_CURR, S.MC_BILL_AMT,\r\n" + 
    		"    S.SV_BILL_AMT, S.SETT_CURR, S.SETT_AMT, S.MARKUP_AMNT_ICCR, S.TRANSACTION_IND,\r\n" + 
    		"    S.TRANSACTION_FEE, S.I_FEE_IND, S.SIGN_IFEE, S.TERM_TYPE, S.TRANSACTION_TYPE,\r\n" + 
    		"    S.TRANSACTION_DESCRIPTION, S.TYPE_OF_CARD, S.MERCHANT_NAME, S.MERCHANT_COUNTRY,\r\n" + 
    		"    S.RECON_TRAN_DATE, S.RECON_TYPE, 'Y',\r\n" + 
    		"    D.BO_UTRNNO, D.TAA_DATE, D.TRXN_DATE, D.TRXN_TIME, D.POST_DATE, D.CARD_NUMBER,\r\n" + 
    		"    D.TRXN_TYPE, D.ACCT_CURRENCY, D.SOURCE_CURR, D.SOURCE_AMT, D.SETT_CURR,\r\n" + 
    		"    D.SETT_AMT, D.BILL_CURR, D.MC_BILL_AMT, D.SV_BILL_AMT, D.TRXN_FEE,\r\n" + 
    		"    D.TRANSACTION_IND, D.I_FEE, D.I_FEE_IND, D.AUTH_CODE, D.MERCHANT_NAME,\r\n" + 
    		"    D.EXCEPTION, D.ARN, D.CYCLE_NUMBER, D.MCC, D.COUNTRY, D.REFNUM,\r\n" + 
    		"    D.RECON_TRAN_DATE, D.RECON_TYPE, 'Y',\r\n" + 
    		"    SEQ_RECON_TXN.NEXTVAL, SYSDATE, :entryUser, 'N', D.CCARD_NUM,\r\n" + 
    		"    S.ABS_ACCT_NUMBER,SYSDATE,SYSDATE\r\n" + 
    		"FROM source_dedup S\r\n" + 
    		"JOIN destination_dedup D ON S.ARN = D.ARN\r\n" + 
    		"WHERE S.ARN = :arnNo",
           nativeQuery = true)
    void insertReconArn( String arnNo, String entryUser);

    @Modifying
    @Transactional 
	@Query(value = "INSERT INTO BRECON_CRMAIN_TABLE (\r\n" + 
			"    S_ARN, S_TR_HIS_ID, S_TRXN_DATE, S_TRXN_TIME, S_POSTING_DATE, S_CARD_NUMBER,\r\n" + 
			"    S_ACCT_CURRENCY, S_SOURCE_CURR, S_SOURCE_AMT, S_BILL_CURR, S_MC_BILL_AMT,\r\n" + 
			"    S_SV_BILL_AMT, S_SETT_CURR, S_SETT_AMT, S_MARKUP_AMNT_ICCR, S_TRANSACTION_IND,\r\n" + 
			"    S_TRANSACTION_FEE, S_I_FEE_IND, S_SIGN_IFEE, S_TERM_TYPE, S_TRANSACTION_TYPE,\r\n" + 
			"    S_TRANSACTION_DESCRIPTION, S_TYPE_OF_CARD, S_MERCHANT_NAME, S_MERCHANT_COUNTRY,\r\n" + 
			"    S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_FLG,\r\n" + 
			"    D_BO_UTRNNO, D_TAA_DATE, D_TRXN_DATE, D_TRXN_TIME, D_POST_DATE, D_CARD_NUMBER,\r\n" + 
			"    D_TRXN_TYPE, D_ACCT_CURRENCY, D_SOURCE_CURR, D_SOURCE_AMT, D_SETT_CURR,\r\n" + 
			"    D_SETT_AMT, D_BILL_CURR, D_MC_BILL_AMT, D_SV_BILL_AMT, D_TRXN_FEE,\r\n" + 
			"    D_TRANSACTION_IND, D_I_FEE, D_I_FEE_IND, D_AUTH_CODE, D_MERCHANT_NAME,\r\n" + 
			"    D_EXCEPTION, D_ARN, D_CYCLE_NUMBER, D_MCC, D_COUNTRY, D_REFNUM,\r\n" + 
			"    D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_FLG,\r\n" + 
			"    SRL_NO, ENTRY_TIME, ENTRY_USER, DEL_FLG, D_CCARD_NUM,\r\n" + 
			"    S_ABS_ACCT_NUMBER, S_RECON_PROCESS_DATE, D_RECON_PROCESS_DATE\r\n" + 
			")\r\n" + 
			"WITH source_dedup AS (\r\n" + 
			"    SELECT *\r\n" + 
			"    FROM (\r\n" + 
			"        SELECT S.*, ROW_NUMBER() OVER (PARTITION BY ARN ORDER BY TRXN_DATE DESC) AS rn\r\n" + 
			"        FROM BRECON_CRFILE_SOURCE_TABLE S\r\n" + 
			"    )\r\n" + 
			"    WHERE rn = 1\r\n" + 
			"),\r\n" + 
			"destination_dedup AS (\r\n" + 
			"    SELECT *\r\n" + 
			"    FROM (\r\n" + 
			"        SELECT D.*, ROW_NUMBER() OVER (PARTITION BY ARN ORDER BY TRXN_DATE DESC) AS rn\r\n" + 
			"        FROM BRECON_CRFILE_DESTINATION_TABLE D\r\n" + 
			"    )\r\n" + 
			"    WHERE rn = 1\r\n" + 
			")\r\n" + 
			"SELECT\r\n" + 
			"    S.ARN, S.TR_HIS_ID, S.TRXN_DATE, S.TRXN_TIME, S.POSTING_DATE, S.CARD_NUMBER,\r\n" + 
			"    S.ACCT_CURRENCY, S.SOURCE_CURR, S.SOURCE_AMT, S.BILL_CURR, S.MC_BILL_AMT,\r\n" + 
			"    S.SV_BILL_AMT, S.SETT_CURR, S.SETT_AMT, S.MARKUP_AMNT_ICCR, S.TRANSACTION_IND,\r\n" + 
			"    S.TRANSACTION_FEE, S.I_FEE_IND, S.SIGN_IFEE, S.TERM_TYPE, S.TRANSACTION_TYPE,\r\n" + 
			"    S.TRANSACTION_DESCRIPTION, S.TYPE_OF_CARD, S.MERCHANT_NAME, S.MERCHANT_COUNTRY,\r\n" + 
			"    S.RECON_TRAN_DATE, S.RECON_TYPE, 'Y',\r\n" + 
			"    D.BO_UTRNNO, D.TAA_DATE, D.TRXN_DATE, D.TRXN_TIME, D.POST_DATE, D.CARD_NUMBER,\r\n" + 
			"    D.TRXN_TYPE, D.ACCT_CURRENCY, D.SOURCE_CURR, D.SOURCE_AMT, D.SETT_CURR,\r\n" + 
			"    D.SETT_AMT, D.BILL_CURR, D.MC_BILL_AMT, D.SV_BILL_AMT, D.TRXN_FEE,\r\n" + 
			"    D.TRANSACTION_IND, D.I_FEE, D.I_FEE_IND, D.AUTH_CODE, D.MERCHANT_NAME,\r\n" + 
			"    D.EXCEPTION, D.ARN, D.CYCLE_NUMBER, D.MCC, D.COUNTRY, D.REFNUM,\r\n" + 
			"    D.RECON_TRAN_DATE, D.RECON_TYPE, 'Y',\r\n" + 
			"    SEQ_RECON_TXN.NEXTVAL, SYSDATE, :entryUser, 'N', D.CCARD_NUM,\r\n" + 
			"    S.ABS_ACCT_NUMBER, TO_DATE(:fromDate,'DD-MM-YYYY'), TO_DATE(:fromDate,'DD-MM-YYYY')\r\n" + 
			"FROM source_dedup S\r\n" + 
			"JOIN destination_dedup D ON S.ARN = D.ARN\r\n" + 
			"WHERE S.DEL_FLG = 'N'\r\n" + 
			"  AND D.DEL_FLG = 'N'\r\n" + 
			"  AND NOT EXISTS (\r\n" + 
			"      SELECT 1 FROM BRECON_CRMAIN_TABLE M WHERE M.S_ARN = S.ARN\r\n" + 
			"  )", nativeQuery = true)
	int insertValueReconTb(@Param("entryUser") String entryUser,@Param("fromDate")  String  fromDate);
	
/// for referesh button
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO BRECON_CRMAIN_TABLE (\r\n" + 
			"    S_ARN, S_TR_HIS_ID, S_TRXN_DATE, S_TRXN_TIME, S_POSTING_DATE, S_CARD_NUMBER,\r\n" + 
			"    S_ACCT_CURRENCY, S_SOURCE_CURR, S_SOURCE_AMT, S_BILL_CURR, S_MC_BILL_AMT,\r\n" + 
			"    S_SV_BILL_AMT, S_SETT_CURR, S_SETT_AMT, S_MARKUP_AMNT_ICCR, S_TRANSACTION_IND,\r\n" + 
			"    S_TRANSACTION_FEE, S_I_FEE_IND, S_SIGN_IFEE, S_TERM_TYPE, S_TRANSACTION_TYPE,\r\n" + 
			"    S_TRANSACTION_DESCRIPTION, S_TYPE_OF_CARD, S_MERCHANT_NAME, S_MERCHANT_COUNTRY,\r\n" + 
			"    S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_FLG,\r\n" + 
			"    D_BO_UTRNNO, D_TAA_DATE, D_TRXN_DATE, D_TRXN_TIME, D_POST_DATE, D_CARD_NUMBER,\r\n" + 
			"    D_TRXN_TYPE, D_ACCT_CURRENCY, D_SOURCE_CURR, D_SOURCE_AMT, D_SETT_CURR,\r\n" + 
			"    D_SETT_AMT, D_BILL_CURR, D_MC_BILL_AMT, D_SV_BILL_AMT, D_TRXN_FEE,\r\n" + 
			"    D_TRANSACTION_IND, D_I_FEE, D_I_FEE_IND, D_AUTH_CODE, D_MERCHANT_NAME,\r\n" + 
			"    D_EXCEPTION, D_ARN, D_CYCLE_NUMBER, D_MCC, D_COUNTRY, D_REFNUM,\r\n" + 
			"    D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_FLG,\r\n" + 
			"    SRL_NO, ENTRY_TIME, ENTRY_USER,  DEL_FLG, D_CCARD_NUM,\r\n" + 
			"    S_ABS_ACCT_NUMBER,S_RECON_PROCESS_DATE,D_RECON_PROCESS_DATE\r\n" + 
			")\r\n" + 
			"WITH source_dedup AS (\r\n" + 
			"    SELECT * FROM (\r\n" + 
			"        SELECT S.*, ROW_NUMBER() OVER (PARTITION BY ARN ORDER BY TRXN_DATE DESC) AS rn\r\n" + 
			"        FROM BRECON_CRFILE_SOURCE_TABLE S\r\n" + 
			"    ) WHERE rn = 1\r\n" + 
			"),\r\n" + 
			"destination_dedup AS (\r\n" + 
			"    SELECT * FROM (\r\n" + 
			"        SELECT D.*, ROW_NUMBER() OVER (PARTITION BY ARN ORDER BY TRXN_DATE DESC) AS rn\r\n" + 
			"        FROM BRECON_CRFILE_DESTINATION_TABLE D\r\n" + 
			"    ) WHERE rn = 1\r\n" + 
			")\r\n" + 
			"SELECT\r\n" + 
			"    S.ARN, S.TR_HIS_ID, S.TRXN_DATE, S.TRXN_TIME, S.POSTING_DATE, S.CARD_NUMBER,\r\n" + 
			"    S.ACCT_CURRENCY, S.SOURCE_CURR, S.SOURCE_AMT, S.BILL_CURR, S.MC_BILL_AMT,\r\n" + 
			"    S.SV_BILL_AMT, S.SETT_CURR, S.SETT_AMT, S.MARKUP_AMNT_ICCR, S.TRANSACTION_IND,\r\n" + 
			"    S.TRANSACTION_FEE, S.I_FEE_IND, S.SIGN_IFEE, S.TERM_TYPE, S.TRANSACTION_TYPE,\r\n" + 
			"    S.TRANSACTION_DESCRIPTION, S.TYPE_OF_CARD, S.MERCHANT_NAME, S.MERCHANT_COUNTRY,\r\n" + 
			"    S.RECON_TRAN_DATE, S.RECON_TYPE, 'Y',\r\n" + 
			"    D.BO_UTRNNO, D.TAA_DATE, D.TRXN_DATE, D.TRXN_TIME, D.POST_DATE, D.CARD_NUMBER,\r\n" + 
			"    D.TRXN_TYPE, D.ACCT_CURRENCY, D.SOURCE_CURR, D.SOURCE_AMT, D.SETT_CURR,\r\n" + 
			"    D.SETT_AMT, D.BILL_CURR, D.MC_BILL_AMT, D.SV_BILL_AMT, D.TRXN_FEE,\r\n" + 
			"    D.TRANSACTION_IND, D.I_FEE, D.I_FEE_IND, D.AUTH_CODE, D.MERCHANT_NAME,\r\n" + 
			"    D.EXCEPTION, D.ARN, D.CYCLE_NUMBER, D.MCC, D.COUNTRY, D.REFNUM,\r\n" + 
			"    D.RECON_TRAN_DATE, D.RECON_TYPE, 'Y',\r\n" + 
			"    SEQ_RECON_TXN.NEXTVAL, SYSDATE, :entryUser, 'N', D.CCARD_NUM,\r\n" + 
			"    S.ABS_ACCT_NUMBER,SYSDATE,SYSDATE\r\n" + 
			"FROM source_dedup S\r\n" + 
			"JOIN destination_dedup D ON S.ARN = D.ARN"
			+ "where S.DEL_FLG ='N' AND D.DEL_FLG ='N' AND S.RECON_FLG ='N' AND D.RECON_FLG = 'N' \r\n" + 
			"", nativeQuery = true)
	void InsertMultiArn(String entryUser);
	
	
	    // FILTER
		@Query(value = "select * from BRECON_CRMAIN_TABLE where del_flg ='N' AND to_char(s_recon_process_date,'DD-MM-YYYY')=?1", nativeQuery = true)
		List<RECON_CRMAIN_ENTITY> getReconDate(String date);
}
