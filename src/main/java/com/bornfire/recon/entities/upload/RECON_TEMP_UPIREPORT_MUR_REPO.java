package com.bornfire.recon.entities.upload;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RECON_TEMP_UPIREPORT_MUR_REPO extends JpaRepository<RECON_TEMP_UPIREPORT_MUR_ENTITY, Date> {
	
	@Query(value = "SELECT * FROM BRECON_TEMP_UPIREPORT_MUR where del_flg ='N' and to_char(recon_date,'DD-MM-YYYY') =?1 AND CURRENCY =?2", nativeQuery = true)
	RECON_TEMP_UPIREPORT_MUR_ENTITY getReconDate(String date , String curr);
	
}
