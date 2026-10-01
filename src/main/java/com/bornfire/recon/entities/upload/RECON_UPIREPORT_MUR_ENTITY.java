package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name = "BRECON_UPIREPORT_MUR")
public class RECON_UPIREPORT_MUR_ENTITY {
	private BigDecimal	atm_intermdiary;
	private BigDecimal	atm_ifee;
	private BigDecimal	atm_sfee;
	private BigDecimal	atm_payable;
	private BigDecimal	atm_inquiry_ifee;
	private BigDecimal	atm_inquiry_sfee;
	private BigDecimal	atm_inquiry_payable;
	private BigDecimal	pos_intermdiary;
	private BigDecimal	pos_ifee;
	private BigDecimal	pos_sfee;
	private BigDecimal	pos_payable;
	private BigDecimal	nostro_mastercard;
	private BigDecimal	nostro_mcb;
	private BigDecimal	nostro_upi;
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

	public BigDecimal getNostro_mastercard() {
		return nostro_mastercard;
	}

	public void setNostro_mastercard(BigDecimal nostro_mastercard) {
		this.nostro_mastercard = nostro_mastercard;
	}

	public BigDecimal getNostro_mcb() {
		return nostro_mcb;
	}

	public void setNostro_mcb(BigDecimal nostro_mcb) {
		this.nostro_mcb = nostro_mcb;
	}

	public BigDecimal getNostro_upi() {
		return nostro_upi;
	}

	public void setNostro_upi(BigDecimal nostro_upi) {
		this.nostro_upi = nostro_upi;
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

	public RECON_UPIREPORT_MUR_ENTITY(BigDecimal atm_intermdiary, BigDecimal atm_ifee, BigDecimal atm_sfee,
			BigDecimal atm_payable, BigDecimal atm_inquiry_ifee, BigDecimal atm_inquiry_sfee,
			BigDecimal atm_inquiry_payable, BigDecimal pos_intermdiary, BigDecimal pos_ifee, BigDecimal pos_sfee,
			BigDecimal pos_payable, BigDecimal nostro_mastercard, BigDecimal nostro_mcb, BigDecimal nostro_upi,
			BigDecimal epos_intermdiary, BigDecimal epos_ifee, BigDecimal epos_sfee, BigDecimal epos_payable,
			String currency, Date recon_date, String del_flg, String entity_flg, String entry_user, Date entry_time,
			String modify_user, Date modify_time, String verify_user, Date verify_time, BigDecimal amount) {
		super();
		this.atm_intermdiary = atm_intermdiary;
		this.atm_ifee = atm_ifee;
		this.atm_sfee = atm_sfee;
		this.atm_payable = atm_payable;
		this.atm_inquiry_ifee = atm_inquiry_ifee;
		this.atm_inquiry_sfee = atm_inquiry_sfee;
		this.atm_inquiry_payable = atm_inquiry_payable;
		this.pos_intermdiary = pos_intermdiary;
		this.pos_ifee = pos_ifee;
		this.pos_sfee = pos_sfee;
		this.pos_payable = pos_payable;
		this.nostro_mastercard = nostro_mastercard;
		this.nostro_mcb = nostro_mcb;
		this.nostro_upi = nostro_upi;
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
	}

	public RECON_UPIREPORT_MUR_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
