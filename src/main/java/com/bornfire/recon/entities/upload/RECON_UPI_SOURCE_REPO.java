package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RECON_UPI_SOURCE_REPO extends JpaRepository<RECON_UPI_SOURCE_ENTITY,BigDecimal> {
	
	@Query(value = "SELECT BRECON_UPI_SOURCE_TABLE_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	BigDecimal getSeq();
	

	@Query(value = "select * from BRECON_UPI_SOURCE_TABLE WHERE DEL_FLG ='N' AND RECON_FLG ='N' AND TRUNC(recon_process_date) = TRUNC(SYSDATE)", nativeQuery = true)
	List<RECON_UPI_SOURCE_ENTITY> getlist();
	
	@Query(value = "select *\r\n" + "from BRECON_UPI_SOURCE_TABLE \r\n" + "WHERE DEL_FLG ='N' \r\n"
			+ "AND RECON_FLG ='N' \r\n"
			+ "AND TRUNC(recon_process_date) = TO_DATE(:recon_process_date, 'DD-MM-YYYY')", nativeQuery = true)
	List<Object[]> getSourceListByDate(@Param("recon_process_date") String recon_process_date);

	@Query(value = "select * from BRECON_UPI_SOURCE_TABLE WHERE DEL_FLG ='N'", nativeQuery = true)
	List<RECON_UPI_SOURCE_ENTITY> getsource();
	
	@Query(value = "select * from BRECON_UPI_SOURCE_TABLE WHERE DEL_FLG ='N' and addl3 =?1", nativeQuery = true)
	RECON_UPI_SOURCE_ENTITY  getref(String ref);
	
	@Query("SELECT s FROM RECON_UPI_SOURCE_ENTITY s " + "WHERE s.addl3 = :addl3 " + "AND s.cr_mov = :amt")
	RECON_UPI_SOURCE_ENTITY findByRefAndAmount(@Param("addl3") String addl3, @Param("amt") BigDecimal amt);

	@Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN 1 ELSE 0 END \r\n"
			+ "                   FROM BRECON_UPI_SOURCE_TABLE A \r\n"
			+ "                   WHERE A.RECON_FLG = 'Y'", nativeQuery = true)
	int existsAnyY();
	
	//FILTER
		@Query(value = "select * from BRECON_UPI_SOURCE_TABLE where del_flg ='N' AND to_char(recon_process_date,'DD-MM-YYYY') =?1 AND RECON_FLG ='N'", nativeQuery = true)
		List<RECON_UPI_SOURCE_ENTITY> getReconDate(String date);
		
		@Modifying(clearAutomatically = true, flushAutomatically = true)
		@Transactional
		@Query(value = "delete from BRECON_UPI_SOURCE_TABLE where addl3 IN (:addl3)", nativeQuery = true)
		int delteaddl(@Param("addl3") List<String> addl3);
		

}
