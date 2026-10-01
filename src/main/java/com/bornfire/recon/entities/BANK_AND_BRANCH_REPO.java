package com.bornfire.recon.entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BANK_AND_BRANCH_REPO extends JpaRepository<BANK_AND_BRANCH_BEAN, String> {

	Optional<BANK_AND_BRANCH_BEAN> findById(String directorId);

	@Query(value = "select * from BRECON_BANK_AND_BRANCH", nativeQuery = true)
	Page<BANK_AND_BRANCH_BEAN> BankandBranchList(Pageable page);

	@Query(value = "select * from BRECON_BANK_AND_BRANCH where sol_id=?1 ", nativeQuery = true)
	BANK_AND_BRANCH_BEAN findByIdcustom(String solId);

	@Query(value = "select * from BRECON_BANK_AND_BRANCH ORDER BY SOL_ID ASC", nativeQuery = true)
	List<BANK_AND_BRANCH_BEAN> BankandBranchList();

	@Query(value = "select count(*) from BRECON_BANK_AND_BRANCH where del_flg='N'  and sol_id=?1 ", nativeQuery = true)
	String getusercount(String sol_id);

	@Query(value = "SELECT * FROM BRECON_BANK_AND_BRANCH WHERE entity_flg = 'Y' AND del_flg = 'N' " + "UNION ALL "
			+ "SELECT * FROM BRECON_BANK_AND_BRANCH_MOD WHERE entity_flg = 'N' AND del_flg = 'N'", nativeQuery = true)
	List<BANK_AND_BRANCH_BEAN> findAllByCustom();
}
