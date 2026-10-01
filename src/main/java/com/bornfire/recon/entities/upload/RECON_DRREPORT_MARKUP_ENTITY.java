package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "DEBIT_MARKUP_FEES_TABLE")
public class RECON_DRREPORT_MARKUP_ENTITY {
	private String	lacct_name;
	private String	lgl_no;
	private BigDecimal	lmur;
	private BigDecimal	lusd;
	private BigDecimal	lcad;
	private BigDecimal	leur;
	@Column(name ="lmur(equiv)")
	private BigDecimal	lmur_equiv;
	private BigDecimal	lchf;
	private BigDecimal	lgbp;
	private String	racct_name;
	private String	rgl_no;
	private BigDecimal	rchf;
	@Column(name ="rmur(equiv)")
	private BigDecimal	rmur_equiv;
	private BigDecimal	rgbp;
	private BigDecimal	rmur;
	private BigDecimal	rusd;
	private BigDecimal	reur;
	private String	acct_type;
	private String	recon_type;
	private String	markup_type;
	@Id
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	recon_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	posting_date;
	private String	rcad;
	public String getLacct_name() {
		return lacct_name;
	}
	public void setLacct_name(String lacct_name) {
		this.lacct_name = lacct_name;
	}
	public String getLgl_no() {
		return lgl_no;
	}
	public void setLgl_no(String lgl_no) {
		this.lgl_no = lgl_no;
	}
	public BigDecimal getLmur() {
		return lmur;
	}
	public void setLmur(BigDecimal lmur) {
		this.lmur = lmur;
	}
	public BigDecimal getLusd() {
		return lusd;
	}
	public void setLusd(BigDecimal lusd) {
		this.lusd = lusd;
	}
	public BigDecimal getLcad() {
		return lcad;
	}
	public void setLcad(BigDecimal lcad) {
		this.lcad = lcad;
	}
	public BigDecimal getLeur() {
		return leur;
	}
	public void setLeur(BigDecimal leur) {
		this.leur = leur;
	}
	public BigDecimal getLmur_equiv() {
		return lmur_equiv;
	}
	public void setLmur_equiv(BigDecimal lmur_equiv) {
		this.lmur_equiv = lmur_equiv;
	}
	public BigDecimal getLchf() {
		return lchf;
	}
	public void setLchf(BigDecimal lchf) {
		this.lchf = lchf;
	}
	public BigDecimal getLgbp() {
		return lgbp;
	}
	public void setLgbp(BigDecimal lgbp) {
		this.lgbp = lgbp;
	}
	public String getRacct_name() {
		return racct_name;
	}
	public void setRacct_name(String racct_name) {
		this.racct_name = racct_name;
	}
	public String getRgl_no() {
		return rgl_no;
	}
	public void setRgl_no(String rgl_no) {
		this.rgl_no = rgl_no;
	}
	public BigDecimal getRchf() {
		return rchf;
	}
	public void setRchf(BigDecimal rchf) {
		this.rchf = rchf;
	}
	public BigDecimal getRmur_equiv() {
		return rmur_equiv;
	}
	public void setRmur_equiv(BigDecimal rmur_equiv) {
		this.rmur_equiv = rmur_equiv;
	}
	public BigDecimal getRgbp() {
		return rgbp;
	}
	public void setRgbp(BigDecimal rgbp) {
		this.rgbp = rgbp;
	}
	public BigDecimal getRmur() {
		return rmur;
	}
	public void setRmur(BigDecimal rmur) {
		this.rmur = rmur;
	}
	public BigDecimal getRusd() {
		return rusd;
	}
	public void setRusd(BigDecimal rusd) {
		this.rusd = rusd;
	}
	public BigDecimal getReur() {
		return reur;
	}
	public void setReur(BigDecimal reur) {
		this.reur = reur;
	}
	public String getAcct_type() {
		return acct_type;
	}
	public void setAcct_type(String acct_type) {
		this.acct_type = acct_type;
	}
	public String getRecon_type() {
		return recon_type;
	}
	public void setRecon_type(String recon_type) {
		this.recon_type = recon_type;
	}
	public String getMarkup_type() {
		return markup_type;
	}
	public void setMarkup_type(String markup_type) {
		this.markup_type = markup_type;
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
	public String getRcad() {
		return rcad;
	}
	public void setRcad(String rcad) {
		this.rcad = rcad;
	}
	public RECON_DRREPORT_MARKUP_ENTITY(String lacct_name, String lgl_no, BigDecimal lmur, BigDecimal lusd,
			BigDecimal lcad, BigDecimal leur, BigDecimal lmur_equiv, BigDecimal lchf, BigDecimal lgbp,
			String racct_name, String rgl_no, BigDecimal rchf, BigDecimal rmur_equiv, BigDecimal rgbp, BigDecimal rmur,
			BigDecimal rusd, BigDecimal reur, String acct_type, String recon_type, String markup_type, Date recon_date,
			Date posting_date, String rcad) {
		super();
		this.lacct_name = lacct_name;
		this.lgl_no = lgl_no;
		this.lmur = lmur;
		this.lusd = lusd;
		this.lcad = lcad;
		this.leur = leur;
		this.lmur_equiv = lmur_equiv;
		this.lchf = lchf;
		this.lgbp = lgbp;
		this.racct_name = racct_name;
		this.rgl_no = rgl_no;
		this.rchf = rchf;
		this.rmur_equiv = rmur_equiv;
		this.rgbp = rgbp;
		this.rmur = rmur;
		this.rusd = rusd;
		this.reur = reur;
		this.acct_type = acct_type;
		this.recon_type = recon_type;
		this.markup_type = markup_type;
		this.recon_date = recon_date;
		this.posting_date = posting_date;
		this.rcad = rcad;
	}
	public RECON_DRREPORT_MARKUP_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	


}
