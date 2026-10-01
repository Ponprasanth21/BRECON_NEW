package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface RECON_DRFILE_DESTINATION_REPO extends JpaRepository<RECON_DRFILE_DESTINATION_ENTITY,BigDecimal>{
	
	@Query(value = "SELECT BRECON_DRFILE_DEST_UPLOAD_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	BigDecimal getSeq();
	
	@Query(value = "select * from BRECON_DRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N'", nativeQuery = true)
	List<RECON_DRFILE_DESTINATION_ENTITY> getdest();
	
	@Query(value = "select * from BRECON_DRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N' AND TRUNC(recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
	List<RECON_DRFILE_DESTINATION_ENTITY> getlist();
	
	@Query(value = "SELECT * FROM BRECON_DRFILE_DESTINATION_TABLE "
			+ "WHERE DEL_FLG = 'N' " + "AND RECON_FLG = 'N' "
			+ "AND TRUNC(RECON_PROCESS_DATE) = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getDestListByDate(@Param("recon_process_date") String recon_process_date);
	
	// 2. Update Destination with matched records
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "UPDATE BRECON_DRFILE_DESTINATION_TABLE A " + "   SET A.RECON_FLG = 'Y', "
			+ "       A.ENTRY_TIME = SYSDATE, " + "       A.RECON_PROCESS_DATE = TO_DATE(:fromDate, 'DD-MM-YYYY') "
			+ " WHERE A.DEL_FLG = 'N' " + "   AND A.AUTH_AMNT = A.TRANSACTION_AMOUNT " + "   AND EXISTS ( "
			+ "         SELECT 1 " + "           FROM BRECON_DRFILE_SOURCE_TABLE B "
			+ "          WHERE B.REF2 = A.REFNUM " + "            AND B.DEL_FLG = 'N' " + "       ) "
			+ "   AND NOT EXISTS ( " + "         SELECT 1 " + "           FROM BRECON_DRFILE_DESTINATION_TABLE M "
			+ "          WHERE M.REFNUM = A.REFNUM " + "            AND M.RECON_FLG = 'Y' "

			+ "       )", nativeQuery = true)
	int UpdateValueDestTb(@Param("fromDate") String fromDate);


	// 3. Update Source with matched records
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "UPDATE BRECON_DRFILE_SOURCE_TABLE A " + "   SET A.RECON_FLG = 'Y', "
			+ "       A.ENTRY_TIME = SYSDATE, " + "       A.RECON_PROCESS_DATE = TO_DATE(:fromDate,'DD-MM-YYYY') "
			+ " WHERE A.DEL_FLG = 'N' " + "   AND EXISTS ( " + "         SELECT 1 "
			+ "           FROM BRECON_DRFILE_DESTINATION_TABLE B " + "          WHERE A.REF2 = B.REFNUM "
			+ "            AND B.DEL_FLG = 'N'  and   b.transaction_type<>'Europay refund'  " + "       ) " + "   AND NOT EXISTS ( " + "         SELECT 1 "
			+ "           FROM BRECON_DRFILE_SOURCE_TABLE M " + "          WHERE M.REF2 = A.REF2 "
			+ "            AND M.RECON_FLG = 'Y' " + "       )", nativeQuery = true)
	int UpdateValueSourTb(@Param("fromDate") String fromDate);

	@Query(value = "select * from BRECON_DRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N' and refnum =?1", nativeQuery = true)
	RECON_DRFILE_DESTINATION_ENTITY  getref (String ref);
	
	@Query(value = "select * from BRECON_DRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N' and refnum =?1  AND to_char(recon_process_date,'DD-MM-YYYY') =?2", nativeQuery = true)
	RECON_DRFILE_DESTINATION_ENTITY  getrefBydate (String ref,String date);
	
	@Query(value = "SELECT d FROM RECON_DRFILE_DESTINATION_ENTITY d " + "WHERE d.refnum = :ref "
			+ "AND d.transaction_amount = :amt")
	RECON_DRFILE_DESTINATION_ENTITY findByRefAndAmount(@Param("ref") String ref, @Param("amt") BigDecimal amt);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "delete from brecon_drfile_destination_table where refnum IN (:refnum)", nativeQuery = true)
	int delteref(@Param("refnum") List<String> refnum);
	
	@Modifying
	@Transactional
	@Query(value = "CALL RECON_DRREPORT_USD_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runReconUsd(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);

	@Modifying
	@Transactional
	@Query(value = "CALL RECON_DRREPORT_MUR_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runReconMur(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);
	
	@Modifying
	@Transactional
	@Query(value = "CALL RECON_DRDETAIL_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runReconDetail(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);
	
	@Modifying
	@Transactional
	@Query(value = "CALL RECON_DRMARKUP_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runMarkupPro(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);
	 
	
	@Modifying
	@Transactional
	@Query(value = "CALL RECON_DEBIT_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void runSourcePro(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);
	 
	
	
	// FILTER
	@Query(value = "select * from BRECON_DRFILE_DESTINATION_TABLE where del_flg ='N' AND to_char(recon_process_date,'DD-MM-YYYY') =?1 AND RECON_FLG ='N'", nativeQuery = true)
	List<RECON_DRFILE_DESTINATION_ENTITY> getReconDate(String date);
	
	@Query(value = "SELECT a.REF2\r\n" + "FROM BRECON_DRFILE_SOURCE_TABLE a\r\n" + "WHERE a.recon_flg = 'N'\r\n"
			+ "  AND EXISTS (\r\n" + "      SELECT 1 \r\n" + "      FROM BRECON_DRFILE_DESTINATION_TABLE b\r\n"
			+ "      WHERE a.REF2 = b.REFNUM  \r\n" + "  )", nativeQuery = true)
	List<Object[]> getCheckREF();
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "delete from BRECON_DRFILE_DESTINATION_TABLE where refnum IN (:refnum)", nativeQuery = true)
	int delterefnum(@Param("refnum") List<String> refnum);

}
