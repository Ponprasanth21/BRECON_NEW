package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SourceDataUploadService {

    private static final Logger logger = LoggerFactory.getLogger(SourceDataUploadService.class);
    private static final int CHUNK_SIZE = 5000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${datasrc.url:}")
    private String datasrcUrl;

    @Value("${datasrc.username:}")
    private String datasrcUsername;

    @Value("${datasrc.password:}")
    private String datasrcPassword;

    @Value("${sqlldr.enabled:true}")
    private boolean sqlldrEnabled;

    @Value("${sqlldr.path:}")
    private String configuredSqlldrPath;

    @Value("${sqlldr.oracleHome:}")
    private String configuredOracleHome;

    @Value("${sqlldr.direct:false}")
    private boolean sqlldrDirect;

    @Value("${sqlldr.bindsize:20971520}")
    private int sqlldrBindSize;

    @Value("${sqlldr.readsize:20971520}")
    private int sqlldrReadSize;

    @Value("${sqlldr.rows:50000}")
    private int sqlldrRows;

    @Value("${sqlldr.errors:100000}")
    private int sqlldrErrors;

    private String cachedSqlldrPath = null;


    private static final String[] DB_COLUMNS = new String[] {
        "TXN_DT", "AUTHX_SEQ_NUM", "HEADX_DAT_TIM", "HEADX_REC_TYP", "HEADX_AUTHX_PPD", 
        "HEADX_TERM_LN", "HEADX_TERM_FIID", "HEADX_TERM_TERM_ID", "HEADX_CRD_LN", 
        "HEADX_CRD_FIID", "HEADX_CRD_PAN", "HEADX_CRD_MBR_NUM", "HEADX_BRCH_ID", 
        "HEADX_REGN_ID", "AUTHX_TYP_CDE", "AUTHX_TYP", "AUTHX_RTE_STAT", "AUTHX_ORIGINATOR", 
        "AUTHX_RESPONDER", "AUTHX_ENTRY_TIM", "AUTHX_EXIT_TIM", "AUTHX_RE_ENTRY_TIM", 
        "AUTHX_TRAN_TIM_HH", "AUTHX_TRAN_TIM_MM", "AUTHX_TRAN_TIM_SS", "AUTHX_TRAN_TIM_TT", 
        "POST_DT", "ACQ_STLDT", "ISSU_STLDT", "AUTHX_TERM_TYP", "AUTHX_TIM_OFST", 
        "AUTHX_ACQ_INST_ID_NUM", "AUTHX_RCV_INST_ID_NUM", "AUTHX_TRAN_CDE_R_T_CDE", 
        "AUTHX_TRAN_CDE_R_T_FROM", "AUTHX_TRAN_CDE_R_T_TO", "AUTHX_FROM_ACCT", 
        "AUTHX_TO_ACCT", "AUTHX_MULT_ACCT", "AUTHX_AMT_1", "AUTHX_AMT_2", "AUTHX_AMT_3", 
        "AUTHX_DEP_BAL_CR", "AUTHX_DEP_TYP", "AUTHX_RESP_CDE_R_RESP_BYTE_1", "RESP_BYTE_2", 
        "AUTHX_TERM_NAME_LOC", "AUTHX_TERM_OWNER_NAME", "AUTHX_TERM_CITY", "AUTHX_TERM_ST_X", 
        "AUTHX_TERM_CNTRY_X", "AUTHX_ORIG_OSEQ_NUM", "AUTHX_ORIG_OTRAN_DAT", "AUTHX_ORIG_OTRAN_TIM", 
        "B24_POST_DAT", "AUTHX_ORIG_CRNCY_CDE", "AUTX_MULT_CRNCY_CDE_ID", "AUTX_MULT_CRNCY_AUTX_CONV_RATE", 
        "AUTX_MULT_CRNCY_SETL_CDE_ID", "AUTX_MULT_CRNCY_SETL_CONV_RATE", "AUTHX_MULT_CRNCY_CONV_DT_TIM", 
        "AUTHX_RVSL_RSN", "AUTHX_PIN_OFST", "AUTHX_SHRG_GRP", "AUTHX_DEST_ORDER", 
        "AUTHX_AUTH_ID_RESP", "AUTHX_REFR_IMP_IND", "AUTHX_REFR_AVAIL_IMP_FROM", 
        "AUTHX_REFR_AVAIL_IMP_TO", "AUTHX_REFR_LEDG_IMP_FROM", "AUTHX_REFR_HLD_AMT_IMP_FROM", 
        "AUTHX_REFR_HLD_AMT_IMP_TO", "AUTHX_DEP_SETL_IMP_FLG", "AUTHX_ADJ_SETL_IMP_FLG", 
        "AUTHX_USER_FLD4", "AUTHX_FRWD_INST_ID_NUM", "AUTHX_CRD_ACCPT_ID_NUM", "AUTHX_CRD_ISS_ID_NUM", 
        "AUTHX_USER_FLD6", "WKF_EXEC_ID_INS", "HASHED_HEADX_CRD_PAN", "ENTRY_TIME", 
        "ENTRY_USER", "DEL_FLAG", "MODIFY_FLAG", "ENTITY_FLAG"
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
        "TRAN_FREE_CODE1", "TRAN_FREE_CODE2", "AUTH_ID"
    };

    private static final String[] EPIN745_COLUMNS = new String[] {
        "BATCH_NUM", "TRAN_DATE", "TRAN_TIME", "CARD_NUM", "RET_REF_NUM", "TRACE_NUM",
        "ISSUER_ID", "TRAN_TYPE", "PROCESS_CODE", "ENT_MODE", "CN_STP", "RSP_CD",
        "TRAN_AMOUNT", "TRAN_CURRENCY", "SETT_AMOUNT", "SETT_INDICATOR", "CA_ID",
        "NAT_INTNAT", "ENTITY_FLG", "MODIFY_FLG", "DEL_FLG", "ENTRY_USER", "MODIFY_USER",
        "VERIFY_USER", "ENTRY_TIME", "MODIFY_TIME", "VERIFY_TIME", "REPORT_CONFIRM",
        "REPORT_MOVED", "MATCHING_CRITERIA", "MATCHING_DETAIL", "SRL_NUM", "RECON_TYPE", "RECON_FLAG"
    };

    private static final String[] POS_COLUMNS = new String[] {
        "TRAN_DT", "AUTHX_SEQ_NUM", "HEADX_DAT_TIM", "HEADX_REC_TYP", 
        "HEADX_CRD_LN", "HEADX_CRD_FIID", "HEADX_CRD_CARD_CRD_NUM", "HEADX_CRD_CARD_MBR_NUM", 
        "HEADX_RETL_KY", "HEADX_RETL_KY_RDFKEY_FIID", "HEADX_RETL_KY_RDFKEY_GRP", "HEADX_RETL_KY_RDFKEY_REGN", 
        "HEADX_RETL_KY_RDFKEY_ID", "HEADX_RETL_TERM_ID", "HEADX_RETL_SHIFT_NUM", "HEADX_RETL_BATCH_NUM", 
        "HEADX_TERM_LN", "HEADX_TERM_FIID", "HEADX_TERM_TERM_ID", "HEADX_TERM_TIM_HH", 
        "HEADX_TERM_TIM_MM", "HEADX_TERM_TIM_SS", "HEADX_TERM_TIM_TT", "HEADX_TKEY_TERM_ID", 
        "HEADX_TKEYRKEY_REC_FRMT", "HEADX_TKEY_RKEY_RETAILER_ID", "HEADX_TKEY_RKEY_CLERK_ID", "HEADX_DATA_FLAG", 
        "AUTHX_TYP", "AUTHX_RTE_STAT", "AUTHX_ORIGINATOR", "AUTHX_RESPONDER", 
        "AUTHX_ISS_CDE", "AUTHX_ENTRY_TIM", "AUTHX_EXIT_TIM", "AUTHX_RE_ENTRY_TIM", 
        "AUTHX_TRAN_TIM_HH", "AUTHX_TRAN_TIM_MM", "AUTHX_TRAN_TIM_SS", "AUTHX_TRAN_TIM_TT", 
        "ACQ_STLDT", "ISS_STLDT", "AUTHX_TERM_NAME_LOC", "AUTHX_TERM_OWNER_NAME", 
        "AUTHX_TERM_CITY", "AUTHX_TERM_ST", "AUTHX_TERM_CNTRY_CDE", "AUTHX_BRCH_ID", 
        "AUTHX_TERM_TIM_OFST", "AUTHX_ACQ_INST_ID_NUM", "AUTHX_RCV_INST_ID_NUM", "AUTHX_TERM_TYP", 
        "AUTHX_CLERK_ID", "AUTHX_CRT_AUTHX_GRP", "AUTHX_CRT_AUTHX_USER_ID", "AUTHX_ORIG", 
        "AUTHX_DEST", "AUTHX_TRAN_CDE_TC", "AUTHX_TRAN_CDE_T", "AUTHX_TRAN_CDE_AA", 
        "AUTHX_TRAN_CDE_C", "AUTHX_CRD_TYP", "AUTHX_ACCT", "AUTHX_RESP_CDE", 
        "AUTHX_AMT_1", "AUTHX_AMT_2", "AUTHX_EXP_DAT", "AUTHX_TRACK2", 
        "AUTHX_PIN_OFST", "AUTHX_PRE_AUTHX_SEQ_NUM", "AUTHX_INVOICE_NUM", "AUTHX_ORIG_INVOICE_NUM", 
        "AUTHX_AUTH_ORIZER", "AUTHX_AUTH_IND", "AUTHX_SHIFT_NUM", "AUTHX_BATCH_SEQ_NUM", 
        "AUTHX_APPRV_CDE", "AUTHX_APPRV_CDE_LGTH", "AUTHX_ICHG_RESP", "AUTHX_PSEUDO_TERM_ID", 
        "AUTHX_RFRL_PHONE", "AUTHX_DFT_CAPTURE_FLG", "AUTHX_SETL_FLAG", "AUTHX_RVRL_CDE", 
        "AUTHX_REA_FOR_CHRGBCK", "AUTHX_NUM_OF_CHRGBCK", "AUTHX_PT_SRV_COND_CDE", "AUTHX_PT_SRV_ENTRY_MDE", 
        "AUTHX_AUTHX_IND2", "AUTHX_ORIG_CRNCY_CDE", "AUTX_MULT_CRNCY_CDE_ID", "AUTX_MULT_CRNCY_AUTX_CONV_RATE", 
        "AUTX_MULT_CRNCY_SETL_CDE_ID", "AUTX_MULT_CRNCY_SETL_CONV_RATE", "AUTHX_MULT_CRNCY_CONV_DT_TIM", "AUTHX_REFRIMPIND", 
        "AUTHX_REFR_AVAIL_BAL", "AUTHX_REFR_LEDG_BAL", "AUTHX_REFR_AMT_ON_HOLD", "AUTHX_REFR_TTL_FLOAT", 
        "AUTHX_REFR_CUR_FLOAT", "AUTHX_ADJ_SETL_IMPACT_FLG", "AUTHX_FRWD_INST_ID_NUM", "AUTHX_CRD_ACCPT_ID_NUM", 
        "AUTHX_CRD_ISS_ID_NUM", "AUTHX_ORIG_MSG_TYP", "AUTHX_ORIG_TRAN_TIM_HH", "AUTHX_ORIG_TRAN_TIM_MM", 
        "AUTHX_ORIG_TRAN_TIM_SS", "AUTHX_ORIG_TRAN_TIM_TT", "AUTHX_ORIG_TRAN_DAT", "AUTHX_ORIG_SEQ_NUM", 
        "AUTHX_ORIG_B24_POST_DAT", "AUTHX_EXCP_RSN_CDE", "AUTHX_OVRRDE_FLG", "AUTHX_PIN_IND", 
        "AUTHX_PIN_TRIES", "WKF_EXEC_ID_INS", "HASHED_HEADX_CRD_NUM", "ENTRY_TIME", 
        "ENTRY_USER", "MODIFY_TIME", "MODIFY_USER", "VERIFY_TIME", 
        "VERIFY_USER", "DEL_FLAG", "MODIFY_FLAG", "ENTITY_FLAG"
    };

    private static final String[] EPIN_COLUMNS = new String[] {
        "LOC_REF_NO", "TRAN_CODE", "TRAN_CODE_QUALIFIER", "TRAN_COMP_SEQ_NO", "ACCT_NO", "ACCT_NO_EXT", 
        "FLOOR_LMT_INDI", "CRB_EXCP_FILE_IND", "POS_CH_AUTH_SER_PCAS", "ACQ_REF_NUM", "ACQ_BUS_ID", 
        "PURCHASE_DATE", "DEST_AMT", "DEST_CRNCY_CODE", "SOURCE_AMT", "SOURCE_CRNCY_CODE", "MRCNT_NAME", 
        "MRCNT_CITY", "MRCNT_CNTRY_CODE", "MRCNT_CTG_CODE", "MRCNT_ZIP_CODE", "MRCNT_STATE_PROV_CODE", 
        "REQ_PAYMT_SER", "NUM_PAYMT_FORMS", "USAGE_CODE", "REASON_CODE", "SETTL_FLG", "AUTH_CHAR_INDI", 
        "AUTH_CODE", "POS_TERMINAL_CAP", "RESERVED", "CARDHOLD_ID_METHOD", "COLL_ONLY_FLG", "POS_ENTRY_MODE", 
        "CEN_PROCESS_DATE_YDDD", "REIMBURSEMENT_ATTR", "RECON_FLG", "RECON_LOGIC_FLG_1", "RECON_LOGIC_FLG_2", 
        "RECON_LOGIC_FLG_3", "RECON_LOGIC_FLG_4", "RECON_LOGIC_FLG_5", "ENTITY_FLG", "MODIFY_FLG", 
        "DEL_FLG", "ENTRY_USER", "MODIFY_USER", "VERIFY_USER", "ENTRY_TIME", "MODIFY_TIME", "VERIFY_TIME", 
        "AUTH_USER", "AUTH_TIME", "REPORT_CONFIRM", "REPORT_MOVED", "SRL_NUM", "MATCHING_CRITERIA", 
        "RECON_TYPE", "RECON_FLAG"
    };

    private List<String> splitCsvLine(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }


    private String cleanKey(String col) {
        if (col == null) return "";
        String s = col.trim().toUpperCase();
        if (s.contains("TRAN_RMKS")) return "TRAN_RMKS";
        if (s.contains(".")) s = s.substring(s.lastIndexOf('.') + 1);
        return s.replaceAll("[^A-Z0-9_]", "");
    }

    private String getFastCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            String typeStr = String.valueOf(cell.getCellType()).toUpperCase();
            if (typeStr.contains("STRING") || "1".equals(typeStr)) {
                String s = cell.getStringCellValue().trim();
                return s.isEmpty() ? null : s;
            } else if (typeStr.contains("NUMERIC") || "0".equals(typeStr)) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return date != null ? new SimpleDateFormat("yyyy-MM-dd").format(date) : null;
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) return String.valueOf((long) num);
                return BigDecimal.valueOf(num).toPlainString();
            } else if (typeStr.contains("BOOLEAN") || "4".equals(typeStr)) {
                return String.valueOf(cell.getBooleanCellValue());
            } else if (typeStr.contains("FORMULA") || "2".equals(typeStr)) {
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    double n = cell.getNumericCellValue();
                    if (n == (long) n) return String.valueOf((long) n);
                    return BigDecimal.valueOf(n).toPlainString();
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private String getCellFast(Row row, Integer idx) {
        if (row == null || idx == null || idx < 0) return null;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return getFastCellValue(cell);
    }

    private Map<String, Integer> processExcelFileInBatch(MultipartFile excelFile, String insertSql, String[] targetColumns, int numColumns, Timestamp currentTime, String effectiveUser, String type) throws Exception {
        int totalRows = 0;
        int totalInserted = 0;
        int duplicates = 0;
        List<Object[]> chunk = new ArrayList<>(CHUNK_SIZE);

        try (InputStream is = excelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> emptyRes = new HashMap<>();
            emptyRes.put("totalRows", 0);
            emptyRes.put("insertedRows", 0);
            emptyRes.put("duplicateRows", 0);

            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                return emptyRes;
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colMap = new HashMap<>();
            
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null) {
                    String name = getFastCellValue(cell);
                    if (name != null) colMap.put(cleanKey(name), c);
                }
            }

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Upload aborted: Process was terminated.");
                }

                Row row = sheet.getRow(r);
                if (row == null) continue;

                Object[] rowValues = new Object[numColumns];
                boolean hasData = false;

                for (int i = 0; i < numColumns; i++) {
                    String col = targetColumns[i];
                    if ("ENTRY_TIME".equals(col)) rowValues[i] = currentTime;
                    else if ("ENTRY_USER".equals(col)) rowValues[i] = effectiveUser;
                    else if ("DEL_FLAG".equals(col) && !"CBS".equalsIgnoreCase(type) && !"VISANET".equalsIgnoreCase(type) && !"EPIN".equalsIgnoreCase(type)) rowValues[i] = "N";
                    else if ("ENTITY_FLAG".equals(col) && !"CBS".equalsIgnoreCase(type) && !"VISANET".equalsIgnoreCase(type) && !"EPIN".equalsIgnoreCase(type)) rowValues[i] = "Y";
                    else if ("MODIFY_FLAG".equals(col) && !"CBS".equalsIgnoreCase(type) && !"VISANET".equalsIgnoreCase(type) && !"EPIN".equalsIgnoreCase(type)) rowValues[i] = "N";
                    else {
                        Integer cIdx = colMap.get(cleanKey(col));
                        if (cIdx == null && (col.equals("HEADX_CRD_PAN") || col.equals("HEADX_CRD_CARD_CRD_NUM"))) {
                            cIdx = colMap.get(cleanKey("HEADX_CRD_CARD_CRD_NUM"));
                            if (cIdx == null) cIdx = colMap.get(cleanKey("HEADX_CRD_PAN"));
                        }
                        if (cIdx == null && (col.equals("TXN_DT") || col.equals("TRAN_DT"))) {
                            cIdx = colMap.get(cleanKey("TRAN_DT"));
                            if (cIdx == null) cIdx = colMap.get(cleanKey("TXN_DT"));
                        }

                        if (cIdx != null && cIdx >= 0) {
                            String val = getCellFast(row, cIdx);
                            if (val != null && !val.isEmpty()) {
                                hasData = true;
                                rowValues[i] = val;
                            }
                        }
                    }
                }

                if (hasData) {
                    totalRows++;
                    chunk.add(rowValues);
                    if (chunk.size() >= CHUNK_SIZE) {
                        Map<String, Integer> batchRes = executeBatchChunkWithStats(insertSql, chunk, numColumns, type, targetColumns);
                        totalInserted += batchRes.get("inserted");
                        duplicates += batchRes.get("duplicates");
                        chunk.clear();
                    }
                }
            }

            if (!chunk.isEmpty()) {
                Map<String, Integer> batchRes = executeBatchChunkWithStats(insertSql, chunk, numColumns, type, targetColumns);
                totalInserted += batchRes.get("inserted");
                duplicates += batchRes.get("duplicates");
                chunk.clear();
            }
        }

        Map<String, Integer> res = new HashMap<>();
        res.put("totalRows", totalRows);
        res.put("insertedRows", totalInserted);
        res.put("duplicateRows", duplicates);
        return res;
    }

    private Map<String, Integer> executeBatchChunkWithStats(String sql, List<Object[]> batch, int numColumns, String type, String[] targetColumns) {
        int inserted = 0;
        int duplicates = 0;
        try {
            int[] counts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Object[] row = batch.get(i);
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
                    return batch.size();
                }
            });
            for (int c : counts) {
                if (c >= 0) inserted += c;
                else inserted += 1;
            }
        } catch (Exception e) {
            logger.warn("Batch chunk execution encountered duplicates, falling back to individual inserts: {}", e.getMessage());
            for (Object[] row : batch) {
                try {
                    jdbcTemplate.update(sql, row);
                    inserted++;
                } catch (Exception ex) {
                    duplicates++;
                    if (type != null && targetColumns != null) {
                        logFailedRecordFromValues(type, targetColumns, row, "Duplicate Record / Constraint Violation");
                    }
                }
            }
        }
        
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("inserted", inserted);
        resultMap.put("duplicates", duplicates);
        return resultMap;
    }

    public synchronized boolean isSqlLoaderAvailable() {
        if (!sqlldrEnabled) return false;
        return resolveSqlldrPath() != null;
    }

    private synchronized String resolveSqlldrPath() {
        if (cachedSqlldrPath != null) return cachedSqlldrPath;
        if (!sqlldrEnabled) return null;

        List<String> candidates = new ArrayList<>();

        // 1. Explicitly configured path from properties or environment
        if (configuredSqlldrPath != null && !configuredSqlldrPath.trim().isEmpty()) {
            candidates.add(configuredSqlldrPath.trim());
        }
        if (configuredOracleHome != null && !configuredOracleHome.trim().isEmpty()) {
            candidates.add(new File(configuredOracleHome.trim(), "bin/sqlldr.exe").getAbsolutePath());
            candidates.add(new File(configuredOracleHome.trim(), "bin/sqlldr").getAbsolutePath());
            candidates.add(new File(configuredOracleHome.trim(), "sqlldr.exe").getAbsolutePath());
            candidates.add(new File(configuredOracleHome.trim(), "sqlldr").getAbsolutePath());
        }

        // 2. ORACLE_HOME environment variable
        String envHome = System.getenv("ORACLE_HOME");
        if (envHome != null && !envHome.trim().isEmpty()) {
            candidates.add(new File(envHome.trim(), "bin/sqlldr.exe").getAbsolutePath());
            candidates.add(new File(envHome.trim(), "bin/sqlldr").getAbsolutePath());
            candidates.add(new File(envHome.trim(), "sqlldr.exe").getAbsolutePath());
            candidates.add(new File(envHome.trim(), "sqlldr").getAbsolutePath());
        }

        // 3. Locate via OS 'where.exe' (Windows) or 'which' (Linux/Unix)
        try {
            boolean isWin = System.getProperty("os.name", "").toLowerCase().contains("win");
            Process p = new ProcessBuilder(isWin ? "where.exe" : "which", "sqlldr").start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && new File(line).exists()) {
                        candidates.add(line);
                    }
                }
            }
        } catch (Exception ignored) {}

        // 4. Scan all directories in system PATH environment variable
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            for (String p : pathEnv.split(Pattern.quote(File.pathSeparator))) {
                String trimmed = p.trim().replaceAll("^\"|\"$", "");
                if (!trimmed.isEmpty()) {
                    File exe = new File(trimmed, "sqlldr.exe");
                    if (exe.exists() && exe.canExecute()) candidates.add(exe.getAbsolutePath());
                    File sh = new File(trimmed, "sqlldr");
                    if (sh.exists() && sh.canExecute()) candidates.add(sh.getAbsolutePath());
                }
            }
        }

        // 5. Dynamic search in user's profile and common installation roots
        String userName = System.getProperty("user.name");
        String envUser = System.getenv("USERNAME");
        Set<String> searchRoots = new LinkedHashSet<>();

        for (String u : new String[] { userName, envUser }) {
            if (u != null && !u.trim().isEmpty()) {
                searchRoots.add("C:\\app\\" + u.trim());
                searchRoots.add("D:\\app\\" + u.trim());
            }
        }
        searchRoots.add("C:\\app");
        searchRoots.add("D:\\app");
        searchRoots.add("E:\\app");
        searchRoots.add("C:\\oracle");
        searchRoots.add("D:\\oracle");
        searchRoots.add("E:\\oracle");
        searchRoots.add("C:\\Program Files\\Oracle");
        searchRoots.add("C:\\Program Files (x86)\\Oracle");

        // Check common Instant Client locations directly on C: and D:
        for (String drive : new String[] { "C:\\", "D:\\" }) {
            File d = new File(drive);
            if (d.exists() && d.isDirectory()) {
                File[] list = d.listFiles(f -> f.isDirectory() && f.getName().toLowerCase().contains("instantclient"));
                if (list != null) {
                    for (File ic : list) {
                        File exe = new File(ic, "sqlldr.exe");
                        if (exe.exists()) candidates.add(exe.getAbsolutePath());
                    }
                }
            }
        }

        for (String rootPath : searchRoots) {
            findSqlldrInDir(new File(rootPath), candidates, 4);
        }

        // Fallbacks
        candidates.add("sqlldr.exe");
        candidates.add("sqlldr");

        for (String cand : candidates) {
            try {
                File f = new File(cand);
                if (f.exists() && f.canExecute()) {
                    cachedSqlldrPath = f.getAbsolutePath();
                    logger.info("Oracle SQL*Loader binary auto-detected at: {}", cachedSqlldrPath);
                    return cachedSqlldrPath;
                }
            } catch (Exception ignored) {}
        }

        for (String cand : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cand, "help=y");
                String home = resolveOracleHome(cand);
                File candFile = new File(cand);
                File candDir = candFile.getParentFile();
                if (candDir != null && candDir.exists()) {
                    String curPath = pb.environment().get("PATH");
                    pb.environment().put("PATH", candDir.getAbsolutePath() + File.pathSeparator + (curPath != null ? curPath : ""));
                }
                if (home != null) {
                    pb.environment().put("ORACLE_HOME", home);
                }
                Process p = pb.start();
                try { p.getOutputStream().close(); } catch (Exception ignored) {}
                boolean finished = p.waitFor(3, TimeUnit.SECONDS);
                if (finished) {
                    cachedSqlldrPath = cand;
                    logger.info("Successfully verified SQL*Loader executable: {}", cand);
                    return cachedSqlldrPath;
                }
            } catch (Exception ignored) {}
        }

        logger.info("Oracle SQL*Loader not installed or not in PATH. System will automatically use optimized JDBC batch.");
        return null;
    }

    private void findSqlldrInDir(File dir, List<String> candidates, int maxDepth) {
        if (dir == null || !dir.exists() || !dir.isDirectory() || maxDepth <= 0) return;
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                // Check direct in folder (Instant Client layout)
                File directExe = new File(child, "sqlldr.exe");
                if (directExe.exists() && !candidates.contains(directExe.getAbsolutePath())) {
                    candidates.add(directExe.getAbsolutePath());
                }
                File directSh = new File(child, "sqlldr");
                if (directSh.exists() && !candidates.contains(directSh.getAbsolutePath())) {
                    candidates.add(directSh.getAbsolutePath());
                }
                // Check bin/ subfolder (Standard Oracle Client layout)
                File binExe = new File(child, "bin/sqlldr.exe");
                if (binExe.exists() && !candidates.contains(binExe.getAbsolutePath())) {
                    candidates.add(binExe.getAbsolutePath());
                }
                File binSh = new File(child, "bin/sqlldr");
                if (binSh.exists() && !candidates.contains(binSh.getAbsolutePath())) {
                    candidates.add(binSh.getAbsolutePath());
                }
                findSqlldrInDir(child, candidates, maxDepth - 1);
            }
        }
    }

    private String resolveOracleHome(String sqlldrPath) {
        if (configuredOracleHome != null && !configuredOracleHome.trim().isEmpty()) {
            return configuredOracleHome.trim();
        }
        String envHome = System.getenv("ORACLE_HOME");
        if (envHome != null && !envHome.trim().isEmpty()) {
            return envHome.trim();
        }
        if (sqlldrPath != null) {
            File f = new File(sqlldrPath);
            File parent = f.getParentFile();
            if (parent != null) {
                if ("bin".equalsIgnoreCase(parent.getName())) {
                    File home = parent.getParentFile();
                    if (home != null && home.exists()) return home.getAbsolutePath();
                } else if (new File(parent, "oci.dll").exists()) {
                    // Instant client folder containing oci.dll
                    return parent.getAbsolutePath();
                }
                return parent.getAbsolutePath();
            }
        }
        return null;
    }

    private String getOracleConnectString() {
        String url = (datasrcUrl != null && !datasrcUrl.trim().isEmpty()) ? datasrcUrl : "";
        if (url.isEmpty()) {
            try {
                url = jdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
            } catch (Exception ignored) {}
        }
        if (url != null && url.contains("@")) {
            String conn = url.substring(url.indexOf('@') + 1).trim();
            if (!conn.startsWith("//") && !conn.startsWith("(")) {
                conn = "//" + conn;
            }
            return conn;
        }
        return "//122.165.217.18:1521/BRECON";
    }

    private String getDbUser() {
        if (datasrcUsername != null && !datasrcUsername.trim().isEmpty()) return datasrcUsername.trim();
        return "BRECON";
    }

    private String getDbPassword() {
        if (datasrcPassword != null && !datasrcPassword.trim().isEmpty()) return datasrcPassword.trim();
        return "brecon";
    }

    private void runSqlLoaderProcess(File ctlFile, File datFile, File logFile, File badFile, File dscFile) throws Exception {
        String sqlldrPath = resolveSqlldrPath();
        if (sqlldrPath == null) throw new IllegalStateException("SQL*Loader executable not available");

        String oracleHome = resolveOracleHome(sqlldrPath);
        File sqlldrFile = new File(sqlldrPath);
        File sqlldrDir = sqlldrFile.getParentFile();

        String connectString = getOracleConnectString();
        String user = getDbUser();
        String pass = getDbPassword();

        List<String> cmd = new ArrayList<>();
        cmd.add(sqlldrPath);
        // Quoting password handles special characters like @, #, $, / in database password
        cmd.add("userid=" + user + "/\"" + pass.replace("\"", "\\\"") + "\"@" + connectString);
        cmd.add("control=\"" + ctlFile.getAbsolutePath() + "\"");
        cmd.add("data=\"" + datFile.getAbsolutePath() + "\"");
        cmd.add("log=\"" + logFile.getAbsolutePath() + "\"");
        cmd.add("bad=\"" + badFile.getAbsolutePath() + "\"");
        cmd.add("discard=\"" + dscFile.getAbsolutePath() + "\"");
        if (sqlldrDirect) {
            cmd.add("direct=true");
        } else {
            cmd.add("direct=false");
            cmd.add("rows=" + sqlldrRows);
            cmd.add("bindsize=" + sqlldrBindSize);
            cmd.add("readsize=" + sqlldrReadSize);
        }
        cmd.add("errors=" + sqlldrErrors);
        cmd.add("silent=header,feedback");

        ProcessBuilder pb = new ProcessBuilder(cmd);

        // Configure environment with SQL*Loader directory at head of PATH (ensures oci.dll resolves)
        String curPath = pb.environment().get("PATH");
        if (curPath == null) curPath = System.getenv("PATH");

        StringBuilder augmentedPath = new StringBuilder();
        if (sqlldrDir != null && sqlldrDir.exists()) {
            augmentedPath.append(sqlldrDir.getAbsolutePath()).append(File.pathSeparator);
        }
        if (oracleHome != null) {
            pb.environment().put("ORACLE_HOME", oracleHome);
            augmentedPath.append(oracleHome).append(File.separator).append("bin").append(File.pathSeparator);
            augmentedPath.append(oracleHome).append(File.pathSeparator);
        }
        if (curPath != null) {
            augmentedPath.append(curPath);
        }
        pb.environment().put("PATH", augmentedPath.toString());
        pb.environment().put("NLS_LANG", "AMERICAN_AMERICA.AL32UTF8");

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Close stdin immediately so sqlldr never hangs waiting for password prompts
        try {
            process.getOutputStream().close();
        } catch (Exception ignored) {}

        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("SQL*Loader process timed out after 5 minutes");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0 && exitCode != 2) {
            String errSnippet = extractSqlLoaderErrors(logFile);
            logger.error("SQL*Loader exited with code {}: {}. Log errors: {}", exitCode, out.toString(), errSnippet);
            throw new RuntimeException("SQL*Loader failed with exit code " + exitCode + ": " + out.toString() + 
                                       (errSnippet.isEmpty() ? "" : " Log errors: " + errSnippet));
        }
    }

    private String extractSqlLoaderErrors(File logFile) {
        if (logFile == null || !logFile.exists()) return "";
        StringBuilder errors = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("ORA-") || line.contains("SQL*Loader-") || 
                    (line.contains("Record ") && line.contains("Rejected"))) {
                    errors.append(line.trim()).append(" | ");
                }
            }
        } catch (Exception ignored) {}
        return errors.toString();
    }

    private Map<String, Integer> parseSqlLoaderLog(File logFile) {
        int loaded = 0;
        int failed = 0;
        int read = 0;

        Pattern pLoaded = Pattern.compile("([0-9,]+)\\s+Rows successfully loaded", Pattern.CASE_INSENSITIVE);
        Pattern pFailed = Pattern.compile("([0-9,]+)\\s+Rows not loaded due to data errors", Pattern.CASE_INSENSITIVE);
        Pattern pRead = Pattern.compile("Total logical records read:\\s*([0-9,]+)", Pattern.CASE_INSENSITIVE);

        if (logFile != null && logFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = pLoaded.matcher(line);
                    if (m.find()) loaded = Integer.parseInt(m.group(1).replace(",", ""));
                    m = pFailed.matcher(line);
                    if (m.find()) failed = Integer.parseInt(m.group(1).replace(",", ""));
                    m = pRead.matcher(line);
                    if (m.find()) read = Integer.parseInt(m.group(1).replace(",", ""));
                }
            } catch (Exception e) {
                logger.error("Error reading SQL*Loader log: {}", e.getMessage());
            }
        }
        Map<String, Integer> res = new HashMap<>();
        res.put("total", read > 0 ? read : (loaded + failed));
        res.put("inserted", loaded);
        res.put("failed", failed);
        return res;
    }

    private int streamCsvToStaging(MultipartFile file, File datFile, String[] targetColumns, String timeStampStr, String dateStr, String effectiveUser, String type) throws Exception {
        boolean isAtm = "ATM".equalsIgnoreCase(type);
        boolean isPos = "POS".equalsIgnoreCase(type);
        boolean isCbs = "CBS".equalsIgnoreCase(type);
        boolean isEpin = "EPIN".equalsIgnoreCase(type);
        boolean isEpin745 = "VISANET".equalsIgnoreCase(type) || "VISA_NET".equalsIgnoreCase(type) ||
                            "EPIN745".equalsIgnoreCase(type) || "EP745".equalsIgnoreCase(type);

        int rowCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 65536);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datFile), StandardCharsets.UTF_8), 65536)) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) return 0;

            char delimiter = headerLine.contains(",") ? ',' : (headerLine.contains("|") ? '|' : '\t');
            List<String> headers = splitCsvLine(headerLine, delimiter);
            Map<String, Integer> colIndexMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String norm = cleanKey(headers.get(i));
                if (!norm.isEmpty()) colIndexMap.put(norm, i);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Upload aborted: Process was terminated.");
                }
                if (line.trim().isEmpty()) continue;

                List<String> tokens = splitCsvLine(line, delimiter);
                boolean hasData = false;
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < targetColumns.length; i++) {
                    String col = targetColumns[i];
                    String val = "";

                    if ("ENTRY_TIME".equals(col)) {
                        val = (isEpin745 || isEpin) ? dateStr : timeStampStr;
                    } else if ("ENTRY_USER".equals(col)) {
                        val = effectiveUser;
                    } else if ("DEL_FLAG".equals(col) && !isCbs && !isEpin745 && !isEpin) {
                        val = "N";
                    } else if ("ENTITY_FLAG".equals(col) && !isCbs && !isEpin745 && !isEpin) {
                        val = "Y";
                    } else if ("MODIFY_FLAG".equals(col) && !isCbs && !isEpin745 && !isEpin) {
                        val = "N";
                    } else {
                        Integer idx = colIndexMap.get(cleanKey(col));
                        if (idx == null && (col.equals("HEADX_CRD_PAN") || col.equals("HEADX_CRD_CARD_CRD_NUM") || col.equals("ACCT_NO"))) {
                            idx = colIndexMap.get(cleanKey("HEADX_CRD_CARD_CRD_NUM"));
                            if (idx == null) idx = colIndexMap.get(cleanKey("HEADX_CRD_PAN"));
                            if (idx == null) idx = colIndexMap.get(cleanKey("ACCT_NO"));
                        }
                        if (idx == null && (col.equals("TXN_DT") || col.equals("TRAN_DT") || col.equals("PURCHASE_DATE"))) {
                            idx = colIndexMap.get(cleanKey("TRAN_DT"));
                            if (idx == null) idx = colIndexMap.get(cleanKey("TXN_DT"));
                            if (idx == null) idx = colIndexMap.get(cleanKey("PURCHASE_DATE"));
                        }

                        if (idx != null && idx >= 0 && idx < tokens.size()) {
                            String t = tokens.get(idx);
                            if (t != null && !t.isEmpty()) {
                                hasData = true;
                                val = t.replaceAll("^\"|\"$", "").replace("\r", " ").replace("\n", " ").replace("|^|", " ").trim();
                            }
                        }
                    }

                    sb.append(val);
                    if (i < targetColumns.length - 1) sb.append("|^|");
                }

                if (hasData) {
                    rowCount++;
                    sb.append("\n");
                    writer.write(sb.toString());
                }
            }
        }
        return rowCount;
    }

    private int streamExcelToStaging(MultipartFile excelFile, File datFile, String[] targetColumns, String timeStampStr, String dateStr, String effectiveUser, String type) throws Exception {
        boolean isAtm = "ATM".equalsIgnoreCase(type);
        boolean isPos = "POS".equalsIgnoreCase(type);
        boolean isCbs = "CBS".equalsIgnoreCase(type);
        boolean isEpin = "EPIN".equalsIgnoreCase(type);
        boolean isEpin745 = "VISANET".equalsIgnoreCase(type) || "VISA_NET".equalsIgnoreCase(type) ||
                            "EPIN745".equalsIgnoreCase(type) || "EP745".equalsIgnoreCase(type);

        int rowCount = 0;
        try (InputStream is = excelFile.getInputStream();
             Workbook workbook = WorkbookFactory.create(is);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datFile), StandardCharsets.UTF_8), 65536)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) return 0;

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colMap = new HashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null) {
                    String name = getFastCellValue(cell);
                    if (name != null) colMap.put(cleanKey(name), c);
                }
            }

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                boolean hasData = false;
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < targetColumns.length; i++) {
                    String col = targetColumns[i];
                    String val = "";

                    if ("ENTRY_TIME".equals(col)) {
                        val = (isEpin745 || isEpin) ? dateStr : timeStampStr;
                    } else if ("ENTRY_USER".equals(col)) {
                        val = effectiveUser;
                    } else if ("DEL_FLAG".equals(col) && !isCbs && !isEpin745 && !isEpin) {
                        val = "N";
                    } else if ("ENTITY_FLAG".equals(col) && !isCbs && !isEpin745 && !isEpin) {
                        val = "Y";
                    } else if ("MODIFY_FLAG".equals(col) && !isCbs && !isEpin745 && !isEpin) {
                        val = "N";
                    } else {
                        Integer cIdx = colMap.get(cleanKey(col));
                        if (cIdx == null && (col.equals("HEADX_CRD_PAN") || col.equals("HEADX_CRD_CARD_CRD_NUM") || col.equals("ACCT_NO"))) {
                            cIdx = colMap.get(cleanKey("HEADX_CRD_CARD_CRD_NUM"));
                            if (cIdx == null) cIdx = colMap.get(cleanKey("HEADX_CRD_PAN"));
                            if (cIdx == null) cIdx = colMap.get(cleanKey("ACCT_NO"));
                        }
                        if (cIdx == null && (col.equals("TXN_DT") || col.equals("TRAN_DT") || col.equals("PURCHASE_DATE"))) {
                            cIdx = colMap.get(cleanKey("TRAN_DT"));
                            if (cIdx == null) cIdx = colMap.get(cleanKey("TXN_DT"));
                            if (cIdx == null) cIdx = colMap.get(cleanKey("PURCHASE_DATE"));
                        }

                        if (cIdx != null && cIdx >= 0) {
                            String cellVal = getCellFast(row, cIdx);
                            if (cellVal != null && !cellVal.isEmpty()) {
                                hasData = true;
                                val = cellVal.replace("\r", " ").replace("\n", " ").replace("|^|", " ").trim();
                            }
                        }
                    }

                    sb.append(val);
                    if (i < targetColumns.length - 1) sb.append("|^|");
                }

                if (hasData) {
                    rowCount++;
                    sb.append("\n");
                    writer.write(sb.toString());
                }
            }
        }
        return rowCount;
    }

    private void generateControlFile(File ctlFile, File datFile, String targetTable, String[] targetColumns) throws Exception {
        StringBuilder ctl = new StringBuilder();
        ctl.append("OPTIONS (ERRORS=").append(sqlldrErrors)
           .append(", ROWS=").append(sqlldrRows)
           .append(", BINDSIZE=").append(sqlldrBindSize)
           .append(", READSIZE=").append(sqlldrReadSize).append(")\n");
        ctl.append("LOAD DATA\n");
        ctl.append("CHARACTERSET UTF8\n");
        ctl.append("INFILE '").append(datFile.getAbsolutePath().replace("\\", "/")).append("' \"STR '\\n'\"\n");
        ctl.append("APPEND\n");
        ctl.append("INTO TABLE ").append(targetTable).append("\n");
        ctl.append("FIELDS TERMINATED BY '|^|'\n");
        ctl.append("TRAILING NULLCOLS\n(\n");

        boolean isAtmOrPos = targetTable.toUpperCase().contains("SWITCH_ATM") || targetTable.toUpperCase().contains("SWITCH_POS");
        boolean isEpin745 = targetTable.toUpperCase().contains("VISA_EPIN745");
        boolean isEpin = targetTable.toUpperCase().contains("VISA_EPIN") && !isEpin745;

        for (int i = 0; i < targetColumns.length; i++) {
            String col = targetColumns[i];
            if ("ENTRY_TIME".equals(col)) {
                if (isAtmOrPos) {
                    ctl.append("  \"").append(col).append("\" TIMESTAMP \"YYYY-MM-DD HH24:MI:SS.FF\"");
                } else if (isEpin745 || isEpin) {
                    ctl.append("  \"").append(col).append("\" DATE \"YYYY-MM-DD HH24:MI:SS\"");
                } else {
                    ctl.append("  \"").append(col).append("\" CHAR(4000)");
                }
            } else if ("PURCHASE_DATE".equals(col) && isEpin) {
                ctl.append("  \"").append(col).append("\" DATE \"YYYY-MM-DD\"");
            } else {
                ctl.append("  \"").append(col).append("\" CHAR(4000)");
            }

            if (i < targetColumns.length - 1) ctl.append(",\n");
            else ctl.append("\n");
        }
        ctl.append(")\n");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ctlFile), StandardCharsets.UTF_8))) {
            bw.write(ctl.toString());
        }
    }

    public Map<String, Integer> uploadTableWithSqlLoader(MultipartFile file, String targetTable, String[] targetColumns, String type, String effectiveUser) throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "brecon_loader");
        if (!tempDir.exists()) tempDir.mkdirs();

        String runId = "ldr_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
        File datFile = new File(tempDir, runId + ".dat");
        File ctlFile = new File(tempDir, runId + ".ctl");
        File logFile = new File(tempDir, runId + ".log");
        File badFile = new File(tempDir, runId + ".bad");
        File dscFile = new File(tempDir, runId + ".dsc");

        String timeStampStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(new Date());
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try {
            int rowCount = 0;
            if (fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
                rowCount = streamCsvToStaging(file, datFile, targetColumns, timeStampStr, dateStr, effectiveUser, type);
            } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                rowCount = streamExcelToStaging(file, datFile, targetColumns, timeStampStr, dateStr, effectiveUser, type);
            } else {
                rowCount = streamCsvToStaging(file, datFile, targetColumns, timeStampStr, dateStr, effectiveUser, type);
            }

            if (rowCount == 0) {
                Map<String, Integer> empty = new HashMap<>();
                empty.put("total", 0);
                empty.put("inserted", 0);
                empty.put("failed", 0);
                return empty;
            }

            generateControlFile(ctlFile, datFile, targetTable, targetColumns);
            runSqlLoaderProcess(ctlFile, datFile, logFile, badFile, dscFile);

            Map<String, Integer> stats = parseSqlLoaderLog(logFile);
            int rejectedCount = 0;
            if (badFile.exists() && badFile.length() > 0) {
                rejectedCount = logRejectedRecordsFromBadFile(badFile, type, targetColumns, null);
            }
            if (rejectedCount > stats.getOrDefault("failed", 0)) {
                stats.put("failed", rejectedCount);
            }

            if (stats.getOrDefault("inserted", 0) == 0 && rowCount > 0) {
                String errSnippet = extractSqlLoaderErrors(logFile);
                logger.warn("SQL*Loader inserted 0 out of {} records for {}. Errors: {}. Triggering auto-fallback to JDBC batch.",
                            rowCount, targetTable, errSnippet);
                throw new RuntimeException("SQL*Loader inserted 0 rows for " + targetTable + ": " + errSnippet);
            }

            logger.info("SQL*Loader completed for {}: Read={}, Inserted={}, Failed={}, LoggedRejected={}", 
                        targetTable, stats.get("total"), stats.get("inserted"), stats.get("failed"), rejectedCount);
            return stats;
        } finally {
            try { if (datFile.exists()) datFile.delete(); } catch (Exception ignored) {}
            try { if (ctlFile.exists()) ctlFile.delete(); } catch (Exception ignored) {}
            try { if (dscFile.exists()) dscFile.delete(); } catch (Exception ignored) {}
            try { if (badFile.exists()) badFile.delete(); } catch (Exception ignored) {}
            try { if (logFile.exists()) logFile.delete(); } catch (Exception ignored) {}
        }
    }

    public Map<String, Integer> uploadRawEpinWithSqlLoader(MultipartFile file, String reportDateStr) throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "brecon_loader");
        if (!tempDir.exists()) tempDir.mkdirs();

        String runId = "epin_raw_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
        File datFile = new File(tempDir, runId + ".dat");
        File ctlFile = new File(tempDir, runId + ".ctl");
        File logFile = new File(tempDir, runId + ".log");
        File badFile = new File(tempDir, runId + ".bad");
        File dscFile = new File(tempDir, runId + ".dsc");

        try {
            int rows = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 65536);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datFile), StandardCharsets.UTF_8), 65536)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    rows++;
                    writer.write(line.replace("\r", " ").replace("\n", " ").replace("|^|", " ") + "|^|" + reportDateStr + "\n");
                }
            }

            if (rows == 0) {
                Map<String, Integer> empty = new HashMap<>();
                empty.put("total", 0);
                empty.put("inserted", 0);
                empty.put("failed", 0);
                return empty;
            }

            String ctlContent =
                "OPTIONS (ERRORS=" + sqlldrErrors + ", ROWS=" + sqlldrRows + ", BINDSIZE=" + sqlldrBindSize + ", READSIZE=" + sqlldrReadSize + ")\n" +
                "LOAD DATA\n" +
                "CHARACTERSET UTF8\n" +
                "INFILE '" + datFile.getAbsolutePath().replace("\\", "/") + "' \"STR '\\n'\"\n" +
                "APPEND\n" +
                "INTO TABLE BRECON.BRECON_EPIN_TEXT_TABLE\n" +
                "FIELDS TERMINATED BY '|^|'\n" +
                "TRAILING NULLCOLS\n" +
                "(\n" +
                "  \"TEXTDATA\" CHAR(4000),\n" +
                "  \"UP_DATE\" DATE \"DD-MM-YY\"\n" +
                ")\n";

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ctlFile), StandardCharsets.UTF_8))) {
                bw.write(ctlContent);
            }

            runSqlLoaderProcess(ctlFile, datFile, logFile, badFile, dscFile);
            Map<String, Integer> stats = parseSqlLoaderLog(logFile);
            if (stats.getOrDefault("total", 0) == 0 && rows > 0) {
                String errSnippet = extractSqlLoaderErrors(logFile);
                logger.warn("SQL*Loader loaded 0 out of {} raw EPIN records. Errors: {}. Falling back to JDBC batch.", rows, errSnippet);
                throw new RuntimeException("SQL*Loader loaded 0 raw EPIN records: " + errSnippet);
            }
            return stats;
        } finally {
            try { if (datFile.exists()) datFile.delete(); } catch (Exception ignored) {}
            try { if (ctlFile.exists()) ctlFile.delete(); } catch (Exception ignored) {}
            try { if (dscFile.exists()) dscFile.delete(); } catch (Exception ignored) {}
        }
    }

    public Map<String, Integer> uploadEpin745WithSqlLoader(MultipartFile file, String effectiveUser) throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "brecon_loader");
        if (!tempDir.exists()) tempDir.mkdirs();

        String runId = "epin745_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
        File datFile = new File(tempDir, runId + ".dat");
        File ctlFile = new File(tempDir, runId + ".ctl");
        File logFile = new File(tempDir, runId + ".log");
        File badFile = new File(tempDir, runId + ".bad");
        File dscFile = new File(tempDir, runId + ".dsc");

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            int count = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 65536);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datFile), StandardCharsets.UTF_8), 65536)) {
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
                        String tranCurr = padded.substring(114, 118).trim();
                        String settAmtRaw = padded.substring(118, 130).replaceAll("[^0-9.-]", "").trim();
                        String settInd = padded.substring(130, 132).trim();

                        StringBuilder row = new StringBuilder();
                        row.append(batchNum).append("|^|")
                           .append(tranDate).append("|^|")
                           .append(tranTime).append("|^|")
                           .append(cardNum).append("|^|")
                           .append(rrn).append("|^|")
                           .append(traceNum).append("|^|")
                           .append(issuerId).append("|^|")
                           .append(tranType).append("|^|")
                           .append(processCode).append("|^|")
                           .append(entMode).append("|^|")
                           .append(cnStp).append("|^|")
                           .append(rspCd).append("|^|")
                           .append(tranAmtStr).append("|^|")
                           .append(tranCurr).append("|^|")
                           .append(settAmtRaw).append("|^|")
                           .append(settInd).append("|^|")
                           .append("VISA CAMEA").append("|^|")
                           .append("Y").append("|^|")
                           .append("N").append("|^|")
                           .append("N").append("|^|")
                           .append(effectiveUser).append("|^|")
                           .append(dateStr).append("\n");

                        writer.write(row.toString());
                        count++;
                    }
                }
            }

            if (count == 0) {
                Map<String, Integer> empty = new HashMap<>();
                empty.put("total", 0);
                empty.put("inserted", 0);
                empty.put("failed", 0);
                return empty;
            }

            String ctlContent =
                "OPTIONS (ERRORS=" + sqlldrErrors + ", ROWS=" + sqlldrRows + ", BINDSIZE=" + sqlldrBindSize + ", READSIZE=" + sqlldrReadSize + ")\n" +
                "LOAD DATA\n" +
                "CHARACTERSET UTF8\n" +
                "INFILE '" + datFile.getAbsolutePath().replace("\\", "/") + "' \"STR '\\n'\"\n" +
                "APPEND\n" +
                "INTO TABLE BRECON.VISA_EPIN745\n" +
                "FIELDS TERMINATED BY '|^|'\n" +
                "TRAILING NULLCOLS\n" +
                "(\n" +
                "  \"BATCH_NUM\" CHAR(4000),\n" +
                "  \"TRAN_DATE\" CHAR(4000),\n" +
                "  \"TRAN_TIME\" CHAR(4000),\n" +
                "  \"CARD_NUM\" CHAR(4000),\n" +
                "  \"RET_REF_NUM\" CHAR(4000),\n" +
                "  \"TRACE_NUM\" CHAR(4000),\n" +
                "  \"ISSUER_ID\" CHAR(4000),\n" +
                "  \"TRAN_TYPE\" CHAR(4000),\n" +
                "  \"PROCESS_CODE\" CHAR(4000),\n" +
                "  \"ENT_MODE\" CHAR(4000),\n" +
                "  \"CN_STP\" CHAR(4000),\n" +
                "  \"RSP_CD\" CHAR(4000),\n" +
                "  \"TRAN_AMOUNT\" CHAR(4000),\n" +
                "  \"TRAN_CURRENCY\" CHAR(4000),\n" +
                "  \"SETT_AMOUNT\" CHAR(4000),\n" +
                "  \"SETT_INDICATOR\" CHAR(4000),\n" +
                "  \"NAT_INTNAT\" CHAR(4000),\n" +
                "  \"ENTITY_FLG\" CHAR(4000),\n" +
                "  \"MODIFY_FLG\" CHAR(4000),\n" +
                "  \"DEL_FLG\" CHAR(4000),\n" +
                "  \"ENTRY_USER\" CHAR(4000),\n" +
                "  \"ENTRY_TIME\" DATE \"YYYY-MM-DD HH24:MI:SS\"\n" +
                ")\n";

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ctlFile), StandardCharsets.UTF_8))) {
                bw.write(ctlContent);
            }

            runSqlLoaderProcess(ctlFile, datFile, logFile, badFile, dscFile);
            Map<String, Integer> stats = parseSqlLoaderLog(logFile);
            int rejectedCount = 0;
            if (badFile.exists() && badFile.length() > 0) {
                rejectedCount = logRejectedEpin745BadFile(badFile, "VISANET");
            }
            if (rejectedCount > stats.getOrDefault("failed", 0)) {
                stats.put("failed", rejectedCount);
            }
            if (stats.getOrDefault("inserted", 0) == 0 && count > 0) {
                String errSnippet = extractSqlLoaderErrors(logFile);
                logger.warn("SQL*Loader inserted 0 out of {} records for EPIN745. Errors: {}. Falling back to JDBC batch.", count, errSnippet);
                throw new RuntimeException("SQL*Loader inserted 0 EPIN745 rows: " + errSnippet);
            }
            return stats;
        } finally {
            try { if (datFile.exists()) datFile.delete(); } catch (Exception ignored) {}
            try { if (ctlFile.exists()) ctlFile.delete(); } catch (Exception ignored) {}
            try { if (dscFile.exists()) dscFile.delete(); } catch (Exception ignored) {}
            try { if (badFile.exists()) badFile.delete(); } catch (Exception ignored) {}
            try { if (logFile.exists()) logFile.delete(); } catch (Exception ignored) {}
        }
    }

    private Map<String, Integer> uploadTableWithOptimizedBatch(MultipartFile file, String targetTable, String[] targetColumns, String type, String effectiveUser) throws Exception {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isCbs = "CBS".equalsIgnoreCase(type);
        boolean isEpin745 = "VISANET".equalsIgnoreCase(type) || "VISA_NET".equalsIgnoreCase(type) ||
                            "EPIN745".equalsIgnoreCase(type) || "EP745".equalsIgnoreCase(type);

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
        final String insertSql = sql.toString();
        final int numColumns = targetColumns.length;
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        int totalRows = 0;
        int totalInserted = 0;
        int totalDuplicates = 0;
        List<Object[]> chunk = new ArrayList<>(CHUNK_SIZE);

        if (fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 65536)) {
                String headerLine = reader.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) {
                    Map<String, Integer> res = new HashMap<>();
                    res.put("totalRows", 0);
                    res.put("insertedRows", 0);
                    res.put("duplicateRows", 0);
                    return res;
                }

                char delimiter = headerLine.contains(",") ? ',' : (headerLine.contains("|") ? '|' : '\t');
                List<String> headers = splitCsvLine(headerLine, delimiter);
                Map<String, Integer> colIndexMap = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String norm = cleanKey(headers.get(i));
                    if (!norm.isEmpty()) colIndexMap.put(norm, i);
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Upload aborted: Process was terminated.");
                    }
                    if (line.trim().isEmpty()) continue;

                    List<String> tokens = splitCsvLine(line, delimiter);
                    Object[] rowValues = new Object[numColumns];
                    boolean hasData = false;

                    for (int i = 0; i < numColumns; i++) {
                        String col = targetColumns[i];
                        if ("ENTRY_TIME".equals(col)) rowValues[i] = currentTime;
                        else if ("ENTRY_USER".equals(col)) rowValues[i] = effectiveUser;
                        else if ("DEL_FLAG".equals(col) && !isCbs && !isEpin745) rowValues[i] = "N";
                        else if ("ENTITY_FLAG".equals(col) && !isCbs && !isEpin745) rowValues[i] = "Y";
                        else if ("MODIFY_FLAG".equals(col) && !isCbs && !isEpin745) rowValues[i] = "N";
                        else {
                            Integer idx = colIndexMap.get(cleanKey(col));
                            if (idx == null && (col.equals("HEADX_CRD_PAN") || col.equals("HEADX_CRD_CARD_CRD_NUM"))) {
                                idx = colIndexMap.get(cleanKey("HEADX_CRD_CARD_CRD_NUM"));
                                if (idx == null) idx = colIndexMap.get(cleanKey("HEADX_CRD_PAN"));
                            }
                            if (idx == null && (col.equals("TXN_DT") || col.equals("TRAN_DT"))) {
                                idx = colIndexMap.get(cleanKey("TRAN_DT"));
                                if (idx == null) idx = colIndexMap.get(cleanKey("TXN_DT"));
                            }

                            if (idx != null && idx >= 0 && idx < tokens.size()) {
                                String val = tokens.get(idx).trim().replaceAll("^\"|\"$", "");
                                if (!val.isEmpty()) {
                                    hasData = true;
                                    rowValues[i] = val;
                                }
                            }
                        }
                    }

                    if (hasData) {
                        totalRows++;
                        chunk.add(rowValues);
                        if (chunk.size() >= CHUNK_SIZE) {
                            Map<String, Integer> batchRes = executeBatchChunkWithStats(insertSql, chunk, numColumns, type, targetColumns);
                            totalInserted += batchRes.get("inserted");
                            totalDuplicates += batchRes.get("duplicates");
                            chunk.clear();
                        }
                    }
                }
            }
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            Map<String, Integer> excelRes = processExcelFileInBatch(file, insertSql, targetColumns, numColumns, currentTime, effectiveUser, type);
            totalRows = excelRes.get("totalRows");
            totalInserted = excelRes.get("insertedRows");
            totalDuplicates = excelRes.get("duplicateRows");
        }

        if (!chunk.isEmpty()) {
            Map<String, Integer> batchRes = executeBatchChunkWithStats(insertSql, chunk, numColumns, type, targetColumns);
            totalInserted += batchRes.get("inserted");
            totalDuplicates += batchRes.get("duplicates");
            chunk.clear();
        }

        Map<String, Integer> res = new HashMap<>();
        res.put("totalRows", totalRows);
        res.put("insertedRows", totalInserted);
        res.put("duplicateRows", totalDuplicates);
        return res;
    }

    public Map<String, Object> uploadTransactionFile(MultipartFile file, String type, String userId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isCbs = "CBS".equalsIgnoreCase(type);
        boolean isEpin = "EPIN".equalsIgnoreCase(type);
        boolean isAtm = "ATM".equalsIgnoreCase(type);
        boolean isPos = "POS".equalsIgnoreCase(type);
        boolean isEpin745 = "VISANET".equalsIgnoreCase(type) || "VISA_NET".equalsIgnoreCase(type) ||
                            "EPIN745".equalsIgnoreCase(type) || "EP745".equalsIgnoreCase(type);

        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";
        long startTime = System.currentTimeMillis();

        // 1. Raw EPIN text file
        if (isEpin && fileName.endsWith(".txt")) {
            String reportDateStr = new SimpleDateFormat("dd-MM-yy").format(new Date());
            try { jdbcTemplate.update("TRUNCATE TABLE BRECON.BRECON_EPIN_TEXT_TABLE"); } catch (Exception ignored) {}

            boolean sqlldrSuccess = false;
            int totalRead = 0;
            if (isSqlLoaderAvailable()) {
                try {
                    logger.info("Starting ultra-fast SQL*Loader execution for Raw EPIN: {}", file.getOriginalFilename());
                    Map<String, Integer> ldrRes = uploadRawEpinWithSqlLoader(file, reportDateStr);
                    totalRead = ldrRes.getOrDefault("total", 0);
                    sqlldrSuccess = true;
                } catch (Exception ex) {
                    logger.warn("SQL*Loader failed for raw EPIN, falling back to batch JDBC: {}", ex.getMessage());
                }
            }

            if (!sqlldrSuccess) {
                logger.info("Loading Raw EPIN via JDBC Batch: {}", file.getOriginalFilename());
                totalRead = uploadRawEpinTextFile(file, reportDateStr);
            }

            int duplicateCount = verifyAndLogEpinDuplicates(reportDateStr);
            executeCompanyEpinProcedure(reportDateStr);

            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRECON.VISA_EPIN", Integer.class);
            int totalInDb = count != null ? count : totalRead;
            int insertedThisRun = Math.max(0, totalRead - duplicateCount);

            result.put("totalRows", totalRead);
            result.put("insertedRows", insertedThisRun);
            result.put("duplicateRows", duplicateCount);
            result.put("failedRows", duplicateCount);
            result.put("totalRowsInDb", totalInDb);
            return result;
        }

        // 2. EPIN745 fixed-width report
        if (isEpin745 && (fileName.endsWith(".txt") || fileName.endsWith(".csv"))) {
            int inserted = 0;
            int failed = 0;
            int total = 0;
            boolean sqlldrSuccess = false;
            if (isSqlLoaderAvailable()) {
                try {
                    logger.info("Starting ultra-fast SQL*Loader execution for EPIN745: {}", file.getOriginalFilename());
                    Map<String, Integer> ldrRes = uploadEpin745WithSqlLoader(file, effectiveUser);
                    inserted = ldrRes.getOrDefault("inserted", 0);
                    failed = ldrRes.getOrDefault("failed", 0);
                    total = ldrRes.getOrDefault("total", inserted + failed);
                    sqlldrSuccess = true;
                } catch (Exception ex) {
                    logger.warn("SQL*Loader failed for EPIN745, falling back to batch JDBC: {}", ex.getMessage());
                }
            }

            if (!sqlldrSuccess) {
                logger.info("Loading EPIN745 via JDBC Batch: {}", file.getOriginalFilename());
                inserted = uploadEpin745File(file, userId);
                total = inserted;
            }

            Integer totalInDb = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRECON.VISA_EPIN745", Integer.class);
            result.put("totalRows", total);
            result.put("insertedRows", inserted);
            result.put("duplicateRows", failed);
            result.put("failedRows", failed);
            result.put("totalRowsInDb", totalInDb != null ? totalInDb : inserted);
            return result;
        }

        // 3. ATM, POS, CBS, VISANET standard table upload
        String targetTable = isAtm ? "BRECON.SWITCH_ATM" :
                             isPos ? "BRECON.SWITCH_POS" :
                             isCbs ? "BRECON.CBS_TXN" :
                             isEpin ? "BRECON.VISA_EPIN" : "BRECON.VISA_EPIN745";

        String[] targetColumns = isCbs ? CBS_COLUMNS :
                                 isPos ? POS_COLUMNS :
                                 isEpin ? EPIN_COLUMNS :
                                 isEpin745 ? EPIN745_COLUMNS : DB_COLUMNS;

        int totalRows = 0;
        int totalInserted = 0;
        int totalDuplicates = 0;

        boolean sqlldrSuccess = false;
        if (isSqlLoaderAvailable()) {
            try {
                logger.info("Starting ultra-fast SQL*Loader bulk upload for [{}] into {}: {}", type, targetTable, file.getOriginalFilename());
                Map<String, Integer> ldrRes = uploadTableWithSqlLoader(file, targetTable, targetColumns, type, effectiveUser);
                totalRows = ldrRes.getOrDefault("total", 0);
                totalInserted = ldrRes.getOrDefault("inserted", 0);
                totalDuplicates = ldrRes.getOrDefault("failed", 0);
                sqlldrSuccess = true;
            } catch (Exception ex) {
                logger.warn("SQL*Loader failed for {}, falling back to optimized JDBC batch: {}", targetTable, ex.getMessage());
            }
        }

        if (!sqlldrSuccess) {
            logger.info("Loading [{}] into {} via optimized JDBC Batch: {}", type, targetTable, file.getOriginalFilename());
            Map<String, Integer> batchRes = uploadTableWithOptimizedBatch(file, targetTable, targetColumns, type, effectiveUser);
            totalRows = batchRes.getOrDefault("totalRows", 0);
            totalInserted = batchRes.getOrDefault("insertedRows", 0);
            totalDuplicates = batchRes.getOrDefault("duplicateRows", 0);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Upload Completed for [{}] in {} ms: {} total, {} inserted, {} duplicates", 
                    type, elapsed, totalRows, totalInserted, totalDuplicates);

        Integer totalInDb = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + targetTable, Integer.class);

        result.put("totalRows", totalRows);
        result.put("insertedRows", totalInserted);
        result.put("duplicateRows", totalDuplicates);
        result.put("failedRows", totalDuplicates);
        result.put("totalRowsInDb", totalInDb != null ? totalInDb : totalInserted);
        return result;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedRecord(String src, String card, String rrn, String auth, String amt, String dt, String reason) {
        try {
            Integer existingPendingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BRECON.UPLOAD_FAILED_RECORDS " +
                "WHERE UPPER(SOURCE_TYPE) = UPPER(?) " +
                "  AND NVL(RAW_RRN, 'N/A') = NVL(?, 'N/A') " +
                "  AND NVL(RAW_AUTH_ID, 'N/A') = NVL(?, 'N/A') " +
                "  AND NVL(RAW_TRAN_DATE, 'N/A') = NVL(?, 'N/A') " +
                "  AND STATUS = 'PENDING'",
                Integer.class,
                src, rrn, auth, dt
            );

            if (existingPendingCount != null && existingPendingCount > 0) return;

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

    public int logRejectedRecordsFromBadFile(File badFile, String sourceType, String[] targetColumns, String defaultReason) {
        if (badFile == null || !badFile.exists() || badFile.length() == 0) {
            return 0;
        }

        String typeUpper = (sourceType != null) ? sourceType.trim().toUpperCase() : "UNKNOWN";
        if ("EPIN745".equals(typeUpper) || "EP745".equals(typeUpper)) typeUpper = "VISANET";

        int cardIdx = -1;
        int rrnIdx = -1;
        int authIdx = -1;
        int amtIdx = -1;
        int dateIdx = -1;

        for (int i = 0; i < targetColumns.length; i++) {
            String col = targetColumns[i].toUpperCase();
            if (cardIdx == -1 && (col.equals("HEADX_CRD_PAN") || col.equals("HEADX_CRD_CARD_CRD_NUM") || col.equals("ACID") || col.equals("CARD_NUM") || col.equals("ACCT_NO") || col.equals("PRNT_ADVC_IND"))) {
                cardIdx = i;
            }
            if (rrnIdx == -1 && (col.equals("AUTHX_ORIG_OSEQ_NUM") || col.equals("AUTHX_SEQ_NUM") || col.equals("TRAN_ID") || col.equals("RET_REF_NUM") || col.equals("ACQ_REF_NUM") || col.equals("PSTD_USER_ID"))) {
                rrnIdx = i;
            }
            if (authIdx == -1 && (col.equals("AUTHX_AUTH_ID_RESP") || col.equals("AUTHX_APPRV_CDE") || col.equals("PART_TRAN_SRL_NUM") || col.equals("TRACE_NUM") || col.equals("AUTH_CODE"))) {
                authIdx = i;
            }
            if (amtIdx == -1 && (col.equals("AUTHX_AMT_1") || col.equals("TRAN_AMT") || col.equals("TRAN_AMOUNT") || col.equals("DEST_AMT"))) {
                amtIdx = i;
            }
            if (dateIdx == -1 && (col.equals("TXN_DT") || col.equals("TRAN_DT") || col.equals("TRAN_DATE") || col.equals("PURCHASE_DATE"))) {
                dateIdx = i;
            }
        }

        List<Object[]> failedRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(badFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\^\\|", -1);

                String card = (cardIdx >= 0 && cardIdx < parts.length) ? parts[cardIdx].trim() : "";
                String rrn = (rrnIdx >= 0 && rrnIdx < parts.length) ? parts[rrnIdx].trim() : "";
                String auth = (authIdx >= 0 && authIdx < parts.length) ? parts[authIdx].trim() : "";
                String amt = (amtIdx >= 0 && amtIdx < parts.length) ? parts[amtIdx].trim() : "";
                String dt = (dateIdx >= 0 && dateIdx < parts.length) ? parts[dateIdx].trim() : "";

                if (card.length() > 100) card = card.substring(0, 100);
                if (rrn.length() > 100) rrn = rrn.substring(0, 100);
                if (auth.length() > 100) auth = auth.substring(0, 100);
                if (amt.length() > 50) amt = amt.substring(0, 50);
                if (dt.length() > 50) dt = dt.substring(0, 50);

                String reason = defaultReason != null ? defaultReason : "Duplicate Record / Constraint Violation";
                failedRows.add(new Object[] { typeUpper, card, rrn, auth, amt, dt, reason });
            }
        } catch (Exception e) {
            logger.error("Error reading bad file {}: {}", badFile.getAbsolutePath(), e.getMessage());
        }

        if (failedRows.isEmpty()) return 0;

        String insertFailedSql = 
            "INSERT INTO BRECON.UPLOAD_FAILED_RECORDS " +
            "(SOURCE_TYPE, RAW_CARD_NO, RAW_RRN, RAW_AUTH_ID, RAW_AMOUNT, RAW_TRAN_DATE, ERROR_REASON, STATUS, CREATED_AT) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', SYSDATE)";

        int insertedCount = 0;
        try {
            int[] counts = jdbcTemplate.batchUpdate(insertFailedSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Object[] row = failedRows.get(i);
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setString(4, (String) row[3]);
                    ps.setString(5, (String) row[4]);
                    ps.setString(6, (String) row[5]);
                    ps.setString(7, (String) row[6]);
                }
                @Override
                public int getBatchSize() {
                    return failedRows.size();
                }
            });
            for (int c : counts) {
                if (c >= 0) insertedCount += c;
                else insertedCount++;
            }
        } catch (Exception e) {
            logger.warn("Batch failed record insert had error, inserting individually: {}", e.getMessage());
            for (Object[] row : failedRows) {
                try {
                    logFailedRecord((String) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4], (String) row[5], (String) row[6]);
                    insertedCount++;
                } catch (Exception ignored) {}
            }
        }

        logger.info("Recorded {} rejected/duplicate records into UPLOAD_FAILED_RECORDS for {}", insertedCount, typeUpper);
        return insertedCount;
    }

    public int logRejectedEpin745BadFile(File badFile, String sourceType) {
        if (badFile == null || !badFile.exists() || badFile.length() == 0) {
            return 0;
        }

        List<Object[]> failedRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(badFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\^\\|", -1);

                String dt = (parts.length > 1) ? parts[1].trim() : "";
                String card = (parts.length > 3) ? parts[3].trim() : "";
                String rrn = (parts.length > 4) ? parts[4].trim() : "";
                String auth = (parts.length > 5) ? parts[5].trim() : "";
                String amt = (parts.length > 12) ? parts[12].trim() : "";

                if (card.length() > 100) card = card.substring(0, 100);
                if (rrn.length() > 100) rrn = rrn.substring(0, 100);
                if (auth.length() > 100) auth = auth.substring(0, 100);
                if (amt.length() > 50) amt = amt.substring(0, 50);
                if (dt.length() > 50) dt = dt.substring(0, 50);

                String reason = "Duplicate Record: Violates unique constraint (CARD_NUM, TRACE_NUM, TRAN_DATE, TRAN_AMOUNT)";
                failedRows.add(new Object[] { "VISANET", card, rrn, auth, amt, dt, reason });
            }
        } catch (Exception e) {
            logger.error("Error reading EPIN745 bad file {}: {}", badFile.getAbsolutePath(), e.getMessage());
        }

        if (failedRows.isEmpty()) return 0;

        String insertFailedSql = 
            "INSERT INTO BRECON.UPLOAD_FAILED_RECORDS " +
            "(SOURCE_TYPE, RAW_CARD_NO, RAW_RRN, RAW_AUTH_ID, RAW_AMOUNT, RAW_TRAN_DATE, ERROR_REASON, STATUS, CREATED_AT) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', SYSDATE)";

        int insertedCount = 0;
        try {
            int[] counts = jdbcTemplate.batchUpdate(insertFailedSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Object[] row = failedRows.get(i);
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setString(4, (String) row[3]);
                    ps.setString(5, (String) row[4]);
                    ps.setString(6, (String) row[5]);
                    ps.setString(7, (String) row[6]);
                }
                @Override
                public int getBatchSize() {
                    return failedRows.size();
                }
            });
            for (int c : counts) {
                if (c >= 0) insertedCount += c;
                else insertedCount++;
            }
        } catch (Exception e) {
            logger.warn("Batch failed record insert had error, inserting individually: {}", e.getMessage());
            for (Object[] row : failedRows) {
                try {
                    logFailedRecord((String) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4], (String) row[5], (String) row[6]);
                    insertedCount++;
                } catch (Exception ignored) {}
            }
        }

        logger.info("Recorded {} rejected/duplicate records into UPLOAD_FAILED_RECORDS for VISANET", insertedCount);
        return insertedCount;
    }

    public int verifyAndLogEpinDuplicates(String reportDateStr) {
        String detectDuplicatesSql = 
            "INSERT INTO BRECON.UPLOAD_FAILED_RECORDS " +
            "(SOURCE_TYPE, RAW_CARD_NO, RAW_RRN, RAW_AUTH_ID, RAW_AMOUNT, RAW_TRAN_DATE, ERROR_REASON, STATUS, CREATED_AT) " +
            "SELECT " +
            "  'EPIN', " +
            "  T.ACCT_NO, " +
            "  T.ACQ_REF_NUM, " +
            "  T.AUTH_CODE, " +
            "  TO_CHAR(T.DEST_AMT), " +
            "  T.PURCHASE_DATE, " +
            "  CASE WHEN T.EXISTING_COUNT > 0 THEN 'Duplicate Record: Already exists in VISA_EPIN' " +
            "       ELSE 'Duplicate Record: Multiple instances in uploaded file' END, " +
            "  'PENDING', " +
            "  SYSDATE " +
            "FROM ( " +
            "  SELECT " +
            "    TRIM(SUBSTR(TEXTDATA, 20, 16)) AS ACCT_NO, " +
            "    TRIM(SUBSTR(TEXTDATA, 40, 12)) AS ACQ_REF_NUM, " +
            "    TRIM(SUBSTR(TEXTDATA, 65, 6)) AS AUTH_CODE, " +
            "    NVL(TO_NUMBER(NULLIF(REGEXP_REPLACE(SUBSTR(TEXTDATA, 100, 13), '[^0-9.]', ''), '')), 0) AS DEST_AMT, " +
            "    TO_CHAR(TRUNC(SYSDATE), 'YYYY-MM-DD') AS PURCHASE_DATE, " +
            "    ROW_NUMBER() OVER ( " +
            "      PARTITION BY TRIM(SUBSTR(TEXTDATA, 20, 16)), TRIM(SUBSTR(TEXTDATA, 65, 6)), NVL(TO_NUMBER(NULLIF(REGEXP_REPLACE(SUBSTR(TEXTDATA, 100, 13), '[^0-9.]', ''), '')), 0) " +
            "      ORDER BY SRL_NO ASC " +
            "    ) AS RN, " +
            "    (SELECT COUNT(*) FROM BRECON.VISA_EPIN V " +
            "     WHERE V.ACCT_NO = TRIM(SUBSTR(B.TEXTDATA, 20, 16)) " +
            "       AND V.AUTH_CODE = TRIM(SUBSTR(B.TEXTDATA, 65, 6)) " +
            "       AND V.PURCHASE_DATE = TRUNC(SYSDATE) " +
            "       AND V.DEST_AMT = NVL(TO_NUMBER(NULLIF(REGEXP_REPLACE(SUBSTR(B.TEXTDATA, 100, 13), '[^0-9.]', ''), '')), 0) " +
            "    ) AS EXISTING_COUNT " +
            "  FROM BRECON.BRECON_EPIN_TEXT_TABLE B " +
            "  WHERE TO_CHAR(UP_DATE, 'DD-MM-YY') = ? " +
            "    AND TRIM(TEXTDATA) IS NOT NULL " +
            "    AND LENGTH(TRIM(TEXTDATA)) >= 70 " +
            ") T " +
            "WHERE (T.RN > 1 OR T.EXISTING_COUNT > 0) " +
            "  AND NOT EXISTS ( " +
            "    SELECT 1 FROM BRECON.UPLOAD_FAILED_RECORDS U " +
            "    WHERE U.SOURCE_TYPE = 'EPIN' " +
            "      AND U.RAW_CARD_NO = T.ACCT_NO " +
            "      AND U.RAW_AUTH_ID = T.AUTH_CODE " +
            "      AND U.RAW_AMOUNT = TO_CHAR(T.DEST_AMT) " +
            "      AND U.STATUS = 'PENDING' " +
            "  )";

        try {
            int logged = jdbcTemplate.update(detectDuplicatesSql, reportDateStr);
            logger.info("Detected and recorded {} duplicate EPIN records into UPLOAD_FAILED_RECORDS", logged);
            return logged;
        } catch (Exception e) {
            logger.error("Error verifying EPIN duplicates: {}", e.getMessage());
            return 0;
        }
    }

    public void logFailedRecordFromValues(String type, String[] targetColumns, Object[] rowValues, String reason) {
        try {
            String typeUpper = (type != null) ? type.trim().toUpperCase() : "UNKNOWN";
            if ("EPIN745".equals(typeUpper) || "EP745".equals(typeUpper)) typeUpper = "VISANET";

            int cardIdx = -1, rrnIdx = -1, authIdx = -1, amtIdx = -1, dateIdx = -1;
            for (int i = 0; i < targetColumns.length; i++) {
                String col = targetColumns[i].toUpperCase();
                if (cardIdx == -1 && (col.equals("HEADX_CRD_PAN") || col.equals("HEADX_CRD_CARD_CRD_NUM") || col.equals("ACID") || col.equals("CARD_NUM") || col.equals("ACCT_NO") || col.equals("PRNT_ADVC_IND"))) cardIdx = i;
                if (rrnIdx == -1 && (col.equals("AUTHX_ORIG_OSEQ_NUM") || col.equals("AUTHX_SEQ_NUM") || col.equals("TRAN_ID") || col.equals("RET_REF_NUM") || col.equals("ACQ_REF_NUM") || col.equals("PSTD_USER_ID"))) rrnIdx = i;
                if (authIdx == -1 && (col.equals("AUTHX_AUTH_ID_RESP") || col.equals("AUTHX_APPRV_CDE") || col.equals("PART_TRAN_SRL_NUM") || col.equals("TRACE_NUM") || col.equals("AUTH_CODE"))) authIdx = i;
                if (amtIdx == -1 && (col.equals("AUTHX_AMT_1") || col.equals("TRAN_AMT") || col.equals("TRAN_AMOUNT") || col.equals("DEST_AMT"))) amtIdx = i;
                if (dateIdx == -1 && (col.equals("TXN_DT") || col.equals("TRAN_DT") || col.equals("TRAN_DATE") || col.equals("PURCHASE_DATE"))) dateIdx = i;
            }

            String card = (cardIdx >= 0 && cardIdx < rowValues.length && rowValues[cardIdx] != null) ? rowValues[cardIdx].toString() : "";
            String rrn = (rrnIdx >= 0 && rrnIdx < rowValues.length && rowValues[rrnIdx] != null) ? rowValues[rrnIdx].toString() : "";
            String auth = (authIdx >= 0 && authIdx < rowValues.length && rowValues[authIdx] != null) ? rowValues[authIdx].toString() : "";
            String amt = (amtIdx >= 0 && amtIdx < rowValues.length && rowValues[amtIdx] != null) ? rowValues[amtIdx].toString() : "";
            String dt = (dateIdx >= 0 && dateIdx < rowValues.length && rowValues[dateIdx] != null) ? rowValues[dateIdx].toString() : "";

            logFailedRecord(typeUpper, card, rrn, auth, amt, dt, reason);
        } catch (Exception ex) {
            logger.warn("Could not log individual batch duplicate: {}", ex.getMessage());
        }
    }

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

        String sql = "INSERT INTO BRECON.VISA_EPIN745 (" +
                     "BATCH_NUM, TRAN_DATE, TRAN_TIME, CARD_NUM, RET_REF_NUM, TRACE_NUM, " +
                     "ISSUER_ID, TRAN_TYPE, PROCESS_CODE, ENT_MODE, CN_STP, RSP_CD, " +
                     "TRAN_AMOUNT, TRAN_CURRENCY, SETT_AMOUNT, SETT_INDICATOR, NAT_INTNAT, " +
                     "ENTITY_FLG, MODIFY_FLG, DEL_FLG, ENTRY_USER, ENTRY_TIME, SRL_NUM) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)";

        int inserted = 0;
        try {
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
            for (int c : updateCounts) {
                if (c >= 0) inserted += c;
                else inserted++;
            }
        } catch (Exception ex) {
            logger.warn("EPIN745 batch encountered duplicates, falling back to individual inserts: {}", ex.getMessage());
            for (Object[] row : batchParams) {
                try {
                    jdbcTemplate.update(sql, row);
                    inserted++;
                } catch (Exception e) {
                    String dt = (String) row[1];
                    String card = (String) row[3];
                    String rrn = (String) row[4];
                    String auth = (String) row[5];
                    String amt = (row[12] != null) ? row[12].toString() : "";
                    logFailedRecord("VISANET", card, rrn, auth, amt, dt, "Duplicate Record: Violates unique constraint (CARD_NUM, TRACE_NUM, TRAN_DATE, TRAN_AMOUNT)");
                }
            }
        }

        return inserted;
    }

    public void exportDataToExcel(String type, HttpServletResponse response) throws Exception {
        String tableName = "";
        String fileNamePrefix = "";

        if (type == null) throw new IllegalArgumentException("Type cannot be null");

        switch (type.toUpperCase().trim()) {
            case "ATM":
                tableName = "BRECON.SWITCH_ATM";
                fileNamePrefix = "ATM_Source_Data";
                break;
            case "POS":
                tableName = "BRECON.SWITCH_POS";
                fileNamePrefix = "POS_Source_Data";
                break;
            case "CBS":
                tableName = "BRECON.CBS_TXN";
                fileNamePrefix = "CBS_Source_Data";
                break;
            case "EPIN":
                tableName = "BRECON.VISA_EPIN";
                fileNamePrefix = "EPIN_Source_Data";
                break;
            case "VISANET":
            case "EP745":
                tableName = "BRECON.VISA_EPIN745";
                fileNamePrefix = "VisaNet_EP745_Source_Data";
                break;
            default:
                throw new IllegalArgumentException("Invalid source data type: " + type);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileNamePrefix + "_" + System.currentTimeMillis() + ".xlsx");

        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT * FROM " + tableName);

        try (Workbook workbook = new XSSFWorkbook(); ServletOutputStream outputStream = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet(type.toUpperCase() + "_Data");

            if (!records.isEmpty()) {
                Row headerRow = sheet.createRow(0);
                Set<String> columns = records.get(0).keySet();
                int colIdx = 0;
                for (String col : columns) {
                    Cell cell = headerRow.createCell(colIdx++);
                    cell.setCellValue(col);
                }

                int rowIdx = 1;
                for (Map<String, Object> record : records) {
                    Row row = sheet.createRow(rowIdx++);
                    int cIdx = 0;
                    for (String col : columns) {
                        Cell cell = row.createCell(cIdx++);
                        Object val = record.get(col);
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                }
            }

            workbook.write(outputStream);
            outputStream.flush();
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