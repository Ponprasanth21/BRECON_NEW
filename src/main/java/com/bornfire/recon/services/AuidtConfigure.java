package com.bornfire.recon.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.AUDIT_TABLE_POJO;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;


@Service
public class AuidtConfigure {

	private static final Logger logger = LoggerFactory.getLogger(AuidtConfigure.class);

	 
	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;
	
	@Autowired
	private SequenceGenerator sequence;

//user audit method
	public List<AUDIT_TABLE_POJO> getauditListLocal(Date fromdate1) {
		List<BRECON_Audit_Entity> auditList = BRECON_Audit_Rep.getauditListLocal(fromdate1);
		List<AUDIT_TABLE_POJO> auditPojoList = new ArrayList<>();

		for (BRECON_Audit_Entity ipsAudit : auditList) {
			boolean isUpdated = false;
			for (AUDIT_TABLE_POJO existingPojo : auditPojoList) {
				String auditRefNo = existingPojo.getAudit_ref_no();
				String remarks = existingPojo.getRemarks();
				String ipsAuditno = ipsAudit.getAudit_ref_no();
				if (auditRefNo != null && ipsAuditno != null
						&& existingPojo.getAudit_ref_no().equals(ipsAudit.getAudit_ref_no()) && remarks != null
						&& ("Login Successfully".equals(existingPojo.getRemarks())
								|| "Logout Successfully".equals(existingPojo.getRemarks()))) {
					existingPojo.setAudit_date(ipsAudit.getAudit_date());
					existingPojo.setAudit_table(ipsAudit.getAudit_table());
					existingPojo.setFunc_code(ipsAudit.getFunc_code());
					existingPojo.setEntry_user(ipsAudit.getEntry_user());
					existingPojo.setEntry_time(ipsAudit.getEntry_time());
					existingPojo.setAuth_user(ipsAudit.getAuth_user());
					existingPojo.setAuth_time(ipsAudit.getAuth_time());
					existingPojo.setRemarks(ipsAudit.getRemarks());
					List<String> fieldName = new ArrayList<>();
					List<String> oldvalue = new ArrayList<>();
					List<String> newvalue = new ArrayList<>();
					String[] dd = ipsAudit.getModi_details().split("\\|\\|");

					for (String str : dd) {
						String[] str1 = str.split("\\+");
						if (str1.length > 0) {
							fieldName.add(str1[0]);
						}
						if (str1.length > 1) {
							oldvalue.add(str1[1]);
						}
						if (str1.length > 2) {
							newvalue.add(str1[2]);
						}
					}
					existingPojo.setFieldName(fieldName);
					existingPojo.setOldvalue(oldvalue);
					existingPojo.setNewvalue(newvalue);
					isUpdated = true;
					break;
				}
			}

			// If no existing entry was updated, create a new one
			if (!isUpdated) {
				AUDIT_TABLE_POJO auditTablePojo = new AUDIT_TABLE_POJO();
				auditTablePojo.setAudit_date(ipsAudit.getAudit_date());
				auditTablePojo.setAudit_table(ipsAudit.getAudit_table());
				auditTablePojo.setFunc_code(ipsAudit.getFunc_code());
				auditTablePojo.setEntry_user(ipsAudit.getEntry_user());
				auditTablePojo.setEntry_time(ipsAudit.getEntry_time());
				auditTablePojo.setAuth_user(ipsAudit.getAuth_user());
				auditTablePojo.setRemarks(ipsAudit.getRemarks());

				List<String> fieldName = new ArrayList<>();
				List<String> oldvalue = new ArrayList<>();
				List<String> newvalue = new ArrayList<>();
				String[] dd = ipsAudit.getModi_details().split("\\|\\|");
				for (String str : dd) {
					String[] str1 = str.split("\\+");
					if (str1.length > 0) {
						fieldName.add(str1[0]);
					}
					if (str1.length > 1) {
						oldvalue.add(str1[1]);
					}
					if (str1.length > 2) {
						newvalue.add(str1[2]);
					}
				}
				auditTablePojo.setFieldName(fieldName);
				auditTablePojo.setOldvalue(oldvalue);
				auditTablePojo.setNewvalue(newvalue);
				auditPojoList.add(auditTablePojo);
			}
		}
		return auditPojoList;
	}
	
	//service audit method
	
	public List<AUDIT_TABLE_POJO> getAuditInquries(Date date1) {
		List<BRECON_Audit_Entity> auditList = BRECON_Audit_Rep.getauditListOpeartion(date1);
		List<AUDIT_TABLE_POJO> auditPojoList = new ArrayList<>();
		for (BRECON_Audit_Entity ipsAudit : auditList) {
			AUDIT_TABLE_POJO auditTablePojo = new AUDIT_TABLE_POJO();
			auditTablePojo.setAudit_date(ipsAudit.getAudit_date());
			auditTablePojo.setAudit_table(ipsAudit.getAudit_table());
			auditTablePojo.setFunc_code(ipsAudit.getFunc_code());
			auditTablePojo.setEntry_user(ipsAudit.getEntry_user());
			auditTablePojo.setEntry_time(ipsAudit.getEntry_time());
			auditTablePojo.setAuth_user(ipsAudit.getAuth_user());
			auditTablePojo.setRemarks(ipsAudit.getRemarks());
			List<String> fieldName = new ArrayList<String>();
			List<String> oldvalue = new ArrayList<String>();
			List<String> newvalue = new ArrayList<String>();
			String[] dd = ipsAudit.getModi_details().split("\\|\\|");
			for (String str : dd) {
				String[] str1 = str.split("\\+");
				if (str1.length > 0) {
					fieldName.add(str1[0]);
				}
				if (str1.length > 1) {
					oldvalue.add(str1[1]);
				}
				if (str1.length > 2) {
					newvalue.add(str1[2]);
				}
			}
			auditTablePojo.setFieldName(fieldName);
			auditTablePojo.setOldvalue(oldvalue);
			auditTablePojo.setNewvalue(newvalue);
			auditPojoList.add(auditTablePojo);
		}
		return auditPojoList;
	}

	public void insertAudit(String userID, String username, String fun_code , String remarks , String table  ,String screen) {
		BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		audit.setAudit_date(new Date());
		audit.setEntry_time(new Date());
		audit.setEntry_user(userID);
		audit.setFunc_code(fun_code);
		audit.setRemarks(remarks);
		audit.setAudit_table(table);
		audit.setAudit_screen(screen);
		audit.setEvent_id(userID);
		audit.setEvent_name(username);
		audit.setModi_details("-");
		audit.setAudit_ref_no(sequence.generateRequestUUId());
		BRECON_Audit_Rep.save(audit);
	}
}
