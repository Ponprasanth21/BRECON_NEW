package com.bornfire.recon.services;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
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

    private String cleanKey(String col) {
        if (col == null) return "";
        String s = col.trim().toUpperCase();
        // Strip table prefixes like "POS." or "ATM." (e.g. POS.TRAN_DT -> TRAN_DT)
        if (s.contains(".")) {
            s = s.substring(s.lastIndexOf('.') + 1);
        }
        return s.replaceAll("[^A-Z0-9_]", "");
    }

    @Transactional
    public int uploadTransactionFile(MultipartFile file, String type, String userId) throws Exception {
        String targetTable;
        if ("ATM".equalsIgnoreCase(type)) {
            targetTable = "BRECON.ATM_TRANSACTION_DATA";
        } else if ("POS".equalsIgnoreCase(type)) {
            targetTable = "BRECON.POS_TRANSATION_DATA";
        } else if ("EPIN".equalsIgnoreCase(type)) {
            targetTable = "BRECON.EPIN_TRANSACTION_DATA";
        } else if ("VISANET".equalsIgnoreCase(type)) {
            targetTable = "BRECON.VISANET_TRANSACTION_DATA";
        } else {
            targetTable = "BRECON.CBS_TRANSACTION_DATA";
        }

        logger.info("Starting upload for category: {} into table: {}", type, targetTable);

        List<Object[]> batchParams = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                logger.warn("Uploaded sheet is empty");
                return 0;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                logger.warn("No header row found in Excel sheet");
                return 0;
            }

            // Map normalized header names to column index
            Map<String, Integer> colIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String rawName = formatter.formatCellValue(cell);
                String normalized = cleanKey(rawName);
                if (!normalized.isEmpty()) {
                    colIndexMap.put(normalized, cell.getColumnIndex());
                }
            }

            logger.info("Detected Excel headers: {}", colIndexMap.keySet());

            // Iterate through data rows
            int totalRows = sheet.getLastRowNum();
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Object[] rowValues = new Object[DB_COLUMNS.length];
                boolean rowHasMeaningfulData = false;

                for (int i = 0; i < DB_COLUMNS.length; i++) {
                    String col = DB_COLUMNS[i];

                    // System metadata columns
                    if ("ENTRY_TIME".equals(col)) {
                        rowValues[i] = currentTime;
                    } else if ("ENTRY_USER".equals(col)) {
                        rowValues[i] = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";
                    } else if ("DEL_FLAG".equals(col)) {
                        rowValues[i] = "N";
                    } else if ("ENTITY_FLAG".equals(col)) {
                        rowValues[i] = "Y";
                    } else if ("MODIFY_FLAG".equals(col)) {
                        rowValues[i] = "N";
                    } else {
                        // Match normalized column name
                        Integer cellIndex = colIndexMap.get(cleanKey(col));
                        if (cellIndex != null) {
                            Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            if (cell != null) {
                                String cellStr = formatter.formatCellValue(cell).trim();
                                if (!cellStr.isEmpty()) {
                                    rowHasMeaningfulData = true;
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

                if (rowHasMeaningfulData) {
                    batchParams.add(rowValues);
                }
            }
        }

        logger.info("Found {} valid rows to insert into {}", batchParams.size(), targetTable);

        if (batchParams.isEmpty()) {
            return 0;
        }

        // Build INSERT query
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(targetTable).append(" (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");
        for (int i = 0; i < DB_COLUMNS.length; i++) {
            sql.append("\"").append(DB_COLUMNS[i]).append("\"");
            placeholders.append("?");
            if (i < DB_COLUMNS.length - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }
        sql.append(")").append(placeholders).append(")");

        int[] updateCounts = jdbcTemplate.batchUpdate(sql.toString(), batchParams);
        logger.info("Batch execution completed. Rows affected: {}", updateCounts.length);

        return updateCounts.length;
    }

    public void exportDataToExcel(String type, HttpServletResponse response) throws Exception {
        String targetTable;
        if ("ATM".equalsIgnoreCase(type)) {
            targetTable = "BRECON.ATM_TRANSACTION_DATA";
        } else if ("POS".equalsIgnoreCase(type)) {
            targetTable = "BRECON.POS_TRANSATION_DATA";
        } else if ("EPIN".equalsIgnoreCase(type)) {
            targetTable = "BRECON.EPIN_TRANSACTION_DATA";
        } else if ("VISANET".equalsIgnoreCase(type)) {
            targetTable = "BRECON.VISANET_TRANSACTION_DATA";
        } else {
            targetTable = "BRECON.CBS_TRANSACTION_DATA";
        }

        logger.info("Exporting Excel report for type: {} from table: {}", type, targetTable);

        // Fetch records from target table
        String query = "SELECT * FROM " + targetTable;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);

        // Stream via SXSSFWorkbook for low memory overhead
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        try {
            Sheet sheet = workbook.createSheet(type.toUpperCase() + "_DATA");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Write Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < DB_COLUMNS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(DB_COLUMNS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Write Data Rows
            int rowIndex = 1;
            for (Map<String, Object> rowMap : rows) {
                Row row = sheet.createRow(rowIndex++);
                for (int colIndex = 0; colIndex < DB_COLUMNS.length; colIndex++) {
                    String colName = DB_COLUMNS[colIndex];
                    Object val = rowMap.get(colName);
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(val != null ? String.valueOf(val) : "");
                }
            }

            // Write to response stream
            String fileName = type.toUpperCase() + "_TRANSACTION_DATA_" + System.currentTimeMillis() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } finally {
            workbook.dispose(); // Cleans up temporary disk backing files
            workbook.close();
        }
    }
}