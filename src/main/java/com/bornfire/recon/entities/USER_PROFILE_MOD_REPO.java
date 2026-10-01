package com.bornfire.recon.entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface USER_PROFILE_MOD_REPO extends CrudRepository<USER_PROFILE_MOD_ENTITY,String>{

	public Optional<USER_PROFILE_MOD_ENTITY> findByusername(String userName);

	@Query(value = "select * from BRECON_USER_PROFILE_MOD_TABLE where USER_ID= ?1", nativeQuery = true)
	USER_PROFILE_MOD_ENTITY findByIdCustom(String Id);

	@Query(value = "select * from BRECON_USER_PROFILE_MOD_TABLE where NVL(DEL_FLG,1) <> 'Y'", nativeQuery = true)
	List<USER_PROFILE_MOD_ENTITY> findByAll(String Id);
}
