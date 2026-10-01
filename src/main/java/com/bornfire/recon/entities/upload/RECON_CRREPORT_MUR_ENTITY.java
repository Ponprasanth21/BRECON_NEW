package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "BRECON_CRREPORT_MUR")
public class RECON_CRREPORT_MUR_ENTITY {
	
	private BigDecimal	r3_mur1;
	private BigDecimal	r3_mur2;
	private BigDecimal	r5_mur1;
	private BigDecimal	r5_mur2;
	private BigDecimal	r10_mur1;
	private BigDecimal	r10_mur2;
	private BigDecimal	mur1_total;
	private BigDecimal	mur2_total;
	@Id
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	recon_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	posting_date;
	private String	trnx_id;
	private String	currency;
	private String	del_flg;
	private String	entity_flg;
	private String	entry_user;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	entry_time;
	private String	modify_user;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	modify_time;
	private String	verify_user;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	verify_time;
	private String	r7_mur1;
	private String	r7_mur2;
	
	public String getR7_mur1() {
		return r7_mur1;
	}
	public void setR7_mur1(String r7_mur1) {
		this.r7_mur1 = r7_mur1;
	}
	public String getR7_mur2() {
		return r7_mur2;
	}
	public void setR7_mur2(String r7_mur2) {
		this.r7_mur2 = r7_mur2;
	}
	public BigDecimal getR3_mur1() {
		return r3_mur1;
	}
	public void setR3_mur1(BigDecimal r3_mur1) {
		this.r3_mur1 = r3_mur1;
	}
	public BigDecimal getR3_mur2() {
		return r3_mur2;
	}
	public void setR3_mur2(BigDecimal r3_mur2) {
		this.r3_mur2 = r3_mur2;
	}
	public BigDecimal getR5_mur1() {
		return r5_mur1;
	}
	public void setR5_mur1(BigDecimal r5_mur1) {
		this.r5_mur1 = r5_mur1;
	}
	public BigDecimal getR5_mur2() {
		return r5_mur2;
	}
	public void setR5_mur2(BigDecimal r5_mur2) {
		this.r5_mur2 = r5_mur2;
	}
	public BigDecimal getR10_mur1() {
		return r10_mur1;
	}
	public void setR10_mur1(BigDecimal r10_mur1) {
		this.r10_mur1 = r10_mur1;
	}
	public BigDecimal getR10_mur2() {
		return r10_mur2;
	}
	public void setR10_mur2(BigDecimal r10_mur2) {
		this.r10_mur2 = r10_mur2;
	}
	public Date getRecon_date() {
		return recon_date;
	}
	public void setRecon_date(Date recon_date) {
		this.recon_date = recon_date;
	}
	public Date getPosting_date() {
		return posting_date;
	}
	public void setPosting_date(Date posting_date) {
		this.posting_date = posting_date;
	}
	public String getTrnx_id() {
		return trnx_id;
	}
	public void setTrnx_id(String trnx_id) {
		this.trnx_id = trnx_id;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
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
	public BigDecimal getMur1_total() {
		return mur1_total;
	}
	public void setMur1_total(BigDecimal mur1_total) {
		this.mur1_total = mur1_total;
	}
	public BigDecimal getMur2_total() {
		return mur2_total;
	}
	public void setMur2_total(BigDecimal mur2_total) {
		this.mur2_total = mur2_total;
	}

	public RECON_CRREPORT_MUR_ENTITY(BigDecimal r3_mur1, BigDecimal r3_mur2, BigDecimal r5_mur1, BigDecimal r5_mur2,
			BigDecimal r10_mur1, BigDecimal r10_mur2, BigDecimal mur1_total, BigDecimal mur2_total, Date recon_date,
			Date posting_date, String trnx_id, String currency, String del_flg, String entity_flg, String entry_user,
			Date entry_time, String modify_user, Date modify_time, String verify_user, Date verify_time, String r7_mur1,
			String r7_mur2) {
		super();
		this.r3_mur1 = r3_mur1;
		this.r3_mur2 = r3_mur2;
		this.r5_mur1 = r5_mur1;
		this.r5_mur2 = r5_mur2;
		this.r10_mur1 = r10_mur1;
		this.r10_mur2 = r10_mur2;
		this.mur1_total = mur1_total;
		this.mur2_total = mur2_total;
		this.recon_date = recon_date;
		this.posting_date = posting_date;
		this.trnx_id = trnx_id;
		this.currency = currency;
		this.del_flg = del_flg;
		this.entity_flg = entity_flg;
		this.entry_user = entry_user;
		this.entry_time = entry_time;
		this.modify_user = modify_user;
		this.modify_time = modify_time;
		this.verify_user = verify_user;
		this.verify_time = verify_time;
		this.r7_mur1 = r7_mur1;
		this.r7_mur2 = r7_mur2;
	}
	public RECON_CRREPORT_MUR_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
