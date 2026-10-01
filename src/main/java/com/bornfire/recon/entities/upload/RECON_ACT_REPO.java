package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface RECON_ACT_REPO extends JpaRepository<RECON_ACT_ENTITY,String>{
	@Query(value = "SELECT * FROM RECON_ACCOUNT_TABLE", nativeQuery = true)
	List<RECON_ACT_ENTITY> getList();
}

