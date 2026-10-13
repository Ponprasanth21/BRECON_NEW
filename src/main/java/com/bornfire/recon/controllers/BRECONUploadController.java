package com.bornfire.recon.controllers;

import java.io.FileNotFoundException; 
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
            table = "BRECON.ATM_TRANSACTION_DATA";
        } else if ("POS".equalsIgnoreCase(type)) {
            table = "BRECON.POS_TRANSATION_DATA";
        } else if ("CBS".equalsIgnoreCase(type)) {
            table = "BRECON.CBS_TXN";
        } else if ("EPIN".equalsIgnoreCase(type)) {
            table = "BRECON.BRECON_CARD_EPIN_DATA_TABLE";
        } else if ("VISANET".equalsIgnoreCase(type) || "EPIN745".equalsIgnoreCase(type) || "EP745".equalsIgnoreCase(type)) {
            table = "BRECON.BRECON_CARD_EP745_DATA_TABLE";
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
                        "INSERT INTO BRECON.ATM_TRANSACTION_DATA (" +
                        "  HEADX_CRD_CARD_CRD_NUM, AUTHX_SEQ_NUM, AUTHX_APPRV_CDE, AUTHX_AMT_1, TRAN_DT, AUTHX_TYP" +
                        ") SELECT ?, ?, ?, ?, ?, ? FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.ATM_TRANSACTION_DATA " +
                        "  WHERE AUTHX_SEQ_NUM = ? AND AUTHX_APPRV_CDE = ? AND TRAN_DT = ? AND AUTHX_TYP = ?" +
                        ")",
                        cleanCard, cleanRrn, cleanAuthId, numericAmount, cleanDate, cleanType,
                        cleanRrn, cleanAuthId, cleanDate, cleanType
                    );
                    break;

                case "POS":
                    inserted = jdbcTemplate.update(
                        "INSERT INTO BRECON.POS_TRANSATION_DATA (" +
                        "  HEADX_CRD_CARD_CRD_NUM, AUTHX_SEQ_NUM, AUTHX_APPRV_CDE, AUTHX_AMT_1, TRAN_DT, AUTHX_TYP" +
                        ") SELECT ?, ?, ?, ?, ?, ? FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.POS_TRANSATION_DATA " +
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
                        "INSERT INTO BRECON.BRECON_CARD_EPIN_DATA_TABLE (" +
                        "  ACCT_NO, AUTH_CODE, DEST_AMT, PURCHASE_DATE" +
                        ") SELECT ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD') FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.BRECON_CARD_EPIN_DATA_TABLE " +
                        "  WHERE ACCT_NO = ? AND AUTH_CODE = ? AND DEST_AMT = ?" +
                        ")",
                        cleanCard, cleanAuthId, numericAmount, cleanDate,
                        cleanCard, cleanAuthId, numericAmount
                    );
                    break;

                case "VISANET":
                case "EP745":
                    inserted = jdbcTemplate.update(
                        "INSERT INTO BRECON.BRECON_CARD_EP745_DATA_TABLE (" +
                        "  CARD_NUM, RET_REF_NUM, TRACE_NUM, TRAN_AMOUNT, TRAN_DATE" +
                        ") SELECT ?, ?, ?, ?, ? FROM DUAL " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM BRECON.BRECON_CARD_EP745_DATA_TABLE " +
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
        data.put("atmCount", getSafeCount("BRECON.ATM_TRANSACTION_DATA"));
        data.put("posCount", getSafeCount("BRECON.POS_TRANSATION_DATA"));
        data.put("cbsCount", getSafeCount("BRECON.CBS_TXN"));
        data.put("epinCount", getSafeCount("BRECON.BRECON_CARD_EPIN_DATA_TABLE"));

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
    // FATCA & EXISTING SERVICES
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
            response.setHeader("Content-Disposition", 
                    "attachment; filename=\"FATCA_REPORT_" + System.currentTimeMillis() + ".xml\"");
            response.getOutputStream().write(xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error downloading FATCA XML", e);
        }
    }

    @GetMapping("/downloadSingleFatcaXml")
    public void downloadSingleFatcaXml(@RequestParam("refNo") String refNo, HttpServletResponse response) {
        try {
            String xmlContent = fatcaService.generateSingleFatcaXml(refNo);
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=\"FATCA_" + refNo + ".xml\"");
            response.getOutputStream().write(xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error downloading single record XML for " + refNo, e);
        }
    }
    
    
    
    @Autowired
    private com.bornfire.recon.services.CrsManagementService crsService;

    // =========================================================================
    // CRS REPORTING ENDPOINTS (MIRRORS FATCA WORKFLOW)
    // =========================================================================

    @GetMapping("/getCrsDashboardData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCrsDashboardData() {
        Map<String, Object> data = new HashMap<>();
        try {
            // Live table query from BRECON.CRS_REPORT_DATA
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT REPORT_ID, ACCOUNT_NUMBER, HOLDER_TYPE, RESIDENCE_COUNTRY, TIN, " +
                "       FIRST_NAME, LAST_NAME, ORGANISATION_NAME, ACCOUNT_BALANCE, CURRENCY_CODE, " +
                "       ACCOUNT_TYPE, DD_PROCEDURE, SELF_CERT_FLAG, STATUS " +
                "FROM BRECON.CRS_REPORT_DATA ORDER BY REPORT_ID DESC"
            );
            
            data.put("records", records);
            data.put("totalCount", records.size());
            data.put("pendingCount", records.stream()
                    .filter(r -> "PENDING".equalsIgnoreCase(String.valueOf(r.get("STATUS"))))
                    .count());
            data.put("completedCount", records.stream()
                    .filter(r -> "COMPLETED".equalsIgnoreCase(String.valueOf(r.get("STATUS"))))
                    .count());
            data.put("errorCount", records.stream()
                    .filter(r -> "ERROR".equalsIgnoreCase(String.valueOf(r.get("STATUS"))) 
                              || "EXCLUDED".equalsIgnoreCase(String.valueOf(r.get("STATUS"))))
                    .count());
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error fetching CRS Dashboard Data", e);
            data.put("records", Collections.emptyList());
            data.put("totalCount", 0);
            data.put("pendingCount", 0);
            data.put("completedCount", 0);
            data.put("errorCount", 0);
            return ResponseEntity.ok(data);
        }
    }

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

    @PostMapping("/updateCrsRecord")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Map<String, Object>> updateCrsRecord(
            @RequestParam("reportId") Long reportId,
            @RequestParam("status") String status,
            HttpServletRequest request) {

        Map<String, Object> res = new HashMap<>();

        if (reportId == null || status == null || status.trim().isEmpty()) {
            res.put("status", "ERROR");
            res.put("message", "Invalid Report ID or Status provided.");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            int updated = jdbcTemplate.update(
                "UPDATE BRECON.CRS_REPORT_DATA SET STATUS = ? WHERE REPORT_ID = ?", 
                status.trim().toUpperCase(), reportId
            );

            res.put("status", updated > 0 ? "SUCCESS" : "ERROR");
            res.put("message", updated > 0 ? "Record updated successfully." : "Record with ID " + reportId + " not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            logger.error("Error updating CRS record ID: {}", reportId, e);
            res.put("status", "ERROR");
            res.put("message", "Database Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/downloadCrsXml")
    public void downloadCrsXml(
            @RequestParam(value = "year", required = false, defaultValue = "2025") String year,
            HttpServletResponse response) {
        try {
            String xmlContent = crsService.generateCrsXml(year);
            
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=\"CRS_REPORT_" + year + "_" + System.currentTimeMillis() + ".xml\"");
            
            response.getOutputStream().write(xmlContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error generating master CRS XML for year {}", year, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate CRS XML: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    @GetMapping("/downloadSingleCrsXml")
    public void downloadSingleCrsXml(
            @RequestParam("reportId") Long reportId, 
            HttpServletResponse response) {
        try {
            String xmlContent = crsService.generateSingleCrsXml(reportId);
            
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=\"CRS_REC_" + reportId + ".xml\"");
            
            response.getOutputStream().write(xmlContent.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("Error downloading individual CRS XML for ID {}", reportId, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate single CRS XML: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }
    
    @PostMapping("/saveCrsRecord")
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Map<String, Object>> saveCrsRecord(
            @RequestParam("reportId") Long reportId,
            @RequestParam("accountNumber") String accountNumber,
            @RequestParam("holderType") String holderType,
            @RequestParam("residenceCountry") String residenceCountry,
            @RequestParam(value = "tin", required = false) String tin,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "organisationName", required = false) String organisationName,
            @RequestParam(value = "addressCity", required = false) String addressCity,
            @RequestParam(value = "accountBalance", defaultValue = "0.0") Double accountBalance,
            @RequestParam(value = "currencyCode", defaultValue = "MUR") String currencyCode,
            @RequestParam(value = "accountType", defaultValue = "CRS1101") String accountType,
            @RequestParam(value = "ddProcedure", defaultValue = "CRS1202") String ddProcedure,
            @RequestParam(value = "selfCertFlag", defaultValue = "CRS901") String selfCertFlag,
            @RequestParam(value = "status", defaultValue = "COMPLETED") String status) {

        Map<String, Object> res = new HashMap<>();

        if (reportId == null) {
            res.put("status", "ERROR");
            res.put("message", "Invalid Record ID.");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            int updated = jdbcTemplate.update(
                "UPDATE BRECON.CRS_REPORT_DATA SET " +
                "  ACCOUNT_NUMBER = ?, HOLDER_TYPE = ?, RESIDENCE_COUNTRY = ?, TIN = ?, " +
                "  FIRST_NAME = ?, LAST_NAME = ?, ORGANISATION_NAME = ?, ADDRESS_CITY = ?, " +
                "  ACCOUNT_BALANCE = ?, CURRENCY_CODE = ?, ACCOUNT_TYPE = ?, DD_PROCEDURE = ?, " +
                "  SELF_CERT_FLAG = ?, STATUS = ? " +
                "WHERE REPORT_ID = ?",
                accountNumber, holderType.toUpperCase(), residenceCountry.toUpperCase(), tin,
                firstName, lastName, organisationName, addressCity,
                accountBalance, currencyCode.toUpperCase(), accountType, ddProcedure,
                selfCertFlag, status.toUpperCase(), reportId
            );

            if (updated > 0) {
                res.put("status", "SUCCESS");
                res.put("message", "CRS record updated successfully.");
                return ResponseEntity.ok(res);
            } else {
                res.put("status", "ERROR");
                res.put("message", "Record with ID " + reportId + " not found.");
                return ResponseEntity.badRequest().body(res);
            }
        } catch (Exception e) {
            logger.error("Error saving CRS record ID: {}", reportId, e);
            res.put("status", "ERROR");
            res.put("message", "Database Error: " + e.getMessage());
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
}