package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name = "BRECON_TEMP_DRREPORT_USD")
public class RECON_TEMP_DREPORT_USD_ENTITY {
	
	private BigDecimal	atm1;
	private BigDecimal	atm2;
	private BigDecimal	atm_payable1;
	private BigDecimal	pos1;
	private BigDecimal	pos2;
	private BigDecimal	pos3;
	private BigDecimal	pos4;
	private BigDecimal	pos_payable1;
	private BigDecimal	chg1;
	private BigDecimal	chg2;
	private BigDecimal	nostro_scb1;
	private BigDecimal	nostro_scb2;
	private BigDecimal	nostro_ent1;
	private BigDecimal	nostro_ent2;
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
	private BigDecimal	atm_payable2;
	private BigDecimal	pos_payable2;
	public BigDecimal getAtm1() {
		return atm1;
	}
	public void setAtm1(BigDecimal atm1) {
		this.atm1 = atm1;
	}
	public BigDecimal getAtm2() {
		return atm2;
	}
	public void setAtm2(BigDecimal atm2) {
		this.atm2 = atm2;
	}
	public BigDecimal getAtm_payable1() {
		return atm_payable1;
	}
	public void setAtm_payable1(BigDecimal atm_payable1) {
		this.atm_payable1 = atm_payable1;
	}
	public BigDecimal getPos1() {
		return pos1;
	}
	public void setPos1(BigDecimal pos1) {
		this.pos1 = pos1;
	}
	public BigDecimal getPos2() {
		return pos2;
	}
	public void setPos2(BigDecimal pos2) {
		this.pos2 = pos2;
	}
	public BigDecimal getPos3() {
		return pos3;
	}
	public void setPos3(BigDecimal pos3) {
		this.pos3 = pos3;
	}
	public BigDecimal getPos4() {
		return pos4;
	}
	public void setPos4(BigDecimal pos4) {
		this.pos4 = pos4;
	}
	public BigDecimal getPos_payable1() {
		return pos_payable1;
	}
	public void setPos_payable1(BigDecimal pos_payable1) {
		this.pos_payable1 = pos_payable1;
	}
	public BigDecimal getChg1() {
		return chg1;
	}
	public void setChg1(BigDecimal chg1) {
		this.chg1 = chg1;
	}
	public BigDecimal getChg2() {
		return chg2;
	}
	public void setChg2(BigDecimal chg2) {
		this.chg2 = chg2;
	}
	public BigDecimal getNostro_scb1() {
		return nostro_scb1;
	}
	public void setNostro_scb1(BigDecimal nostro_scb1) {
		this.nostro_scb1 = nostro_scb1;
	}
	public BigDecimal getNostro_scb2() {
		return nostro_scb2;
	}
	public void setNostro_scb2(BigDecimal nostro_scb2) {
		this.nostro_scb2 = nostro_scb2;
	}
	public BigDecimal getNostro_ent1() {
		return nostro_ent1;
	}
	public void setNostro_ent1(BigDecimal nostro_ent1) {
		this.nostro_ent1 = nostro_ent1;
	}
	public BigDecimal getNostro_ent2() {
		return nostro_ent2;
	}
	public void setNostro_ent2(BigDecimal nostro_ent2) {
		this.nostro_ent2 = nostro_ent2;
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
	public BigDecimal getAtm_payable2() {
		return atm_payable2;
	}
	public void setAtm_payable2(BigDecimal atm_payable2) {
		this.atm_payable2 = atm_payable2;
	}
	public BigDecimal getPos_payable2() {
		return pos_payable2;
	}
	public void setPos_payable2(BigDecimal pos_payable2) {
		this.pos_payable2 = pos_payable2;
	}
	public RECON_TEMP_DREPORT_USD_ENTITY(BigDecimal atm1, BigDecimal atm2, BigDecimal atm_payable1, BigDecimal pos1,
			BigDecimal pos2, BigDecimal pos3, BigDecimal pos4, BigDecimal pos_payable1, BigDecimal chg1,
			BigDecimal chg2, BigDecimal nostro_scb1, BigDecimal nostro_scb2, BigDecimal nostro_ent1,
			BigDecimal nostro_ent2, String currency, Date recon_date, String del_flg, String entity_flg,
			String entry_user, Date entry_time, String modify_user, Date modify_time, String verify_user,
			Date verify_time, BigDecimal amount, BigDecimal atm_payable2, BigDecimal pos_payable2) {
		super();
		this.atm1 = atm1;
		this.atm2 = atm2;
		this.atm_payable1 = atm_payable1;
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.pos3 = pos3;
		this.pos4 = pos4;
		this.pos_payable1 = pos_payable1;
		this.chg1 = chg1;
		this.chg2 = chg2;
		this.nostro_scb1 = nostro_scb1;
		this.nostro_scb2 = nostro_scb2;
		this.nostro_ent1 = nostro_ent1;
		this.nostro_ent2 = nostro_ent2;
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
		this.atm_payable2 = atm_payable2;
		this.pos_payable2 = pos_payable2;
	}
	public RECON_TEMP_DREPORT_USD_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
}
