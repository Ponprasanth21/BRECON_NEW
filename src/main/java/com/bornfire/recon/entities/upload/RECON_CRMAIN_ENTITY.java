package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "BRECON_CRMAIN_TABLE")
public class RECON_CRMAIN_ENTITY {
	private String	s_arn;
	private String	s_tr_his_id;
	private Date	s_trxn_date;
	private String	s_trxn_time;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	s_posting_date;
	private String	s_card_number;
	private String	s_acct_currency;
	private String	s_source_curr;
	private BigDecimal	s_source_amt;
	private String	s_bill_curr;
	private BigDecimal	s_mc_bill_amt;
	private BigDecimal	s_sv_bill_amt;
	private String	s_sett_curr;
	private BigDecimal	s_sett_amt;
	private BigDecimal	s_markup_amnt_iccr;
	private String	s_transaction_ind;
	private BigDecimal	s_transaction_fee;
	private String	s_i_fee_ind;
	private BigDecimal	s_sign_ifee;
	private String	s_term_type;
	private String	s_transaction_type;
	private String	s_transaction_description;
	private String	s_type_of_card;
	private String	s_merchant_name;
	private String	s_merchant_country;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	s_recon_tran_date;
	private String	s_recon_type;
	private String	d_bo_utrnno;
	private String	d_taa_date;
	private String	d_trxn_date;
	private String	d_trxn_time;
	private String	d_post_date;
	private String	d_card_number;
	private String	d_trxn_type;
	private String	d_acct_currency;
	private String	d_source_curr;
	private BigDecimal	d_source_amt;
	private String	d_sett_curr;
	private BigDecimal	d_sett_amt;
	private String	d_bill_curr;
	private BigDecimal	d_mc_bill_amt;
	private BigDecimal	d_sv_bill_amt;
	private BigDecimal	d_trxn_fee;
	private String	d_transaction_ind;
	private BigDecimal	d_i_fee;
	private String	d_i_fee_ind;
	private String	d_auth_code;
	private String	d_merchant_name;
	private String	d_exception;
	private String	d_arn;
	private String	d_cycle_number;
	private String	d_mcc;
	private String	d_country;
	private String	d_refnum;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	d_recon_tran_date;
	private String	d_recon_type;
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
	@Id
	private BigDecimal	srl_no;
	private String	s_recon_flg;
	private String	d_recon_flg;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	s_recon_process_date;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	d_recon_process_date;
	
	 
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
	public String getS_arn() {
		return s_arn;
	}
	public void setS_arn(String s_arn) {
		this.s_arn = s_arn;
	}
	public String getS_tr_his_id() {
		return s_tr_his_id;
	}
	public void setS_tr_his_id(String s_tr_his_id) {
		this.s_tr_his_id = s_tr_his_id;
	}
	public Date getS_trxn_date() {
		return s_trxn_date;
	}
	public void setS_trxn_date(Date s_trxn_date) {
		this.s_trxn_date = s_trxn_date;
	}
	public String getS_trxn_time() {
		return s_trxn_time;
	}
	public void setS_trxn_time(String s_trxn_time) {
		this.s_trxn_time = s_trxn_time;
	}
	public Date getS_posting_date() {
		return s_posting_date;
	}
	public void setS_posting_date(Date s_posting_date) {
		this.s_posting_date = s_posting_date;
	}
	public String getS_card_number() {
		return s_card_number;
	}
	public void setS_card_number(String s_card_number) {
		this.s_card_number = s_card_number;
	}
	public String getS_acct_currency() {
		return s_acct_currency;
	}
	public void setS_acct_currency(String s_acct_currency) {
		this.s_acct_currency = s_acct_currency;
	}
	public String getS_source_curr() {
		return s_source_curr;
	}
	public void setS_source_curr(String s_source_curr) {
		this.s_source_curr = s_source_curr;
	}
	public BigDecimal getS_source_amt() {
		return s_source_amt;
	}
	public void setS_source_amt(BigDecimal s_source_amt) {
		this.s_source_amt = s_source_amt;
	}
	public String getS_bill_curr() {
		return s_bill_curr;
	}
	public void setS_bill_curr(String s_bill_curr) {
		this.s_bill_curr = s_bill_curr;
	}
	public BigDecimal getS_mc_bill_amt() {
		return s_mc_bill_amt;
	}
	public void setS_mc_bill_amt(BigDecimal s_mc_bill_amt) {
		this.s_mc_bill_amt = s_mc_bill_amt;
	}
	public BigDecimal getS_sv_bill_amt() {
		return s_sv_bill_amt;
	}
	public void setS_sv_bill_amt(BigDecimal s_sv_bill_amt) {
		this.s_sv_bill_amt = s_sv_bill_amt;
	}
	public String getS_sett_curr() {
		return s_sett_curr;
	}
	public void setS_sett_curr(String s_sett_curr) {
		this.s_sett_curr = s_sett_curr;
	}
	public BigDecimal getS_sett_amt() {
		return s_sett_amt;
	}
	public void setS_sett_amt(BigDecimal s_sett_amt) {
		this.s_sett_amt = s_sett_amt;
	}
	public BigDecimal getS_markup_amnt_iccr() {
		return s_markup_amnt_iccr;
	}
	public void setS_markup_amnt_iccr(BigDecimal s_markup_amnt_iccr) {
		this.s_markup_amnt_iccr = s_markup_amnt_iccr;
	}
	public String getS_transaction_ind() {
		return s_transaction_ind;
	}
	public void setS_transaction_ind(String s_transaction_ind) {
		this.s_transaction_ind = s_transaction_ind;
	}
	public BigDecimal getS_transaction_fee() {
		return s_transaction_fee;
	}
	public void setS_transaction_fee(BigDecimal s_transaction_fee) {
		this.s_transaction_fee = s_transaction_fee;
	}
	public String getS_i_fee_ind() {
		return s_i_fee_ind;
	}
	public void setS_i_fee_ind(String s_i_fee_ind) {
		this.s_i_fee_ind = s_i_fee_ind;
	}
	public BigDecimal getS_sign_ifee() {
		return s_sign_ifee;
	}
	public void setS_sign_ifee(BigDecimal s_sign_ifee) {
		this.s_sign_ifee = s_sign_ifee;
	}
	public String getS_term_type() {
		return s_term_type;
	}
	public void setS_term_type(String s_term_type) {
		this.s_term_type = s_term_type;
	}
	public String getS_transaction_type() {
		return s_transaction_type;
	}
	public void setS_transaction_type(String s_transaction_type) {
		this.s_transaction_type = s_transaction_type;
	}
	public String getS_transaction_description() {
		return s_transaction_description;
	}
	public void setS_transaction_description(String s_transaction_description) {
		this.s_transaction_description = s_transaction_description;
	}
	public String getS_type_of_card() {
		return s_type_of_card;
	}
	public void setS_type_of_card(String s_type_of_card) {
		this.s_type_of_card = s_type_of_card;
	}
	public String getS_merchant_name() {
		return s_merchant_name;
	}
	public void setS_merchant_name(String s_merchant_name) {
		this.s_merchant_name = s_merchant_name;
	}
	public String getS_merchant_country() {
		return s_merchant_country;
	}
	public void setS_merchant_country(String s_merchant_country) {
		this.s_merchant_country = s_merchant_country;
	}
	 
	public String getS_recon_type() {
		return s_recon_type;
	}
	public void setS_recon_type(String s_recon_type) {
		this.s_recon_type = s_recon_type;
	}
	public String getD_bo_utrnno() {
		return d_bo_utrnno;
	}
	public void setD_bo_utrnno(String d_bo_utrnno) {
		this.d_bo_utrnno = d_bo_utrnno;
	}
	public String getD_taa_date() {
		return d_taa_date;
	}
	public void setD_taa_date(String d_taa_date) {
		this.d_taa_date = d_taa_date;
	}
	public String getD_trxn_date() {
		return d_trxn_date;
	}
	public void setD_trxn_date(String d_trxn_date) {
		this.d_trxn_date = d_trxn_date;
	}
	public String getD_trxn_time() {
		return d_trxn_time;
	}
	public void setD_trxn_time(String d_trxn_time) {
		this.d_trxn_time = d_trxn_time;
	}
	public String getD_post_date() {
		return d_post_date;
	}
	public void setD_post_date(String d_post_date) {
		this.d_post_date = d_post_date;
	}
	public String getD_card_number() {
		return d_card_number;
	}
	public void setD_card_number(String d_card_number) {
		this.d_card_number = d_card_number;
	}
	public String getD_trxn_type() {
		return d_trxn_type;
	}
	public void setD_trxn_type(String d_trxn_type) {
		this.d_trxn_type = d_trxn_type;
	}
	public String getD_acct_currency() {
		return d_acct_currency;
	}
	public void setD_acct_currency(String d_acct_currency) {
		this.d_acct_currency = d_acct_currency;
	}
	public String getD_source_curr() {
		return d_source_curr;
	}
	public void setD_source_curr(String d_source_curr) {
		this.d_source_curr = d_source_curr;
	}
	public BigDecimal getD_source_amt() {
		return d_source_amt;
	}
	public void setD_source_amt(BigDecimal d_source_amt) {
		this.d_source_amt = d_source_amt;
	}
	public String getD_sett_curr() {
		return d_sett_curr;
	}
	public void setD_sett_curr(String d_sett_curr) {
		this.d_sett_curr = d_sett_curr;
	}
	public BigDecimal getD_sett_amt() {
		return d_sett_amt;
	}
	public void setD_sett_amt(BigDecimal d_sett_amt) {
		this.d_sett_amt = d_sett_amt;
	}
	public String getD_bill_curr() {
		return d_bill_curr;
	}
	public void setD_bill_curr(String d_bill_curr) {
		this.d_bill_curr = d_bill_curr;
	}
	public BigDecimal getD_mc_bill_amt() {
		return d_mc_bill_amt;
	}
	public void setD_mc_bill_amt(BigDecimal d_mc_bill_amt) {
		this.d_mc_bill_amt = d_mc_bill_amt;
	}
	public BigDecimal getD_sv_bill_amt() {
		return d_sv_bill_amt;
	}
	public void setD_sv_bill_amt(BigDecimal d_sv_bill_amt) {
		this.d_sv_bill_amt = d_sv_bill_amt;
	}
	public BigDecimal getD_trxn_fee() {
		return d_trxn_fee;
	}
	public void setD_trxn_fee(BigDecimal d_trxn_fee) {
		this.d_trxn_fee = d_trxn_fee;
	}
	public String getD_transaction_ind() {
		return d_transaction_ind;
	}
	public void setD_transaction_ind(String d_transaction_ind) {
		this.d_transaction_ind = d_transaction_ind;
	}
	public BigDecimal getD_i_fee() {
		return d_i_fee;
	}
	public void setD_i_fee(BigDecimal d_i_fee) {
		this.d_i_fee = d_i_fee;
	}
	public String getD_i_fee_ind() {
		return d_i_fee_ind;
	}
	public void setD_i_fee_ind(String d_i_fee_ind) {
		this.d_i_fee_ind = d_i_fee_ind;
	}
	public String getD_auth_code() {
		return d_auth_code;
	}
	public void setD_auth_code(String d_auth_code) {
		this.d_auth_code = d_auth_code;
	}
	public String getD_merchant_name() {
		return d_merchant_name;
	}
	public void setD_merchant_name(String d_merchant_name) {
		this.d_merchant_name = d_merchant_name;
	}
	public String getD_exception() {
		return d_exception;
	}
	public void setD_exception(String d_exception) {
		this.d_exception = d_exception;
	}
	public String getD_arn() {
		return d_arn;
	}
	public void setD_arn(String d_arn) {
		this.d_arn = d_arn;
	}
	public String getD_cycle_number() {
		return d_cycle_number;
	}
	public void setD_cycle_number(String d_cycle_number) {
		this.d_cycle_number = d_cycle_number;
	}
	public String getD_mcc() {
		return d_mcc;
	}
	public void setD_mcc(String d_mcc) {
		this.d_mcc = d_mcc;
	}
	public String getD_country() {
		return d_country;
	}
	public void setD_country(String d_country) {
		this.d_country = d_country;
	}
	public String getD_refnum() {
		return d_refnum;
	}
	public void setD_refnum(String d_refnum) {
		this.d_refnum = d_refnum;
	}
	public String getD_recon_type() {
		return d_recon_type;
	}
	public void setD_recon_type(String d_recon_type) {
		this.d_recon_type = d_recon_type;
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
	public String getS_recon_flg() {
		return s_recon_flg;
	}
	public void setS_recon_flg(String s_recon_flg) {
		this.s_recon_flg = s_recon_flg;
	}
	public String getD_recon_flg() {
		return d_recon_flg;
	}
	public void setD_recon_flg(String d_recon_flg) {
		this.d_recon_flg = d_recon_flg;
	}
	
	public RECON_CRMAIN_ENTITY(String s_arn, String s_tr_his_id, Date s_trxn_date, String s_trxn_time,
			Date s_posting_date, String s_card_number, String s_acct_currency, String s_source_curr,
			BigDecimal s_source_amt, String s_bill_curr, BigDecimal s_mc_bill_amt, BigDecimal s_sv_bill_amt,
			String s_sett_curr, BigDecimal s_sett_amt, BigDecimal s_markup_amnt_iccr, String s_transaction_ind,
			BigDecimal s_transaction_fee, String s_i_fee_ind, BigDecimal s_sign_ifee, String s_term_type,
			String s_transaction_type, String s_transaction_description, String s_type_of_card, String s_merchant_name,
			String s_merchant_country, Date s_recon_tran_date, String s_recon_type, String d_bo_utrnno, String d_taa_date,
			String d_trxn_date, String d_trxn_time, String d_post_date, String d_card_number, String d_trxn_type,
			String d_acct_currency, String d_source_curr, BigDecimal d_source_amt, String d_sett_curr,
			BigDecimal d_sett_amt, String d_bill_curr, BigDecimal d_mc_bill_amt, BigDecimal d_sv_bill_amt,
			BigDecimal d_trxn_fee, String d_transaction_ind, BigDecimal d_i_fee, String d_i_fee_ind, String d_auth_code,
			String d_merchant_name, String d_exception, String d_arn, String d_cycle_number, String d_mcc,
			String d_country, String d_refnum, Date d_recon_tran_date, String d_recon_type, String entity_flg,
			String auth_flg, String modify_flg, String del_flg, String entry_user, String modify_user, String auth_user,
			Date entry_time, Date modify_time, Date auth_time, BigDecimal srl_no, String s_recon_flg,
			String d_recon_flg, Date s_recon_process_date, Date d_recon_process_date) {
		super();
		this.s_arn = s_arn;
		this.s_tr_his_id = s_tr_his_id;
		this.s_trxn_date = s_trxn_date;
		this.s_trxn_time = s_trxn_time;
		this.s_posting_date = s_posting_date;
		this.s_card_number = s_card_number;
		this.s_acct_currency = s_acct_currency;
		this.s_source_curr = s_source_curr;
		this.s_source_amt = s_source_amt;
		this.s_bill_curr = s_bill_curr;
		this.s_mc_bill_amt = s_mc_bill_amt;
		this.s_sv_bill_amt = s_sv_bill_amt;
		this.s_sett_curr = s_sett_curr;
		this.s_sett_amt = s_sett_amt;
		this.s_markup_amnt_iccr = s_markup_amnt_iccr;
		this.s_transaction_ind = s_transaction_ind;
		this.s_transaction_fee = s_transaction_fee;
		this.s_i_fee_ind = s_i_fee_ind;
		this.s_sign_ifee = s_sign_ifee;
		this.s_term_type = s_term_type;
		this.s_transaction_type = s_transaction_type;
		this.s_transaction_description = s_transaction_description;
		this.s_type_of_card = s_type_of_card;
		this.s_merchant_name = s_merchant_name;
		this.s_merchant_country = s_merchant_country;
		this.s_recon_tran_date = s_recon_tran_date;
		this.s_recon_type = s_recon_type;
		this.d_bo_utrnno = d_bo_utrnno;
		this.d_taa_date = d_taa_date;
		this.d_trxn_date = d_trxn_date;
		this.d_trxn_time = d_trxn_time;
		this.d_post_date = d_post_date;
		this.d_card_number = d_card_number;
		this.d_trxn_type = d_trxn_type;
		this.d_acct_currency = d_acct_currency;
		this.d_source_curr = d_source_curr;
		this.d_source_amt = d_source_amt;
		this.d_sett_curr = d_sett_curr;
		this.d_sett_amt = d_sett_amt;
		this.d_bill_curr = d_bill_curr;
		this.d_mc_bill_amt = d_mc_bill_amt;
		this.d_sv_bill_amt = d_sv_bill_amt;
		this.d_trxn_fee = d_trxn_fee;
		this.d_transaction_ind = d_transaction_ind;
		this.d_i_fee = d_i_fee;
		this.d_i_fee_ind = d_i_fee_ind;
		this.d_auth_code = d_auth_code;
		this.d_merchant_name = d_merchant_name;
		this.d_exception = d_exception;
		this.d_arn = d_arn;
		this.d_cycle_number = d_cycle_number;
		this.d_mcc = d_mcc;
		this.d_country = d_country;
		this.d_refnum = d_refnum;
		this.d_recon_tran_date = d_recon_tran_date;
		this.d_recon_type = d_recon_type;
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
		this.s_recon_flg = s_recon_flg;
		this.d_recon_flg = d_recon_flg;
		this.s_recon_process_date = s_recon_process_date;
		this.d_recon_process_date = d_recon_process_date;
	}
	public RECON_CRMAIN_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
