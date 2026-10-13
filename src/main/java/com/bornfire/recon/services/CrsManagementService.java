package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
public class CrsManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CrsManagementService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // 1. INGESTION: UPLOAD CRS FILE (EXCEL / CSV) INTO BRECON.CRS_REPORT_DATA
    // =========================================================================
    @Transactional
    public int uploadCrsFile(MultipartFile file, String userId) throws Exception {
        if (file == null || file.isEmpty()) {
            return 0;
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<Object[]> batchParams = new ArrayList<>();
        String effectiveUser = (userId != null && !userId.trim().isEmpty()) ? userId : "SYSTEM";

        // --- CSV INGESTION ---
        if (fileName.endsWith(".csv")) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                String headerLine = reader.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) return 0;

                String delimiter = headerLine.contains(",") ? "," : "\\|";
                String[] headers = headerLine.split(delimiter);
                Map<String, Integer> colMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String clean = headers[i].trim().toUpperCase().replaceAll("[^A-Z0-9_]", "");
                    colMap.put(clean, i);
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(delimiter, -1);

                    String accNum     = getCsvVal(tokens, colMap, "ACCOUNT_NUMBER", "ACCOUNT_NO", "ACCOUNTNUMBER");
                    if (accNum.isEmpty()) continue;

                    String holderType = getCsvVal(tokens, colMap, "HOLDER_TYPE", "TYPE");
                    if (holderType.isEmpty()) holderType = "INDIVIDUAL";

                    String resCountry = getCsvVal(tokens, colMap, "RESIDENCE_COUNTRY", "COUNTRY", "RES_COUNTRY");
                    String tin        = getCsvVal(tokens, colMap, "TIN", "TAX_ID");
                    String firstName  = getCsvVal(tokens, colMap, "FIRST_NAME", "FIRSTNAME", "NAME");
                    String lastName   = getCsvVal(tokens, colMap, "LAST_NAME", "LASTNAME", "SURNAME");
                    String orgName    = getCsvVal(tokens, colMap, "ORGANISATION_NAME", "ORG_NAME", "COMPANY_NAME");
                    String city       = getCsvVal(tokens, colMap, "ADDRESS_CITY", "CITY");
                    String balStr     = getCsvVal(tokens, colMap, "ACCOUNT_BALANCE", "BALANCE", "AMOUNT");
                    String curr       = getCsvVal(tokens, colMap, "CURRENCY_CODE", "CURRENCY");
                    if (curr.isEmpty()) curr = "MUR";

                    double balance = 0.0;
                    try {
                        if (!balStr.isEmpty()) balance = Double.parseDouble(balStr.replace(",", ""));
                    } catch (Exception ignored) {}

                    batchParams.add(new Object[] {
                        accNum, holderType.toUpperCase(), resCountry.toUpperCase(), tin,
                        firstName, lastName, orgName, city, balance, curr.toUpperCase(),
                        "CRS1101", "CRS1202", "CRS901", "PENDING", "2025"
                    });
                }
            }
        } 
        // --- EXCEL (.XLSX / .XLS) INGESTION ---
        else {
            try (InputStream is = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) return 0;

                Row headerRow = sheet.getRow(0);
                Map<String, Integer> colMap = new HashMap<>();
                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    Cell cell = headerRow.getCell(c);
                    if (cell != null) {
                        String name = getCellValueAsString(cell);
                        if (name != null && !name.trim().isEmpty()) {
                            colMap.put(name.trim().toUpperCase().replaceAll("[^A-Z0-9_]", ""), c);
                        }
                    }
                }

                int lastRow = sheet.getLastRowNum();
                for (int r = 1; r <= lastRow; r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    String accNum = getXlsVal(row, colMap, "ACCOUNT_NUMBER", "ACCOUNT_NO", "ACCOUNTNUMBER");
                    if (accNum.isEmpty()) continue;

                    String holderType = getXlsVal(row, colMap, "HOLDER_TYPE", "TYPE");
                    if (holderType.isEmpty()) holderType = "INDIVIDUAL";

                    String resCountry = getXlsVal(row, colMap, "RESIDENCE_COUNTRY", "COUNTRY", "RES_COUNTRY");
                    String tin        = getXlsVal(row, colMap, "TIN", "TAX_ID");
                    String firstName  = getXlsVal(row, colMap, "FIRST_NAME", "FIRSTNAME", "NAME");
                    String lastName   = getXlsVal(row, colMap, "LAST_NAME", "LASTNAME", "SURNAME");
                    String orgName    = getXlsVal(row, colMap, "ORGANISATION_NAME", "ORG_NAME", "COMPANY_NAME");
                    String city       = getXlsVal(row, colMap, "ADDRESS_CITY", "CITY");
                    String balStr     = getXlsVal(row, colMap, "ACCOUNT_BALANCE", "BALANCE", "AMOUNT");
                    String curr       = getXlsVal(row, colMap, "CURRENCY_CODE", "CURRENCY");
                    if (curr.isEmpty()) curr = "MUR";

                    double balance = 0.0;
                    try {
                        if (!balStr.isEmpty()) balance = Double.parseDouble(balStr.replace(",", ""));
                    } catch (Exception ignored) {}

                    batchParams.add(new Object[] {
                        accNum, holderType.toUpperCase(), resCountry.toUpperCase(), tin,
                        firstName, lastName, orgName, city, balance, curr.toUpperCase(),
                        "CRS1101", "CRS1202", "CRS901", "PENDING", "2025"
                    });
                }
            }
        }

        if (batchParams.isEmpty()) return 0;

        String insertSql = "INSERT INTO BRECON.CRS_REPORT_DATA (" +
                           "  ACCOUNT_NUMBER, HOLDER_TYPE, RESIDENCE_COUNTRY, TIN, " +
                           "  FIRST_NAME, LAST_NAME, ORGANISATION_NAME, ADDRESS_CITY, " +
                           "  ACCOUNT_BALANCE, CURRENCY_CODE, ACCOUNT_TYPE, DD_PROCEDURE, " +
                           "  SELF_CERT_FLAG, STATUS, REPORTING_YEAR " +
                           ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int[] counts = jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] row = batchParams.get(i);
                for (int col = 0; col < row.length; col++) {
                    Object val = row[col];
                    if (val == null) {
                        ps.setNull(col + 1, Types.VARCHAR);
                    } else if (val instanceof Double) {
                        ps.setDouble(col + 1, (Double) val);
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

        logger.info("Successfully uploaded {} CRS records by user: {}", counts.length, effectiveUser);
        return counts.length;
    }

    // =========================================================================
    // 2. XML GENERATION: MASTER CRS v3.0 XML
    // =========================================================================
    public String generateCrsXml(String reportingYear) {
        if (reportingYear == null || reportingYear.trim().isEmpty()) {
            reportingYear = "2025";
        }

        String bankTin = "20001234";
        String bankName = "STATE COMMERCIAL BANK LTD";
        String countryCode = "MU";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        String msgRefId = countryCode + reportingYear + countryCode + bankTin + "_CRS_" + System.currentTimeMillis();

        String sql = "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE REPORTING_YEAR = ? AND STATUS != 'EXCLUDED'";
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql, reportingYear);

        boolean isNil = records.isEmpty();
        String messageTypeIndic = isNil ? "CRS703" : "CRS701";

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<crs:CRS_OECD xmlns:cfc=\"urn:oecd:ties:commontypesfatcacrs:v2\"\n");
        xml.append("              xmlns:stf=\"urn:oecd:ties:crsstf:v5\"\n");
        xml.append("              xmlns:iso=\"urn:oecd:ties:isocrstypes:v1\"\n");
        xml.append("              xmlns:crs=\"urn:oecd:ties:crs:v3\"\n");
        xml.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("              version=\"3.0\">\n");

        xml.append("    <crs:MessageSpec>\n");
        xml.append("        <crs:SendingCompanyIN>").append(bankTin).append("</crs:SendingCompanyIN>\n");
        xml.append("        <crs:TransmittingCountry>").append(countryCode).append("</crs:TransmittingCountry>\n");
        xml.append("        <crs:ReceivingCountry>").append(countryCode).append("</crs:ReceivingCountry>\n");
        xml.append("        <crs:MessageType>CRS</crs:MessageType>\n");
        xml.append("        <crs:MessageRefId>").append(msgRefId).append("</crs:MessageRefId>\n");
        xml.append("        <crs:MessageTypeIndic>").append(messageTypeIndic).append("</crs:MessageTypeIndic>\n");
        xml.append("        <crs:ReportingPeriod>").append(reportingYear).append("-12-31</crs:ReportingPeriod>\n");
        xml.append("        <crs:Timestamp>").append(timestamp).append("</crs:Timestamp>\n");
        xml.append("    </crs:MessageSpec>\n");

        xml.append("    <crs:CrsBody>\n");
        xml.append("        <crs:ReportingFI>\n");
        xml.append("            <crs:ResCountryCode>").append(countryCode).append("</crs:ResCountryCode>\n");
        xml.append("            <crs:IN issuedBy=\"").append(countryCode).append("\" INType=\"TIN\">").append(bankTin).append("</crs:IN>\n");
        xml.append("            <crs:Name>").append(escapeXml(bankName)).append("</crs:Name>\n");
        xml.append("            <crs:Address legalAddressType=\"OECD304\">\n");
        xml.append("                <cfc:CountryCode>").append(countryCode).append("</cfc:CountryCode>\n");
        xml.append("                <cfc:AddressFix>\n");
        xml.append("                    <cfc:Street>Place D'Armes</cfc:Street>\n");
        xml.append("                    <cfc:City>Port Louis</cfc:City>\n");
        xml.append("                </cfc:AddressFix>\n");
        xml.append("            </crs:Address>\n");
        xml.append("            <crs:DocSpec>\n");
        xml.append("                <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>\n");
        xml.append("                <stf:DocRefId>").append(countryCode).append(reportingYear).append(countryCode).append(bankTin).append("_FI_001</stf:DocRefId>\n");
        xml.append("            </crs:DocSpec>\n");
        xml.append("        </crs:ReportingFI>\n");

        if (!isNil) {
            xml.append("        <crs:ReportingGroup>\n");
            int counter = 1;

            for (Map<String, Object> row : records) {
                String docRefId = countryCode + reportingYear + countryCode + bankTin + "_AR_" + String.format("%05d", counter++);
                String accNum = String.valueOf(row.get("ACCOUNT_NUMBER"));
                String holderType = String.valueOf(row.getOrDefault("HOLDER_TYPE", "INDIVIDUAL"));
                String resCountry = String.valueOf(row.get("RESIDENCE_COUNTRY"));
                String balance = String.valueOf(row.getOrDefault("ACCOUNT_BALANCE", "0.00"));
                String curr = String.valueOf(row.getOrDefault("CURRENCY_CODE", "MUR"));
                String selfCert = String.valueOf(row.getOrDefault("SELF_CERT_FLAG", "CRS901"));
                String ddProc = String.valueOf(row.getOrDefault("DD_PROCEDURE", "CRS1202"));
                String accType = String.valueOf(row.getOrDefault("ACCOUNT_TYPE", "CRS1101"));

                xml.append("            <crs:AccountReport>\n");
                xml.append("                <crs:DocSpec>\n");
                xml.append("                    <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>\n");
                xml.append("                    <stf:DocRefId>").append(docRefId).append("</stf:DocRefId>\n");
                xml.append("                </crs:DocSpec>\n");
                xml.append("                <crs:AccountNumber AcctNumberType=\"OECD601\">").append(accNum).append("</crs:AccountNumber>\n");

                xml.append("                <crs:AccountHolder>\n");
                xml.append("                    <crs:SelfCert>").append(selfCert).append("</crs:SelfCert>\n");

                if ("ORGANISATION".equalsIgnoreCase(holderType)) {
                    xml.append("                    <crs:Organisation>\n");
                    xml.append("                        <crs:ResCountryCode>").append(resCountry).append("</crs:ResCountryCode>\n");
                    xml.append("                        <crs:Name>").append(escapeXml(String.valueOf(row.get("ORGANISATION_NAME")))).append("</crs:Name>\n");
                    xml.append("                        <crs:Address>\n");
                    xml.append("                            <cfc:CountryCode>").append(resCountry).append("</cfc:CountryCode>\n");
                    xml.append("                            <cfc:AddressFix><cfc:City>").append(escapeXml(String.valueOf(row.get("ADDRESS_CITY")))).append("</cfc:City></cfc:AddressFix>\n");
                    xml.append("                        </crs:Address>\n");
                    xml.append("                    </crs:Organisation>\n");
                    xml.append("                    <crs:AcctHolderType>").append(row.getOrDefault("ACCT_HOLDER_CATEGORY", "CRS101")).append("</crs:AcctHolderType>\n");
                } else {
                    xml.append("                    <crs:Individual>\n");
                    xml.append("                        <crs:ResCountryCode>").append(resCountry).append("</crs:ResCountryCode>\n");
                    if (row.get("TIN") != null && !String.valueOf(row.get("TIN")).trim().isEmpty()) {
                        xml.append("                        <crs:TIN issuedBy=\"").append(resCountry).append("\">").append(row.get("TIN")).append("</crs:TIN>\n");
                    }
                    xml.append("                        <crs:Name>\n");
                    xml.append("                            <crs:FirstName>").append(escapeXml(String.valueOf(row.get("FIRST_NAME")))).append("</crs:FirstName>\n");
                    xml.append("                            <crs:LastName>").append(escapeXml(String.valueOf(row.get("LAST_NAME")))).append("</crs:LastName>\n");
                    xml.append("                        </crs:Name>\n");
                    xml.append("                        <crs:Address>\n");
                    xml.append("                            <cfc:CountryCode>").append(resCountry).append("</cfc:CountryCode>\n");
                    xml.append("                            <cfc:AddressFix>\n");
                    xml.append("                                <cfc:City>").append(escapeXml(String.valueOf(row.get("ADDRESS_CITY")))).append("</cfc:City>\n");
                    xml.append("                            </cfc:AddressFix>\n");
                    xml.append("                        </crs:Address>\n");
                    xml.append("                    </crs:Individual>\n");
                }
                xml.append("                </crs:AccountHolder>\n");

                xml.append("                <crs:AccountBalance currCode=\"").append(curr).append("\">").append(balance).append("</crs:AccountBalance>\n");
                xml.append("                <crs:DDProcedure>").append(ddProc).append("</crs:DDProcedure>\n");
                xml.append("                <crs:AccountType>").append(accType).append("</crs:AccountType>\n");
                xml.append("            </crs:AccountReport>\n");
            }
            xml.append("        </crs:ReportingGroup>\n");
        }

        xml.append("    </crs:CrsBody>\n");
        xml.append("</crs:CRS_OECD>");

        return xml.toString();
    }

    // =========================================================================
    // 3. XML GENERATION: SINGLE RECORD CRS XML
    // =========================================================================
    public String generateSingleCrsXml(Long reportId) {
        String bankTin = "20001234";
        String countryCode = "MU";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());

        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE REPORT_ID = ?", reportId
        );

        String reportingYear = String.valueOf(row.getOrDefault("REPORTING_YEAR", "2025"));
        String docRefId = countryCode + reportingYear + countryCode + bankTin + "_REC_" + reportId;

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<crs:CRS_OECD xmlns:cfc=\"urn:oecd:ties:commontypesfatcacrs:v2\"\n");
        xml.append("              xmlns:stf=\"urn:oecd:ties:crsstf:v5\"\n");
        xml.append("              xmlns:iso=\"urn:oecd:ties:isocrstypes:v1\"\n");
        xml.append("              xmlns:crs=\"urn:oecd:ties:crs:v3\"\n");
        xml.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("              version=\"3.0\">\n");
        xml.append("    <crs:MessageSpec>\n");
        xml.append("        <crs:SendingCompanyIN>").append(bankTin).append("</crs:SendingCompanyIN>\n");
        xml.append("        <crs:TransmittingCountry>").append(countryCode).append("</crs:TransmittingCountry>\n");
        xml.append("        <crs:ReceivingCountry>").append(countryCode).append("</crs:ReceivingCountry>\n");
        xml.append("        <crs:MessageType>CRS</crs:MessageType>\n");
        xml.append("        <crs:MessageRefId>").append(docRefId).append("</crs:MessageRefId>\n");
        xml.append("        <crs:MessageTypeIndic>CRS701</crs:MessageTypeIndic>\n");
        xml.append("        <crs:ReportingPeriod>").append(reportingYear).append("-12-31</crs:ReportingPeriod>\n");
        xml.append("        <crs:Timestamp>").append(timestamp).append("</crs:Timestamp>\n");
        xml.append("    </crs:MessageSpec>\n");
        xml.append("    <crs:CrsBody>\n");
        xml.append("        <crs:ReportingFI>\n");
        xml.append("            <crs:ResCountryCode>").append(countryCode).append("</crs:ResCountryCode>\n");
        xml.append("            <crs:IN issuedBy=\"").append(countryCode).append("\" INType=\"TIN\">").append(bankTin).append("</crs:IN>\n");
        xml.append("            <crs:Name>STATE COMMERCIAL BANK LTD</crs:Name>\n");
        xml.append("            <crs:Address legalAddressType=\"OECD304\"><cfc:CountryCode>MU</cfc:CountryCode><cfc:AddressFix><cfc:City>Port Louis</cfc:City></cfc:AddressFix></crs:Address>\n");
        xml.append("            <crs:DocSpec><stf:DocTypeIndic>OECD1</stf:DocTypeIndic><stf:DocRefId>").append(docRefId).append("_FI</stf:DocRefId></crs:DocSpec>\n");
        xml.append("        </crs:ReportingFI>\n");
        xml.append("        <crs:ReportingGroup>\n");
        xml.append("            <crs:AccountReport>\n");
        xml.append("                <crs:DocSpec><stf:DocTypeIndic>OECD1</stf:DocTypeIndic><stf:DocRefId>").append(docRefId).append("</stf:DocRefId></crs:DocSpec>\n");
        xml.append("                <crs:AccountNumber AcctNumberType=\"OECD601\">").append(row.get("ACCOUNT_NUMBER")).append("</crs:AccountNumber>\n");
        xml.append("                <crs:AccountHolder>\n");
        xml.append("                    <crs:SelfCert>").append(row.getOrDefault("SELF_CERT_FLAG", "CRS901")).append("</crs:SelfCert>\n");
        xml.append("                    <crs:Individual>\n");
        xml.append("                        <crs:ResCountryCode>").append(row.get("RESIDENCE_COUNTRY")).append("</crs:ResCountryCode>\n");
        if (row.get("TIN") != null) {
            xml.append("                        <crs:TIN issuedBy=\"").append(row.get("RESIDENCE_COUNTRY")).append("\">").append(row.get("TIN")).append("</crs:TIN>\n");
        }
        xml.append("                        <crs:Name><crs:FirstName>").append(escapeXml(String.valueOf(row.get("FIRST_NAME")))).append("</crs:FirstName><crs:LastName>").append(escapeXml(String.valueOf(row.get("LAST_NAME")))).append("</crs:LastName></crs:Name>\n");
        xml.append("                        <crs:Address><cfc:CountryCode>").append(row.get("RESIDENCE_COUNTRY")).append("</cfc:CountryCode><cfc:AddressFix><cfc:City>").append(escapeXml(String.valueOf(row.getOrDefault("ADDRESS_CITY", "Port Louis")))).append("</cfc:City></cfc:AddressFix></crs:Address>\n");
        xml.append("                    </crs:Individual>\n");
        xml.append("                </crs:AccountHolder>\n");
        xml.append("                <crs:AccountBalance currCode=\"").append(row.getOrDefault("CURRENCY_CODE", "MUR")).append("\">").append(row.getOrDefault("ACCOUNT_BALANCE", "0.00")).append("</crs:AccountBalance>\n");
        xml.append("                <crs:DDProcedure>").append(row.getOrDefault("DD_PROCEDURE", "CRS1202")).append("</crs:DDProcedure>\n");
        xml.append("                <crs:AccountType>").append(row.getOrDefault("ACCOUNT_TYPE", "CRS1101")).append("</crs:AccountType>\n");
        xml.append("            </crs:AccountReport>\n");
        xml.append("        </crs:ReportingGroup>\n");
        xml.append("    </crs:CrsBody>\n");
        xml.append("</crs:CRS_OECD>");

        return xml.toString();
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private String getCsvVal(String[] tokens, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            Integer idx = colMap.get(k);
            if (idx != null && idx >= 0 && idx < tokens.length) {
                String val = tokens[idx].trim().replaceAll("^\"|\"$", "");
                if (!val.isEmpty()) return val;
            }
        }
        return "";
    }

    private String getXlsVal(Row row, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            Integer idx = colMap.get(k);
            if (idx != null && idx >= 0) {
                Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) {
                    String val = getCellValueAsString(cell);
                    if (val != null && !val.trim().isEmpty()) return val.trim();
                }
            }
        }
        return "";
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        try {
            String typeStr = String.valueOf(cell.getCellType()).toUpperCase();
            if (typeStr.contains("STRING") || "1".equals(typeStr)) {
                return cell.getStringCellValue();
            } else if (typeStr.contains("NUMERIC") || "0".equals(typeStr)) {
                double num = cell.getNumericCellValue();
                if (num == (long) num) return String.valueOf((long) num);
                return String.valueOf(num);
            } else if (typeStr.contains("BOOLEAN") || "4".equals(typeStr)) {
                return String.valueOf(cell.getBooleanCellValue());
            } else if (typeStr.contains("FORMULA") || "2".equals(typeStr)) {
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}