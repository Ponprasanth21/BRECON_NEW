package com.bornfire.recon.entities;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RATE_MAINMOD_REPO extends CrudRepository<RATE_MAINMOD_ENTITY,String>{
	
	@Query(value = "SELECT NVL(MAX(version), 0) FROM RATE_MAINTENANCE", nativeQuery = true)
	BigDecimal findMaxversion();
	
	@Query(value = "SELECT BRECON_RATE.NEXTVAL FROM DUAL", nativeQuery = true)
	String getNextAuditSrlNo();
	
	@Query("SELECT MAX(r.version) FROM RATE_MAIN_ENTITY r WHERE r.fxd_crncy = :fxd AND r.var_crncy = :var")
	BigDecimal findMaxVersionByCurrencyPair(@Param("fxd") String fxd, @Param("var") String var);

}
