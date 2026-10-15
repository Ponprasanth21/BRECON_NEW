package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    private static final String[] DB_COLUMNS = new String[] {
        "BRN", "RES_COUNTRY_CODE", "PASSPORT_NO", "SURNAME", 
        "OTHER_NAME", "FOREIGN_AMOUNT", "FOREIGN_CURR", "TXN_TYPE", "AMOUNT_MUR", 
        "RECORD_STATUS", "DEL_FLAG", "RECORD_SOURCE", "ENTRY_TIME", "ENTRY_USER"
    };

    private static final Set<String> DATE_COLUMNS = new HashSet<>(Arrays.asList(
        "LOAD_DATE", "MESSAGE_TIMESTAMP"
    ));

    private static final Set<String> NUMERIC_COLUMNS = new HashSet<>(Arrays.asList(
        "FOREIGN_AMOUNT", "AMOUNT_MUR", "EXCHANGE_RATE", "ACCOUNT_BALANCE", "PAYMENT_AMOUNT"
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

    @Transactional
    public int uploadFatcaFile(MultipartFile file, String userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or null.");
        }

        String originalFilename = file.getOriginalFilename();
        boolean isCsv = originalFilename != null && originalFilename.toLowerCase().endsWith(".csv");

        List<Object[]> batchParams = new ArrayList<>();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String effectiveUser = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

        if (isCsv) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                if (headerLine == null) return 0;

                String[] headers = headerLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                Map<String, Integer> colIndexMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String dbColName = mapCsvColumnToDb(headers[i].replace("\"", ""));
                    colIndexMap.put(dbColName, i);
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
                        } else if ("RECORD_STATUS".equals(col)) {
                            rowValues[i] = "PENDING";
                        } else if ("RECORD_SOURCE".equals(col)) {
                            rowValues[i] = "FX_CSV";
                        } else {
                            Integer idx = colIndexMap.get(col);
                            if (idx != null && idx >= 0 && idx < cols.length) {
                                String val = cols[idx].replace("\"", "").trim();
                                if (!val.isEmpty() && !"null".equalsIgnoreCase(val)) {
                                    hasData = true;
                                    if (DATE_COLUMNS.contains(col)) {
                                        rowValues[i] = parseDate(val);
                                    } else if (NUMERIC_COLUMNS.contains(col)) {
                                        rowValues[i] = parseBigDecimal(val);
                                    } else {
                                        rowValues[i] = val;
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
        } else {
            DataFormatter formatter = new DataFormatter();
            try (InputStream is = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) return 0;

                Row headerRow = sheet.getRow(0);
                Map<String, Integer> colIndexMap = new HashMap<>();
                for (Cell cell : headerRow) {
                    if (cell != null) {
                        colIndexMap.put(mapCsvColumnToDb(formatter.formatCellValue(cell)), cell.getColumnIndex());
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
                        } else if ("RECORD_STATUS".equals(col)) {
                            rowValues[i] = "PENDING";
                        } else if ("RECORD_SOURCE".equals(col)) {
                            rowValues[i] = "FX_CSV";
                        } else {
                            Integer idx = colIndexMap.get(col);
                            if (idx != null && idx >= 0) {
                                Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                                if (cell != null) {
                                    hasData = true;
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
                "SELECT TO_CHAR(TRN_ID) AS REF_NO, TO_CHAR(TRN_ID) AS UNIQUE_REF_ID, SURNAME AS ENTITY_NAME, " +
                "TO_CHAR(ENTRY_TIME, 'YYYY-MM-DD HH24:MI:SS') AS UPLOAD_DATE, " +
                "NVL(RECORD_STATUS, 'Pending') AS STATUS, FOREIGN_AMOUNT AS AMOUNT_ORG " +
                "FROM BRECON.FACTA_TRANSCATION_DATA " +
                "WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND ROWNUM <= 100 ORDER BY ENTRY_TIME DESC"
            );
        } catch (Exception ex) {
            logger.warn("Querying FACTA_TRANSCATION_DATA records: {}", ex.getMessage());
        }
        data.put("records", records);

        return data;
    }

    @Transactional
    public boolean updateFatcaRecord(String refNo, String status, String userId) {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA " +
                         "SET RECORD_STATUS = ? " +
                         "WHERE TRN_ID = ? AND NVL(DEL_FLAG, 'N') <> 'Y'";

            int rowsUpdated = jdbcTemplate.update(sql, status, refNo);
            return rowsUpdated > 0;
        } catch (Exception e) {
            logger.error("Error updating FATCA record", e);
            return false;
        }
    }

    public Map<String, Object> getFatcaRecordDetails(String refNo) {
        String sql = "SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE TRN_ID = ? AND ROWNUM = 1";
        try {
            return jdbcTemplate.queryForMap(sql, refNo);
        } catch (Exception ex) {
            return jdbcTemplate.queryForMap("SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE ROWNUM = 1");
        }
    }

    // Client-Matched OECD FATCA XML Generator for all records
    public String generateFatcaXml() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE NVL(DEL_FLAG, 'N') <> 'Y'";
            list = jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("Error reading FATCA records for XML generation", e);
        }

        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date());

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

    // Client-Matched OECD FATCA XML Generator for a single record
    public String generateSingleFatcaXml(String refNo) {
        Map<String, Object> row;
        try {
            row = jdbcTemplate.queryForMap(
                "SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE TRN_ID = ? AND ROWNUM = 1", refNo);
        } catch (Exception ex) {
            row = jdbcTemplate.queryForMap(
                "SELECT * FROM BRECON.FACTA_TRANSCATION_DATA WHERE ROWNUM = 1");
        }

        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new java.util.Date());
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
        return val.toString()
                  .replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }

    // Fetch duplicate records (grouped by BRN and PASSPORT_NO)
    public List<Map<String, Object>> getFatcaDuplicateRecords() {
        String sql = "SELECT ROWNUM AS DUP_ID, BRN, RES_COUNTRY_CODE AS COUNTRY_CODE, " +
                     "       PASSPORT_NO, SURNAME AS ENTITY_NAME, FOREIGN_AMOUNT AS AMOUNT_ORG, " +
                     "       FOREIGN_CURR, TRN_ID AS REF_NO, 'Duplicate Key Match' AS FILE_NAME, 'PENDING' AS STATUS " +
                     "FROM BRECON.FACTA_TRANSCATION_DATA " +
                     "WHERE NVL(DEL_FLAG, 'N') <> 'Y' AND (BRN, PASSPORT_NO) IN (" +
                     "    SELECT BRN, PASSPORT_NO FROM BRECON.FACTA_TRANSCATION_DATA " +
                     "    WHERE NVL(DEL_FLAG, 'N') <> 'Y' GROUP BY BRN, PASSPORT_NO HAVING COUNT(*) > 1" +
                     ")";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("Error loading FATCA duplicates", e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public boolean rectifyFatcaDuplicate(String dupId, String updatedStatus, String userId) {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA " +
                         "SET RECORD_STATUS = ? " +
                         "WHERE TRN_ID = ?";
            int updated = jdbcTemplate.update(sql, updatedStatus, dupId);
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
            int updated = jdbcTemplate.update(sql, dupId);
            return updated > 0;
        } catch (Exception e) {
            logger.error("Error deleting FATCA duplicate", e);
            return false;
        }
    }

    @Transactional
    public boolean deleteAllFatcaDuplicates() {
        try {
            String sql = "UPDATE BRECON.FACTA_TRANSCATION_DATA SET DEL_FLAG = 'Y' " +
                         "WHERE (BRN, PASSPORT_NO) IN (" +
                         "    SELECT BRN, PASSPORT_NO FROM BRECON.FACTA_TRANSCATION_DATA " +
                         "    WHERE NVL(DEL_FLAG, 'N') <> 'Y' GROUP BY BRN, PASSPORT_NO HAVING COUNT(*) > 1" +
                         ")";
            jdbcTemplate.update(sql);
            return true;
        } catch (Exception e) {
            logger.error("Error clearing FATCA duplicates", e);
            return false;
        }
    }
}