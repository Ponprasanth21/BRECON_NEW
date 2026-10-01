package com.bornfire.recon.entities;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface REFERENCE_CODE_REPO extends CrudRepository<REFERENCE_CODE_ENTITY,String> {
	
	@Query(value = "select * from BRECON_REF_MASTER ORDER BY ref_type", nativeQuery = true)
	List<REFERENCE_CODE_ENTITY> getreflist();
	
	@Query(value = "select * from BRECON_REF_MASTER where ref_id =?1", nativeQuery = true)
	REFERENCE_CODE_ENTITY getrefview(String ref_id);
	
	@Query(value = "SELECT ref_id_desc from BRECON_REF_MASTER WHERE ref_type=?1 and del_flg='N' ", nativeQuery = true)
	List<String> getReferenceList(String ref_type);

	@Query(value = "SELECT ref_id_desc from BRECON_REF_MASTER WHERE ref_type='C001' and del_flg='N' ", nativeQuery = true)
	List<String> getCountryCode();
	
	@Query(value = "SELECT ref_id_desc from BRECON_REF_MASTER WHERE ref_id=?1 and del_flg='N' ", nativeQuery = true)
	String[] getBranchDesc(String ref_id);
	
	@Query(value = "SELECT ref_id from BRECON_REF_MASTER WHERE ref_type='B001' and del_flg='N' ", nativeQuery = true)
	List<String> getBranchCode();
	
	@Query(value = "SELECT ref_id_desc from BRECON_REF_MASTER WHERE ref_type='S001' and del_flg='N' ", nativeQuery = true)
	List<String> getSolType();
}
