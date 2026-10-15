package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    private static final int CHUNK_SIZE = 1000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String[] DB_COLUMNS = new String[] {
        "BRN", "RES_COUNTRY_CODE", "PASSPORT_NO", "SURNAME", 
        "OTHER_NAME", "FOREIGN_AMOUNT", "FOREIGN_CURR", "TXN_TYPE", "AMOUNT_MUR", 
        "RECORD_STATUS", "DEL_FLAG", "RECORD_SOURCE", "ENTRY_TIME", "ENTRY_USER"
    };

    private String cleanKey(String col) {
        if (col == null) return "";
        String s = col.trim().toUpperCase();
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
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
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

    private String mapCsvColumnToDb(String csvHeader) {
        String clean = cleanKey(csvHeader);
        switch (clean) {
            case "ID": return "TRN_ID";
            case "BRN": return "BRN";
            case "NATIONALITY": return "RES_COUNTRY_CODE";
            case "PASSPORT": return "PASSPORT_NO";
            case "SURNAME": return "SURNAME";
            case "OTHERNAME": return "OTHER_NAME";
            case "AMOUNT": return "FOREIGN_AMOUNT";
            case "FOREIGN_CURRENCY":
            case "FOREIGNCURRENCY": return "FOREIGN_CURR";
            case "TYPE_OF_TRANSACTION":
            case "TYPEOFTRANSACTION": return "TXN_TYPE";
            case "EQUIVALENT_IN_MUR":
            case "EQUIVALENTINMUR": return "AMOUNT_MUR";
            default: return clean;
        }
    }

    // =========================================================================
    // HIGH-PERFORMANCE FILE UPLOAD WITH DUPLICATE QUARANTINE ROUTING
    // =========================================================================
    @Transactional
    public Map<String, Object> uploadFatcaFile(MultipartFile file, String userId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or null.");
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isCsv = fileName.endsWith(".csv") || fileName.endsWith(".txt");

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
        final String insertSql = sql.toString();
        final int numColumns = DB_COLUMNS.length;

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

        int totalInserted = 0;
        int totalFailed = 0;
        List<Object[]> chunk = new ArrayList<>(CHUNK_SIZE);
        Set<String> seenInBatch = new HashSet<>();

        // 1. CSV / Text Streaming Upload
        if (isCsv) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) {
                    result.put("insertedRows", 0);
                    result.put("failedRows", 0);
                    return result;
                }

                String delimiter = headerLine.contains(",") ? ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" : "\\|";
                String[] headers = headerLine.split(delimiter, -1);
                Map<String, Integer> colIndexMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String norm = mapCsvColumnToDb(headers[i].replace("\"", ""));
                    if (!norm.isEmpty()) colIndexMap.put(norm, i);
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(delimiter, -1);

                    Object[] rowValues = new Object[numColumns];
                    boolean hasData = false;
                    String surname = "";
                    String otherName = "";

                    for (int i = 0; i < numColumns; i++) {
                        String col = DB_COLUMNS[i];
                        if ("ENTRY_TIME".equals(col)) rowValues[i] = currentTime;
                        else if ("ENTRY_USER".equals(col)) rowValues[i] = effectiveUser;
                        else if ("DEL_FLAG".equals(col)) rowValues[i] = "N";
                        else if ("RECORD_SOURCE".equals(col)) rowValues[i] = "FX_CSV";
                        else if ("RECORD_STATUS".equals(col)) {
                            // Temporary placeholder evaluated below
                            rowValues[i] = "Completed";
                        } else {
                            Integer idx = colIndexMap.get(col);
                            if (idx != null && idx >= 0 && idx < tokens.length) {
                                String val = tokens[idx].replace("\"", "").trim();
                                if (!val.isEmpty() && !"null".equalsIgnoreCase(val)) {
                                    hasData = true;
                                    rowValues[i] = val;
                                    if ("SURNAME".equals(col)) surname = val.toLowerCase();
                                    if ("OTHER_NAME".equals(col)) otherName = val.toLowerCase();
                                }
                            }
                        }
                    }

                    if (hasData) {
                        // Check duplicate in batch or database
                        String compositeKey = surname + "|" + otherName;
                        boolean isDuplicate = false;
                        if (!surname.isEmpty() && seenInBatch.contains(compositeKey)) {
                            isDuplicate = true;
                        } else {
                            seenInBatch.add(compositeKey);
                        }

                        if (!isDuplicate && !surname.isEmpty()) {
                            try {
                                Integer count = jdbcTemplate.queryForObject(
                                    "SELECT COUNT(*) FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'Completed') <> 'DUPLICATE' AND LOWER(TRIM(SURNAME)) = ? AND LOWER(TRIM(NVL(OTHER_NAME, ''))) = ?",
                                    Integer.class, surname, otherName
                                );
                                if (count != null && count > 0) isDuplicate = true;
                            } catch (Exception ignored) {}
                        }

                        rowValues[9] = isDuplicate ? "DUPLICATE" : "Completed";

                        chunk.add(rowValues);
                        if (chunk.size() >= CHUNK_SIZE) {
                            totalInserted += executeBatchChunk(insertSql, chunk, numColumns);
                            chunk.clear();
                        }
                    }
                }
            }
        } 
        // 2. Excel Streaming Upload (.xlsx, .xls)
        else {
            try (InputStream is = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                    result.put("insertedRows", 0);
                    result.put("failedRows", 0);
                    return result;
                }

                Row headerRow = sheet.getRow(0);
                Map<String, Integer> colMap = new HashMap<>();
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    Cell cell = headerRow.getCell(c);
                    if (cell != null) {
                        String name = getFastCellValue(cell);
                        if (name != null) colMap.put(mapCsvColumnToDb(name), c);
                    }
                }

                int lastRow = sheet.getLastRowNum();
                for (int r = 1; r <= lastRow; r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    Object[] rowValues = new Object[numColumns];
                    boolean hasData = false;
                    String surname = "";
                    String otherName = "";

                    for (int i = 0; i < numColumns; i++) {
                        String col = DB_COLUMNS[i];
                        if ("ENTRY_TIME".equals(col)) rowValues[i] = currentTime;
                        else if ("ENTRY_USER".equals(col)) rowValues[i] = effectiveUser;
                        else if ("DEL_FLAG".equals(col)) rowValues[i] = "N";
                        else if ("RECORD_SOURCE".equals(col)) rowValues[i] = "EXCEL_UP";
                        else if ("RECORD_STATUS".equals(col)) rowValues[i] = "Completed";
                        else {
                            Integer cIdx = colMap.get(col);
                            if (cIdx != null) {
                                String val = getCellFast(row, cIdx);
                                if (val != null) {
                                    hasData = true;
                                    rowValues[i] = val;
                                    if ("SURNAME".equals(col)) surname = val.toLowerCase();
                                    if ("OTHER_NAME".equals(col)) otherName = val.toLowerCase();
                                }
                            }
                        }
                    }

                    if (hasData) {
                        String compositeKey = surname + "|" + otherName;
                        boolean isDuplicate = false;
                        if (!surname.isEmpty() && seenInBatch.contains(compositeKey)) {
                            isDuplicate = true;
                        } else {
                            seenInBatch.add(compositeKey);
                        }

                        if (!isDuplicate && !surname.isEmpty()) {
                            try {
                                Integer count = jdbcTemplate.queryForObject(
                                    "SELECT COUNT(*) FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'Completed') <> 'DUPLICATE' AND LOWER(TRIM(SURNAME)) = ? AND LOWER(TRIM(NVL(OTHER_NAME, ''))) = ?",
                                    Integer.class, surname, otherName
                                );
                                if (count != null && count > 0) isDuplicate = true;
                            } catch (Exception ignored) {}
                        }

                        rowValues[9] = isDuplicate ? "DUPLICATE" : "Completed";

                        chunk.add(rowValues);
                        if (chunk.size() >= CHUNK_SIZE) {
                            totalInserted += executeBatchChunk(insertSql, chunk, numColumns);
                            chunk.clear();
                        }
                    }
                }
            }
        }

        // Flush remaining chunk
        if (!chunk.isEmpty()) {
            totalInserted += executeBatchChunk(insertSql, chunk, numColumns);
            chunk.clear();
        }

        result.put("insertedRows", totalInserted);
        result.put("failedRows", totalFailed);
        return result;
    }

    private String getCellFast(Row row, Integer idx) {
        if (row == null || idx == null || idx < 0) return null;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return getFastCellValue(cell);
    }

  
    private int executeBatchChunk(String sql, List<Object[]> batch, int numColumns) {
        int[] counts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] row = batch.get(i);
                for (int colIndex = 0; colIndex < numColumns; colIndex++) {
                    Object val = (colIndex < row.length) ? row[colIndex] : null;
                    if (val == null) {
                        ps.setNull(colIndex + 1, Types.VARCHAR);
                    } else if (val instanceof Timestamp) {
                        ps.setTimestamp(colIndex + 1, (Timestamp) val);
                    } else if (val instanceof java.sql.Date) {
                        ps.setDate(colIndex + 1, (java.sql.Date) val);
                    } else {
                        ps.setString(colIndex + 1, val.toString());
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
    
    // =========================================================================
    // DASHBOARD & QUEUE METHODS
    // =========================================================================
    public Map<String, Object> getFatcaDashboardData() {
        Map<String, Object> data = new HashMap<>();
        int total = 0, pending = 0, completed = 0, error = 0;
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'Completed') <> 'DUPLICATE'", Integer.class);
            total = count != null ? count : 0;

            List<Map<String, Object>> statusList = jdbcTemplate.queryForList(
                "SELECT NVL(RECORD_STATUS, 'Completed') AS STAT, COUNT(*) AS CNT " +
                "FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'Completed') <> 'DUPLICATE' GROUP BY NVL(RECORD_STATUS, 'Completed')"
            );

            for (Map<String, Object> r : statusList) {
                String st = String.valueOf(r.get("STAT")).toUpperCase();
                int cnt = ((Number) r.get("CNT")).intValue();
                if (st.contains("A") || st.contains("COMPLET")) completed += cnt;
                else if (st.contains("ERR") || st.contains("REJ")) error += cnt;
                else pending += cnt;
            }
        } catch (Exception ex) {
            logger.warn("Querying FATCA counts: {}", ex.getMessage());
        }

        data.put("totalRecords", total);
        data.put("pendingRecords", pending);
        data.put("completedRecords", completed);
        data.put("errorRecords", error);

        List<Map<String, Object>> records = new ArrayList<>();
        try {
            records = jdbcTemplate.queryForList(
                "SELECT TO_CHAR(TRN_ID) AS REF_NO, TO_CHAR(TRN_ID) AS UNIQUE_REF_ID, SURNAME AS ENTITY_NAME, " +
                "TO_CHAR(ENTRY_TIME, 'YYYY-MM-DD HH24:MI:SS') AS UPLOAD_DATE, " +
                "NVL(RECORD_STATUS, 'Completed') AS STATUS, FOREIGN_AMOUNT AS AMOUNT_ORG " +
                "FROM BRECON.FACTA_TRANSCATION_DATA " +
                "WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'Completed') <> 'DUPLICATE' AND ROWNUM <= 100 ORDER BY ENTRY_TIME DESC"
            );
        } catch (Exception ex) {
            logger.warn("Querying FATCA records: {}", ex.getMessage());
        }
        data.put("records", records);
        return data;
    }

    public List<Map<String, Object>> getFatcaDuplicateRecords() {
        String sql = "SELECT TRN_ID AS DUP_ID, BRN AS ACCOUNT_NUMBER, RES_COUNTRY_CODE AS COUNTRY_CODE, " +
                     "       PASSPORT_NO AS TIN, SURNAME AS FIRST_NAME, OTHER_NAME AS LAST_NAME, " +
                     "       FOREIGN_AMOUNT AS ACCOUNT_BALANCE, FOREIGN_CURR AS CURRENCY_CODE, " +
                     "       TO_CHAR(TRN_ID) AS REF_NO, SURNAME AS ENTITY_NAME, FOREIGN_AMOUNT AS AMOUNT_ORG, " +
                     "       'File_Upload' AS FILE_NAME, 'PENDING' AS STATUS " +
                     "FROM BRECON.FACTA_TRANSCATION_DATA " +
                     "WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND RECORD_STATUS = 'DUPLICATE'";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("Error loading FATCA duplicate queue", e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public boolean rectifyFatcaDuplicate(String dupId, String accountNumber, String firstName, 
                                        String lastName, String countryCode, String tin, 
                                        BigDecimal balance, String currency, String status) {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA " +
                         "SET BRN = ?, SURNAME = ?, OTHER_NAME = ?, RES_COUNTRY_CODE = ?, " +
                         "    PASSPORT_NO = ?, FOREIGN_AMOUNT = ?, FOREIGN_CURR = ?, RECORD_STATUS = ? " +
                         "WHERE TRN_ID = ?";
            int updated = jdbcTemplate.update(sql, accountNumber, firstName, lastName, 
                                              countryCode, tin, balance, currency, status, dupId);
            return updated > 0;
        } catch (Exception e) {
            logger.error("Error rectifying FATCA duplicate", e);
            return false;
        }
    }

    @Transactional
    public boolean deleteFatcaDuplicate(String dupId) {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA SET DEL_FLAG = 'Y' WHERE TRN_ID = ?";
            return jdbcTemplate.update(sql, dupId) > 0;
        } catch (Exception e) {
            logger.error("Error deleting FATCA duplicate record", e);
            return false;
        }
    }

    @Transactional
    public boolean deleteAllFatcaDuplicates() {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA SET DEL_FLAG = 'Y' WHERE RECORD_STATUS = 'DUPLICATE'";
            jdbcTemplate.update(sql);
            return true;
        } catch (Exception e) {
            logger.error("Error clearing all FATCA duplicate records", e);
            return false;
        }
    }

    @Transactional
    public boolean updateFatcaRecord(String refNo, String status, String userId) {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA SET RECORD_STATUS = ? WHERE TRN_ID = ? AND NVL(DEL_FLAG, 'N') <> 'Y'";
            return jdbcTemplate.update(sql, status, refNo) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getFatcaRecordDetails(String refNo) {
        try {
            return jdbcTemplate.queryForMap("SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE TRN_ID = ? AND ROWNUM = 1", refNo);
        } catch (Exception ex) {
            return jdbcTemplate.queryForMap("SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE ROWNUM = 1");
        }
    }

    // =========================================================================
    // OECD XML GENERATORS & EXPORT
    // =========================================================================
    public String generateFatcaXml() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = jdbcTemplate.queryForList("SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND NVL(RECORD_STATUS, 'Completed') <> 'DUPLICATE'");
        } catch (Exception e) {
            logger.error("Error reading FATCA records for XML", e);
        }

        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ftc:FATCA_OECD xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
           .append("xmlns:ftc=\"urn:oecd:ties:fatca:v2\" xmlns:sfa=\"urn:oecd:ties:stffatcatypes:v2\" ")
           .append("version=\"2.0\" xsi:schemaLocation=\"urn:oecd:ties:fatca:v2 FatcaXML_v2.0.xsd\">\n");
        
        xml.append("  <ftc:MessageSpec>\n");
        xml.append("    <sfa:SendingCompanyIN>8H4YD6.99999.BR.480</sfa:SendingCompanyIN>\n");
        xml.append("    <sfa:TransmittingCountry>MU</sfa:TransmittingCountry>\n");
        xml.append("    <sfa:ReceivingCountry>US</sfa:ReceivingCountry>\n");
        xml.append("    <sfa:MessageType>FATCA</sfa:MessageType>\n");
        xml.append("    <sfa:MessageRefId>8H4YD6.99999.BR.480.2026_0001</sfa:MessageRefId>\n");
        xml.append("    <sfa:ReportingPeriod>2025-12-31</sfa:ReportingPeriod>\n");
        xml.append("    <sfa:Timestamp>").append(currentTimeStamp).append("</sfa:Timestamp>\n");
        xml.append("  </ftc:MessageSpec>\n");
        xml.append("  <ftc:FATCA>\n");
        xml.append("    <ftc:ReportingFI>\n");
        xml.append("      <sfa:ResCountryCode>MU</sfa:ResCountryCode>\n");
        xml.append("      <sfa:TIN>8H4YD6.99999.BR.480</sfa:TIN>\n");
        xml.append("      <sfa:Name>BANK OF BARODA</sfa:Name>\n");
        xml.append("      <sfa:Address>\n");
        xml.append("        <sfa:CountryCode>MU</sfa:CountryCode>\n");
        xml.append("        <sfa:AddressFree>Sir William Newton Street, Port Louis, Mauritius.</sfa:AddressFree>\n");
        xml.append("      </sfa:Address>\n");
        xml.append("      <ftc:DocSpec>\n");
        xml.append("        <ftc:DocTypeIndic>FATCA1</ftc:DocTypeIndic>\n");
        xml.append("        <ftc:DocRefId>8H4YD6.99999.BR.480.2025.RF1</ftc:DocRefId>\n");
        xml.append("      </ftc:DocSpec>\n");
        xml.append("    </ftc:ReportingFI>\n");
        xml.append("    <ftc:ReportingGroup>\n");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> r = list.get(i);
            String trnId = String.valueOf(r.getOrDefault("TRN_ID", i + 1));
            String brn = escapeXml(r.get("BRN"));
            String resCountry = escapeXml(r.get("RES_COUNTRY_CODE"));
            String surname = escapeXml(r.get("SURNAME"));
            String otherName = escapeXml(r.get("OTHER_NAME"));
            String passportNo = escapeXml(r.get("PASSPORT_NO"));
            String foreignCurr = escapeXml(r.get("FOREIGN_CURR"));
            if (foreignCurr.isEmpty()) foreignCurr = "USD";
            Object foreignAmount = r.get("FOREIGN_AMOUNT");
            Object amountMur = r.get("AMOUNT_MUR");
            String docRefId = "8H4YD6.99999.BR.480.2025.AR" + trnId + ".ID1";

            xml.append("      <ftc:AccountReport>\n");
            xml.append("        <ftc:DocSpec>\n");
            xml.append("          <ftc:DocTypeIndic>FATCA1</ftc:DocTypeIndic>\n");
            xml.append("          <ftc:DocRefId>").append(docRefId).append("</ftc:DocRefId>\n");
            xml.append("        </ftc:DocSpec>\n");
            xml.append("        <ftc:AccountNumber>").append(brn.isEmpty() ? "94560190000291" : brn).append("</ftc:AccountNumber>\n");
            xml.append("        <ftc:AccountClosed>0</ftc:AccountClosed>\n");
            xml.append("        <ftc:AccountHolder>\n");
            xml.append("          <ftc:Individual>\n");
            xml.append("            <sfa:ResCountryCode>").append(resCountry.isEmpty() ? "MU" : resCountry).append("</sfa:ResCountryCode>\n");
            xml.append("            <sfa:TIN issuedBy=\"US\">").append(passportNo.isEmpty() ? "000000000" : passportNo).append("</sfa:TIN>\n");
            xml.append("            <sfa:Name>\n");
            xml.append("              <sfa:FirstName>").append(surname).append("</sfa:FirstName>\n");
            xml.append("              <sfa:LastName>").append(otherName).append("</sfa:LastName>\n");
            xml.append("            </sfa:Name>\n");
            xml.append("            <sfa:Address>\n");
            xml.append("              <sfa:CountryCode>").append(resCountry.isEmpty() ? "MU" : resCountry).append("</sfa:CountryCode>\n");
            xml.append("              <sfa:AddressFree>COURT SUITE 308 ST DENIS STREET PORT LOUIS, MAURITIUS</sfa:AddressFree>\n");
            xml.append("            </sfa:Address>\n");
            xml.append("          </ftc:Individual>\n");
            xml.append("        </ftc:AccountHolder>\n");
            xml.append("        <ftc:AccountBalance currCode=\"").append(foreignCurr).append("\">").append(foreignAmount != null ? foreignAmount : "0.00").append("</ftc:AccountBalance>\n");
            xml.append("        <ftc:Payment>\n");
            xml.append("          <ftc:Type>FATCA502</ftc:Type>\n");
            xml.append("          <ftc:PaymentAmnt currCode=\"").append(foreignCurr).append("\">").append(amountMur != null ? amountMur : "0.00").append("</ftc:PaymentAmnt>\n");
            xml.append("        </ftc:Payment>\n");
            xml.append("      </ftc:AccountReport>\n");
        }
        xml.append("    </ftc:ReportingGroup>\n");
        xml.append("  </ftc:FATCA>\n");
        xml.append("</ftc:FATCA_OECD>");
        return xml.toString();
    }

    public String generateSingleFatcaXml(String refNo) {
        Map<String, Object> row;
        try {
            row = jdbcTemplate.queryForMap("SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE TRN_ID = ? AND ROWNUM = 1", refNo);
        } catch (Exception ex) {
            row = jdbcTemplate.queryForMap("SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE ROWNUM = 1");
        }

        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        String trnId = String.valueOf(row.getOrDefault("TRN_ID", "1"));
        String brn = escapeXml(row.get("BRN"));
        String resCountry = escapeXml(row.get("RES_COUNTRY_CODE"));
        String surname = escapeXml(row.get("SURNAME"));
        String otherName = escapeXml(row.get("OTHER_NAME"));
        String passportNo = escapeXml(row.get("PASSPORT_NO"));
        String foreignCurr = escapeXml(row.get("FOREIGN_CURR"));
        if (foreignCurr.isEmpty()) foreignCurr = "USD";
        Object foreignAmount = row.get("FOREIGN_AMOUNT");
        Object amountMur = row.get("AMOUNT_MUR");

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ftc:FATCA_OECD xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
           .append("xmlns:ftc=\"urn:oecd:ties:fatca:v2\" xmlns:sfa=\"urn:oecd:ties:stffatcatypes:v2\" ")
           .append("version=\"2.0\" xsi:schemaLocation=\"urn:oecd:ties:fatca:v2 FatcaXML_v2.0.xsd\">\n");
        xml.append("  <ftc:MessageSpec>\n");
        xml.append("    <sfa:SendingCompanyIN>8H4YD6.99999.BR.480</sfa:SendingCompanyIN>\n");
        xml.append("    <sfa:TransmittingCountry>MU</sfa:TransmittingCountry>\n");
        xml.append("    <sfa:ReceivingCountry>US</sfa:ReceivingCountry>\n");
        xml.append("    <sfa:MessageType>FATCA</sfa:MessageType>\n");
        xml.append("    <sfa:MessageRefId>8H4YD6.99999.BR.480.2026_0001</sfa:MessageRefId>\n");
        xml.append("    <sfa:ReportingPeriod>2025-12-31</sfa:ReportingPeriod>\n");
        xml.append("    <sfa:Timestamp>").append(currentTimeStamp).append("</sfa:Timestamp>\n");
        xml.append("  </ftc:MessageSpec>\n");
        xml.append("  <ftc:FATCA>\n");
        xml.append("    <ftc:ReportingFI>\n");
        xml.append("      <sfa:ResCountryCode>MU</sfa:ResCountryCode>\n");
        xml.append("      <sfa:TIN>8H4YD6.99999.BR.480</sfa:TIN>\n");
        xml.append("      <sfa:Name>BANK OF BARODA</sfa:Name>\n");
        xml.append("      <sfa:Address>\n");
        xml.append("        <sfa:CountryCode>MU</sfa:CountryCode>\n");
        xml.append("        <sfa:AddressFree>Sir William Newton Street, Port Louis, Mauritius.</sfa:AddressFree>\n");
        xml.append("      </sfa:Address>\n");
        xml.append("      <ftc:DocSpec>\n");
        xml.append("        <ftc:DocTypeIndic>FATCA1</ftc:DocTypeIndic>\n");
        xml.append("        <ftc:DocRefId>8H4YD6.99999.BR.480.2025.RF1</ftc:DocRefId>\n");
        xml.append("      </ftc:DocSpec>\n");
        xml.append("    </ftc:ReportingFI>\n");
        xml.append("    <ftc:ReportingGroup>\n");
        xml.append("      <ftc:AccountReport>\n");
        xml.append("        <ftc:DocSpec>\n");
        xml.append("          <ftc:DocTypeIndic>FATCA1</ftc:DocTypeIndic>\n");
        xml.append("          <ftc:DocRefId>8H4YD6.99999.BR.480.2025.AR").append(trnId).append(".ID1</ftc:DocRefId>\n");
        xml.append("        </ftc:DocSpec>\n");
        xml.append("        <ftc:AccountNumber>").append(brn.isEmpty() ? "94560190000291" : brn).append("</ftc:AccountNumber>\n");
        xml.append("        <ftc:AccountClosed>0</ftc:AccountClosed>\n");
        xml.append("        <ftc:AccountHolder>\n");
        xml.append("          <ftc:Individual>\n");
        xml.append("            <sfa:ResCountryCode>").append(resCountry.isEmpty() ? "MU" : resCountry).append("</sfa:ResCountryCode>\n");
        xml.append("            <sfa:TIN issuedBy=\"US\">").append(passportNo.isEmpty() ? "000000000" : passportNo).append("</sfa:TIN>\n");
        xml.append("            <sfa:Name>\n");
        xml.append("              <sfa:FirstName>").append(surname).append("</sfa:FirstName>\n");
        xml.append("              <sfa:LastName>").append(otherName).append("</sfa:LastName>\n");
        xml.append("            </sfa:Name>\n");
        xml.append("            <sfa:Address>\n");
        xml.append("              <sfa:CountryCode>").append(resCountry.isEmpty() ? "MU" : resCountry).append("</sfa:CountryCode>\n");
        xml.append("              <sfa:AddressFree>COURT SUITE 308 ST DENIS STREET PORT LOUIS, MAURITIUS</sfa:AddressFree>\n");
        xml.append("            </sfa:Address>\n");
        xml.append("          </ftc:Individual>\n");
        xml.append("        </ftc:AccountHolder>\n");
        xml.append("        <ftc:AccountBalance currCode=\"").append(foreignCurr).append("\">").append(foreignAmount != null ? foreignAmount : "0.00").append("</ftc:AccountBalance>\n");
        xml.append("        <ftc:Payment>\n");
        xml.append("          <ftc:Type>FATCA502</ftc:Type>\n");
        xml.append("          <ftc:PaymentAmnt currCode=\"").append(foreignCurr).append("\">").append(amountMur != null ? amountMur : "0.00").append("</ftc:PaymentAmnt>\n");
        xml.append("        </ftc:Payment>\n");
        xml.append("      </ftc:AccountReport>\n");
        xml.append("    </ftc:ReportingGroup>\n");
        xml.append("  </ftc:FATCA>\n");
        xml.append("</ftc:FATCA_OECD>");
        return xml.toString();
    }

    private String escapeXml(Object val) {
        if (val == null) return "";
        return val.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}