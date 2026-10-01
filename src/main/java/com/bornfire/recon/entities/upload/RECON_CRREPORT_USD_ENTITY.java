package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "BRECON_CRREPORT_USD")
public class RECON_CRREPORT_USD_ENTITY {
	private BigDecimal	r3_usd;
	private BigDecimal	r3_mur1;
	private BigDecimal	r3_i;
	private BigDecimal	r3_mur2;
	private BigDecimal	r3_sett;
	private BigDecimal	r3_pex;
	private BigDecimal	r3_diff;
	private BigDecimal	r4_usd;
	private BigDecimal	r4_mur1;
	private BigDecimal	r4_i;
	private BigDecimal	r4_mur2;
	private BigDecimal	r4_sett;
	private BigDecimal	r4_pex;
	private BigDecimal	r4_diff;
	private BigDecimal	r5_usd;
	private BigDecimal	r5_mur1;
	private BigDecimal	r5_i;
	private BigDecimal	r5_mur2;
	private BigDecimal	r5_sett;
	private BigDecimal	r5_pex;
	private BigDecimal	r5_diff;
	private BigDecimal	r6_usd;
	private BigDecimal	r6_mur1;
	private BigDecimal	r6_i;
	private BigDecimal	r6_mur2;
	private BigDecimal	r6_sett;
	private BigDecimal	r6_pex;
	private BigDecimal	r6_diff;
	private BigDecimal	r7_mur2;
	private BigDecimal	r8_usd;
	private BigDecimal	r8_mur1;
	private BigDecimal	r8_i;
	private BigDecimal	r8_mur2;
	private BigDecimal	r9_usd;
	private BigDecimal	r9_mur1;
	private BigDecimal	r9_i;
	private BigDecimal	r9_mur2;
	private BigDecimal	r10_mur2;
	private BigDecimal	r11_usd;
	private BigDecimal	r11_mur1;
	private BigDecimal	r11_i;
	private BigDecimal	r11_mur2;
	private BigDecimal	r12_usd;
	private BigDecimal	r12_mur1;
	private BigDecimal	r12_i;
	private BigDecimal	r12_mur2;
	private BigDecimal	r13_mur2;
	private BigDecimal	r14_usd;
	private BigDecimal	r14_mur1;
	private BigDecimal	r14_i;
	private BigDecimal	r14_mur2;
	private BigDecimal	r15_usd;
	private BigDecimal	r15_mur1;
	private BigDecimal	r15_i;
	private BigDecimal	r15_mur2;
	private BigDecimal	r16_usd;
	private BigDecimal	r16_mur1;
	private BigDecimal	r16_i;
	private BigDecimal	r16_mur2;
	private BigDecimal	r17_usd;
	private BigDecimal	r17_mur1;
	private BigDecimal	r17_i;
	private BigDecimal	r17_mur2;
	private BigDecimal	r19_usd;
	private BigDecimal	r19_mur1;
	private BigDecimal	r19_i;
	private BigDecimal	r19_mur2;
	@Id
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	recon_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	posting_date;
	private String	trnx_id;
	private String	currency;
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
	public BigDecimal getR3_usd() {
		return r3_usd;
	}
	public void setR3_usd(BigDecimal r3_usd) {
		this.r3_usd = r3_usd;
	}
	public BigDecimal getR3_mur1() {
		return r3_mur1;
	}
	public void setR3_mur1(BigDecimal r3_mur1) {
		this.r3_mur1 = r3_mur1;
	}
	public BigDecimal getR3_i() {
		return r3_i;
	}
	public void setR3_i(BigDecimal r3_i) {
		this.r3_i = r3_i;
	}
	public BigDecimal getR3_mur2() {
		return r3_mur2;
	}
	public void setR3_mur2(BigDecimal r3_mur2) {
		this.r3_mur2 = r3_mur2;
	}
	public BigDecimal getR3_sett() {
		return r3_sett;
	}
	public void setR3_sett(BigDecimal r3_sett) {
		this.r3_sett = r3_sett;
	}
	public BigDecimal getR3_pex() {
		return r3_pex;
	}
	public void setR3_pex(BigDecimal r3_pex) {
		this.r3_pex = r3_pex;
	}
	public BigDecimal getR3_diff() {
		return r3_diff;
	}
	public void setR3_diff(BigDecimal r3_diff) {
		this.r3_diff = r3_diff;
	}
	public BigDecimal getR4_usd() {
		return r4_usd;
	}
	public void setR4_usd(BigDecimal r4_usd) {
		this.r4_usd = r4_usd;
	}
	public BigDecimal getR4_mur1() {
		return r4_mur1;
	}
	public void setR4_mur1(BigDecimal r4_mur1) {
		this.r4_mur1 = r4_mur1;
	}
	public BigDecimal getR4_i() {
		return r4_i;
	}
	public void setR4_i(BigDecimal r4_i) {
		this.r4_i = r4_i;
	}
	public BigDecimal getR4_mur2() {
		return r4_mur2;
	}
	public void setR4_mur2(BigDecimal r4_mur2) {
		this.r4_mur2 = r4_mur2;
	}
	public BigDecimal getR4_sett() {
		return r4_sett;
	}
	public void setR4_sett(BigDecimal r4_sett) {
		this.r4_sett = r4_sett;
	}
	public BigDecimal getR4_pex() {
		return r4_pex;
	}
	public void setR4_pex(BigDecimal r4_pex) {
		this.r4_pex = r4_pex;
	}
	public BigDecimal getR4_diff() {
		return r4_diff;
	}
	public void setR4_diff(BigDecimal r4_diff) {
		this.r4_diff = r4_diff;
	}
	public BigDecimal getR5_usd() {
		return r5_usd;
	}
	public void setR5_usd(BigDecimal r5_usd) {
		this.r5_usd = r5_usd;
	}
	public BigDecimal getR5_mur1() {
		return r5_mur1;
	}
	public void setR5_mur1(BigDecimal r5_mur1) {
		this.r5_mur1 = r5_mur1;
	}
	public BigDecimal getR5_i() {
		return r5_i;
	}
	public void setR5_i(BigDecimal r5_i) {
		this.r5_i = r5_i;
	}
	public BigDecimal getR5_mur2() {
		return r5_mur2;
	}
	public void setR5_mur2(BigDecimal r5_mur2) {
		this.r5_mur2 = r5_mur2;
	}
	public BigDecimal getR5_sett() {
		return r5_sett;
	}
	public void setR5_sett(BigDecimal r5_sett) {
		this.r5_sett = r5_sett;
	}
	public BigDecimal getR5_pex() {
		return r5_pex;
	}
	public void setR5_pex(BigDecimal r5_pex) {
		this.r5_pex = r5_pex;
	}
	public BigDecimal getR5_diff() {
		return r5_diff;
	}
	public void setR5_diff(BigDecimal r5_diff) {
		this.r5_diff = r5_diff;
	}
	public BigDecimal getR6_usd() {
		return r6_usd;
	}
	public void setR6_usd(BigDecimal r6_usd) {
		this.r6_usd = r6_usd;
	}
	public BigDecimal getR6_mur1() {
		return r6_mur1;
	}
	public void setR6_mur1(BigDecimal r6_mur1) {
		this.r6_mur1 = r6_mur1;
	}
	public BigDecimal getR6_i() {
		return r6_i;
	}
	public void setR6_i(BigDecimal r6_i) {
		this.r6_i = r6_i;
	}
	public BigDecimal getR6_mur2() {
		return r6_mur2;
	}
	public void setR6_mur2(BigDecimal r6_mur2) {
		this.r6_mur2 = r6_mur2;
	}
	public BigDecimal getR6_sett() {
		return r6_sett;
	}
	public void setR6_sett(BigDecimal r6_sett) {
		this.r6_sett = r6_sett;
	}
	public BigDecimal getR6_pex() {
		return r6_pex;
	}
	public void setR6_pex(BigDecimal r6_pex) {
		this.r6_pex = r6_pex;
	}
	public BigDecimal getR6_diff() {
		return r6_diff;
	}
	public void setR6_diff(BigDecimal r6_diff) {
		this.r6_diff = r6_diff;
	}
	public BigDecimal getR7_mur2() {
		return r7_mur2;
	}
	public void setR7_mur2(BigDecimal r7_mur2) {
		this.r7_mur2 = r7_mur2;
	}
	public BigDecimal getR8_usd() {
		return r8_usd;
	}
	public void setR8_usd(BigDecimal r8_usd) {
		this.r8_usd = r8_usd;
	}
	public BigDecimal getR8_mur1() {
		return r8_mur1;
	}
	public void setR8_mur1(BigDecimal r8_mur1) {
		this.r8_mur1 = r8_mur1;
	}
	public BigDecimal getR8_i() {
		return r8_i;
	}
	public void setR8_i(BigDecimal r8_i) {
		this.r8_i = r8_i;
	}
	public BigDecimal getR8_mur2() {
		return r8_mur2;
	}
	public void setR8_mur2(BigDecimal r8_mur2) {
		this.r8_mur2 = r8_mur2;
	}
	public BigDecimal getR9_usd() {
		return r9_usd;
	}
	public void setR9_usd(BigDecimal r9_usd) {
		this.r9_usd = r9_usd;
	}
	public BigDecimal getR9_mur1() {
		return r9_mur1;
	}
	public void setR9_mur1(BigDecimal r9_mur1) {
		this.r9_mur1 = r9_mur1;
	}
	public BigDecimal getR9_i() {
		return r9_i;
	}
	public void setR9_i(BigDecimal r9_i) {
		this.r9_i = r9_i;
	}
	public BigDecimal getR9_mur2() {
		return r9_mur2;
	}
	public void setR9_mur2(BigDecimal r9_mur2) {
		this.r9_mur2 = r9_mur2;
	}
	public BigDecimal getR10_mur2() {
		return r10_mur2;
	}
	public void setR10_mur2(BigDecimal r10_mur2) {
		this.r10_mur2 = r10_mur2;
	}
	public BigDecimal getR11_usd() {
		return r11_usd;
	}
	public void setR11_usd(BigDecimal r11_usd) {
		this.r11_usd = r11_usd;
	}
	public BigDecimal getR11_mur1() {
		return r11_mur1;
	}
	public void setR11_mur1(BigDecimal r11_mur1) {
		this.r11_mur1 = r11_mur1;
	}
	public BigDecimal getR11_i() {
		return r11_i;
	}
	public void setR11_i(BigDecimal r11_i) {
		this.r11_i = r11_i;
	}
	public BigDecimal getR11_mur2() {
		return r11_mur2;
	}
	public void setR11_mur2(BigDecimal r11_mur2) {
		this.r11_mur2 = r11_mur2;
	}
	public BigDecimal getR12_usd() {
		return r12_usd;
	}
	public void setR12_usd(BigDecimal r12_usd) {
		this.r12_usd = r12_usd;
	}
	public BigDecimal getR12_mur1() {
		return r12_mur1;
	}
	public void setR12_mur1(BigDecimal r12_mur1) {
		this.r12_mur1 = r12_mur1;
	}
	public BigDecimal getR12_i() {
		return r12_i;
	}
	public void setR12_i(BigDecimal r12_i) {
		this.r12_i = r12_i;
	}
	public BigDecimal getR12_mur2() {
		return r12_mur2;
	}
	public void setR12_mur2(BigDecimal r12_mur2) {
		this.r12_mur2 = r12_mur2;
	}
	public BigDecimal getR13_mur2() {
		return r13_mur2;
	}
	public void setR13_mur2(BigDecimal r13_mur2) {
		this.r13_mur2 = r13_mur2;
	}
	public BigDecimal getR14_usd() {
		return r14_usd;
	}
	public void setR14_usd(BigDecimal r14_usd) {
		this.r14_usd = r14_usd;
	}
	public BigDecimal getR14_mur1() {
		return r14_mur1;
	}
	public void setR14_mur1(BigDecimal r14_mur1) {
		this.r14_mur1 = r14_mur1;
	}
	public BigDecimal getR14_i() {
		return r14_i;
	}
	public void setR14_i(BigDecimal r14_i) {
		this.r14_i = r14_i;
	}
	public BigDecimal getR14_mur2() {
		return r14_mur2;
	}
	public void setR14_mur2(BigDecimal r14_mur2) {
		this.r14_mur2 = r14_mur2;
	}
	public BigDecimal getR15_usd() {
		return r15_usd;
	}
	public void setR15_usd(BigDecimal r15_usd) {
		this.r15_usd = r15_usd;
	}
	public BigDecimal getR15_mur1() {
		return r15_mur1;
	}
	public void setR15_mur1(BigDecimal r15_mur1) {
		this.r15_mur1 = r15_mur1;
	}
	public BigDecimal getR15_i() {
		return r15_i;
	}
	public void setR15_i(BigDecimal r15_i) {
		this.r15_i = r15_i;
	}
	public BigDecimal getR15_mur2() {
		return r15_mur2;
	}
	public void setR15_mur2(BigDecimal r15_mur2) {
		this.r15_mur2 = r15_mur2;
	}
	public BigDecimal getR16_usd() {
		return r16_usd;
	}
	public void setR16_usd(BigDecimal r16_usd) {
		this.r16_usd = r16_usd;
	}
	public BigDecimal getR16_mur1() {
		return r16_mur1;
	}
	public void setR16_mur1(BigDecimal r16_mur1) {
		this.r16_mur1 = r16_mur1;
	}
	public BigDecimal getR16_i() {
		return r16_i;
	}
	public void setR16_i(BigDecimal r16_i) {
		this.r16_i = r16_i;
	}
	public BigDecimal getR16_mur2() {
		return r16_mur2;
	}
	public void setR16_mur2(BigDecimal r16_mur2) {
		this.r16_mur2 = r16_mur2;
	}
	public BigDecimal getR17_usd() {
		return r17_usd;
	}
	public void setR17_usd(BigDecimal r17_usd) {
		this.r17_usd = r17_usd;
	}
	public BigDecimal getR17_mur1() {
		return r17_mur1;
	}
	public void setR17_mur1(BigDecimal r17_mur1) {
		this.r17_mur1 = r17_mur1;
	}
	public BigDecimal getR17_i() {
		return r17_i;
	}
	public void setR17_i(BigDecimal r17_i) {
		this.r17_i = r17_i;
	}
	public BigDecimal getR17_mur2() {
		return r17_mur2;
	}
	public void setR17_mur2(BigDecimal r17_mur2) {
		this.r17_mur2 = r17_mur2;
	}
	public BigDecimal getR19_usd() {
		return r19_usd;
	}
	public void setR19_usd(BigDecimal r19_usd) {
		this.r19_usd = r19_usd;
	}
	public BigDecimal getR19_mur1() {
		return r19_mur1;
	}
	public void setR19_mur1(BigDecimal r19_mur1) {
		this.r19_mur1 = r19_mur1;
	}
	public BigDecimal getR19_i() {
		return r19_i;
	}
	public void setR19_i(BigDecimal r19_i) {
		this.r19_i = r19_i;
	}
	public BigDecimal getR19_mur2() {
		return r19_mur2;
	}
	public void setR19_mur2(BigDecimal r19_mur2) {
		this.r19_mur2 = r19_mur2;
	}
	public Date getRecon_date() {
		return recon_date;
	}
	public void setRecon_date(Date recon_date) {
		this.recon_date = recon_date;
	}
	public Date getPosting_date() {
		return posting_date;
	}
	public void setPosting_date(Date posting_date) {
		this.posting_date = posting_date;
	}
	public String getTrnx_id() {
		return trnx_id;
	}
	public void setTrnx_id(String trnx_id) {
		this.trnx_id = trnx_id;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
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
	public RECON_CRREPORT_USD_ENTITY(BigDecimal r3_usd, BigDecimal r3_mur1, BigDecimal r3_i, BigDecimal r3_mur2,
			BigDecimal r3_sett, BigDecimal r3_pex, BigDecimal r3_diff, BigDecimal r4_usd, BigDecimal r4_mur1,
			BigDecimal r4_i, BigDecimal r4_mur2, BigDecimal r4_sett, BigDecimal r4_pex, BigDecimal r4_diff,
			BigDecimal r5_usd, BigDecimal r5_mur1, BigDecimal r5_i, BigDecimal r5_mur2, BigDecimal r5_sett,
			BigDecimal r5_pex, BigDecimal r5_diff, BigDecimal r6_usd, BigDecimal r6_mur1, BigDecimal r6_i,
			BigDecimal r6_mur2, BigDecimal r6_sett, BigDecimal r6_pex, BigDecimal r6_diff, BigDecimal r7_mur2,
			BigDecimal r8_usd, BigDecimal r8_mur1, BigDecimal r8_i, BigDecimal r8_mur2, BigDecimal r9_usd,
			BigDecimal r9_mur1, BigDecimal r9_i, BigDecimal r9_mur2, BigDecimal r10_mur2, BigDecimal r11_usd,
			BigDecimal r11_mur1, BigDecimal r11_i, BigDecimal r11_mur2, BigDecimal r12_usd, BigDecimal r12_mur1,
			BigDecimal r12_i, BigDecimal r12_mur2, BigDecimal r13_mur2, BigDecimal r14_usd, BigDecimal r14_mur1,
			BigDecimal r14_i, BigDecimal r14_mur2, BigDecimal r15_usd, BigDecimal r15_mur1, BigDecimal r15_i,
			BigDecimal r15_mur2, BigDecimal r16_usd, BigDecimal r16_mur1, BigDecimal r16_i, BigDecimal r16_mur2,
			BigDecimal r17_usd, BigDecimal r17_mur1, BigDecimal r17_i, BigDecimal r17_mur2, BigDecimal r19_usd,
			BigDecimal r19_mur1, BigDecimal r19_i, BigDecimal r19_mur2, Date recon_date, Date posting_date,
			String trnx_id, String currency, String del_flg, String entity_flg, String entry_user, Date entry_time,
			String modify_user, Date modify_time, String verify_user, Date verify_time) {
		super();
		this.r3_usd = r3_usd;
		this.r3_mur1 = r3_mur1;
		this.r3_i = r3_i;
		this.r3_mur2 = r3_mur2;
		this.r3_sett = r3_sett;
		this.r3_pex = r3_pex;
		this.r3_diff = r3_diff;
		this.r4_usd = r4_usd;
		this.r4_mur1 = r4_mur1;
		this.r4_i = r4_i;
		this.r4_mur2 = r4_mur2;
		this.r4_sett = r4_sett;
		this.r4_pex = r4_pex;
		this.r4_diff = r4_diff;
		this.r5_usd = r5_usd;
		this.r5_mur1 = r5_mur1;
		this.r5_i = r5_i;
		this.r5_mur2 = r5_mur2;
		this.r5_sett = r5_sett;
		this.r5_pex = r5_pex;
		this.r5_diff = r5_diff;
		this.r6_usd = r6_usd;
		this.r6_mur1 = r6_mur1;
		this.r6_i = r6_i;
		this.r6_mur2 = r6_mur2;
		this.r6_sett = r6_sett;
		this.r6_pex = r6_pex;
		this.r6_diff = r6_diff;
		this.r7_mur2 = r7_mur2;
		this.r8_usd = r8_usd;
		this.r8_mur1 = r8_mur1;
		this.r8_i = r8_i;
		this.r8_mur2 = r8_mur2;
		this.r9_usd = r9_usd;
		this.r9_mur1 = r9_mur1;
		this.r9_i = r9_i;
		this.r9_mur2 = r9_mur2;
		this.r10_mur2 = r10_mur2;
		this.r11_usd = r11_usd;
		this.r11_mur1 = r11_mur1;
		this.r11_i = r11_i;
		this.r11_mur2 = r11_mur2;
		this.r12_usd = r12_usd;
		this.r12_mur1 = r12_mur1;
		this.r12_i = r12_i;
		this.r12_mur2 = r12_mur2;
		this.r13_mur2 = r13_mur2;
		this.r14_usd = r14_usd;
		this.r14_mur1 = r14_mur1;
		this.r14_i = r14_i;
		this.r14_mur2 = r14_mur2;
		this.r15_usd = r15_usd;
		this.r15_mur1 = r15_mur1;
		this.r15_i = r15_i;
		this.r15_mur2 = r15_mur2;
		this.r16_usd = r16_usd;
		this.r16_mur1 = r16_mur1;
		this.r16_i = r16_i;
		this.r16_mur2 = r16_mur2;
		this.r17_usd = r17_usd;
		this.r17_mur1 = r17_mur1;
		this.r17_i = r17_i;
		this.r17_mur2 = r17_mur2;
		this.r19_usd = r19_usd;
		this.r19_mur1 = r19_mur1;
		this.r19_i = r19_i;
		this.r19_mur2 = r19_mur2;
		this.recon_date = recon_date;
		this.posting_date = posting_date;
		this.trnx_id = trnx_id;
		this.currency = currency;
		this.del_flg = del_flg;
		this.entity_flg = entity_flg;
		this.entry_user = entry_user;
		this.entry_time = entry_time;
		this.modify_user = modify_user;
		this.modify_time = modify_time;
		this.verify_user = verify_user;
		this.verify_time = verify_time;
	}
	public RECON_CRREPORT_USD_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}

}
