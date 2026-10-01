package com.bornfire.recon.entities.upload;

import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RECON_DRREPORT_MARKUP_REPO extends CrudRepository<RECON_DRREPORT_MARKUP_ENTITY, Date>{
	@Query(value = "SELECT COUNT(*) FROM BRECON_DR_MARKUP_LOCAL where   to_char(recon_date,'DD-MM-YYYY') =?1 ", nativeQuery = true)
	Long  getReconDate(String date , String markup_type );

}
