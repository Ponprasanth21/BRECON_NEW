package com.bornfire.recon.entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface BANK_AND_BRANCH_MOD_REPO extends JpaRepository<BANK_AND_BRANCH_BEANMOD, String>{
	
	
	 Optional<BANK_AND_BRANCH_BEANMOD> findById( String directorId); 
	 
	 @Query(value = "select * from BRECON_BANK_AND_BRANCH_MOD ", nativeQuery = true) 
	 Page<BANK_AND_BRANCH_BEANMOD> BankandBranchList(Pageable page);
	 
	 
	 @Query(value = "select * from BRECON_BANK_AND_BRANCH_MOD where sol_id=?1 ", nativeQuery = true) 
	 BANK_AND_BRANCH_BEANMOD findByIdcustom(String solId);
	 
	 @Query(value = "select * from BRECON_BANK_AND_BRANCH_MOD ORDER BY SOL_ID ASC", nativeQuery = true) 
	 List<BANK_AND_BRANCH_BEANMOD> BankandBranchList();

}
