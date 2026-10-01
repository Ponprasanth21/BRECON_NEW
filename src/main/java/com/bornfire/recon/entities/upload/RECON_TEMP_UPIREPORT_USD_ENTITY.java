package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name = "BRECON_TEMP_UPIREPORT_USD")
public class RECON_TEMP_UPIREPORT_USD_ENTITY {
	private BigDecimal	atm_intermdiary;
	private BigDecimal	atm_ifee;
	private BigDecimal	atm_sfee;
	private BigDecimal	atm_payable;
	private BigDecimal	pos_intermdiary;
	private BigDecimal	pos_ifee;
	private BigDecimal	pos_sfee;
	private BigDecimal	pos_payable;
	private BigDecimal	nostro_upi_cardpay;
	private BigDecimal	nostro_scb;
	private BigDecimal	nostro_upi_cardrev;
	private BigDecimal	epos_intermdiary;
	private BigDecimal	epos_ifee;
	private BigDecimal	epos_sfee;
	private BigDecimal	epos_payable;
	private String	currency;
	@Id
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	recon_date;
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
	private BigDecimal amount;
	private BigDecimal	atm_inquiry_ifee;
	private BigDecimal	atm_inquiry_sfee;
	private BigDecimal	atm_inquiry_payable;
	public BigDecimal getAtm_intermdiary() {
		return atm_intermdiary;
	}
	public void setAtm_intermdiary(BigDecimal atm_intermdiary) {
		this.atm_intermdiary = atm_intermdiary;
	}
	public BigDecimal getAtm_ifee() {
		return atm_ifee;
	}
	public void setAtm_ifee(BigDecimal atm_ifee) {
		this.atm_ifee = atm_ifee;
	}
	public BigDecimal getAtm_sfee() {
		return atm_sfee;
	}
	public void setAtm_sfee(BigDecimal atm_sfee) {
		this.atm_sfee = atm_sfee;
	}
	public BigDecimal getAtm_payable() {
		return atm_payable;
	}
	public void setAtm_payable(BigDecimal atm_payable) {
		this.atm_payable = atm_payable;
	}
	public BigDecimal getPos_intermdiary() {
		return pos_intermdiary;
	}
	public void setPos_intermdiary(BigDecimal pos_intermdiary) {
		this.pos_intermdiary = pos_intermdiary;
	}
	public BigDecimal getPos_ifee() {
		return pos_ifee;
	}
	public void setPos_ifee(BigDecimal pos_ifee) {
		this.pos_ifee = pos_ifee;
	}
	public BigDecimal getPos_sfee() {
		return pos_sfee;
	}
	public void setPos_sfee(BigDecimal pos_sfee) {
		this.pos_sfee = pos_sfee;
	}
	public BigDecimal getPos_payable() {
		return pos_payable;
	}
	public void setPos_payable(BigDecimal pos_payable) {
		this.pos_payable = pos_payable;
	}
	public BigDecimal getNostro_upi_cardpay() {
		return nostro_upi_cardpay;
	}
	public void setNostro_upi_cardpay(BigDecimal nostro_upi_cardpay) {
		this.nostro_upi_cardpay = nostro_upi_cardpay;
	}
	public BigDecimal getNostro_scb() {
		return nostro_scb;
	}
	public void setNostro_scb(BigDecimal nostro_scb) {
		this.nostro_scb = nostro_scb;
	}
	public BigDecimal getNostro_upi_cardrev() {
		return nostro_upi_cardrev;
	}
	public void setNostro_upi_cardrev(BigDecimal nostro_upi_cardrev) {
		this.nostro_upi_cardrev = nostro_upi_cardrev;
	}
	public BigDecimal getEpos_intermdiary() {
		return epos_intermdiary;
	}
	public void setEpos_intermdiary(BigDecimal epos_intermdiary) {
		this.epos_intermdiary = epos_intermdiary;
	}
	public BigDecimal getEpos_ifee() {
		return epos_ifee;
	}
	public void setEpos_ifee(BigDecimal epos_ifee) {
		this.epos_ifee = epos_ifee;
	}
	public BigDecimal getEpos_sfee() {
		return epos_sfee;
	}
	public void setEpos_sfee(BigDecimal epos_sfee) {
		this.epos_sfee = epos_sfee;
	}
	public BigDecimal getEpos_payable() {
		return epos_payable;
	}
	public void setEpos_payable(BigDecimal epos_payable) {
		this.epos_payable = epos_payable;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Date getRecon_date() {
		return recon_date;
	}
	public void setRecon_date(Date recon_date) {
		this.recon_date = recon_date;
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
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getAtm_inquiry_ifee() {
		return atm_inquiry_ifee;
	}
	public void setAtm_inquiry_ifee(BigDecimal atm_inquiry_ifee) {
		this.atm_inquiry_ifee = atm_inquiry_ifee;
	}
	public BigDecimal getAtm_inquiry_sfee() {
		return atm_inquiry_sfee;
	}
	public void setAtm_inquiry_sfee(BigDecimal atm_inquiry_sfee) {
		this.atm_inquiry_sfee = atm_inquiry_sfee;
	}
	public BigDecimal getAtm_inquiry_payable() {
		return atm_inquiry_payable;
	}
	public void setAtm_inquiry_payable(BigDecimal atm_inquiry_payable) {
		this.atm_inquiry_payable = atm_inquiry_payable;
	}
	public RECON_TEMP_UPIREPORT_USD_ENTITY(BigDecimal atm_intermdiary, BigDecimal atm_ifee, BigDecimal atm_sfee,
			BigDecimal atm_payable, BigDecimal pos_intermdiary, BigDecimal pos_ifee, BigDecimal pos_sfee,
			BigDecimal pos_payable, BigDecimal nostro_upi_cardpay, BigDecimal nostro_scb, BigDecimal nostro_upi_cardrev,
			BigDecimal epos_intermdiary, BigDecimal epos_ifee, BigDecimal epos_sfee, BigDecimal epos_payable,
			String currency, Date recon_date, String del_flg, String entity_flg, String entry_user, Date entry_time,
			String modify_user, Date modify_time, String verify_user, Date verify_time, BigDecimal amount,
			BigDecimal atm_inquiry_ifee, BigDecimal atm_inquiry_sfee, BigDecimal atm_inquiry_payable) {
		super();
		this.atm_intermdiary = atm_intermdiary;
		this.atm_ifee = atm_ifee;
		this.atm_sfee = atm_sfee;
		this.atm_payable = atm_payable;
		this.pos_intermdiary = pos_intermdiary;
		this.pos_ifee = pos_ifee;
		this.pos_sfee = pos_sfee;
		this.pos_payable = pos_payable;
		this.nostro_upi_cardpay = nostro_upi_cardpay;
		this.nostro_scb = nostro_scb;
		this.nostro_upi_cardrev = nostro_upi_cardrev;
		this.epos_intermdiary = epos_intermdiary;
		this.epos_ifee = epos_ifee;
		this.epos_sfee = epos_sfee;
		this.epos_payable = epos_payable;
		this.currency = currency;
		this.recon_date = recon_date;
		this.del_flg = del_flg;
		this.entity_flg = entity_flg;
		this.entry_user = entry_user;
		this.entry_time = entry_time;
		this.modify_user = modify_user;
		this.modify_time = modify_time;
		this.verify_user = verify_user;
		this.verify_time = verify_time;
		this.amount = amount;
		this.atm_inquiry_ifee = atm_inquiry_ifee;
		this.atm_inquiry_sfee = atm_inquiry_sfee;
		this.atm_inquiry_payable = atm_inquiry_payable;
	}
	public RECON_TEMP_UPIREPORT_USD_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
