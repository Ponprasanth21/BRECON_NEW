package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
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

    // 91 Clean Core Banking HTD Columns
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

 // 34 Visanet EP745 Columns matching BRECON_CARD_EP745_DATA_TABLE
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
        if (s.contains("TRAN_RMKS")) {
            return "TRAN_RMKS";
        }
        if (s.contains(".")) {
            s = s.substring(s.lastIndexOf('.') + 1);
        }
        return s.replaceAll("[^A-Z0-9_]", "");
    }

// =========================================================================
    // 1. MAIN TRANSACTION FILE UPLOAD ROUTER
    // =========================================================================
    @Transactional
    public int uploadTransactionFile(MultipartFile file, String type, String userId) throws Exception {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isCbs = "CBS".equalsIgnoreCase(type);
        boolean isEpin = "EPIN".equalsIgnoreCase(type);

        // Flexible alias matching for Visanet EP745
     // Flexible matching including VISANET, EPIN745, EP745
        boolean isEpin745 = "VISANET".equalsIgnoreCase(type) ||
                            "VISA_NET".equalsIgnoreCase(type) ||
                            "EPIN745".equalsIgnoreCase(type) || 
                            "EP745".equalsIgnoreCase(type) || 
                            "VISA_EPIN745".equalsIgnoreCase(type) ||
                            "VISA_EPIN_745".equalsIgnoreCase(type);

        if (isEpin745 && (fileName.endsWith(".txt") || fileName.endsWith(".csv") || !fileName.contains("."))) {
            logger.info("Routing Visanet file [{}] to uploadEpin745File with type [{}]", file.getOriginalFilename(), type);
            return uploadEpin745File(file, userId);
        }

        // 1. Raw text handling for EPIN (Standard Base II)
        if (isEpin && fileName.endsWith(".txt")) {
            String reportDateStr = new SimpleDateFormat("dd-MM-yy").format(new Date());
            int count = uploadRawEpinTextFile(file, reportDateStr);
            executeCompanyEpinProcedure(reportDateStr);
            return count;
        }

        // 2. Dedicated routing for Visanet EP745 files (.txt or .csv)
        if (isEpin745 && (fileName.endsWith(".txt") || fileName.endsWith(".csv"))) {
            logger.info("Routing EPIN745 file [{}] to dedicated parser uploadEpin745File", file.getOriginalFilename());
            return uploadEpin745File(file, userId);
        }

        String targetTable;
        String[] targetColumns;

        if ("ATM".equalsIgnoreCase(type)) {
            targetTable = "BRECON.ATM_TRANSACTION_DATA";
            targetColumns = DB_COLUMNS;
        } else if ("POS".equalsIgnoreCase(type)) {
            targetTable = "BRECON.POS_TRANSATION_DATA";
            targetColumns = DB_COLUMNS;
        } else if (isEpin) {
            targetTable = "BRECON.VISA_EPIN";
            targetColumns = DB_COLUMNS;
        } else if (isEpin745) {
            targetTable = "BRECON.BRECON_CARD_EP745_DATA_TABLE";
            targetColumns = EPIN745_COLUMNS;
        } else if (isCbs) {
            targetTable = "BRECON.CBS_TXN";
            targetColumns = CBS_COLUMNS; // 91 columns
        } else {
            targetTable = "BRECON." + type.toUpperCase() + "_TRANSACTION_DATA";
            targetColumns = DB_COLUMNS;
        }

        logger.info("Uploading file [{}] as [{}] into table [{}] with {} target columns", 
                    file.getOriginalFilename(), type, targetTable, targetColumns.length);

        List<Object[]> batchParams = new ArrayList<>();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

        boolean isTextCsv = fileName.endsWith(".csv") || fileName.endsWith(".txt");

        if (isTextCsv) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) {
                    return 0;
                }

                String delimiter = headerLine.contains(",") ? "," : "\\|";
                String[] headers = headerLine.split(delimiter);

                Map<String, Integer> colIndexMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String norm = cleanKey(headers[i]);
                    if (!norm.isEmpty()) {
                        colIndexMap.put(norm, i);
                    }
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(delimiter, -1);

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
                            Integer idx = colIndexMap.get(cleanKey(col));
                            if (idx != null && idx >= 0 && idx < tokens.length) {
                                String val = tokens[idx].trim().replaceAll("^\"|\"$", "");
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

                    if (hasData) {
                        batchParams.add(rowValues);
                    }
                }
            }
        } else {
            DataFormatter formatter = new DataFormatter();
            try (InputStream is = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) return 0;

                int totalCols = targetColumns.length;

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    Object[] rowValues = new Object[totalCols];
                    boolean hasData = false;

                    for (int c = 0; c < totalCols; c++) {
                        Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (cell != null) {
                            String cellStr = formatter.formatCellValue(cell).trim();
                            if (!cellStr.isEmpty()) {
                                hasData = true;
                                rowValues[c] = cellStr;
                            } else {
                                rowValues[c] = null;
                            }
                        } else {
                            rowValues[c] = null;
                        }
                    }

                    if (hasData) {
                        batchParams.add(rowValues);
                    }
                }
            }
        }

        if (batchParams.isEmpty()) {
            return 0;
        }

        // Build INSERT SQL without invoking metadata parser
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(targetTable).append(" (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");
        for (int i = 0; i < targetColumns.length; i++) {
            sql.append("\"").append(targetColumns[i]).append("\"");
            placeholders.append("?");
            if (i < targetColumns.length - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }
        sql.append(")").append(placeholders).append(")");

        final int numColumns = targetColumns.length;

        int[] updateCounts = jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] row = batchParams.get(i);
                for (int colIndex = 0; colIndex < numColumns; colIndex++) {
                    Object val = (colIndex < row.length) ? row[colIndex] : null;
                    if (val != null) {
                        ps.setString(colIndex + 1, val.toString());
                    } else {
                        ps.setNull(colIndex + 1, Types.VARCHAR);
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return batchParams.size();
            }
        });

        logger.info("Successfully inserted {} records into {}", updateCounts.length, targetTable);
        return updateCounts.length;
    }

    // =========================================================================
    // 2. DEDICATED VISANET EP745 INGESTION (INSERTS INTO BRECON_CARD_EP745_DATA_TABLE)
    // =========================================================================
@Transactional
    public int uploadEpin745File(MultipartFile file, String userId) throws Exception {
        List<Object[]> batchParams = new ArrayList<>();
        java.sql.Date entryDate = new java.sql.Date(System.currentTimeMillis());
        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";
        int totalScanned = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                totalScanned++;

                line = line.replace("\f", "").replace("\r", "");

                // Skip headers, separators, empty lines
                if (line.trim().length() < 70) {
                    continue;
                }

                String padded = String.format("%-140s", line);

                String batchNum = padded.substring(2, 4).trim();
                String cardNum = padded.substring(20, 36).trim();

                // 1. Must be Visa card (starts with 4 or 5)
                boolean isVisaCard = cardNum.startsWith("4") || cardNum.startsWith("5");

                // 2. Ignore control batch headers
                boolean notIgnoredBatch = !batchNum.equals("RE") && !batchNum.equals("RO") && !batchNum.equals("AT");

                // 3. Ignore POS ENTRY and DUP/ORI text lines
                boolean notExcluded = !padded.contains("POS ENTRY") && !padded.contains("DUP/ORI");

                // ❌ REMOVED: The != '0.00' filter has been removed to allow all 32 transactions
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
                    Double tranAmt = null;
                    if (!tranAmtStr.isEmpty()) {
                        try { tranAmt = Double.parseDouble(tranAmtStr); } catch (Exception ignored) {}
                    }

                    String tranCurr = padded.substring(114, 118).trim();

                    String settAmtRaw = padded.substring(118, 130).replaceAll("[^0-9.-]", "").trim();
                    Double settAmt = null;
                    if (!settAmtRaw.isEmpty()) {
                        try { settAmt = Double.parseDouble(settAmtRaw); } catch (Exception ignored) {}
                    }

                    String settInd = padded.substring(130, 132).trim();

                    batchParams.add(new Object[] {
                        batchNum,
                        tranDate,
                        tranTime,
                        cardNum,
                        rrn,
                        traceNum,
                        issuerId,
                        tranType,
                        processCode,
                        entMode,
                        cnStp,
                        rspCd,
                        tranAmt,
                        tranCurr,
                        settAmt,
                        settInd,
                        "VISA CAMEA",
                        "Y",
                        "N",
                        "N",
                        effectiveUser,
                        entryDate
                    });
                }
            }
        }

        logger.info("Visanet Scan: read {} lines, qualified {} transactions", totalScanned, batchParams.size());

        if (batchParams.isEmpty()) {
            return 0;
        }

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
                    if (val == null) {
                        ps.setNull(col + 1, Types.VARCHAR);
                    } else if (val instanceof Double) {
                        ps.setDouble(col + 1, (Double) val);
                    } else if (val instanceof java.sql.Date) {
                        ps.setDate(col + 1, (java.sql.Date) val);
                    } else {
                        ps.setString(col + 1, val.toString());
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return batchParams.size();
            }
        });

        logger.info("Successfully inserted {} records into BRECON.BRECON_CARD_EP745_DATA_TABLE", updateCounts.length);
        return updateCounts.length;
    }	
    
	public void exportDataToExcel(String type, HttpServletResponse response) throws Exception {
        String targetTable;
        boolean isCbs = "CBS".equalsIgnoreCase(type);

        if ("ATM".equalsIgnoreCase(type)) {
            targetTable = "BRECON.ATM_TRANSACTION_DATA";
        } else if ("POS".equalsIgnoreCase(type)) {
            targetTable = "BRECON.POS_TRANSATION_DATA";
        } else if ("EPIN".equalsIgnoreCase(type)) {
            targetTable = "BRECON.VISA_EPIN";
        } else if ("EPIN745".equalsIgnoreCase(type)) {
            targetTable = "BRECON.VISA_EPIN745";
        } else if (isCbs) {
            targetTable = "BRECON.CBS_TXN";
        } else {
            targetTable = "BRECON." + type.toUpperCase() + "_TRANSACTION_DATA";
        }

        logger.info("Exporting Excel report for type: [{}] from table: [{}]", type, targetTable);

        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            String query = "SELECT * FROM " + targetTable;
            rows = jdbcTemplate.queryForList(query);
        } catch (Exception ex) {
            logger.warn("Table {} query issue: {}", targetTable, ex.getMessage());
        }

        List<String> columnNames = new ArrayList<>();
        if (!rows.isEmpty()) {
            columnNames.addAll(rows.get(0).keySet());
        } else if (isCbs) {
            for (String col : CBS_COLUMNS) {
                columnNames.add(col);
            }
        } else {
            for (String col : DB_COLUMNS) {
                columnNames.add(col);
            }
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            Sheet sheet = workbook.createSheet(type.toUpperCase() + "_DATA");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnNames.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Map<String, Object> rowMap : rows) {
                Row row = sheet.createRow(rowIndex++);
                for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
                    String col = columnNames.get(colIndex);
                    Object val = rowMap.get(col);
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(val != null ? String.valueOf(val) : "");
                }
            }

            String fileName = type.toUpperCase() + "_DATA_" + System.currentTimeMillis() + ".xlsx";
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }

    @Transactional
    public int executeVisaReconciliation(String reconDate) {
        logger.info("Executing SP_EXECUTE_VISA_RECON for date: {}", reconDate);

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{call BRECON.SP_EXECUTE_VISA_RECON(?, ?)}")) {
                if (reconDate != null && !reconDate.trim().isEmpty()) {
                    cs.setString(1, reconDate);
                } else {
                    cs.setNull(1, Types.VARCHAR);
                }
                cs.registerOutParameter(2, Types.INTEGER);
                cs.execute();
                return cs.getInt(2);
            }
        });
    }

    // Executes the company-provided BRECON_CARD_EPIN_PROCEDURE
    @Transactional
    public void executeCompanyEpinProcedure(String reportDateStr) {
        logger.info("Executing BRECON_CARD_EPIN_PROCEDURE for date: {}", reportDateStr);
        jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{call BRECON.BRECON_CARD_EPIN_PROCEDURE(?)}")) {
                cs.setString(1, reportDateStr);
                cs.execute();
                return null;
            }
        });
    }

    // Raw text file reader for EPIN Base II files
    @Transactional
    public int uploadRawEpinTextFile(MultipartFile file, String reportDateStr) throws Exception {
        String sql = "INSERT INTO BRECON.BRECON_EPIN_TEXT_TABLE (TEXTDATA, UP_DATE) VALUES (?, TO_DATE(?, 'DD-MM-YY'))";
        List<Object[]> batchArgs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    batchArgs.add(new Object[] { line, reportDateStr });
                }
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
            logger.info("Inserted {} lines into BRECON_EPIN_TEXT_TABLE", batchArgs.size());
        }
        return batchArgs.size();
    }

    public void exportFailedRefundReport(HttpServletResponse response) throws Exception {
        String sql = "SELECT CARD_NUMBER, RRN, AUTH_ID, AMOUNT, TRAN_DT, AUTHX_RESP_CDE, AUTHX_TYP, REASON " +
                     "FROM BRECON.RECON_TXN " +
                     "WHERE RECON_STATUS = 'FAILED_REFUND'";

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            Sheet sheet = workbook.createSheet("FAILED_TXN_REFUND_REPORT");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.MAROON.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = new String[] {
                "Card Number", "RRN", "Auth ID", "Amount", "Txn Date", 
                "Response Code", "Auth Type", "Reason"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Map<String, Object> r : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.get("CARD_NUMBER") != null ? String.valueOf(r.get("CARD_NUMBER")) : "");
                row.createCell(1).setCellValue(r.get("RRN") != null ? String.valueOf(r.get("RRN")) : "");
                row.createCell(2).setCellValue(r.get("AUTH_ID") != null ? String.valueOf(r.get("AUTH_ID")) : "");
                row.createCell(3).setCellValue(r.get("AMOUNT") != null ? String.valueOf(r.get("AMOUNT")) : "");
                row.createCell(4).setCellValue(r.get("TRAN_DT") != null ? String.valueOf(r.get("TRAN_DT")) : "");
                row.createCell(5).setCellValue(r.get("AUTHX_RESP_CDE") != null ? String.valueOf(r.get("AUTHX_RESP_CDE")) : "");
                row.createCell(6).setCellValue(r.get("AUTHX_TYP") != null ? String.valueOf(r.get("AUTHX_TYP")) : "");
                row.createCell(7).setCellValue(r.get("REASON") != null ? String.valueOf(r.get("REASON")) : "Reversal Not Received");
            }

            String fileName = "FAILED_TXN_REFUND_REPORT_" + System.currentTimeMillis() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }
}