package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

public interface RECON_UPIMAIN_REPO extends CrudRepository<RECON_UPIMAIN_ENTITY,BigDecimal> {
	
	@Query(value = "SELECT * FROM RECON_UPIMAIN_TABLE WHERE DEL_FLG ='N' AND TRUNC(s_recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
    List<RECON_UPIMAIN_ENTITY> getlist();
    
	@Query(value = "select *\r\n" + "from RECON_UPIMAIN_TABLE \r\n" + "WHERE DEL_FLG ='N' \r\n"
			+ "AND TRUNC(s_recon_process_date)  = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getMainListByDate(@Param("recon_process_date") String recon_process_date);
    
	@Modifying
	@Transactional
	@Query(value =
	    "INSERT INTO RECON_UPIMAIN_TABLE ( " +
	    " S_TRN_DT, S_VALUE_DT, S_CTRY, S_REF, S_TRN_DESC, S_ADDL2, S_ADDL3, S_AC_CCY, " +
	    " S_DR_MOV, S_CR_MOV, S_RECON_FLG, S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_PROCESS_DATE, " +
	    " D_STTL_DATE, D_ARN, D_MCC, D_AUTH_AMNT, D_ACCT_AMNT, D_AUTH_CURRENCY, " +
	    " D_TRANSACTION_DATE, D_REFNUM, D_RECONCILE_AMOUNT, D_RECONCILE_CUR, D_TRANSACTION_AMOUNT, " +
	    " D_TRANSACTION_CURRENCY, D_TRANSACTION_TYPE, D_CBS_ACC_NO, D_CARD_NUMBER, D_CARD_TYPE, D_MERCHANT_DESC, " +
	    " D_TRN_COUNTRY, D_APPROVAL_CODE, D_INTERCHANGE_FEES, D_SERVICE_FEE, D_SERVICE_IND, " +
	    " D_I_FEE_IND, D_DR_CR, D_MARKUP_VALUE, D_MARKUP_CURRENCY, " +
	    " D_RECON_FLG, D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_PROCESS_DATE, " +
	    " ENTITY_FLG, AUTH_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER, " +
	    " MODIFY_USER, AUTH_USER, ENTRY_TIME, MODIFY_TIME, AUTH_TIME, " +
	    " SRL_NO, S_TRN_TYPE, S_ADDL_TEXT ) " +

	    "WITH source_dedup AS ( " +
	    "  SELECT * FROM ( " +
	    "    SELECT S.*, ROW_NUMBER() OVER (PARTITION BY S.ADDL3 ORDER BY S.TRN_DT DESC) rn " +
	    "    FROM BRECON_UPI_SOURCE_TABLE S " +
	    "  ) WHERE rn = 1 " +
	    "), " +

	    "destination_dedup AS ( " +
	    "  SELECT * FROM ( " +
	    "    SELECT D.*, ROW_NUMBER() OVER (PARTITION BY D.REFNUM ORDER BY D.TRANSACTION_DATE DESC) rn " +
	    "    FROM BRECON_UPI_DESTINATION_TABLE D " +
	    "  ) WHERE rn = 1 " +
	    ") " +

	    "SELECT " +
	    " S.TRN_DT, S.VALUE_DT, S.CTRY, S.REF, S.TRN_DESC, S.ADDL2, S.ADDL3, S.AC_CCY, " +
	    " S.DR_MOV, S.CR_MOV, 'Y', " +
	    " SYSDATE, " +
	    " S.RECON_TYPE, " +
	    " TO_DATE(:fromDate,'DD-MM-YYYY'), " +
	    " D.STTL_DATE, D.ARN, D.MCC, D.AUTH_AMNT, D.ACCT_AMNT, D.AUTH_CURRENCY, " +
	    " D.TRANSACTION_DATE, D.REFNUM, D.RECONCILE_AMOUNT, D.RECONCILE_CUR, D.TRANSACTION_AMOUNT, " +
	    " D.TRANSACTION_CURRENCY, D.TRANSACTION_TYPE, D.CBS_ACC_NO, D.CARD_NUMBER, D.CARD_TYPE, D.MERCHANT_DESC, " +
	    " D.TRN_COUNTRY, D.APPROVAL_CODE, D.INTERCHANGE_FEES, D.SERVICE_FEE, D.SERVICE_IND, " +
	    " D.I_FEE_IND, D.DR_CR, D.MARKUP_VALUE, D.MARKUP_CURRENCY, " +
	    " 'Y', " +
	    " SYSDATE, " +
	    " D.RECON_TYPE, " +
	    " TO_DATE(:fromDate,'DD-MM-YYYY'), " +
	    " 'N', 'N', 'N', 'N', :entryUser, " +
	    " :entryUser, :entryUser, SYSDATE, SYSDATE, SYSDATE, " +
	    " SEQ_RECON_TXN.NEXTVAL, S.TRN_TYPE, S.ADDL_TEXT " +

	    "FROM source_dedup S " +
	    "JOIN destination_dedup D " +
	    "  ON S.ADDL3 = D.REFNUM " +
	    " AND D.AUTH_AMNT = D.TRANSACTION_AMOUNT " +
	    "WHERE S.DEL_FLG = 'N' " +
	    "  AND D.DEL_FLG = 'N' " +
	    "  AND NOT EXISTS ( " +
	    "      SELECT 1 FROM RECON_UPIMAIN_TABLE M WHERE M.S_ADDL3 = S.ADDL3 " +
	    "  )",
	    nativeQuery = true
	)
	int insertValueReconTb(
	    @Param("entryUser") String entryUser,
	    @Param("fromDate") String fromDate
	);
    
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "INSERT INTO RECON_UPIMAIN_TABLE ( "
			+ "    S_TRN_DT, S_VALUE_DT, S_CTRY, S_REF, S_TRN_DESC, S_ADDL2, S_ADDL3, S_AC_CCY, "
			+ "    S_DR_MOV, S_CR_MOV, S_RECON_FLG, S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_PROCESS_DATE, " +

			"    D_STTL_DATE, D_ARN, D_MCC, D_AUTH_AMNT, D_ACCT_AMNT, D_AUTH_CURRENCY, "
			+ "    D_TRANSACTION_DATE, D_REFNUM, D_RECONCILE_AMOUNT, D_RECONCILE_CUR, D_TRANSACTION_AMOUNT, "
			+ "    D_TRANSACTION_CURRENCY, D_TRANSACTION_TYPE, D_CBS_ACC_NO, D_CARD_NUMBER, D_CARD_TYPE, "
			+ "    D_MERCHANT_DESC, D_TRN_COUNTRY, D_APPROVAL_CODE, D_INTERCHANGE_FEES, "
			+ "    D_SERVICE_FEE, D_SERVICE_IND, D_I_FEE_IND, D_DR_CR, D_MARKUP_VALUE, "
			+ "    D_MARKUP_CURRENCY, D_RECON_FLG, D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_PROCESS_DATE, " +

			"    ENTITY_FLG, AUTH_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER, "
			+ "    MODIFY_USER, AUTH_USER, ENTRY_TIME, MODIFY_TIME, AUTH_TIME, "
			+ "    SRL_NO, S_TRN_TYPE, S_ADDL_TEXT " + ") " +

			"WITH source_dedup AS ( " + "    SELECT * FROM ( "
			+ "        SELECT S.*, ROW_NUMBER() OVER (PARTITION BY S.ADDL3 ORDER BY S.TRN_DT DESC) rn "
			+ "        FROM BRECON_UPI_SOURCE_TABLE S " + "    ) WHERE rn = 1 " + "), " + "destination_dedup AS ( "
			+ "    SELECT * FROM ( "
			+ "        SELECT D.*, ROW_NUMBER() OVER (PARTITION BY D.REFNUM ORDER BY D.TRANSACTION_DATE DESC) rn "
			+ "        FROM BRECON_UPI_DESTINATION_TABLE D " + "    ) WHERE rn = 1 " + ") " +

			"SELECT " + "    S.TRN_DT, S.VALUE_DT, S.CTRY, S.REF, S.TRN_DESC, S.ADDL2, S.ADDL3, S.AC_CCY, "
			+ "    S.DR_MOV, S.CR_MOV, 'Y', S.RECON_TRAN_DATE, S.RECON_TYPE, S.RECON_PROCESS_DATE, " +

			"    D.STTL_DATE, D.ARN, D.MCC, D.AUTH_AMNT, D.ACCT_AMNT, D.AUTH_CURRENCY, "
			+ "    D.TRANSACTION_DATE, D.REFNUM, D.RECONCILE_AMOUNT, D.RECONCILE_CUR, D.TRANSACTION_AMOUNT, "
			+ "    D.TRANSACTION_CURRENCY, D.TRANSACTION_TYPE, D.CBS_ACC_NO, D.CARD_NUMBER, D.CARD_TYPE, "
			+ "    D.MERCHANT_DESC, D.TRN_COUNTRY, D.APPROVAL_CODE, D.INTERCHANGE_FEES, "
			+ "    D.SERVICE_FEE, D.SERVICE_IND, D.I_FEE_IND, D.DR_CR, D.MARKUP_VALUE, "
			+ "    D.MARKUP_CURRENCY, 'Y', D.RECON_TRAN_DATE, D.RECON_TYPE, D.RECON_PROCESS_DATE, " +

			"    'N','N','N','N', :entryUser, " + "    :entryUser, :entryUser, SYSDATE, SYSDATE, SYSDATE, "
			+ "    SEQ_RECON_TXN.NEXTVAL, S.TRN_TYPE, S.ADDL_TEXT " +

			"FROM source_dedup S " + "JOIN destination_dedup D " + "  ON S.ADDL3 = D.REFNUM "
			+ " AND D.AUTH_AMNT = D.TRANSACTION_AMOUNT " + "WHERE S.ADDL3 = :ref", nativeQuery = true)
	void insertReconRef(@Param("ref") String ref, @Param("entryUser") String entryUser);
    
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "INSERT INTO RECON_UPIMAIN_TABLE ( "
			+ "   S_TRN_DT, S_VALUE_DT, S_CTRY, S_REF, S_TRN_DESC, S_ADDL2, S_ADDL3, S_AC_CCY, "
			+ "   S_DR_MOV, S_CR_MOV, S_RECON_FLG, S_RECON_TRAN_DATE, S_RECON_TYPE, S_RECON_PROCESS_DATE, " +

			"   D_STTL_DATE, D_ARN, D_MCC, D_AUTH_AMNT, D_ACCT_AMNT, D_AUTH_CURRENCY, "
			+ "   D_TRANSACTION_DATE, D_REFNUM, D_RECONCILE_AMOUNT, D_RECONCILE_CUR, D_TRANSACTION_AMOUNT, "
			+ "   D_TRANSACTION_CURRENCY, D_TRANSACTION_TYPE, D_CBS_ACC_NO, D_CARD_NUMBER, D_CARD_TYPE, "
			+ "   D_MERCHANT_DESC, D_TRN_COUNTRY, D_APPROVAL_CODE, D_INTERCHANGE_FEES, "
			+ "   D_SERVICE_FEE, D_SERVICE_IND, D_I_FEE_IND, D_DR_CR, D_MARKUP_VALUE, "
			+ "   D_MARKUP_CURRENCY, D_RECON_FLG, D_RECON_TRAN_DATE, D_RECON_TYPE, D_RECON_PROCESS_DATE, " +

			"   ENTITY_FLG, AUTH_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER, MODIFY_USER, AUTH_USER, "
			+ "   ENTRY_TIME, MODIFY_TIME, AUTH_TIME, SRL_NO, S_TRN_TYPE, S_ADDL_TEXT " + ") " +

			"SELECT " + "   NULL, NULL, NULL, NULL, NULL, NULL, D.REFNUM, D.MARKUP_CURRENCY, "
			+ "   NULL, NULL, 'Y', D.RECON_TRAN_DATE, D.RECON_TYPE, D.RECON_PROCESS_DATE, " +

			"   D.STTL_DATE, D.ARN, D.MCC, D.AUTH_AMNT, D.ACCT_AMNT, D.AUTH_CURRENCY, "
			+ "   D.TRANSACTION_DATE, D.REFNUM, D.RECONCILE_AMOUNT, D.RECONCILE_CUR, D.TRANSACTION_AMOUNT, "
			+ "   D.TRANSACTION_CURRENCY, D.TRANSACTION_TYPE, D.CBS_ACC_NO, D.CARD_NUMBER, D.CARD_TYPE, "
			+ "   D.MERCHANT_DESC, D.TRN_COUNTRY, D.APPROVAL_CODE, D.INTERCHANGE_FEES, "
			+ "   D.SERVICE_FEE, D.SERVICE_IND, D.I_FEE_IND, D.DR_CR, D.MARKUP_VALUE, "
			+ "   D.MARKUP_CURRENCY, 'Y', D.RECON_TRAN_DATE, D.RECON_TYPE, D.RECON_PROCESS_DATE, " +

			"   'N','N','N','N', :entryUser, :entryUser, :entryUser, "
			+ "   SYSDATE, SYSDATE, SYSDATE, SEQ_RECON_TXN.NEXTVAL, NULL, D.REFNUM " +

			"FROM BRECON_UPI_DESTINATION_TABLE D " + "WHERE D.REFNUM = :ref", nativeQuery = true)
	void insertReconRefFromDest(@Param("ref") String ref, @Param("entryUser") String entryUser);

	// FILTER
	@Query(value = "select * from RECON_UPIMAIN_TABLE where del_flg ='N' AND to_char(S_RECON_PROCESS_DATE,'DD-MM-YYYY') =?1", nativeQuery = true)
	List<RECON_UPIMAIN_ENTITY> getReconDate(String date);
}
