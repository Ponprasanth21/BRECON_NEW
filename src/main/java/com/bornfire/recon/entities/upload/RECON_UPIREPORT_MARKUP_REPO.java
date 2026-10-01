package com.bornfire.recon.entities.upload;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RECON_UPIREPORT_MARKUP_REPO extends JpaRepository<RECON_UPIREPORT_MARKUP_ENTITY, Date>{
	
	@Query(value = "SELECT * FROM brecon_upi_markup_local where to_char(recon_date,'DD-MM-YYYY') =?1 ", nativeQuery = true)
	RECON_UPIREPORT_MARKUP_ENTITY getReconDate(String date);

}
