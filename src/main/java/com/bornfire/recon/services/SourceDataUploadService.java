package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SourceDataUploadService {

    private static final Logger logger = LoggerFactory.getLogger(SourceDataUploadService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String[] DB_COLUMNS = new String[] {
        "TRAN_DT", "AUTHX_SEQ_NUM", "HEADX_DAT_TIM", "HEADX_REC_TYP", "HEADX_CRD_LN", 
        "HEADX_CRD_FIID", "HEADX_CRD_CARD_CRD_NUM", "HEADX_CRD_CARD_MBR_NUM", "HEADX_RETL_KY", 
        "HEADX_RETL_KY_RDFKEY_FIID", "HEADX_RETL_KY_RDFKEY_GRP", "HEADX_RETL_KY_RDFKEY_REGN", 
        "HEADX_RETL_KY_RDFKEY_ID", "HEADX_RETL_TERM_ID", "HEADX_RETL_SHIFT_NUM", "HEADX_RETL_BATCH_NUM", 
        "HEADX_TERM_LN", "HEADX_TERM_FIID", "HEADX_TERM_TERM_ID", "HEADX_TERM_TIM_HH", 
        "HEADX_TERM_TIM_MM", "HEADX_TERM_TIM_SS", "HEADX_TERM_TIM_TT", "HEADX_TKEY_TERM_ID", 
        "HEADX_TKEYRKEY_REC_FRMT", "HEADX_TKEY_RKEY_RETAILER_ID", "HEADX_TKEY_RKEY_CLERK_ID", 
        "HEADX_DATA_FLAG", "AUTHX_TYP", "AUTHX_RTE_STAT", "AUTHX_ORIGINATOR", "AUTHX_RESPONDER", 
        "AUTHX_ISS_CDE", "AUTHX_ENTRY_TIM", "AUTHX_EXIT_TIM", "AUTHX_RE_ENTRY_TIM", "AUTHX_TRAN_TIM_HH", 
        "AUTHX_TRAN_TIM_MM", "AUTHX_TRAN_TIM_SS", "AUTHX_TRAN_TIM_TT", "ACQ_STLDT", "ISS_STLDT", 
        "AUTHX_TERM_NAME_LOC", "AUTHX_TERM_OWNER_NAME", "AUTHX_TERM_CITY", "AUTHX_TERM_ST", 
        "AUTHX_TERM_CNTRY_CDE", "AUTHX_BRCH_ID", "AUTHX_TERM_TIM_OFST", "AUTHX_ACQ_INST_ID_NUM", 
        "AUTHX_RCV_INST_ID_NUM", "AUTHX_TERM_TYP", "AUTHX_CLERK_ID", "AUTHX_CRT_AUTHX_GRP", 
        "AUTHX_CRT_AUTHX_USER_ID", "AUTHX_ORIG", "AUTHX_DEST", "AUTHX_TRAN_CDE_TC", "AUTHX_TRAN_CDE_T", 
        "AUTHX_TRAN_CDE_AA", "AUTHX_TRAN_CDE_C", "AUTHX_CRD_TYP", "AUTHX_ACCT", "AUTHX_RESP_CDE", 
        "AUTHX_AMT_1", "AUTHX_AMT_2", "AUTHX_EXP_DAT", "AUTHX_TRACK2", "AUTHX_PIN_OFST", 
        "AUTHX_PRE_AUTHX_SEQ_NUM", "AUTHX_INVOICE_NUM", "AUTHX_ORIG_INVOICE_NUM", "AUTHX_AUTH_ORIZER", 
        "AUTHX_AUTH_IND", "AUTHX_SHIFT_NUM", "AUTHX_BATCH_SEQ_NUM", "AUTHX_APPRV_CDE", 
        "AUTHX_APPRV_CDE_LGTH", "AUTHX_ICHG_RESP", "AUTHX_PSEUDO_TERM_ID", "AUTHX_RFRL_PHONE", 
        "AUTHX_DFT_CAPTURE_FLG", "AUTHX_SETL_FLAG", "AUTHX_RVRL_CDE", "AUTHX_REA_FOR_CHRGBCK", 
        "AUTHX_NUM_OF_CHRGBCK", "AUTHX_PT_SRV_COND_CDE", "AUTHX_PT_SRV_ENTRY_MDE", "AUTHX_AUTHX_IND2", 
        "AUTHX_ORIG_CRNCY_CDE", "AUTX_MULT_CRNCY_CDE_ID", "AUTX_MULT_CRNCY_AUTX_CONV_RATE", 
        "AUTX_MULT_CRNCY_SETL_CDE_ID", "AUTX_MULT_CRNCY_SETL_CONV_RATE", "AUTHX_MULT_CRNCY_CONV_DT_TIM", 
        "AUTHX_REFRIMPIND", "AUTHX_REFR_AVAIL_BAL", "AUTHX_REFR_LEDG_BAL", "AUTHX_REFR_AMT_ON_HOLD", 
        "AUTHX_REFR_TTL_FLOAT", "AUTHX_REFR_CUR_FLOAT", "AUTHX_ADJ_SETL_IMPACT_FLG", 
        "AUTHX_FRWD_INST_ID_NUM", "AUTHX_CRD_ACCPT_ID_NUM", "AUTHX_CRD_ISS_ID_NUM", "AUTHX_ORIG_MSG_TYP", 
        "AUTHX_ORIG_TRAN_TIM_HH", "AUTHX_ORIG_TRAN_TIM_MM", "AUTHX_ORIG_TRAN_TIM_SS", 
        "AUTHX_ORIG_TRAN_TIM_TT", "AUTHX_ORIG_TRAN_DAT", "AUTHX_ORIG_SEQ_NUM", "AUTHX_ORIG_B24_POST_DAT", 
        "AUTHX_EXCP_RSN_CDE", "AUTHX_OVRRDE_FLG", "AUTHX_PIN_IND", "AUTHX_PIN_TRIES", 
        "WKF_EXEC_ID_INS", "HASHED_HEADX_CRD_NUM", "ENTRY_TIME", "ENTRY_USER", "DEL_FLAG", 
        "MODIFY_FLAG", "ENTITY_FLAG"
    };

    private static final String[] CBS_COLUMNS = new String[] {
        "TRAN_DATE", "TRAN_ID", "PART_TRAN_SRL_NUM", "DEL_FLG", "TRAN_TYPE", "TRAN_SUB_TYPE",
        "PART_TRAN_TYPE", "GL_SUB_HEAD_CODE", "ACID", "VALUE_DATE", "TRAN_AMT", "TRAN_PARTICULAR",
        "ENTRY_USER_ID", "PSTD_USER_ID", "VFD_USER_ID", "ENTRY_DATE", "PSTD_DATE", "VFD_DATE",
        "RPT_CODE", "REF_NUM", "INSTRMNT_TYPE", "INSTRMNT_DATE", "INSTRMNT_NUM", "INSTRMNT_ALPHA",
        "TRAN_RMKS", "PSTD_FLG", "PRNT_ADVC_IND", "AMT_RESERVATION_IND", "RESERVATION_AMT",
        "RESTRICT_MODIFY_IND", "LCHG_USER_ID", "LCHG_TIME", "RCRE_USER_ID", "RCRE_TIME",
        "CUST_ID", "VOUCHER_PRINT_FLG", "MODULE_ID", "BR_CODE", "FX_TRAN_AMT", "RATE_CODE",
        "RATE", "CRNCY_CODE", "NAVIGATION_FLG", "TRAN_CRNCY_CODE", "REF_CRNCY_CODE", "REF_AMT",
        "SOL_ID", "BANK_CODE", "TREA_REF_NUM", "TREA_RATE", "TS_CNT", "GST_UPD_FLG", "ISO_FLG",
        "EABFAB_UPD_FLG", "LIFT_LIEN_FLG", "PROXY_POST_IND", "SI_SRL_NUM", "SI_ORG_EXEC_DATE",
        "PR_SRL_NUM", "SERIAL_NUM", "DEL_MEMO_PAD", "UAD_MODULE_ID", "UAD_MODULE_KEY",
        "REVERSAL_DATE", "REVERSAL_VALUE_DATE", "PTTM_EVENT_TYPE", "PROXY_ACID", "TOD_ENTITY_TYPE",
        "TOD_ENTITY_ID", "DTH_INIT_SOL_ID", "REGULARIZATION_AMT", "PRINCIPAL_PORTION_AMT",
        "TF_ENTITY_SOL_ID", "TRAN_PARTICULAR_2", "TRAN_PARTICULAR_CODE", "TR_STATUS",
        "PARTY_CODE", "SVS_TRAN_ID", "CRNCY_HOL_CHK_DONE_FLG", "REFERRAL_ID", "GL_DATE",
        "BKDT_TRAN_FLG", "BANK_ID", "IMPL_CASH_PART_TRAN_FLG", "PTRAN_CHRG_EXISTS_FLG",
        "MUD_POOL_BAL_BUILD_FLG", "GL_SEGMENT_STRING", "SYS_PART_TRAN_CODE", "USER_PART_TRAN_CODE",
        "TRAN_FREE_CODE1", "TRAN_FREE_CODE2"
    };

    private static final String[] EPIN745_COLUMNS = new String[] {
        "BATCH_NUM", "TRAN_DATE", "TRAN_TIME", "CARD_NUM", "RET_REF_NUM", "TRACE_NUM",
        "ISSUER_ID", "TRAN_TYPE", "PROCESS_CODE", "ENT_MODE", "CN_STP", "RSP_CD",
        "TRAN_AMOUNT", "TRAN_CURRENCY", "SETT_AMOUNT", "SETT_INDICATOR", "CA_ID",
        "NAT_INTNAT", "ENTITY_FLG", "MODIFY_FLG", "DEL_FLG", "ENTRY_USER", "MODIFY_USER",
        "VERIFY_USER", "ENTRY_TIME", "MODIFY_TIME", "VERIFY_TIME", "REPORT_CONFIRM",
        "REPORT_MOVED", "MATCHING_CRITERIA", "MATCHING_DETAIL", "SRL_NUM", "RECON_TYPE", "RECON_FLAG"
    };

    private String cleanKey(String col) {
        if (col == null) return "";
        String s = col.trim().toUpperCase();
        if (s.contains("TRAN_RMKS")) return "TRAN_RMKS";
        if (s.contains(".")) {
            s = s.substring(s.lastIndexOf('.') + 1);
        }
        return s.replaceAll("[^A-Z0-9_]", "");
    }

    // =========================================================================
    // 1. MAIN TRANSACTION FILE UPLOAD (RETURNS BOTH INSERTED & FAILED COUNTS)
    // =========================================================================
    public Map<String, Object> uploadTransactionFile(MultipartFile file, String type, String userId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isCbs = "CBS".equalsIgnoreCase(type);
        boolean isEpin = "EPIN".equalsIgnoreCase(type);
        boolean isEpin745 = "VISANET".equalsIgnoreCase(type) || "VISA_NET".equalsIgnoreCase(type) ||
                            "EPIN745".equalsIgnoreCase(type) || "EP745".equalsIgnoreCase(type);

        // EPIN Text
        if (isEpin && fileName.endsWith(".txt")) {
            String reportDateStr = new SimpleDateFormat("dd-MM-yy").format(new Date());
            try {
                jdbcTemplate.update("DELETE FROM BRECON.BRECON_EPIN_TEXT_TABLE WHERE UP_DATE = TO_DATE(?, 'DD-MM-YY')", reportDateStr);
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.update("DELETE FROM BRECON.BRECON_CARD_EPIN_DATA_TABLE WHERE TO_CHAR(ENTRY_TIME, 'DD-MM-YY') = ?", reportDateStr);
            } catch (Exception ignored) {}

            uploadRawEpinTextFile(file, reportDateStr);
            executeCompanyEpinProcedure(reportDateStr);

            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRECON.BRECON_CARD_EPIN_DATA_TABLE", Integer.class);
            result.put("insertedRows", count != null ? count : 0);
            result.put("failedRows", 0);
            return result;
        }

        // EP745 Text
        if (isEpin745 && (fileName.endsWith(".txt") || fileName.endsWith(".csv"))) {
            int inserted = uploadEpin745File(file, userId);
            result.put("insertedRows", inserted);
            result.put("failedRows", 0);
            return result;
        }

        String targetTable = "ATM".equalsIgnoreCase(type) ? "BRECON.ATM_TRANSACTION_DATA" :
                             "POS".equalsIgnoreCase(type) ? "BRECON.POS_TRANSATION_DATA" :
                             isCbs ? "BRECON.CBS_TXN" : "BRECON.BRECON_CARD_EP745_DATA_TABLE";

        String[] targetColumns = isCbs ? CBS_COLUMNS : isEpin745 ? EPIN745_COLUMNS : DB_COLUMNS;
        int insertedCount = 0;
        int failedCount = 0;

        DataFormatter formatter = new DataFormatter();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                result.put("insertedRows", 0);
                result.put("failedRows", 0);
                return result;
            }

            // Map Headers
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colMap = new HashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null) {
                    colMap.put(cleanKey(formatter.formatCellValue(cell)), c);
                }
            }

            // Prepare single-row INSERT SQL
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(targetTable).append(" (");
            StringBuilder ph = new StringBuilder(" VALUES (");
            for (int i = 0; i < targetColumns.length; i++) {
                sql.append("\"").append(targetColumns[i]).append("\"");
                ph.append("?");
                if (i < targetColumns.length - 1) {
                    sql.append(", ");
                    ph.append(", ");
                }
            }
            sql.append(")").append(ph).append(")");
            String insertSql = sql.toString();

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                // Extract Validation Fields
                String tranDt = getCellVal(row, colMap.get(cleanKey("TRAN_DT")), formatter);
                String rrn = getCellVal(row, colMap.get(cleanKey("AUTHX_SEQ_NUM")), formatter);
                String cardNo = getCellVal(row, colMap.get(cleanKey("HEADX_CRD_CARD_CRD_NUM")), formatter);
                String authId = getCellVal(row, colMap.get(cleanKey("AUTHX_APPRV_CDE")), formatter);
                String amtStr = getCellVal(row, colMap.get(cleanKey("AUTHX_AMT_1")), formatter);
                String authTyp = getCellVal(row, colMap.get(cleanKey("AUTHX_TYP")), formatter);

                if (authTyp.isEmpty()) authTyp = "210";

                // --- 1. VALIDATION CHECKS (FOR ATM / POS) ---
                if ("ATM".equalsIgnoreCase(type) || "POS".equalsIgnoreCase(type)) {
                    if (rrn.isEmpty() || "null".equalsIgnoreCase(rrn)) {
                        logFailedRecord(type, cardNo, "N/A", authId, amtStr, tranDt, "Missing Mandatory RRN");
                        failedCount++;
                        continue;
                    }

                    if (cardNo.length() < 13 || cardNo.length() > 19 || !cardNo.matches("^[0-9xX]+$")) {
                        logFailedRecord(type, cardNo, rrn, authId, amtStr, tranDt, "Invalid Card Number: Must be 13-19 digits or masked");
                        failedCount++;
                        continue;
                    }

                    try {
                        double parsedAmt = Double.parseDouble(amtStr.replace(",", ""));
                        if (parsedAmt < 0) {
                            logFailedRecord(type, cardNo, rrn, authId, amtStr, tranDt, "Amount Format Mismatch: Negative Amount");
                            failedCount++;
                            continue;
                        }
                    } catch (Exception ex) {
                        logFailedRecord(type, cardNo, rrn, authId, amtStr, tranDt, "Amount Format Mismatch: Non-numeric value");
                        failedCount++;
                        continue;
                    }

                    // Pre-check for duplicate in DB
                    Integer exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + targetTable + " " +
                        "WHERE AUTHX_SEQ_NUM = ? AND AUTHX_APPRV_CDE = ? AND TRAN_DT = ? AND AUTHX_TYP = ?",
                        Integer.class, rrn, authId, tranDt, authTyp
                    );

                    if (exists != null && exists > 0) {
                        logFailedRecord(type, cardNo, rrn, authId, amtStr, tranDt, "Duplicate Record: Key already exists in Database");
                        failedCount++;
                        continue;
                    }
                }

                // --- 2. PREPARE ROW PARAMS & EXECUTE INSERT ---
                Object[] rowValues = new Object[targetColumns.length];
                boolean hasData = false;

                for (int i = 0; i < targetColumns.length; i++) {
                    String col = targetColumns[i];
                    if ("ENTRY_TIME".equals(col)) {
                        rowValues[i] = currentTime;
                    } else if ("ENTRY_USER".equals(col)) {
                        rowValues[i] = effectiveUser;
                    } else if ("DEL_FLAG".equals(col) && !isCbs && !isEpin745) {
                        rowValues[i] = "N";
                    } else if ("ENTITY_FLAG".equals(col) && !isCbs && !isEpin745) {
                        rowValues[i] = "Y";
                    } else if ("MODIFY_FLAG".equals(col) && !isCbs && !isEpin745) {
                        rowValues[i] = "N";
                    } else {
                        Integer cIdx = colMap.get(cleanKey(col));
                        if (cIdx != null) {
                            String val = getCellVal(row, cIdx, formatter);
                            if (!val.isEmpty()) {
                                hasData = true;
                                rowValues[i] = val;
                            } else {
                                rowValues[i] = null;
                            }
                        } else {
                            rowValues[i] = null;
                        }
                    }
                }

                if (!hasData) continue;

                try {
                    jdbcTemplate.update(insertSql, rowValues);
                    insertedCount++;
                } catch (org.springframework.dao.DuplicateKeyException dke) {
                    logFailedRecord(type, cardNo, rrn, authId, amtStr, tranDt, "Duplicate Record: Constraint Violated");
                    failedCount++;
                } catch (Exception ex) {
                    logFailedRecord(type, cardNo, rrn, authId, amtStr, tranDt, "DB Insert Error: " + ex.getMessage());
                    failedCount++;
                }
            }
        }

        result.put("insertedRows", insertedCount);
        result.put("failedRows", failedCount);
        return result;
    }

    private String getCellVal(Row row, Integer idx, DataFormatter formatter) {
        if (idx == null || idx < 0) return "";
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedRecord(String src, String card, String rrn, String auth, String amt, String dt, String reason) {
        try {
            jdbcTemplate.update(
                "INSERT INTO BRECON.UPLOAD_FAILED_RECORDS " +
                "(SOURCE_TYPE, RAW_CARD_NO, RAW_RRN, RAW_AUTH_ID, RAW_AMOUNT, RAW_TRAN_DATE, ERROR_REASON, STATUS) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')",
                src, card, rrn, auth, amt, dt, reason
            );
        } catch (Exception e) {
            logger.error("Error writing to UPLOAD_FAILED_RECORDS: {}", e.getMessage());
        }
    }

    // EP745 Parser (same as existing)
    @Transactional
    public int uploadEpin745File(MultipartFile file, String userId) throws Exception {
        List<Object[]> batchParams = new ArrayList<>();
        java.sql.Date entryDate = new java.sql.Date(System.currentTimeMillis());
        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\f", "").replace("\r", "");
                if (line.trim().length() < 70) continue;

                String padded = String.format("%-140s", line);
                String batchNum = padded.substring(2, 4).trim();
                String cardNum = padded.substring(20, 36).trim();

                boolean isVisaCard = cardNum.startsWith("4") || cardNum.startsWith("5");
                boolean notIgnoredBatch = !batchNum.equals("RE") && !batchNum.equals("RO") && !batchNum.equals("AT");
                boolean notExcluded = !padded.contains("POS ENTRY") && !padded.contains("DUP/ORI");

                if (isVisaCard && notIgnoredBatch && notExcluded) {
                    String tranDate = padded.substring(5, 10).trim();
                    String tranTime = padded.substring(11, 19).trim();
                    String rrn = padded.substring(40, 52).trim();
                    String traceNum = padded.substring(53, 59).trim();
                    String issuerId = padded.substring(60, 66).trim();
                    String tranType = padded.substring(72, 76).trim();
                    String processCode = padded.substring(77, 83).trim();
                    String entMode = padded.substring(84, 87).trim();
                    String cnStp = padded.substring(93, 95).trim();
                    String rspCd = padded.substring(97, 99).trim();

                    String tranAmtStr = padded.substring(100, 113).replaceAll("[^0-9.-]", "").trim();
                    Double tranAmt = !tranAmtStr.isEmpty() ? Double.parseDouble(tranAmtStr) : null;
                    String tranCurr = padded.substring(114, 118).trim();

                    String settAmtRaw = padded.substring(118, 130).replaceAll("[^0-9.-]", "").trim();
                    Double settAmt = !settAmtRaw.isEmpty() ? Double.parseDouble(settAmtRaw) : null;
                    String settInd = padded.substring(130, 132).trim();

                    batchParams.add(new Object[] {
                        batchNum, tranDate, tranTime, cardNum, rrn, traceNum,
                        issuerId, tranType, processCode, entMode, cnStp, rspCd,
                        tranAmt, tranCurr, settAmt, settInd, "VISA CAMEA",
                        "Y", "N", "N", effectiveUser, entryDate
                    });
                }
            }
        }

        if (batchParams.isEmpty()) return 0;

        String sql = "INSERT INTO BRECON.BRECON_CARD_EP745_DATA_TABLE (" +
                     "BATCH_NUM, TRAN_DATE, TRAN_TIME, CARD_NUM, RET_REF_NUM, TRACE_NUM, " +
                     "ISSUER_ID, TRAN_TYPE, PROCESS_CODE, ENT_MODE, CN_STP, RSP_CD, " +
                     "TRAN_AMOUNT, TRAN_CURRENCY, SETT_AMOUNT, SETT_INDICATOR, NAT_INTNAT, " +
                     "ENTITY_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER, ENTRY_TIME, SRL_NUM) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)";

        int[] updateCounts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] row = batchParams.get(i);
                for (int col = 0; col < row.length; col++) {
                    Object val = row[col];
                    if (val == null) ps.setNull(col + 1, Types.VARCHAR);
                    else if (val instanceof Double) ps.setDouble(col + 1, (Double) val);
                    else if (val instanceof java.sql.Date) ps.setDate(col + 1, (java.sql.Date) val);
                    else ps.setString(col + 1, val.toString());
                }
            }
            @Override
            public int getBatchSize() { return batchParams.size(); }
        });

        return updateCounts.length;
    }

    public void exportDataToExcel(String type, HttpServletResponse response) throws Exception {
        String targetTable = "ATM".equalsIgnoreCase(type) ? "BRECON.ATM_TRANSACTION_DATA" :
                             "POS".equalsIgnoreCase(type) ? "BRECON.POS_TRANSATION_DATA" :
                             "EPIN".equalsIgnoreCase(type) ? "BRECON.VISA_EPIN" :
                             "EPIN745".equalsIgnoreCase(type) ? "BRECON.VISA_EPIN745" :
                             "CBS".equalsIgnoreCase(type) ? "BRECON.CBS_TXN" : "BRECON." + type.toUpperCase() + "_TRANSACTION_DATA";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + targetTable);
        List<String> columnNames = new ArrayList<>();
        if (!rows.isEmpty()) columnNames.addAll(rows.get(0).keySet());
        else for (String c : ("CBS".equalsIgnoreCase(type) ? CBS_COLUMNS : DB_COLUMNS)) columnNames.add(c);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            Sheet sheet = workbook.createSheet(type.toUpperCase() + "_DATA");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnNames.size(); i++) headerRow.createCell(i).setCellValue(columnNames.get(i));

            int rIdx = 1;
            for (Map<String, Object> map : rows) {
                Row r = sheet.createRow(rIdx++);
                for (int c = 0; c < columnNames.size(); c++) {
                    Object v = map.get(columnNames.get(c));
                    r.createCell(c).setCellValue(v != null ? String.valueOf(v) : "");
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + type + "_DATA_" + System.currentTimeMillis() + ".xlsx");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    @Transactional
    public int executeVisaReconciliation(String reconDate) {
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{call BRECON.SP_EXECUTE_VISA_RECON(?, ?)}")) {
                if (reconDate != null && !reconDate.trim().isEmpty()) cs.setString(1, reconDate);
                else cs.setNull(1, Types.VARCHAR);
                cs.registerOutParameter(2, Types.INTEGER);
                cs.execute();
                return cs.getInt(2);
            }
        });
    }

    @Transactional
    public void executeCompanyEpinProcedure(String reportDateStr) {
        jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{call BRECON.BRECON_CARD_EPIN_PROCEDURE(?)}")) {
                cs.setString(1, reportDateStr);
                cs.execute();
                return null;
            }
        });
    }

    @Transactional
    public int uploadRawEpinTextFile(MultipartFile file, String reportDateStr) throws Exception {
        String sql = "INSERT INTO BRECON.BRECON_EPIN_TEXT_TABLE (TEXTDATA, UP_DATE) VALUES (?, TO_DATE(?, 'DD-MM-YY'))";
        List<Object[]> batchArgs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) batchArgs.add(new Object[] { line, reportDateStr });
            }
        }
        if (!batchArgs.isEmpty()) jdbcTemplate.batchUpdate(sql, batchArgs);
        return batchArgs.size();
    }

    public void exportFailedRefundReport(HttpServletResponse response) throws Exception {
        String sql = "SELECT CARD_NUMBER, RRN, AUTH_ID, AMOUNT, TRAN_DT, AUTHX_RESP_CDE, AUTHX_TYP, REASON " +
                     "FROM BRECON.RECON_TXN WHERE RECON_STATUS = 'FAILED_REFUND'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            Sheet sheet = workbook.createSheet("FAILED_TXN_REFUND_REPORT");
            String[] headers = { "Card Number", "RRN", "Auth ID", "Amount", "Txn Date", "Response Code", "Auth Type", "Reason" };
            Row hRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) hRow.createCell(i).setCellValue(headers[i]);

            int rowIdx = 1;
            for (Map<String, Object> r : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(r.get("CARD_NUMBER") != null ? r.get("CARD_NUMBER") : ""));
                row.createCell(1).setCellValue(String.valueOf(r.get("RRN") != null ? r.get("RRN") : ""));
                row.createCell(2).setCellValue(String.valueOf(r.get("AUTH_ID") != null ? r.get("AUTH_ID") : ""));
                row.createCell(3).setCellValue(String.valueOf(r.get("AMOUNT") != null ? r.get("AMOUNT") : ""));
                row.createCell(4).setCellValue(String.valueOf(r.get("TRAN_DT") != null ? r.get("TRAN_DT") : ""));
                row.createCell(5).setCellValue(String.valueOf(r.get("AUTHX_RESP_CDE") != null ? r.get("AUTHX_RESP_CDE") : ""));
                row.createCell(6).setCellValue(String.valueOf(r.get("AUTHX_TYP") != null ? r.get("AUTHX_TYP") : ""));
                row.createCell(7).setCellValue(String.valueOf(r.get("REASON") != null ? r.get("REASON") : ""));
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=FAILED_TXN_REFUND_REPORT_" + System.currentTimeMillis() + ".xlsx");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }
}