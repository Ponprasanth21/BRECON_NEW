package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "BRECON_CRFILE_SOURCE_TABLE")
public class RECON_CRFILE_SOURCE_ENTITY {
	
	private String	arn;
	private String	tr_his_id;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	trxn_date;
	private String	trxn_time;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	posting_date;
	private String	card_number;
	private String	acct_currency;
	private String	source_curr;
	private BigDecimal	source_amt;
	private String	bill_curr;
	private BigDecimal	mc_bill_amt;
	private BigDecimal	sv_bill_amt;
	private String	sett_curr;
	private BigDecimal	sett_amt;
	private BigDecimal	markup_amnt_iccr;
	private String	transaction_ind;
	private BigDecimal	transaction_fee;
	private String	i_fee_ind;
	private BigDecimal	sign_ifee;
	private String	term_type;
	private String	transaction_type;
	private String	transaction_description;
	private String	type_of_card;
	private String	merchant_name;
	private String	merchant_country;
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
    private String	recon_flg;
    private String	recon_type;
    private String  abs_acct_number;
    @DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	recon_tran_date;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	recon_process_date;
	
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
	public String getRecon_type() {
		return recon_type;
	}

	public void setRecon_type(String recon_type) {
		this.recon_type = recon_type;
	}
	
	public String getRecon_flg() {
		return recon_flg;
	}

	public void setRecon_flg(String recon_flg) {
		this.recon_flg = recon_flg;
	}
	
	public BigDecimal getSrl_no() {
		return srl_no;
	}
	public void setSrl_no(BigDecimal srl_no) {
		this.srl_no = srl_no;
	}
	public String getArn() {
		return arn;
	}
	public void setArn(String arn) {
		this.arn = arn;
	}
	public String getTr_his_id() {
		return tr_his_id;
	}
	public void setTr_his_id(String tr_his_id) {
		this.tr_his_id = tr_his_id;
	}
	public Date getTrxn_date() {
		return trxn_date;
	}
	public void setTrxn_date(Date trxn_date) {
		this.trxn_date = trxn_date;
	}
	public String getTrxn_time() {
		return trxn_time;
	}
	public void setTrxn_time(String trxn_time) {
		this.trxn_time = trxn_time;
	}
	public Date getPosting_date() {
		return posting_date;
	}
	public void setPosting_date(Date posting_date) {
		this.posting_date = posting_date;
	}
	public String getCard_number() {
		return card_number;
	}
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}
	public String getAcct_currency() {
		return acct_currency;
	}
	public void setAcct_currency(String acct_currency) {
		this.acct_currency = acct_currency;
	}
	public String getSource_curr() {
		return source_curr;
	}
	public void setSource_curr(String source_curr) {
		this.source_curr = source_curr;
	}
	public BigDecimal getSource_amt() {
		return source_amt;
	}
	public void setSource_amt(BigDecimal source_amt) {
		this.source_amt = source_amt;
	}
	public String getBill_curr() {
		return bill_curr;
	}
	public void setBill_curr(String bill_curr) {
		this.bill_curr = bill_curr;
	}
	public BigDecimal getMc_bill_amt() {
		return mc_bill_amt;
	}
	public void setMc_bill_amt(BigDecimal mc_bill_amt) {
		this.mc_bill_amt = mc_bill_amt;
	}
	public BigDecimal getSv_bill_amt() {
		return sv_bill_amt;
	}
	public void setSv_bill_amt(BigDecimal sv_bill_amt) {
		this.sv_bill_amt = sv_bill_amt;
	}
	public String getSett_curr() {
		return sett_curr;
	}
	public void setSett_curr(String sett_curr) {
		this.sett_curr = sett_curr;
	}
	public BigDecimal getSett_amt() {
		return sett_amt;
	}
	public void setSett_amt(BigDecimal sett_amt) {
		this.sett_amt = sett_amt;
	}
	public BigDecimal getMarkup_amnt_iccr() {
		return markup_amnt_iccr;
	}
	public void setMarkup_amnt_iccr(BigDecimal markup_amnt_iccr) {
		this.markup_amnt_iccr = markup_amnt_iccr;
	}
	public String getTransaction_ind() {
		return transaction_ind;
	}
	public void setTransaction_ind(String transaction_ind) {
		this.transaction_ind = transaction_ind;
	}
	public BigDecimal getTransaction_fee() {
		return transaction_fee;
	}
	public void setTransaction_fee(BigDecimal transaction_fee) {
		this.transaction_fee = transaction_fee;
	}
	public String getI_fee_ind() {
		return i_fee_ind;
	}
	public void setI_fee_ind(String i_fee_ind) {
		this.i_fee_ind = i_fee_ind;
	}
	public BigDecimal getSign_ifee() {
		return sign_ifee;
	}
	public void setSign_ifee(BigDecimal sign_ifee) {
		this.sign_ifee = sign_ifee;
	}
	public String getTerm_type() {
		return term_type;
	}
	public void setTerm_type(String term_type) {
		this.term_type = term_type;
	}
	public String getTransaction_type() {
		return transaction_type;
	}
	public void setTransaction_type(String transaction_type) {
		this.transaction_type = transaction_type;
	}
	public String getTransaction_description() {
		return transaction_description;
	}
	public void setTransaction_description(String transaction_description) {
		this.transaction_description = transaction_description;
	}
	public String getType_of_card() {
		return type_of_card;
	}
	public void setType_of_card(String type_of_card) {
		this.type_of_card = type_of_card;
	}
	public String getMerchant_name() {
		return merchant_name;
	}
	public void setMerchant_name(String merchant_name) {
		this.merchant_name = merchant_name;
	}
	public String getMerchant_country() {
		return merchant_country;
	}
	public void setMerchant_country(String merchant_country) {
		this.merchant_country = merchant_country;
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
	public String getAbs_acct_number() {
		return abs_acct_number;
	}
	public void setAbs_acct_number(String abs_acct_number) {
		this.abs_acct_number = abs_acct_number;
	}
	 
	public RECON_CRFILE_SOURCE_ENTITY(String arn, String tr_his_id, Date trxn_date, String trxn_time, Date posting_date,
			String card_number, String acct_currency, String source_curr, BigDecimal source_amt, String bill_curr,
			BigDecimal mc_bill_amt, BigDecimal sv_bill_amt, String sett_curr, BigDecimal sett_amt,
			BigDecimal markup_amnt_iccr, String transaction_ind, BigDecimal transaction_fee, String i_fee_ind,
			BigDecimal sign_ifee, String term_type, String transaction_type, String transaction_description,
			String type_of_card, String merchant_name, String merchant_country, String entity_flg, String auth_flg,
			String modify_flg, String del_flg, String entry_user, String modify_user, String auth_user, Date entry_time,
			Date modify_time, Date auth_time, BigDecimal srl_no, String recon_flg, String recon_type,
			String abs_acct_number, Date recon_tran_date, Date recon_process_date) {
		super();
		this.arn = arn;
		this.tr_his_id = tr_his_id;
		this.trxn_date = trxn_date;
		this.trxn_time = trxn_time;
		this.posting_date = posting_date;
		this.card_number = card_number;
		this.acct_currency = acct_currency;
		this.source_curr = source_curr;
		this.source_amt = source_amt;
		this.bill_curr = bill_curr;
		this.mc_bill_amt = mc_bill_amt;
		this.sv_bill_amt = sv_bill_amt;
		this.sett_curr = sett_curr;
		this.sett_amt = sett_amt;
		this.markup_amnt_iccr = markup_amnt_iccr;
		this.transaction_ind = transaction_ind;
		this.transaction_fee = transaction_fee;
		this.i_fee_ind = i_fee_ind;
		this.sign_ifee = sign_ifee;
		this.term_type = term_type;
		this.transaction_type = transaction_type;
		this.transaction_description = transaction_description;
		this.type_of_card = type_of_card;
		this.merchant_name = merchant_name;
		this.merchant_country = merchant_country;
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
		this.recon_type = recon_type;
		this.abs_acct_number = abs_acct_number;
		this.recon_tran_date = recon_tran_date;
		this.recon_process_date = recon_process_date;
	}
	public RECON_CRFILE_SOURCE_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
