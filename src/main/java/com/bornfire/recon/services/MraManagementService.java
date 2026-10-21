package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MraManagementService {

    private static final Logger logger = LoggerFactory.getLogger(MraManagementService.class);
    private static final int CHUNK_SIZE = 1000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String[] SFX_COLUMNS = new String[] {
        "ID", "BRN", "NATIONALITY", "PASSPORT", "SURNAME", "OTHERNAME", 
        "AMOUNT", "FOREIGN_CURRENCY", "TYPE_OF_TRANSACTION", "EQUIVALENT_IN_MUR"
    };

    private static final String[] SFT_COLUMNS = new String[] {
        "ID", "BRN", "NATIONALITY", "PASSPORT", "SURNAME", "OTHERNAME", 
        "JOINT_ACCOUNT", "ACCOUNT_NUMBER", "TOTAL_DEPOSITS", "BALANCE"
    };

    private String cleanKey(String col) {
        if (col == null) return "";
        String s = col.trim().toUpperCase();
        if (s.contains(".")) s = s.substring(s.lastIndexOf('.') + 1);
        s = s.replaceAll("[\\s\\-]+", "_");
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
                // Use BigDecimal.toPlainString() to strictly avoid scientific notation (E+13)
                BigDecimal bd = BigDecimal.valueOf(num);
                return bd.stripTrailingZeros().toPlainString();
            } else if (typeStr.contains("BOOLEAN") || "4".equals(typeStr)) {
                return String.valueOf(cell.getBooleanCellValue());
            } else if (typeStr.contains("FORMULA") || "2".equals(typeStr)) {
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    double n = cell.getNumericCellValue();
                    return BigDecimal.valueOf(n).stripTrailingZeros().toPlainString();
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }
    private String mapColumn(String rawHeader) {
        String clean = cleanKey(rawHeader);
        switch (clean) {
            case "ID":
            case "NATIONAL_ID":
            case "NIC": return "ID";
            case "BRN":
            case "BUSINESS_REGISTRATION_NUMBER": return "BRN";
            case "NATIONALITY":
            case "COUNTRY": return "NATIONALITY";
            case "PASSPORT":
            case "PASSPORT_NO":
            case "PASSPORT_NUMBER": return "PASSPORT";
            case "SURNAME":
            case "LAST_NAME":
            case "LASTNAME": return "SURNAME";
            case "OTHERNAME":
            case "OTHER_NAME":
            case "OTHER_NAMES":
            case "FIRST_NAME":
            case "FIRSTNAME": return "OTHERNAME";
            
            // SFX specific columns
            case "AMOUNT":
            case "FOREIGN_AMOUNT": return "AMOUNT";
            case "FOREIGN_CURRENCY":
            case "FOREIGNCURRENCY":
            case "CURRENCY":
            case "CURR": return "FOREIGN_CURRENCY";
            case "TYPE_OF_TRANSACTION":
            case "TYPEOFTRANSACTION":
            case "TRANSACTION_TYPE":
            case "TXN_TYPE": return "TYPE_OF_TRANSACTION";
            case "EQUIVALENT_IN_MUR":
            case "EQUIVALENTINMUR":
            case "MUR_EQUIVALENT":
            case "AMOUNT_IN_MUR": return "EQUIVALENT_IN_MUR";

            // SFT specific columns
            case "JOINT_ACCOUNT":
            case "JOINTACCOUNT":
            case "JOINT": return "JOINT_ACCOUNT";
            case "ACCOUNT_NUMBER":
            case "ACCOUNTNUMBER":
            case "ACCOUNT_NO":
            case "ACC_NO":
            case "ACCOUNT": return "ACCOUNT_NUMBER";
            case "TOTAL_DEPOSITS":
            case "TOTALDEPOSITS":
            case "DEPOSITS":
            case "AGGREGATE_DEPOSITS": return "TOTAL_DEPOSITS";
            case "BALANCE":
            case "ACCOUNT_BALANCE":
            case "CLOSING_BALANCE": return "BALANCE";
            default: return clean;
        }
    }

    // 1. Get Paginated Dashboard Records
    public Map<String, Object> getMraDashboardData(String type, int page, int size, String search, String status) {
        Map<String, Object> data = new HashMap<>();
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";

        int total = 0, pending = 0, completed = 0, errorAndDup = 0;
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE NVL(DEL_FLAG, 'N') <> 'Y'", Integer.class);
            total = count != null ? count : 0;

            List<Map<String, Object>> statusList = jdbcTemplate.queryForList(
                "SELECT NVL(RECORD_STATUS, 'PENDING') AS STAT, COUNT(*) AS CNT " +
                "FROM " + tableName + " WHERE NVL(DEL_FLAG, 'N') <> 'Y' GROUP BY NVL(RECORD_STATUS, 'PENDING')"
            );

            for (Map<String, Object> r : statusList) {
                String st = String.valueOf(r.get("STAT")).toUpperCase();
                int cnt = ((Number) r.get("CNT")).intValue();
                if (st.contains("COMPLET")) completed += cnt;
                else if (st.contains("DUP") || st.contains("ERR") || st.contains("REJ")) errorAndDup += cnt;
                else pending += cnt;
            }
        } catch (Exception ex) {
            logger.warn("Querying MRA counts for {}: {}", type, ex.getMessage());
        }

        data.put("totalCount", total);
        data.put("pendingCount", pending);
        data.put("completedCount", completed);
        data.put("errorCount", errorAndDup);

        List<Map<String, Object>> records = new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            if (isSft) {
                sql.append("SELECT SFT_ID AS REPORT_ID, TO_CHAR(NVL(ACCOUNT_NUMBER, ID)) AS ACCOUNT_NUMBER, ")
                   .append("       'MRA-' || TO_CHAR(SFT_ID) AS REF_ID, ")
                   .append("       TO_CHAR(NVL(ENTRY_TIME, SYSDATE), 'YYYY-MM-DD HH24:MI:SS') AS UPLOAD_DATE, ")
                   .append("       NVL(RECORD_STATUS, 'PENDING') AS STATUS, ")
                   .append("       SURNAME AS HOLDER_NAME, NATIONALITY AS TAX_COUNTRY, ")
                   .append("       NVL(BALANCE, TOTAL_DEPOSITS) AS ACCOUNT_BALANCE, 'MUR' AS CURRENCY_CODE, ")
                   .append("       PASSPORT, JOINT_ACCOUNT AS EXTRA_COL ")
                   .append("FROM BRECON.SFT_REPORT_DATA ")
                   .append("WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'PENDING') <> 'DUPLICATE' ");
            } else {
                sql.append("SELECT SFX_ID AS REPORT_ID, TO_CHAR(ID) AS ACCOUNT_NUMBER, ")
                   .append("       'MRA-' || TO_CHAR(SFX_ID) AS REF_ID, ")
                   .append("       TO_CHAR(NVL(ENTRY_TIME, SYSDATE), 'YYYY-MM-DD HH24:MI:SS') AS UPLOAD_DATE, ")
                   .append("       NVL(RECORD_STATUS, 'PENDING') AS STATUS, ")
                   .append("       SURNAME AS HOLDER_NAME, NATIONALITY AS TAX_COUNTRY, ")
                   .append("       AMOUNT AS ACCOUNT_BALANCE, NVL(FOREIGN_CURRENCY, 'MUR') AS CURRENCY_CODE, ")
                   .append("       PASSPORT, TYPE_OF_TRANSACTION AS EXTRA_COL ")
                   .append("FROM BRECON.SFX_REPORT_DATA ")
                   .append("WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'PENDING') <> 'DUPLICATE' ");
            }
            if (search != null && !search.trim().isEmpty()) {
                sql.append("AND (LOWER(ID) LIKE ? OR LOWER(SURNAME) LIKE ? OR LOWER(NATIONALITY) LIKE ?) ");
                String sParam = "%" + search.trim().toLowerCase() + "%";
                params.add(sParam);
                params.add(sParam);
                params.add(sParam);
            }

            if (status != null && !status.trim().isEmpty()) {
                sql.append("AND UPPER(NVL(RECORD_STATUS, 'PENDING')) = ? ");
                params.add(status.trim().toUpperCase());
            }

            String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ")";
            int filteredTotal = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
            data.put("filteredTotal", filteredTotal);

            sql.append("ORDER BY 1 DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            params.add((page - 1) * size);
            params.add(size);

            records = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception ex) {
            logger.warn("Querying MRA records for {}: {}", type, ex.getMessage());
        }
        data.put("records", records); 
        return data;
    }

    @Transactional
    public int uploadMraFile(MultipartFile file, String requestedType, String userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or null.");
        }

        String fileName = file.getOriginalFilename();
        boolean isCsv = fileName != null && (fileName.toLowerCase().endsWith(".csv") || fileName.toLowerCase().endsWith(".txt"));

        Map<String, Integer> colMap = new HashMap<>();
        List<String[]> rawRows = new ArrayList<>();

        if (isCsv) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) return 0;
                headerLine = headerLine.replace("\uFEFF", "");

                String delimiter = headerLine.contains(",") ? ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" : "\\|";
                String[] headers = headerLine.split(delimiter, -1);
                for (int i = 0; i < headers.length; i++) {
                    String mappedName = mapColumn(headers[i].replace("\"", ""));
                    if (!mappedName.isEmpty()) {
                        colMap.put(mappedName, i);
                    }
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        rawRows.add(line.split(delimiter, -1));
                    }
                }
            }
        } else {
            try (InputStream is = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(is)) {
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) return 0;

                Row headerRow = sheet.getRow(0);
                int maxCols = headerRow.getLastCellNum();
                for (int c = 0; c < maxCols; c++) {
                    String val = getCellFast(headerRow, c);
                    if (val != null) {
                        String mappedName = mapColumn(val);
                        if (!mappedName.isEmpty()) {
                            colMap.put(mappedName, c);
                        }
                    }
                }

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    String[] tokens = new String[maxCols];
                    for (int c = 0; c < maxCols; c++) {
                        tokens[c] = getCellFast(row, c);
                    }
                    rawRows.add(tokens);
                }
            }
        }

        if (rawRows.isEmpty()) return 0;

        boolean hasSftHeaders = colMap.containsKey("ACCOUNT_NUMBER") 
                           || colMap.containsKey("TOTAL_DEPOSITS") 
                           || colMap.containsKey("BALANCE") 
                           || colMap.containsKey("JOINT_ACCOUNT");

        boolean hasSfxHeaders = colMap.containsKey("FOREIGN_CURRENCY") 
                           || colMap.containsKey("TYPE_OF_TRANSACTION") 
                           || colMap.containsKey("EQUIVALENT_IN_MUR");

        String targetType = (requestedType != null) ? requestedType.trim().toUpperCase() : "SFX";
        if (hasSftHeaders && !hasSfxHeaders) {
            targetType = "SFT";
        } else if (hasSfxHeaders && !hasSftHeaders) {
            targetType = "SFX";
        }

        boolean isSft = "SFT".equalsIgnoreCase(targetType);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String[] targetColumns = isSft ? SFT_COLUMNS : SFX_COLUMNS;

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");
        for (int i = 0; i < targetColumns.length; i++) {
            sql.append("\"").append(targetColumns[i]).append("\"");
            placeholders.append("?");
            if (i < targetColumns.length - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }
        sql.append(", SOURCE_FILE, RECORD_STATUS, ENTRY_USER)").append(placeholders).append(", ?, ?, ?)");
        final String insertSql = sql.toString();
        final int numColumns = targetColumns.length;

        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";
        List<Object[]> processedBatch = new ArrayList<>();

        for (String[] tokens : rawRows) {
            Object[] rowValues = new Object[numColumns + 3];
            boolean hasData = false;

            for (int i = 0; i < numColumns; i++) {
                String colName = targetColumns[i];
                Integer colIdx = colMap.get(colName);

                if (colIdx != null && colIdx >= 0 && colIdx < tokens.length) {
                    String val = tokens[colIdx] != null ? tokens[colIdx].replace("\"", "").trim() : "";
                    if (!val.isEmpty() && !"null".equalsIgnoreCase(val)) {
                        hasData = true;
                        rowValues[i] = val;
                    }
                }
            }

            rowValues[numColumns] = fileName;
            rowValues[numColumns + 1] = "PENDING";
            rowValues[numColumns + 2] = effectiveUser;

            if (hasData) {
                processedBatch.add(rowValues);
            }
        }

        if (processedBatch.isEmpty()) return 0;

        Set<String> existingKeys = new HashSet<>();
        try {
            if (isSft) {
                List<String> dbKeys = jdbcTemplate.queryForList(
                    "SELECT LOWER(TRIM(NVL(ID, NVL(PASSPORT, NVL(BRN, ''))))) || '|' || LOWER(TRIM(NVL(ACCOUNT_NUMBER, ''))) " +
                    "FROM BRECON.SFT_REPORT_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'PENDING') <> 'DUPLICATE'",
                    String.class
                );
                existingKeys.addAll(dbKeys);
            } else {
                List<String> dbKeys = jdbcTemplate.queryForList(
                    "SELECT LOWER(TRIM(NVL(ID, NVL(PASSPORT, NVL(BRN, ''))))) || '|' || LOWER(TRIM(NVL(SURNAME, ''))) || '|' || TO_CHAR(NVL(AMOUNT, 0)) || '|' || LOWER(TRIM(NVL(FOREIGN_CURRENCY, ''))) " +
                    "FROM BRECON.SFX_REPORT_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'PENDING') <> 'DUPLICATE'",
                    String.class
                );
                existingKeys.addAll(dbKeys);
            }
        } catch (Exception e) {
            logger.warn("Could not pre-fetch existing keys for duplicate check: {}", e.getMessage());
        }

        Set<String> seenInBatch = new HashSet<>();
        List<Object[]> finalBatch = new ArrayList<>();

        for (Object[] row : processedBatch) {
            String idVal = (row[0] != null) ? row[0].toString().trim().toLowerCase() : "";
            String brnVal = (row[1] != null) ? row[1].toString().trim().toLowerCase() : "";
            String passportVal = (row[3] != null) ? row[3].toString().trim().toLowerCase() : "";
            String surname = (row[4] != null) ? row[4].toString().trim().toLowerCase() : "";

            String personId = !idVal.isEmpty() ? idVal : (!passportVal.isEmpty() ? passportVal : brnVal);

            String compositeKey;
            if (isSft) {
                String accNo = (row[7] != null) ? row[7].toString().trim().toLowerCase() : "";
                compositeKey = personId + "|" + accNo;
            } else {
                String amt = (row[6] != null) ? row[6].toString().trim() : "0";
                try {
                    amt = new BigDecimal(amt).stripTrailingZeros().toPlainString();
                } catch (Exception ignored) {}
                String curr = (row[7] != null) ? row[7].toString().trim().toLowerCase() : "";
                compositeKey = personId + "|" + surname + "|" + amt + "|" + curr;
            }

            boolean isDuplicate = false;
            if (seenInBatch.contains(compositeKey) || existingKeys.contains(compositeKey)) {
                isDuplicate = true;
            } else {
                seenInBatch.add(compositeKey);
            }

            row[numColumns + 1] = isDuplicate ? "DUPLICATE" : "PENDING";
            finalBatch.add(row);
        }

        return executeBatchChunk(insertSql, finalBatch, numColumns + 3);
    }

    public List<Map<String, Object>> getMraDuplicateRecords(String type) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        try {
            String sql;
            if (isSft) {
                sql = "SELECT SFT_ID AS DUP_ID, NVL(SOURCE_FILE, 'Dataset.csv') AS FILE_NAME, " +
                      "       TO_CHAR(NVL(ACCOUNT_NUMBER, NVL(ID, PASSPORT)), 'FM999999999999999999999999999999') AS ACCOUNT_NUMBER, " +
                      "       TRIM(SURNAME || ' ' || NVL(OTHERNAME, '')) AS HOLDER_NAME, " +
                      "       NATIONALITY AS RESIDENCE_COUNTRY, " +
                      "       NVL(BALANCE, NVL(TOTAL_DEPOSITS, 0)) AS ACCOUNT_BALANCE, " +
                      "       'MUR' AS CURRENCY_CODE, 'PENDING' AS STATUS " +
                      "FROM BRECON.SFT_REPORT_DATA " +
                      "WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND RECORD_STATUS = 'DUPLICATE' " +
                      "ORDER BY SFT_ID DESC";
            } else {
                sql = "SELECT SFX_ID AS DUP_ID, NVL(SOURCE_FILE, 'Dataset.csv') AS FILE_NAME, " +
                      "       TO_CHAR(NVL(ID, NVL(PASSPORT, BRN)), 'FM999999999999999999999999999999') AS ACCOUNT_NUMBER, " +
                      "       TRIM(SURNAME || ' ' || NVL(OTHERNAME, '')) AS HOLDER_NAME, " +
                      "       NATIONALITY AS RESIDENCE_COUNTRY, " +
                      "       NVL(AMOUNT, 0) AS ACCOUNT_BALANCE, " +
                      "       NVL(FOREIGN_CURRENCY, 'MUR') AS CURRENCY_CODE, 'PENDING' AS STATUS " +
                      "FROM BRECON.SFX_REPORT_DATA " +
                      "WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND RECORD_STATUS = 'DUPLICATE' " +
                      "ORDER BY SFX_ID DESC";
            }
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("Error fetching MRA duplicate records for {}: {}", type, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public boolean rectifyDuplicateRecord(String type, Long dupId, Double updatedBalance, String updatedStatus) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String idCol = isSft ? "SFT_ID" : "SFX_ID";
        String amtCol = isSft ? "BALANCE" : "AMOUNT";
        try {
            int updated = jdbcTemplate.update(
                "UPDATE " + tableName + " SET " + amtCol + " = ?, RECORD_STATUS = ? WHERE " + idCol + " = ?",
                updatedBalance, updatedStatus.toUpperCase(), dupId
            );
            return updated > 0;
        } catch (Exception e) {
            logger.error("Error rectifying MRA duplicate", e);
            return false;
        }
    }

    @Transactional
    public boolean deleteDuplicateRecord(String type, Long dupId) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String idCol = isSft ? "SFT_ID" : "SFX_ID";
        try {
            int updated = jdbcTemplate.update("UPDATE " + tableName + " SET DEL_FLAG = 'Y' WHERE " + idCol + " = ?", dupId);
            return updated > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void deleteAllDuplicateRecords(String type) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        try {
            jdbcTemplate.update("UPDATE " + tableName + " SET DEL_FLAG = 'Y' WHERE RECORD_STATUS = 'DUPLICATE'");
        } catch (Exception ignored) {}
    }

    private String getCellFast(Row row, Integer idx) {
        if (row == null || idx == null || idx < 0) return null;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return getFastCellValue(cell);
    }

    private int executeBatchChunk(String sql, List<Object[]> batch, int totalParams) {
        int[] counts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] row = batch.get(i);
                for (int c = 0; c < totalParams; c++) {
                    Object val = (c < row.length) ? row[c] : null;
                    if (val != null) {
                        ps.setString(c + 1, val.toString());
                    } else {
                        ps.setNull(c + 1, Types.VARCHAR);
                    }
                }
            }
            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });
        return counts.length;
    }

    @Transactional
    public boolean updateMraRecord(String type, Long reportId, String accountNumber, String holderName, String taxCountry, Double balance, String currencyCode, String status) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String idCol = isSft ? "SFT_ID" : "SFX_ID";
        String amtCol = isSft ? "BALANCE" : "AMOUNT";
        try {
            int updated = jdbcTemplate.update(
                "UPDATE " + tableName + " SET ID = ?, SURNAME = ?, NATIONALITY = ?, " + amtCol + " = ?, RECORD_STATUS = ? WHERE " + idCol + " = ?",
                accountNumber, holderName, taxCountry, balance, status.toUpperCase(), reportId
            );
            return updated > 0;
        } catch (Exception e) {
            logger.error("Error updating MRA record", e);
            return false;
        }
    }

    public Map<String, Object> getMraRecordDetails(String type, Long reportId) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String idCol = isSft ? "SFT_ID" : "SFX_ID";
        try {
            return jdbcTemplate.queryForMap("SELECT * FROM " + tableName + " WHERE " + idCol + " = ? AND ROWNUM = 1", reportId);
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    public String generateSingleMraCsv(String type, Long reportId) {
        Map<String, Object> r = getMraRecordDetails(type, reportId);
        return buildCsvString(Collections.singletonList(r));
    }

    public String generateMraCsv(String type) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM " + tableName + " WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'PENDING') <> 'DUPLICATE'");
        return buildCsvString(list);
    }

    private String buildCsvString(List<Map<String, Object>> list) {
        StringBuilder csv = new StringBuilder();
        if (list == null || list.isEmpty()) return "";

        // Header row from first record keys
        Set<String> keys = list.get(0).keySet();
        csv.append(String.join(",", keys)).append("\n");

        // Data rows
        for (Map<String, Object> r : list) {
            List<String> vals = new ArrayList<>();
            for (String key : keys) {
                Object val = r.get(key);
                String valStr = val != null ? val.toString().replace("\"", "\"\"") : "";
                if (valStr.contains(",") || valStr.contains("\n") || valStr.contains("\"")) {
                    vals.add("\"" + valStr + "\"");
                } else {
                    vals.add(valStr);
                }
            }
            csv.append(String.join(",", vals)).append("\n");
        }
        return csv.toString();
    }
    
    public Map<String, Object> getMraDuplicateRecordDetails(String type, Long dupId) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String idCol = isSft ? "SFT_ID" : "SFX_ID";
        try {
            return jdbcTemplate.queryForMap("SELECT * FROM " + tableName + " WHERE " + idCol + " = ? AND ROWNUM = 1", dupId);
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }
    
    @Transactional
    public boolean rectifyDuplicateRecordFull(String type, Long dupId, Map<String, String> formParams) {
        boolean isSft = "SFT".equalsIgnoreCase(type);
        String tableName = isSft ? "BRECON.SFT_REPORT_DATA" : "BRECON.SFX_REPORT_DATA";
        String idCol = isSft ? "SFT_ID" : "SFX_ID";

        try {
            StringBuilder updateSql = new StringBuilder("UPDATE ").append(tableName)
                .append(" SET RECORD_STATUS = ?, ")
                .append("     MODIFY_TIME = SYSDATE, ")
                .append("     MODIFY_USER = 'SYSTEM', ")
                .append("     MODIFY_FLAG = 'M', ")
                .append("     ENTITY_FLAG = NVL(ENTITY_FLAG, 'A') ");

            List<Object> args = new ArrayList<>();
            
            // Resolution status (Completed or Error)
            String resolutionStatus = formParams.getOrDefault("updatedStatus", "COMPLETED").toUpperCase();
            args.add(resolutionStatus);

            // Columns to exclude from dynamic database column update
            Set<String> excludedCols = new HashSet<>(Arrays.asList(
                "DUPID", "DUP_ID", "TYPE", "UPDATEDSTATUS", "SFX_ID", "SFT_ID", 
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
            
            // Fallback: If 0 rows updated, try matching by standard ID column
            if (updated == 0) {
                updated = jdbcTemplate.update(
                    "UPDATE " + tableName + " SET RECORD_STATUS = ?, MODIFY_TIME = SYSDATE WHERE ID = ?",
                    resolutionStatus, dupId.toString()
                );
            }

            return updated > 0;
        } catch (Exception e) {
            logger.error("Error rectifying full MRA duplicate: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}