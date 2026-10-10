package com.bornfire.recon.controllers;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_ENTITY;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_REPO;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_TEMP_ENTITY;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_TEMP_REPO;
import com.bornfire.recon.entities.AccessAndRolesDTO;
import com.bornfire.recon.entities.BANK_AND_BRANCH_BEAN;
import com.bornfire.recon.entities.BANK_AND_BRANCH_REPO;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.FOLLOW_UP_REPO;
import com.bornfire.recon.entities.RATE_ENGINE_ENTITY;
import com.bornfire.recon.entities.RATE_ENGINE_REPO;
import com.bornfire.recon.entities.RATE_MAINMOD_ENTITY;
import com.bornfire.recon.entities.RATE_MAIN_REPO;
import com.bornfire.recon.entities.REFERENCE_CODE_ENTITY;
import com.bornfire.recon.entities.REFERENCE_CODE_REPO;
import com.bornfire.recon.entities.USER_PROFILE_ENTITY;
import com.bornfire.recon.entities.USER_PROFILE_MOD_ENTITY;
import com.bornfire.recon.entities.USER_PROFILE_REPO;
import com.bornfire.recon.entities.upload.RECON_ACT_MST_ENTITY;
import com.bornfire.recon.entities.upload.RECON_ACT_MST_REPO;
import com.bornfire.recon.entities.upload.RECON_ACT_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_CRMAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_DREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRFILE_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_DRFILE_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_DRMAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_DRREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_DRREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_PARAMETER_REPO;
import com.bornfire.recon.entities.upload.RECON_PRINT_ENQUIRY_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_DREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_DRREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_DRREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_DRREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIMAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_REPO;
import com.bornfire.recon.services.AccessAndRolesServices;
import com.bornfire.recon.services.AuidtConfigure;
import com.bornfire.recon.services.BIPSBankandBranchServices;
import com.bornfire.recon.services.LoginServices;
import com.bornfire.recon.services.RateMaintenance;
import com.bornfire.recon.services.ReportServices;
import com.bornfire.recon.services.ReportServices.ReportTitle;
import com.bornfire.recon.services.upload.CrUploadService;
import com.bornfire.recon.services.upload.DrUploadService;
import com.bornfire.recon.services.upload.NostroService;
import com.bornfire.recon.services.upload.UpiUploadService;

@Controller
@ConfigurationProperties("default")
public class BRECONNavigationController {

	private static final Logger logger = LoggerFactory.getLogger(BRECONNavigationController.class);

	@Autowired
	LoginServices loginServices;

	@Autowired
	AuidtConfigure audit;

	@Autowired
	ReportServices reportServices;

	@Autowired
	RateMaintenance rateMainService;

	@Autowired
	RATE_MAIN_REPO rate_main_Repo;

	@Autowired
	ACCESS_AND_ROLES_REPO accessandrolesrepository;

	@Autowired
	AccessAndRolesServices AccessRoleService;

	@Autowired
	BIPSBankandBranchServices bankandBranchServices;

	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;

	@Autowired
	BANK_AND_BRANCH_REPO bipsSolRepository;

	@Autowired
	REFERENCE_CODE_REPO referenceCodeRep;

	@Autowired
	RECON_PARAMETER_REPO recon_Parameter_Repo;

	@Autowired
	RECON_ACT_REPO recon_Act_Repo;

	@Autowired
	RECON_ACT_MST_REPO recon_Act_Mst_Repo;

	@Autowired
	RECON_CRFILE_SOURCE_REPO RECON_CRFILE_SOURCE_REPO;

	@Autowired
	RECON_DRFILE_SOURCE_REPO RECON_DRFILE_SOURCE_REPO;

	@Autowired
	RECON_UPI_DESTINATION_REPO BRECON_UPI_DESTINATION_REPO;

	@Autowired
	RECON_CRFILE_DESTINATION_REPO RECON_CRFILE_DESTINATION_REPO;

	@Autowired
	RECON_DRFILE_DESTINATION_REPO BRECON_DRFILE_DESTINATION_REPO;

	@Autowired(required = true)
	RECON_CRMAIN_REPO RECON_CRMAIN_REPO;

	@Autowired
	RECON_DRMAIN_REPO RECON_DRMAIN_REPO;

	@Autowired
	RATE_ENGINE_REPO Rate_Engine_Repo;

	@Autowired
	USER_PROFILE_REPO UserProfileRep;

	@Autowired
	AuidtConfigure AuidtConfigure;

	@Autowired
	SequenceGenerator sequence;

	@Autowired
	FOLLOW_UP_REPO follow_Up_Repo;

	@Autowired
	RECON_CRREPORT_MUR_REPO RECON_CRREPORT_MUR_REPO;

	@Autowired
	RECON_CRREPORT_USD_REPO RECON_CRREPORT_USD_REPO;

	@Autowired
	private DataSource dataSource;

	@Autowired
	RECON_UPIREPORT_USD_REPO RECON_UPIREPORT_USD_REPO;

	@Autowired
	RECON_UPIREPORT_MUR_REPO RECON_UPIREPORT_MUR_REPO;

	@Autowired
	CrUploadService crUploadService;

	@Autowired
	UpiUploadService UpiUploadService;

	@Autowired
	DrUploadService DrUploadService;

	@Autowired
	RECON_UPIMAIN_REPO RECON_UPIMAIN_REPO;

	@Autowired
	RECON_PRINT_ENQUIRY_REPO PRINT_ENQUIRY_REPO;

	@Autowired
	RECON_UPI_DESTINATION_REPO RECON_UPI_DESTINATION_REPO;

	@Autowired
	RECON_UPI_SOURCE_REPO RECON_UPI_SOURCE_REPO;

	@Autowired
	RECON_DRREPORT_USD_REPO RECON_DREPORT_USD_REPO;

	@Autowired
	ACCESS_AND_ROLES_TEMP_REPO aCCESS_AND_ROLES_TEMP_REPO;

	@Autowired
	RECON_DRREPORT_MUR_REPO RECON_DRREPORT_MUR_REPO;

	@Autowired
	NostroService nostroService;

	@Autowired
	RECON_TEMP_DRREPORT_USD_REPO RECON_TEMP_DRREPORT_USD_REPO;

	@Autowired
	RECON_TEMP_DRREPORT_MUR_REPO RECON_TEMP_DRREPORT_MUR_REPO;

	@Autowired
	RECON_TEMP_UPIREPORT_MUR_REPO RECON_TEMP_UPIREPORT_MUR_REPO;

	@Autowired
	RECON_TEMP_UPIREPORT_USD_REPO RECON_TEMP_UPIREPORT_USD_REPO;

	private String pagesize;

	public String getPagesize() {
		return pagesize;
	}

	public void setPagesize(String pagesize) {
		this.pagesize = pagesize;
	}

	@RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public String getdashboard(Model md, HttpServletRequest req) {

		String domainid = (String) req.getSession().getAttribute("DOMAINID");

		String userid = (String) req.getSession().getAttribute("USERID");
		md.addAttribute("menu", "Dashboard");
		md.addAttribute("checkpassExpiry", loginServices.checkpassexpirty(userid));
		md.addAttribute("checkAcctExpiry", loginServices.checkAcctexpirty(userid));
		md.addAttribute("changepassword", loginServices.checkPasswordChangeReq(userid));

		int completed = 0;
		int uncompleted = 0;

		List<ReportTitle> ls = reportServices.getDashBoardRepList(domainid);

		for (ReportTitle var : ls) {
			if (var.getCompletedFlg().equals('Y')) {
				completed++;
			} else {
				uncompleted++;
			}
		}

		md.addAttribute("reportList", ls);
		md.addAttribute("completed", completed);
		md.addAttribute("uncompleted", uncompleted);
		md.addAttribute("menu", "Dashboard");
		return "XBRLDashboard";
	}

	@RequestMapping(value = "Dashboard", method = { RequestMethod.GET, RequestMethod.POST })
	public String dashboard(Model md, HttpServletRequest req) {

		String domainid = (String) req.getSession().getAttribute("DOMAINID");
		String userid = (String) req.getSession().getAttribute("USERID");

		md.addAttribute("changepassword", loginServices.checkPasswordChangeReq(userid));
		md.addAttribute("checkpassExpiry", loginServices.checkpassexpirty(userid));
		md.addAttribute("checkAcctExpiry", loginServices.checkAcctexpirty(userid));
		int completed = 0;
		int uncompleted = 0;

		/*
		 * List<ReportTitle> ls = reportServices.getDashBoardRepList(domainid);
		 * 
		 * for (ReportTitle var : ls) { if (var.getCompletedFlg().equals('Y')) {
		 * completed++; } else { uncompleted++; } }
		 * 
		 * md.addAttribute("reportList", ls);
		 */
		md.addAttribute("completed", completed);
		md.addAttribute("uncompleted", uncompleted);
		md.addAttribute("menu", "Dashboard");
		return "XBRLDashboard";
	}

	@RequestMapping(value = "UserProfile", method = { RequestMethod.GET, RequestMethod.POST })
	public String userprofile(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid,
			@RequestParam(value = "page", required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req) {

		int currentPage = page.orElse(0);
		int pageSize = size.orElse(Integer.parseInt(pagesize));

		String loginuserid = (String) req.getSession().getAttribute("USERID");
		String ROLEIDAC = (String) req.getSession().getAttribute("ROLEID");
		String WORKCLASSAC = (String) req.getSession().getAttribute("WORKCLASS");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");

		md.addAttribute("RuleIDType", accessandrolesrepository.roleidtype());
		loginServices.SessionLogging("USERPROFILE", "M2", req.getSession().getId(), loginuserid, req.getRemoteAddr(),
				"ACTIVE");
		md.addAttribute("loginuser", loginuserid);
		md.addAttribute("menu", "UserProfile");

		if (formmode == null || formmode.equals("list")) {

			md.addAttribute("formmode", "list"); // to set which form - valid values are "edit" , "add" & "list"
			md.addAttribute("WORKCLASSAC", WORKCLASSAC);
			md.addAttribute("ROLEIDAC", ROLEIDAC);
			md.addAttribute("userProfiles", UserProfileRep.getList());
			md.addAttribute("bankbranch", bipsSolRepository.BankandBranchList());

		} else if (formmode.equals("add")) {

			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", new USER_PROFILE_MOD_ENTITY());
			md.addAttribute("name", "");// Ensure you're passing an object, not calling a method without parentheses
			md.addAttribute("code", referenceCodeRep.getCountryCode());
			// user id sequence
			int maxSeq = 0;

			List<USER_PROFILE_ENTITY> sub = UserProfileRep.getUserId();
			int maxSeq1 = 0;
			for (USER_PROFILE_ENTITY srl : sub) {
				if (srl == null) {
					System.out.println("Found null entity in list!");
					continue;
				}

				String subcode = srl.getUserid();
				if (subcode == null) {
					System.out.println("Found entity with null rule_code: " + srl);
					continue;
				}
				if (srl.getUserid() != null && srl.getUserid().startsWith("BFI")) {
					try {
						int seq = Integer.parseInt(srl.getUserid().substring(3));
						if (seq > maxSeq1) {
							maxSeq1 = seq;
						}
					} catch (NumberFormatException e) {
						System.out.println("Invalid rule_code format: " + subcode);
					}
				}
			}
			int newSeq = maxSeq1 + 1;
			System.out.println();
			String formattedSeq = String.format("%03d", newSeq);
			String Seq = "BFI" + formattedSeq;
			System.out.println(Seq);
			md.addAttribute("Userid", Seq);
			md.addAttribute("role", accessandrolesrepository.rulelist());
			md.addAttribute("loginuserid", loginuserid);
			md.addAttribute("USERNAME", USERNAME);
			md.addAttribute("brcode", referenceCodeRep.getBranchCode());
			md.addAttribute("soltype", referenceCodeRep.getSolType());
		} else if (formmode.equals("edit")) {
			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getUser(userid));
			md.addAttribute("role", accessandrolesrepository.rulelist());
			md.addAttribute("code", referenceCodeRep.getCountryCode());
			md.addAttribute("brcode", referenceCodeRep.getBranchCode());
			md.addAttribute("soltype", referenceCodeRep.getSolType());
		} else if (formmode.equals("verify")) {
			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getModUser(userid));
		} else if (formmode.equals("view")) {
			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getUser(userid));
			md.addAttribute("code", referenceCodeRep.getCountryCode());
		} else if (formmode.equals("viewmod")) {
			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getModUser(userid));
			md.addAttribute("code", referenceCodeRep.getCountryCode());
		} else if (formmode.equals("delete")) {

			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getModUser(userid));
		} else {
			md.addAttribute("formmode", formmode);
			md.addAttribute("userProfile", loginServices.getUser(""));
		}

		return "UserProfile";
	}

	private Object UserProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	@RequestMapping(value = "BankBranchMaster", method = { RequestMethod.GET, RequestMethod.POST })
	public String BankBranchMaster(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String solId, @RequestParam(required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req) {

		int currentPage = page.orElse(0);
		int pageSize = size.orElse(Integer.parseInt(pagesize));

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));
		md.addAttribute("PdfViewer", "BankBranchMaster");

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("menuname", "Organization and Branch Details");
			md.addAttribute("menu", "MMenupage");
			md.addAttribute("formmode", "list");
			md.addAttribute("BankandBranchList",
					bankandBranchServices.BankandBranchList(PageRequest.of(currentPage, pageSize)));
		} else if (formmode.equals("add")) {
			md.addAttribute("formmode", "add");
			md.addAttribute("menuname1", "Organization and Branch Details-Add");
			md.addAttribute("BankandBranch", new BANK_AND_BRANCH_BEAN());
			md.addAttribute("brcode", referenceCodeRep.getBranchCode());
			md.addAttribute("soltype", referenceCodeRep.getSolType());
		} else if (formmode.equals("edit")) {
			md.addAttribute("formmode", "edit");
			md.addAttribute("menuname1", "Organization and Branch Details-Modify");
			md.addAttribute("BankandBranch", bankandBranchServices.getSolID(solId));
			md.addAttribute("brcode", referenceCodeRep.getBranchCode());
			md.addAttribute("soltype", referenceCodeRep.getSolType());
		} else if (formmode.equals("editnew")) {
			md.addAttribute("formmode", "edit");
			md.addAttribute("menuname1", "Organization and Branch Details-Modify");
			md.addAttribute("BankandBranch", bankandBranchServices.getModSolID(solId));
			md.addAttribute("brcode", referenceCodeRep.getBranchCode());
			md.addAttribute("soltype", referenceCodeRep.getSolType());
		} else if (formmode.equals("view")) {
			md.addAttribute("formmode", "view");
			md.addAttribute("menuname1", "Organization and Branch Details-Inquiry");
			md.addAttribute("BankandBranch", bankandBranchServices.getSolID(solId));
		} else if (formmode.equals("viewnew")) {
			md.addAttribute("formmode", "viewnew");
			md.addAttribute("menuname1", "Organization and Branch Details-Inquiry");
			md.addAttribute("BankandBranch", bankandBranchServices.getModSolID(solId));
		} else if (formmode.equals("verify")) {
			md.addAttribute("formmode", "verify");
			md.addAttribute("menuname1", "Organization and Branch Details-Verify");
			md.addAttribute("BankandBranch", bankandBranchServices.getModSolID(solId));
		} else if (formmode.equals("cancel")) {
			md.addAttribute("formmode", "cancel");
			md.addAttribute("menuname1", "Organization and Branch Details-Cancel");
			md.addAttribute("BankandBranch", bankandBranchServices.getModSolID(solId));
		}
		md.addAttribute("adminflag", "adminflag");
		return "BranchMaster";
	}

	@RequestMapping(value = "ReportCode", method = RequestMethod.GET)
	public String repcode(Model md, HttpServletRequest req) {

		String userid = (String) req.getSession().getAttribute("USERID");
		// Logging Navigation
		loginServices.SessionLogging("REPCODE", "M7", req.getSession().getId(), userid, req.getRemoteAddr(), "ACTIVE");

		md.addAttribute("menu", "ReportCode");
		return "XBRLRepCodeConfig";
	}

	@RequestMapping(value = "Userlog", method = RequestMethod.GET)
	public String userlog(Model md, HttpServletRequest req) {

		String userid = (String) req.getSession().getAttribute("USERID");
		// Logging Navigation
		loginServices.SessionLogging("USERLOG", "M4", req.getSession().getId(), userid, req.getRemoteAddr(), "ACTIVE");

		LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		md.addAttribute("menu", "Userlog");
		md.addAttribute("userlog", loginServices.getUserLog(
				Date.from(localDateTime.plusDays(-5).atZone(ZoneId.systemDefault()).toInstant()), new Date()));

		return "XBRLUserLogs";
	}

	@RequestMapping(value = "XBRLFileUpload", method = RequestMethod.GET)
	public String xbrlFileUpload(Model md, HttpServletRequest req) {

		String userid = (String) req.getSession().getAttribute("USERID");
		// Logging Navigation
		loginServices.SessionLogging("FILEUPLOAD", "M10", req.getSession().getId(), userid, req.getRemoteAddr(),
				"ACTIVE");

		md.addAttribute("menu", "XBRLFileUpload");

		String domainid = (String) req.getSession().getAttribute("DOMAINID");

		md.addAttribute("reportlist", reportServices.getFileUploadList());
		return "XBRLFileUpload";
	}

	@RequestMapping(value = "createUser", method = RequestMethod.POST)
	@ResponseBody
	public String createUser(@RequestParam("formmode") String formmode,
			@ModelAttribute USER_PROFILE_MOD_ENTITY userprofile_mod, Model md, HttpServletRequest rq,
			@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
		String userid = (String) rq.getSession().getAttribute("USERID");
		String userName = (String) rq.getSession().getAttribute("USERNAME");
		if (file != null) {
			byte[] byteArr = file.getBytes();
			userprofile_mod.setPhoto(byteArr);
		}
		String msg = loginServices.addUser(userprofile_mod, formmode, userid, userName);

		return msg;

	}

	@RequestMapping(value = "editUser", method = RequestMethod.POST)
	@ResponseBody
	public String editUser(@RequestParam("formmode") String formmode, @ModelAttribute USER_PROFILE_ENTITY userprofile,
			@ModelAttribute USER_PROFILE_MOD_ENTITY userProfile1,
			@RequestParam(value = "file", required = false) MultipartFile file, Model md, HttpServletRequest rq)
			throws ParseException, IOException {
		String userid = (String) rq.getSession().getAttribute("USERID");
		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));
		if (file != null) {
			byte[] byteArr = file.getBytes();
			userProfile1.setPhoto(byteArr);
		}
		String msg = loginServices.editUser(userProfile1, formmode, userid, USERNAME);
		// System.out.println(msg);
		return msg;
	}

	@RequestMapping(value = "verifyUser", method = RequestMethod.POST)
	@ResponseBody
	public String verifyUser(@RequestParam(required = false) String entryuser,
			@ModelAttribute USER_PROFILE_MOD_ENTITY userprofile, Model md, HttpServletRequest rq) {
		String userid = (String) rq.getSession().getAttribute("USERID");
		Date systemDate = new Date();

		String msg = loginServices.verifyUser(userprofile, userid, entryuser, systemDate);
		md.addAttribute("modtable", loginServices.deleteUser1(userprofile, userid));
		return msg;

	}

	@RequestMapping(value = "cancelUser", method = RequestMethod.POST)
	@ResponseBody
	public String cancel(@ModelAttribute USER_PROFILE_MOD_ENTITY userprofile, Model md, HttpServletRequest rq) {
		String inputUser = (String) rq.getSession().getAttribute("USERID");
		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));
		md.addAttribute("flagchange", loginServices.cancelUserentity(userprofile, inputUser, USERNAME));
		String msg = loginServices.cancel(userprofile.getUserid());
		// System.out.println(msg);
		return msg;
	}

	@RequestMapping(value = "passwordReset", method = RequestMethod.POST)
	@ResponseBody
	public String passwordReset(@ModelAttribute USER_PROFILE_ENTITY userprofile, Model md, HttpServletRequest rq) {
		String userid = (String) rq.getSession().getAttribute("USERID");
		String msg = loginServices.passwordReset(userprofile, userid);

		return msg;

	}

	@RequestMapping(value = "changePassword", method = RequestMethod.POST)
	@ResponseBody
	public String changePassword(@RequestParam("oldpass") String oldpass, @RequestParam("newpass") String newpass,
			Model md, HttpServletRequest rq) {
		String userid = (String) rq.getSession().getAttribute("USERID");
		String msg = loginServices.changePassword(oldpass, newpass, userid);

		return msg;

	}

	@RequestMapping(value = "userLogs/Download", method = RequestMethod.GET)
	@ResponseBody
	public InputStreamResource UserDownload(HttpServletResponse response, @RequestParam String fromdate,
			@RequestParam String todate) throws IOException, SQLException {
		response.setContentType("application/octet-stream");

		InputStreamResource resource = null;

		try {
			Date fromdate2 = new SimpleDateFormat("dd-MM-yyyy").parse(fromdate);
			Date todate2 = new SimpleDateFormat("dd-MM-yyyy").parse(todate);
			File repfile = loginServices.getUserLogFile(fromdate2, todate2);
			response.setHeader("Content-Disposition", "attachment; filename=" + repfile.getName());
			resource = new InputStreamResource(new FileInputStream(repfile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resource;
	}

	@RequestMapping(value = "auditLogs/Download", method = RequestMethod.GET)
	@ResponseBody
	public InputStreamResource auditDownload(HttpServletResponse response, @RequestParam String fromdate,
			@RequestParam String todate) throws IOException, SQLException {
		response.setContentType("application/octet-stream");

		InputStreamResource resource = null;

		try {
			Date fromdate2 = new SimpleDateFormat("dd-MM-yyyy").parse(fromdate);
			Date todate2 = new SimpleDateFormat("dd-MM-yyyy").parse(todate);
			File repfile = reportServices.getAuditLogFile(fromdate2, todate2);
			response.setHeader("Content-Disposition", "attachment; filename=" + repfile.getName());
			resource = new InputStreamResource(new FileInputStream(repfile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resource;
	}

	@RequestMapping(value = "logoutUpdate", method = RequestMethod.POST)
	@ResponseBody
	public String logoutUpdate(HttpServletRequest req) {

		String msg;

		String userid = (String) req.getSession().getAttribute("USERID");

		try {
			logger.info("Updating Logout");
			loginServices.SessionLogging("LOGOUT", "M0", req.getSession().getId(), userid, req.getRemoteAddr(),
					"IN-ACTIVE");
			msg = "success";
		} catch (Exception e) {
			e.printStackTrace();
			msg = "failed";
		}
		return msg;
	}
	// AUDIT METHOD

	@RequestMapping(value = "User_Audit", method = { RequestMethod.GET, RequestMethod.POST })
	public String USERACTIVITIES(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String Fromdate, @RequestParam(required = false) String Todate,
			@RequestParam(required = false) String ListFlg, Model md, HttpServletRequest req) throws ParseException {

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));
		md.addAttribute("PdfViewer", "UserAudit");
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
		final Calendar cal = Calendar.getInstance();
		String currentDate = dateFormat.format(cal.getTime());
		String currentFormattedDate = dateFormat1.format(cal.getTime());

		if (ListFlg != null && ListFlg.equals("Y")) {
			try {
				Date fromDateParsed = dateFormat.parse(Fromdate);
				String formattedFromDate = dateFormat1.format(fromDateParsed);
				md.addAttribute("Fromdate", Fromdate);
				md.addAttribute("AuditList", AuidtConfigure.getauditListLocal(dateFormat1.parse(formattedFromDate)));
			} catch (ParseException e) {
				md.addAttribute("error", "Invalid Fromdate format. Please use dd/MM/yyyy.");
				return "IPSAudit";
			}
		} else {
			md.addAttribute("Fromdate", currentDate);
			md.addAttribute("AuditList", AuidtConfigure.getauditListLocal(dateFormat1.parse(currentFormattedDate)));
		}
		md.addAttribute("menuname", "User Activity Audits");
		md.addAttribute("auditflag", "auditflag");
		md.addAttribute("formmode", "list1");
		return "User_Audit";
	}

	@RequestMapping(value = "Audits", method = { RequestMethod.GET, RequestMethod.POST })
	public String serviceAudit(@RequestParam(required = false) String Fromdate,
			@RequestParam(required = false) String userid, @RequestParam(required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req,
			@RequestParam(required = false) String ListFlg) throws ParseException {
		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));
		md.addAttribute("PdfViewer", "ServiceAudit");

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yyyy");
		final Calendar cal = Calendar.getInstance();
		String currentDate = dateFormat.format(cal.getTime());
		String currentFormattedDate = dateFormat1.format(cal.getTime());

		if (ListFlg != null && ListFlg.equals("Y")) {
			try {
				Date fromDateParsed = dateFormat.parse(Fromdate);
				String formattedFromDate = dateFormat1.format(fromDateParsed);
				md.addAttribute("Fromdate", Fromdate);
				md.addAttribute("AuditList", AuidtConfigure.getAuditInquries(dateFormat1.parse(formattedFromDate)));
			} catch (ParseException e) {
				md.addAttribute("error", "Invalid Fromdate format. Please use dd/MM/yyyy.");
				return "IPSAudit";
			}
		} else {
			md.addAttribute("Fromdate", currentDate);
			md.addAttribute("AuditList", AuidtConfigure.getAuditInquries(dateFormat1.parse(currentFormattedDate)));
		}

// System.out.println(date + date1);
		md.addAttribute("menuname", "Service Audits");
		md.addAttribute("auditflag", "auditflag");
		md.addAttribute("formmode", "list");
// md.addAttribute("Todate", date);
		return "Audits";
	}

	@GetMapping("getRoleDetails")
	@ResponseBody
	public ACCESS_AND_ROLES_ENTITY getRoleDetails(@RequestParam String roleId) {
		System.out.println("role id for fetching is : " + roleId);
		return accessandrolesrepository.findById(roleId).orElse(null);
	}

	@RequestMapping(value = "AccessandRoles", method = { RequestMethod.GET, RequestMethod.POST })
	public String IPSAccessandRoles(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, @RequestParam(required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size, Model md, HttpServletRequest req) {

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		String userid1 = (String) req.getSession().getAttribute("USERID");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));

		System.out.println("emp_id" + userid1);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("menu", "ACCESS AND ROLES");
			md.addAttribute("menuname", "ACCESS AND ROLES");
			md.addAttribute("formmode", "list");
			List<String> entryUsers = aCCESS_AND_ROLES_TEMP_REPO.getEntryUser();
			md.addAttribute("entryuser", entryUsers);
			md.addAttribute("emp_id", userid1);

			// Fetch only non-deleted TEMP roles
			List<ACCESS_AND_ROLES_TEMP_ENTITY> tempRoles = aCCESS_AND_ROLES_TEMP_REPO.findByDelFlgNot("Y");

			// Fetch only non-deleted ENTITY roles
			List<ACCESS_AND_ROLES_ENTITY> mainRoles = accessandrolesrepository.findByDelFlgNot("Y");

			// Wrap into DTO
			AccessAndRolesDTO dto = new AccessAndRolesDTO(tempRoles, mainRoles);

			// Pass to UI
			md.addAttribute("AccessandRoles", dto);
		} else if (formmode.equals("add")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - ADD");
			md.addAttribute("formmode", "add");
		} else if (formmode.equals("edit")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - EDIT");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleIdedit(userid));
		} else if (formmode.equals("view")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - INQUIRY");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleIdView(userid));

		} else if (formmode.equals("verify")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - VERIFY");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleId(userid));

		} else if (formmode.equals("delete")) {
			md.addAttribute("menuname", "ACCESS AND ROLES - DELETE");
			md.addAttribute("formmode", formmode);
			md.addAttribute("IPSAccessRole", AccessRoleService.getRoleId(userid));
		}

		md.addAttribute("adminflag", "adminflag");
		md.addAttribute("userprofileflag", "userprofileflag");

		return "AccessandRoles";
	}

	@RequestMapping(value = "createAccessRole", method = RequestMethod.POST)
	@ResponseBody
	public String createAccessRoleEn(@RequestParam("formmode") String formmode,
			@RequestParam(value = "adminValue", required = false) String adminValue,
			@RequestParam(value = "BRF_ReportsValue", required = false) String BRF_ReportsValue,
			@RequestParam(value = "Basel_ReportsValue", required = false) String Basel_ReportsValue,
			@RequestParam(value = "ArchivalValue", required = false) String ArchivalValue,
			@RequestParam(value = "Audit_InquiriesValue", required = false) String Audit_InquiriesValue,
			@RequestParam(value = "RBR_ReportsValue", required = false) String RBR_ReportsValue,
			@RequestParam(value = "VAT_LedgerValue", required = false) String VAT_LedgerValue,
			@RequestParam(value = "Invoice_DataValue", required = false) String Invoice_DataValue,
			@RequestParam(value = "finalString", required = false) String finalString,

			@ModelAttribute ACCESS_AND_ROLES_TEMP_ENTITY alertparam, Model md, HttpServletRequest rq) {

		String userid = (String) rq.getSession().getAttribute("USERID");
		String roleId = (String) rq.getSession().getAttribute("ROLEID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));

		String msg = AccessRoleService.addPARAMETER(alertparam, formmode, adminValue, BRF_ReportsValue,
				Basel_ReportsValue, ArchivalValue, Audit_InquiriesValue, RBR_ReportsValue, VAT_LedgerValue,
				Invoice_DataValue, finalString, userid, USERNAME);

		return msg;

	}

	// REFERENCE CODE
	@RequestMapping(value = "refcode", method = { RequestMethod.GET, RequestMethod.POST })
	public String refcode(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String ref_id, Model md, HttpServletRequest req) throws SQLException {
		String roleId = (String) req.getSession().getAttribute("ROLEID");
		md.addAttribute("IPSRoleMenu", AccessRoleService.getRoleMenu(roleId));
		md.addAttribute("PdfViewer", "ReferenceCode");

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("reflist", referenceCodeRep.getreflist());

		} else if (formmode.equals("view")) {

			md.addAttribute("formmode", "view");
			md.addAttribute("RefCodeMaster", referenceCodeRep.getrefview(ref_id));

		} else if (formmode.equals("modify")) {

			md.addAttribute("formmode", "modify");
			md.addAttribute("RefCodeMaster", referenceCodeRep.getrefview(ref_id));
			md.addAttribute("refTypeList", referenceCodeRep.getreflist());

		} else if (formmode.equals("edit")) {

			md.addAttribute("formmode", "edit");

		} else if (formmode.equals("add")) {

			md.addAttribute("formmode", "add");

		} else if (formmode.equals("deleteList")) {

			md.addAttribute("formmode", "deleteList");
		} else if (formmode.equals("delete")) {

			md.addAttribute("formmode", "delete");
		}

		return "ReferenceCode";
	}

	@RequestMapping(value = "refcodeadd", method = RequestMethod.POST)
	@ResponseBody
	public String adminRefCodeAdd(@RequestParam("formmode") String formmode, @RequestParam("ref_id") String refId,
			@ModelAttribute REFERENCE_CODE_ENTITY referenceCodeEntity) {

		referenceCodeEntity.setRef_id(refId); // ✅ explicitly set ref_id
		referenceCodeEntity.setEntity_flg("Y");
		referenceCodeEntity.setDel_flg("N");
		referenceCodeEntity.setModify_flg("N");

		referenceCodeRep.save(referenceCodeEntity);
		return "Added Successfully";
	}

	@RequestMapping(value = "/adminRefCodeMasterAdd", method = RequestMethod.POST)
	@ResponseBody
	public String adminRefCodeMasterAdd(@RequestParam("formmode") String formmode, @RequestParam("ref_id") String refId,
			@ModelAttribute REFERENCE_CODE_ENTITY referenceCodeEntity, Model md, HttpServletRequest rq) {

		referenceCodeEntity.setEntity_flg("Y");
		referenceCodeEntity.setDel_flg("N");
		referenceCodeEntity.setModify_flg("Y");

		referenceCodeRep.save(referenceCodeEntity);

		return "Update saved!";
	}

	@RequestMapping(value = "/refcodedelete", method = RequestMethod.POST)
	@ResponseBody
	public String refcodedelete(@RequestParam(required = false) String refId, Model md, HttpServletRequest rq) {

		REFERENCE_CODE_ENTITY up = referenceCodeRep.getrefview(refId);
		referenceCodeRep.delete(up);
		return "Deleted successfully";
	}

	@RequestMapping(value = "ModBankBranchMaster", method = RequestMethod.POST)
	@ResponseBody
	public String ModBankBranchMaster(@RequestParam("formmode") String formmode,
			@ModelAttribute BANK_AND_BRANCH_BEAN bamlSolEntity, Model md, HttpServletRequest rq) {
		String userID = (String) rq.getSession().getAttribute("USERID");
		String userName = (String) rq.getSession().getAttribute("USERNAME");

		String msg = bankandBranchServices.modDetails(bamlSolEntity, formmode, userID, userName);
		md.addAttribute("adminflag", "adminflag");
		return msg;
	}

//	FILE UPLOAD SCREEN STARTS
	@RequestMapping(value = "Credit_Upload", method = { RequestMethod.GET, RequestMethod.POST })
	public String Credit_Upload(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md, @RequestParam(required = false) String arn) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list"); // to set which form - valid values are "edit" , "add" & "list"
		} else if (formmode.equals("listsource")) {
			md.addAttribute("formmode", "listsource");
			md.addAttribute("source", RECON_CRFILE_SOURCE_REPO.getsource());
		} else if (formmode.equals("listdest")) {
			md.addAttribute("formmode", "listdest");
			md.addAttribute("dest", RECON_CRFILE_DESTINATION_REPO.getdest());
		} else if (formmode.equals("viewsour")) {
			md.addAttribute("formmode", "viewsour");
			md.addAttribute("source", RECON_CRFILE_SOURCE_REPO.getarn(arn));
		} else if (formmode.equals("viewdest")) {
			md.addAttribute("formmode", "viewdest");
			md.addAttribute("dest", RECON_CRFILE_DESTINATION_REPO.getArn_N(arn));
		}
		return "CreditCardUpload";
	}

	@RequestMapping(value = "Debit_Upload", method = { RequestMethod.GET, RequestMethod.POST })
	public String Debit_Upload(@RequestParam(required = false) String formmode, String ref,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list"); // to set which form - valid values are "edit" , "add" & "list"
		} else if (formmode.equals("listsource")) {
			md.addAttribute("formmode", "listsource");
			md.addAttribute("source", RECON_DRFILE_SOURCE_REPO.getsource());
		} else if (formmode.equals("listdest")) {
			md.addAttribute("formmode", "listdest");
			md.addAttribute("dest", BRECON_DRFILE_DESTINATION_REPO.getdest());
		} else if (formmode.equals("viewsour")) {
			md.addAttribute("formmode", "viewsour");
			md.addAttribute("source", RECON_DRFILE_SOURCE_REPO.getref(ref));
		} else if (formmode.equals("viewdest")) {
			md.addAttribute("formmode", "viewdest");
			md.addAttribute("dest", BRECON_DRFILE_DESTINATION_REPO.getref(ref));
		}
		return "DebitCardUpload";
	}

	@RequestMapping(value = "Upi_Debit_Upload", method = { RequestMethod.GET, RequestMethod.POST })
	public String Upi_Debit_Upload(@RequestParam(required = false) String formmode, String ref,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list"); // to set which form - valid values are "edit" , "add" & "list"
		} else if (formmode.equals("listsource")) {
			md.addAttribute("formmode", "listsource");
			md.addAttribute("source", RECON_UPI_SOURCE_REPO.getsource());
		} else if (formmode.equals("listdest")) {
			md.addAttribute("formmode", "listdest");
			md.addAttribute("dest", RECON_UPI_DESTINATION_REPO.getdest());
		} else if (formmode.equals("viewsour")) {
			md.addAttribute("formmode", "viewsour");
			md.addAttribute("source", RECON_UPI_SOURCE_REPO.getref(ref));
		} else if (formmode.equals("viewdest")) {
			md.addAttribute("formmode", "viewdest");
			md.addAttribute("dest", RECON_UPI_DESTINATION_REPO.getref(ref));
		}
		return "UpiDebitUpload";
	}

	@RequestMapping(value = "Parameter", method = { RequestMethod.GET, RequestMethod.POST })
	public String Parameter(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("list", recon_Parameter_Repo.getList());

		}
		return "Parameter";
	}

	@RequestMapping(value = "Account", method = { RequestMethod.GET, RequestMethod.POST })
	public String Account(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("list", recon_Act_Repo.getList());

		}
		return "RECONAccount";
	}

	@RequestMapping(value = "AccountMaster", method = { RequestMethod.GET, RequestMethod.POST })
	public String AccountMaster(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String no, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("list", recon_Act_Mst_Repo.getList());

		} else if (formmode.equals("add")) {
			md.addAttribute("formmode", "add");
			md.addAttribute("reconForm", new RECON_ACT_MST_ENTITY());

		} else if (formmode.equals("edit")) {
			md.addAttribute("formmode", "edit");
			md.addAttribute("reconForm", recon_Act_Mst_Repo.getNo(no));

		} else if (formmode.equals("view")) {
			md.addAttribute("formmode", "view");
			md.addAttribute("acct", recon_Act_Mst_Repo.getNo(no));

		} else if (formmode.equals("verify")) {
			md.addAttribute("formmode", "verify");
			md.addAttribute("acct", recon_Act_Mst_Repo.getNo(no));

		}
		return "RECONActMaster";
	}

	@RequestMapping(value = "Rule_engine", method = { RequestMethod.GET, RequestMethod.POST })
	public String Rule_engine(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md, @RequestParam(required = false) String rule,
			@RequestParam(required = false) String subrule, HttpServletRequest rq) {

		String userID = (String) rq.getSession().getAttribute("USERID");
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("list", Rate_Engine_Repo.getRuleList());
			List<RATE_ENGINE_ENTITY> existingMerchants = Rate_Engine_Repo.getSrlNo();
			int maxSeq = 0;
			for (RATE_ENGINE_ENTITY srl : existingMerchants) {
				if (srl == null) {
					System.out.println("Found null entity in list!");
					continue;
				}

				String ruleCode = srl.getRule_code();
				if (ruleCode == null) {
					System.out.println("Found entity with null rule_code: " + srl);
					continue;
				}

				if (ruleCode.startsWith("R")) {
					try {
						int seq = Integer.parseInt(ruleCode.substring(1));
						if (seq > maxSeq) {
							maxSeq = seq;
						}
					} catch (NumberFormatException e) {
						System.out.println("Invalid rule_code format: " + ruleCode);
					}
				}
			}

			int newSeq = maxSeq + 1;
			String formattedSeq = String.format("%04d", maxSeq);
			String Seq = "R" + formattedSeq;
			md.addAttribute("RuleSeq", Seq);
			// for sub rule
			// md.addAttribute("subrule", Rate_Engine_Repo.getSubrule(rule));
			List<RATE_ENGINE_ENTITY> sub = Rate_Engine_Repo.getSubrule(rule);
			int maxSeq1 = 0;
			for (RATE_ENGINE_ENTITY srl : sub) {
				if (srl == null) {
					System.out.println("Found null entity in list!");
					continue;
				}

				String subcode = srl.getVersion();
				if (subcode == null) {
					System.out.println("Found entity with null rule_code: " + srl);
					continue;
				}
				try {
					int seq = Integer.parseInt(subcode);
					if (seq > maxSeq1) {
						maxSeq1 = seq;
					}
				} catch (NumberFormatException e) {
					System.out.println("Invalid rule_code format: " + subcode);
				}
			}
			md.addAttribute("SubRule", maxSeq1);
			md.addAttribute("userID", userID);

		}
		return "RuleEngine";
	}

	@RequestMapping(value = "Follow_up", method = { RequestMethod.GET, RequestMethod.POST })
	public String Follow_up(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("followlist", follow_Up_Repo.getList());
		} else if (formmode.equals("view")) {
			md.addAttribute("formmode", "view");
		}
		return "FollowUp";
	}

	@RequestMapping(value = "Recon_oper", method = { RequestMethod.GET, RequestMethod.POST })
	public String Recon_oper(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		} else if (formmode.equals("operation")) {
			md.addAttribute("formmode", "operation");
		} else if (formmode.equals("process")) {
			md.addAttribute("formmode", "process");
		} else if (formmode.equals("status")) {
			md.addAttribute("formmode", "status");
		}
		return "ReconOper";
	}

	@RequestMapping(value = "Recon_rep", method = { RequestMethod.GET, RequestMethod.POST })
	public String Recon_rep(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
		}
		return "ReconRep";
	}

	@RequestMapping(value = "AccountMasterAdd", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String AccountMasterAdd(@ModelAttribute RECON_ACT_MST_ENTITY Entity, HttpServletRequest req,
			@RequestParam(required = false) String formmode) {
		System.out.println("768");
		String user = (String) req.getSession().getAttribute("USERID");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");
		String audit_ref_no = sequence.generateRequestUUId();

		if (formmode.equalsIgnoreCase("add")) {
			RECON_ACT_MST_ENTITY up = Entity;
			up.setDel_flg("N");
			up.setEntity_flg("N");
			up.setModify_flg("N");
			up.setEntry_user(user);
			up.setEntry_time(new Date());

			// AUDIT

			BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
			audit.setAudit_date(new Date());
			audit.setEntry_time(new Date());
			audit.setEntry_user(user);
			audit.setFunc_code("ADD");
			audit.setRemarks("New entry saved!");
			audit.setAudit_table("RECON_ACCOUNT_MASTER_TABLE");
			audit.setAudit_screen("ACCOUNT MASTER");
			audit.setEvent_id(user);
			audit.setEvent_name(USERNAME);
			audit.setModi_details("-");
			audit.setAudit_ref_no(audit_ref_no);
			BRECON_Audit_Rep.save(audit);

			recon_Act_Mst_Repo.save(up);
			return "New entry saved!";
		} else {
			RECON_ACT_MST_ENTITY up = Entity;
			up.setDel_flg("N");
			up.setEntity_flg("N");
			up.setModify_flg("Y");
			up.setModify_user(user);
			up.setModify_time(new Date());

			BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
			audit.setAudit_date(new Date());
			audit.setEntry_time(new Date());
			audit.setEntry_user(user);
			audit.setFunc_code("EDIT");
			audit.setRemarks("Update saved!");
			audit.setAudit_table("RECON_ACCOUNT_MASTER_TABLE");
			audit.setAudit_screen("ACCOUNT MASTER");
			audit.setEvent_id(user);
			audit.setEvent_name(USERNAME);
			audit.setModi_details("-");
			audit.setAudit_ref_no(audit_ref_no);
			BRECON_Audit_Rep.save(audit);

			recon_Act_Mst_Repo.save(up);
			return "Update saved!";
		}
	}

	@RequestMapping(value = "AccountMasterVerify", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String AccountMasterVerify(@ModelAttribute RECON_ACT_MST_ENTITY Entity, HttpServletRequest req,
			@RequestParam(required = false) String no) {
		String user = (String) req.getSession().getAttribute("USERID");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");
		String audit_ref_no = sequence.generateRequestUUId();

		RECON_ACT_MST_ENTITY up = recon_Act_Mst_Repo.getNo(no);
		up.setDel_flg("N");
		up.setEntity_flg("Y");
		up.setModify_flg("N");
		up.setVerify_user(user);
		up.setVerify_time(new Date());

		BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		audit.setAudit_date(new Date());
		audit.setEntry_time(new Date());
		audit.setEntry_user(user);
		audit.setFunc_code("VERIFY");
		audit.setRemarks("Update saved!");
		audit.setAudit_table("RECON_ACCOUNT_MASTER_TABLE");
		audit.setAudit_screen("ACCOUNT MASTER");
		audit.setEvent_id(user);
		audit.setEvent_name(USERNAME);
		audit.setModi_details("-");
		audit.setAudit_ref_no(audit_ref_no);
		BRECON_Audit_Rep.save(audit);

		recon_Act_Mst_Repo.save(up);
		return "Verification complete!.";
	}

	@RequestMapping(value = "AccountMasterDelete", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String AccountMasterDelete(@ModelAttribute RECON_ACT_MST_ENTITY Entity, HttpServletRequest req,
			@RequestParam(required = false) String no) {
		String user = (String) req.getSession().getAttribute("USERID");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");
		String audit_ref_no = sequence.generateRequestUUId();

		RECON_ACT_MST_ENTITY up = recon_Act_Mst_Repo.getNo(no);
		up.setDel_flg("Y");

		BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		audit.setAudit_date(new Date());
		audit.setEntry_time(new Date());
		audit.setEntry_user(user);
		audit.setFunc_code("DELETE");
		audit.setRemarks("Deletion complete!.");
		audit.setAudit_table("RECON_ACCOUNT_MASTER_TABLE");
		audit.setAudit_screen("ACCOUNT MASTER");
		audit.setEvent_id(user);
		audit.setEvent_name(USERNAME);
		audit.setModi_details("-");
		audit.setAudit_ref_no(audit_ref_no);
		BRECON_Audit_Rep.save(audit);

		recon_Act_Mst_Repo.save(up);
		return "Deletion complete!.";
	}

	@RequestMapping(value = "Rate_main", method = { RequestMethod.GET, RequestMethod.POST })
	public String Rate_main(@RequestParam(required = false) String formmode, HttpServletRequest req,
			@RequestParam(required = false) String userid, Model md) {
		String user = (String) req.getSession().getAttribute("USERID");
		System.out.println(user);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("RateList", rate_main_Repo.getList());
			md.addAttribute("loginuser", user);
		}
		return "Ratemain";
	}

	@RequestMapping(value = "CrRecon_pros", method = { RequestMethod.GET, RequestMethod.POST })
	public String CrRecon_pros(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("Recon", RECON_CRMAIN_REPO.getlist());
			md.addAttribute("SourceList", RECON_CRFILE_SOURCE_REPO.getDATE());
			md.addAttribute("DestinList", RECON_CRFILE_DESTINATION_REPO.getlist());
		}
		return "CrReconProcess";
	}

	@RequestMapping(value = "CRReconProcess", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String CRReconProcess(HttpServletRequest req, @RequestParam(required = false) String arn,
			HttpServletRequest rq) {
		System.out.println(arn);

		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		RECON_CRFILE_SOURCE_ENTITY up = RECON_CRFILE_SOURCE_REPO.getarn(arn);
		up.setRecon_flg("Y");
		up.setRecon_type("PARTIAL");
		RECON_CRFILE_SOURCE_REPO.save(up);

		RECON_CRFILE_DESTINATION_ENTITY up1 = RECON_CRFILE_DESTINATION_REPO.getarn(arn);
		up1.setRecon_flg("Y");
		up1.setRecon_type("PARTIAL");
		RECON_CRFILE_DESTINATION_REPO.save(up1);

		RECON_CRMAIN_REPO.insertReconArn(arn, user);
		audit.insertAudit(user, USERNAME, "MANUAL RECON PROCESS", "DATA MATCHED SUCCESSFULLY",
				"BRECON_CRFILE_SOURCE_TABLE,BRECON_CRFILE_DESTINATION_TABLE", "RECON PROCESS");
		return "Data synced successfully!.";
	}

	@RequestMapping(value = "DRReconProcess", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String DRReconProcess(@RequestParam(required = false) String arn, HttpServletRequest rq) {
		System.out.println(arn);

		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		RECON_DRFILE_SOURCE_ENTITY up = RECON_DRFILE_SOURCE_REPO.getref(arn);
		up.setRecon_flg("Y");
		up.setRecon_type("PARTIAL");
		RECON_DRFILE_SOURCE_REPO.save(up);

		RECON_DRFILE_DESTINATION_ENTITY up1 = BRECON_DRFILE_DESTINATION_REPO.getref(arn);
		up1.setRecon_flg("Y");
		up1.setRecon_type("PARTIAL");
		BRECON_DRFILE_DESTINATION_REPO.save(up1);

		RECON_DRMAIN_REPO.insertReconDr(arn, user);
		audit.insertAudit(user, USERNAME, "MANUAL RECON PROCESS", "DATA MATCHED SUCCESSFULLY",
				"BRECON_DRFILE_SOURCE_TABLE,BRECON_DRFILE_DESTINATION_TABLE", "RECON PROCESS");
		return "Data synced successfully!.";
	}

	@PostMapping("DRReconSourceOnly")
	@ResponseBody
	public String reconSourceOnly(@RequestParam String arn, HttpServletRequest rq, String fromDate) {

		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");

		RECON_DRFILE_SOURCE_ENTITY src = RECON_DRFILE_SOURCE_REPO.getref(arn);
		src.setRecon_flg("Y");
		src.setRecon_type("PARTIAL");
		RECON_DRFILE_SOURCE_REPO.save(src);
		audit.insertAudit(user, USERNAME, "MANUAL RECON PROCESS", "RECON FLAG UPDATED SUCCESSFULLY",
				"BRECON_DRFILE_SOURCE_TABLE", "RECON PROCESS");

		return "Source updated successfully";
	}

	@PostMapping("DRReconDestOnly")
	@ResponseBody
	public String reconDestOnly(@RequestParam String arn, HttpServletRequest rq, String fromDate) {

		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");

		RECON_DRFILE_DESTINATION_ENTITY dest = BRECON_DRFILE_DESTINATION_REPO.getrefBydate(arn, fromDate);
		dest.setRecon_flg("Y");
		dest.setRecon_type("PARTIAL");
		BRECON_DRFILE_DESTINATION_REPO.save(dest);

		RECON_DRMAIN_REPO.insertReconDrFromDest(arn, user);
		audit.insertAudit(user, USERNAME, "MANUAL RECON PROCESS", "RECON FLAG UPDATED SUCCESSFULLY",
				"BRECON_DRFILE_DESTINATION_TABLE", "RECON PROCESS");
		BRECON_DRFILE_DESTINATION_REPO.runSourcePro(fromDate, "IB");
		return "Destination updated successfully";
	}

	// 1️⃣ Both Source & Destination selected
	@RequestMapping(value = "UPIReconProcess", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String UPIReconProcess(HttpServletRequest req, @RequestParam(required = false) String ref,
			HttpServletRequest rq) {
		System.out.println(ref);
		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		// Update Source
		RECON_UPI_SOURCE_ENTITY src = RECON_UPI_SOURCE_REPO.getref(ref);
		src.setRecon_flg("Y");
		src.setRecon_type("PARTIAL");
		RECON_UPI_SOURCE_REPO.save(src);

		// Update Destination
		RECON_UPI_DESTINATION_ENTITY dest = RECON_UPI_DESTINATION_REPO.getref(ref);
		dest.setRecon_flg("Y");
		dest.setRecon_type("PARTIAL");
		RECON_UPI_DESTINATION_REPO.save(dest);

		// Insert into main table
		RECON_UPIMAIN_REPO.insertReconRef(ref, user);
		audit.insertAudit(user, USERNAME, "MANUAL RECON PROCESS", "DATA MATCHED SUCCESSFULLY",
				"BRECON_UPI_SOURCE_TABLE,BRECON_UPI_DESTINATION_TABLE", "RECON PROCESS");

		return "Data synced successfully!";
	}

	// 2️⃣ Only Source selected
	@PostMapping("UPIReconSourceOnly")
	@ResponseBody
	public String UPIReconSourceOnly(@RequestParam String ref, HttpServletRequest rq, String fromDate) {
		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		RECON_UPI_SOURCE_ENTITY src = RECON_UPI_SOURCE_REPO.getref(ref);
		src.setRecon_flg("Y");
		src.setRecon_type("PARTIAL");
		RECON_UPI_SOURCE_REPO.save(src);
		audit.insertAudit(user, USERNAME, "MANUAL RECON PROCESS", "RECON FLAG UPDATED SUCCESSFULLY",
				"BRECON_UPI_SOURCE_TABLE", "RECON PROCESS");

		return "Source updated successfully";
	}

	// 3️⃣ Only Destination selected
	@PostMapping("UPIReconDestOnly")
	@ResponseBody
	public String UPIReconDestOnly(@RequestParam String ref, HttpServletRequest rq, String fromDate) {
		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");

		RECON_UPI_DESTINATION_ENTITY dest = RECON_UPI_DESTINATION_REPO.getref(ref);
		dest.setRecon_flg("Y");
		dest.setRecon_type("PARTIAL");
		RECON_UPI_DESTINATION_REPO.save(dest);

		RECON_UPIMAIN_REPO.insertReconRefFromDest(ref, user);
		audit.insertAudit(user, USERNAME, "MANUAL RECON PROCESS", "RECON FLAG UPDATED SUCCESSFULLY",
				"BRECON_UPI_DESTINATION_TABLE", "RECON PROCESS");
		RECON_UPI_DESTINATION_REPO.runSourcePro(fromDate);
		return "Destination updated successfully";
	}

	@RequestMapping(value = "CRRefershProcess", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String CRRefershProcess(HttpServletRequest req, @RequestParam(required = false) String arn,
			HttpServletRequest rq) {
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		String user = (String) rq.getSession().getAttribute("USERID");

		crUploadService.CrRefershMethod(user, USERNAME);
		return "Data synced successfully!.";
	}

	@RequestMapping(value = "DRRefershProcess", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String DRRefershProcess(HttpServletRequest req, @RequestParam(required = false) String arn,
			HttpServletRequest rq) {
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		String user = (String) rq.getSession().getAttribute("USERID");

		DrUploadService.DrRefershMethod(user, USERNAME);
		return "Data synced successfully!.";
	}

	@RequestMapping(value = "UPIRefershProcess", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String UPIRefershProcess(HttpServletRequest req, @RequestParam(required = false) String arn,
			HttpServletRequest rq) {
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		String user = (String) rq.getSession().getAttribute("USERID");

		UpiUploadService.UpiRefershMethod(user, USERNAME);
		return "Data synced successfully!.";
	}

	@RequestMapping(value = "CRCheckARN", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String CRCheckARN(HttpServletRequest req, @RequestParam(required = false) String arn,
			HttpServletRequest rq) {
		System.out.println("CHECK ARN NO");

		List<Object[]> a = RECON_CRFILE_DESTINATION_REPO.getCheckARN();

		if (a == null || a.isEmpty()) {
			return "Noreturn"; // no records found
		} else {
			for (Object[] check_arn : a) {
				System.out.println(check_arn[0]);
				String val = check_arn[0].toString();
			}
			return "Return";
		}
	}

	@RequestMapping(value = "DRCheckREF", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String DRCheckREF(HttpServletRequest req, @RequestParam(required = false) String arn,
			HttpServletRequest rq) {
		System.out.println("CHECK REFNUM NO");

		List<Object[]> a = BRECON_DRFILE_DESTINATION_REPO.getCheckREF();

		if (a == null || a.isEmpty()) {
			return "Noreturn"; // no records found
		} else {
			for (Object[] check_ref : a) {
				System.out.println(check_ref[0]);
				String val = check_ref[0].toString();
			}
			return "Return";
		}
	}

	@RequestMapping(value = "UPICheckREF", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String DRCheckARN(HttpServletRequest req, @RequestParam(required = false) String arn,
			HttpServletRequest rq) {
		System.out.println("CHECK REFNUM NO");

		List<Object[]> a = RECON_UPI_DESTINATION_REPO.getCheckREF();

		if (a == null || a.isEmpty()) {
			return "Noreturn"; // no records found
		} else {
			for (Object[] check_ref : a) {
				System.out.println(check_ref[0]);
				String val = check_ref[0].toString();
			}
			return "Return";
		}
	}

	@RequestMapping(value = "DrRecon_pros", method = { RequestMethod.GET, RequestMethod.POST })
	public String DrRecon_pros(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("Recon", RECON_DRMAIN_REPO.getlist());
			md.addAttribute("SourceList", RECON_DRFILE_SOURCE_REPO.getlist());
			md.addAttribute("DestinList", BRECON_DRFILE_DESTINATION_REPO.getlist());
		}
		return "DrReconProcess";
	}

	@RequestMapping(value = "UpiRecon_pros", method = { RequestMethod.GET, RequestMethod.POST })
	public String UpiRecon_pros(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("Recon", RECON_UPIMAIN_REPO.getlist());
			md.addAttribute("SourceList", RECON_UPI_SOURCE_REPO.getlist());
			md.addAttribute("DestinList", RECON_UPI_DESTINATION_REPO.getlist());
		}
		return "UPIReconProcess";
	}

	@PostMapping("/submitAllRates")
	@ResponseBody
	public ResponseEntity<?> saveRates(HttpServletRequest rq, @RequestBody List<RATE_MAINMOD_ENTITY> rates) {
		String user = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		try {
			rateMainService.saveRates(rates, user, USERNAME);
			return ResponseEntity.ok("Saved successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving rates");
		}
	}

	@PostMapping("/submitVersions")
	@ResponseBody
	public ResponseEntity<?> submitVersions(@RequestBody List<RATE_MAINMOD_ENTITY> versions,
			HttpServletRequest request) {
		String user = (String) request.getSession().getAttribute("USERID");
		String username = (String) request.getSession().getAttribute("USERNAME");
		try {
			rateMainService.submitVersions(versions, user, username);
			return ResponseEntity.ok("Versions saved successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving versions");
		}
	}

	@PostMapping("/submitLists")
	@ResponseBody
	public ResponseEntity<?> submitLists(@RequestBody List<RATE_MAINMOD_ENTITY> listEntries,
			HttpServletRequest request) {
		String user = (String) request.getSession().getAttribute("USERID");
		String username = (String) request.getSession().getAttribute("USERNAME");
		try {
			rateMainService.submitLists(listEntries, user, username);
			return ResponseEntity.ok("List entries saved successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving list entries");
		}
	}

	@PostMapping("/uploadRateExcel")
	@ResponseBody
	public ResponseEntity<?> uploadRateExcel(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite,
			HttpServletRequest request) {

		String user = (String) request.getSession().getAttribute("USERID");
		String username = (String) request.getSession().getAttribute("USERNAME");

		try {
			Map<String, Object> result = rateMainService.uploadRateExcel(file, user, username, overwrite);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, Object> error = new HashMap<>();
			error.put("status", "error");
			error.put("message", "Error during upload: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@PostMapping("/uploadRatePex")
	@ResponseBody
	public ResponseEntity<?> uploadRatePex(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite,
			HttpServletRequest request) {

		String user = (String) request.getSession().getAttribute("USERID");
		String username = (String) request.getSession().getAttribute("USERNAME");

		try {
			Map<String, Object> result = rateMainService.uploadRatePex(file, user, username, overwrite);

			String status = (String) result.get("status");

			if ("duplicate".equals(status)) {
				return ResponseEntity.ok(result);
			} else if ("success".equals(status)) {
				Map<String, Object> response = new HashMap<>();
				response.put("status", "success");
				response.put("message", "Uploaded and saved successfully");
				response.put("data", result.get("data"));
				return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.ok(result);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Map<String, Object> error = new HashMap<>();
			error.put("status", "error");
			error.put("message", "Error during upload: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@PostMapping("/verifyRate")
	@ResponseBody
	public ResponseEntity<String> verifyRate(HttpServletRequest request, @RequestParam String unique_id) {
		String loginUserId = (String) request.getSession().getAttribute("USERID");
		String username = (String) request.getSession().getAttribute("USERNAME");

		try {
			String result = rateMainService.verifyRate(unique_id, loginUserId, username);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during verification");
		}
	}

	@RequestMapping(value = "RuleAdd", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String RuleAdd(@RequestBody List<RATE_ENGINE_ENTITY> list, HttpServletRequest req,
			@RequestParam(required = false) String formmode) {

		String user = (String) req.getSession().getAttribute("USERID");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");
		String audit_ref_no = sequence.generateRequestUUId();

		List<RATE_ENGINE_ENTITY> val = new ArrayList<>();
		for (RATE_ENGINE_ENTITY dbEntity : list) {
			System.out.println(dbEntity.getRule_code());
			RATE_ENGINE_ENTITY rule = Rate_Engine_Repo.getUnique(dbEntity.getUnique_id());
			if (dbEntity.getUnique_id() == null) {
				if (dbEntity.getRule_code() == null) {

				} else {
					// ADD
					dbEntity.setRule_code(dbEntity.getRule_code());
					dbEntity.setRule_srl(dbEntity.getRule_srl());
					dbEntity.setDescription(dbEntity.getDescription());
					dbEntity.setMatch_type(dbEntity.getMatch_type());
					dbEntity.setInternal_ref(dbEntity.getInternal_ref());
					dbEntity.setExternal_ref(dbEntity.getExternal_ref());
					dbEntity.setValue_date(dbEntity.getValue_date()); // Already a Date object
					dbEntity.setAmount_type(dbEntity.getAmount_type());
					dbEntity.setTolerance(dbEntity.getTolerance());
					dbEntity.setMatch_status(dbEntity.getMatch_status());
					dbEntity.setAdjustment_entry(dbEntity.getAdjustment_entry());
					dbEntity.setMatching_sequence(dbEntity.getMatching_sequence());
					dbEntity.setRemarks(dbEntity.getRemarks());
					dbEntity.setDel_flg("N");
					dbEntity.setEntity_flg("Y");
					dbEntity.setModify_flg("N");
					dbEntity.setAuth_flg("N");
					dbEntity.setEntry_user(user);
					dbEntity.setEntry_time(new Date());
					dbEntity.setUnique_id(Rate_Engine_Repo.getSeq());

					// AUDIT

					BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
					audit.setAudit_date(new Date());
					audit.setEntry_time(new Date());
					audit.setEntry_user(user);
					audit.setFunc_code("ADD");
					audit.setRemarks("New entry saved!");
					audit.setAudit_table("RULE_ENGINE_TABLE");
					audit.setAudit_screen("RULE ENGINEE");
					audit.setEvent_id(user);
					audit.setEvent_name(USERNAME);
					audit.setModi_details("-");
					audit.setAudit_ref_no(audit_ref_no);
					BRECON_Audit_Rep.save(audit);

					val.add(dbEntity);
				}
			} else {
				// MODIFY

				rule.setRule_srl(dbEntity.getRule_srl());
				rule.setDescription(dbEntity.getDescription());
				rule.setMatch_type(dbEntity.getMatch_type());
				rule.setInternal_ref(dbEntity.getInternal_ref());
				rule.setExternal_ref(dbEntity.getExternal_ref());
				rule.setValue_date(dbEntity.getValue_date()); // Already a Date object
				rule.setAmount_type(dbEntity.getAmount_type());
				rule.setTolerance(dbEntity.getTolerance());
				rule.setMatch_status(dbEntity.getMatch_status());
				rule.setAdjustment_entry(dbEntity.getAdjustment_entry());
				rule.setMatching_sequence(dbEntity.getMatching_sequence());
				rule.setRemarks(dbEntity.getRemarks());
				rule.setDel_flg("N");
				rule.setModify_flg("Y");
				rule.setAuth_flg("N");
				rule.setModify_user(user);
				rule.setModify_time(new Date());

				BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
				audit.setAudit_date(new Date());
				audit.setEntry_time(new Date());
				audit.setEntry_user(user);
				audit.setFunc_code("ADD");
				audit.setRemarks("Update saved!");
				audit.setAudit_table("RULE_ENGINE_TABLE");
				audit.setAudit_screen("RULE ENGINEE");
				audit.setEvent_id(user);
				audit.setEvent_name(USERNAME);
				audit.setModi_details("-");
				audit.setAudit_ref_no(audit_ref_no);
				BRECON_Audit_Rep.save(audit);

				val.add(rule);
			}
		}
		Rate_Engine_Repo.saveAll(val);
		return "Rule successfully saved!";
	}

	@RequestMapping(value = "Report_Cr", method = { RequestMethod.GET, RequestMethod.POST })
	public String Report(@RequestParam(required = false) String formmode, HttpServletRequest req,
			@RequestParam(required = false) String userid, Model md, @RequestParam(required = false) String ReconDate,
			@RequestParam(required = false) String curr, @RequestParam(required = false) String type) {
		String user = (String) req.getSession().getAttribute("USERID");
		System.out.println(user);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("RateList", rate_main_Repo.getList());
			md.addAttribute("loginuser", user);
		} else if (formmode.equals("ReportUSD")) {
			md.addAttribute("formmode", "ReportUSD");
			RECON_CRREPORT_USD_ENTITY usd = RECON_CRREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", usd);
		} else if (formmode.equals("ReportMUR")) {
			md.addAttribute("formmode", "ReportMUR");
			RECON_CRREPORT_MUR_ENTITY mur = RECON_CRREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", mur);
		}
		return "CRReport";
	}

	@RequestMapping(value = "Report_UPI", method = { RequestMethod.GET, RequestMethod.POST })
	public String Report_UPI(@RequestParam(required = false) String formmode, HttpServletRequest req,
			@RequestParam(required = false) String userid, Model md, @RequestParam(required = false) String ReconDate,
			@RequestParam(required = false) String curr, @RequestParam(required = false) String type) {
		String user = (String) req.getSession().getAttribute("USERID");
		System.out.println(user);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("RateList", rate_main_Repo.getList());
			md.addAttribute("loginuser", user);
		} else if (formmode.equals("ReportUSD")) {
			md.addAttribute("formmode", "ReportUSD");
			RECON_UPIREPORT_USD_ENTITY usd = RECON_UPIREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", usd);

			RECON_TEMP_UPIREPORT_USD_ENTITY usd1 = RECON_TEMP_UPIREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", usd1);
		} else if (formmode.equals("ModiReportUSD")) {
			md.addAttribute("formmode", "ModiReportUSD");
			RECON_UPIREPORT_USD_ENTITY usd = RECON_UPIREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", usd);

			RECON_TEMP_UPIREPORT_USD_ENTITY usd1 = RECON_TEMP_UPIREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", usd1);
		} else if (formmode.equals("ModiReportMUR")) {
			md.addAttribute("formmode", "ModiReportMUR");
			RECON_UPIREPORT_MUR_ENTITY mur = RECON_UPIREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", mur);

			RECON_TEMP_UPIREPORT_MUR_ENTITY mur1 = RECON_TEMP_UPIREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", mur1);
		} else if (formmode.equals("ReportMUR")) {
			md.addAttribute("formmode", "ReportMUR");
			RECON_UPIREPORT_MUR_ENTITY mur = RECON_UPIREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", mur);

			RECON_TEMP_UPIREPORT_MUR_ENTITY mur1 = RECON_TEMP_UPIREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", mur1);
		}
		return "UPIReport";
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (text == null || text.trim().isEmpty()) {
					setValue(null);
					return;
				}
				try {
					// ✅ Handles standard date format yyyy-MM-dd
					setValue(new SimpleDateFormat("yyyy-MM-dd").parse(text));
				} catch (Exception e) {
					throw new IllegalArgumentException("Invalid date format: " + text, e);
				}
			}
		});
	}

	@RequestMapping(value = "modiNostroUpi", method = RequestMethod.POST)
	@ResponseBody
	public String modiNostroUpi(@RequestParam("formmode") String formmode, @RequestParam("currency") String currency,
			@ModelAttribute RECON_UPIREPORT_USD_ENTITY nostroUsd, @ModelAttribute RECON_UPIREPORT_MUR_ENTITY nostroMur,
			HttpServletRequest request) {

		String userId = (String) request.getSession().getAttribute("USERID");
		String userName = (String) request.getSession().getAttribute("USERNAME");

		try {
			if ("USD".equalsIgnoreCase(currency)) {
				return nostroService.editUpiUsd(nostroUsd, formmode, userId, userName);
			} else if ("MUR".equalsIgnoreCase(currency)) {
				return nostroService.editUpiMur(nostroMur, formmode, userId, userName);
			} else {
				return "Invalid currency specified!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while processing request.";
		}
	}

	@RequestMapping(value = "/verifyNostroUpi", method = RequestMethod.POST)
	@ResponseBody
	public String verifyNostroUpi(@RequestParam("currency") String currency,
			@ModelAttribute RECON_UPIREPORT_USD_ENTITY nostroUsd, @ModelAttribute RECON_UPIREPORT_MUR_ENTITY nostroMur,
			HttpServletRequest request) {

		String userId = (String) request.getSession().getAttribute("USERID");
		String userName = (String) request.getSession().getAttribute("USERNAME");

		try {
			if ("USD".equalsIgnoreCase(currency)) {
				return nostroService.verifyUpiUsd(nostroUsd, userId, userName);
			} else if ("MUR".equalsIgnoreCase(currency)) {
				return nostroService.verifyUpiMur(nostroMur, userId, userName);
			} else {
				return "Invalid currency specified!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while processing verification.";
		}
	}

	@RequestMapping(value = "/cancelVerifyUpi", method = RequestMethod.POST)
	@ResponseBody
	public String cancelVerifyUpi(@RequestParam("currency") String currency,
			@ModelAttribute RECON_UPIREPORT_USD_ENTITY nostroUsd, @ModelAttribute RECON_UPIREPORT_MUR_ENTITY nostroMur,
			HttpServletRequest request) {

		String userId = (String) request.getSession().getAttribute("USERID");
		String userName = (String) request.getSession().getAttribute("USERNAME");

		try {
			if ("USD".equalsIgnoreCase(currency)) {
				return nostroService.cancelVerifyUpiUsd(nostroUsd, userId, userName);
			} else if ("MUR".equalsIgnoreCase(currency)) {
				return nostroService.cancelVerifyUpiMur(nostroMur, userId, userName);
			} else {
				return "Invalid currency specified!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while cancelling verification.";
		}
	}

	// source data mapping
	
	@RequestMapping(value = "SourceData", method = { RequestMethod.GET, RequestMethod.POST })
	public String SourceData(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {

		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("List", PRINT_ENQUIRY_REPO.getValues());
		}

		return "SourceData";
	}
	
	//fatca mapping
	
	@GetMapping("/FATCA")
	public String fatcaPage(Model model, HttpServletRequest request) {
	    String userId = (String) request.getSession().getAttribute("USERID");
	    String userName = (String) request.getSession().getAttribute("USERNAME");
	    model.addAttribute("userId", userId);
	    model.addAttribute("userName", userName);
	    return "FATCA"; // loads src/main/resources/templates/FATCA.html
	}
	
	
	@GetMapping("/CRS")
    public String loadCRSContent() {
        return "CRS";
    }
	
	@GetMapping("/MRI") 
	public String showMRIPage() { 
		return "MRI"; }
	

	@RequestMapping(value = "Report_Dr", method = { RequestMethod.GET, RequestMethod.POST })
	public String Report_Dr(@RequestParam(required = false) String formmode, HttpServletRequest req,
			@RequestParam(required = false) String userid, Model md, @RequestParam(required = false) String ReconDate,
			@RequestParam(required = false) String curr, @RequestParam(required = false) String type) {
		String user = (String) req.getSession().getAttribute("USERID");
		System.out.println(user);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("RateList", rate_main_Repo.getList());
			md.addAttribute("loginuser", user);
		} else if (formmode.equals("ReportUSD")) {
			md.addAttribute("formmode", "ReportUSD");
			RECON_DREPORT_USD_ENTITY usd = RECON_DREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", usd);

			RECON_TEMP_DREPORT_USD_ENTITY usd1 = RECON_TEMP_DRREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", usd1);
		} else if (formmode.equals("ReportMUR")) {
			md.addAttribute("formmode", "ReportMUR");
			RECON_DRREPORT_MUR_ENTITY mur = RECON_DRREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", mur);

			RECON_TEMP_DRREPORT_MUR_ENTITY mur1 = RECON_TEMP_DRREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", mur1);
		} else if (formmode.equals("ModiReportUSD")) {
			md.addAttribute("formmode", "ModiReportUSD");
			RECON_DREPORT_USD_ENTITY usd = RECON_DREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", usd);

			RECON_TEMP_DREPORT_USD_ENTITY usd1 = RECON_TEMP_DRREPORT_USD_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", usd1);
		} else if (formmode.equals("ModiReportMUR")) {
			md.addAttribute("formmode", "ModiReportMUR");
			RECON_DRREPORT_MUR_ENTITY mur = RECON_DRREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report", mur);

			RECON_TEMP_DRREPORT_MUR_ENTITY mur1 = RECON_TEMP_DRREPORT_MUR_REPO.getReconDate(ReconDate, curr);
			md.addAttribute("report1", mur1);
		}
		return "DRReport";
	}

	@RequestMapping(value = "modiNostroDr", method = RequestMethod.POST)
	@ResponseBody
	public String modiNostroDr(@RequestParam("formmode") String formmode, @RequestParam("currency") String currency,
			@ModelAttribute RECON_DREPORT_USD_ENTITY nostroUsd, @ModelAttribute RECON_DRREPORT_MUR_ENTITY nostroMur,
			HttpServletRequest request) {

		String userId = (String) request.getSession().getAttribute("USERID");
		String userName = (String) request.getSession().getAttribute("USERNAME");

		try {
			if ("USD".equalsIgnoreCase(currency)) {
				return nostroService.editDrUsd(nostroUsd, formmode, userId, userName);
			} else if ("MUR".equalsIgnoreCase(currency)) {
				return nostroService.editDrMur(nostroMur, formmode, userId, userName);
			} else {
				return "Invalid currency specified!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while processing request.";
		}
	}

	@RequestMapping(value = "/verifyNostroDr", method = RequestMethod.POST)
	@ResponseBody
	public String verifyNostroDr(@RequestParam("currency") String currency,
			@ModelAttribute RECON_DREPORT_USD_ENTITY nostroUsd, @ModelAttribute RECON_DRREPORT_MUR_ENTITY nostroMur,
			HttpServletRequest request) {

		String userId = (String) request.getSession().getAttribute("USERID");
		String userName = (String) request.getSession().getAttribute("USERNAME");

		try {
			if ("USD".equalsIgnoreCase(currency)) {
				return nostroService.verifyDrUsd(nostroUsd, userId, userName);
			} else if ("MUR".equalsIgnoreCase(currency)) {
				return nostroService.verifyDrMur(nostroMur, userId, userName);
			} else {
				return "Invalid currency specified!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while processing verification.";
		}
	}

	@RequestMapping(value = "/cancelVerifyDr", method = RequestMethod.POST)
	@ResponseBody
	public String cancelVerifyDr(@RequestParam("currency") String currency,
			@ModelAttribute RECON_DREPORT_USD_ENTITY nostroUsd, @ModelAttribute RECON_DRREPORT_MUR_ENTITY nostroMur,
			HttpServletRequest request) {

		String userId = (String) request.getSession().getAttribute("USERID");
		String userName = (String) request.getSession().getAttribute("USERNAME");

		try {
			if ("USD".equalsIgnoreCase(currency)) {
				return nostroService.cancelVerifyDrUsd(nostroUsd, userId, userName);
			} else if ("MUR".equalsIgnoreCase(currency)) {
				return nostroService.cancelVerifyDrMur(nostroMur, userId, userName);
			} else {
				return "Invalid currency specified!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while cancelling verification.";
		}
	}

	@RequestMapping(value = "DeleteReport", method = { RequestMethod.GET, RequestMethod.POST })
	public String DeleteReport(@RequestParam(required = false) String formmode,
			@RequestParam(required = false) String userid, Model md) {
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("List", PRINT_ENQUIRY_REPO.getValues());
		}
		return "DeleteReport";
	}

	@RequestMapping(value = "BulkReportCR", method = { RequestMethod.GET, RequestMethod.POST })
	public String BulkReportCR(@RequestParam(required = false) String formmode, HttpServletRequest req,
			@RequestParam(required = false) String userid, Model md,
			@RequestParam(required = false) String ReconFromDate, @RequestParam(required = false) String ReconToDate,
			@RequestParam(required = false) String curr, @RequestParam(required = false) String type) {
		String user = (String) req.getSession().getAttribute("USERID");
		System.out.println(user);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("RateList", rate_main_Repo.getList());
			md.addAttribute("loginuser", user);
		} else if (formmode.equals("ReportUSD")) {
			md.addAttribute("formmode", "ReportUSD");
			RECON_CRREPORT_USD_ENTITY usd = RECON_CRREPORT_USD_REPO.getReconDateBulk(ReconFromDate, ReconToDate, curr);
			md.addAttribute("report", usd);
		} else if (formmode.equals("ReportMUR")) {
			md.addAttribute("formmode", "ReportMUR");
			RECON_CRREPORT_MUR_ENTITY mur = RECON_CRREPORT_MUR_REPO.getReconDateBulk(ReconFromDate, ReconToDate, curr);
			md.addAttribute("report", mur);
		}
		return "CRReportBulk";
	}

	@RequestMapping(value = "Report_UPIBULK", method = { RequestMethod.GET, RequestMethod.POST })
	public String Report_UPIBULK(@RequestParam(required = false) String formmode, HttpServletRequest req,
			@RequestParam(required = false) String userid, Model md,
			@RequestParam(required = false) String ReconFromDate, @RequestParam(required = false) String ReconToDate,
			@RequestParam(required = false) String curr, @RequestParam(required = false) String type) {
		String user = (String) req.getSession().getAttribute("USERID");
		System.out.println(user);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("RateList", rate_main_Repo.getList());
			md.addAttribute("loginuser", user);
		} else if (formmode.equals("ReportUSD")) {
			md.addAttribute("formmode", "ReportUSD");
			RECON_UPIREPORT_USD_ENTITY usd = RECON_UPIREPORT_USD_REPO.getReconDateBulk(ReconFromDate, ReconToDate,
					curr);
			md.addAttribute("report", usd);

		} else if (formmode.equals("ReportMUR")) {
			md.addAttribute("formmode", "ReportMUR");
			RECON_UPIREPORT_MUR_ENTITY mur = RECON_UPIREPORT_MUR_REPO.getReconDateBulk(ReconFromDate, ReconToDate,
					curr);
			md.addAttribute("report", mur);

		}
		return "UPIReportBulk";
	}

	@RequestMapping(value = "Report_DrBulk", method = { RequestMethod.GET, RequestMethod.POST })
	public String Report_DrBulk(@RequestParam(required = false) String formmode, HttpServletRequest req,
			@RequestParam(required = false) String userid, Model md,
			@RequestParam(required = false) String ReconFromDate, @RequestParam(required = false) String ReconToDate,
			@RequestParam(required = false) String curr, @RequestParam(required = false) String type) {
		String user = (String) req.getSession().getAttribute("USERID");
		System.out.println(user);
		if (formmode == null || formmode.equals("list")) {
			md.addAttribute("formmode", "list");
			md.addAttribute("RateList", rate_main_Repo.getList());
			md.addAttribute("loginuser", user);
		} else if (formmode.equals("ReportUSD")) {
			md.addAttribute("formmode", "ReportUSD");
			RECON_DREPORT_USD_ENTITY usd = RECON_DREPORT_USD_REPO.getReconDateBulk(ReconFromDate, ReconToDate, curr);
			md.addAttribute("report", usd);

		} else if (formmode.equals("ReportMUR")) {
			md.addAttribute("formmode", "ReportMUR");
			RECON_DRREPORT_MUR_ENTITY mur = RECON_DRREPORT_MUR_REPO.getReconDateBulk(ReconFromDate, ReconToDate, curr);
			md.addAttribute("report", mur);

		}
		return "DRReportBulk";
	}

}
