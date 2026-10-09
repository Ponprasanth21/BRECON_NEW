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

    private static final String[] CBS_COLUMNS = new String[] {
        "ACCOUNT_NUMBER", "CARD_NUMBER", "RRN", "AMOUNT", "TRAN_DT", 
        "TRAN_TYPE", "DR_CR_FLAG", "TRAN_DESCRIPTION", "ENTRY_TIME", 
        "ENTRY_USER", "DEL_FLAG", "ENTITY_FLAG", "MODIFY_FLAG"
    };

    private String cleanKey(String col) {
        if (col == null) return "";
        String s = col.trim().toUpperCase();
        if (s.contains(".")) {
            s = s.substring(s.lastIndexOf('.') + 1);
        }
        return s.replaceAll("[^A-Z0-9_]", "");
    }

    @Transactional
    public int uploadTransactionFile(MultipartFile file, String type, String userId) throws Exception {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isCbs = "CBS".equalsIgnoreCase(type);
        boolean isEpin = "EPIN".equalsIgnoreCase(type);

        // If EPIN raw TXT file is uploaded via standard Upload dropdown, route directly to raw text parser
        if (isEpin && fileName.endsWith(".txt")) {
            String reportDateStr = new SimpleDateFormat("dd-MM-yy").format(new Date());
            int count = uploadRawEpinTextFile(file, reportDateStr);
            executeCompanyEpinProcedure(reportDateStr);
            return count;
        }

        String targetTable;
        String[] targetColumns;

        if ("ATM".equalsIgnoreCase(type)) {
            targetTable = "BRECON.ATM_TRANSACTION_DATA";
            targetColumns = DB_COLUMNS;
        } else if ("POS".equalsIgnoreCase(type)) {
            targetTable = "BRECON.POS_TRANSATION_DATA";
            targetColumns = DB_COLUMNS;
        } else if ("EPIN".equalsIgnoreCase(type)) {
            targetTable = "BRECON.VISA_EPIN";
            targetColumns = DB_COLUMNS;
        } else if ("EPIN745".equalsIgnoreCase(type)) {
            targetTable = "BRECON.VISA_EPIN745";
            targetColumns = DB_COLUMNS;
        } else if (isCbs) {
            targetTable = "BRECON.CBS_TXN";
            targetColumns = CBS_COLUMNS;
        } else {
            targetTable = "BRECON." + type.toUpperCase() + "_TRANSACTION_DATA";
            targetColumns = DB_COLUMNS;
        }

        logger.info("Uploading file [{}] as [{}] into table [{}]", file.getOriginalFilename(), type, targetTable);

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
                        } else if ("DEL_FLAG".equals(col)) {
                            rowValues[i] = "N";
                        } else if ("ENTITY_FLAG".equals(col)) {
                            rowValues[i] = "Y";
                        } else if ("MODIFY_FLAG".equals(col)) {
                            rowValues[i] = "N";
                        } else {
                            Integer idx = colIndexMap.get(cleanKey(col));
                            if (idx != null && idx < tokens.length) {
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
                if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) return 0;

                Row headerRow = sheet.getRow(0);
                if (headerRow == null) return 0;

                Map<String, Integer> colIndexMap = new HashMap<>();
                for (Cell cell : headerRow) {
                    String rawName = formatter.formatCellValue(cell);
                    String normalized = cleanKey(rawName);
                    if (!normalized.isEmpty()) {
                        colIndexMap.put(normalized, cell.getColumnIndex());
                    }
                }

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    Object[] rowValues = new Object[targetColumns.length];
                    boolean hasData = false;

                    for (int i = 0; i < targetColumns.length; i++) {
                        String col = targetColumns[i];

                        if ("ENTRY_TIME".equals(col)) {
                            rowValues[i] = currentTime;
                        } else if ("ENTRY_USER".equals(col)) {
                            rowValues[i] = effectiveUser;
                        } else if ("DEL_FLAG".equals(col)) {
                            rowValues[i] = "N";
                        } else if ("ENTITY_FLAG".equals(col)) {
                            rowValues[i] = "Y";
                        } else if ("MODIFY_FLAG".equals(col)) {
                            rowValues[i] = "N";
                        } else {
                            Integer cellIndex = colIndexMap.get(cleanKey(col));
                            if (cellIndex != null) {
                                Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                                if (cell != null) {
                                    String cellStr = formatter.formatCellValue(cell).trim();
                                    if (!cellStr.isEmpty()) {
                                        hasData = true;
                                        rowValues[i] = cellStr;
                                    } else {
                                        rowValues[i] = null;
                                    }
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
        }

        if (batchParams.isEmpty()) {
            return 0;
        }

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

        int[] updateCounts = jdbcTemplate.batchUpdate(sql.toString(), batchParams);
        logger.info("Successfully inserted {} records into {}", updateCounts.length, targetTable);
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