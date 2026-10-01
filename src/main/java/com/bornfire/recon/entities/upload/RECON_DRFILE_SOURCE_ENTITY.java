package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "BRECON_DRFILE_SOURCE_TABLE")
public class RECON_DRFILE_SOURCE_ENTITY {
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	trn_dt;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	value_dt;
	private String	trn_ref;
	private String	ctry;
	private String	related_account;
	private String	trn_desc;
	private String	addl_text;
	private String	ac_ccy;
	private String	dr_org;
	private String	cr_org;
	private String	entity_flg;
	private String	auth_flg;
	private String	modify_flg;
	private String	del_flg;
	private String	entry_user;
	private String	modify_user;
	private String	auth_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	entry_time;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	modify_time;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	auth_time;
	@Id
	private BigDecimal	srl_no;
	private String	recon_flg;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	recon_tran_date;
	private String	recon_type;
	private Date	recon_process_date;
	private String  ref1;
	private String  ref2;
	public Date getTrn_dt() {
		return trn_dt;
	}
	public void setTrn_dt(Date trn_dt) {
		this.trn_dt = trn_dt;
	}
	public Date getValue_dt() {
		return value_dt;
	}
	public void setValue_dt(Date value_dt) {
		this.value_dt = value_dt;
	}
	public String getTrn_ref() {
		return trn_ref;
	}
	public void setTrn_ref(String trn_ref) {
		this.trn_ref = trn_ref;
	}
	public String getCtry() {
		return ctry;
	}
	public void setCtry(String ctry) {
		this.ctry = ctry;
	}
	public String getRelated_account() {
		return related_account;
	}
	public void setRelated_account(String related_account) {
		this.related_account = related_account;
	}
	public String getTrn_desc() {
		return trn_desc;
	}
	public void setTrn_desc(String trn_desc) {
		this.trn_desc = trn_desc;
	}
	public String getAddl_text() {
		return addl_text;
	}
	public void setAddl_text(String addl_text) {
		this.addl_text = addl_text;
	}
	public String getCr_org() {
		return cr_org;
	}
	public void setCr_org(String cr_org) {
		this.cr_org = cr_org;
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
	public BigDecimal getSrl_no() {
		return srl_no;
	}
	public void setSrl_no(BigDecimal srl_no) {
		this.srl_no = srl_no;
	}
	public String getRecon_flg() {
		return recon_flg;
	}
	public void setRecon_flg(String recon_flg) {
		this.recon_flg = recon_flg;
	}
	public Date getRecon_date() {
		return recon_tran_date;
	}
	public void setRecon_date(Date recon_date) {
		this.recon_tran_date = recon_date;
	}
	public String getRecon_type() {
		return recon_type;
	}
	public void setRecon_type(String recon_type) {
		this.recon_type = recon_type;
	}
	public Date getRecon_tran_date() {
		return recon_tran_date;
	}
	public void setRecon_tran_date(Date recon_tran_date) {
		this.recon_tran_date = recon_tran_date;
	}
	public Date getRecon_process_date() {
		return recon_process_date;
	}
	public void setRecon_process_date(Date recon_process_date) {
		this.recon_process_date = recon_process_date;
	}
	public String getAc_ccy() {
		return ac_ccy;
	}
	public void setAc_ccy(String ac_ccy) {
		this.ac_ccy = ac_ccy;
	}
	public String getDr_org() {
		return dr_org;
	}
	public void setDr_org(String dr_org) {
		this.dr_org = dr_org;
	}
	public String getRef1() {
		return ref1;
	}
	public void setRef1(String ref1) {
		this.ref1 = ref1;
	}
	public String getRef2() {
		return ref2;
	}
	public void setRef2(String ref2) {
		this.ref2 = ref2;
	}
	public RECON_DRFILE_SOURCE_ENTITY(Date trn_dt, Date value_dt, String trn_ref, String ctry, String related_account,
			String trn_desc, String addl_text, String ac_ccy, String dr_org, String cr_org, String entity_flg,
			String auth_flg, String modify_flg, String del_flg, String entry_user, String modify_user, String auth_user,
			Date entry_time, Date modify_time, Date auth_time, BigDecimal srl_no, String recon_flg,
			Date recon_tran_date, String recon_type, Date recon_process_date, String ref1, String ref2) {
		super();
		this.trn_dt = trn_dt;
		this.value_dt = value_dt;
		this.trn_ref = trn_ref;
		this.ctry = ctry;
		this.related_account = related_account;
		this.trn_desc = trn_desc;
		this.addl_text = addl_text;
		this.ac_ccy = ac_ccy;
		this.dr_org = dr_org;
		this.cr_org = cr_org;
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
		this.srl_no = srl_no;
		this.recon_flg = recon_flg;
		this.recon_tran_date = recon_tran_date;
		this.recon_type = recon_type;
		this.recon_process_date = recon_process_date;
		this.ref1 = ref1;
		this.ref2 = ref2;
	}
	public RECON_DRFILE_SOURCE_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
