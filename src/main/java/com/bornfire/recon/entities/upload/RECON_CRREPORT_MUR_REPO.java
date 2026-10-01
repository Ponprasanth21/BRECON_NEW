package com.bornfire.recon.entities.upload;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RECON_CRREPORT_MUR_REPO extends JpaRepository<RECON_CRREPORT_MUR_ENTITY, Date>{

	@Query(value = "SELECT * FROM BRECON_CRREPORT_MUR where del_flg ='N' and to_char(recon_date,'dd-MM-YYYY') =?1 AND CURRENCY =?2", nativeQuery = true)
	RECON_CRREPORT_MUR_ENTITY getReconDate(String date , String curr);
	
	
	@Modifying
	@Transactional
	@Query(value = "CALL RECON_DELETEREPORTBYDATE_PROCEDURE(:fromDate, :p_currency)", nativeQuery = true)
	void deleteRecon(@Param("fromDate") String fromDate, @Param("p_currency") String p_currency);

	@Query(value = "SELECT * FROM TABLE(fn_sum_recon_MUR_by_date(?1,?2))", nativeQuery = true)
	RECON_CRREPORT_MUR_ENTITY getReconDateBulk(String date1,String date2 , String curr);
}
