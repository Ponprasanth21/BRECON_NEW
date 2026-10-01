package com.bornfire.recon.entities.upload;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RECON_PRINT_ENQUIRY_REPO extends JpaRepository<RECON_PRINT_ENQUIRY_ENTITY,String>{

	@Query(value = "SELECT * FROM BRECON_PRINT_QUEUE_TABLE where del_flg ='N' ", nativeQuery = true)
	List<RECON_PRINT_ENQUIRY_ENTITY> getValues();
	
	@Query(value = "SELECT BRECON_PRINT_SEQ.NEXTVAL FROM DUAL ", nativeQuery = true)
	String getSeq();
	
}
