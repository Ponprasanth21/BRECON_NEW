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
public interface RECON_DRFILE_SOURCE_REPO extends JpaRepository<RECON_DRFILE_SOURCE_ENTITY,BigDecimal> {
	
	@Query(value = "SELECT BRECON_DRFILE_SOURCE_TABLE_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	BigDecimal getSeq();
	
	@Query(value = "select * from BRECON_DRFILE_SOURCE_TABLE WHERE DEL_FLG ='N'", nativeQuery = true)
	List<RECON_DRFILE_SOURCE_ENTITY> getsource();
	
	@Query(value = "select * from BRECON_DRFILE_SOURCE_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N' AND TRUNC(recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
	List<RECON_DRFILE_SOURCE_ENTITY> getlist();
	
	@Query(value = "SELECT * FROM BRECON_DRFILE_SOURCE_TABLE " + "WHERE DEL_FLG = 'N' AND RECON_FLG = 'N' "
			+ "AND TRUNC(RECON_PROCESS_DATE) = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getSourceListByDate(@Param("recon_process_date") String recon_process_date);
	
	@Query(value = "select * from BRECON_DRFILE_SOURCE_TABLE WHERE DEL_FLG ='N' and ref2 =?1", nativeQuery = true)
	RECON_DRFILE_SOURCE_ENTITY  getref(String ref);
	
	@Query("SELECT s FROM RECON_DRFILE_SOURCE_ENTITY s " + "WHERE s.ref2 = :ref " + "AND s.cr_org = :crOrg")
	RECON_DRFILE_SOURCE_ENTITY findByRefAndAmount(@Param("ref") String ref, @Param("crOrg") String crOrg);
	
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "delete from BRECON_DRFILE_SOURCE_TABLE where ref2 IN (:refnum)", nativeQuery = true)
	int delterefnum(@Param("refnum") List<String> refnum);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = "delete from BRECON_DRFILE_SOURCE_TABLE where trn_ref IN (:trn_ref)", nativeQuery = true)
	int delteref(@Param("trn_ref") List<String> trn_ref);
	
	@Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN 1 ELSE 0 END \r\n" + 
			"                   FROM BRECON_DRFILE_SOURCE_TABLE A \r\n" + 
			"                   WHERE A.RECON_FLG = 'Y'", nativeQuery = true)
	 int existsAnyY();
	
	//FILTER
	@Query(value = "select * from BRECON_DRFILE_SOURCE_TABLE where del_flg ='N' AND to_char(recon_process_date,'DD-MM-YYYY') =?1 AND RECON_FLG ='N'", nativeQuery = true)
	List<RECON_DRFILE_SOURCE_ENTITY> getReconDate(String date);
}
