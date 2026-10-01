package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "RECON_DRMAIN_TABLE")
public class RECON_DRMAIN_ENTITY {
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date s_trn_dt;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date s_value_dt;
	private String s_trn_ref;
	private String s_ctry;
	private String s_related_account;
	private String s_trn_desc;
	private String s_addl_text;
	private String s_ac_ccy;
	private String s_Dr_org;
	private String s_cr_org;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date s_recon_tran_date;
	private String s_recon_type;
	private String s_recon_flg;

	private String d_arn;
	private String d_mcc;
	private BigDecimal d_auth_amnt;
	private BigDecimal d_acct_amnt;
	private String d_auth_currency;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date d_transaction_date;
	private BigDecimal d_transaction_amount;
	private String d_transaction_currency;
	private String d_transaction_type;
	private String d_cbs_account_number;
	private String d_card_number;
	private String d_card_type;
	private String d_merchant_desc;
	private String d_country_name;
	private String d_approval_code;
	private BigDecimal d_reconcile_amount;
	private String d_reconcile_cur;
	private BigDecimal d_fees;
	private String d_i_fee_ind;
	private String d_refnum;
	private String d_dr_cr;
	private BigDecimal d_markup_value;
	private String d_markup_currency;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date d_recon_tran_date;
	private String d_recon_type;
	private String d_recon_flg;

	private String entity_flg;
	private String auth_flg;
	private String modify_flg;
	private String del_flg;

	private String entry_user;
	private String modify_user;
	private String auth_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date entry_time;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date modify_time;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date auth_time;
    @Id
	private BigDecimal srl_no;
    private String S_ref1;
	private String S_ref2;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date s_recon_process_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date d_recon_process_date;
	private String free_text1;
	private String free_text2;
	private String free_text3;
	private String free_text4;
	public Date getS_trn_dt() {
		return s_trn_dt;
	}

	public void setS_trn_dt(Date s_trn_dt) {
		this.s_trn_dt = s_trn_dt;
	}

	public Date getS_value_dt() {
		return s_value_dt;
	}

	public void setS_value_dt(Date s_value_dt) {
		this.s_value_dt = s_value_dt;
	}

	public String getS_trn_ref() {
		return s_trn_ref;
	}

	public void setS_trn_ref(String s_trn_ref) {
		this.s_trn_ref = s_trn_ref;
	}

	public String getS_ctry() {
		return s_ctry;
	}

	public void setS_ctry(String s_ctry) {
		this.s_ctry = s_ctry;
	}

	public String getS_related_account() {
		return s_related_account;
	}

	public void setS_related_account(String s_related_account) {
		this.s_related_account = s_related_account;
	}

	public String getS_trn_desc() {
		return s_trn_desc;
	}

	public void setS_trn_desc(String s_trn_desc) {
		this.s_trn_desc = s_trn_desc;
	}

	public String getS_addl_text() {
		return s_addl_text;
	}

	public void setS_addl_text(String s_addl_text) {
		this.s_addl_text = s_addl_text;
	}

	public String getS_cr_org() {
		return s_cr_org;
	}

	public void setS_cr_org(String s_cr_org) {
		this.s_cr_org = s_cr_org;
	}
	public Date getS_recon_date() {
		return s_recon_tran_date;
	}

	public void setS_recon_date(Date s_recon_date) {
		this.s_recon_tran_date = s_recon_date;
	}

	public String getS_recon_type() {
		return s_recon_type;
	}

	public void setS_recon_type(String s_recon_type) {
		this.s_recon_type = s_recon_type;
	}

	public String getS_recon_flg() {
		return s_recon_flg;
	}

	public void setS_recon_flg(String s_recon_flg) {
		this.s_recon_flg = s_recon_flg;
	}

	public String getD_arn() {
		return d_arn;
	}

	public void setD_arn(String d_arn) {
		this.d_arn = d_arn;
	}

	public String getD_mcc() {
		return d_mcc;
	}

	public void setD_mcc(String d_mcc) {
		this.d_mcc = d_mcc;
	}

	public BigDecimal getD_auth_amnt() {
		return d_auth_amnt;
	}

	public void setD_auth_amnt(BigDecimal d_auth_amnt) {
		this.d_auth_amnt = d_auth_amnt;
	}

	public BigDecimal getD_acct_amnt() {
		return d_acct_amnt;
	}

	public void setD_acct_amnt(BigDecimal d_acct_amnt) {
		this.d_acct_amnt = d_acct_amnt;
	}

	public String getD_auth_currency() {
		return d_auth_currency;
	}

	public void setD_auth_currency(String d_auth_currency) {
		this.d_auth_currency = d_auth_currency;
	}

	public Date getD_transaction_date() {
		return d_transaction_date;
	}

	public void setD_transaction_date(Date d_transaction_date) {
		this.d_transaction_date = d_transaction_date;
	}

	public BigDecimal getD_transaction_amount() {
		return d_transaction_amount;
	}

	public void setD_transaction_amount(BigDecimal d_transaction_amount) {
		this.d_transaction_amount = d_transaction_amount;
	}

	public String getD_transaction_currency() {
		return d_transaction_currency;
	}

	public void setD_transaction_currency(String d_transaction_currency) {
		this.d_transaction_currency = d_transaction_currency;
	}

	public String getD_transaction_type() {
		return d_transaction_type;
	}

	public void setD_transaction_type(String d_transaction_type) {
		this.d_transaction_type = d_transaction_type;
	}

	public String getD_cbs_account_number() {
		return d_cbs_account_number;
	}

	public void setD_cbs_account_number(String d_cbs_account_number) {
		this.d_cbs_account_number = d_cbs_account_number;
	}

	public String getD_card_number() {
		return d_card_number;
	}

	public void setD_card_number(String d_card_number) {
		this.d_card_number = d_card_number;
	}

	public String getD_card_type() {
		return d_card_type;
	}

	public void setD_card_type(String d_card_type) {
		this.d_card_type = d_card_type;
	}

	public String getD_merchant_desc() {
		return d_merchant_desc;
	}

	public void setD_merchant_desc(String d_merchant_desc) {
		this.d_merchant_desc = d_merchant_desc;
	}

	public String getD_country_name() {
		return d_country_name;
	}

	public void setD_country_name(String d_country_name) {
		this.d_country_name = d_country_name;
	}

	public String getD_approval_code() {
		return d_approval_code;
	}

	public void setD_approval_code(String d_approval_code) {
		this.d_approval_code = d_approval_code;
	}

	public BigDecimal getD_reconcile_amount() {
		return d_reconcile_amount;
	}

	public void setD_reconcile_amount(BigDecimal d_reconcile_amount) {
		this.d_reconcile_amount = d_reconcile_amount;
	}

	public String getD_reconcile_cur() {
		return d_reconcile_cur;
	}

	public void setD_reconcile_cur(String d_reconcile_cur) {
		this.d_reconcile_cur = d_reconcile_cur;
	}

	public BigDecimal getD_fees() {
		return d_fees;
	}

	public void setD_fees(BigDecimal d_fees) {
		this.d_fees = d_fees;
	}

	public String getD_i_fee_ind() {
		return d_i_fee_ind;
	}

	public void setD_i_fee_ind(String d_i_fee_ind) {
		this.d_i_fee_ind = d_i_fee_ind;
	}

	public String getD_refnum() {
		return d_refnum;
	}

	public void setD_refnum(String d_refnum) {
		this.d_refnum = d_refnum;
	}

	public String getD_dr_cr() {
		return d_dr_cr;
	}

	public void setD_dr_cr(String d_dr_cr) {
		this.d_dr_cr = d_dr_cr;
	}

	public BigDecimal getD_markup_value() {
		return d_markup_value;
	}

	public void setD_markup_value(BigDecimal d_markup_value) {
		this.d_markup_value = d_markup_value;
	}

	public String getD_markup_currency() {
		return d_markup_currency;
	}

	public void setD_markup_currency(String d_markup_currency) {
		this.d_markup_currency = d_markup_currency;
	}

	public Date getD_recon_date() {
		return d_recon_tran_date;
	}

	public void setD_recon_date(Date d_recon_date) {
		this.d_recon_tran_date = d_recon_date;
	}

	public String getD_recon_type() {
		return d_recon_type;
	}

	public void setD_recon_type(String d_recon_type) {
		this.d_recon_type = d_recon_type;
	}

	public String getD_recon_flg() {
		return d_recon_flg;
	}

	public void setD_recon_flg(String d_recon_flg) {
		this.d_recon_flg = d_recon_flg;
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
	
	public String getS_ac_ccy() {
		return s_ac_ccy;
	}

	public void setS_ac_ccy(String s_ac_ccy) {
		this.s_ac_ccy = s_ac_ccy;
	}

	public String getS_Dr_org() {
		return s_Dr_org;
	}

	public void setS_Dr_org(String s_Dr_org) {
		this.s_Dr_org = s_Dr_org;
	}

	public Date getS_recon_tran_date() {
		return s_recon_tran_date;
	}

	public void setS_recon_tran_date(Date s_recon_tran_date) {
		this.s_recon_tran_date = s_recon_tran_date;
	}

	public Date getD_recon_tran_date() {
		return d_recon_tran_date;
	}

	public void setD_recon_tran_date(Date d_recon_tran_date) {
		this.d_recon_tran_date = d_recon_tran_date;
	}

	public String getS_ref1() {
		return S_ref1;
	}

	public void setS_ref1(String s_ref1) {
		S_ref1 = s_ref1;
	}

	public String getS_ref2() {
		return S_ref2;
	}

	public void setS_ref2(String s_ref2) {
		S_ref2 = s_ref2;
	}

	public Date getS_recon_process_date() {
		return s_recon_process_date;
	}

	public void setS_recon_process_date(Date s_recon_process_date) {
		this.s_recon_process_date = s_recon_process_date;
	}

	public Date getD_recon_process_date() {
		return d_recon_process_date;
	}

	public void setD_recon_process_date(Date d_recon_process_date) {
		this.d_recon_process_date = d_recon_process_date;
	}
    
	public String getFree_text1() {
		return free_text1;
	}

	public void setFree_text1(String free_text1) {
		this.free_text1 = free_text1;
	}

	public String getFree_text2() {
		return free_text2;
	}

	public void setFree_text2(String free_text2) {
		this.free_text2 = free_text2;
	}

	public String getFree_text3() {
		return free_text3;
	}

	public void setFree_text3(String free_text3) {
		this.free_text3 = free_text3;
	}

	public String getFree_text4() {
		return free_text4;
	}

	public void setFree_text4(String free_text4) {
		this.free_text4 = free_text4;
	}

	public RECON_DRMAIN_ENTITY(Date s_trn_dt, Date s_value_dt, String s_trn_ref, String s_ctry,
			String s_related_account, String s_trn_desc, String s_addl_text, String s_ac_ccy, String s_Dr_org,
			String s_cr_org, Date s_recon_tran_date, String s_recon_type, String s_recon_flg, String d_arn,
			String d_mcc, BigDecimal d_auth_amnt, BigDecimal d_acct_amnt, String d_auth_currency,
			Date d_transaction_date, BigDecimal d_transaction_amount, String d_transaction_currency,
			String d_transaction_type, String d_cbs_account_number, String d_card_number, String d_card_type,
			String d_merchant_desc, String d_country_name, String d_approval_code, BigDecimal d_reconcile_amount,
			String d_reconcile_cur, BigDecimal d_fees, String d_i_fee_ind, String d_refnum, String d_dr_cr,
			BigDecimal d_markup_value, String d_markup_currency, Date d_recon_tran_date, String d_recon_type,
			String d_recon_flg, String entity_flg, String auth_flg, String modify_flg, String del_flg,
			String entry_user, String modify_user, String auth_user, Date entry_time, Date modify_time, Date auth_time,
			BigDecimal srl_no, String s_ref1, String s_ref2, Date s_recon_process_date, Date d_recon_process_date,
			String free_text1, String free_text2, String free_text3, String free_text4) {
		super();
		this.s_trn_dt = s_trn_dt;
		this.s_value_dt = s_value_dt;
		this.s_trn_ref = s_trn_ref;
		this.s_ctry = s_ctry;
		this.s_related_account = s_related_account;
		this.s_trn_desc = s_trn_desc;
		this.s_addl_text = s_addl_text;
		this.s_ac_ccy = s_ac_ccy;
		this.s_Dr_org = s_Dr_org;
		this.s_cr_org = s_cr_org;
		this.s_recon_tran_date = s_recon_tran_date;
		this.s_recon_type = s_recon_type;
		this.s_recon_flg = s_recon_flg;
		this.d_arn = d_arn;
		this.d_mcc = d_mcc;
		this.d_auth_amnt = d_auth_amnt;
		this.d_acct_amnt = d_acct_amnt;
		this.d_auth_currency = d_auth_currency;
		this.d_transaction_date = d_transaction_date;
		this.d_transaction_amount = d_transaction_amount;
		this.d_transaction_currency = d_transaction_currency;
		this.d_transaction_type = d_transaction_type;
		this.d_cbs_account_number = d_cbs_account_number;
		this.d_card_number = d_card_number;
		this.d_card_type = d_card_type;
		this.d_merchant_desc = d_merchant_desc;
		this.d_country_name = d_country_name;
		this.d_approval_code = d_approval_code;
		this.d_reconcile_amount = d_reconcile_amount;
		this.d_reconcile_cur = d_reconcile_cur;
		this.d_fees = d_fees;
		this.d_i_fee_ind = d_i_fee_ind;
		this.d_refnum = d_refnum;
		this.d_dr_cr = d_dr_cr;
		this.d_markup_value = d_markup_value;
		this.d_markup_currency = d_markup_currency;
		this.d_recon_tran_date = d_recon_tran_date;
		this.d_recon_type = d_recon_type;
		this.d_recon_flg = d_recon_flg;
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
		S_ref1 = s_ref1;
		S_ref2 = s_ref2;
		this.s_recon_process_date = s_recon_process_date;
		this.d_recon_process_date = d_recon_process_date;
		this.free_text1 = free_text1;
		this.free_text2 = free_text2;
		this.free_text3 = free_text3;
		this.free_text4 = free_text4;
	}

	public RECON_DRMAIN_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
