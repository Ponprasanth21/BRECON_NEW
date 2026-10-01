package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;


@Entity
@Table(name = "BRECON_CRFILE_DESTINATION_TABLE")
public class RECON_CRFILE_DESTINATION_ENTITY {
	private String	bo_utrnno;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private String	taa_date;
	private String	trxn_date;
	private String	trxn_time;
	private String	post_date;
	private String	card_number;
	private String	trxn_type;
	private String	acct_currency;
	private String	source_curr;
	private BigDecimal	source_amt;
	private String	sett_curr;
	private BigDecimal	sett_amt;
	private String	bill_curr;
	private BigDecimal	mc_bill_amt;
	private BigDecimal	sv_bill_amt;
	private BigDecimal	trxn_fee;
	private String	transaction_ind;
	private BigDecimal	i_fee;
	private String	i_fee_ind;
	private String	auth_code;
	private String	merchant_name;
	private String	exception;
	private String	arn;
	private String	cycle_number;
	private String	mcc;
	private String	country;
	private String	refnum;
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
	private String  ccard_num;
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
	public String getCcard_num() {
		return ccard_num;
	}
	public void setCcard_num(String ccard_num) {
		this.ccard_num = ccard_num;
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
	public String getBo_utrnno() {
		return bo_utrnno;
	}
	public void setBo_utrnno(String bo_utrnno) {
		this.bo_utrnno = bo_utrnno;
	}
	public String getTaa_date() {
		return taa_date;
	}
	public void setTaa_date(String taa_date) {
		this.taa_date = taa_date;
	}
	public String getTrxn_date() {
		return trxn_date;
	}
	public void setTrxn_date(	String trxn_date) {
		this.trxn_date = trxn_date;
	}
	public String getTrxn_time() {
		return trxn_time;
	}
	public void setTrxn_time(String trxn_time) {
		this.trxn_time = trxn_time;
	}
	public String getPost_date() {
		return post_date;
	}
	public void setPost_date(String post_date) {
		this.post_date = post_date;
	}
	public String getCard_number() {
		return card_number;
	}
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}
	public String getTrxn_type() {
		return trxn_type;
	}
	public void setTrxn_type(String trxn_type) {
		this.trxn_type = trxn_type;
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
	public BigDecimal getTrxn_fee() {
		return trxn_fee;
	}
	public void setTrxn_fee(BigDecimal trxn_fee) {
		this.trxn_fee = trxn_fee;
	}
	public String getTransaction_ind() {
		return transaction_ind;
	}
	public void setTransaction_ind(String transaction_ind) {
		this.transaction_ind = transaction_ind;
	}
	public BigDecimal getI_fee() {
		return i_fee;
	}
	public void setI_fee(BigDecimal i_fee) {
		this.i_fee = i_fee;
	}
	public String getI_fee_ind() {
		return i_fee_ind;
	}
	public void setI_fee_ind(String i_fee_ind) {
		this.i_fee_ind = i_fee_ind;
	}
	public String getAuth_code() {
		return auth_code;
	}
	public void setAuth_code(String auth_code) {
		this.auth_code = auth_code;
	}

	public String getMerchant_name() {
		return merchant_name;
	}

	public void setMerchant_name(String merchant_name) {
		this.merchant_name = merchant_name;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public String getCycle_number() {
		return cycle_number;
	}

	public void setCycle_number(String cycle_number) {
		this.cycle_number = cycle_number;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getRefnum() {
		return refnum;
	}

	public void setRefnum(String refnum) {
		this.refnum = refnum;
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
 
	
	public RECON_CRFILE_DESTINATION_ENTITY(String bo_utrnno, String taa_date, String trxn_date, String trxn_time,
			String post_date, String card_number, String trxn_type, String acct_currency, String source_curr,
			BigDecimal source_amt, String sett_curr, BigDecimal sett_amt, String bill_curr, BigDecimal mc_bill_amt,
			BigDecimal sv_bill_amt, BigDecimal trxn_fee, String transaction_ind, BigDecimal i_fee, String i_fee_ind,
			String auth_code, String merchant_name, String exception, String arn, String cycle_number, String mcc,
			String country, String refnum, String entity_flg, String auth_flg, String modify_flg, String del_flg,
			String entry_user, String modify_user, String auth_user, Date entry_time, Date modify_time, Date auth_time,
			BigDecimal srl_no, String recon_flg, String recon_type, String ccard_num, Date recon_tran_date,
			Date recon_process_date) {
		super();
		this.bo_utrnno = bo_utrnno;
		this.taa_date = taa_date;
		this.trxn_date = trxn_date;
		this.trxn_time = trxn_time;
		this.post_date = post_date;
		this.card_number = card_number;
		this.trxn_type = trxn_type;
		this.acct_currency = acct_currency;
		this.source_curr = source_curr;
		this.source_amt = source_amt;
		this.sett_curr = sett_curr;
		this.sett_amt = sett_amt;
		this.bill_curr = bill_curr;
		this.mc_bill_amt = mc_bill_amt;
		this.sv_bill_amt = sv_bill_amt;
		this.trxn_fee = trxn_fee;
		this.transaction_ind = transaction_ind;
		this.i_fee = i_fee;
		this.i_fee_ind = i_fee_ind;
		this.auth_code = auth_code;
		this.merchant_name = merchant_name;
		this.exception = exception;
		this.arn = arn;
		this.cycle_number = cycle_number;
		this.mcc = mcc;
		this.country = country;
		this.refnum = refnum;
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
		this.ccard_num = ccard_num;
		this.recon_tran_date = recon_tran_date;
		this.recon_process_date = recon_process_date;
	}
	public RECON_CRFILE_DESTINATION_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
