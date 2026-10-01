package com.bornfire.recon.entities.upload;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RECON_ACT_MST_REPO extends JpaRepository<RECON_ACT_MST_ENTITY,String>{
	@Query(value = "SELECT * FROM RECON_ACCOUNT_MASTER_TABLE where del_flg ='N'", nativeQuery = true)
	List<RECON_ACT_MST_ENTITY> getList();
	
	@Query(value = "SELECT * FROM RECON_ACCOUNT_MASTER_TABLE where del_flg ='N' and acct_num =?1", nativeQuery = true)
	RECON_ACT_MST_ENTITY getNo(String no);
}
