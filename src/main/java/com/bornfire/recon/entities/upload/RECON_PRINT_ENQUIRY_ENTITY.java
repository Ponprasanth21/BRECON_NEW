package com.bornfire.recon.entities.upload;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name = "BRECON_PRINT_QUEUE_TABLE")
public class RECON_PRINT_ENQUIRY_ENTITY {
    @Id
	private String	srl_no;
	private String	rpt_code;
	private String	rpt_name;
	private String	rpt_location;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	rpt_date;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	gen_date;
	private String	gen_by;
	@DateTimeFormat(pattern="dd-MM-yyyy")
	private Date	print_date;
	private String	print_by;
	private BigDecimal	print_count;
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
	public String getSrl_no() {
		return srl_no;
	}
	public void setSrl_no(String srl_no) {
		this.srl_no = srl_no;
	}
	public String getRpt_code() {
		return rpt_code;
	}
	public void setRpt_code(String rpt_code) {
		this.rpt_code = rpt_code;
	}
	public String getRpt_name() {
		return rpt_name;
	}
	public void setRpt_name(String rpt_name) {
		this.rpt_name = rpt_name;
	}
	public String getRpt_location() {
		return rpt_location;
	}
	public void setRpt_location(String rpt_location) {
		this.rpt_location = rpt_location;
	}
	public Date getRpt_date() {
		return rpt_date;
	}
	public void setRpt_date(Date rpt_date) {
		this.rpt_date = rpt_date;
	}
	public Date getGen_date() {
		return gen_date;
	}
	public void setGen_date(Date gen_date) {
		this.gen_date = gen_date;
	}
	public String getGen_by() {
		return gen_by;
	}
	public void setGen_by(String gen_by) {
		this.gen_by = gen_by;
	}
	public Date getPrint_date() {
		return print_date;
	}
	public void setPrint_date(Date print_date) {
		this.print_date = print_date;
	}
	public String getPrint_by() {
		return print_by;
	}
	public void setPrint_by(String print_by) {
		this.print_by = print_by;
	}
	public BigDecimal getPrint_count() {
		return print_count;
	}
	public void setPrint_count(BigDecimal print_count) {
		this.print_count = print_count;
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
	public RECON_PRINT_ENQUIRY_ENTITY(String srl_no, String rpt_code, String rpt_name, String rpt_location,
			Date rpt_date, Date gen_date, String gen_by, Date print_date, String print_by, BigDecimal print_count,
			String del_flg, String entity_flg, String entry_user, Date entry_time, String modify_user, Date modify_time,
			String verify_user, Date verify_time) {
		super();
		this.srl_no = srl_no;
		this.rpt_code = rpt_code;
		this.rpt_name = rpt_name;
		this.rpt_location = rpt_location;
		this.rpt_date = rpt_date;
		this.gen_date = gen_date;
		this.gen_by = gen_by;
		this.print_date = print_date;
		this.print_by = print_by;
		this.print_count = print_count;
		this.del_flg = del_flg;
		this.entity_flg = entity_flg;
		this.entry_user = entry_user;
		this.entry_time = entry_time;
		this.modify_user = modify_user;
		this.modify_time = modify_time;
		this.verify_user = verify_user;
		this.verify_time = verify_time;
	}
	public RECON_PRINT_ENQUIRY_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
