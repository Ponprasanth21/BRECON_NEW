package com.bornfire.recon.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name = "RULE_ENGINE_TABLE")
public class RATE_ENGINE_ENTITY {
	
	private String	rule_code;
	private BigDecimal	rule_srl;
	private String	description;
	private String	match_type;
	private String	internal_ref;
	private String	external_ref;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	value_date;
	private String	amount_type;
	private BigDecimal	tolerance;
	private String	match_status;
	private String	adjustment_entry;
	private BigDecimal	matching_sequence;
	private String	remarks;
	private String	entity_flg;
	private String	del_flg;
	private String	modify_flg;
	private String	entry_user;
	private String	modify_user;
	private String	verify_user;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	entry_time;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	modify_time;
	@DateTimeFormat(pattern="dd-MM-yyyy HH:mm")
	private Date	verify_time;
	private String	version;
	@Id
	private String	unique_id;
	private String	auth_flg;
	
	public String getAuth_flg() {
		return auth_flg;
	}
	public void setAuth_flg(String auth_flg) {
		this.auth_flg = auth_flg;
	}
	public String getUnique_id() {
		return unique_id;
	}
	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getRule_code() {
		return rule_code;
	}
	public void setRule_code(String rule_code) {
		this.rule_code = rule_code;
	}
	public BigDecimal getRule_srl() {
		return rule_srl;
	}
	public void setRule_srl(BigDecimal rule_srl) {
		this.rule_srl = rule_srl;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMatch_type() {
		return match_type;
	}
	public void setMatch_type(String match_type) {
		this.match_type = match_type;
	}
	public String getInternal_ref() {
		return internal_ref;
	}
	public void setInternal_ref(String internal_ref) {
		this.internal_ref = internal_ref;
	}
	public String getExternal_ref() {
		return external_ref;
	}
	public void setExternal_ref(String external_ref) {
		this.external_ref = external_ref;
	}
	public Date getValue_date() {
		return value_date;
	}
	public void setValue_date(Date value_date) {
		this.value_date = value_date;
	}
	public String getAmount_type() {
		return amount_type;
	}
	public void setAmount_type(String amount_type) {
		this.amount_type = amount_type;
	}
	public BigDecimal getTolerance() {
		return tolerance;
	}
	public void setTolerance(BigDecimal tolerance) {
		this.tolerance = tolerance;
	}
	public String getMatch_status() {
		return match_status;
	}
	public void setMatch_status(String match_status) {
		this.match_status = match_status;
	}
	public String getAdjustment_entry() {
		return adjustment_entry;
	}
	public void setAdjustment_entry(String adjustment_entry) {
		this.adjustment_entry = adjustment_entry;
	}
	public BigDecimal getMatching_sequence() {
		return matching_sequence;
	}
	public void setMatching_sequence(BigDecimal matching_sequence) {
		this.matching_sequence = matching_sequence;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getEntity_flg() {
		return entity_flg;
	}
	public void setEntity_flg(String entity_flg) {
		this.entity_flg = entity_flg;
	}
	public String getDel_flg() {
		return del_flg;
	}
	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
	}
	public String getModify_flg() {
		return modify_flg;
	}
	public void setModify_flg(String modify_flg) {
		this.modify_flg = modify_flg;
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
	public String getVerify_user() {
		return verify_user;
	}
	public void setVerify_user(String verify_user) {
		this.verify_user = verify_user;
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
	public Date getVerify_time() {
		return verify_time;
	}
	public void setVerify_time(Date verify_time) {
		this.verify_time = verify_time;
	}
	public RATE_ENGINE_ENTITY(String rule_code, BigDecimal rule_srl, String description, String match_type,
			String internal_ref, String external_ref, Date value_date, String amount_type, BigDecimal tolerance,
			String match_status, String adjustment_entry, BigDecimal matching_sequence, String remarks,
			String entity_flg, String del_flg, String modify_flg, String entry_user, String modify_user,
			String verify_user, Date entry_time, Date modify_time, Date verify_time, String version, String unique_id, String auth_flg) {
		super();
		this.rule_code = rule_code;
		this.rule_srl = rule_srl;
		this.description = description;
		this.match_type = match_type;
		this.internal_ref = internal_ref;
		this.external_ref = external_ref;
		this.value_date = value_date;
		this.amount_type = amount_type;
		this.tolerance = tolerance;
		this.match_status = match_status;
		this.adjustment_entry = adjustment_entry;
		this.matching_sequence = matching_sequence;
		this.remarks = remarks;
		this.entity_flg = entity_flg;
		this.del_flg = del_flg;
		this.modify_flg = modify_flg;
		this.entry_user = entry_user;
		this.modify_user = modify_user;
		this.verify_user = verify_user;
		this.entry_time = entry_time;
		this.modify_time = modify_time;
		this.verify_time = verify_time;
		this.version = version;
		this.unique_id =unique_id;
		this.auth_flg =auth_flg;
	}
	public RATE_ENGINE_ENTITY() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
