package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

public interface RECON_DRMAIN_REPO extends CrudRepository<RECON_DRMAIN_ENTITY, BigDecimal> {

	@Query(value = "SELECT * FROM RECON_DRMAIN_TABLE WHERE DEL_FLG ='N' AND TRUNC(s_recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
	List<RECON_DRMAIN_ENTITY> getlist();
	
	@Query(value = "SELECT * FROM RECON_DRMAIN_TABLE " + "WHERE DEL_FLG = 'N' "
			+ "AND TRUNC(S_RECON_PROCESS_DATE) = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getMainListByDate(@Param("recon_process_date") String recon_process_date);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "INSERT INTO RECON_DRMAIN_TABLE ( \r\n"
			+ "    S_TRN_DT, S_VALUE_DT, S_TRN_REF, S_CTRY, S_RELATED_ACCOUNT,\r\n"
			+ "    S_TRN_DESC, S_ADDL_TEXT, S_AC_CCY, S_DR_ORG, S_CR_ORG,\r\n"
			+ "    S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_FLG, S_RECON_PROCESS_DATE,\r\n"
			+ "    D_ARN, D_MCC, D_AUTH_AMNT, D_ACCT_AMNT, D_AUTH_CURRENCY,\r\n"
			+ "    D_TRANSACTION_DATE, D_TRANSACTION_AMOUNT, D_TRANSACTION_CURRENCY, D_TRANSACTION_TYPE,\r\n"
			+ "    D_CBS_ACCOUNT_NUMBER, D_CARD_NUMBER, D_CARD_TYPE, D_MERCHANT_DESC, D_COUNTRY_NAME,\r\n"
			+ "    D_APPROVAL_CODE, D_RECONCILE_AMOUNT, D_RECONCILE_CUR, D_FEES, D_I_FEE_IND,\r\n"
			+ "    D_REFNUM, D_DR_CR, D_MARKUP_VALUE, D_MARKUP_CURRENCY,\r\n"
			+ "    D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_FLG, D_RECON_PROCESS_DATE,\r\n"
			+ "    ENTITY_FLG, AUTH_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER,\r\n"
			+ "    MODIFY_USER, AUTH_USER, ENTRY_TIME, MODIFY_TIME, AUTH_TIME,\r\n" + "    SRL_NO, S_REF1, S_REF2 \r\n"
			+ ") \r\n" + "WITH source_dedup AS ( \r\n" + "    SELECT * FROM ( \r\n"
			+ "        SELECT S.*, ROW_NUMBER() OVER (PARTITION BY S.REF2 ORDER BY S.TRN_DT DESC) AS rn \r\n"
			+ "        FROM BRECON_DRFILE_SOURCE_TABLE S \r\n" + "    ) WHERE rn = 1 \r\n" + "), \r\n"
			+ "destination_dedup AS ( \r\n" + "    SELECT * FROM ( \r\n"
			+ "        SELECT D.*, ROW_NUMBER() OVER (PARTITION BY D.REFNUM ORDER BY D.TRANSACTION_DATE DESC) AS rn \r\n"
			+ "        FROM BRECON_DRFILE_DESTINATION_TABLE D \r\n" + "    ) WHERE rn = 1 \r\n" + ") \r\n"
			+ "SELECT \r\n" + "    S.TRN_DT, S.VALUE_DT, S.TRN_REF, S.CTRY, S.RELATED_ACCOUNT,\r\n"
			+ "    S.TRN_DESC, S.ADDL_TEXT, S.AC_CCY, S.DR_ORG, S.CR_ORG,\r\n"
			+ "    SYSDATE,                          -- S_RECON_TRAN_DATE \r\n" + "    S.RECON_TYPE, \r\n"
			+ "    'Y',                              -- S_RECON_FLG \r\n"
			+ "    TO_DATE(:fromDate,'DD-MM-YYYY'),  -- S_RECON_PROCESS_DATE \r\n"
			+ "    D.ARN, D.MCC, D.AUTH_AMNT, D.ACCT_AMNT, D.AUTH_CURRENCY,\r\n"
			+ "    D.TRANSACTION_DATE, D.TRANSACTION_AMOUNT, D.TRANSACTION_CURRENCY, D.TRANSACTION_TYPE,\r\n"
			+ "    D.CBS_ACCOUNT_NUMBER, D.CARD_NUMBER, D.CARD_TYPE, D.MERCHANT_DESC, D.COUNTRY_NAME,\r\n"
			+ "    D.APPROVAL_CODE, D.RECONCILE_AMOUNT, D.RECONCILE_CUR, D.FEES, D.I_FEE_IND,\r\n"
			+ "    D.REFNUM, D.DR_CR, D.MARKUP_VALUE, D.MARKUP_CURRENCY,\r\n"
			+ "    SYSDATE,                          -- D_RECON_TRAN_DATE \r\n" + "    D.RECON_TYPE, \r\n"
			+ "    'Y',                              -- D_RECON_FLG \r\n"
			+ "    TO_DATE(:fromDate,'DD-MM-YYYY'),  -- D_RECON_PROCESS_DATE \r\n"
			+ "    'N', 'N', 'N', 'N', :entryUser, \r\n" + "    :entryUser, :entryUser, SYSDATE, SYSDATE, SYSDATE, \r\n"
			+ "    SEQ_RECON_TXN.NEXTVAL, S.REF1, S.REF2 \r\n" + "FROM source_dedup S \r\n"
			+ "JOIN destination_dedup D \r\n" + "  ON S.REF2 = D.REFNUM \r\n"
			+ " AND D.AUTH_AMNT = D.TRANSACTION_AMOUNT \r\n" + "WHERE S.DEL_FLG = 'N' \r\n"
			+ "  AND D.DEL_FLG = 'N' \r\n" + "  AND NOT EXISTS ( \r\n"
			+ "      SELECT 1 FROM RECON_DRMAIN_TABLE M WHERE M.S_REF2 = S.REF2 \r\n" + "  )", nativeQuery = true)
	int insertValueReconTb(@Param("entryUser") String entryUser, @Param("fromDate") String fromDate);
	
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "INSERT INTO RECON_DRMAIN_TABLE ( "
			+ "    S_TRN_DT, S_VALUE_DT, S_TRN_REF, S_CTRY, S_RELATED_ACCOUNT, "
			+ "    S_TRN_DESC, S_ADDL_TEXT, S_AC_CCY, S_DR_ORG, S_CR_ORG, "
			+ "    S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_FLG, S_RECON_PROCESS_DATE, " +

			"    D_ARN, D_MCC, D_AUTH_AMNT, D_ACCT_AMNT, D_AUTH_CURRENCY, "
			+ "    D_TRANSACTION_DATE, D_TRANSACTION_AMOUNT, D_TRANSACTION_CURRENCY, D_TRANSACTION_TYPE, "
			+ "    D_CBS_ACCOUNT_NUMBER, D_CARD_NUMBER, D_CARD_TYPE, D_MERCHANT_DESC, D_COUNTRY_NAME, "
			+ "    D_APPROVAL_CODE, D_RECONCILE_AMOUNT, D_RECONCILE_CUR, D_FEES, D_I_FEE_IND, "
			+ "    D_REFNUM, D_DR_CR, D_MARKUP_VALUE, D_MARKUP_CURRENCY, "
			+ "    D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_FLG, D_RECON_PROCESS_DATE, " +

			"    ENTITY_FLG, AUTH_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER, "
			+ "    MODIFY_USER, AUTH_USER, ENTRY_TIME, MODIFY_TIME, AUTH_TIME, " + "    SRL_NO, S_REF1, S_REF2 " + ") "
			+

			"WITH source_dedup AS ( " + "    SELECT * FROM ( "
			+ "        SELECT S.*, ROW_NUMBER() OVER (PARTITION BY S.REF2 ORDER BY S.TRN_DT DESC) rn "
			+ "        FROM BRECON_DRFILE_SOURCE_TABLE S " + "    ) WHERE rn = 1 " + "), " + "destination_dedup AS ( "
			+ "    SELECT * FROM ( "
			+ "        SELECT D.*, ROW_NUMBER() OVER (PARTITION BY D.REFNUM ORDER BY D.TRANSACTION_DATE DESC) rn "
			+ "        FROM BRECON_DRFILE_DESTINATION_TABLE D " + "    ) WHERE rn = 1 " + ") " +

			"SELECT " + "    S.TRN_DT, S.VALUE_DT, S.TRN_REF, S.CTRY, S.RELATED_ACCOUNT, "
			+ "    S.TRN_DESC, S.ADDL_TEXT, S.AC_CCY, S.DR_ORG, S.CR_ORG, "
			+ "    S.RECON_TRAN_DATE, S.RECON_TYPE, 'Y', S.RECON_PROCESS_DATE, " +

			"    D.ARN, D.MCC, D.AUTH_AMNT, D.ACCT_AMNT, D.AUTH_CURRENCY, "
			+ "    D.TRANSACTION_DATE, D.TRANSACTION_AMOUNT, D.TRANSACTION_CURRENCY, D.TRANSACTION_TYPE, "
			+ "    D.CBS_ACCOUNT_NUMBER, D.CARD_NUMBER, D.CARD_TYPE, D.MERCHANT_DESC, D.COUNTRY_NAME, "
			+ "    D.APPROVAL_CODE, D.RECONCILE_AMOUNT, D.RECONCILE_CUR, D.FEES, D.I_FEE_IND, "
			+ "    D.REFNUM, D.DR_CR, D.MARKUP_VALUE, D.MARKUP_CURRENCY, "
			+ "    D.RECON_TRAN_DATE, D.RECON_TYPE, 'Y', D.RECON_PROCESS_DATE, " +

			"    'N','N','N','N', :entryUser, " + "    :entryUser, :entryUser, SYSDATE, SYSDATE, SYSDATE, "
			+ "    SEQ_RECON_TXN.NEXTVAL, S.REF1, S.REF2 " +

			"FROM source_dedup S " + "JOIN destination_dedup D " + "  ON S.REF2 = D.REFNUM "
			+ " AND D.AUTH_AMNT = D.TRANSACTION_AMOUNT " + "WHERE S.REF2 = :ref", nativeQuery = true)
	void insertReconDr(@Param("ref") String ref, @Param("entryUser") String entryUser);
	
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "INSERT INTO RECON_DRMAIN_TABLE ( "
			+ "   S_TRN_DT, S_VALUE_DT, S_TRN_REF, S_CTRY, S_RELATED_ACCOUNT, "
			+ "   S_TRN_DESC, S_ADDL_TEXT, S_AC_CCY, S_DR_ORG, S_CR_ORG, "
			+ "   S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_FLG, S_RECON_PROCESS_DATE, "
			+ "   D_ARN, D_MCC, D_AUTH_AMNT, D_ACCT_AMNT, D_AUTH_CURRENCY, "
			+ "   D_TRANSACTION_DATE, D_TRANSACTION_AMOUNT, D_TRANSACTION_CURRENCY, D_TRANSACTION_TYPE, "
			+ "   D_CBS_ACCOUNT_NUMBER, D_CARD_NUMBER, D_CARD_TYPE, D_MERCHANT_DESC, D_COUNTRY_NAME, "
			+ "   D_APPROVAL_CODE, D_RECONCILE_AMOUNT, D_RECONCILE_CUR, D_FEES, D_I_FEE_IND, "
			+ "   D_REFNUM, D_DR_CR, D_MARKUP_VALUE, D_MARKUP_CURRENCY, "
			+ "   D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_FLG, D_RECON_PROCESS_DATE, "
			+ "   ENTITY_FLG, AUTH_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER, MODIFY_USER, AUTH_USER, "
			+ "   ENTRY_TIME, MODIFY_TIME, AUTH_TIME, SRL_NO, S_REF1, S_REF2 " + ") " + "SELECT "
			+ "   NULL, NULL, NULL, NULL, NULL, " + "   NULL, NULL, D.MARKUP_CURRENCY, NULL, NULL, "
			+ "   D.RECON_TRAN_DATE, D.RECON_TYPE, 'Y', D.RECON_PROCESS_DATE, " + // <-- fetch from DEST
			"   D.ARN, D.MCC, D.AUTH_AMNT, D.ACCT_AMNT, D.AUTH_CURRENCY, "
			+ "   D.TRANSACTION_DATE, D.TRANSACTION_AMOUNT, D.TRANSACTION_CURRENCY, D.TRANSACTION_TYPE, "
			+ "   D.CBS_ACCOUNT_NUMBER, D.CARD_NUMBER, D.CARD_TYPE, D.MERCHANT_DESC, D.COUNTRY_NAME, "
			+ "   D.APPROVAL_CODE, D.RECONCILE_AMOUNT, D.RECONCILE_CUR, D.FEES, D.I_FEE_IND, "
			+ "   D.REFNUM, D.DR_CR, D.MARKUP_VALUE, D.MARKUP_CURRENCY, "
			+ "   D.RECON_TRAN_DATE, D.RECON_TYPE, 'Y', D.RECON_PROCESS_DATE, " + // <-- fetch from DEST
			"   'N','N','N','N', :entryUser, :entryUser, :entryUser, SYSDATE, SYSDATE, SYSDATE, "
			+ "   SEQ_RECON_TXN.NEXTVAL, NULL, D.REFNUM " + "FROM BRECON_DRFILE_DESTINATION_TABLE D "
			+ "WHERE D.REFNUM = :ref", nativeQuery = true)
	void insertReconDrFromDest(@Param("ref") String ref, @Param("entryUser") String entryUser);

	// FILTER
	@Query(value = "select * from RECON_DRMAIN_TABLE where del_flg ='N' AND to_char(s_recon_process_date,'DD-MM-YYYY') =?1", nativeQuery = true)
	List<RECON_DRMAIN_ENTITY> getReconDate(String date);
}
