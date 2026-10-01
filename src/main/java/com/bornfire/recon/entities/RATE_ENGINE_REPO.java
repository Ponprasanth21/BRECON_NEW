package com.bornfire.recon.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RATE_ENGINE_REPO extends JpaRepository<RATE_ENGINE_ENTITY, String>{
	@Query(value = "select * from RULE_ENGINE_TABLE where del_flg ='N' ORDER BY rule_code , VERSION ", nativeQuery = true)
	List<RATE_ENGINE_ENTITY> getRuleList();
	
	@Query(value = "SELECT * FROM RULE_ENGINE_TABLE", nativeQuery = true)
	List<RATE_ENGINE_ENTITY> getSrlNo();
	
	@Query(value = "SELECT * FROM RULE_ENGINE_TABLE where del_flg ='N' and rule_code =?1", nativeQuery = true)
	RATE_ENGINE_ENTITY getrule(String rule);
	
	@Query(value = "SELECT BRECON_RULE_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	String getSeq();
	
	@Query(value = "SELECT * FROM RULE_ENGINE_TABLE where del_flg ='N' and unique_id =?1", nativeQuery = true)
	RATE_ENGINE_ENTITY getUnique(String rule);
	
	@Query(value = "SELECT * FROM RULE_ENGINE_TABLE where del_flg ='N' and rule_code =?1 ", nativeQuery = true)
	List<RATE_ENGINE_ENTITY> getSubrule(String rule );
}
