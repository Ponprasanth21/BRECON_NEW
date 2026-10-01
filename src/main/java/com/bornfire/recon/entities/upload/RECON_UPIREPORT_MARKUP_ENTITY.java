package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "brecon_upi_markup_local")
public class RECON_UPIREPORT_MARKUP_ENTITY {

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date trn_dt;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date value_dt;

	private String trn_ref;
	private String ctry;
	private String related_account;
	private String trn_desc;
	@Id
	private String addl_text;
	private String ac_ccy;
	private String dr_org;
	private String cr_org;

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date recon_date;
	private String mur_amt;
	private String d_auth_amnt;
	private String d_acct_amnt;
	private String d_auth_currency;
	private String d_transaction_amount;
	private String d_transaction_currency;
	private String markupv;
	private String d_reconcile_amount;

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

	public String getCr_org() {
		return cr_org;
	}

	public void setCr_org(String cr_org) {
		this.cr_org = cr_org;
	}

	public Date getRecon_date() {
		return recon_date;
	}

	public void setRecon_date(Date recon_date) {
		this.recon_date = recon_date;
	}

	public String getMur_amt() {
		return mur_amt;
	}

	public void setMur_amt(String mur_amt) {
		this.mur_amt = mur_amt;
	}

	public String getD_auth_amnt() {
		return d_auth_amnt;
	}

	public void setD_auth_amnt(String d_auth_amnt) {
		this.d_auth_amnt = d_auth_amnt;
	}

	public String getD_acct_amnt() {
		return d_acct_amnt;
	}

	public void setD_acct_amnt(String d_acct_amnt) {
		this.d_acct_amnt = d_acct_amnt;
	}

	public String getD_auth_currency() {
		return d_auth_currency;
	}

	public void setD_auth_currency(String d_auth_currency) {
		this.d_auth_currency = d_auth_currency;
	}

	public String getD_transaction_amount() {
		return d_transaction_amount;
	}

	public void setD_transaction_amount(String d_transaction_amount) {
		this.d_transaction_amount = d_transaction_amount;
	}

	public String getD_transaction_currency() {
		return d_transaction_currency;
	}

	public void setD_transaction_currency(String d_transaction_currency) {
		this.d_transaction_currency = d_transaction_currency;
	}

	public String getMarkupv() {
		return markupv;
	}

	public void setMarkupv(String markupv) {
		this.markupv = markupv;
	}

	public String getD_reconcile_amount() {
		return d_reconcile_amount;
	}

	public void setD_reconcile_amount(String d_reconcile_amount) {
		this.d_reconcile_amount = d_reconcile_amount;
	}

	public RECON_UPIREPORT_MARKUP_ENTITY(Date trn_dt, Date value_dt, String trn_ref, String ctry,
			String related_account, String trn_desc, String addl_text, String ac_ccy, String dr_org, String cr_org,
			Date recon_date, String mur_amt, String d_auth_amnt, String d_acct_amnt, String d_auth_currency,
			String d_transaction_amount, String d_transaction_currency, String markupv, String d_reconcile_amount) {
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
		this.recon_date = recon_date;
		this.mur_amt = mur_amt;
		this.d_auth_amnt = d_auth_amnt;
		this.d_acct_amnt = d_acct_amnt;
		this.d_auth_currency = d_auth_currency;
		this.d_transaction_amount = d_transaction_amount;
		this.d_transaction_currency = d_transaction_currency;
		this.markupv = markupv;
		this.d_reconcile_amount = d_reconcile_amount;
	}

	public RECON_UPIREPORT_MARKUP_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}

}
