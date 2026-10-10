package com.bornfire.recon.services;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FatcaManagementService {

    private static final Logger logger = LoggerFactory.getLogger(FatcaManagementService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Full 56 columns of BRECON.FACTA_TRANSCATION_DATA
    private static final String[] DB_COLUMNS = new String[] {
        "GRP_ID", "ENTITY_CODE", "ENTITY_TYPE", "RESIDENT_FLAG", "ENTITY_NAME", 
        "ENTITY_OTHER_NAME", "DOB", "SEX", "COUNTRY_CODE", "PASSPORT_NO", 
        "ADDRESS1", "ADDRESS2", "ADDRESS3", "ADDRESS4", "ADDRESS5", 
        "REF_NO", "CREDIT_TYPE", "DATE_APPROVED", "PARENT_CO_NO", "PARENT_CO_NAME", 
        "SECTOR_LOAN_CLASS", "CURR", "AMOUNT_ORG", "AMOUNT_OUT", "AMOUNT_DIS", 
        "DATE_UPDATE", "AMOUNT_INST", "DATE_FIRST_INST", "PERIODICITY", "DATE_LAST_INST", 
        "DATE_EXP", "NO_INST", "DATE_DEFAULT", "BAL_DEFAULT", "AMOUNT_ARRS", 
        "TYPE_CLASS", "FIRST_COLL", "SECOND_COLL", "THIRD_COLL", "FOURTH_COLL", 
        "FIFTH_COLL", "REMARKS", "RECORD_STATUS", "UNIQUE_REF_ID", "DATE_REGULARISED", 
        "ACTION_TAKEN", "ACTION_DATE", "ENTRY_TIME", "ENTRY_USER", "MODIFY_TIME", 
        "MODIFY_USER", "VERIFY_TIME", "VERIFY_USER", "DEL_FLAG", "MODIFY_FLAG", "ENTITY_FLAG"
    };

    private static final Set<String> DATE_COLUMNS = new HashSet<>(Arrays.asList(
        "DOB", "DATE_APPROVED", "DATE_UPDATE", "DATE_FIRST_INST", "DATE_LAST_INST", 
        "DATE_EXP", "DATE_DEFAULT", "DATE_REGULARISED", "ACTION_DATE"
    ));

    private static final Set<String> NUMERIC_COLUMNS = new HashSet<>(Arrays.asList(
        "AMOUNT_ORG", "AMOUNT_OUT", "AMOUNT_DIS", "AMOUNT_INST", "PERIODICITY", 
        "NO_INST", "BAL_DEFAULT", "AMOUNT_ARRS"
    ));

    private String cleanKey(String col) {
        if (col == null) return "";
        return col.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "");
    }

    private Date parseDate(Object cellVal) {
        if (cellVal == null) return null;
        if (cellVal instanceof java.util.Date) {
            return new Date(((java.util.Date) cellVal).getTime());
        }
        String s = cellVal.toString().trim();
        if (s.isEmpty()) return null;
        String[] formats = {"yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "ddMMyyyy", "yyyyMMdd"};
        for (String fmt : formats) {
            try {
                return new Date(new SimpleDateFormat(fmt).parse(s).getTime());
            } catch (Exception ignored) {}
        }
        return null;
    }

    private BigDecimal parseBigDecimal(Object cellVal) {
        if (cellVal == null) return null;
        try {
            String s = cellVal.toString().replaceAll("[^0-9.-]", "").trim();
            if (s.isEmpty()) return null;
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public int uploadFatcaFile(MultipartFile file, String userId) throws Exception {
        List<Object[]> batchParams = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) return 0;

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null) {
                    colIndexMap.put(cleanKey(formatter.formatCellValue(cell)), cell.getColumnIndex());
                }
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Object[] rowValues = new Object[DB_COLUMNS.length];
                boolean hasData = false;

                for (int i = 0; i < DB_COLUMNS.length; i++) {
                    String col = DB_COLUMNS[i];

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
                    } else if ("MODIFY_TIME".equals(col) || "MODIFY_USER".equals(col) ||
                               "VERIFY_TIME".equals(col) || "VERIFY_USER".equals(col)) {
                        rowValues[i] = null;
                    } else {
                        Integer idx = colIndexMap.get(cleanKey(col));
                        if (idx != null && idx >= 0) {
                            Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            if (cell != null) {
                                hasData = true;
                                // ✅ Safe, version-independent date parser:
								if (DATE_COLUMNS.contains(col)) {
								    try {
								        if (DateUtil.isCellDateFormatted(cell)) {
								            java.util.Date d = cell.getDateCellValue();
								            rowValues[i] = (d != null) ? new Date(d.getTime()) : null;
								        } else {
								            rowValues[i] = parseDate(formatter.formatCellValue(cell));
								        }
								    } catch (Exception e) {
								        rowValues[i] = parseDate(formatter.formatCellValue(cell));
								    }
								} else if (NUMERIC_COLUMNS.contains(col)) {
                                    rowValues[i] = parseBigDecimal(formatter.formatCellValue(cell));
                                } else {
                                    String str = formatter.formatCellValue(cell).trim();
                                    rowValues[i] = str.isEmpty() ? null : str;
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

        if (batchParams.isEmpty()) return 0;

        StringBuilder sql = new StringBuilder("INSERT INTO BRECON.FACTA_TRANSCATION_DATA (");
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

        final int totalCols = DB_COLUMNS.length;
        int[] updateCounts = jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] row = batchParams.get(i);
                for (int c = 0; c < totalCols; c++) {
                    Object val = row[c];
                    if (val == null) {
                        ps.setNull(c + 1, Types.VARCHAR);
                    } else if (val instanceof Timestamp) {
                        ps.setTimestamp(c + 1, (Timestamp) val);
                    } else if (val instanceof Date) {
                        ps.setDate(c + 1, (Date) val);
                    } else if (val instanceof BigDecimal) {
                        ps.setBigDecimal(c + 1, (BigDecimal) val);
                    } else {
                        ps.setString(c + 1, val.toString());
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return batchParams.size();
            }
        });

        logger.info("Successfully inserted {} records into BRECON.FACTA_TRANSCATION_DATA", updateCounts.length);
        return updateCounts.length;
    }

    public Map<String, Object> getFatcaDashboardData() {
        Map<String, Object> data = new HashMap<>();

        int total = 0, pending = 0, completed = 0, error = 0;
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y'", Integer.class);
            total = count != null ? count : 0;

            List<Map<String, Object>> statusList = jdbcTemplate.queryForList(
                "SELECT NVL(RECORD_STATUS, 'PENDING') AS STAT, COUNT(*) AS CNT " +
                "FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' GROUP BY NVL(RECORD_STATUS, 'PENDING')"
            );

            for (Map<String, Object> r : statusList) {
                String st = String.valueOf(r.get("STAT")).toUpperCase();
                int cnt = ((Number) r.get("CNT")).intValue();
                if (st.contains("A") || st.contains("COMPLET")) completed += cnt;
                else if (st.contains("ERR") || st.contains("REJ")) error += cnt;
                else pending += cnt;
            }
        } catch (Exception ex) {
            logger.warn("Querying FACTA_TRANSCATION_DATA counts: {}", ex.getMessage());
        }

        data.put("totalRecords", total);
        data.put("pendingRecords", pending);
        data.put("completedRecords", completed);
        data.put("errorRecords", error);

        List<Map<String, Object>> records = new ArrayList<>();
        try {
            records = jdbcTemplate.queryForList(
                "SELECT REF_NO, UNIQUE_REF_ID, ENTITY_NAME, " +
                "TO_CHAR(ENTRY_TIME, 'YYYY-MM-DD HH24:MI:SS') AS UPLOAD_DATE, " +
                "NVL(RECORD_STATUS, 'Pending') AS STATUS, AMOUNT_ORG " +
                "FROM BRECON.FACTA_TRANSCATION_DATA " +
                "WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND ROWNUM <= 100 ORDER BY ENTRY_TIME DESC"
            );
        } catch (Exception ex) {
            logger.warn("Querying FACTA_TRANSCATION_DATA records: {}", ex.getMessage());
        }
        data.put("records", records);

        return data;
    }

    // 1. Update record status from Modal edit form
    @Transactional
    public boolean updateFatcaRecord(String refNo, String status, String userId) {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA " +
                         "SET RECORD_STATUS = ?, " +
                         "    MODIFY_TIME = SYSTIMESTAMP, " +
                         "    MODIFY_USER = ?, " +
                         "    MODIFY_FLAG = 'Y' " +
                         "WHERE REF_NO = ? AND NVL(DEL_FLAG, 'N') <> 'Y'";

            int rowsUpdated = jdbcTemplate.update(sql, 
                status, 
                (userId != null && !userId.isEmpty()) ? userId : "SYSTEM", 
                refNo
            );

            logger.info("Updated status to [{}] for REF_NO [{}] - rows affected: {}", status, refNo, rowsUpdated);
            return rowsUpdated > 0;
        } catch (Exception e) {
            logger.error("Error updating FATCA record for REF_NO: " + refNo, e);
            return false;
        }
    }

    // 2. Generate XML output matching MCIB / FATCA standards from Oracle DB
    public String generateFatcaXml() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT GRP_ID, ENTITY_CODE, ENTITY_TYPE, COUNTRY_CODE, " +
                         "       NVL(RECORD_STATUS, 'A') AS RECORD_STATUS, REF_NO, UNIQUE_REF_ID, " +
                         "       CREDIT_TYPE, CURR, AMOUNT_ARRS, TYPE_CLASS, " +
                         "       TO_CHAR(DATE_DEFAULT, 'DDMMYYYY') AS DATE_DEF_STR, " +
                         "       TO_CHAR(DATE_UPDATE, 'DDMMYYYY') AS DATE_UPD_STR " +
                         "FROM BRECON.FACTA_TRANSCATION_DATA " +
                         "WHERE NVL(DEL_FLAG, 'N') <> 'Y'";
            list = jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("Error reading FATCA records for XML generation", e);
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<MCIB>\n");
        xml.append("  <BANK grp_id=\"BARB\">\n");

        for (Map<String, Object> r : list) {
            xml.append("    <CREDIT>\n");
            xml.append("      <ENTITY_CODE>").append(escapeXml(r.get("ENTITY_CODE"))).append("</ENTITY_CODE>\n");
            xml.append("      <ENTITY_TYPE>").append(escapeXml(r.get("ENTITY_TYPE"))).append("</ENTITY_TYPE>\n");
            xml.append("      <COUNTRY_CODE>").append(escapeXml(r.get("COUNTRY_CODE"))).append("</COUNTRY_CODE>\n");
            xml.append("      <RECORD_STATUS>").append(escapeXml(r.get("RECORD_STATUS"))).append("</RECORD_STATUS>\n");
            xml.append("      <REF_NO>").append(escapeXml(r.get("REF_NO"))).append("</REF_NO>\n");
            xml.append("      <UNIQUE_REF_ID>").append(escapeXml(r.get("UNIQUE_REF_ID"))).append("</UNIQUE_REF_ID>\n");
            xml.append("      <CREDIT_TYPE>").append(escapeXml(r.get("CREDIT_TYPE"))).append("</CREDIT_TYPE>\n");
            xml.append("      <CURR>").append(escapeXml(r.get("CURR"))).append("</CURR>\n");
            xml.append("      <DATE_UPDATE>").append(escapeXml(r.get("DATE_UPD_STR"))).append("</DATE_UPDATE>\n");
            xml.append("      <AMOUNT_ARRS>").append(escapeXml(r.get("AMOUNT_ARRS"))).append("</AMOUNT_ARRS>\n");
            xml.append("      <DATE_DEFAULT>").append(escapeXml(r.get("DATE_DEF_STR"))).append("</DATE_DEFAULT>\n");
            xml.append("      <TYPE_CLASS>").append(escapeXml(r.get("TYPE_CLASS"))).append("</TYPE_CLASS>\n");
            xml.append("    </CREDIT>\n");
        }

        xml.append("  </BANK>\n");
        xml.append("</MCIB>");
        return xml.toString();
    }

    private String escapeXml(Object val) {
        if (val == null) return "";
        return val.toString()
                  .replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}