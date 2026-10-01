package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RECON_CRFILE_SOURCE_REPO extends JpaRepository<RECON_CRFILE_SOURCE_ENTITY,BigDecimal>{
	
	@Query(value = "SELECT BRECON_CRFILE_SOURCE_UPLOAD_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	BigDecimal getSeq();
	
	@Query(value = "select * from BRECON_CRFILE_SOURCE_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N'", nativeQuery = true)
	List<RECON_CRFILE_SOURCE_ENTITY> getlist();
	
	@Query(value = "select A.* , TO_CHAR(TRXN_DATE, 'DD-MM-YY') as tran_date,\r\n"
			+ "TO_CHAR(POSTING_DATE, 'DD-MM-YY') as post_date\r\n" + "from BRECON_CRFILE_SOURCE_TABLE A\r\n"
			+ "WHERE DEL_FLG ='N' \r\n" + "AND RECON_FLG ='N' \r\n"
			+ "AND TRUNC(recon_process_date) =  TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getSourceListByDate(@Param("recon_process_date") String recon_process_date);
	
	@Query(value = "select * from BRECON_CRFILE_SOURCE_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N' AND  TRUNC(recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
	List<RECON_CRFILE_SOURCE_ENTITY> getDATE();
	
	//FILTER
	@Query(value = "select * from BRECON_CRFILE_SOURCE_TABLE where del_flg ='N' AND to_char(recon_process_date,'DD-MM-YYYY') =?1 AND RECON_FLG ='N'", nativeQuery = true)
	List<RECON_CRFILE_SOURCE_ENTITY> getReconDate(String date);

	@Query(value = "select * from BRECON_CRFILE_SOURCE_TABLE WHERE DEL_FLG ='N' and arn =?1", nativeQuery = true)
	 RECON_CRFILE_SOURCE_ENTITY  getarn(String arn);
	
	@Query(value = "select * from BRECON_CRFILE_SOURCE_TABLE  WHERE DEL_FLG ='N'", nativeQuery = true)
	List<RECON_CRFILE_SOURCE_ENTITY> getsource();
	
	@Query(value = "select * from BRECON_CRFILE_SOURCE_TABLE WHERE DEL_FLG ='N'   and arn =?1", nativeQuery = true)
	RECON_CRFILE_SOURCE_ENTITY  getArn_N(String arn);
	
	@Query("SELECT s FROM RECON_CRFILE_SOURCE_ENTITY s " + "WHERE s.arn = :arn " + "AND s.source_amt = :amt")
	RECON_CRFILE_SOURCE_ENTITY findByArnAndAmount(@Param("arn") String arn, @Param("amt") BigDecimal amt);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "delete from BRECON_CRFILE_SOURCE_TABLE where arn IN (:arns)", nativeQuery = true)
	int deltearn(@Param("arns") List<String> arns);
	
	@Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN 1 ELSE 0 END \r\n" + 
			"                   FROM BRECON_CRFILE_SOURCE_TABLE A \r\n" + 
			"                   WHERE A.RECON_FLG = 'Y'", nativeQuery = true)
	 int existsAnyY();
	
	@Query(value = "select * from BRECON_CRFILE_SOURCE_TABLE WHERE DEL_FLG ='N' and tr_his_id =?1", nativeQuery = true)
	RECON_CRFILE_SOURCE_ENTITY  getTrHisId(String tr_his_id);
	
}
