package com.bornfire.recon.controllers;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.ACCESS_AND_ROLES_REPO;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.RATE_ENGINE_ENTITY;
import com.bornfire.recon.entities.RATE_ENGINE_REPO;
import com.bornfire.recon.entities.RATE_MAIN_ENTITY;
import com.bornfire.recon.entities.RATE_MAIN_REPO;
import com.bornfire.recon.entities.RECON_SESSION;
import com.bornfire.recon.entities.REFERENCE_CODE_REPO;
import com.bornfire.recon.entities.USER_PROFILE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_CRMAIN_ENTITY;
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
import com.bornfire.recon.entities.upload.RECON_DRMAIN_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRMAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_DRREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIMAIN_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIMAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.*;
import com.bornfire.recon.services.LoginServices;


@RestController
public class BRECONRestController {
	
	@Autowired
	RATE_MAIN_REPO rate_main_Repo;
	
	@Autowired
	LoginServices loginServices;
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	ACCESS_AND_ROLES_REPO accessandRolesRepository;
	
	@Autowired
	RATE_ENGINE_REPO Rate_Engine_Repo;
	
	@Autowired
	SequenceGenerator sequence;
	
	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;
	
	@Autowired
	RECON_CRREPORT_MUR_REPO RECON_CRREPORT_MUR_REPO;
	
	@Autowired
	RECON_CRREPORT_USD_REPO RECON_CRREPORT_USD_REPO;
	
	@Autowired
	RECON_CRFILE_SOURCE_REPO fileSourceRepo;

	@Autowired
	RECON_CRFILE_DESTINATION_REPO fileDestinationRepo;

	@Autowired
	RECON_CRMAIN_REPO reconProRepo;
	
	@Autowired
	RECON_UPIREPORT_MUR_REPO RECON_UPIREPORT_MUR_REPO;
	
	@Autowired
	RECON_UPIREPORT_USD_REPO RECON_UPIREPORT_USD_REPO;

	@Autowired
	RECON_DRFILE_SOURCE_REPO RECON_DRFILE_SOURCE_REPO;
	
	@Autowired
	RECON_DRFILE_DESTINATION_REPO BRECON_DRFILE_DESTINATION_REPO;

	@Autowired
	RECON_DRMAIN_REPO RECON_DRMAIN_REPO;

	@Autowired
	RECON_UPI_SOURCE_REPO RECON_UPI_SOURCE_REPO;

	@Autowired
	RECON_UPI_DESTINATION_REPO RECON_UPI_DESTINATION_REPO;

	@Autowired
	RECON_UPIMAIN_REPO RECON_UPIMAIN_REPO;
	
	@Autowired
	RECON_DRREPORT_USD_REPO RECON_DRREPORT_USD_REPO;
	
	@Autowired 
	REFERENCE_CODE_REPO referenceCodeRep;
	
	@Autowired 
	RECON_DRREPORT_MUR_REPO RECON_DRREPORT_MUR_REPO;
	
	@Autowired
	RECON_UPIREPORT_MARKUP_REPO RECON_UPIREPORT_MARKUP_REPO;
	
	@Autowired
	RECON_DRREPORT_MARKUP_REPO RECON_DRREPORT_MARKUP_REPO;
	
	@RequestMapping(value = "userlogList", method = RequestMethod.GET)
	public List<RECON_SESSION> userLogList(@RequestParam String fromdate, @RequestParam String todate) {

		Date fromdate2 = null;
		Date todate2 = null;

		try {
			fromdate2 = new SimpleDateFormat("dd-MM-yyyy").parse(fromdate);
			todate2 = new SimpleDateFormat("dd-MM-yyyy").parse(todate);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return loginServices.getUserLog(fromdate2, todate2);

	}
	
	@RequestMapping(value = "ApiRoleId", method = RequestMethod.GET)
	public String[] ApiRoleId(@RequestParam String role_id) {

		return accessandRolesRepository.CheckRoleId(role_id);

	}
	
	
	@RequestMapping(value = "ApiRoleDesc", method = RequestMethod.GET)
	public String[] ApiRoleDesc(@RequestParam String role_desc) {

		return accessandRolesRepository.CheckRoleDesc(role_desc);

	}
	@RequestMapping(value = "CheckRule", method = RequestMethod.GET)
	public String CheckRule(@RequestParam String rule) {
	//	System.out.println(rule);
		RATE_ENGINE_ENTITY rule1 = 	Rate_Engine_Repo.getrule(rule);
		return (rule1 == null) ? "NO" : "SAME";
	}
	
	@RequestMapping(value = "RuleDel", method = RequestMethod.GET)
	public String RuleDel(@RequestParam String unique, HttpServletRequest req) {
		
		String user = (String) req.getSession().getAttribute("USERID");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");
		String audit_ref_no =sequence.generateRequestUUId();
		
		RATE_ENGINE_ENTITY rule1 = 	Rate_Engine_Repo.getUnique(unique);
		rule1.setDel_flg("Y");
		
		//AUDIT
		 
        BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		audit.setAudit_date(new Date());
		audit.setEntry_time(new Date());
		audit.setEntry_user(user);
		audit.setFunc_code("DELETE");
		audit.setRemarks("Deletion complete!");
		audit.setAudit_table("RULE_ENGINE_TABLE");
		audit.setAudit_screen("RULE ENGINEE");
		audit.setEvent_id(user);
		audit.setEvent_name(USERNAME);
		audit.setModi_details("-");
		audit.setAudit_ref_no(audit_ref_no);
		BRECON_Audit_Rep.save(audit);
		
		Rate_Engine_Repo.save(rule1);
		return "Deletion complete!";
	}
	
	@RequestMapping(value = "getMaxVersion", method = RequestMethod.GET)
	public int getMaxVersion(@RequestParam("ruleCode") String ruleCode) {
	    List<RATE_ENGINE_ENTITY> sub = Rate_Engine_Repo.getSubrule(ruleCode);
	    int maxSeq1 = 0;

	    for (RATE_ENGINE_ENTITY srl : sub) {
	        if (srl == null || srl.getVersion() == null) continue;

	        try {
	            int seq = Integer.parseInt(srl.getVersion());
	            if (seq > maxSeq1) {
	                maxSeq1 = seq;
	            }
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid version format: " + srl.getVersion());
	        }
	    }

	    return maxSeq1;
	}
	
	@RequestMapping(value = "getUserBlobImage/{userID}", method = RequestMethod.GET)
	@ResponseBody
	public String BlobImageUser(@PathVariable("userID") String userID, Model md) {
		USER_PROFILE_ENTITY userProfile = loginServices.UserBlobImages(userID);
		System.out.println(userID);
		if (userProfile != null && userProfile.getPhoto() != null) {
			return Base64.getEncoder().encodeToString(userProfile.getPhoto());
		}
		return "";
	}
	
	@RequestMapping(value = "RuleVerify", method = RequestMethod.GET)
	public String RuleVerify(@RequestParam String unique, HttpServletRequest req) {
				
		String user = (String) req.getSession().getAttribute("USERID");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");
		String audit_ref_no =sequence.generateRequestUUId();
		
		RATE_ENGINE_ENTITY rule1 = 	Rate_Engine_Repo.getUnique(unique);
		rule1.setAuth_flg("Y");
		rule1.setVerify_user(user);
		rule1.setVerify_time(new Date());
		
		//AUDIT
		 
        BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		audit.setAudit_date(new Date());
		audit.setEntry_time(new Date());
		audit.setEntry_user(user);
		audit.setFunc_code("VERIFY");
		audit.setRemarks("Verification complete!");
		audit.setAudit_table("RULE_ENGINE_TABLE");
		audit.setAudit_screen("RULE ENGINEE");
		audit.setEvent_id(user);
		audit.setEvent_name(USERNAME);
		audit.setModi_details("-");
		audit.setAudit_ref_no(audit_ref_no);
		BRECON_Audit_Rep.save(audit);
		
		Rate_Engine_Repo.save(rule1);
		return "Verification complete!";
	}
	
	@GetMapping("/getTodayVersions")
	@ResponseBody
	public List<RATE_MAIN_ENTITY> getTodayVersions() {
	    try {
	        List<RATE_MAIN_ENTITY> all = rate_main_Repo.fetchMaxVersionRates();

	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        String today = sdf.format(new Date());

	        return all.stream()
	                  .filter(e -> sdf.format(e.getEntry_time()).equals(today))
	                  .collect(Collectors.toList());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }
	}
	@GetMapping("/getYesterdayVersions")
	@ResponseBody
	public List<RATE_MAIN_ENTITY> getYesterdayVersions() {
	    try {
	        return rate_main_Repo.findLatestVerifiedRatesExcludingToday();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }
	}

	@GetMapping("/getVersionsByDate")
	@ResponseBody
	public List<RATE_MAIN_ENTITY> getVersionsByDate(@RequestParam("selectedDate") String selectedDate) {
	    try {
	        // Convert yyyy-MM-dd to dd-MM-yyyy
	        LocalDate date = LocalDate.parse(selectedDate); // parses yyyy-MM-dd
	        String reformattedDate = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

	        return rate_main_Repo.findByEntryDate(reformattedDate);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }
	}
	
	@GetMapping("getReconDate")
	@ResponseBody
	public String getReconDate(@RequestParam(required = false)String curr,
	                           @RequestParam("recon_date") String recon_date,
	                           @RequestParam("type") String type) {
		System.out.println("works");
		System.out.println(recon_date);
	    try {
	        if ("CREDIT".equalsIgnoreCase(type)) {
	        	if ("USD".equalsIgnoreCase(curr)) {
	        	    RECON_CRREPORT_USD_ENTITY usd = RECON_CRREPORT_USD_REPO.getReconDate(recon_date, curr);
	        	    return (usd == null || usd.getRecon_date() == null) ? "No Data Found" : "Found";
	        	} else if ("MUR".equalsIgnoreCase(curr)) {
	        	    RECON_CRREPORT_MUR_ENTITY mur = RECON_CRREPORT_MUR_REPO.getReconDate(recon_date, curr);
	        	    return (mur == null || mur.getRecon_date() == null) ? "No Data Found" : "Found";
	        	}

	        }else  if ("UPI".equalsIgnoreCase(type)) {
	        	if ("USD".equalsIgnoreCase(curr)) {
	        	    RECON_UPIREPORT_USD_ENTITY usd = RECON_UPIREPORT_USD_REPO.getReconDate(recon_date, curr);
	        	    return (usd == null || usd.getRecon_date() == null) ? "No Data Found" : "Found";
	        	} else if ("MUR".equalsIgnoreCase(curr)) {
	        	    RECON_UPIREPORT_MUR_ENTITY mur = RECON_UPIREPORT_MUR_REPO.getReconDate(recon_date, curr);
	        	    return (mur == null || mur.getRecon_date() == null) ? "No Data Found" : "Found";
	        	}else  if ("MARKUP".equalsIgnoreCase(curr)) {
	        		System.out.println(curr);
		        	RECON_UPIREPORT_MARKUP_ENTITY mur = RECON_UPIREPORT_MARKUP_REPO.getReconDate(recon_date);
		        	    return (mur == null || mur.getRecon_date() == null) ? "No Data Found" : "Found";
		        }
	        }else  if ("DEBIT".equalsIgnoreCase(type)) {
	        	if ("USD".equalsIgnoreCase(curr)) {
	        		RECON_DREPORT_USD_ENTITY usd = RECON_DRREPORT_USD_REPO.getReconDate(recon_date, curr);
	        	    return (usd == null || usd.getRecon_date() == null) ? "No Data Found" : "Found";
	        	} else if ("MUR".equalsIgnoreCase(curr)) {
	        		RECON_DRREPORT_MUR_ENTITY mur = RECON_DRREPORT_MUR_REPO.getReconDate(recon_date, curr);
	        	    return (mur == null || mur.getRecon_date() == null) ? "No Data Found" : "Found";
	        	}else if ("LOCAL".equalsIgnoreCase(curr) || "IB".equalsIgnoreCase(curr)) {
	        	    System.out.println(curr);
	        	    Long count = RECON_DRREPORT_MARKUP_REPO.getReconDate(recon_date, curr);
	        	    return (count == null || count == 0) ? "No Data Found" : "Found";
	        	}
	        }
	        return "Invalid Type or Currency";
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "Error occurred while fetching Recon Date";
	    }
	}



	@RequestMapping(value = "CRFilter", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> CRFilter(@RequestParam String Date) {
	    
	    List<RECON_CRFILE_SOURCE_ENTITY> a = fileSourceRepo.getReconDate(Date);
	    List<RECON_CRFILE_DESTINATION_ENTITY> b = fileDestinationRepo.getReconDate(Date);
	    List<RECON_CRMAIN_ENTITY> c = reconProRepo.getReconDate(Date);
	    
	    // Put all lists into a map
	    Map<String, Object> result = new HashMap<>();
	    result.put("maintable", c); // matched records
	    result.put("sourceListUnmatched", a); // unmatched source
	    result.put("destinList", b); 
	    
	    return result; // Spring converts it to JSON automatically
	}
	
	@RequestMapping(value = "DRFilter", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> DRFilter(@RequestParam String Date) {
	    
	    List<RECON_DRFILE_SOURCE_ENTITY> a = RECON_DRFILE_SOURCE_REPO.getReconDate(Date);
	    List<RECON_DRFILE_DESTINATION_ENTITY> b = BRECON_DRFILE_DESTINATION_REPO.getReconDate(Date);
	    List<RECON_DRMAIN_ENTITY> c = RECON_DRMAIN_REPO.getReconDate(Date);
	    
	    // Put all lists into a map
	    Map<String, Object> result = new HashMap<>();
	    result.put("maintable", c); // matched records
	    result.put("sourceListUnmatched", a); // unmatched source
	    result.put("destinList", b); 
	    
	    return result; // Spring converts it to JSON automatically
	}
	
	@RequestMapping(value = "UPIFilter", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> UPIFilter(@RequestParam String Date) {
	    
	    List<RECON_UPI_SOURCE_ENTITY> a = RECON_UPI_SOURCE_REPO.getReconDate(Date);
	    List<RECON_UPI_DESTINATION_ENTITY> b = RECON_UPI_DESTINATION_REPO.getReconDate(Date);
	    List<RECON_UPIMAIN_ENTITY> c = RECON_UPIMAIN_REPO.getReconDate(Date);
	    
	    // Put all lists into a map
	    Map<String, Object> result = new HashMap<>();
	    result.put("maintable", c); // matched records
	    result.put("sourceListUnmatched", a); // unmatched source
	    result.put("destinList", b); 
	    
	    return result; // Spring converts it to JSON automatically
	}
	

	@RequestMapping(value = "getrefdesc", method = RequestMethod.GET)
	public String[] getrefdesc(@RequestParam String refdesc) {

		return referenceCodeRep.getBranchDesc(refdesc);

	}

	
	
	@GetMapping("deleteReconDate")
	@ResponseBody
	public String deleteReconDate(@RequestParam(required = false)String curr,
	                           @RequestParam("recon_date") String recon_date,
	                           @RequestParam("type") String type, HttpServletRequest req) {
		System.out.println("works");
		System.out.println(recon_date);
		
		String user = (String) req.getSession().getAttribute("USERID");
		String USERNAME = (String) req.getSession().getAttribute("USERNAME");
		String audit_ref_no =sequence.generateRequestUUId();
		 
		//AUDIT
		 
        BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		audit.setAudit_date(new Date());
		audit.setEntry_time(new Date());
		audit.setEntry_user(user);
		audit.setFunc_code("Delete");
		audit.setRemarks("Deletion complete!");
		audit.setAudit_table(type+"_MAIN_TABLE");
		audit.setAudit_screen(type+"_SCREEN");
		audit.setEvent_id(user);
		audit.setEvent_name(USERNAME);
		audit.setModi_details("Recon Data Deleted for "+type+ " Dated :"+recon_date);
		audit.setAudit_ref_no(audit_ref_no);
		BRECON_Audit_Rep.save(audit);
		
		
	    try {
	         RECON_CRREPORT_MUR_REPO.deleteRecon(recon_date, type);
	        return "Record Deleted for " +type+ " Dated : "+recon_date;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "Error occurred while fetching Recon Date";
	    }
	}
	
	@GetMapping("getReconDateCredit")
	@ResponseBody
	public String getReconDateCredit(@RequestParam(required = false)String curr,
	                           @RequestParam("recon_from_date") String recon_from_date,
	                           @RequestParam("recon_to_date") String recon_to_date,
	                           @RequestParam("type") String type) {
		System.out.println("works");
		System.out.println(recon_from_date);
		try {
			if ("CREDIT".equalsIgnoreCase(type)) {

				if ("USD".equalsIgnoreCase(curr)) {
					String usd = RECON_CRREPORT_USD_REPO.getReconDateCredit(recon_from_date, recon_to_date, curr);
					if (usd == null) {
						return "No Data Found";
					}
					BigDecimal amount = new BigDecimal(usd);
					return (amount.compareTo(BigDecimal.ZERO) == 0) ? "No Data Found" : "Found";
				}
				if ("MUR".equalsIgnoreCase(curr)) {
					String mur = RECON_CRREPORT_USD_REPO.getReconDateCreditMUR(recon_from_date, recon_to_date,
							curr);
					if (mur == null) {
						return "No Data Found";
					}
					BigDecimal amount = new BigDecimal(mur);
					return (amount.compareTo(BigDecimal.ZERO) == 0) ? "No Data Found" : "Found";
				}
			}else  if ("UPI".equalsIgnoreCase(type)) {
	        	if ("USD".equalsIgnoreCase(curr)) {
	        	    String usd = RECON_UPIREPORT_USD_REPO.getReconDateUSD(recon_from_date, recon_to_date, curr);
	        	    if (usd == null) {
						return "No Data Found";
					}
					BigDecimal amount = new BigDecimal(usd);
					return (amount.compareTo(BigDecimal.ZERO) == 0) ? "No Data Found" : "Found";
	        	} else if ("MUR".equalsIgnoreCase(curr)) {
	        	    String mur = RECON_UPIREPORT_MUR_REPO.getReconDateMUR(recon_from_date, recon_to_date, curr);
	        	    if (mur == null) {
						return "No Data Found";
					}
					BigDecimal amount = new BigDecimal(mur);
					return (amount.compareTo(BigDecimal.ZERO) == 0) ? "No Data Found" : "Found";
	        	}
			 }else  if ("DEBIT".equalsIgnoreCase(type)) {
		        	if ("USD".equalsIgnoreCase(curr)) {
		        		String usd = RECON_DRREPORT_USD_REPO.getReconDateUSD(recon_from_date, recon_to_date, curr);
		        		if (usd == null) {
							return "No Data Found";
						}
						BigDecimal amount = new BigDecimal(usd);
						return (amount.compareTo(BigDecimal.ZERO) == 0) ? "No Data Found" : "Found";
		        	} else if ("MUR".equalsIgnoreCase(curr)) {
		        		String mur = RECON_DRREPORT_MUR_REPO.getReconDateMUR(recon_from_date, recon_to_date, curr);
		        		if (mur == null) {
							return "No Data Found";
						}
						BigDecimal amount = new BigDecimal(mur);
						return (amount.compareTo(BigDecimal.ZERO) == 0) ? "No Data Found" : "Found";
		        	}
		        }

			return "Invalid Type or Currency";
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "Error occurred while fetching Recon Date";
	    }
	}

}
