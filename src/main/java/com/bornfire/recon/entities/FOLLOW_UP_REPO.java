package com.bornfire.recon.entities;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FOLLOW_UP_REPO extends CrudRepository<FOLLOW_UP_ENTITY,String>{
	
	@Query(value = "SELECT * FROM FOLLOW_UP_TABLE", nativeQuery = true)
	List<FOLLOW_UP_ENTITY> getList();

}
