package com.bornfire.recon.entities;

import java.util.Date;
import java.util.List;

public class AUDIT_TABLE_POJO {
	private String audit_ref_no;
	private Date audit_date;
	private String audit_table;
	private String audit_screen;
	private String event_id;
	private String event_name;
	private String entry_user;
	private Date entry_time;
	private String remarks;
	private String auth_user;
	private Date auth_time;
	private String func_code;
	private List<String> fieldName;
	private List<String> newvalue;
	private List<String> oldvalue;
	public String getAudit_ref_no() {
		return audit_ref_no;
	}
	public void setAudit_ref_no(String audit_ref_no) {
		this.audit_ref_no = audit_ref_no;
	}
	public Date getAudit_date() {
		return audit_date;
	}
	public void setAudit_date(Date audit_date) {
		this.audit_date = audit_date;
	}
	public String getAudit_table() {
		return audit_table;
	}
	public void setAudit_table(String audit_table) {
		this.audit_table = audit_table;
	}
	public String getAudit_screen() {
		return audit_screen;
	}
	public void setAudit_screen(String audit_screen) {
		this.audit_screen = audit_screen;
	}
	public String getEvent_id() {
		return event_id;
	}
	public void setEvent_id(String event_id) {
		this.event_id = event_id;
	}
	public String getEvent_name() {
		return event_name;
	}
	public void setEvent_name(String event_name) {
		this.event_name = event_name;
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
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getAuth_user() {
		return auth_user;
	}
	public void setAuth_user(String auth_user) {
		this.auth_user = auth_user;
	}
	public Date getAuth_time() {
		return auth_time;
	}
	public void setAuth_time(Date auth_time) {
		this.auth_time = auth_time;
	}
	public String getFunc_code() {
		return func_code;
	}
	public void setFunc_code(String func_code) {
		this.func_code = func_code;
	}
	public List<String> getFieldName() {
		return fieldName;
	}
	public void setFieldName(List<String> fieldName) {
		this.fieldName = fieldName;
	}
	public List<String> getNewvalue() {
		return newvalue;
	}
	public void setNewvalue(List<String> newvalue) {
		this.newvalue = newvalue;
	}
	public List<String> getOldvalue() {
		return oldvalue;
	}
	public void setOldvalue(List<String> oldvalue) {
		this.oldvalue = oldvalue;
	}
	
	
	
}
