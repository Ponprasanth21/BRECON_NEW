package com.bornfire.recon.controllers;



import java.io.FileNotFoundException; 
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_DRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_DRMAIN_REPO;
import com.bornfire.recon.services.upload.CrUploadService;
import com.bornfire.recon.services.upload.DateParser;
import com.bornfire.recon.services.upload.DownloadService;
import com.bornfire.recon.services.upload.DownloadServiceBulk;
import com.bornfire.recon.services.upload.DrUploadService;
import com.bornfire.recon.services.upload.ProcessDownloadService;
import com.bornfire.recon.services.upload.UpiUploadService;
import com.bornfire.recon.entities.upload.*;
import java.util.ArrayList;
import org.springframework.jdbc.core.JdbcTemplate;

@Controller
@ConfigurationProperties("default")
public class BRECONUploadController {
	private static final Logger logger = LoggerFactory.getLogger(BRECONUploadController.class);

	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;

	@Autowired
	SequenceGenerator sequence;
	
	@Autowired
	DateParser DateParser;

	@Autowired
	CrUploadService cruploadService;
	
	@Autowired
	DrUploadService druploadService;
	
	@Autowired
	UpiUploadService upiUploadService;

	@Autowired
	RECON_CRFILE_SOURCE_REPO file_Source_Repo;

	@Autowired
	RECON_CRFILE_DESTINATION_REPO File_Destination_Repo;

	@Autowired
	DownloadService downloadService;
	
	
	@Autowired
	DownloadServiceBulk downloadServiceBulk;
	@Autowired
	ProcessDownloadService processdownloadService;
	
	@Autowired
	RECON_DRFILE_DESTINATION_REPO RECON_DRFILE_DESTINATION_REPO;
	
	@Autowired
	RECON_DRFILE_SOURCE_REPO RECON_DRFILE_SOURCE_REPO;
	
	@Autowired
	RECON_DRMAIN_REPO RECON_DRMAIN_REPO;
	
	@Autowired
	RECON_CRMAIN_REPO RECON_CRMAIN_REPO;
	
	@Autowired
	RECON_CRFILE_SOURCE_REPO RECON_CRFILE_SOURCE_REPO;
	
	@Autowired
	RECON_CRFILE_DESTINATION_REPO RECON_CRFILE_DESTINATION_REPO;
	
	@Autowired
	RECON_UPIMAIN_REPO RECON_UPIMAIN_REPO;
	
	@Autowired
	RECON_UPI_SOURCE_REPO RECON_UPI_SOURCE_REPO;
	
	@Autowired
	RECON_UPI_DESTINATION_REPO RECON_UPI_DESTINATION_REPO;
	
	
	// file upload controller for source data
	
	@Autowired
    private com.bornfire.recon.services.SourceDataUploadService sourceDataUploadService;

	@PostMapping(value = "SourceDataUpload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sourceDataUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            HttpServletRequest rq) {

        Map<String, Object> response = new HashMap<>();
        String userId = (String) rq.getSession().getAttribute("USERID");

        try {
            int insertedRows = sourceDataUploadService.uploadTransactionFile(file, type, userId);

            response.put("status", "SUCCESS");
            response.put("type", type);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", (file.getSize() / 1024) + " KB");
            response.put("insertedRows", insertedRows);
            return ResponseEntity.ok(response);
        } catch (Throwable e) {
            logger.error("UPLOAD ERROR DETAILS:", e);
            e.printStackTrace(); // Prints the exact line number to Eclipse Console
            
            // Build meaningful message for browser alert
            String errorDetail = e.getClass().getSimpleName() + ": " + 
                                 (e.getMessage() != null ? e.getMessage() : "No message");
            if (e.getStackTrace().length > 0) {
                StackTraceElement top = e.getStackTrace()[0];
                errorDetail += " at " + top.getFileName() + ":" + top.getLineNumber();
            }
            
            response.put("status", "ERROR");
            response.put("message", errorDetail);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    // download the data from sourcedata

    @GetMapping("/SourceDataExportExcel")
    public void sourceDataExportExcel(
            @RequestParam("type") String type, 
            HttpServletResponse response) {
        try {
            sourceDataUploadService.exportDataToExcel(type, response);
        } catch (Exception e) {
            logger.error("Error generating Excel download for " + type, e);
        }
    }

    //reconsilanation
    
    @PostMapping("runVisaReconciliation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> runVisaReconciliation(
            @RequestParam(value = "reconDate", required = false) String reconDate) {

        Map<String, Object> res = new HashMap<>();
        try {
            int totalProcessed = sourceDataUploadService.executeVisaReconciliation(reconDate);
            res.put("status", "SUCCESS");
            res.put("totalProcessed", totalProcessed);
            res.put("message", "Reconciliation completed successfully. Processed records: " + totalProcessed);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("downloadFailedRefundReport")
    public void downloadFailedRefundReport(HttpServletResponse response) {
        try {
            sourceDataUploadService.exportFailedRefundReport(response);
        } catch (Exception e) {
            logger.error("Error downloading Failed Refund Report", e);
        }
    }
    
    
    
    // home page analytics
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/getReconAnalytics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReconAnalytics() {
        Map<String, Object> data = new HashMap<>();

        // Helper lambda or method to safely fetch count without throwing table missing errors
        data.put("atmCount", getSafeCount("BRECON.ATM_TRANSACTION_DATA"));
        data.put("posCount", getSafeCount("BRECON.POS_TRANSATION_DATA"));
        data.put("cbsCount", getSafeCount("BRECON.CBS_TXN"));
        data.put("epinCount", getSafeCount("BRECON.BRECON_CARD_EPIN_DATA_TABLE"));

        // Reconciliation Status Counts
        int settled = 0, failedRefund = 0, pending = 0;
        try {
            List<Map<String, Object>> statusList = jdbcTemplate.queryForList(
                "SELECT RECON_STATUS, COUNT(*) AS CNT FROM BRECON.RECON_TXN GROUP BY RECON_STATUS"
            );
            for (Map<String, Object> row : statusList) {
                String status = row.get("RECON_STATUS") != null ? String.valueOf(row.get("RECON_STATUS")) : "";
                Object cntObj = row.get("CNT");
                int cnt = cntObj instanceof Number ? ((Number) cntObj).intValue() : 0;

                if ("SETTLED".equalsIgnoreCase(status)) {
                    settled = cnt;
                } else if ("FAILED_REFUND".equalsIgnoreCase(status)) {
                    failedRefund = cnt;
                } else if ("PENDING_INVESTIGATION".equalsIgnoreCase(status)) {
                    pending = cnt;
                }
            }
        } catch (Exception e) {
            // Log warning if table is empty or being created
            System.err.println("Warning querying RECON_TXN status: " + e.getMessage());
        }

        data.put("settled", settled);
        data.put("failedRefund", failedRefund);
        data.put("pending", pending);
        data.put("totalRecon", (settled + failedRefund + pending));

        // Top 10 Exception records
        List<Map<String, Object>> exceptions = new ArrayList<>();
        try {
            exceptions = jdbcTemplate.queryForList(
                "SELECT CARD_NUMBER, RRN, AUTH_ID, AMOUNT, TRAN_DT, RECON_STATUS, REASON " +
                "FROM BRECON.RECON_TXN " +
                "WHERE RECON_STATUS IN ('FAILED_REFUND', 'PENDING_INVESTIGATION') " +
                "AND ROWNUM <= 10"
            );
        } catch (Exception e) {
            System.err.println("Warning querying RECON_TXN exceptions: " + e.getMessage());
        }
        data.put("exceptions", exceptions);

        return ResponseEntity.ok(data);
    }

    // Safe count helper method (place right below in the same controller)
    private int getSafeCount(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            return count != null ? count : 0;
        } catch (Exception ex) {
            System.err.println("Table " + tableName + " not available or empty: " + ex.getMessage());
            return 0;
        }
    }
   
    
    //fatca uploadcontroller 
    
    @Autowired
    private com.bornfire.recon.services.FatcaManagementService fatcaService;
    
		 // =========================================================================
		 // FATCA / MCIB REPORTING ENDPOINTS
		 // =========================================================================
		
		 @GetMapping("/getFatcaDashboardData")
		 @ResponseBody
		 public ResponseEntity<Map<String, Object>> getFatcaDashboardData() {
		     return ResponseEntity.ok(fatcaService.getFatcaDashboardData());
		 }
		
		 @PostMapping("/uploadFatcaFile")
		 @ResponseBody
		 public ResponseEntity<Map<String, Object>> uploadFatcaFile(
		         @RequestParam("file") MultipartFile file,
		         HttpServletRequest request) {
		
		     Map<String, Object> res = new HashMap<>();
		     String userId = (String) request.getSession().getAttribute("USERID");
		
		     try {
		         int inserted = fatcaService.uploadFatcaFile(file, userId);
		         res.put("status", "SUCCESS");
		         res.put("insertedRows", inserted);
		         res.put("message", "Uploaded " + inserted + " records into BRECON.FACTA_TRANSCATION_DATA.");
		         return ResponseEntity.ok(res);
		     } catch (Exception e) {
		         logger.error("FATCA upload failed", e);
		         res.put("status", "ERROR");
		         res.put("message", e.getMessage());
		         return ResponseEntity.badRequest().body(res);
		     }
		 }
		
		 @PostMapping("/updateFatcaRecord")
		 @ResponseBody
		 public ResponseEntity<Map<String, Object>> updateFatcaRecord(
		         @RequestParam("refNo") String refNo,
		         @RequestParam("status") String status,
		         HttpServletRequest request) {
		
		     Map<String, Object> res = new HashMap<>();
		     String userId = (String) request.getSession().getAttribute("USERID");
		     boolean updated = fatcaService.updateFatcaRecord(refNo, status, userId);
		
		     res.put("status", updated ? "SUCCESS" : "ERROR");
		     res.put("message", updated ? "Record updated successfully" : "Failed to update record");
		     return ResponseEntity.ok(res);
		 }
		
		 @GetMapping("/downloadFatcaXml")
		 public void downloadFatcaXml(HttpServletResponse response) {
		     try {
		         String xmlContent = fatcaService.generateFatcaXml();
		         response.setContentType("application/xml");
		         response.setHeader("Content-Disposition", 
		                 "attachment; filename=\"FATCA_REPORT_" + System.currentTimeMillis() + ".xml\"");
		         response.getOutputStream().write(xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		         response.getOutputStream().flush();
		     } catch (Exception e) {
		         logger.error("Error downloading FATCA XML", e);
		     }
		 }
 
 
    
    
    
    
	@PostMapping(value = "CrFileUpload")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> UploadExcel(@RequestParam("file") MultipartFile[] file, @RequestParam("fileInput") String fileInput,
	@RequestParam("overwrite") Boolean	overwrite ,Model md, HttpServletRequest rq ,@RequestParam("Fromdate") String Fromdate )
	throws FileNotFoundException, SQLException, IOException, NullPointerException {
		System.out.println("fileSize" + file.length);
		String userID = (String) rq.getSession().getAttribute("USERID");
		String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
		Map<String, Object> resultMap = new LinkedHashMap<>();
		if (fileInput.equalsIgnoreCase("SOURCE")) {
			// Save in Source table
			resultMap = cruploadService.saveCrSourceFile(fileInput, file, userID, USERNAME ,overwrite ,Fromdate);
		} else if (fileInput.equalsIgnoreCase("DESTINATION")) {
			// Save in Destination table
			resultMap = cruploadService.saveCrDestinationFile(fileInput, file, userID, USERNAME,overwrite, Fromdate);
		}else {
			resultMap.put("message", "Invalid file type specified");
		}
		return ResponseEntity.ok(resultMap);
	}
	@PostMapping(value = "DrFileUpload")
	@ResponseBody
	public Map<String, Object> DrFileUpload(@RequestParam("file") MultipartFile[] files, @RequestParam("fileInput") String fileInput,@RequestParam("fromDate") String fromDate,
			boolean overwrite,HttpServletRequest rq)throws SQLException, IOException {

	    System.out.println("Total files uploaded: " + files.length);
	    String userID = (String) rq.getSession().getAttribute("USERID");
	    String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
	    Map<String, Object> result = new HashMap<>();
	    if (fileInput.equalsIgnoreCase("SOURCE")) {
	        result = druploadService.SaveDrSourceFiles(fileInput, files, userID, USERNAME,overwrite,fromDate);
	    } else if (fileInput.equalsIgnoreCase("DESTINATION")) {
	        result = druploadService.SaveDrDestFiles(fileInput, files, userID, USERNAME,overwrite,fromDate);
	    } else {
	        result.put("status", "error");
	        result.put("message", "Invalid file type specified");
	        return result;
	    }
	    return result;
	}
	@PostMapping(value = "UpiFileUpload")
	@ResponseBody
	public Map<String, Object> UpiFileUpload(@RequestParam("file") MultipartFile[] files, @RequestParam("fileInput") String fileInput,@RequestParam("fromDate") String fromDate,   // ✅ New param
			boolean overwrite,HttpServletRequest rq)throws SQLException, IOException {

	    System.out.println("Total files uploaded: " + files.length);
	    String userID = (String) rq.getSession().getAttribute("USERID");
	    String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
	    
	    Map<String, Object> result = new HashMap<>();
	    if (fileInput.equalsIgnoreCase("SOURCE")) {
	        result = upiUploadService.SaveUpiSourceFiles(fileInput, files, userID, USERNAME,overwrite,fromDate);
	    } else if (fileInput.equalsIgnoreCase("DESTINATION")) {
	        result = upiUploadService.SaveUpiDestFiles(fileInput, files, userID, USERNAME,overwrite,fromDate);
	    } else {
	        result.put("status", "error");
	        result.put("message", "Invalid file type specified");
	        return result;
	    }
	    return result;
	}
	
	@GetMapping("/CrDisplayExcel")
	public void CrDisplayExcel(@RequestParam("type") String type, HttpServletRequest request, HttpServletResponse response) {
	    String userID = (String) request.getSession().getAttribute("USERID");
	    String userName = (String) request.getSession().getAttribute("USERNAME");
	    String auditRefNo = sequence.generateRequestUUId();
	    downloadService.CrExportExcel(type, userID, userName, auditRefNo, response);
	}
	
	@GetMapping("/DrDisplayExcel")
	public void DrDisplayExcel(@RequestParam("type") String type, HttpServletRequest request, HttpServletResponse response) {
	    String userID = (String) request.getSession().getAttribute("USERID");
	    String userName = (String) request.getSession().getAttribute("USERNAME");
	    String auditRefNo = sequence.generateRequestUUId();
	    downloadService.DrExportExcel(type, userID, userName, auditRefNo, response);
	}
	
	@GetMapping("/UpiDisplayExcel")
	public void UpiDisplayExcel(@RequestParam("type") String type, HttpServletRequest request, HttpServletResponse response) {
	    String userID = (String) request.getSession().getAttribute("USERID");
	    String userName = (String) request.getSession().getAttribute("USERNAME");
	    String auditRefNo = sequence.generateRequestUUId();
	    downloadService.UpiExportExcel(type, userID, userName, auditRefNo, response);
	}

	@GetMapping("/ReconDownload")
	public void reconDownload(HttpServletResponse response, @RequestParam(required = false) String Cur,
			@RequestParam(required = false) String type, @RequestParam(required = false) String filetype,
			@RequestParam(required = false) String recon_date , HttpServletRequest request,@RequestParam(required = false) String report) {
		 String userID = (String) request.getSession().getAttribute("USERID");
		 String userName = (String) request.getSession().getAttribute("USERNAME");
		    
		downloadService.exportCurrencyReport(Cur, recon_date, type, response,report);
		downloadService.GenerateList(userID ,userName);
	}
	
	@GetMapping("/ReconDownloadBulk")
	public void ReconDownloadBulk(HttpServletResponse response, @RequestParam(required = false) String Cur,
			@RequestParam(required = false) String type, @RequestParam(required = false) String filetype,
			@RequestParam(required = false) String recon_from_date,@RequestParam(required = false) String recon_to_date , HttpServletRequest request,@RequestParam(required = false) String report) {
		 String userID = (String) request.getSession().getAttribute("USERID");
		 String userName = (String) request.getSession().getAttribute("USERNAME");
		    
		 downloadServiceBulk.exportCurrencyReport(Cur, recon_from_date,recon_to_date, type, response,report);
		 downloadServiceBulk.GenerateList(userID ,userName);
	}
	
	@GetMapping("/ReconProcessDownload")
	public void ReconProcessDownload(HttpServletResponse response, @RequestParam String recon_process_date,
			@RequestParam String type) throws Exception {

		System.out.println("Recon Process Date: " + recon_process_date);
		System.out.println("Type: " + type);

		List<Object[]> destData;
		List<Object[]> sourceData;
		List<Object[]> matchedData;

		// ✅ Conditional logic based on type
		if ("DEBIT".equalsIgnoreCase(type)) {

			destData = RECON_DRFILE_DESTINATION_REPO.getDestListByDate(recon_process_date);
			sourceData = RECON_DRFILE_SOURCE_REPO.getSourceListByDate(recon_process_date);
			matchedData = RECON_DRMAIN_REPO.getMainListByDate(recon_process_date);

		} else if ("CREDIT".equalsIgnoreCase(type)) {

			destData = RECON_CRFILE_DESTINATION_REPO.getDestListByDate(recon_process_date);
			sourceData = RECON_CRFILE_SOURCE_REPO.getSourceListByDate(recon_process_date);
			matchedData = RECON_CRMAIN_REPO.getMainListByDate(recon_process_date);

		} else if ("UPI".equalsIgnoreCase(type)) {

			destData = RECON_UPI_DESTINATION_REPO.getDestListByDate(recon_process_date);
			sourceData = RECON_UPI_SOURCE_REPO.getSourceListByDate(recon_process_date);
			matchedData = RECON_UPIMAIN_REPO.getMainListByDate(recon_process_date);

		} else {
			throw new IllegalArgumentException("Invalid type specified: " + type);
		}

		// ✅ Pass type to service
		byte[] excelBytes = processdownloadService.generateReconCombinedExcel(destData, sourceData, matchedData,
				recon_process_date, type);

		// ✅ Prepare response for Excel download
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition",
				"attachment; filename=Recon_Report_" + type + "_" + recon_process_date + ".xlsx");

		response.getOutputStream().write(excelBytes);
		response.getOutputStream().flush();
	}

}
