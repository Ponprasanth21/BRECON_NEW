package com.bornfire.recon.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "RATE_MAINTENANCE_MOD")
public class RATE_MAINMOD_ENTITY {
	private String rate_code_desc;
	private String fxd_crncy;
	private String var_crncy;
	private String rate;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date rate_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date eff_date;
	private BigDecimal version;
	private String del_flg;
	private String entity_flg;
	private String modify_flg;
	private String verify_flg;
	private String entry_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date entry_time;
	private String modify_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date modify_time;
	private String verify_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date verify_time;
	private String upload_flg;
	private String remarks;
	private String channel;
	@Id
	private String unique_id;

	public String getRate_code_desc() {
		return rate_code_desc;
	}

	public void setRate_code_desc(String rate_code_desc) {
		this.rate_code_desc = rate_code_desc;
	}

	public String getFxd_crncy() {
		return fxd_crncy;
	}

	public void setFxd_crncy(String fxd_crncy) {
		this.fxd_crncy = fxd_crncy;
	}

	public String getVar_crncy() {
		return var_crncy;
	}

	public void setVar_crncy(String var_crncy) {
		this.var_crncy = var_crncy;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public Date getRate_date() {
		return rate_date;
	}

	public void setRate_date(Date rate_date) {
		this.rate_date = rate_date;
	}

	public Date getEff_date() {
		return eff_date;
	}

	public void setEff_date(Date eff_date) {
		this.eff_date = eff_date;
	}

	public BigDecimal getVersion() {
		return version;
	}

	public void setVersion(BigDecimal version) {
		this.version = version;
	}

	public String getDel_flg() {
		return del_flg;
	}

	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
	}

	public String getEntity_flg() {
		return entity_flg;
	}

	public void setEntity_flg(String entity_flg) {
		this.entity_flg = entity_flg;
	}

	public String getEntry_user() {
		return entry_user;
	}

	public void setEntry_user(String entry_user) {
		this.entry_user = entry_user;
	}

	public Date getEntry_time() {
		return entry_time;
	}

	public void setEntry_time(Date entry_time) {
		this.entry_time = entry_time;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	public Date getModify_time() {
		return modify_time;
	}

	public void setModify_time(Date modify_time) {
		this.modify_time = modify_time;
	}

	public String getVerify_user() {
		return verify_user;
	}

	public void setVerify_user(String verify_user) {
		this.verify_user = verify_user;
	}

	public Date getVerify_time() {
		return verify_time;
	}

	public void setVerify_time(Date verify_time) {
		this.verify_time = verify_time;
	}

	public String getUpload_flg() {
		return upload_flg;
	}

	public void setUpload_flg(String upload_flg) {
		this.upload_flg = upload_flg;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getModify_flg() {
		return modify_flg;
	}

	public void setModify_flg(String modify_flg) {
		this.modify_flg = modify_flg;
	}

	public String getVerify_flg() {
		return verify_flg;
	}

	public void setVerify_flg(String verify_flg) {
		this.verify_flg = verify_flg;
	}

	public String getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	public RATE_MAINMOD_ENTITY(String rate_code_desc, String fxd_crncy, String var_crncy, String rate, Date rate_date,
			Date eff_date, BigDecimal version, String del_flg, String entity_flg, String modify_flg, String verify_flg,
			String entry_user, Date entry_time, String modify_user, Date modify_time, String verify_user,
			Date verify_time, String upload_flg, String remarks, String channel, String unique_id) {
		super();
		this.rate_code_desc = rate_code_desc;
		this.fxd_crncy = fxd_crncy;
		this.var_crncy = var_crncy;
		this.rate = rate;
		this.rate_date = rate_date;
		this.eff_date = eff_date;
		this.version = version;
		this.del_flg = del_flg;
		this.entity_flg = entity_flg;
		this.modify_flg = modify_flg;
		this.verify_flg = verify_flg;
		this.entry_user = entry_user;
		this.entry_time = entry_time;
		this.modify_user = modify_user;
		this.modify_time = modify_time;
		this.verify_user = verify_user;
		this.verify_time = verify_time;
		this.upload_flg = upload_flg;
		this.remarks = remarks;
		this.channel = channel;
		this.unique_id = unique_id;
	}

	public RATE_MAINMOD_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}

}
