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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CrsManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CrsManagementService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Reporting Financial Institution Details (Bank of Baroda Mauritius)
    private static final String BANK_TIN = "20009206";
    private static final String BANK_NAME = "BANK OF BARODA";
    private static final String BANK_COUNTRY = "MU";
    private static final String BANK_ADDRESS = "Sir William Newton Street, Port Louis";

    // POI Formatter instance
    private final DataFormatter dataFormatter = new DataFormatter();

    // =========================================================================
    // 1. INGESTION WITH DEDUPLICATION (PREVENTS DUPLICATES & ROUTES TO QUEUE)
    // =========================================================================
    @Transactional
    public int uploadCrsFile(MultipartFile file, String userId) throws Exception {
        Map<String, Object> res = uploadCrsFileWithDeduplication(file, userId);
        return (Integer) res.getOrDefault("inserted", 0);
    }

    @Transactional
    public Map<String, Object> uploadCrsFileWithDeduplication(MultipartFile file, String userId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("inserted", 0);
            result.put("duplicates", 0);
            result.put("message", "File is empty.");
            return result;
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "UNKNOWN";
        String effectiveUser = (userId != null && !userId.trim().isEmpty()) ? userId : "SYSTEM";

        List<Object[]> parsedRows = parseFileToRows(file, effectiveUser);
        int insertedCount = 0;
        int duplicateCount = 0;

        String checkSql = "SELECT COUNT(1) FROM BRECON.CRS_REPORT_DATA WHERE ACCOUNT_NUMBER = ? AND REPORTING_YEAR = ?";

        String insertSql = "INSERT INTO BRECON.CRS_REPORT_DATA (" +
                           "  ACCOUNT_NUMBER, ACCT_NUMBER_TYPE, IS_CLOSED, IS_DORMANT, IS_UNDOCUMENTED, " +
                           "  HOLDER_TYPE, ACCT_HOLDER_CATEGORY, ACCOUNT_TYPE, DD_PROCEDURE, SELF_CERT_FLAG, " +
                           "  RESIDENCE_COUNTRY, TIN, TIN_ISSUER, FIRST_NAME, LAST_NAME, ORGANISATION_NAME, " +
                           "  BIRTH_DATE, BIRTH_CITY, BIRTH_COUNTRY, ADDRESS_STREET, ADDRESS_CITY, ADDRESS_POSTCODE, ADDRESS_COUNTRY, " +
                           "  CP_FIRST_NAME, CP_LAST_NAME, CP_RESIDENCE_COUNTRY, CP_TIN, CP_ROLE, CP_SELF_CERT, " +
                           "  CP_BIRTH_DATE, CP_BIRTH_CITY, CP_BIRTH_COUNTRY, CP_ADDRESS_STREET, CP_ADDRESS_CITY, CP_ADDRESS_POSTCODE, CP_ADDRESS_COUNTRY, " +
                           "  ACCOUNT_BALANCE, CURRENCY_CODE, PAYMENT_TYPE, PAYMENT_AMOUNT, " +
                           "  REPORTING_YEAR, STATUS, ENTRY_USER " +
                           ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String logDupSql = "INSERT INTO BRECON.CRS_DUPLICATE_RECORDS (" +
                           "  FILE_NAME, ACCOUNT_NUMBER, HOLDER_TYPE, HOLDER_NAME, " +
                           "  RESIDENCE_COUNTRY, TIN, ACCOUNT_BALANCE, CURRENCY_CODE, " +
                           "  REPORTING_YEAR, REASON, STATUS " +
                           ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, '2025', 'Duplicate Account Number already present in database', 'PENDING')";

        for (Object[] r : parsedRows) {
            String accNum = (String) r[0];
            String year = (String) r[40]; // REPORTING_YEAR

            Integer exists = 0;
            try {
                exists = jdbcTemplate.queryForObject(checkSql, Integer.class, accNum, year);
            } catch (Exception ignored) {}

            if (exists != null && exists > 0) {
                // Route to duplicate exception queue
                String holderType = (String) r[5];
                String name = "ORGANISATION".equalsIgnoreCase(holderType) ? 
                              (String) r[15] : ((String) r[13] + " " + (String) r[14]);
                String ctry = (String) r[10];
                String tin = (String) r[11];
                Double bal = (Double) r[36];
                String curr = (String) r[37];

                try {
                    jdbcTemplate.update(logDupSql, fileName, accNum, holderType, name, ctry, tin, bal, curr);
                } catch (Exception ex) {
                    logger.warn("Could not insert to CRS_DUPLICATE_RECORDS: {}", ex.getMessage());
                }
                duplicateCount++;
            } else {
                // Insert new unique record
                executeSingleInsert(insertSql, r);
                insertedCount++;
            }
        }

        result.put("inserted", insertedCount);
        result.put("duplicates", duplicateCount);
        result.put("message", "Inserted " + insertedCount + " new record(s). Flagged " + duplicateCount + " duplicate(s).");
        return result;
    }

    // =========================================================================
    // 2. PARSE EXCEL / CSV FILES INTO GENERIC OBJECT ROWS
    // =========================================================================
    public List<Object[]> parseFileToRows(MultipartFile file, String effectiveUser) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        if (fileName.endsWith(".csv")) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                
                String headerLine = reader.readLine();
                if (headerLine == null || headerLine.trim().isEmpty()) return rows;

                String delimiter = headerLine.contains(",") ? "," : "\\|";
                String[] headers = headerLine.split(delimiter);
                Map<String, Integer> colMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    colMap.put(cleanCol(headers[i]), i);
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(delimiter, -1);

                    String accNum = getCsvVal(tokens, colMap, "ACCOUNT_NUMBER", "ACCOUNT_NO", "ACCOUNTNUMBER", "IBAN", "ACNO", "FORACID");
                    if (accNum.isEmpty()) continue;

                    String holderType = getCsvVal(tokens, colMap, "HOLDER_TYPE", "TYPE");
                    if (holderType.isEmpty()) holderType = "INDIVIDUAL";

                    String resCountry = getCsvVal(tokens, colMap, "RESIDENCE_COUNTRY", "COUNTRY", "TINCTRY", "TAXCTRY");
                    if (resCountry.isEmpty()) resCountry = "IN";

                    String tin       = getCsvVal(tokens, colMap, "TIN", "FATCATIN", "TAX_ID", "ENTITYTIN");
                    String firstName = getCsvVal(tokens, colMap, "FIRSTNAME", "FIRST_NAME");
                    String lastName  = getCsvVal(tokens, colMap, "LASTNAME", "LAST_NAME");
                    String orgName   = getCsvVal(tokens, colMap, "NAME", "ORGANISATION_NAME", "COMPANY_NAME", "ENTITYLEGALNAME");
                    String city      = getCsvVal(tokens, colMap, "CITY", "ADDRESS_CITY", "CITYLOCATION");
                    String addr      = getCsvVal(tokens, colMap, "ADDRESS", "ADDR1", "ADD");
                    String curr      = getCsvVal(tokens, colMap, "CURRENCY_CODE", "CRN", "CURRENCY", "CURR");
                    if (curr.isEmpty()) curr = "MUR";

                    double balance = 0.0;
                    try {
                        String balStr = getCsvVal(tokens, colMap, "ACCOUNT_BALANCE", "BALANCE", "BAL").replace(",", "");
                        if (!balStr.isEmpty()) balance = Double.parseDouble(balStr);
                    } catch (Exception ignored) {}

                    double intPaid = 0.0;
                    try {
                        String intStr = getCsvVal(tokens, colMap, "INTPAID", "INT").replace(",", "");
                        if (!intStr.isEmpty()) intPaid = Double.parseDouble(intStr);
                    } catch (Exception ignored) {}

                    rows.add(new Object[] {
                        accNum, "OECD602", "false", "false", "false",
                        holderType.toUpperCase(), null, "CRS1101", "CRS1202", "CRS901",
                        resCountry.toUpperCase(), tin, resCountry.toUpperCase(),
                        firstName, lastName, orgName,
                        null, city, resCountry.toUpperCase(),
                        addr, city, null, resCountry.toUpperCase(),
                        null, null, null, null, null, null, null, null, null, null, null, null, null,
                        balance, curr.toUpperCase(), "CRS502", intPaid,
                        "2025", "COMPLETED", effectiveUser
                    });
                }
            }
        } else {
            // Excel (.xlsx / .xls) parsing
            try (InputStream is = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(is)) {

                int totalSheets = workbook.getNumberOfSheets();
                for (int s = 0; s < totalSheets; s++) {
                    Sheet sheet = workbook.getSheetAt(s);
                    String sheetName = sheet.getSheetName() != null ? sheet.getSheetName().trim().toUpperCase() : "";

                    if (sheetName.startsWith("INT") || sheet.getPhysicalNumberOfRows() <= 1) {
                        continue;
                    }

                    Row headerRow = sheet.getRow(0);
                    if (headerRow == null) continue;

                    Map<String, Integer> colMap = new HashMap<>();
                    for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                        Cell cell = headerRow.getCell(c);
                        if (cell != null) {
                            String name = getCellString(cell);
                            if (name != null && !name.trim().isEmpty()) {
                                colMap.put(cleanCol(name), c);
                            }
                        }
                    }

                    boolean isFileCrs101 = fileName.contains("101") || sheetName.contains("101");
                    boolean isIndiv = hasKey(colMap, "FIRSTNAME") && hasKey(colMap, "LASTNAME") && !hasKey(colMap, "CRSCODE") && !hasKey(colMap, "UBOTAX") && !hasKey(colMap, "CONTROLLINGPERSON") && !isFileCrs101;
                    boolean isCrs101 = isFileCrs101 || hasKey(colMap, "UBOTAX") || hasKey(colMap, "UBOCIF") || hasKey(colMap, "CONTROLLINGPERSON") || hasKey(colMap, "UBOTIN");

                    int lastRow = sheet.getLastRowNum();
                    for (int r = 1; r <= lastRow; r++) {
                        Row row = sheet.getRow(r);
                        if (row == null) continue;

                        if (isIndiv) {
                            String accNum = getVal(row, colMap, "IBAN", "ACNO", "ACCOUNTNUMBER", "ACCOUNT", "FORACID");
                            if (accNum.isEmpty()) continue;

                            String firstName = getVal(row, colMap, "FIRSTNAME", "FIRST_NAME");
                            String lastName  = getVal(row, colMap, "LASTNAME", "LAST_NAME");
                            String country   = getVal(row, colMap, "TINCTRY", "COUNTRY", "NATION");
                            if (country.isEmpty()) country = "IN";

                            String tin       = getVal(row, colMap, "FATCATIN", "TIN");
                            String city      = getVal(row, colMap, "CITY");
                            String address   = getVal(row, colMap, "ADDRESS");
                            Date dob         = getCellDate(row, colMap, "DOB");

                            double balance = getNumeric(row, colMap, "BALANCE", "BAL", "ACCOUNTBALANCE");
                            double intPaid = getNumeric(row, colMap, "INTPAID", "INT");
                            String curr    = getVal(row, colMap, "CRN", "CURRENCYCODE", "CURRENCY", "CRNCY");
                            if (curr.isEmpty()) curr = "MUR";

                            String isDormant = getBooleanStr(row, colMap, "DORMANT");
                            String isClosed  = getBooleanStr(row, colMap, "CLOSE", "CLOSED");
                            String isUndoc   = getBooleanStr(row, colMap, "UNDOC");

                            rows.add(new Object[] {
                                accNum, "OECD602", isClosed, isDormant, isUndoc,
                                "INDIVIDUAL", null, "CRS1101", "CRS1202", "CRS901",
                                country.toUpperCase(), tin, country.toUpperCase(),
                                firstName, lastName, null,
                                dob, city, country.toUpperCase(),
                                address, city, null, country.toUpperCase(),
                                null, null, null, null, null, null, null, null, null, null, null, null, null,
                                balance, curr.toUpperCase(), "CRS502", intPaid,
                                "2025", "COMPLETED", effectiveUser
                            });
                        } else {
                            String cpFirstCheck = getVal(row, colMap, "CONTROLLINGPERSON", "FIRSTNAME", "CP_FIRST_NAME");
                            String cpTaxCheck   = getVal(row, colMap, "UBOTAX", "CP_TIN", "UBOTIN");
                            boolean rowIsCrs101 = isCrs101 || !cpFirstCheck.isEmpty() || !cpTaxCheck.isEmpty();

                            String accNum = getVal(row, colMap, "IBAN", "FORACID", "ACCOUNTNUMBER", "ACCOUNT");
                            if (accNum.isEmpty()) continue;

                            String orgName = getVal(row, colMap, "NAME", "ORGANISATIONNAME", "ENTITYNAME", "ENTITYLEGALNAME");
                            String txCtry  = getVal(row, colMap, "TXCTRY", "CTRY", "ENTITYCOUNTRY", "TAXCTRY");
                            if (txCtry.isEmpty()) txCtry = "MU";

                            String tin     = getVal(row, colMap, "TIN", "ENTITYTIN");
                            String addr    = getVal(row, colMap, "ADD", "ADDR1", "ADDRESS");
                            String city    = getVal(row, colMap, "CITY", "CITYLOCATION", "UNNAMED15");

                            double balance = getNumeric(row, colMap, "BAL", "BALANCE", "ACCOUNTBALANCE");
                            double intPaid = getNumeric(row, colMap, "INT");
                            String curr    = getVal(row, colMap, "CRNCY", "CRN", "CURRENCYCODE", "CURRENCY", "CUR");
                            if (curr.isEmpty()) curr = "USD";

                            String isDormant = getBooleanStr(row, colMap, "DORMANT");
                            String isClosed  = getBooleanStr(row, colMap, "CLOSED", "CLOSE");
                            String isUndoc   = getBooleanStr(row, colMap, "UNDOC");

                            if (rowIsCrs101) {
                                String cpFull = getVal(row, colMap, "CONTROLLINGPERSON", "CP_FIRST_NAME");
                                String cpFirst = cpFull;
                                String cpLast = getVal(row, colMap, "LASTNAME", "CPLASTNAME");
                                
                                // If last name is blank, split the combined controlling person name
                                if (cpLast.isEmpty() && cpFull.contains(" ")) {
                                    int lastSpace = cpFull.lastIndexOf(" ");
                                    cpFirst = cpFull.substring(0, lastSpace).trim();
                                    cpLast = cpFull.substring(lastSpace + 1).trim();
                                }

                                String cpTax   = getVal(row, colMap, "UBOTAX", "CP_TIN", "UBOTIN");
                                String cpCtry  = getVal(row, colMap, "UBOCTRY", "CPRESIDENCECOUNTRY", "UBOCOUNTRY");
                                if (cpCtry.isEmpty()) cpCtry = "GB";

                                String cpCity  = getVal(row, colMap, "UBOCITY", "CPADDRESSCITY");
                                String cpAddr  = getVal(row, colMap, "UBOADD", "CPADDRESSSTREET");
                                Date cpDob     = getCellDate(row, colMap, "DOB");

                                rows.add(new Object[] {
                                    accNum, "OECD602", isClosed, isDormant, isUndoc,
                                    "ORGANISATION", "CRS101", "CRS1101", "CRS1202", "CRS901",
                                    txCtry.toUpperCase(), tin, txCtry.toUpperCase(),
                                    null, null, orgName,
                                    null, null, null,
                                    addr, city, null, txCtry.toUpperCase(),
                                    cpFirst, cpLast, cpCtry.toUpperCase(), cpTax, "CRS801", "CRS1001",
                                    cpDob, cpCity, cpCtry.toUpperCase(), cpAddr, cpCity, null, cpCtry.toUpperCase(),
                                    balance, curr.toUpperCase(), "CRS502", intPaid,
                                    "2025", "COMPLETED", effectiveUser
                                });
                            } else {
                                // CRS102 / CRS103
                                String crsCode = getVal(row, colMap, "CRSCODE", "CRS_CODE");
                                if (crsCode.isEmpty()) crsCode = "CRS102";

                                rows.add(new Object[] {
                                    accNum, "OECD602", isClosed, isDormant, isUndoc,
                                    "ORGANISATION", crsCode, "CRS1101", "CRS1202", "CRS901",
                                    txCtry.toUpperCase(), tin, txCtry.toUpperCase(),
                                    null, null, orgName,
                                    null, null, null,
                                    addr, city, null, txCtry.toUpperCase(),
                                    null, null, null, null, null, null, null, null, null, null, null, null, null,
                                    balance, curr.toUpperCase(), "CRS502", intPaid,
                                    "2025", "COMPLETED", effectiveUser
                                });
                            }
                        }
                    }
                }
            }
        }
        return rows;
    }

    private void executeSingleInsert(String sql, Object[] r) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int col = 0; col < r.length; col++) {
                Object val = r[col];
                int idx = col + 1;
                if (val == null) {
                    ps.setNull(idx, Types.VARCHAR);
                } else if (val instanceof java.util.Date) {
                    ps.setDate(idx, new java.sql.Date(((java.util.Date) val).getTime()));
                } else if (val instanceof Double) {
                    ps.setDouble(idx, (Double) val);
                } else {
                    ps.setString(idx, val.toString().trim());
                }
            }
            return ps;
        });
    }

    // =========================================================================
    // 3. RECTIFY DUPLICATE: OVERWRITE MAIN DB RECORD & MARK RESOLVED
    // =========================================================================
    @Transactional
    public boolean rectifyDuplicateRecord(Long dupId, Double updatedBalance, String updatedStatus) {
        Map<String, Object> dup;
        try {
            dup = jdbcTemplate.queryForMap("SELECT * FROM BRECON.CRS_DUPLICATE_RECORDS WHERE DUP_ID = ?", dupId);
        } catch (Exception e) {
            return false;
        }

        String accNum = (String) dup.get("ACCOUNT_NUMBER");
        String year = (String) dup.get("REPORTING_YEAR");

        int updated = jdbcTemplate.update(
            "UPDATE BRECON.CRS_REPORT_DATA SET ACCOUNT_BALANCE = ?, STATUS = ? WHERE ACCOUNT_NUMBER = ? AND REPORTING_YEAR = ?",
            updatedBalance, updatedStatus, accNum, year
        );

        jdbcTemplate.update("UPDATE BRECON.CRS_DUPLICATE_RECORDS SET STATUS = 'RESOLVED' WHERE DUP_ID = ?", dupId);
        return updated > 0;
    }

    // =========================================================================
    // 4. MASTER XML GENERATOR: ASSEMBLES FULL OECD CRS v3.0 XML FILE
    // =========================================================================
    public String generateCrsXml(String reportingYear) {
        if (reportingYear == null || reportingYear.trim().isEmpty()) {
            reportingYear = "2025";
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        String msgRefId = BANK_COUNTRY + reportingYear + BANK_COUNTRY + BANK_TIN + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        String sql = "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE REPORTING_YEAR = ? AND STATUS != 'EXCLUDED' ORDER BY REPORT_ID ASC";
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql, reportingYear);

        boolean isNil = records.isEmpty();
        String messageTypeIndic = isNil ? "CRS703" : "CRS701";

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<crs:CRS_OECD xmlns:crs=\"urn:oecd:ties:crs:v3\"\n");
        xml.append("              xmlns:cfc=\"urn:oecd:ties:commontypesfatcacrs:v2\"\n");
        xml.append("              xmlns:stf=\"urn:oecd:ties:crsstf:v5\"\n");
        xml.append("              xmlns:iso=\"urn:oecd:ties:isocrstypes:v1\"\n");
        xml.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("              version=\"3.0\">\n");

        xml.append("    <crs:MessageSpec>\n");
        xml.append("        <crs:SendingCompanyIN>").append(BANK_TIN).append("</crs:SendingCompanyIN>\n");
        xml.append("        <crs:TransmittingCountry>").append(BANK_COUNTRY).append("</crs:TransmittingCountry>\n");
        xml.append("        <crs:ReceivingCountry>").append(BANK_COUNTRY).append("</crs:ReceivingCountry>\n");
        xml.append("        <crs:MessageType>CRS</crs:MessageType>\n");
        xml.append("        <crs:MessageRefId>").append(msgRefId).append("</crs:MessageRefId>\n");
        xml.append("        <crs:MessageTypeIndic>").append(messageTypeIndic).append("</crs:MessageTypeIndic>\n");
        xml.append("        <crs:ReportingPeriod>").append(reportingYear).append("-12-31</crs:ReportingPeriod>\n");
        xml.append("        <crs:Timestamp>").append(timestamp).append("</crs:Timestamp>\n");
        xml.append("    </crs:MessageSpec>\n");

        xml.append("    <crs:CrsBody>\n");
        xml.append("        <crs:ReportingFI>\n");
        xml.append("            <crs:ResCountryCode>").append(BANK_COUNTRY).append("</crs:ResCountryCode>\n");
        xml.append("            <crs:IN issuedBy=\"").append(BANK_COUNTRY).append("\" INType=\"TIN\">").append(BANK_TIN).append("</crs:IN>\n");
        xml.append("            <crs:Name>").append(escapeXml(BANK_NAME)).append("</crs:Name>\n");
        xml.append("            <crs:Address>\n");
        xml.append("                <cfc:CountryCode>").append(BANK_COUNTRY).append("</cfc:CountryCode>\n");
        xml.append("                <cfc:AddressFree>").append(escapeXml(BANK_ADDRESS)).append("</cfc:AddressFree>\n");
        xml.append("            </crs:Address>\n");
        xml.append("            <crs:DocSpec>\n");
        xml.append("                <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>\n");
        xml.append("                <stf:DocRefId>").append(BANK_COUNTRY).append(reportingYear).append(BANK_COUNTRY).append(BANK_TIN).append("100000</stf:DocRefId>\n");
        xml.append("            </crs:DocSpec>\n");
        xml.append("        </crs:ReportingFI>\n");

        xml.append("        <crs:ReportingGroup>\n");
        if (!isNil) {
            int seq = 1;
            for (Map<String, Object> row : records) {
                appendAccountReport(xml, row, reportingYear, seq++);
            }
        }
        xml.append("        </crs:ReportingGroup>\n");
        xml.append("    </crs:CrsBody>\n");
        xml.append("</crs:CRS_OECD>");

        return xml.toString();
    }

    // =========================================================================
    // 5. SINGLE RECORD GENERATOR (FOR INDIVIDUAL XML DOWNLOAD)
    // =========================================================================
    public String generateSingleCrsXml(Long reportId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE REPORT_ID = ?", reportId
        );

        String reportingYear = String.valueOf(row.getOrDefault("REPORTING_YEAR", "2025"));
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        String msgRefId = BANK_COUNTRY + reportingYear + BANK_COUNTRY + BANK_TIN + "_REC_" + reportId;

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<crs:CRS_OECD xmlns:crs=\"urn:oecd:ties:crs:v3\"\n");
        xml.append("              xmlns:cfc=\"urn:oecd:ties:commontypesfatcacrs:v2\"\n");
        xml.append("              xmlns:stf=\"urn:oecd:ties:crsstf:v5\"\n");
        xml.append("              xmlns:iso=\"urn:oecd:ties:isocrstypes:v1\"\n");
        xml.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("              version=\"3.0\">\n");

        xml.append("    <crs:MessageSpec>\n");
        xml.append("        <crs:SendingCompanyIN>").append(BANK_TIN).append("</crs:SendingCompanyIN>\n");
        xml.append("        <crs:TransmittingCountry>").append(BANK_COUNTRY).append("</crs:TransmittingCountry>\n");
        xml.append("        <crs:ReceivingCountry>").append(BANK_COUNTRY).append("</crs:ReceivingCountry>\n");
        xml.append("        <crs:MessageType>CRS</crs:MessageType>\n");
        xml.append("        <crs:MessageRefId>").append(msgRefId).append("</crs:MessageRefId>\n");
        xml.append("        <crs:MessageTypeIndic>CRS701</crs:MessageTypeIndic>\n");
        xml.append("        <crs:ReportingPeriod>").append(reportingYear).append("-12-31</crs:ReportingPeriod>\n");
        xml.append("        <crs:Timestamp>").append(timestamp).append("</crs:Timestamp>\n");
        xml.append("    </crs:MessageSpec>\n");

        xml.append("    <crs:CrsBody>\n");
        xml.append("        <crs:ReportingFI>\n");
        xml.append("            <crs:ResCountryCode>").append(BANK_COUNTRY).append("</crs:ResCountryCode>\n");
        xml.append("            <crs:IN issuedBy=\"").append(BANK_COUNTRY).append("\" INType=\"TIN\">").append(BANK_TIN).append("</crs:IN>\n");
        xml.append("            <crs:Name>").append(escapeXml(BANK_NAME)).append("</crs:Name>\n");
        xml.append("            <crs:Address>\n");
        xml.append("                <cfc:CountryCode>").append(BANK_COUNTRY).append("</cfc:CountryCode>\n");
        xml.append("                <cfc:AddressFree>").append(escapeXml(BANK_ADDRESS)).append("</cfc:AddressFree>\n");
        xml.append("            </crs:Address>\n");
        xml.append("            <crs:DocSpec>\n");
        xml.append("                <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>\n");
        xml.append("                <stf:DocRefId>").append(BANK_COUNTRY).append(reportingYear).append(BANK_COUNTRY).append(BANK_TIN).append("100000</stf:DocRefId>\n");
        xml.append("            </crs:DocSpec>\n");
        xml.append("        </crs:ReportingFI>\n");

        xml.append("        <crs:ReportingGroup>\n");
        appendAccountReport(xml, row, reportingYear, 1);
        xml.append("        </crs:ReportingGroup>\n");
        xml.append("    </crs:CrsBody>\n");
        xml.append("</crs:CRS_OECD>");

        return xml.toString();
    }

    // =========================================================================
    // 6. BUILD ACCOUNT REPORT XML ELEMENT
    // =========================================================================
    private void appendAccountReport(StringBuilder xml, Map<String, Object> r, String reportingYear, int seq) {
        String holderType = String.valueOf(r.getOrDefault("HOLDER_TYPE", "INDIVIDUAL"));
        String category   = r.get("ACCT_HOLDER_CATEGORY") != null ? String.valueOf(r.get("ACCT_HOLDER_CATEGORY")) : "";
        String accNum     = String.valueOf(r.get("ACCOUNT_NUMBER"));
        String accNumType = String.valueOf(r.getOrDefault("ACCT_NUMBER_TYPE", "OECD602"));
        String isClosed   = String.valueOf(r.getOrDefault("IS_CLOSED", "false"));
        String isDormant  = String.valueOf(r.getOrDefault("IS_DORMANT", "false"));
        String isUndoc    = String.valueOf(r.getOrDefault("IS_UNDOCUMENTED", "false"));

        String docRefId = BANK_COUNTRY + reportingYear + "-" + (category.isEmpty() ? "IND" : category) + "-" + String.format("%06d", seq);

        xml.append("            <crs:AccountReport>\n");
        xml.append("                <crs:DocSpec>\n");
        xml.append("                    <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>\n");
        xml.append("                    <stf:DocRefId>").append(docRefId).append("</stf:DocRefId>\n");
        xml.append("                </crs:DocSpec>\n");

        xml.append("                <crs:AccountNumber AcctNumberType=\"").append(accNumType)
           .append("\" ClosedAccount=\"").append(isClosed)
           .append("\" DormantAccount=\"").append(isDormant)
           .append("\" UndocumentedAccount=\"").append(isUndoc).append("\">")
           .append(escapeXml(accNum)).append("</crs:AccountNumber>\n");

        xml.append("                <crs:AccountHolder>\n");
        xml.append("                    <crs:SelfCert>").append(r.getOrDefault("SELF_CERT_FLAG", "CRS901")).append("</crs:SelfCert>\n");

        if ("INDIVIDUAL".equalsIgnoreCase(holderType)) {
            String ctry = String.valueOf(r.getOrDefault("RESIDENCE_COUNTRY", "IN"));
            String first = safeVal(r.get("FIRST_NAME"));
            String last  = safeVal(r.get("LAST_NAME"));
            String tin   = safeVal(r.get("TIN"));
            String street= safeVal(r.get("ADDRESS_STREET"));
            String city  = safeVal(r.get("ADDRESS_CITY"));

            xml.append("                    <crs:Individual>\n");
            xml.append("                        <crs:ResCountryCode>").append(ctry).append("</crs:ResCountryCode>\n");
            if (!tin.isEmpty()) {
                xml.append("                        <crs:TIN issuedBy=\"").append(ctry).append("\">").append(escapeXml(tin)).append("</crs:TIN>\n");
            }
            xml.append("                        <crs:Name>\n");
            xml.append("                            <crs:FirstName>").append(escapeXml(first)).append("</crs:FirstName>\n");
            xml.append("                            <crs:LastName>").append(escapeXml(last)).append("</crs:LastName>\n");
            xml.append("                        </crs:Name>\n");
            xml.append("                        <crs:Address>\n");
            xml.append("                            <cfc:CountryCode>").append(ctry).append("</cfc:CountryCode>\n");
            xml.append("                            <cfc:AddressFix>\n");
            if (!street.isEmpty()) xml.append("                                <cfc:Street>").append(escapeXml(street)).append("</cfc:Street>\n");
            if (!city.isEmpty())   xml.append("                                <cfc:City>").append(escapeXml(city)).append("</cfc:City>\n");
            xml.append("                            </cfc:AddressFix>\n");
            xml.append("                        </crs:Address>\n");

            if (r.get("BIRTH_DATE") != null) {
                String dobStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) r.get("BIRTH_DATE"));
                xml.append("                        <crs:BirthInfo>\n");
                xml.append("                            <crs:BirthDate>").append(dobStr).append("</crs:BirthDate>\n");
                if (!city.isEmpty()) xml.append("                            <crs:City>").append(escapeXml(city)).append("</crs:City>\n");
                xml.append("                        </crs:BirthInfo>\n");
            }
            xml.append("                    </crs:Individual>\n");
        } else {
            String orgName = safeVal(r.get("ORGANISATION_NAME"));
            String ctry    = safeVal(r.get("RESIDENCE_COUNTRY"));
            String tin     = safeVal(r.get("TIN"));
            String street  = safeVal(r.get("ADDRESS_STREET"));
            String city    = safeVal(r.get("ADDRESS_CITY"));
            String post    = safeVal(r.get("ADDRESS_POSTCODE"));

            xml.append("                    <crs:Organisation>\n");
            xml.append("                        <crs:ResCountryCode>").append(ctry).append("</crs:ResCountryCode>\n");
            if (!tin.isEmpty()) {
                xml.append("                        <crs:TIN issuedBy=\"").append(ctry).append("\">").append(escapeXml(tin)).append("</crs:TIN>\n");
            }
            xml.append("                        <crs:Name>").append(escapeXml(orgName)).append("</crs:Name>\n");
            xml.append("                        <crs:Address legalAddressType=\"OECD304\">\n");
            xml.append("                            <cfc:CountryCode>").append(ctry).append("</cfc:CountryCode>\n");
            xml.append("                            <cfc:AddressFix>\n");
            if (!street.isEmpty()) xml.append("                                <cfc:Street>").append(escapeXml(street)).append("</cfc:Street>\n");
            if (!post.isEmpty())   xml.append("                                <cfc:PostCode>").append(escapeXml(post)).append("</cfc:PostCode>\n");
            if (!city.isEmpty())   xml.append("                                <cfc:City>").append(escapeXml(city)).append("</cfc:City>\n");
            xml.append("                            </cfc:AddressFix>\n");
            xml.append("                        </crs:Address>\n");
            xml.append("                    </crs:Organisation>\n");

            if (!category.isEmpty()) {
                xml.append("                    <crs:AcctHolderType>").append(category).append("</crs:AcctHolderType>\n");
            }
        }
        xml.append("                </crs:AccountHolder>\n");

        if ("CRS101".equalsIgnoreCase(category) && r.get("CP_LAST_NAME") != null) {
            String cpFirst  = safeVal(r.get("CP_FIRST_NAME"));
            String cpLast   = safeVal(r.get("CP_LAST_NAME"));
            String cpCtry   = safeVal(r.get("CP_RESIDENCE_COUNTRY"));
            String cpTin    = safeVal(r.get("CP_TIN"));
            String cpStreet = safeVal(r.get("CP_ADDRESS_STREET"));
            String cpCity   = safeVal(r.get("CP_ADDRESS_CITY"));
            String cpPost   = safeVal(r.get("CP_ADDRESS_POSTCODE"));
            String cpRole   = String.valueOf(r.getOrDefault("CP_ROLE", "CRS801"));

            xml.append("                <crs:ControllingPerson>\n");
            xml.append("                    <crs:Individual>\n");
            xml.append("                        <crs:ResCountryCode>").append(cpCtry).append("</crs:ResCountryCode>\n");
            if (!cpTin.isEmpty()) {
                xml.append("                        <crs:TIN issuedBy=\"").append(cpCtry).append("\">").append(escapeXml(cpTin)).append("</crs:TIN>\n");
            }
            xml.append("                        <crs:Name>\n");
            xml.append("                            <crs:FirstName>").append(escapeXml(cpFirst)).append("</crs:FirstName>\n");
            xml.append("                            <crs:LastName>").append(escapeXml(cpLast)).append("</crs:LastName>\n");
            xml.append("                        </crs:Name>\n");
            xml.append("                        <crs:Address legalAddressType=\"OECD304\">\n");
            xml.append("                            <cfc:CountryCode>").append(cpCtry).append("</cfc:CountryCode>\n");
            xml.append("                            <cfc:AddressFix>\n");
            if (!cpStreet.isEmpty()) xml.append("                                <cfc:Street>").append(escapeXml(cpStreet)).append("</cfc:Street>\n");
            if (!cpPost.isEmpty())   xml.append("                                <cfc:PostCode>").append(escapeXml(cpPost)).append("</cfc:PostCode>\n");
            if (!cpCity.isEmpty())   xml.append("                                <cfc:City>").append(escapeXml(cpCity)).append("</cfc:City>\n");
            xml.append("                            </cfc:AddressFix>\n");
            xml.append("                        </crs:Address>\n");

            if (r.get("CP_BIRTH_DATE") != null) {
                String cpDobStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) r.get("CP_BIRTH_DATE"));
                xml.append("                        <crs:BirthInfo>\n");
                xml.append("                            <crs:BirthDate>").append(cpDobStr).append("</crs:BirthDate>\n");
                if (!cpCity.isEmpty()) xml.append("                            <crs:City>").append(escapeXml(cpCity)).append("</crs:City>\n");
                xml.append("                            <crs:CountryInfo><crs:CountryCode>").append(cpCtry).append("</crs:CountryCode></crs:CountryInfo>\n");
                xml.append("                        </crs:BirthInfo>\n");
            }
            xml.append("                    </crs:Individual>\n");
            xml.append("                    <crs:CtrlgPersonType>").append(cpRole).append("</crs:CtrlgPersonType>\n");
            xml.append("                    <crs:SelfCert>").append(r.getOrDefault("CP_SELF_CERT", "CRS1001")).append("</crs:SelfCert>\n");
            xml.append("                </crs:ControllingPerson>\n");
        }

        double bal = r.get("ACCOUNT_BALANCE") instanceof Number ? ((Number) r.get("ACCOUNT_BALANCE")).doubleValue() : 0.0;
        String curr = String.valueOf(r.getOrDefault("CURRENCY_CODE", "MUR"));
        xml.append("                <crs:AccountBalance currCode=\"").append(curr).append("\">")
           .append(String.format(Locale.US, "%.2f", bal)).append("</crs:AccountBalance>\n");

        double pmt = r.get("PAYMENT_AMOUNT") instanceof Number ? ((Number) r.get("PAYMENT_AMOUNT")).doubleValue() : 0.0;
        xml.append("                <crs:Payment>\n");
        xml.append("                    <crs:Type>CRS502</crs:Type>\n");
        xml.append("                    <crs:PaymentAmnt currCode=\"").append(curr).append("\">")
           .append(String.format(Locale.US, "%.2f", pmt)).append("</crs:PaymentAmnt>\n");
        xml.append("                </crs:Payment>\n");

        xml.append("                <crs:DDProcedure>").append(r.getOrDefault("DD_PROCEDURE", "CRS1202")).append("</crs:DDProcedure>\n");
        xml.append("                <crs:AccountType>").append(r.getOrDefault("ACCOUNT_TYPE", "CRS1101")).append("</crs:AccountType>\n");
        xml.append("            </crs:AccountReport>\n");
    }

    // =========================================================================
    // 7. SAFE POI PARSING UTILITIES WITH VERSION-SAFE SCIENTIFIC NOTATION FIX
    // =========================================================================
    private String cleanCol(String name) {
        return name.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    private boolean hasKey(Map<String, Integer> colMap, String keyword) {
        String cleanKey = cleanCol(keyword);
        for (String col : colMap.keySet()) {
            if (col.contains(cleanKey)) return true;
        }
        return false;
    }

    private String getCsvVal(String[] tokens, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            Integer idx = colMap.get(cleanCol(k));
            if (idx != null && idx >= 0 && idx < tokens.length) {
                String val = tokens[idx].trim().replaceAll("^\"|\"$", "");
                if (!val.isEmpty()) return val;
            }
        }
        return "";
    }

    private String getVal(Row row, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            String cleanKey = cleanCol(k);
            for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
                if (entry.getKey().contains(cleanKey)) {
                    Cell cell = row.getCell(entry.getValue(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        try {
                            if (cell.getCellType() == 0) { // Cell.CELL_TYPE_NUMERIC constant across all POI versions
                                double val = cell.getNumericCellValue();
                                return String.format(Locale.US, "%.0f", val).trim();
                            }
                        } catch (Exception ignored) {}

                        String s = dataFormatter.formatCellValue(cell).trim();
                        if (!s.isEmpty()) return s;
                    }
                }
            }
        }
        return "";
    }

    private double getNumeric(Row row, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            String cleanKey = cleanCol(k);
            for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
                if (entry.getKey().contains(cleanKey)) {
                    Cell cell = row.getCell(entry.getValue(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        try {
                            return cell.getNumericCellValue();
                        } catch (Exception e) {
                            try {
                                String s = dataFormatter.formatCellValue(cell).replaceAll("[,\\s]", "");
                                if (!s.isEmpty()) {
                                    return Double.parseDouble(s);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    private Date getCellDate(Row row, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            String cleanKey = cleanCol(k);
            for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
                if (entry.getKey().contains(cleanKey)) {
                    Cell cell = row.getCell(entry.getValue(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        try {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                return cell.getDateCellValue();
                            }
                        } catch (Exception ignored) {}

                        try {
                            String s = dataFormatter.formatCellValue(cell).trim();
                            if (!s.isEmpty()) {
                                return new SimpleDateFormat("yyyy-MM-dd").parse(s);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return null;
    }

    private String getBooleanStr(Row row, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            String cleanKey = cleanCol(k);
            for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
                if (entry.getKey().contains(cleanKey)) {
                    Cell cell = row.getCell(entry.getValue(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        try {
                            return String.valueOf(cell.getBooleanCellValue());
                        } catch (Exception e) {
                            String s = dataFormatter.formatCellValue(cell).trim().toLowerCase();
                            if ("true".equals(s) || "y".equals(s) || "yes".equals(s) || "1".equals(s)) {
                                return "true";
                            }
                        }
                    }
                }
            }
        }
        return "false";
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        try {
            return dataFormatter.formatCellValue(cell).trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String safeVal(Object obj) {
        if (obj == null) return "";
        String s = String.valueOf(obj).trim();
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

 // =========================================================================
    // 8. DASHBOARD DATA API METHOD FOR CONTROLLER
    // =========================================================================
    public Map<String, Object> getCrsDashboardData(String reportingYear) {
        if (reportingYear == null || reportingYear.trim().isEmpty()) {
            reportingYear = "2025";
        }
        Map<String, Object> data = new HashMap<>();
        try {
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE REPORTING_YEAR = ? ORDER BY REPORT_ID DESC", reportingYear
            );
            
            long total = records.size();
            long pending = records.stream().filter(r -> "PENDING".equalsIgnoreCase(String.valueOf(r.get("STATUS")))).count();
            long completed = records.stream().filter(r -> "COMPLETED".equalsIgnoreCase(String.valueOf(r.get("STATUS")))).count();
            long error = total - pending - completed;

            data.put("totalCount", total);
            data.put("pendingCount", pending);
            data.put("completedCount", completed);
            data.put("errorCount", error);
            data.put("records", records);
        } catch (Exception e) {
            logger.error("Error fetching CRS dashboard data", e);
            data.put("totalCount", 0);
            data.put("pendingCount", 0);
            data.put("completedCount", 0);
            data.put("errorCount", 0);
            data.put("records", new ArrayList<>());
        }
        return data;
    }

    
}