package com.bornfire.recon.entities;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface  ACCESS_AND_ROLES_REPO extends JpaRepository<ACCESS_AND_ROLES_ENTITY,String> {
	 Optional<ACCESS_AND_ROLES_ENTITY> findById( String directorId);

	 
	 List<ACCESS_AND_ROLES_ENTITY> findByDelFlgNot(String delFlg);

	 
	 @Query(value = "select * from BRECON_ACCES_ROLES_TABLE  where ROLE_ID =?1", nativeQuery = true)
		String FindByAll(String roleId);

	 
	 @Query(value = "select * from BRECON_ACCES_ROLES_TABLE where DEL_FLG!='Y'", nativeQuery = true)
	 List<ACCESS_AND_ROLES_ENTITY> rulelist();
	 
	 
	 @Modifying
		@Query(value = "UPDATE BRECON_ACCES_ROLES_TABLE set DEL_FLG ='Y' where ROLE_ID =?1", nativeQuery = true)
		String findByfgdg1(String roleId);
	 
	 @Query(value = "select distinct ROLE_ID from BRECON_ACCES_ROLES_TABLE  where DEL_FLG='N' AND ENTITY_FLG='Y'", nativeQuery = true)
		List<String> roleidtype();
	 
	 @Query(value = "select ROLE_ID from BRECON_ACCES_ROLES_TABLE where DEL_FLG='N' AND ROLE_ID =?1", nativeQuery = true)
	 String[] CheckRoleId(String role_id);
	 
	 @Query(value = "select role_desc from BRECON_ACCES_ROLES_TABLE where DEL_FLG='N' AND role_desc =?1", nativeQuery = true)
	 String[] CheckRoleDesc(String role_desc);

}
