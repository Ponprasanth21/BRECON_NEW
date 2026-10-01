package com.bornfire.recon.entities.upload;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface RECON_DRREPORT_MUR_REPO  extends JpaRepository<RECON_DRREPORT_MUR_ENTITY, Date>{
	@Query(value = "SELECT * FROM BRECON_DRREPORT_MUR where del_flg ='N' and TO_CHAR(recon_date,'DD-MM-YYYY') =?1 AND CURRENCY =?2", nativeQuery = true)
	RECON_DRREPORT_MUR_ENTITY getReconDate(String date , String curr);

	@Query(value = "SELECT * FROM BRECON_DRREPORT_MUR where del_flg ='N' and RECON_DATE >= TO_DATE(?1, 'DD-MM-YYYY') AND RECON_DATE <  TO_DATE(?2, 'DD-MM-YYYY') AND CURRENCY =?3", nativeQuery = true)
	String getReconDateMUR(String date1,String date2 , String curr);
	
	@Query(value = "SELECT * FROM TABLE(fn_sum_DRrecon_MUR_by_date(?1,?2))", nativeQuery = true)
	RECON_DRREPORT_MUR_ENTITY getReconDateBulk(String date1,String date2 , String curr);

	
			}
