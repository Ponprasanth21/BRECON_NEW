package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "RECON_PARAMETER_TABLE")
public class RECON_PARAMETER_ENTITY {
	@Id
	private BigDecimal	recon_srl;
	private String	internal_account;
	private String	external_account;
	private String	recon_ref;
	private String	period;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	start_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	end_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	next_due_date;
	private String	status;
	private String	entity_flg;
	private String	auth_flg;
	private String	modify_flg;
	private String	del_flg;
	private String	entry_user;
	private String	modify_user;
	private String	auth_user;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	entry_time;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	modify_time;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	auth_time;
	public BigDecimal getRecon_srl() {
		return recon_srl;
	}
	public void setRecon_srl(BigDecimal recon_srl) {
		this.recon_srl = recon_srl;
	}
	public String getInternal_account() {
		return internal_account;
	}
	public void setInternal_account(String internal_account) {
		this.internal_account = internal_account;
	}
	public String getExternal_account() {
		return external_account;
	}
	public void setExternal_account(String external_account) {
		this.external_account = external_account;
	}
	public String getRecon_ref() {
		return recon_ref;
	}
	public void setRecon_ref(String recon_ref) {
		this.recon_ref = recon_ref;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public Date getStart_date() {
		return start_date;
	}
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}
	public Date getEnd_date() {
		return end_date;
	}
	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}
	public Date getNext_due_date() {
		return next_due_date;
	}
	public void setNext_due_date(Date next_due_date) {
		this.next_due_date = next_due_date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public String getDel_flg() {
		return del_flg;
	}
	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
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
	public RECON_PARAMETER_ENTITY(BigDecimal recon_srl, String internal_account, String external_account,
			String recon_ref, String period, Date start_date, Date end_date, Date next_due_date, String status,
			String entity_flg, String auth_flg, String modify_flg, String del_flg, String entry_user,
			String modify_user, String auth_user, Date entry_time, Date modify_time, Date auth_time) {
		super();
		this.recon_srl = recon_srl;
		this.internal_account = internal_account;
		this.external_account = external_account;
		this.recon_ref = recon_ref;
		this.period = period;
		this.start_date = start_date;
		this.end_date = end_date;
		this.next_due_date = next_due_date;
		this.status = status;
		this.entity_flg = entity_flg;
		this.auth_flg = auth_flg;
		this.modify_flg = modify_flg;
		this.del_flg = del_flg;
		this.entry_user = entry_user;
		this.modify_user = modify_user;
		this.auth_user = auth_user;
		this.entry_time = entry_time;
		this.modify_time = modify_time;
		this.auth_time = auth_time;
	}
	public RECON_PARAMETER_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
