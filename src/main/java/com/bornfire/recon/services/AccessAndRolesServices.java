package com.bornfire.recon.services;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_ENTITY;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_REPO;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_TEMP_ENTITY;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_TEMP_REPO;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;


@Service
@ConfigurationProperties("output")
@Transactional
public class AccessAndRolesServices {

	@Autowired
	ACCESS_AND_ROLES_REPO accessandrolesrepository;
	
	@Autowired
	ACCESS_AND_ROLES_TEMP_REPO aCCESS_AND_ROLES_TEMP_REPO;

	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	SequenceGenerator sequence;
	
	@Autowired
	BRECON_AUDIT_REPO AuditRepo;
	
	@Autowired
	AuidtConfigure audit;

	@SuppressWarnings("unchecked")
	public List<ACCESS_AND_ROLES_ENTITY> gettingaccessDetails(String roleid) {
	    return sessionFactory.getCurrentSession()
	            .createQuery("from ACCESS_AND_ROLES_ENTITY where role_id = :roleid")
	            .setParameter("roleid", roleid)
	            .getResultList();
	}

	public String addPARAMETER(
	        ACCESS_AND_ROLES_TEMP_ENTITY alertparam,
	        String formmode,
	        String adminValue,
	        String BRF_ReportsValue,
	        String Basel_ReportsValue,
	        String ArchivalValue,
	        String Audit_InquiriesValue,
	        String RBR_ReportsValue,
	        String VAT_LedgerValue,
	        String Invoice_DataValue,
	        String finalString,
	        String USERID,
	        String USERNAME
	) {
	    String msg = "";

	    try {
	        if ("add".equalsIgnoreCase(formmode)) {
	            // === ADD ===
	            alertparam.setDelFlg("N");
	            alertparam.setModify_flg("N");
	            alertparam.setEntity_flg("N");
	            alertparam.setAdmin(adminValue);
	            alertparam.setEntry_user(USERID);
	            alertparam.setEntry_time(new Date());
	            alertparam.setAuditInquiries(Audit_InquiriesValue);
	            alertparam.setBrfReports(BRF_ReportsValue);
	            alertparam.setBaselReports(Basel_ReportsValue);
	            alertparam.setArchivals(ArchivalValue);
	            alertparam.setRbrReports(RBR_ReportsValue);
	            alertparam.setVatLedger(VAT_LedgerValue);
	            alertparam.setInvoiceData(Invoice_DataValue);
	            alertparam.setMenulist(finalString);

	            audit.insertAudit(USERID, USERNAME, "ROLE ADD", "ROLE ADDED SUCCESSFULLY","ACCESS_AND_ROLES_TABLE", "ACCESS AND ROLE");
	            aCCESS_AND_ROLES_TEMP_REPO.save(alertparam);
	            msg = "New entry saved!";

	        } else if ("edit".equalsIgnoreCase(formmode)) {
	            // === EDIT ===
	            Optional<ACCESS_AND_ROLES_ENTITY> userOpt =
	                    accessandrolesrepository.findById(alertparam.getRole_id());

	            if (userOpt.isPresent()) {
	                ACCESS_AND_ROLES_ENTITY mainUser = userOpt.get();

	                boolean noChange =
	                        (Objects.equals(mainUser.getRole_desc(), alertparam.getRole_desc())) &&
	                        (Objects.equals(mainUser.getPermissions(), alertparam.getPermissions())) &&
	                        (Objects.equals(mainUser.getWork_class(), alertparam.getWork_class())) &&
	                        (Objects.equals(mainUser.getDomain_id(), alertparam.getDomain_id())) &&
	                        (Objects.equals(mainUser.getMenulist(), finalString)) &&
	                        (Objects.equals(mainUser.getAdmin(), adminValue)) &&
	                        (Objects.equals(mainUser.getBrfReports(), BRF_ReportsValue)) &&
	                        (Objects.equals(mainUser.getBaselReports(), Basel_ReportsValue)) &&
	                        (Objects.equals(mainUser.getArchivals(), ArchivalValue)) &&
	                        (Objects.equals(mainUser.getAuditInquiries(), Audit_InquiriesValue)) &&
	                        (Objects.equals(mainUser.getRbrReports(), RBR_ReportsValue)) &&
	                        (Objects.equals(mainUser.getVatLedger(), VAT_LedgerValue)) &&
	                        (Objects.equals(mainUser.getInvoiceData(), Invoice_DataValue));

	                if (noChange) {
	                    msg = "No Modification done";
	                } else {
	                    ACCESS_AND_ROLES_TEMP_ENTITY tempUser = new ACCESS_AND_ROLES_TEMP_ENTITY();

	                    tempUser.setRole_id(mainUser.getRole_id());
	                    tempUser.setRole_desc(alertparam.getRole_desc());
	                    tempUser.setPermissions(alertparam.getPermissions());
	                    tempUser.setWork_class(alertparam.getWork_class());
	                    tempUser.setDomain_id(alertparam.getDomain_id());

	                    tempUser.setAdmin(adminValue);
	                    tempUser.setMenulist(finalString);
	                    tempUser.setBrfReports(BRF_ReportsValue);
	                    tempUser.setBaselReports(Basel_ReportsValue);
	                    tempUser.setArchivals(ArchivalValue);
	                    tempUser.setAuditInquiries(Audit_InquiriesValue);
	                    tempUser.setRbrReports(RBR_ReportsValue);
	                    tempUser.setVatLedger(VAT_LedgerValue);
	                    tempUser.setInvoiceData(Invoice_DataValue);

	                    // Preserve entry info
	                    tempUser.setEntry_user(mainUser.getEntry_user());
	                    tempUser.setEntry_time(mainUser.getEntry_time());

	                    // Modify info
	                    tempUser.setModify_user(USERID);
	                    tempUser.setModify_time(new Date());
	                    tempUser.setDelFlg("N");
	                    tempUser.setModify_flg("Y");
	                    tempUser.setEntity_flg("N");

	                    aCCESS_AND_ROLES_TEMP_REPO.save(tempUser);
	                 // ✅ Delete main record after modification
	                    accessandrolesrepository.delete(mainUser);

	                    msg = "Role Updated Successfully!";

	                    // === AUDIT LOG ===
	                    StringBuilder changes = new StringBuilder();
	                    if (!Objects.equals(mainUser.getRole_desc(), alertparam.getRole_desc()))
	                        changes.append("Role Description+" + mainUser.getRole_desc() + "+" + alertparam.getRole_desc() + "||");
	                    if (!Objects.equals(mainUser.getPermissions(), alertparam.getPermissions()))
	                        changes.append("Permissions+" + mainUser.getPermissions() + "+" + alertparam.getPermissions() + "||");
	                    if (!Objects.equals(mainUser.getWork_class(), alertparam.getWork_class()))
	                        changes.append("Work Class+" + mainUser.getWork_class() + "+" + alertparam.getWork_class() + "||");
	                    if (!Objects.equals(mainUser.getDomain_id(), alertparam.getDomain_id()))
	                        changes.append("Domain ID+" + mainUser.getDomain_id() + "+" + alertparam.getDomain_id() + "||");
	                    if (!Objects.equals(mainUser.getMenulist(), finalString))
	                        changes.append("Menu List+" + mainUser.getMenulist() + "+" + finalString + "||");
	                    if (!Objects.equals(mainUser.getAdmin(), adminValue))
	                        changes.append("Admin+" + mainUser.getAdmin() + "+" + adminValue + "||");
	                    if (!Objects.equals(mainUser.getBrfReports(), BRF_ReportsValue))
	                        changes.append("BRF Reports+" + mainUser.getBrfReports() + "+" + BRF_ReportsValue + "||");
	                    if (!Objects.equals(mainUser.getBaselReports(), Basel_ReportsValue))
	                        changes.append("Basel Reports+" + mainUser.getBaselReports() + "+" + Basel_ReportsValue + "||");
	                    if (!Objects.equals(mainUser.getArchivals(), ArchivalValue))
	                        changes.append("Archivals+" + mainUser.getArchivals() + "+" + ArchivalValue + "||");
	                    if (!Objects.equals(mainUser.getAuditInquiries(), Audit_InquiriesValue))
	                        changes.append("Audit Inquiries+" + mainUser.getAuditInquiries() + "+" + Audit_InquiriesValue + "||");
	                    if (!Objects.equals(mainUser.getRbrReports(), RBR_ReportsValue))
	                        changes.append("RBR Reports+" + mainUser.getRbrReports() + "+" + RBR_ReportsValue + "||");
	                    if (!Objects.equals(mainUser.getVatLedger(), VAT_LedgerValue))
	                        changes.append("VAT Ledger+" + mainUser.getVatLedger() + "+" + VAT_LedgerValue + "||");
	                    if (!Objects.equals(mainUser.getInvoiceData(), Invoice_DataValue))
	                        changes.append("Invoice Data+" + mainUser.getInvoiceData() + "+" + Invoice_DataValue + "||");
	                    
	                    audit.insertAudit(USERID, USERNAME, "ROLE MODIFICATION", "ROLE MODIFIED SUCCESSFULLY","ACCESS_AND_ROLES_TABLE", "ACCESS AND ROLE");
	                }
	            } else {
	                msg = "Role not found!";
	            }

	        } else if ("delete".equalsIgnoreCase(formmode)) {
	            // === DELETE ===
	            Optional<ACCESS_AND_ROLES_TEMP_ENTITY> userOpt =
	                    aCCESS_AND_ROLES_TEMP_REPO.findById(alertparam.getRole_id());

	            if (userOpt.isPresent()) {
	                ACCESS_AND_ROLES_TEMP_ENTITY tempUser = userOpt.get();

	                // Mark as deleted instead of direct delete (safer for audit)
	                tempUser.setDelFlg("Y");
	                tempUser.setEntity_flg("N");
	                tempUser.setModify_user(USERID);
	                tempUser.setModify_time(new Date());
	                
	                audit.insertAudit(USERID, USERNAME, "ROLE DELETE", "ROLE DELETED SUCCESSFULLY","ACCESS_AND_ROLES_TABLE", "ACCESS AND ROLE");
	                aCCESS_AND_ROLES_TEMP_REPO.save(tempUser);

	                msg = "Role Deleted Successfully";
	            } else {
	                msg = "Role not found!";
	            }

	        } else if ("verify".equalsIgnoreCase(formmode)) {
	            // === VERIFY ===
	            Optional<ACCESS_AND_ROLES_TEMP_ENTITY> userOpt =
	                    aCCESS_AND_ROLES_TEMP_REPO.findById(alertparam.getRole_id());

	            if (userOpt.isPresent()) {
	                ACCESS_AND_ROLES_TEMP_ENTITY tempUser = userOpt.get();
	                ACCESS_AND_ROLES_ENTITY verifiedUser = new ACCESS_AND_ROLES_ENTITY();

	                BeanUtils.copyProperties(tempUser, verifiedUser);

	                verifiedUser.setDelFlg("N");
	                verifiedUser.setModify_flg("N");
	                verifiedUser.setEntity_flg("Y");
	                verifiedUser.setAuth_user(USERID);
	                verifiedUser.setAuth_time(new Date());

	                audit.insertAudit(USERID, USERNAME, "ROLE VERIFY", "ROLE VERIFIED  SUCCESSFULLY","ACCESS_AND_ROLES_TABLE", "ACCESS AND ROLE");
	                accessandrolesrepository.save(verifiedUser);

	                aCCESS_AND_ROLES_TEMP_REPO.delete(tempUser);

	                msg = "Role Verified Successfully!";
	            } else {
	                msg = "Role not found!";
	            }
	        }

	    } catch (Exception e) {
	        msg = "Error occurred. Please contact Administrator.";
	        e.printStackTrace();
	    }

	    return msg;
	}


	public ACCESS_AND_ROLES_TEMP_ENTITY getRoleId(String id) {
		Session session = sessionFactory.getCurrentSession();
		Query<ACCESS_AND_ROLES_TEMP_ENTITY> query = session
				.createQuery(" from ACCESS_AND_ROLES_TEMP_ENTITY where role_id=?1 ", ACCESS_AND_ROLES_TEMP_ENTITY.class);
		query.setParameter(1, id);
		List<ACCESS_AND_ROLES_TEMP_ENTITY> result = query.getResultList();
		if (!result.isEmpty()) {
			return result.get(0);
		} else {
			return new ACCESS_AND_ROLES_TEMP_ENTITY();
		}

	}
	
	public ACCESS_AND_ROLES_ENTITY getRoleIdedit(String id) {
		Session session = sessionFactory.getCurrentSession();
		Query<ACCESS_AND_ROLES_ENTITY> query = session
				.createQuery(" from ACCESS_AND_ROLES_ENTITY where role_id=?1 ", ACCESS_AND_ROLES_ENTITY.class);
		query.setParameter(1, id);
		List<ACCESS_AND_ROLES_ENTITY> result = query.getResultList();
		if (!result.isEmpty()) {
			return result.get(0);
		} else {
			return new ACCESS_AND_ROLES_ENTITY();
		}

	}
	
	public Object getRoleIdView(String id) {
	    Session session = sessionFactory.getCurrentSession();

	    // First check main entity
	    Query<ACCESS_AND_ROLES_ENTITY> mainQuery = session.createQuery(
	        "from ACCESS_AND_ROLES_ENTITY where role_id = :roleId",
	        ACCESS_AND_ROLES_ENTITY.class
	    );
	    mainQuery.setParameter("roleId", id);
	    List<ACCESS_AND_ROLES_ENTITY> mainResult = mainQuery.getResultList();

	    if (!mainResult.isEmpty()) {
	        return mainResult.get(0);
	    }

	    // If not found, check TEMP entity
	    Query<ACCESS_AND_ROLES_TEMP_ENTITY> tempQuery = session.createQuery(
	        "from ACCESS_AND_ROLES_TEMP_ENTITY where role_id = :roleId",
	        ACCESS_AND_ROLES_TEMP_ENTITY.class
	    );
	    tempQuery.setParameter("roleId", id);
	    List<ACCESS_AND_ROLES_TEMP_ENTITY> tempResult = tempQuery.getResultList();

	    if (!tempResult.isEmpty()) {
	        return tempResult.get(0);
	    }

	    // If not found in both, return empty ENTITY by default
	    return new ACCESS_AND_ROLES_ENTITY();
	}


	public ACCESS_AND_ROLES_ENTITY getRoleMenu(String id) {
		Session session = sessionFactory.getCurrentSession();
		Query<ACCESS_AND_ROLES_ENTITY> query = session.createQuery(" from ACCESS_AND_ROLES_ENTITY where role_id=?1", ACCESS_AND_ROLES_ENTITY.class);
		query.setParameter(1, id);
		List<ACCESS_AND_ROLES_ENTITY> result = query.getResultList();
		if (!result.isEmpty()) {
			return result.get(0);
		} else {
			return new ACCESS_AND_ROLES_ENTITY();
		}

	}

	public String deleteRole(String userid) {
		Session hs = sessionFactory.getCurrentSession();
		Query qr;
		qr = hs.createQuery("select count(*) from UserProfile where role_id= ?1");
		qr.setParameter(1, userid);
		long count = (long) qr.getSingleResult();
		String msg = "";
		if (count == 0) {
			Optional<ACCESS_AND_ROLES_ENTITY> user = accessandrolesrepository.findById(userid);
			ACCESS_AND_ROLES_ENTITY reg = user.get();
			reg.setDelFlg("Y");
			accessandrolesrepository.save(reg);
			msg = "Role Deleted Successfully";
		} else {
			msg = "This role has been assigned to an User.Cannot Delete ";
		}
		return msg;
	}
}