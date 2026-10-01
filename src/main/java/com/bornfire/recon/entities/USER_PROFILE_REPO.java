package com.bornfire.recon.entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface USER_PROFILE_REPO extends CrudRepository<USER_PROFILE_ENTITY,String>{
	

	public Optional<USER_PROFILE_ENTITY> findByusername(String userName);
	
	@Query(value = "select * from BRECON_USER_PROFILE_TABLE where user_id=?1 ", nativeQuery = true)
	List<USER_PROFILE_ENTITY> delete(String userid);
	
	@Query(value = "select * from BRECON_USER_PROFILE_TABLE", nativeQuery = true)
	List<USER_PROFILE_ENTITY> getalluser();
	
	@Query(value = "select * from BRECON_USER_PROFILE_TABLE where USER_ID=?1", nativeQuery = true)
	USER_PROFILE_ENTITY getRole(String userId);
	
	@Query(value = "select ROLE_ID from BRECON_USER_PROFILE_TABLE where USER_ID=?1", nativeQuery = true)
	String getRoleID(String userId);

	@Query(value = "select * from BRECON_USER_PROFILE_TABLE UNION ALL select * from BRECON_USER_PROFILE_MOD_TABLE", nativeQuery = true)
	List<USER_PROFILE_ENTITY> getUserId();
			
	@Query(value = "SELECT * FROM (" +
            "SELECT * FROM BRECON_USER_PROFILE_TABLE WHERE ENTITY_FLG = 'Y' " +
            "UNION ALL " +
            "SELECT * FROM BRECON_USER_PROFILE_MOD_TABLE WHERE ENTITY_FLG = 'N'" +
            ") ORDER BY USER_ID", nativeQuery = true)
List<USER_PROFILE_ENTITY> getList();

	
	@Query(value = "SELECT * FROM BRECON_USER_PROFILE_TABLE WHERE USER_ID=?1 AND del_flg='N' UNION ALL SELECT * FROM BRECON_USER_PROFILE_MOD_TABLE WHERE USER_ID=?1 AND del_flg='N'", nativeQuery = true)
	List<USER_PROFILE_ENTITY> getBlobImg(String userid);
	
	@Query(value = "select * from BRECON_USER_PROFILE_TABLE where USER_ID= ?1", nativeQuery = true)
	USER_PROFILE_ENTITY findByIdCustom(String Id);
	
}
