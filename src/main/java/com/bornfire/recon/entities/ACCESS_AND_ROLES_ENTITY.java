package com.bornfire.recon.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BRECON_ACCES_ROLES_TABLE")
public class ACCESS_AND_ROLES_ENTITY {

	@Id
	private String	role_id;
	private String	role_desc;
	private String	permissions;
	private String	work_class;
	private String	domain_id;
	private String	admin;
	private String	entity_flg;
	private String	auth_flg;
	private String	modify_flg;
	@Column(name = "DEL_FLG")
	private String	delFlg;
	private String	menulist;
	private String	entry_user;
	private String	modify_user;
	private String	auth_user;
	private Date	entry_time;
	private Date	modify_time;
	private Date	auth_time;
	private String audit_operations;
	private String ips_operations;
	private String monitoring;
	private String myt_registration;
	@Column(name = "BRF_Reports")
	private String brfReports;

	@Column(name = "Basel_Reports")
	private String baselReports;

	@Column(name = "Archivals")
	private String archivals;

	@Column(name = "Audit_Inquiries")
	private String auditInquiries;

	@Column(name = "RBR_Reports")
	private String rbrReports;

	@Column(name = "VAT_Ledger")
	private String vatLedger;

	@Column(name = "Invoice_Data")
	private String invoiceData;
	
	public String getRole_id() {
		return role_id;
	}

	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}

	public String getRole_desc() {
		return role_desc;
	}

	public void setRole_desc(String role_desc) {
		this.role_desc = role_desc;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public String getWork_class() {
		return work_class;
	}

	public void setWork_class(String work_class) {
		this.work_class = work_class;
	}

	public String getDomain_id() {
		return domain_id;
	}

	public void setDomain_id(String domain_id) {
		this.domain_id = domain_id;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public String getEntity_flg() {
		return entity_flg;
	}

	public void setEntity_flg(String entity_flg) {
		this.entity_flg = entity_flg;
	}

	public String getAuth_flg() {
		return auth_flg;
	}

	public void setAuth_flg(String auth_flg) {
		this.auth_flg = auth_flg;
	}

	public String getModify_flg() {
		return modify_flg;
	}

	public void setModify_flg(String modify_flg) {
		this.modify_flg = modify_flg;
	}

	public String getDelFlg() {
		return delFlg;
	}

	public void setDelFlg(String delFlg) {
		this.delFlg = delFlg;
	}

	public String getMenulist() {
		return menulist;
	}

	public void setMenulist(String menulist) {
		this.menulist = menulist;
	}

	public String getEntry_user() {
		return entry_user;
	}

	public void setEntry_user(String entry_user) {
		this.entry_user = entry_user;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	public String getAuth_user() {
		return auth_user;
	}

	public void setAuth_user(String auth_user) {
		this.auth_user = auth_user;
	}

	public Date getEntry_time() {
		return entry_time;
	}

	public void setEntry_time(Date entry_time) {
		this.entry_time = entry_time;
	}

	public Date getModify_time() {
		return modify_time;
	}

	public void setModify_time(Date modify_time) {
		this.modify_time = modify_time;
	}

	public Date getAuth_time() {
		return auth_time;
	}

	public void setAuth_time(Date auth_time) {
		this.auth_time = auth_time;
	}

	public String getAudit_operations() {
		return audit_operations;
	}

	public void setAudit_operations(String audit_operations) {
		this.audit_operations = audit_operations;
	}

	public String getIps_operations() {
		return ips_operations;
	}

	public void setIps_operations(String ips_operations) {
		this.ips_operations = ips_operations;
	}

	public String getMonitoring() {
		return monitoring;
	}

	public void setMonitoring(String monitoring) {
		this.monitoring = monitoring;
	}

	public String getMyt_registration() {
		return myt_registration;
	}

	public void setMyt_registration(String myt_registration) {
		this.myt_registration = myt_registration;
	}

	public String getBrfReports() {
		return brfReports;
	}

	public void setBrfReports(String brfReports) {
		this.brfReports = brfReports;
	}

	public String getBaselReports() {
		return baselReports;
	}

	public void setBaselReports(String baselReports) {
		this.baselReports = baselReports;
	}

	public String getArchivals() {
		return archivals;
	}

	public void setArchivals(String archivals) {
		this.archivals = archivals;
	}

	public String getAuditInquiries() {
		return auditInquiries;
	}

	public void setAuditInquiries(String auditInquiries) {
		this.auditInquiries = auditInquiries;
	}

	public String getRbrReports() {
		return rbrReports;
	}

	public void setRbrReports(String rbrReports) {
		this.rbrReports = rbrReports;
	}

	public String getVatLedger() {
		return vatLedger;
	}

	public void setVatLedger(String vatLedger) {
		this.vatLedger = vatLedger;
	}

	public String getInvoiceData() {
		return invoiceData;
	}

	public void setInvoiceData(String invoiceData) {
		this.invoiceData = invoiceData;
	}
	
	

	public ACCESS_AND_ROLES_ENTITY(String role_id, String role_desc, String permissions, String work_class,
			String domain_id, String admin, String entity_flg, String auth_flg, String modify_flg, String delFlg,
			String menulist, String entry_user, String modify_user, String auth_user, Date entry_time, Date modify_time,
			Date auth_time, String audit_operations, String ips_operations, String monitoring, String myt_registration,
			String brfReports, String baselReports, String archivals, String auditInquiries, String rbrReports,
			String vatLedger, String invoiceData) {
		super();
		this.role_id = role_id;
		this.role_desc = role_desc;
		this.permissions = permissions;
		this.work_class = work_class;
		this.domain_id = domain_id;
		this.admin = admin;
		this.entity_flg = entity_flg;
		this.auth_flg = auth_flg;
		this.modify_flg = modify_flg;
		this.delFlg = delFlg;
		this.menulist = menulist;
		this.entry_user = entry_user;
		this.modify_user = modify_user;
		this.auth_user = auth_user;
		this.entry_time = entry_time;
		this.modify_time = modify_time;
		this.auth_time = auth_time;
		this.audit_operations = audit_operations;
		this.ips_operations = ips_operations;
		this.monitoring = monitoring;
		this.myt_registration = myt_registration;
		this.brfReports = brfReports;
		this.baselReports = baselReports;
		this.archivals = archivals;
		this.auditInquiries = auditInquiries;
		this.rbrReports = rbrReports;
		this.vatLedger = vatLedger;
		this.invoiceData = invoiceData;
	}
	
	

	public ACCESS_AND_ROLES_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ACCESS_AND_ROLES_ENTITY other = (ACCESS_AND_ROLES_ENTITY) obj;
		if (admin == null) {
			if (other.admin != null)
				return false;
		} else if (!admin.equals(other.admin))
			return false;
		if (audit_operations == null) {
			if (other.audit_operations != null)
				return false;
		} else if (!audit_operations.equals(other.audit_operations))
			return false;
		if (auth_flg == null) {
			if (other.auth_flg != null)
				return false;
		} else if (!auth_flg.equals(other.auth_flg))
			return false;
		if (auth_time == null) {
			if (other.auth_time != null)
				return false;
		} else if (!auth_time.equals(other.auth_time))
			return false;
		if (auth_user == null) {
			if (other.auth_user != null)
				return false;
		} else if (!auth_user.equals(other.auth_user))
			return false;
		if (delFlg == null) {
			if (other.delFlg != null)
				return false;
		} else if (!delFlg.equals(other.delFlg))
			return false;
		if (domain_id == null) {
			if (other.domain_id != null)
				return false;
		} else if (!domain_id.equals(other.domain_id))
			return false;
		if (entity_flg == null) {
			if (other.entity_flg != null)
				return false;
		} else if (!entity_flg.equals(other.entity_flg))
			return false;
		if (entry_time == null) {
			if (other.entry_time != null)
				return false;
		} else if (!entry_time.equals(other.entry_time))
			return false;
		if (entry_user == null) {
			if (other.entry_user != null)
				return false;
		} else if (!entry_user.equals(other.entry_user))
			return false;
		if (ips_operations == null) {
			if (other.ips_operations != null)
				return false;
		} else if (!ips_operations.equals(other.ips_operations))
			return false;
		if (menulist == null) {
			if (other.menulist != null)
				return false;
		} else if (!menulist.equals(other.menulist))
			return false;
		if (modify_flg == null) {
			if (other.modify_flg != null)
				return false;
		} else if (!modify_flg.equals(other.modify_flg))
			return false;
		if (modify_time == null) {
			if (other.modify_time != null)
				return false;
		} else if (!modify_time.equals(other.modify_time))
			return false;
		if (modify_user == null) {
			if (other.modify_user != null)
				return false;
		} else if (!modify_user.equals(other.modify_user))
			return false;
		if (monitoring == null) {
			if (other.monitoring != null)
				return false;
		} else if (!monitoring.equals(other.monitoring))
			return false;
		if (myt_registration == null) {
			if (other.myt_registration != null)
				return false;
		} else if (!myt_registration.equals(other.myt_registration))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		if (role_desc == null) {
			if (other.role_desc != null)
				return false;
		} else if (!role_desc.equals(other.role_desc))
			return false;
		if (role_id == null) {
			if (other.role_id != null)
				return false;
		} else if (!role_id.equals(other.role_id))
			return false;
		if (work_class == null) {
			if (other.work_class != null)
				return false;
		} else if (!work_class.equals(other.work_class))
			return false;
		return true;
	}
	
	
	
	
	
}

