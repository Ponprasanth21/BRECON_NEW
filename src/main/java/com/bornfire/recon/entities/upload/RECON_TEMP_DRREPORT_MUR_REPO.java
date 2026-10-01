package com.bornfire.recon.entities.upload;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface RECON_TEMP_DRREPORT_MUR_REPO  extends JpaRepository<RECON_TEMP_DRREPORT_MUR_ENTITY, Date>{
	@Query(value = "SELECT * FROM BRECON_TEMP_DRREPORT_MUR where del_flg ='N' and TO_CHAR(recon_date,'DD-MM-YYYY') =?1 AND CURRENCY =?2", nativeQuery = true)
	RECON_TEMP_DRREPORT_MUR_ENTITY getReconDate(String date , String curr);
}
