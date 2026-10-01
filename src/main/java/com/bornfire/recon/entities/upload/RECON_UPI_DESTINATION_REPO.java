package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RECON_UPI_DESTINATION_REPO extends JpaRepository<RECON_UPI_DESTINATION_ENTITY, BigDecimal> {

	@Query(value = "SELECT BRECON_UPI_DESTINATION_TABLE_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	BigDecimal getSeq();

	@Query(value = "select * from BRECON_UPI_DESTINATION_TABLE WHERE DEL_FLG ='N'", nativeQuery = true)
	List<RECON_UPI_DESTINATION_ENTITY> getdest();

	@Query(value = "select * from BRECON_UPI_DESTINATION_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N' AND TRUNC(recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
	List<RECON_UPI_DESTINATION_ENTITY> getlist();
	
	@Query(value = "select *\r\n" + "from BRECON_UPI_DESTINATION_TABLE \r\n" + "WHERE DEL_FLG ='N' \r\n"
			+ "AND RECON_FLG ='N' \r\n"
			+ "AND TRUNC(RECON_PROCESS_DATE) = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getDestListByDate(@Param("recon_process_date") String recon_process_date);

	@Query(value = "select * from BRECON_UPI_DESTINATION_TABLE WHERE DEL_FLG ='N' and refnum =?1", nativeQuery = true)
	RECON_UPI_DESTINATION_ENTITY getref(String ref);
	
	@Query(value = "SELECT d FROM RECON_UPI_DESTINATION_ENTITY d " + "WHERE d.refnum = :ref "
			+ "AND d.transaction_amount = :amt")
	RECON_UPI_DESTINATION_ENTITY findByRefAndAmount(@Param("ref") String ref, @Param("amt") BigDecimal amt);

	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "UPDATE BRECON_UPI_DESTINATION_TABLE " + "SET RECON_FLG = 'Y' " + "WHERE RECONCILE_CUR = '480-MUR' "
			+ "  AND TRANSACTION_TYPE = 'Balance enquiry' "
			+ "  AND TRUNC(RECON_PROCESS_DATE) = TO_DATE(:fromDate, 'DD-MM-YYYY')", nativeQuery = true)
	int updateValueDestTb(@Param("fromDate") String fromDate);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "UPDATE BRECON_UPI_SOURCE_TABLE A " + "   SET A.RECON_FLG = 'Y', "
			+ "       A.ENTRY_TIME = SYSDATE, " + "       A.RECON_PROCESS_DATE = TO_DATE(:fromDate,'DD-MM-YYYY') "
			+ " WHERE A.DEL_FLG = 'N' " + "   AND EXISTS ( " + "         SELECT 1 "
			+ "           FROM BRECON_UPI_DESTINATION_TABLE B " + "          WHERE A.ADDL3 = B.REFNUM "
			+ "            AND B.DEL_FLG = 'N' " + "       ) " + "   AND NOT EXISTS ( " + "         SELECT 1 "
			+ "           FROM BRECON_UPI_SOURCE_TABLE M " + "          WHERE M.ADDL3 = A.ADDL3 "
			+ "            AND M.RECON_FLG = 'Y' " + "       )", nativeQuery = true)
	int UpdateValueSourTb(@Param("fromDate") String fromDate);
	
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "UPDATE BRECON_UPI_DESTINATION_TABLE A " + "   SET A.RECON_FLG = 'Y', "
			+ "       A.ENTRY_TIME = SYSDATE, " + "       A.RECON_PROCESS_DATE = TO_DATE(:fromDate,'DD-MM-YYYY') "
			+ " WHERE A.DEL_FLG = 'N' " + "    AND A.AUTH_AMNT = A.TRANSACTION_AMOUNT " + "  AND EXISTS ( " + "         SELECT 1 "
			+ "           FROM BRECON_UPI_SOURCE_TABLE B " + "          WHERE B.ADDL3 = A.REFNUM "
			+ "            AND B.DEL_FLG = 'N' " + "       ) " + "   AND NOT EXISTS ( " + "         SELECT 1 "
			+ "           FROM BRECON_UPI_DESTINATION_TABLE M " + "          WHERE M.REFNUM = A.REFNUM "
			+ "            AND M.RECON_FLG = 'Y' " + "       )", nativeQuery = true)
	int UpdateValueDestTb(@Param("fromDate") String fromDate);
	
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "update BRECON_UPI_DESTINATION_TABLE  set  RECON_FLG ='Y'\r\n" + 
			"WHERE RECONCILE_CUR ='480-MUR' AND  transaction_type ='Balance enquiry'\r\n" + 
			"--AND RECON_FLG ='Y'\r\n" + 
			"and   TRUNC(recon_process_date) =TO_DATE(:fromDate,'DD-MM-YYYY')\r\n" + 
			"", nativeQuery = true)
	int UpdateBalanceEnquiry(@Param("fromDate") String fromDate);

	@Modifying
	@Transactional
	@Query(value = "CALL RECON_UPIREPORT_USD_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runReconUsd(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);

	@Modifying
	@Transactional
	@Query(value = "CALL RECON_UPIREPORT_MUR_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runReconMur(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);

	// FILTER
	@Query(value = "select * from BRECON_UPI_DESTINATION_TABLE where del_flg ='N' AND to_char(recon_process_date,'DD-MM-YYYY') =?1 AND RECON_FLG ='N'", nativeQuery = true)
	List<RECON_UPI_DESTINATION_ENTITY> getReconDate(String date);

	@Query(value = "SELECT a.ADDL3\r\n" + "FROM BRECON_UPI_SOURCE_TABLE a\r\n" + "WHERE a.recon_flg = 'N'\r\n"
			+ "  AND EXISTS (\r\n" + "      SELECT 1 \r\n" + "      FROM BRECON_UPI_DESTINATION_TABLE b\r\n"
			+ "      WHERE a.ADDL3 = b.REFNUM  \r\n" + "  )", nativeQuery = true)
	List<Object[]> getCheckREF();

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "delete from BRECON_UPI_DESTINATION_TABLE where refnum IN (:refnum)", nativeQuery = true)
	int delterefnum(@Param("refnum") List<String> refnum);

	@Modifying
	@Transactional
	@Query(value = "CALL RECON_UPIDETAIL_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runReconDetail(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);
	
	@Modifying
	@Transactional
	@Query(value = "CALL RECON_UPIMARKUP_PROCEDURE(:p_date)", nativeQuery = true)
	void runMarkupPro(@Param("p_date") String p_date);
	
	@Modifying
	@Transactional
	@Query(value = "CALL RECON_UPI_PROCEDURE(:p_date)", nativeQuery = true)
	void runSourcePro(@Param("p_date") String p_date);

}
