package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "BRECON_UPI_DESTINATION_TABLE")
public class RECON_UPI_DESTINATION_ENTITY {
	private String	sttl_date;
	private String	arn;
	private String	mcc;
	private BigDecimal	auth_amnt;
	private BigDecimal	acct_amnt;
	private String	auth_currency;
	private String	transaction_date;
	private String	refnum;
	private BigDecimal	reconcile_amount;
	private String	reconcile_cur;
	private BigDecimal	transaction_amount;
	private String	transaction_currency;
	private String	transaction_type;
	private String	cbs_acc_no;
	private String	card_number;
	private String	card_type;
	private String	merchant_desc;
	private String	trn_country;
	private String	approval_code;
	private BigDecimal	interchange_fees;
	private BigDecimal	service_fee;
	private String	service_ind;
	private String	i_fee_ind;
	private String	dr_cr;
	private BigDecimal	markup_value;
	private String	markup_currency;
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
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	recon_process_date;
	public String getSttl_date() {
		return sttl_date;
	}
	public void setSttl_date(String sttl_date) {
		this.sttl_date = sttl_date;
	}
	public String getArn() {
		return arn;
	}
	public void setArn(String arn) {
		this.arn = arn;
	}
	public String getMcc() {
		return mcc;
	}
	public void setMcc(String mcc) {
		this.mcc = mcc;
	}
	public BigDecimal getAuth_amnt() {
		return auth_amnt;
	}
	public void setAuth_amnt(BigDecimal auth_amnt) {
		this.auth_amnt = auth_amnt;
	}
	public BigDecimal getAcct_amnt() {
		return acct_amnt;
	}
	public void setAcct_amnt(BigDecimal acct_amnt) {
		this.acct_amnt = acct_amnt;
	}
	public String getAuth_currency() {
		return auth_currency;
	}
	public void setAuth_currency(String auth_currency) {
		this.auth_currency = auth_currency;
	}
	public String getTransaction_date() {
		return transaction_date;
	}
	public void setTransaction_date(String transaction_date) {
		this.transaction_date = transaction_date;
	}
	public String getRefnum() {
		return refnum;
	}
	public void setRefnum(String refnum) {
		this.refnum = refnum;
	}
	public BigDecimal getReconcile_amount() {
		return reconcile_amount;
	}
	public void setReconcile_amount(BigDecimal reconcile_amount) {
		this.reconcile_amount = reconcile_amount;
	}
	public String getReconcile_cur() {
		return reconcile_cur;
	}
	public void setReconcile_cur(String reconcile_cur) {
		this.reconcile_cur = reconcile_cur;
	}
	public BigDecimal getTransaction_amount() {
		return transaction_amount;
	}
	public void setTransaction_amount(BigDecimal transaction_amount) {
		this.transaction_amount = transaction_amount;
	}
	public String getTransaction_currency() {
		return transaction_currency;
	}
	public void setTransaction_currency(String transaction_currency) {
		this.transaction_currency = transaction_currency;
	}
	public String getTransaction_type() {
		return transaction_type;
	}
	public void setTransaction_type(String transaction_type) {
		this.transaction_type = transaction_type;
	}
	public String getCbs_acc_no() {
		return cbs_acc_no;
	}
	public void setCbs_acc_no(String cbs_acc_no) {
		this.cbs_acc_no = cbs_acc_no;
	}
	public String getCard_number() {
		return card_number;
	}
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}
	public String getCard_type() {
		return card_type;
	}
	public void setCard_type(String card_type) {
		this.card_type = card_type;
	}
	public String getMerchant_desc() {
		return merchant_desc;
	}
	public void setMerchant_desc(String merchant_desc) {
		this.merchant_desc = merchant_desc;
	}
	public String getTrn_country() {
		return trn_country;
	}
	public void setTrn_country(String trn_country) {
		this.trn_country = trn_country;
	}
	public String getApproval_code() {
		return approval_code;
	}
	public void setApproval_code(String approval_code) {
		this.approval_code = approval_code;
	}
	public BigDecimal getInterchange_fees() {
		return interchange_fees;
	}
	public void setInterchange_fees(BigDecimal interchange_fees) {
		this.interchange_fees = interchange_fees;
	}
	public BigDecimal getService_fee() {
		return service_fee;
	}
	public void setService_fee(BigDecimal service_fee) {
		this.service_fee = service_fee;
	}
	public String getService_ind() {
		return service_ind;
	}
	public void setService_ind(String service_ind) {
		this.service_ind = service_ind;
	}
	public String getI_fee_ind() {
		return i_fee_ind;
	}
	public void setI_fee_ind(String i_fee_ind) {
		this.i_fee_ind = i_fee_ind;
	}
	public String getDr_cr() {
		return dr_cr;
	}
	public void setDr_cr(String dr_cr) {
		this.dr_cr = dr_cr;
	}
	public BigDecimal getMarkup_value() {
		return markup_value;
	}
	public void setMarkup_value(BigDecimal markup_value) {
		this.markup_value = markup_value;
	}
	public String getMarkup_currency() {
		return markup_currency;
	}
	public void setMarkup_currency(String markup_currency) {
		this.markup_currency = markup_currency;
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
	public Date getRecon_tran_date() {
		return recon_tran_date;
	}
	public void setRecon_tran_date(Date recon_tran_date) {
		this.recon_tran_date = recon_tran_date;
	}
	public String getRecon_type() {
		return recon_type;
	}
	public void setRecon_type(String recon_type) {
		this.recon_type = recon_type;
	}
	public Date getRecon_process_date() {
		return recon_process_date;
	}
	public void setRecon_process_date(Date recon_process_date) {
		this.recon_process_date = recon_process_date;
	}
	public RECON_UPI_DESTINATION_ENTITY(String sttl_date, String arn, String mcc, BigDecimal auth_amnt,
			BigDecimal acct_amnt, String auth_currency, String transaction_date, String refnum,
			BigDecimal reconcile_amount, String reconcile_cur, BigDecimal transaction_amount,
			String transaction_currency, String transaction_type, String cbs_acc_no, String card_number,
			String card_type, String merchant_desc, String trn_country, String approval_code,
			BigDecimal interchange_fees, BigDecimal service_fee, String service_ind, String i_fee_ind, String dr_cr,
			BigDecimal markup_value, String markup_currency, String entity_flg,
			String auth_flg, String modify_flg, String del_flg, String entry_user, String modify_user, String auth_user,
			Date entry_time, Date modify_time, Date auth_time, BigDecimal srl_no, String recon_flg,
			Date recon_tran_date, String recon_type, Date recon_process_date) {
		super();
		this.sttl_date = sttl_date;
		this.arn = arn;
		this.mcc = mcc;
		this.auth_amnt = auth_amnt;
		this.acct_amnt = acct_amnt;
		this.auth_currency = auth_currency;
		this.transaction_date = transaction_date;
		this.refnum = refnum;
		this.reconcile_amount = reconcile_amount;
		this.reconcile_cur = reconcile_cur;
		this.transaction_amount = transaction_amount;
		this.transaction_currency = transaction_currency;
		this.transaction_type = transaction_type;
		this.cbs_acc_no = cbs_acc_no;
		this.card_number = card_number;
		this.card_type = card_type;
		this.merchant_desc = merchant_desc;
		this.trn_country = trn_country;
		this.approval_code = approval_code;
		this.interchange_fees = interchange_fees;
		this.service_fee = service_fee;
		this.service_ind = service_ind;
		this.i_fee_ind = i_fee_ind;
		this.dr_cr = dr_cr;
		this.markup_value = markup_value;
		this.markup_currency = markup_currency;
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
	}
	public RECON_UPI_DESTINATION_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}