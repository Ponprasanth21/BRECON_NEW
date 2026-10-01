package com.bornfire.recon.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "FOLLOW_UP_TABLE")
public class FOLLOW_UP_ENTITY {
	
	private String	place_holder;
	private String	mirror_account_no;
	private String	account_no;
	private String	account_name;
	private String	mirror_account_name;
	private String	currency;
	private String	remark;
	@Id
	private String	srl;
	private BigDecimal	age;
	private BigDecimal	no_of_trans;
	private BigDecimal	value;
	private BigDecimal	min_amount;
	private BigDecimal	max_amount;
	private BigDecimal	percentage;
	private String	details;
	private String	del_flg;
	private String	entity_flg;
	private String	entry_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	entry_time;
	private String	modify_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	modify_time;
	private String	verify_user;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	verify_time;
	private String	modify_flg;
	private String	verify_flg;
	public String getPlace_holder() {
		return place_holder;
	}
	public void setPlace_holder(String place_holder) {
		this.place_holder = place_holder;
	}
	public String getMirror_account_no() {
		return mirror_account_no;
	}
	public void setMirror_account_no(String mirror_account_no) {
		this.mirror_account_no = mirror_account_no;
	}
	public String getAccount_no() {
		return account_no;
	}
	public void setAccount_no(String account_no) {
		this.account_no = account_no;
	}
	
	public String getAccount_name() {
		return account_name;
	}
	public void setAccount_name(String account_name) {
		this.account_name = account_name;
	}
	public String getMirror_account_name() {
		return mirror_account_name;
	}
	public void setMirror_account_name(String mirror_account_name) {
		this.mirror_account_name = mirror_account_name;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSrl() {
		return srl;
	}
	public void setSrl(String srl) {
		this.srl = srl;
	}
	public BigDecimal getAge() {
		return age;
	}
	public void setAge(BigDecimal age) {
		this.age = age;
	}
	public BigDecimal getNo_of_trans() {
		return no_of_trans;
	}
	public void setNo_of_trans(BigDecimal no_of_trans) {
		this.no_of_trans = no_of_trans;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public BigDecimal getMin_amount() {
		return min_amount;
	}
	public void setMin_amount(BigDecimal min_amount) {
		this.min_amount = min_amount;
	}
	public BigDecimal getMax_amount() {
		return max_amount;
	}
	public void setMax_amount(BigDecimal max_amount) {
		this.max_amount = max_amount;
	}
	public BigDecimal getPercentage() {
		return percentage;
	}
	public void setPercentage(BigDecimal percentage) {
		this.percentage = percentage;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
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
	public FOLLOW_UP_ENTITY(String place_holder, String mirror_account_no, String account_no, String account_name,
			String mirror_account_name, String currency, String remark, String srl, BigDecimal age,
			BigDecimal no_of_trans, BigDecimal value, BigDecimal min_amount, BigDecimal max_amount,
			BigDecimal percentage, String details, String del_flg, String entity_flg, String entry_user,
			Date entry_time, String modify_user, Date modify_time, String verify_user, Date verify_time,
			String modify_flg, String verify_flg) {
		super();
		this.place_holder = place_holder;
		this.mirror_account_no = mirror_account_no;
		this.account_no = account_no;
		this.account_name = account_name;
		this.mirror_account_name = mirror_account_name;
		this.currency = currency;
		this.remark = remark;
		this.srl = srl;
		this.age = age;
		this.no_of_trans = no_of_trans;
		this.value = value;
		this.min_amount = min_amount;
		this.max_amount = max_amount;
		this.percentage = percentage;
		this.details = details;
		this.del_flg = del_flg;
		this.entity_flg = entity_flg;
		this.entry_user = entry_user;
		this.entry_time = entry_time;
		this.modify_user = modify_user;
		this.modify_time = modify_time;
		this.verify_user = verify_user;
		this.verify_time = verify_time;
		this.modify_flg = modify_flg;
		this.verify_flg = verify_flg;
	}
	public FOLLOW_UP_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
