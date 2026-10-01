package com.bornfire.recon.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ACCESS_AND_ROLES_TEMP_REPO extends JpaRepository<ACCESS_AND_ROLES_TEMP_ENTITY,String>{

	List<ACCESS_AND_ROLES_TEMP_ENTITY> findByDelFlgNot(String delFlg);

	
	 @Query(value = "select entry_user from BRECON_ACCES_ROLES_TEMP_TABLE ", nativeQuery = true)
	 List<String> getEntryUser();
}
