package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface RECON_CRFILE_DESTINATION_REPO  extends JpaRepository<RECON_CRFILE_DESTINATION_ENTITY,BigDecimal>{
	
	@Query(value = "SELECT BRECON_CRFILE_DEST_UPLOAD_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	BigDecimal getSeq();

	@Query(value = "select * from BRECON_CRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N'", nativeQuery = true)
	List<RECON_CRFILE_DESTINATION_ENTITY> getlist();
	
	@Query(value = "select *\r\n" + "from BRECON_CRFILE_DESTINATION_TABLE \r\n" + "WHERE DEL_FLG ='N' \r\n"
			+ "AND RECON_FLG ='N' \r\n"
			+ "AND TRUNC(RECON_PROCESS_DATE) = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getDestListByDate(@Param("recon_process_date") String recon_process_date);

	@Query(value = "select * from BRECON_CRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N' AND  TRUNC(recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
	List<RECON_CRFILE_DESTINATION_ENTITY> getDATE();

	// FILTER
	@Query(value = "select * from BRECON_CRFILE_DESTINATION_TABLE where del_flg ='N' AND to_char(recon_process_date,'DD-MM-YYYY') =?1 AND RECON_FLG ='N'", nativeQuery = true)
	List<RECON_CRFILE_DESTINATION_ENTITY> getReconDate(String date);
	
	@Query(value = "select * from BRECON_CRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N'  and arn =?1", nativeQuery = true)
	RECON_CRFILE_DESTINATION_ENTITY  getarn(String arn);


	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "UPDATE BRECON_CRFILE_DESTINATION_TABLE A\r\n" + 
			"   SET A.RECON_FLG = 'Y',\r\n" + 
			"       A.ENTRY_TIME = SYSDATE,\r\n" + 
			"       A.RECON_PROCESS_DATE =TO_DATE(:fromDate,'DD-MM-YYYY')\r\n" + 
			" WHERE A.DEL_FLG = 'N'\r\n" + 
			"   AND EXISTS (\r\n" + 
			"         SELECT 1\r\n" + 
			"           FROM BRECON_CRFILE_SOURCE_TABLE B\r\n" + 
			"          WHERE B.ARN = A.ARN\r\n" + 
			"            AND B.DEL_FLG = 'N'\r\n" + 
			"       )\r\n" + 
			"   AND NOT EXISTS (\r\n" + 
			"         SELECT 1\r\n" + 
			"           FROM BRECON_CRFILE_DESTINATION_TABLE M\r\n" + 
			"          WHERE M.ARN = A.ARN\r\n" + 
			"            AND M.RECON_FLG = 'Y'\r\n" + 
			"       )", nativeQuery = true)
	int UpdateValueDestTb(@Param("fromDate")  String  fromDate);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "  \r\n" + 
			"UPDATE BRECON_CRFILE_SOURCE_TABLE A\r\n" + 
			"   SET A.RECON_FLG = 'Y',\r\n" + 
			"       A.ENTRY_TIME = SYSDATE,\r\n" + 
			"       A.RECON_PROCESS_DATE = TO_DATE(:fromDate,'DD-MM-YYYY')\r\n" + 
			" WHERE A.DEL_FLG = 'N'\r\n" + 
			"   AND EXISTS (\r\n" + 
			"         SELECT 1\r\n" + 
			"           FROM BRECON_CRFILE_DESTINATION_TABLE B\r\n" + 
			"          WHERE B.ARN = A.ARN\r\n" + 
			"            AND B.DEL_FLG = 'N'\r\n" + 
			"       )\r\n" + 
			"   AND NOT EXISTS (\r\n" + 
			"         SELECT 1\r\n" + 
			"           FROM BRECON_CRFILE_SOURCE_TABLE M\r\n" + 
			"          WHERE M.ARN = A.ARN\r\n" + 
			"            AND M.RECON_FLG = 'Y'\r\n" + 
			"       )", nativeQuery = true)
	int UpdateValueSourTb(@Param("fromDate")  String  fromDate);

	@Query(value = "select * from BRECON_CRFILE_DESTINATION_TABLE WHERE DEL_FLG = 'N'", nativeQuery = true)
	List<RECON_CRFILE_DESTINATION_ENTITY> getdest();
	
	@Query(value = "SELECT a.arn\r\n" + 
			"FROM BRECON_CRFILE_SOURCE_TABLE a\r\n" + 
			"WHERE a.recon_flg = 'N'\r\n" + 
			"  AND EXISTS (\r\n" + 
			"      SELECT 1 \r\n" + 
			"      FROM BRECON_CRFILE_DESTINATION_TABLE b\r\n" + 
			"      WHERE b.arn = a.arn  \r\n" + 
			"  )", nativeQuery = true)
	List<Object[]> getCheckARN();
	
	@Query(value = "select * from BRECON_CRFILE_DESTINATION_TABLE WHERE DEL_FLG ='N' and arn =?1", nativeQuery = true)
	RECON_CRFILE_DESTINATION_ENTITY  getArn_N(String arn);
	
	@Query("SELECT s FROM RECON_CRFILE_DESTINATION_ENTITY s " + "WHERE s.arn = :arn " + "AND s.source_amt = :amt")
	RECON_CRFILE_DESTINATION_ENTITY findByArnAndAmount(@Param("arn") String arn, @Param("amt") BigDecimal amt);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "delete from brecon_crfile_destination_table where arn IN (:arns)", nativeQuery = true)
	int deltearn(@Param("arns") List<String> arns);
	
	 @Modifying
	 @Transactional
	 @Query(value = "CALL RECON_CRREPORT_USD_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	 void runReconUsd(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);

	 @Modifying
	 @Transactional
	 @Query(value = "CALL RECON_CRREPORT_MUR_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	 void runReconMur(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);
	 
	 @Modifying
	 @Transactional
	 @Query(value = "CALL RECON_CRDETAIL_USD_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	 void runReconDetailUsd(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);

	 @Modifying
	 @Transactional
	 @Query(value = "CALL RECON_CRDETAIL_MUR_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	 void runReconDetailMur(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);
}
