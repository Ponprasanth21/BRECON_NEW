package com.bornfire.recon.controllers;

import java.io.FileNotFoundException; 
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.upload.*;
import com.bornfire.recon.services.SourceDataUploadService;
import com.bornfire.recon.services.upload.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;

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

    @Autowired
    private SourceDataUploadService sourceDataUploadService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.bornfire.recon.services.FatcaManagementService fatcaService;

    // =========================================================================
    // SOURCE DATA UPLOAD
    // =========================================================================
    @PostMapping(value = "SourceDataUpload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sourceDataUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            HttpServletRequest rq) {

        Map<String, Object> response = new HashMap<>();
        String userId = (String) rq.getSession().getAttribute("USERID");

        try {
            Map<String, Object> uploadResult = sourceDataUploadService.uploadTransactionFile(file, type, userId);
            int currentTotalRows = fetchLiveRowCount(type);

            response.put("status", "SUCCESS");
            response.put("message", "File upload processed successfully!");
            response.put("type", type);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", (file.getSize() / 1024) + " KB");
            response.put("insertedRows", uploadResult.get("insertedRows"));
            response.put("failedRows", uploadResult.get("failedRows"));
            response.put("totalRowsInDb", currentTotalRows);
            return ResponseEntity.ok(response);
        } catch (Throwable e) {
            logger.error("UPLOAD ERROR DETAILS:", e);
            response.put("status", "ERROR");
            response.put("message", e.getMessage() != null ? e.getMessage() : "Upload processing failed");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/getSourceDataCounts")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getSourceDataCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("CBS", fetchLiveRowCount("CBS"));
        counts.put("ATM", fetchLiveRowCount("ATM"));
        counts.put("POS", fetchLiveRowCount("POS"));
        counts.put("EPIN", fetchLiveRowCount("EPIN"));
        counts.put("VISANET", fetchLiveRowCount("VISANET"));
        return ResponseEntity.ok(counts);
    }

    private int fetchLiveRowCount(String type) {
        String table;
        if ("ATM".equalsIgnoreCase(type)) {
            table = "BRECON.SWITCH_ATM";
        } else if ("POS".equalsIgnoreCase(type)) {
            table = "BRECON.SWITCH_POS";
        } else if ("CBS".equalsIgnoreCase(type)) {
            table = "BRECON.CBS_TXN";
        } else if ("EPIN".equalsIgnoreCase(type)) {
            table = "BRECON.VISA_EPIN";
        } else if ("VISANET".equalsIgnoreCase(type) || "EPIN745".equalsIgnoreCase(type) || "EP745".equalsIgnoreCase(type)) {
            table = "BRECON.VISA_EPIN745";
        } else {
            return 0;
        }

        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
            return count != null ? count : 0;
        } catch (Exception ex) {
            logger.warn("Table {} count unavailable: {}", table, ex.getMessage());
            return 0;
        }
    }

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

    // =========================================================================
    // FAILED RECORDS QUEUE & IN-PLACE SUBMISSION
    // =========================================================================
    @GetMapping("/getUploadFailedRecords")
    @ResponseBody
    public List<Map<String, Object>> getUploadFailedRecords(@RequestParam("sourceType") String sourceType) {
        String sql = "SELECT FAIL_ID, SOURCE_TYPE, RAW_CARD_NO, RAW_RRN, RAW_AUTH_ID, RAW_AMOUNT, RAW_TRAN_DATE, ERROR_REASON " +
                     "FROM BRECON.UPLOAD_FAILED_RECORDS " +
                     "WHERE UPPER(SOURCE_TYPE) = UPPER(?) AND STATUS = 'PENDING' " +
                     "ORDER BY FAIL_ID DESC";
        return jdbcTemplate.queryForList(sql, sourceType);
    }

    @PostMapping("/submitCorrectedRecord")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitCorrectedRecord(
            @RequestParam("failId") Long failId,
            @RequestParam("sourceType") String sourceType,
            @RequestParam("cardNo") String cardNo,
            @RequestParam("rrn") String rrn,
            @RequestParam("authId") String authId,
            @RequestParam("amount") String amount,
            @RequestParam("tranDate") String tranDate,
            @RequestParam(value = "authxTyp", required = false, defaultValue = "210") String authxTyp) {

        Map<String, Object> response = new HashMap<>();

        String cleanCard   = (cardNo != null) ? cardNo.trim() : "";
        String cleanRrn    = (rrn != null) ? rrn.trim() : "";
        String cleanAuthId = (authId != null) ? authId.trim() : "";
        String cleanAmtStr = (amount != null) ? amount.trim().replace(",", "") : "0";
        String cleanDate   = (tranDate != null) ? tranDate.trim() : "";
        String cleanType   = (authxTyp != null && !authxTyp.trim().isEmpty()) ? authxTyp.trim() : "210";

        if (cleanCard.length() < 13 || cleanCard.length() > 19 || !cleanCard.matches("^[0-9xX]+$")) {
            response.put("status", "ERROR");
            response.put("message", "Validation Error: Card number must be 13-19 characters (digits or masked x/X).");
            return ResponseEntity.badRequest().body(response);
        }

        BigDecimal numericAmount;
        try {
            numericAmount = new BigDecimal(cleanAmtStr);
            if (numericAmount.compareTo(BigDecimal.ZERO) < 0) {
                response.put("status", "ERROR");
                response.put("message", "Validation Error: Amount cannot be negative.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception ex) {
            response.put("status", "ERROR");
            response.put("message", "Validation Error: Invalid Amount format (" + cleanAmtStr + ").");
            return ResponseEntity.badRequest().body(response);
        }

        if (cleanDate.isEmpty()) {
            response.put("status", "ERROR");
            response.put("message", "Validation Error: Transaction Date is mandatory.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            int inserted = 0;
            String typeUpper = sourceType.trim().toUpperCase();

            switch (typeUpper) {
                case "ATM":
                    inserted = jdbcTemplate.update(
                        "INSERT INTO BRECON.SWITCH_ATM (" +
                        "  HEADX_CRD_CARD_CRD_NUM, AUTHX_SEQ_NUM, AUTHX_APPRV_CDE, AUTHX_AMT_1, TRAN_DT, AUTHX_TYP" +
                        ") SELECT ?, ?, ?, ?, ?, ? FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.SWITCH_ATM " +
                        "  WHERE AUTHX_SEQ_NUM = ? AND AUTHX_APPRV_CDE = ? AND TRAN_DT = ? AND AUTHX_TYP = ?" +
                        ")",
                        cleanCard, cleanRrn, cleanAuthId, numericAmount, cleanDate, cleanType,
                        cleanRrn, cleanAuthId, cleanDate, cleanType
                    );
                    break;

                case "POS":
                    inserted = jdbcTemplate.update(
                        "INSERT INTO BRECON.SWITCH_POS (" +
                        "  HEADX_CRD_CARD_CRD_NUM, AUTHX_SEQ_NUM, AUTHX_APPRV_CDE, AUTHX_AMT_1, TRAN_DT, AUTHX_TYP" +
                        ") SELECT ?, ?, ?, ?, ?, ? FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.SWITCH_POS " +
                        "  WHERE AUTHX_SEQ_NUM = ? AND AUTHX_APPRV_CDE = ? AND TRAN_DT = ? AND AUTHX_TYP = ?" +
                        ")",
                        cleanCard, cleanRrn, cleanAuthId, numericAmount, cleanDate, cleanType,
                        cleanRrn, cleanAuthId, cleanDate, cleanType
                    );
                    break;

                case "CBS":
                    inserted = jdbcTemplate.update(
                        "INSERT INTO BRECON.CBS_TXN (" +
                        "  CARD_NUMBER, RRN, AMOUNT, TRAN_DATE" +
                        ") SELECT ?, ?, ?, ? FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.CBS_TXN " +
                        "  WHERE RRN = ? AND CARD_NUMBER = ? AND TRAN_DATE = ?" +
                        ")",
                        cleanCard, cleanRrn, numericAmount.toPlainString(), cleanDate,
                        cleanRrn, cleanCard, cleanDate
                    );
                    break;

                case "EPIN":
                    inserted = jdbcTemplate.update(
                        "INSERT INTO BRECON.VISA_EPIN (" +
                        "  ACCT_NO, AUTH_CODE, DEST_AMT, PURCHASE_DATE" +
                        ") SELECT ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD') FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.VISA_EPIN " +
                        "  WHERE ACCT_NO = ? AND AUTH_CODE = ? AND DEST_AMT = ?" +
                        ")",
                        cleanCard, cleanAuthId, numericAmount, cleanDate,
                        cleanCard, cleanAuthId, numericAmount
                    );
                    break;

                case "VISANET":
                case "EP745":
                    inserted = jdbcTemplate.update(
                        "INSERT INTO BRECON.VISA_EPIN745 (" +
                        "  CARD_NUM, RET_REF_NUM, TRACE_NUM, TRAN_AMOUNT, TRAN_DATE" +
                        ") SELECT ?, ?, ?, ?, ? FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.VISA_EPIN745 " +
                        "  WHERE CARD_NUM = ? AND RET_REF_NUM = ? AND TRAN_DATE = ?" +
                        ")",
                        cleanCard, cleanRrn, cleanAuthId, numericAmount, cleanDate,
                        cleanCard, cleanRrn, cleanDate
                    );
                    break;

                default:
                    response.put("status", "ERROR");
                    response.put("message", "Unknown source type: " + sourceType);
                    return ResponseEntity.badRequest().body(response);
            }

            if (inserted == 0) {
                response.put("status", "DUPLICATE");
                response.put("message", "Duplicate Detected: A record with this unique key already exists in " + typeUpper + ".");
                return ResponseEntity.badRequest().body(response);
            }

            jdbcTemplate.update("UPDATE BRECON.UPLOAD_FAILED_RECORDS SET STATUS = 'RESOLVED' WHERE FAIL_ID = ?", failId);

            response.put("status", "SUCCESS");
            response.put("message", "Record validated and successfully inserted into " + typeUpper + " without duplicates.");
            return ResponseEntity.ok(response);

        } catch (org.springframework.dao.DuplicateKeyException dke) {
            response.put("status", "DUPLICATE");
            response.put("message", "Database Constraint Violation: Duplicate transaction key already exists.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Database Insertion Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
 // =========================================================================
    // 1. SINGLE RECORD DELETE (With explicit Transaction Commit)
    // =========================================================================
    @PostMapping("/deleteFailedRecord")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Map<String, Object>> deleteFailedRecord(@RequestParam("failId") Long failId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int rowsDeleted = jdbcTemplate.update(
                "DELETE FROM BRECON.UPLOAD_FAILED_RECORDS WHERE FAIL_ID = ?", failId
            );

            if (rowsDeleted > 0) {
                response.put("status", "SUCCESS");
                response.put("message", "Record deleted from failed queue successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Record not found or already deleted.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Error deleting failed record ID: {}", failId, e);
            response.put("status", "ERROR");
            response.put("message", "Database Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =========================================================================
    // 2. MULTIPLE RECORD BULK DELETE (With explicit Transaction Commit)
    // =========================================================================
    @PostMapping("/deleteMultipleFailedRecords")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Map<String, Object>> deleteMultipleFailedRecords(@RequestParam("failIds") List<Long> failIds) {
        Map<String, Object> response = new HashMap<>();

        if (failIds == null || failIds.isEmpty()) {
            response.put("status", "ERROR");
            response.put("message", "No records selected for deletion.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String placeholders = String.join(",", Collections.nCopies(failIds.size(), "?"));
            String sql = "DELETE FROM BRECON.UPLOAD_FAILED_RECORDS WHERE FAIL_ID IN (" + placeholders + ")";

            int rowsDeleted = jdbcTemplate.update(sql, failIds.toArray());

            response.put("status", "SUCCESS");
            response.put("deletedCount", rowsDeleted);
            response.put("message", rowsDeleted + " record(s) deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error bulk deleting records: {}", failIds, e);
            response.put("status", "ERROR");
            response.put("message", "Database Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =========================================================================
    // 3. DELETE ALL FAILED RECORDS (With explicit Transaction Commit)
    // =========================================================================
    @PostMapping("/deleteAllFailedRecords")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Map<String, Object>> deleteAllFailedRecords(@RequestParam("sourceType") String sourceType) {
        Map<String, Object> response = new HashMap<>();
        try {
            int rowsDeleted = jdbcTemplate.update(
                "DELETE FROM BRECON.UPLOAD_FAILED_RECORDS WHERE UPPER(SOURCE_TYPE) = UPPER(?) AND STATUS = 'PENDING'",
                sourceType
            );

            response.put("status", "SUCCESS");
            response.put("deletedCount", rowsDeleted);
            response.put("message", "All " + rowsDeleted + " record(s) deleted successfully for " + sourceType.toUpperCase() + ".");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting all failed records for type: {}", sourceType, e);
            response.put("status", "ERROR");
            response.put("message", "Database Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/downloadFailedRecordsExcel")
    public void downloadFailedRecordsExcel(
            @RequestParam("sourceType") String sourceType,
            @RequestParam(value = "failIds", required = false) List<Long> failIds,
            HttpServletResponse response) {

        String sql;
        List<Map<String, Object>> failedList;

        if (failIds != null && !failIds.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(failIds.size(), "?"));
            sql = "SELECT FAIL_ID, SOURCE_TYPE, RAW_CARD_NO, RAW_RRN, RAW_AUTH_ID, RAW_AMOUNT, RAW_TRAN_DATE, ERROR_REASON, STATUS, CREATED_AT " +
                  "FROM BRECON.UPLOAD_FAILED_RECORDS " +
                  "WHERE FAIL_ID IN (" + placeholders + ") ORDER BY FAIL_ID DESC";
            failedList = jdbcTemplate.queryForList(sql, failIds.toArray());
        } else {
            sql = "SELECT FAIL_ID, SOURCE_TYPE, RAW_CARD_NO, RAW_RRN, RAW_AUTH_ID, RAW_AMOUNT, RAW_TRAN_DATE, ERROR_REASON, STATUS, CREATED_AT " +
                  "FROM BRECON.UPLOAD_FAILED_RECORDS " +
                  "WHERE UPPER(SOURCE_TYPE) = UPPER(?) AND STATUS = 'PENDING' ORDER BY FAIL_ID DESC";
            failedList = jdbcTemplate.queryForList(sql, sourceType);
        }

        try (org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("REJECTED_RECORDS");

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.RED.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            String[] headers = {
                "Fail ID", "Source Type", "Card Number", "RRN", "Auth ID", 
                "Amount", "Transaction Date", "Error Reason", "Status", "Reported Time"
            };

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Map<String, Object> r : failedList) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(r.get("FAIL_ID") != null ? r.get("FAIL_ID") : ""));
                row.createCell(1).setCellValue(String.valueOf(r.get("SOURCE_TYPE") != null ? r.get("SOURCE_TYPE") : ""));
                row.createCell(2).setCellValue(String.valueOf(r.get("RAW_CARD_NO") != null ? r.get("RAW_CARD_NO") : ""));
                row.createCell(3).setCellValue(String.valueOf(r.get("RAW_RRN") != null ? r.get("RAW_RRN") : ""));
                row.createCell(4).setCellValue(String.valueOf(r.get("RAW_AUTH_ID") != null ? r.get("RAW_AUTH_ID") : ""));
                row.createCell(5).setCellValue(String.valueOf(r.get("RAW_AMOUNT") != null ? r.get("RAW_AMOUNT") : ""));
                row.createCell(6).setCellValue(String.valueOf(r.get("RAW_TRAN_DATE") != null ? r.get("RAW_TRAN_DATE") : ""));
                row.createCell(7).setCellValue(String.valueOf(r.get("ERROR_REASON") != null ? r.get("ERROR_REASON") : ""));
                row.createCell(8).setCellValue(String.valueOf(r.get("STATUS") != null ? r.get("STATUS") : ""));
                row.createCell(9).setCellValue(String.valueOf(r.get("CREATED_AT") != null ? r.get("CREATED_AT") : ""));
            }

            String fileName = "FAILED_RECORDS_" + sourceType.toUpperCase() + "_" + System.currentTimeMillis() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error generating failed records Excel export", e);
        }
    }
    


    // =========================================================================
    // VISA RECONCILIATION & REPORTS
    // =========================================================================
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

    @GetMapping("/getReconAnalytics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReconAnalytics() {
        Map<String, Object> data = new HashMap<>();
        data.put("atmCount", getSafeCount("BRECON.SWITCH_ATM"));
        data.put("posCount", getSafeCount("BRECON.SWITCH_POS"));
        data.put("cbsCount", getSafeCount("BRECON.CBS_TXN"));
        data.put("epinCount", getSafeCount("BRECON.VISA_EPIN"));

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
            logger.warn("Warning querying RECON_TXN status: {}", e.getMessage());
        }

        data.put("settled", settled);
        data.put("failedRefund", failedRefund);
        data.put("pending", pending);
        data.put("totalRecon", (settled + failedRefund + pending));

        List<Map<String, Object>> exceptions = new ArrayList<>();
        try {
            exceptions = jdbcTemplate.queryForList(
                "SELECT CARD_NUMBER, RRN, AUTH_ID, AMOUNT, TRAN_DT, RECON_STATUS, REASON " +
                "FROM BRECON.RECON_TXN " +
                "WHERE RECON_STATUS IN ('FAILED_REFUND', 'PENDING_INVESTIGATION') " +
                "AND ROWNUM <= 10"
                
            );
        } catch (Exception e) {
            logger.warn("Warning querying RECON_TXN exceptions: {}", e.getMessage());
        }
        data.put("exceptions", exceptions);
        return ResponseEntity.ok(data);
    }

    private int getSafeCount(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            return count != null ? count : 0;
        } catch (Exception ex) {
            return 0;
        }
    }
    
    
 // =========================================================================
    // EPIN & VISANET (EP745) DOWNLOAD / EXPORT ENDPOINTS
    // =========================================================================

    @GetMapping("/downloadEpinReport")
    public void downloadEpinReport(HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=EPIN_Report_" + System.currentTimeMillis() + ".TXT");
        
        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT * FROM BRECON.VISA_EPIN");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("CARD_NO|RRN|AUTH_ID|AMOUNT|TRAN_DATE");
            for (Map<String, Object> r : records) {
                writer.println(
                    r.getOrDefault("CARD_NO", "") + "|" +
                    r.getOrDefault("RRN", "") + "|" +
                    r.getOrDefault("AUTH_ID", "") + "|" +
                    r.getOrDefault("AMOUNT", "") + "|" +
                    r.getOrDefault("TRAN_DATE", "")
                );
            }
            writer.flush();
        }
    }

    @GetMapping("/downloadVisanetReport")
    public void downloadVisanetReport(HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=VisaNet_EP745_Report_" + System.currentTimeMillis() + ".TXT");
        
        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT * FROM BRECON.VISA_EPIN745");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("CARD_NO|RRN|AUTH_ID|AMOUNT|SETT_DATE");
            for (Map<String, Object> r : records) {
                writer.println(
                    r.getOrDefault("CARD_NO", "") + "|" +
                    r.getOrDefault("RRN", "") + "|" +
                    r.getOrDefault("AUTH_ID", "") + "|" +
                    r.getOrDefault("AMOUNT", "") + "|" +
                    r.getOrDefault("SETT_DATE", "")
                );
            }
            writer.flush();
        }
    }

 
    
 // =========================================================================
    // FATCA REPORTING ENDPOINTS (MIRRORS CRS WORKFLOW)
    // =========================================================================

    @GetMapping("/getFatcaDashboardData")
    @ResponseBody
    public Map<String, Object> getFatcaDashboardData() {
        Map<String, Object> data = new HashMap<>();
        try {
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT TRN_ID AS REF_NO, BRN, RES_COUNTRY_CODE, PASSPORT_NO, SURNAME, " +
                "       OTHER_NAME, FOREIGN_AMOUNT, FOREIGN_CURR, TXN_TYPE, AMOUNT_MUR, " +
                "       NVL(RECORD_STATUS, 'COMPLETED') AS STATUS, " +
                "       TO_CHAR(ENTRY_TIME, 'YYYY-MM-DD HH24:MI:SS') AS UPLOAD_DATE " +
                "FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'COMPLETED') <> 'DUPLICATE' " +
                "ORDER BY ENTRY_TIME DESC"
            );

            data.put("records", records);
            data.put("totalRecords", records.size());
            data.put("pendingRecords", records.stream()
                    .filter(r -> "PENDING".equalsIgnoreCase(String.valueOf(r.get("STATUS"))))
                    .count());
            data.put("completedRecords", records.stream()
                    .filter(r -> "COMPLETED".equalsIgnoreCase(String.valueOf(r.get("STATUS"))))
                    .count());
            data.put("errorRecords", records.stream()
                    .filter(r -> "ERROR".equalsIgnoreCase(String.valueOf(r.get("STATUS"))) 
                            || "REJECTED".equalsIgnoreCase(String.valueOf(r.get("STATUS"))))
                    .count());

            return data;
        } catch (Exception e) {
            logger.error("Error fetching FATCA Dashboard Data", e);
            data.put("records", Collections.emptyList());
            data.put("totalRecords", 0);
            data.put("pendingRecords", 0);
            data.put("completedRecords", 0);
            data.put("errorRecords", 0);
            return data;
        }
    }

    @PostMapping("/uploadFatcaFile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFatcaFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Map<String, Object> res = new HashMap<>();
        String userId = "SYSTEM";
        if (request != null && request.getSession(false) != null) {
            Object userObj = request.getSession(false).getAttribute("USERID");
            if (userObj != null && !userObj.toString().trim().isEmpty()) {
                userId = userObj.toString().trim();
            }
        }

        if (file == null || file.isEmpty()) {
            res.put("status", "ERROR");
            res.put("message", "Please select a valid non-empty file to upload.");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            Map<String, Object> uploadResult = fatcaService.uploadFatcaFile(file, userId);
            int inserted = (int) uploadResult.getOrDefault("insertedRows", 0);
            res.put("status", "SUCCESS");
            res.put("insertedRows", inserted);
            res.put("message", "Uploaded and processed " + inserted + " record(s) into BRECON.FACTA_TRANSCATION_DATA.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("FATCA file upload failed", e);
            res.put("status", "ERROR");
            res.put("message", e.getMessage() != null ? e.getMessage() : "Error processing FATCA upload.");
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/updateFatcaRecord")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Map<String, Object>> updateFatcaRecord(
            @RequestParam("refNo") String refNo,
            @RequestParam("status") String status,
            HttpServletRequest request) {

        Map<String, Object> res = new HashMap<>();

        if (refNo == null || refNo.trim().isEmpty() || status == null || status.trim().isEmpty()) {
            res.put("status", "ERROR");
            res.put("message", "Invalid Reference ID or Status provided.");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            int updated = jdbcTemplate.update(
                "UPDATE BRECON.FACTA_TRANSCATION_DATA SET RECORD_STATUS = ? WHERE TRN_ID = ? AND NVL(DEL_FLAG, 'N') <> 'Y'", 
                status.trim().toUpperCase(), refNo
            );

            res.put("status", updated > 0 ? "SUCCESS" : "ERROR");
            res.put("message", updated > 0 ? "Record updated successfully." : "Record with Ref No " + refNo + " not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error updating FATCA record Ref No: {}", refNo, e);
            res.put("status", "ERROR");
            res.put("message", "Database Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/getFatcaRecordDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFatcaRecordDetails(@RequestParam("refNo") String refNo) {
        try {
            Map<String, Object> data = fatcaService.getFatcaRecordDetails(refNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/downloadFatcaXml")
    public void downloadFatcaXml(HttpServletResponse response) {
        try {
            String xmlContent = fatcaService.generateFatcaXml();
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=\"FATCA_REPORT_" + System.currentTimeMillis() + ".xml\"");
            response.getOutputStream().write(xmlContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error generating master FATCA XML", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate FATCA XML: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    @GetMapping("/downloadSingleFatcaXml")
    public ResponseEntity<?> downloadSingleFatcaXml(@RequestParam("refNo") String refNo) {
        try {
            String xmlContent = fatcaService.generateSingleFatcaXml(refNo);
            byte[] xmlBytes = xmlContent.getBytes(StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"FATCA_" + refNo + ".xml\"")
                    .body(xmlBytes);
        } catch (Exception e) {
            logger.error("Error downloading individual FATCA XML for Ref No {}", refNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate single FATCA XML: " + e.getMessage());
        }
    }

    // =========================================================================
    // FATCA DUPLICATE RECTIFICATION & QUEUE ENDPOINTS
    // =========================================================================

    @GetMapping("/getFatcaDuplicateRecords")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFatcaDuplicateRecords() {
        Map<String, Object> res = new HashMap<>();
        try {
            List<Map<String, Object>> records = fatcaService.getFatcaDuplicateRecords();
            res.put("status", "SUCCESS");
            res.put("records", records);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error fetching FATCA duplicate records", e);
            res.put("status", "ERROR");
            res.put("records", Collections.emptyList());
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/rectifyFatcaDuplicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rectifyFatcaDuplicate(
            @RequestParam("dupId") String dupId,
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("firstName") String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam("countryCode") String countryCode,
            @RequestParam("tin") String tin,
            @RequestParam("accountBalance") BigDecimal accountBalance,
            @RequestParam("currencyCode") String currencyCode,
            @RequestParam("status") String status) {
        
        Map<String, Object> res = new HashMap<>();
        try {
            boolean success = fatcaService.rectifyFatcaDuplicate(dupId, accountNumber, firstName, lastName, countryCode, tin, accountBalance, currencyCode, status);
            if (success) {
                res.put("status", "SUCCESS");
                res.put("message", "Duplicate record rectified and promoted successfully.");
            } else {
                res.put("status", "ERROR");
                res.put("message", "Record not found or update failed.");
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error rectifying FATCA duplicate ID: {}", dupId, e);
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/deleteFatcaDuplicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFatcaDuplicate(@RequestParam("dupId") String dupId) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean success = fatcaService.deleteFatcaDuplicate(dupId);
            if (success) {
                res.put("status", "SUCCESS");
                res.put("message", "Duplicate record deleted successfully.");
            } else {
                res.put("status", "ERROR");
                res.put("message", "Record not found.");
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error deleting FATCA duplicate ID: {}", dupId, e);
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/deleteAllFatcaDuplicates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAllFatcaDuplicates() {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean success = fatcaService.deleteAllFatcaDuplicates();
            res.put("status", "SUCCESS");
            res.put("message", "All duplicate records cleared successfully.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error clearing all FATCA duplicates", e);
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

   

   

    @Autowired
    private com.bornfire.recon.services.CrsManagementService crsService;

    // =========================================================================
    // 1. CRS DASHBOARD & LIVE DATA (ALIGNED TO REAL BRECON.CRS_REPORT_DATA SCHEMA)
    // =========================================================================

    @GetMapping("/getCrsDashboardData")
    @ResponseBody
    public Map<String, Object> getCrsDashboardData(
            @RequestParam(value = "reportingYear", required = false, defaultValue = "2025") String reportingYear) {
        Map<String, Object> data = new HashMap<>();
        try {
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT REPORT_TYPE, REF_ID, CRS_CODE, ACCOUNT_NUMBER, IBAN, " +
                "       CURRENCY_CODE, ACCOUNT_BALANCE, INTEREST_PAID, IS_DORMANT, " +
                "       IS_CLOSED, IS_UNDOCUMENTED, CUSTOMER_ID, ENTITY_NAME, " +
                "       FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, NATIONALITY, TAX_COUNTRY, " +
                "       TIN, ADDRESS, CITY, COUNTRY, ENTRY_USER " +
                "FROM BRECON.CRS_REPORT_DATA ORDER BY ACCOUNT_NUMBER ASC"
            );
            
            data.put("records", records);
            data.put("totalCount", records.size());
            data.put("completedCount", records.size());
            data.put("pendingCount", 0);
            data.put("errorCount", 0);
            return data;
        } catch (Exception e) {
            logger.error("Error fetching CRS Dashboard Data", e);
            data.put("records", Collections.emptyList());
            data.put("totalCount", 0);
            data.put("completedCount", 0);
            data.put("pendingCount", 0);
            data.put("errorCount", 0);
            return data;
        }
    }

    // =========================================================================
    // 2. INGESTION / FILE UPLOAD
    // =========================================================================

    @PostMapping("/uploadCrsFile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadCrsFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Map<String, Object> res = new HashMap<>();
        String userId = "SYSTEM";
        if (request != null && request.getSession(false) != null) {
            Object userObj = request.getSession(false).getAttribute("USERID");
            if (userObj != null && !userObj.toString().trim().isEmpty()) {
                userId = userObj.toString().trim();
            }
        }

        if (file == null || file.isEmpty()) {
            res.put("status", "ERROR");
            res.put("message", "Please select a valid non-empty file to upload.");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            int inserted = crsService.uploadCrsFile(file, userId);
            res.put("status", "SUCCESS");
            res.put("insertedRows", inserted);
            res.put("message", "Uploaded and processed " + inserted + " record(s) into BRECON.CRS_REPORT_DATA.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("CRS file upload failed", e);
            res.put("status", "ERROR");
            res.put("message", e.getMessage() != null ? e.getMessage() : "Error processing CRS upload.");
            return ResponseEntity.badRequest().body(res);
        }
    }

    // =========================================================================
    // 3. DEDICATED XML DOWNLOAD ENDPOINTS (INDIVIDUAL, CRS101, CRS102, MASTER)
    // =========================================================================

    /**
     * Download Individual XML strictly based on ind.xml structure[cite: 5]
     */
    @GetMapping("/downloadIndividualCrsXml")
    public void downloadIndividualCrsXml(
            @RequestParam(value = "year", required = false, defaultValue = "2025") String year,
            HttpServletResponse response) {
        streamXmlToResponse(
            response, 
            crsService.generateIndividualCrsXml(year), 
            "CRS_INDIVIDUAL_" + year + "_" + System.currentTimeMillis() + ".xml"
        );
    }

    /**
     * Download CRS101 XML (Passive NFE with Controlling Person) based on CRS101 corp.docx[cite: 6]
     */
    @GetMapping("/downloadCrs101Xml")
    public void downloadCrs101Xml(
            @RequestParam(value = "year", required = false, defaultValue = "2025") String year,
            HttpServletResponse response) {
        streamXmlToResponse(
            response, 
            crsService.generateCrs101Xml(year), 
            "CRS_101_" + year + "_" + System.currentTimeMillis() + ".xml"
        );
    }

    /**
     * Download CRS102 XML (Active NFE / Corporate without CP) based on 102corp_2.xml[cite: 7]
     */
    @GetMapping("/downloadCrs102Xml")
    public void downloadCrs102Xml(
            @RequestParam(value = "year", required = false, defaultValue = "2025") String year,
            HttpServletResponse response) {
        streamXmlToResponse(
            response, 
            crsService.generateCrs102Xml(year), 
            "CRS_102_" + year + "_" + System.currentTimeMillis() + ".xml"
        );
    }

    /**
     * Parameterized download route: Pass ?type=INDIVIDUAL, ?type=CRS101, or ?type=CRS102
     */
    @GetMapping("/downloadCrsXmlByType")
    public void downloadCrsXmlByType(
            @RequestParam(value = "year", required = false, defaultValue = "2025") String year,
            @RequestParam(value = "type", required = false, defaultValue = "ALL") String type,
            HttpServletResponse response) {
        String cleanType = (type != null && !type.trim().isEmpty()) ? type.toUpperCase() : "ALL";
        String xmlContent = crsService.generateCrsXmlByType(year, cleanType);
        streamXmlToResponse(
            response, 
            xmlContent, 
            "CRS_" + cleanType + "_" + year + "_" + System.currentTimeMillis() + ".xml"
        );
    }

    /**
     * Master XML Download (All reporting categories in one file)
     */
    @GetMapping("/downloadCrsXml")
    public void downloadCrsXml(
            @RequestParam(value = "year", required = false, defaultValue = "2025") String year,
            HttpServletResponse response) {
        streamXmlToResponse(
            response, 
            crsService.generateCrsXml(year), 
            "CRS_MASTER_" + year + "_" + System.currentTimeMillis() + ".xml"
        );
    }

    /**
     * Single Record XML download by Account Number
     */
    @GetMapping("/downloadSingleCrsXml")
    public void downloadSingleCrsXml(
            @RequestParam(value = "accountNumber", required = false) String accountNumber,
            @RequestParam(value = "refId", required = false) String refId,
            @RequestParam(value = "reportId", required = false) String reportId,
            HttpServletResponse response) {
        try {
            // Determine the identifier passed
            String identifier = (accountNumber != null && !accountNumber.trim().isEmpty()) ? accountNumber :
                                (refId != null && !refId.trim().isEmpty()) ? refId : reportId;

            if (identifier == null || identifier.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing account identifier.");
                return;
            }

            String xmlContent = crsService.generateSingleCrsXml(identifier.trim());
            
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=\"CRS_REC_" + identifier.trim() + ".xml\"");
            
            response.getOutputStream().write(xmlContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error downloading individual CRS XML for identifier: {}", accountNumber, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate single CRS XML: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    private void streamXmlToResponse(HttpServletResponse response, String xmlContent, String filename) {
        try {
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.getOutputStream().write(xmlContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error streaming XML file {}", filename, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating download: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    // =========================================================================
    // 4. RECORD DETAILS & RECORD MODIFICATION (SCHEMA-COMPLIANT)
    // =========================================================================

    @PostMapping("/saveCrsRecord")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Map<String, Object>> saveCrsRecord(
            @RequestParam(value = "dupId", required = false) Long dupId,
            @RequestParam("docRefId") String docRefId,
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("reportType") String reportType,
            @RequestParam(value = "crsCode", required = false, defaultValue = "CRS102") String crsCode,
            @RequestParam("tin") String tin,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "entityName", required = false) String entityName,
            @RequestParam("taxCountry") String taxCountry,
            @RequestParam("city") String city,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "accountBalance", defaultValue = "0.0") Double accountBalance,
            @RequestParam(value = "currencyCode", defaultValue = "MUR") String currencyCode) {

        Map<String, Object> res = new HashMap<>();

        try {
            // Check if record exists by REF_ID in the main reporting table
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM BRECON.CRS_REPORT_DATA WHERE REF_ID = ?", 
                Integer.class, docRefId.trim()
            );

            if (count != null && count > 0) {
                // 1. UPDATE EXISTING RECORD
                jdbcTemplate.update(
                    "UPDATE BRECON.CRS_REPORT_DATA SET " +
                    "  REPORT_TYPE = ?, CRS_CODE = ?, ACCOUNT_NUMBER = ?, IBAN = ?, CURRENCY_CODE = ?, " +
                    "  ACCOUNT_BALANCE = ?, ENTITY_NAME = ?, FIRST_NAME = ?, LAST_NAME = ?, " +
                    "  TAX_COUNTRY = ?, TIN = ?, ADDRESS = ?, CITY = ?, COUNTRY = ? " +
                    "WHERE REF_ID = ?",
                    reportType.toUpperCase(), crsCode, accountNumber, accountNumber, currencyCode.toUpperCase(),
                    accountBalance, entityName, firstName, lastName,
                    taxCountry.toUpperCase(), tin, address, city, taxCountry.toUpperCase(),
                    docRefId.trim()
                );
            } else {
                // 2. INSERT RECTIFIED DUPLICATE INTO MAIN REPORT DATA
                jdbcTemplate.update(
                    "INSERT INTO BRECON.CRS_REPORT_DATA (" +
                    "  REPORT_TYPE, REF_ID, CRS_CODE, ACCOUNT_NUMBER, IBAN, CURRENCY_CODE, " +
                    "  ACCOUNT_BALANCE, INTEREST_PAID, IS_DORMANT, IS_CLOSED, IS_UNDOCUMENTED, " +
                    "  CUSTOMER_ID, ENTITY_NAME, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, " +
                    "  NATIONALITY, TAX_COUNTRY, TIN, ADDRESS, CITY, COUNTRY, " +
                    "  CREATED_DATE, ENTRY_USER " +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, 0, 'N', 'N', 'N', NULL, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, SYSDATE, 'RECTIFIED')",
                    reportType.toUpperCase(), docRefId.trim(), crsCode, accountNumber, accountNumber, currencyCode.toUpperCase(),
                    accountBalance, entityName, firstName, lastName,
                    taxCountry.toUpperCase(), taxCountry.toUpperCase(), tin, address, city, taxCountry.toUpperCase()
                );
            }

            // If this was a duplicate from the queue, mark it resolved
            if (dupId != null && dupId > 0) {
                jdbcTemplate.update("UPDATE BRECON.CRS_DUPLICATE_RECORDS SET STATUS = 'RESOLVED' WHERE DUP_ID = ?", dupId);
            }

            res.put("status", "SUCCESS");
            res.put("message", "Record saved successfully and reflected in reporting table.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error saving CRS record DocRefId: {}", docRefId, e);
            res.put("status", "ERROR");
            res.put("message", "Database Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/getCrsRecordDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCrsRecordDetails(@RequestParam("accountNumber") String accountNumber) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE ACCOUNT_NUMBER = ?", accountNumber
            );

            if (records == null || records.isEmpty()) {
                records = jdbcTemplate.queryForList(
                    "SELECT DUP_ID AS REPORT_ID, ACCOUNT_NUMBER, RESIDENCE_COUNTRY AS TAX_COUNTRY, " +
                    "       TIN, HOLDER_NAME AS ENTITY_NAME, ACCOUNT_BALANCE, CURRENCY_CODE, " +
                    "       STATUS FROM BRECON.CRS_DUPLICATE_RECORDS WHERE ACCOUNT_NUMBER = ?", accountNumber
                );
            }

            if (records != null && !records.isEmpty()) {
                res.put("status", "SUCCESS");
                res.put("record", records.get(0));
                return ResponseEntity.ok(res);
            } else {
                res.put("status", "ERROR");
                res.put("message", "Record not found with Account Number: " + accountNumber);
                return ResponseEntity.badRequest().body(res);
            }
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // =========================================================================
    // 5. DUPLICATE EXCEPTION QUEUE MANAGEMENT
    // =========================================================================

    @PostMapping("/rectifyCrsDuplicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rectifyCrsDuplicate(
            @RequestParam("dupId") Long dupId,
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("reportType") String reportType,
            @RequestParam("taxCountry") String taxCountry,
            @RequestParam(value = "tin", required = false) String tin,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "entityName", required = false) String entityName,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "accountBalance", defaultValue = "0.0") Double accountBalance,
            @RequestParam(value = "currencyCode", defaultValue = "MUR") String currencyCode,
            @RequestParam(value = "crsCode", defaultValue = "CRS102") String crsCode,
            @RequestParam(value = "docRefId", required = false) String docRefId) {

        Map<String, Object> res = new HashMap<>();
        try {
            boolean success = crsService.rectifyDuplicateRecord(
                dupId, accountNumber, reportType, taxCountry, tin,
                firstName, lastName, entityName, address, city,
                accountBalance, currencyCode, crsCode, docRefId
            );

            if (success) {
                res.put("status", "SUCCESS");
                res.put("message", "Duplicate rectified and successfully promoted to CRS Reportable Accounts List.");
                return ResponseEntity.ok(res);
            } else {
                res.put("status", "ERROR");
                res.put("message", "Failed to rectify duplicate record. Please ensure unique DocRefId.");
                return ResponseEntity.badRequest().body(res);
            }
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    
    @GetMapping("/getCrsDuplicateRecords")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCrsDuplicateRecords() {
        Map<String, Object> res = new HashMap<>();
        try {
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT * FROM BRECON.CRS_DUPLICATE_RECORDS WHERE STATUS = 'PENDING' ORDER BY DUP_ID DESC"
            );
            res.put("status", "SUCCESS");
            res.put("records", records);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    
    @PostMapping("/deleteCrsDuplicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCrsDuplicate(@RequestParam("dupId") Long dupId) {
        Map<String, Object> res = new HashMap<>();
        try {
            int deleted = jdbcTemplate.update("DELETE FROM BRECON.CRS_DUPLICATE_RECORDS WHERE DUP_ID = ?", dupId);
            if (deleted > 0) {
                res.put("status", "SUCCESS");
                res.put("message", "Duplicate record deleted successfully.");
            } else {
                res.put("status", "ERROR");
                res.put("message", "Record not found.");
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    
    @PostMapping("/deleteAllCrsDuplicates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAllCrsDuplicates() {
        Map<String, Object> res = new HashMap<>();
        try {
            jdbcTemplate.update("DELETE FROM BRECON.CRS_DUPLICATE_RECORDS");
            res.put("status", "SUCCESS");
            res.put("message", "All duplicate records cleared successfully.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    //
    

    @PostMapping(value = "CrFileUpload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> UploadExcel(@RequestParam("file") MultipartFile[] file, @RequestParam("fileInput") String fileInput,
    @RequestParam("overwrite") Boolean overwrite ,Model md, HttpServletRequest rq ,@RequestParam("Fromdate") String Fromdate )
    throws FileNotFoundException, SQLException, IOException, NullPointerException {
        String userID = (String) rq.getSession().getAttribute("USERID");
        String USERNAME = (String) rq.getSession().getAttribute("USERNAME");
        Map<String, Object> resultMap = new LinkedHashMap<>();
        if (fileInput.equalsIgnoreCase("SOURCE")) {
            resultMap = cruploadService.saveCrSourceFile(fileInput, file, userID, USERNAME ,overwrite ,Fromdate);
        } else if (fileInput.equalsIgnoreCase("DESTINATION")) {
            resultMap = cruploadService.saveCrDestinationFile(fileInput, file, userID, USERNAME,overwrite, Fromdate);
        } else {
            resultMap.put("message", "Invalid file type specified");
        }
        return ResponseEntity.ok(resultMap);
    }

    @PostMapping(value = "DrFileUpload")
    @ResponseBody
    public Map<String, Object> DrFileUpload(@RequestParam("file") MultipartFile[] files, @RequestParam("fileInput") String fileInput,@RequestParam("fromDate") String fromDate,
            boolean overwrite,HttpServletRequest rq)throws SQLException, IOException {

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
    public Map<String, Object> UpiFileUpload(@RequestParam("file") MultipartFile[] files, @RequestParam("fileInput") String fileInput,@RequestParam("fromDate") String fromDate,
            boolean overwrite,HttpServletRequest rq)throws SQLException, IOException {

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

        List<Object[]> destData;
        List<Object[]> sourceData;
        List<Object[]> matchedData;

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

        byte[] excelBytes = processdownloadService.generateReconCombinedExcel(destData, sourceData, matchedData,
                recon_process_date, type);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Recon_Report_" + type + "_" + recon_process_date + ".xlsx");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }
    
   
    @Autowired
    private com.bornfire.recon.services.MraManagementService mraService;

    @GetMapping("/getMraDashboardData")
    @ResponseBody
    public Map<String, Object> getMraDashboardData(
            @RequestParam("type") String type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status) {
        try {
            return mraService.getMraDashboardData(type, page, size, search, status);
        } catch (Exception e) {
            logger.error("Error fetching MRA dashboard data for {}", type, e);
            Map<String, Object> err = new HashMap<>();
            err.put("totalCount", 0);
            err.put("pendingCount", 0);
            err.put("completedCount", 0);
            err.put("errorCount", 0);
            err.put("filteredTotal", 0);
            err.put("records", Collections.emptyList());
            return err;
        }
    }

    @PostMapping("/uploadMraFile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadMraFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            HttpServletRequest request) {

        Map<String, Object> res = new HashMap<>();
        String userId = "SYSTEM";
        if (request != null && request.getSession(false) != null) {
            Object userObj = request.getSession(false).getAttribute("USERID");
            if (userObj != null) userId = userObj.toString().trim();
        }

        if (file == null || file.isEmpty()) {
            res.put("status", "ERROR");
            res.put("message", "Please select a valid non-empty file to upload.");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            int inserted = mraService.uploadMraFile(file, type, userId);
            res.put("status", "SUCCESS");
            res.put("insertedRows", inserted);
            res.put("message", "Successfully uploaded and parsed " + inserted + " record(s) into " + type.toUpperCase() + " table.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("MRA file upload failed for type {}", type, e);
            res.put("status", "ERROR");
            res.put("message", e.getMessage() != null ? e.getMessage() : "Error processing file upload.");
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/getMraDuplicateRecords")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMraDuplicateRecords(@RequestParam("type") String type) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<Map<String, Object>> records = mraService.getMraDuplicateRecords(type);
            res.put("status", "SUCCESS");
            res.put("records", records);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("records", Collections.emptyList());
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    
    @PostMapping("/rectifyMraDuplicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rectifyMraDuplicate(
            @RequestParam("type") String type,
            @RequestParam("dupId") Long dupId,
            @RequestParam("updatedBalance") Double updatedBalance,
            @RequestParam("updatedStatus") String updatedStatus) {
        
        Map<String, Object> res = new HashMap<>();
        try {
            boolean success = mraService.rectifyDuplicateRecord(type, dupId, updatedBalance, updatedStatus);
            res.put("status", success ? "SUCCESS" : "ERROR");
            res.put("message", success ? "Duplicate resolved and promoted successfully." : "Record not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/deleteMraDuplicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMraDuplicate(
            @RequestParam("type") String type,
            @RequestParam("dupId") Long dupId) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean success = mraService.deleteDuplicateRecord(type, dupId);
            res.put("status", success ? "SUCCESS" : "ERROR");
            res.put("message", success ? "Duplicate record deleted." : "Record not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/deleteAllMraDuplicates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAllMraDuplicates(@RequestParam("type") String type) {
        Map<String, Object> res = new HashMap<>();
        try {
            mraService.deleteAllDuplicateRecords(type);
            res.put("status", "SUCCESS");
            res.put("message", "All duplicate records cleared.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/updateMraRecord")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMraRecord(
            @RequestParam("type") String type,
            @RequestParam("reportId") Long reportId,
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("holderName") String holderName,
            @RequestParam("taxCountry") String taxCountry,
            @RequestParam("accountBalance") Double accountBalance,
            @RequestParam("currencyCode") String currencyCode,
            @RequestParam("status") String status) {

        Map<String, Object> res = new HashMap<>();
        try {
            boolean success = mraService.updateMraRecord(type, reportId, accountNumber, holderName, taxCountry, accountBalance, currencyCode, status);
            res.put("status", success ? "SUCCESS" : "ERROR");
            res.put("message", success ? "Record updated successfully." : "Record not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/getMraRecordDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMraRecordDetails(
            @RequestParam("type") String type,
            @RequestParam("reportId") Long reportId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Map<String, Object> data = mraService.getMraRecordDetails(type, reportId);
            res.put("status", "SUCCESS");
            res.put("record", data);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/downloadMraCsv")
    public void downloadMraCsv(
            @RequestParam("type") String type,
            HttpServletResponse response) {
        try {
            String csvContent = mraService.generateMraCsv(type);
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"MRA_" + type.toUpperCase() + "_" + System.currentTimeMillis() + ".csv\"");
            response.getOutputStream().write(csvContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error generating MRA CSV for type {}", type, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate CSV: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    @GetMapping("/downloadSingleMraCsv")
    public void downloadSingleMraCsv(
            @RequestParam("type") String type,
            @RequestParam("reportId") Long reportId,
            HttpServletResponse response) {
        try {
            String csvContent = mraService.generateSingleMraCsv(type, reportId);
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"MRA_" + type.toUpperCase() + "_" + reportId + ".csv\"");
            response.getOutputStream().write(csvContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error generating single MRA CSV", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate CSV: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }
    
    @GetMapping("/getMraDuplicateRecordDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMraDuplicateRecordDetails(
            @RequestParam("type") String type,
            @RequestParam("dupId") Long dupId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Map<String, Object> data = mraService.getMraDuplicateRecordDetails(type, dupId);
            res.put("status", "SUCCESS");
            res.put("record", data);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @Transactional
    public boolean rectifyDuplicateRecordFull(String type, Long dupId, Map<String, String> formParams) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String idCol = isSft ? "SFT_ID" : "SFX_ID";

        // Fallback: If dupId is null or 0, try to pull it from form parameters
        if ((dupId == null || dupId <= 0) && formParams.containsKey("dupId")) {
            try {
                dupId = Long.parseLong(formParams.get("dupId"));
            } catch (Exception ignored) {}
        }

        try {
            StringBuilder updateSql = new StringBuilder("UPDATE ").append(tableName)
                .append(" SET RECORD_STATUS = ?, ")
                .append("     MODIFY_TIME = SYSDATE, ")
                .append("     MODIFY_USER = 'SYSTEM', ")
                .append("     MODIFY_FLAG = 'M', ")
                .append("     ENTITY_FLAG = NVL(ENTITY_FLAG, 'A') ");

            List<Object> args = new ArrayList<>();
            // Set resolution status (Completed or Error/Discard)
            String resolutionStatus = formParams.getOrDefault("updatedStatus", "COMPLETED").toUpperCase();
            args.add(resolutionStatus);

            Set<String> excludedCols = new HashSet<>(Arrays.asList(
                "DUP_ID", "TYPE", "UPDATEDSTATUS", "SFX_ID", "SFT_ID", 
                "MODIFY_TIME", "MODIFY_USER", "VERIFY_TIME", "VERIFY_USER", 
                "MODIFY_FLAG", "ENTITY_FLAG", "ENTRY_TIME", "ENTRY_USER"
            ));

            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                String key = entry.getKey().toUpperCase();
                if (!excludedCols.contains(key) && entry.getValue() != null) {
                    updateSql.append(", \"").append(key).append("\" = ? ");
                    args.add(entry.getValue());
                }
            }

            updateSql.append(" WHERE ").append(idCol).append(" = ?");
            args.add(dupId);

            int updated = jdbcTemplate.update(updateSql.toString(), args.toArray());

            // Fallback check: If 0 rows updated, try matching by standard ID column
            if (updated == 0 && dupId != null) {
                updated = jdbcTemplate.update(
                    "UPDATE " + tableName + " SET RECORD_STATUS = ?, MODIFY_TIME = SYSDATE WHERE ID = ?",
                    resolutionStatus, dupId.toString()
                );
            }

            return updated > 0;
        } catch (Exception e) {
            logger.error("Error rectifying full MRA duplicate: {}", e.getMessage(), e);
            throw new RuntimeException("Database Update Failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/rectifyMraDuplicateFull")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rectifyMraDuplicateFull(@RequestParam Map<String, String> allParams) {
        Map<String, Object> res = new HashMap<>();
        try {
            String type = allParams.getOrDefault("type", "SFX");
            String dupIdStr = allParams.get("dupId");
            
            if (dupIdStr == null || dupIdStr.trim().isEmpty()) {
                res.put("status", "ERROR");
                res.put("message", "Error: Missing Record ID (dupId) for rectification.");
                return ResponseEntity.ok(res);
            }

            Long dupId = Long.parseLong(dupIdStr.trim());
            boolean success = mraService.rectifyDuplicateRecordFull(type, dupId, allParams);
            
            res.put("status", success ? "SUCCESS" : "ERROR");
            res.put("message", success ? "Record updated and stored to database successfully." : "Record not found in database table.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error in rectifyMraDuplicateFull", e);
            res.put("status", "ERROR");
            res.put("message", "Error: " + e.getMessage());
            return ResponseEntity.ok(res);
        }
    }
    
}