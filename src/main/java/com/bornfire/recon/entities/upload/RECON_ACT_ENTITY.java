package com.bornfire.recon.entities.upload;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name = "RECON_ACCOUNT_TABLE")
public class RECON_ACT_ENTITY {
	private String	bank_id;
	private String	sol_id;
	@Id
	private String	acct_num;
	private String	acct_name;
	private String	acct_place_holder;
	private String	acct_access_code;
	private String	crncy_code;
	private String	crncy_cntry;
	private String	mirror_acct_num;
	private String	mirror_acct_name;
	private String	acct_bank_code;
	private String	acct_bank_name;
	private String	addr_1;
	private String	addr_2;
	private String	acct_city;
	private String	acct_cntry_code;
	private String	addl_ref;
	private String	acct_remarks;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	last_recon_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	next_recon_date;
	private String	recon_period_freq;
	private String	recon_status;
	private String	entity_flg;
	private String	del_flg;
	private String	modify_flg;
	private String	entry_user;
	private String	modify_user;
	private String	verify_user;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	entry_time;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	modify_time;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	verify_time;
	public String getBank_id() {
		return bank_id;
	}
	public void setBank_id(String bank_id) {
		this.bank_id = bank_id;
	}
	public String getSol_id() {
		return sol_id;
	}
	public void setSol_id(String sol_id) {
		this.sol_id = sol_id;
	}
	public String getAcct_num() {
		return acct_num;
	}
	public void setAcct_num(String acct_num) {
		this.acct_num = acct_num;
	}
	public String getAcct_name() {
		return acct_name;
	}
	public void setAcct_name(String acct_name) {
		this.acct_name = acct_name;
	}
	public String getAcct_place_holder() {
		return acct_place_holder;
	}
	public void setAcct_place_holder(String acct_place_holder) {
		this.acct_place_holder = acct_place_holder;
	}
	public String getAcct_access_code() {
		return acct_access_code;
	}
	public void setAcct_access_code(String acct_access_code) {
		this.acct_access_code = acct_access_code;
	}
	public String getCrncy_code() {
		return crncy_code;
	}
	public void setCrncy_code(String crncy_code) {
		this.crncy_code = crncy_code;
	}
	public String getCrncy_cntry() {
		return crncy_cntry;
	}
	public void setCrncy_cntry(String crncy_cntry) {
		this.crncy_cntry = crncy_cntry;
	}
	public String getMirror_acct_num() {
		return mirror_acct_num;
	}
	public void setMirror_acct_num(String mirror_acct_num) {
		this.mirror_acct_num = mirror_acct_num;
	}
	public String getMirror_acct_name() {
		return mirror_acct_name;
	}
	public void setMirror_acct_name(String mirror_acct_name) {
		this.mirror_acct_name = mirror_acct_name;
	}
	public String getAcct_bank_code() {
		return acct_bank_code;
	}
	public void setAcct_bank_code(String acct_bank_code) {
		this.acct_bank_code = acct_bank_code;
	}
	public String getAcct_bank_name() {
		return acct_bank_name;
	}
	public void setAcct_bank_name(String acct_bank_name) {
		this.acct_bank_name = acct_bank_name;
	}
	public String getAddr_1() {
		return addr_1;
	}
	public void setAddr_1(String addr_1) {
		this.addr_1 = addr_1;
	}
	public String getAddr_2() {
		return addr_2;
	}
	public void setAddr_2(String addr_2) {
		this.addr_2 = addr_2;
	}
	public String getAcct_city() {
		return acct_city;
	}
	public void setAcct_city(String acct_city) {
		this.acct_city = acct_city;
	}
	public String getAcct_cntry_code() {
		return acct_cntry_code;
	}
	public void setAcct_cntry_code(String acct_cntry_code) {
		this.acct_cntry_code = acct_cntry_code;
	}
	public String getAddl_ref() {
		return addl_ref;
	}
	public void setAddl_ref(String addl_ref) {
		this.addl_ref = addl_ref;
	}
	public String getAcct_remarks() {
		return acct_remarks;
	}
	public void setAcct_remarks(String acct_remarks) {
		this.acct_remarks = acct_remarks;
	}
	public Date getLast_recon_date() {
		return last_recon_date;
	}
	public void setLast_recon_date(Date last_recon_date) {
		this.last_recon_date = last_recon_date;
	}
	public Date getNext_recon_date() {
		return next_recon_date;
	}
	public void setNext_recon_date(Date next_recon_date) {
		this.next_recon_date = next_recon_date;
	}
	public String getRecon_period_freq() {
		return recon_period_freq;
	}
	public void setRecon_period_freq(String recon_period_freq) {
		this.recon_period_freq = recon_period_freq;
	}
	public String getRecon_status() {
		return recon_status;
	}
	public void setRecon_status(String recon_status) {
		this.recon_status = recon_status;
	}
	public String getEntity_flg() {
		return entity_flg;
	}
	public void setEntity_flg(String entity_flg) {
		this.entity_flg = entity_flg;
	}
	public String getDel_flg() {
		return del_flg;
	}
	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
	}
	public String getModify_flg() {
		return modify_flg;
	}
	public void setModify_flg(String modify_flg) {
		this.modify_flg = modify_flg;
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
	public String getVerify_user() {
		return verify_user;
	}
	public void setVerify_user(String verify_user) {
		this.verify_user = verify_user;
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
	public Date getVerify_time() {
		return verify_time;
	}
	public void setVerify_time(Date verify_time) {
		this.verify_time = verify_time;
	}
	public RECON_ACT_ENTITY(String bank_id, String sol_id, String acct_num, String acct_name,
			String acct_place_holder, String acct_access_code, String crncy_code, String crncy_cntry,
			String mirror_acct_num, String mirror_acct_name, String acct_bank_code, String acct_bank_name,
			String addr_1, String addr_2, String acct_city, String acct_cntry_code, String addl_ref,
			String acct_remarks, Date last_recon_date, Date next_recon_date, String recon_period_freq,
			String recon_status, String entity_flg, String del_flg, String modify_flg, String entry_user,
			String modify_user, String verify_user, Date entry_time, Date modify_time, Date verify_time) {
		super();
		this.bank_id = bank_id;
		this.sol_id = sol_id;
		this.acct_num = acct_num;
		this.acct_name = acct_name;
		this.acct_place_holder = acct_place_holder;
		this.acct_access_code = acct_access_code;
		this.crncy_code = crncy_code;
		this.crncy_cntry = crncy_cntry;
		this.mirror_acct_num = mirror_acct_num;
		this.mirror_acct_name = mirror_acct_name;
		this.acct_bank_code = acct_bank_code;
		this.acct_bank_name = acct_bank_name;
		this.addr_1 = addr_1;
		this.addr_2 = addr_2;
		this.acct_city = acct_city;
		this.acct_cntry_code = acct_cntry_code;
		this.addl_ref = addl_ref;
		this.acct_remarks = acct_remarks;
		this.last_recon_date = last_recon_date;
		this.next_recon_date = next_recon_date;
		this.recon_period_freq = recon_period_freq;
		this.recon_status = recon_status;
		this.entity_flg = entity_flg;
		this.del_flg = del_flg;
		this.modify_flg = modify_flg;
		this.entry_user = entry_user;
		this.modify_user = modify_user;
		this.verify_user = verify_user;
		this.entry_time = entry_time;
		this.modify_time = modify_time;
		this.verify_time = verify_time;
	}
	public RECON_ACT_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}

}
