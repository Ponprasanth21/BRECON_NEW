package com.bornfire.recon.entities;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface BRECON_AUDIT_REPO extends JpaRepository<BRECON_Audit_Entity, String>{
	
	@Query(value = "select * from BRECON_AUDIT_TABLE where trunc(audit_date)= ?1  AND audit_table not in ('BRECON_USER_PROFILE_TABLE','BRECON_USER_PROFILE_MOD_TABLE') order by audit_date desc", nativeQuery = true)
	List<BRECON_Audit_Entity> getauditListOpeartion(Date Fromdate);

	@Query(value = "select * from BRECON_AUDIT_TABLE where trunc(audit_date)= ?1   AND audit_table  in ('BRECON_USER_PROFILE_TABLE','BRECON_USER_PROFILE_MOD_TABLE') and modi_details is not null order by entry_time desc", nativeQuery = true)
	List<BRECON_Audit_Entity> getauditListLocal(Date Fromdate);
	
	@Query(value = "select * from BRECON_AUDIT_TABLE where event_id=?1 and func_code =?2 order by audit_ref_no desc fetch first 1 row only ", nativeQuery = true)
	BRECON_Audit_Entity getModifyList1(String userid, String func_code);
	
	@Query(value = "select * from BRECON_AUDIT_TABLE where event_id=?1 and func_code ='USER MODIFICATION' order by audit_ref_no desc fetch first 1 row only ", nativeQuery = true)
	BRECON_Audit_Entity getModifyList(String userid, String func_code);
	
	@Query(value = "SELECT * FROM BRECON_AUDIT_TABLE", nativeQuery = true)
	List<BRECON_Audit_Entity> getUserAuditList();

	@Query(value = "SELECT BRECON_AUDIT_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
	Long getAuditRefUUID();
 
}
