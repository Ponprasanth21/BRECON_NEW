package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RECON_PARAMETER_REPO extends JpaRepository<RECON_PARAMETER_ENTITY,BigDecimal>{
	@Query(value = "SELECT * FROM RECON_PARAMETER_TABLE", nativeQuery = true)
	List<RECON_PARAMETER_ENTITY> getList();
}
