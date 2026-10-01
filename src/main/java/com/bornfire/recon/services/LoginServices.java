package com.bornfire.recon.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import com.bornfire.recon.config.PasswordEncryption;
import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.RECON_SESSION;
import com.bornfire.recon.entities.USER_PROFILE_ENTITY;
import com.bornfire.recon.entities.USER_PROFILE_REPO;
import com.bornfire.recon.entities.USER_PROFILE_MOD_ENTITY;
import com.bornfire.recon.entities.USER_PROFILE_MOD_REPO;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Service
@ConfigurationProperties("output")
@Transactional
public class LoginServices {

	private static final Logger logger = LoggerFactory.getLogger(LoginServices.class);

	@Autowired
	USER_PROFILE_REPO userProfileRep;
 
	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	DataSource srcdataSource;

	@Autowired
	SequenceGenerator sequence;
	
	@Autowired
	BRECON_AUDIT_REPO AuditRepo;
	
	@Autowired
	USER_PROFILE_MOD_REPO User_Profile_Mod_Repo;
	
	@Autowired
	USER_PROFILE_REPO UserProfileRep;
	
	@NotNull
	private String exportpath;
	
	@Autowired
	AuidtConfigure audit2;

	@Value("${default.password}")
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getExportpath() {
		return exportpath;
	}

	public void setExportpath(String exportpath) {
		this.exportpath = exportpath;
	}

	/*
	 * Getting 3 inputs -
	 * 
	 * UserProfile Object, Formmode - Valid values : add, edit, inputuser - user who
	 * edited the data
	 * 
	 * if formmode is add - Get password from application.properties create the user
	 * 
	 * if formmode is edit - Get password from database for that user and use other
	 * fields came from front end.
	 * 
	 * 
	 */

	public String addUser(USER_PROFILE_MOD_ENTITY userprofile_mod, String formmode, String inputUser, String userName) {
	    String msg = "";

	    try {
	        if (formmode.equals("add")) {
	        	System.out.println("add");
	        	USER_PROFILE_MOD_ENTITY up = userprofile_mod;
	            System.out.println(up.getUserid());     
	            String encryptedPassword = PasswordEncryption.getEncryptedPassword(this.password);
	            up.setCountrycode(userprofile_mod.getCountrycode());
	            up.setPassword(encryptedPassword);
	            up.setLogin_flg("N"); // prompt user to change password on first login
	            up.setNo_of_attmp(0);
	            up.setUser_locked_flg(up.getLogin_status().equals("Active") ? "N" : "Y");
	            up.setDisable_flg(up.getUser_status().equals("Active") ? "N" : "Y");
	            up.setEntity_flg("N");
	            up.setDel_flg("N");
	            up.setModify_flg("N");
	            up.setAuth_flg("N");
	            up.setNo_of_attmp(0);
	            up.setAcc_exp_date(up.getPass_exp_date());
	            up.setEntry_time(new Date());
	            up.setEntry_user(inputUser);
	            User_Profile_Mod_Repo.save(up);

	            audit2.insertAudit(inputUser, userName, "USER PROFILE CREATION", "USER CREATED SUCCESSFULLY","BRECON_USER_PROFILE_TABLE", "USER PROFILE");
	            msg = "New entry saved!";
	        } 
			
		
	    } catch (Exception e) {
	        e.printStackTrace();
	        logger.info(e.getMessage());
	        msg = "Error Occurred. Please contact Administrator";
	    }

	    return msg;
	}
	public String editUser(USER_PROFILE_MOD_ENTITY userProfile, String formmode, String inputUser, String userName) {
		String msg = "";
		String audit_ref_no = sequence.generateRequestUUId();
		USER_PROFILE_ENTITY mainrecord = userProfileRep.findByIdCustom(userProfile.getUserid());
		BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		try {
			if (formmode.equals("edit")) {
				// System.out.println("edit mode");
				Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(userProfile.getUserid());
				if (up.isPresent()) {
					USER_PROFILE_ENTITY us1 = up.get();
					DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
					String acctexpold = dateFormat.format(us1.getAcc_exp_date());
					String acctexpnew = dateFormat.format(userProfile.getAcc_exp_date());
					
					String disableStartOld = dateFormat.format(us1.getDisable_start_date());
					String disableStartNew = dateFormat.format(userProfile.getDisable_start_date());
					
					String disableEndOld = dateFormat.format(us1.getDisable_end_date());
					String disableEndNew = dateFormat.format(userProfile.getDisable_end_date());
					
					String passExpOld = dateFormat.format(us1.getPass_exp_date());
					String passExpNew = dateFormat.format(userProfile.getPass_exp_date());
					
					if ((us1.getBank_code().equals(userProfile.getBank_code()))
					        && (us1.getBank_name().equals(userProfile.getBank_name()))
					        && (us1.getBranch_code().equals(userProfile.getBranch_code()))
					        && (us1.getBranch_name().equals(userProfile.getBranch_name()))
					        && (us1.getEmpid().equals(userProfile.getEmpid()))
					        && (us1.getEmp_name().equals(userProfile.getEmp_name()))
					        && (us1.getUsername().equals(userProfile.getUsername()))
					        && (acctexpold.equals(acctexpnew))   // for acc_exp_date
					        && (us1.getLogin_low().equals(userProfile.getLogin_low()))
					        && (us1.getLogin_high().equals(userProfile.getLogin_high()))
					        && (disableStartOld.equals(disableStartNew))   // for disable_start_date
					        && (disableEndOld.equals(disableEndNew))       // for disable_end_date
					        && (passExpOld.equals(passExpNew))             // for pass_exp_date
					        && (us1.getUser_status().equals(userProfile.getUser_status()))
					        && (us1.getLogin_status().equals(userProfile.getLogin_status()))
					        && (us1.getMob_number().equals(userProfile.getMob_number()))
					        && (us1.getEmail_id().equals(userProfile.getEmail_id()))
					        && (us1.getRole_id().equals(userProfile.getRole_id()))
					        && (us1.getRole_desc().equals(userProfile.getRole_desc()))
					) {
						msg = "No Modification done";

					} else {

						userProfile.setPassword(up.get().getPassword());

						if (userProfile.getLogin_status().equals("ACTIVE")) {
							userProfile.setUser_locked_flg("N");
						} else {
							userProfile.setUser_locked_flg("Y");
						}

						if (userProfile.getUser_status().equals("ACTIVE")) {
							userProfile.setDisable_flg("N");
						} else {
							userProfile.setDisable_flg("Y");
						}

						userProfile.setNo_of_attmp(0);
						userProfile.setEntity_flg("N");
						userProfile.setModify_flg("Y");
						userProfile.setModify_user(inputUser);
						userProfile.setModify_time(new Date());
						userProfile.setEntry_time(mainrecord.getEntry_time());
						userProfile.setAuth_time(mainrecord.getAuth_time());
						userProfile.setEntry_user(mainrecord.getEntry_user());
						userProfile.setAuth_user(mainrecord.getAuth_user());
						userProfile.setPhoto(mainrecord.getPhoto());
						userProfile.setDisable_start_date(mainrecord.getDisable_start_date());
						userProfile.setDisable_end_date(mainrecord.getDisable_end_date());
						userProfile.setAuth_flg(mainrecord.getAuth_flg());
						Session session = sessionFactory.getCurrentSession();
						session.saveOrUpdate(userProfile);

						// userProfileModRep.save(userProfile)

						USER_PROFILE_ENTITY us = userProfileRep.findById(userProfile.getUserid()).get();
						us.setEntity_flg("N");
						userProfileRep.save(us);
						msg = "User Edited Successfully";

						StringBuilder stringBuilder = new StringBuilder();

						if ((us1.getEmail_id().equals(userProfile.getEmail_id()))
								&& (us1.getMob_number().equals(userProfile.getMob_number()))
								&& (us1.getLogin_low().equals(userProfile.getLogin_low()))
								&& (us1.getLogin_high().equals(userProfile.getLogin_high()))
								&& (acctexpold.equals(acctexpnew))
								&& (us1.getUser_status().equals(userProfile.getUser_status()))
								&& (us1.getLogin_status().equals(userProfile.getLogin_status()))
								// && (us1.getRemarks().equals(userProfile.getRemarks()))
								&& (us1.getRole_id().equals(userProfile.getRole_id()))) {

						}
						if (!us1.getEmail_id().equals(userProfile.getEmail_id())) {
							stringBuilder = stringBuilder
									.append("Email ID+" + us1.getEmail_id() + "+" + userProfile.getEmail_id() + "||");
						}

						if (!us1.getMob_number().equals(userProfile.getMob_number())) {
							stringBuilder = stringBuilder.append(
									"Mobile Number+" + us1.getMob_number() + "+" + userProfile.getMob_number() + "||");
						}

						if (!us1.getLogin_low().equals(userProfile.getLogin_low())) {
							stringBuilder = stringBuilder.append(
									"Login Low Time+" + us1.getLogin_low() + "+" + userProfile.getLogin_low() + "||");
						}

						if (!us1.getLogin_high().equals(userProfile.getLogin_high())) {
							stringBuilder = stringBuilder.append("Login High Time+" + us1.getLogin_high() + "+"
									+ userProfile.getLogin_high() + "||");
						}

						if ((!acctexpold.equals(acctexpnew))) {
							stringBuilder = stringBuilder
									.append("Account Expiry Time+" + acctexpold + "+" + acctexpnew + "||");
						}

						if (!us1.getUser_status().equals(userProfile.getUser_status())) {
							stringBuilder = stringBuilder.append(
									"User Status+" + us1.getUser_status() + "+" + userProfile.getUser_status() + "||");
						}

						if (!us1.getLogin_status().equals(userProfile.getLogin_status())) {
							stringBuilder = stringBuilder.append("Login Status+" + us1.getLogin_status() + "+"
									+ userProfile.getLogin_status() + "||");
						}

						if (!us1.getRole_id().equals(userProfile.getRole_id())) {
							stringBuilder = stringBuilder
									.append("Role ID+" + us1.getRole_id() + "+" + userProfile.getRole_id());
						}

						/*
						 * if(!us1.getRemarks().equals(userProfile.getRemarks())) {
						 * stringBuilder=stringBuilder.append("Remarks+"+us1.getRemarks()+"+"+
						 * userProfile.getRemarks()); }
						 */
   
						audit2.insertAudit(inputUser, userName, "USER PROFILE MODIFICATION", "USER MODIFIED SUCCESSFULLY","BRECON_USER_PROFILE_TABLE", "USER PROFILE");
					}
				}
			}
		} catch (Exception e) {
			msg = "Error Occured. Please contact Administrator";
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}

		return msg;

	}

	public Iterable<USER_PROFILE_ENTITY> getUsersList() {
		Iterable<USER_PROFILE_ENTITY> users = userProfileRep.getList();
		return users;
	}

	public USER_PROFILE_ENTITY  getUser(String id) {
		logger.info(id);
		if (userProfileRep.existsById(id)) {
			USER_PROFILE_ENTITY  up = userProfileRep.findById(id).get();
			logger.info(up.getEntity_flg());
			return up;
		} else {
			return new  USER_PROFILE_ENTITY ();
		}

	};

	public USER_PROFILE_MOD_ENTITY getModUser(String id) {
		logger.info(id);
		if (User_Profile_Mod_Repo.existsById(id)) {
			USER_PROFILE_MOD_ENTITY up = User_Profile_Mod_Repo.findById(id).get();
			logger.info(up.getEntity_flg());
			return up;
		} else {
			return new USER_PROFILE_MOD_ENTITY();
		}

	};

	public String verifyUser(USER_PROFILE_MOD_ENTITY up, String inputUser, String entryuser, Date entrytime) {

		String msg = "";
		USER_PROFILE_MOD_ENTITY mainrecord = User_Profile_Mod_Repo.findByIdCustom(up.getUserid());
		Optional<USER_PROFILE_ENTITY> user1 = userProfileRep.findById(up.getUserid());
		String audit_ref_no = sequence.generateRequestUUId();
		try {
			System.out.println(up.getLogin_status());
			System.out.println(up.getUser_status());
			if (up.getLogin_status().equalsIgnoreCase("ACTIVE")) {
				up.setUser_locked_flg("N");
				up.setNo_of_attmp(0);
			} else {
				up.setUser_locked_flg("Y");
			}
			if (up.getUser_status().equalsIgnoreCase("ACTIVE")) {
				up.setDisable_flg("N");
			} else {
				up.setDisable_flg("Y");
			}
			System.out.println(up.getUser_locked_flg());
			System.out.println(up.getDisable_flg());
			up.setEntry_user(entryuser);
			up.setEntry_time(entrytime);
			up.setEntity_flg("Y");
			up.setAuth_time(new Date());
			up.setAuth_user(inputUser);
			up.setDel_flg("N");
			up.setModify_flg("N");
			up.setLogin_flg("N");
			up.setNo_of_attmp(0);
			up.setModify_user(mainrecord.getModify_user() == null ? null : mainrecord.getModify_user());
			up.setModify_time(mainrecord.getModify_time() == null ? null : mainrecord.getModify_time());
			up.setDisable_start_date(
					mainrecord.getDisable_start_date() == null ? null : mainrecord.getDisable_start_date());
			up.setDisable_end_date(mainrecord.getDisable_end_date() == null ? null : mainrecord.getDisable_end_date());
			up.setAuth_flg(mainrecord.getAuth_flg() == null ? null : mainrecord.getAuth_flg());
			LocalDate today = LocalDate.now();
			LocalDate expiryDate = today.plusDays(180);
			try {
				up.setPass_exp_date(java.sql.Date.valueOf(expiryDate));
				up.setAcc_exp_date(java.sql.Date.valueOf(expiryDate));
			} catch (Exception e) {
				LocalDate fallbackExpiryDate = today.plusDays(10);
				up.setPass_exp_date(java.sql.Date.valueOf(fallbackExpiryDate));
			}
			if (user1.isPresent()) {
				up.setPassword(user1.get().getPassword());
				Optional<USER_PROFILE_MOD_ENTITY> us = User_Profile_Mod_Repo.findById(up.getUserid());
				up.setPassword(us.get().getPassword());
				up.setPhoto(us.get().getPhoto());
				String entryUser = up.getEntry_user();
				USER_PROFILE_ENTITY userProfile = user1.get();
				userProfile.setEntry_user(entryUser);
				 userProfileRep.save(userProfile); 
			} else {
				Optional<USER_PROFILE_MOD_ENTITY> us = User_Profile_Mod_Repo.findById(up.getUserid());
				up.setPassword(us.get().getPassword());
				up.setPhoto(us.get().getPhoto());
			}
			USER_PROFILE_ENTITY user = new USER_PROFILE_ENTITY(up);
			user.setEntity_flg("Y");
			//user.setUserlog_flg("BUSER");
			user.setPass_exp_date(up.getPass_exp_date());
			user.setAcc_exp_date(up.getPass_exp_date());
			user.setEntry_user(up.getEntry_user());
			user.setEntry_time(up.getEntry_time());
			user.setModify_time(up.getModify_time());
			user.setModify_user(up.getModify_user());
		    userProfileRep.save(user); 
			if (!user1.isPresent()) {
				audit2.insertAudit(inputUser, entryuser, "USER PROFILE VERIFICATION", "USER VERIFIED SUCCESSFULLY","BRECON_USER_PROFILE_TABLE", "USER PROFILE"); 
			} else {
				BRECON_Audit_Entity audit1 = AuditRepo.getModifyList(up.getUserid(), "USER MODIFICATION");
				BRECON_Audit_Entity auditTable = new BRECON_Audit_Entity();
				auditTable.setAudit_date(new Date());
				auditTable.setAudit_table("BRECON_USER_PROFILE_TABLE");
				auditTable.setAudit_screen("USER PROFILE - VERIFICATION");
				auditTable.setFunc_code("USER VERIFICATION");
				auditTable.setRemarks(up.getUserid() + " : Verified Successfully");
				auditTable.setEvent_id(up.getUserid());
				auditTable.setEvent_name(up.getUsername());
				auditTable.setModi_details(audit1.getModi_details());
				auditTable.setAuth_user(inputUser);
				auditTable.setAuth_time(new Date());
				auditTable.setEntry_time(new Date());
				auditTable.setEntry_user(inputUser);
				auditTable.setAudit_ref_no(audit_ref_no);
				 AuditRepo.save(auditTable); 
			}
			msg = "User verified Successfully";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}
	public String passwordReset(USER_PROFILE_ENTITY userprofile, String userid) {

		String msg = "";

		try {
			String encryptedPassword = PasswordEncryption.getEncryptedPassword(this.password);

			Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(userprofile.getUserid());

			if (up.isPresent()) {
				USER_PROFILE_ENTITY user = up.get();
				user.setPassword(encryptedPassword);
				user.setNo_of_attmp(0);
				user.setLogin_flg("N");
				user.setUser_locked_flg("N");
				userProfileRep.save(user);
			}

			msg = "Password Resetted Successfully";

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {

			e.printStackTrace();

			msg = "Error Occured. Please contact Administrator";
		}

		return msg;
	}

	/*
	 * Getting LoginFlg -
	 * 
	 * If loginFlg = 'N' - User should be prompted to change password. else thats
	 * not required.
	 * 
	 * Loginflg ='N' will be updated at the time of new user creation and at the
	 * time of password reset by admin.
	 * 
	 */
	public String checkPasswordChangeReq(String userid) {

		Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(userid);
		String loginflg = up.get().getLogin_flg();

		return loginflg;
	}

	public int checkAcctexpirty(String userid) {

		Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(userid);
		Date expDate = up.get().getAcc_exp_date();
		Date currDate = new Date();

		DateTime dt1 = new DateTime(currDate);
		DateTime dt2 = new DateTime(expDate);

		int remaindays = Days.daysBetween(dt1, dt2).getDays();

		logger.info("Account Expired in:" + remaindays);
		return remaindays;
	}

	public int checkpassexpirty(String userid) {

		Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(userid);
		Date expDate = up.get().getPass_exp_date();
		Date currDate = new Date();

		DateTime dt1 = new DateTime(currDate);
		DateTime dt2 = new DateTime(expDate);

		int remaindays = Days.daysBetween(dt1, dt2).getDays();

		logger.info("Password Expired in:" + remaindays);
		return remaindays;
	}

	public String changePassword(String oldpass, String newpass, String userid) {
		String msg = "";

		Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(userid);

		try {
			if (up.isPresent()) {
				USER_PROFILE_ENTITY user = up.get();
				if (PasswordEncryption.validatePassword(oldpass, user.getPassword())) {
					
					if (!PasswordEncryption.validatePassword(newpass, user.getPassword())) {
						
						String encryptedPassword = PasswordEncryption.getEncryptedPassword(newpass);
						user.setPassword(encryptedPassword);
						user.setLogin_flg("Y");
						
						LocalDateTime localDateTime = user.getPass_exp_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
						user.setPass_exp_date(Date.from(localDateTime.plusDays(365).atZone(ZoneId.systemDefault()).toInstant()));
						
						userProfileRep.save(user);
						msg = "Password Changed Successfully";
						
					}else {
						
						msg = "New password cannot be Same as Old password";
					}
					
					
				} else {
					msg = "Incorrect Old Password!";
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			msg = "Error Occured. Please contact Administrator";
		}
		logger.info(msg);
		return msg;
	};

	public void SessionLogging(String menuname, String menuid, String sessionid, String userid, String ip,
			String status) {
		Session hs = sessionFactory.getCurrentSession();

		try {

			if (menuname.equals("LOGOUT")) {

				hs.createQuery("update XBRLSession set status='IN-ACTIVE' where session_id = ?1")
						.setParameter(1, sessionid).executeUpdate();

			} else {
				
				hs.save(new RECON_SESSION(menuname, menuid, sessionid, userid, ip, new Date(), status));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String deleteUser1(USER_PROFILE_MOD_ENTITY userProfilemoden, String inputUser) {

		User_Profile_Mod_Repo.deleteById(userProfilemoden.getUserid());
		return inputUser;
	}

	

	public File getUserLogFile(Date fromdate, Date todate) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

		String path = exportpath;
		String fileName = "USER_LOGS_"+dateFormat.format(new Date())+".xlsx";
		File outputFile;

		File jasperFile;
		
		File folders = new File(path);
		if (!folders.exists()) {
			folders.mkdirs();
		}
		
		
		try {
			jasperFile = ResourceUtils.getFile("classpath:static/jasper/USER_LOGS/UserLogs.jasper");
			JasperReport jr = (JasperReport) JRLoader.loadObject(jasperFile);
			HashMap<String, Object> map = new HashMap<String, Object>();

			logger.info("Assigning Parameters for Jasper");
			map.put("FromDate", dateFormat.format(fromdate));
			map.put("ToDate", dateFormat.format(todate));
			
			
			
			path = path + "/" + fileName;
			JasperPrint jp = JasperFillManager.fillReport(jr, map, srcdataSource.getConnection());
			JRXlsxExporter exporter = new JRXlsxExporter();
			exporter.setExporterInput(new SimpleExporterInput(jp));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(path));
			exporter.exportReport();
			logger.info("Excel File exported");
			
		} catch (FileNotFoundException|JRException|SQLException e) {
			
			e.printStackTrace();
		}


		outputFile = new File(path);	

		
	return outputFile;
	}

	public List<RECON_SESSION> getUserLog(Date fromdate, Date todate) {
		
		
		Session hs = sessionFactory.getCurrentSession();
		
		List<RECON_SESSION> ls = hs.createQuery("from XBRLSession where trunc(entry_time,'DD') between ?1 and ?2 and menu in ('LOGIN','LOGOUT') order by entry_time desc ", RECON_SESSION.class)
		.setParameter(1, fromdate)
		.setParameter(2, todate)
		.getResultList();
		
		
		return ls;
	}
	
	


	public String getSrlNoValue() {
		Session hs = sessionFactory.getCurrentSession();

		DecimalFormat numformate = new DecimalFormat("000");
		BigDecimal billNumber = (BigDecimal) hs.createNativeQuery("SELECT SEN_MANUAL_SEQ.NEXTVAL AS SRL_NO FROM DUAL").getSingleResult();
		String serialno = "ALT" + numformate.format(billNumber);
		System.out.println("billno" + serialno);
		return serialno;
	}
	
	public USER_PROFILE_ENTITY UserBlobImages(String userID) {
		// System.out.println(userID);
		List<USER_PROFILE_ENTITY> query = userProfileRep.getBlobImg(userID);
		if (query.isEmpty()) {
			return new USER_PROFILE_ENTITY();
		} else {
			return query.get(0);
		}
	}
	public String cancel( String inputUser) {
		String msg = "";
		User_Profile_Mod_Repo.deleteById(inputUser);
		msg = "Last changes are removed ";
		return msg;
	}

	
	public String cancelUserentity(USER_PROFILE_MOD_ENTITY userProfile, String inputUser, String userName) {
		String msg = "";
		Optional<USER_PROFILE_MOD_ENTITY> up = User_Profile_Mod_Repo.findById(userProfile.getUserid());
		if (up.isPresent()) {
			String audit_ref_no = sequence.generateRequestUUId();
			userProfile.setPassword(up.get().getPassword());
			USER_PROFILE_MOD_ENTITY user = up.get();
			user.setNo_of_attmp(0);
			user.setEntity_flg("Y");
			user.setModify_user(inputUser);
			user.setModify_time(new Date());
			user.setLogin_flg("Y");
			User_Profile_Mod_Repo.save(user);
			BRECON_Audit_Entity audit1 = AuditRepo.getModifyList(userProfile.getUserid(), "USER MODIFICATION");
			
			audit2.insertAudit(inputUser, userName, "USER PROFILE DELETE", "USER DELETED SUCCESSFULLY","BRECON_USER_PROFILE_TABLE", "USER PROFILE");
		}
		return msg;
	}
	
 

}