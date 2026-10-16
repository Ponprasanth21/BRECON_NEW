
package com.bornfire.recon.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
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

	private final DataFormatter dataFormatter = new DataFormatter();

	// =========================================================================
	// 1. INGESTION WITH EXACT TABLE SCHEMA MATCHING & REF_ID DEDUPLICATION
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

        String fileName = (file.getOriginalFilename() != null) ? file.getOriginalFilename() : "UNKNOWN";
        String effectiveUser = (userId != null && !userId.trim().isEmpty()) ? userId : "SYSTEM";
        List<Object[]> parsedRows = parseFileToRows(file, effectiveUser);
        int insertedCount = 0;
        int duplicateCount = 0;

        // Check by REF_ID
        String checkSql = "SELECT COUNT(1) FROM BRECON.CRS_REPORT_DATA WHERE REF_ID = ?";

        // Insert into Master Data
        String insertSql = "INSERT INTO BRECON.CRS_REPORT_DATA (" +
                "  REPORT_TYPE, REF_ID, CRS_CODE, ACCOUNT_NUMBER, IBAN, CURRENCY_CODE, " +
                "  ACCOUNT_BALANCE, INTEREST_PAID, IS_DORMANT, IS_CLOSED, IS_UNDOCUMENTED, " +
                "  CUSTOMER_ID, ENTITY_NAME, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, " +
                "  NATIONALITY, TAX_COUNTRY, TIN, ADDRESS, CITY, COUNTRY, " +
                "  UBO_CUSTOMER_ID, UBO_FIRST_NAME, UBO_LAST_NAME, UBO_DOB, UBO_TAX_ID, " +
                "  UBO_ADDRESS, UBO_CITY, UBO_COUNTRY, CREATED_DATE, ENTRY_USER " +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Insert into Duplicate Rectification Queue
        String logDupSql = "INSERT INTO BRECON.CRS_DUPLICATE_RECORDS (" +
                "  FILE_NAME, ACCOUNT_NUMBER, HOLDER_TYPE, HOLDER_NAME, " +
                "  RESIDENCE_COUNTRY, TIN, ACCOUNT_BALANCE, CURRENCY_CODE, " +
                "  REPORTING_YEAR, REASON, STATUS " +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, '2025', 'Duplicate REF_ID already present in database', 'PENDING')";

        for (Object[] r : parsedRows) {
            String refId  = (String) r[1]; // REF_ID
            String accNum = (String) r[3]; // ACCOUNT_NUMBER

            Integer exists = 0;
            try {
                exists = jdbcTemplate.queryForObject(checkSql, Integer.class, refId);
            } catch (Exception ignored) {}

            if (exists != null && exists > 0) {
                // Route to duplicate exception queue
                String reportType = (String) r[0];
                String firstName  = (String) r[13];
                String lastName   = (String) r[14];
                String entityName = (String) r[12];
                String holderName = (entityName != null && !entityName.isEmpty()) ? entityName : ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
                String country    = (String) r[17];
                String tin        = (String) r[18];
                Double balance    = (Double) r[6];
                String currency   = (String) r[5];

                try {
                    jdbcTemplate.update(logDupSql, fileName, accNum, reportType, holderName, country, tin, balance, currency);
                } catch (Exception ex) {
                    logger.warn("Could not insert duplicate into CRS_DUPLICATE_RECORDS: {}", ex.getMessage());
                }
                duplicateCount++;
            } else {
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
	// 2. PARSE EXCEL / CSV INTO EXACT 32 COLUMNS (WITH REAL REFID SUPPORT)
	// =========================================================================
	public List<Object[]> parseFileToRows(MultipartFile file, String effectiveUser) throws Exception {
		List<Object[]> rows = new ArrayList<>();
		String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
		Date now = new Date();

		if (fileName.endsWith(".csv")) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

				String headerLine = reader.readLine();
				if (headerLine == null || headerLine.trim().isEmpty())
					return rows;

				String delimiter = headerLine.contains(",") ? "," : "\\|";
				String[] headers = headerLine.split(delimiter);
				Map<String, Integer> colMap = new HashMap<>();
				for (int i = 0; i < headers.length; i++) {
					colMap.put(cleanCol(headers[i]), i);
				}

				String line;
				while ((line = reader.readLine()) != null) {
					if (line.trim().isEmpty())
						continue;
					String[] tokens = line.split(delimiter, -1);

					String accNum = getCsvVal(tokens, colMap, "ACCOUNT_NUMBER", "ACCOUNT_NO", "ACCOUNTNUMBER", "IBAN",
							"ACNO", "FORACID");
					if (accNum.isEmpty())
						continue;

					String refId = getCsvVal(tokens, colMap, "REFID", "REF_ID", "DOCREFID");
					if (refId.isEmpty()) {
						refId = "MU2025" + accNum;
					}

					String holderType = getCsvVal(tokens, colMap, "HOLDER_TYPE", "REPORT_TYPE", "TYPE");
					if (holderType.isEmpty())
						holderType = "INDIVIDUAL";

					String country = getCsvVal(tokens, colMap, "TAX_COUNTRY", "RESIDENCE_COUNTRY", "COUNTRY", "TINCTRY",
							"TAXCTRY");
					if (country.isEmpty())
						country = "IN";

					String tin = getCsvVal(tokens, colMap, "TIN", "FATCATIN", "TAX_ID", "ENTITYTIN");
					String firstName = getCsvVal(tokens, colMap, "FIRSTNAME", "FIRST_NAME");
					String lastName = getCsvVal(tokens, colMap, "LASTNAME", "LAST_NAME");
					String entityName = getCsvVal(tokens, colMap, "NAME", "ENTITY_NAME", "ORGANISATION_NAME",
							"COMPANY_NAME");
					String city = getCsvVal(tokens, colMap, "CITY", "ADDRESS_CITY");
					String addr = getCsvVal(tokens, colMap, "ADDRESS", "ADDR1", "ADD");
					String curr = getCsvVal(tokens, colMap, "CURRENCY_CODE", "CRN", "CURRENCY", "CURR");
					if (curr.isEmpty())
						curr = "MUR";

					double balance = 0.0;
					try {
						String balStr = getCsvVal(tokens, colMap, "ACCOUNT_BALANCE", "BALANCE", "BAL").replace(",", "");
						if (!balStr.isEmpty())
							balance = Double.parseDouble(balStr);
					} catch (Exception ignored) {
					}

					double intPaid = 0.0;
					try {
						String intStr = getCsvVal(tokens, colMap, "INTEREST_PAID", "INTPAID", "INT").replace(",", "");
						if (!intStr.isEmpty())
							intPaid = Double.parseDouble(intStr);
					} catch (Exception ignored) {
					}

					String reportType = "INDIVIDUAL".equalsIgnoreCase(holderType) ? "INDIVIDUAL" : "ORGANISATION";
					String crsCode = "INDIVIDUAL".equals(reportType) ? null : "CRS102";

					rows.add(new Object[] { reportType, refId, crsCode, accNum, accNum, curr.toUpperCase(), balance,
							intPaid, "N", "N", "N", null, entityName, firstName, lastName, null, country.toUpperCase(),
							country.toUpperCase(), tin, addr, city, country.toUpperCase(), null, null, null, null, null,
							null, null, null, now, effectiveUser });
				}
			}
		} else {
			// Excel (.xlsx / .xls) parsing
			try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

				int totalSheets = workbook.getNumberOfSheets();
				for (int s = 0; s < totalSheets; s++) {
					Sheet sheet = workbook.getSheetAt(s);
					String sheetName = sheet.getSheetName() != null ? sheet.getSheetName().trim().toUpperCase() : "";

					// Exclude auxiliary sheets, instructions, and empty sheets
					if (sheetName.startsWith("INT") || sheetName.equalsIgnoreCase("SHEET2")
							|| sheet.getPhysicalNumberOfRows() <= 1) {
						continue;
					}

					Row headerRow = sheet.getRow(0);
					if (headerRow == null)
						continue;

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
					boolean isIndiv = hasKey(colMap, "FIRSTNAME") && hasKey(colMap, "LASTNAME")
							&& !hasKey(colMap, "CRSCODE") && !hasKey(colMap, "UBOTAX")
							&& !hasKey(colMap, "CONTROLLINGPERSON") && !isFileCrs101;
					boolean isCrs101 = isFileCrs101 || hasKey(colMap, "UBOTAX") || hasKey(colMap, "UBOCIF")
							|| hasKey(colMap, "CONTROLLINGPERSON") || hasKey(colMap, "UBOTIN");

					int lastRow = sheet.getLastRowNum();
					for (int r = 1; r <= lastRow; r++) {
						Row row = sheet.getRow(r);
						if (row == null)
							continue;

						String accNum = getVal(row, colMap, "IBAN", "ACNO", "ACCOUNTNUMBER", "ACCOUNT_NUMBER",
								"ACCOUNT", "FORACID");
						if (accNum.isEmpty())
							continue;

						// Retrieve real REFID from Excel; fallback to generated format if empty
						String refId = getVal(row, colMap, "REFID", "REF_ID", "DOCREFID");
						if (refId.isEmpty()) {
							refId = "MU2025" + accNum;
						}

						double balance = getNumeric(row, colMap, "BALANCE", "BAL", "ACCOUNT_BALANCE", "ACCOUNTBALANCE");
						double intPaid = getNumeric(row, colMap, "INTEREST_PAID", "INTPAID", "INT");
						String curr = getVal(row, colMap, "CRN", "CURRENCY_CODE", "CURRENCYCODE", "CURRENCY", "CRNCY");
						if (curr.isEmpty())
							curr = "MUR";

						String isDormant = getCharBoolean(row, colMap, "DORMANT", "IS_DORMANT");
						String isClosed = getCharBoolean(row, colMap, "CLOSE", "CLOSED", "IS_CLOSED");
						String isUndoc = getCharBoolean(row, colMap, "UNDOC", "IS_UNDOCUMENTED");

						if (isIndiv) {
							String firstName = getVal(row, colMap, "FIRSTNAME", "FIRST_NAME");
							String lastName = getVal(row, colMap, "LASTNAME", "LAST_NAME");
							String country = getVal(row, colMap, "TAX_COUNTRY", "TINCTRY", "COUNTRY", "NATION");
							if (country.isEmpty())
								country = "IN";

							String tin = getVal(row, colMap, "TIN", "FATCATIN");
							String city = getVal(row, colMap, "CITY");
							String address = getVal(row, colMap, "ADDRESS");
							Date dob = getCellDate(row, colMap, "DOB", "DATE_OF_BIRTH");

							rows.add(new Object[] { "INDIVIDUAL", refId, null, accNum, accNum, curr.toUpperCase(),
									balance, intPaid, isDormant, isClosed, isUndoc, null, null, firstName, lastName,
									dob, country.toUpperCase(), country.toUpperCase(), tin, address, city,
									country.toUpperCase(), null, null, null, null, null, null, null, null, now,
									effectiveUser });
						} else {
							String cpFirstCheck = getVal(row, colMap, "CONTROLLINGPERSON", "UBO_FIRST_NAME",
									"FIRSTNAME");
							String cpTaxCheck = getVal(row, colMap, "UBOTAX", "UBO_TAX_ID", "CP_TIN", "UBOTIN");
							boolean rowIsCrs101 = isCrs101 || !cpFirstCheck.isEmpty() || !cpTaxCheck.isEmpty();

							String entityName = getVal(row, colMap, "NAME", "ENTITY_NAME", "ORGANISATIONNAME",
									"ENTITYNAME");
							String txCtry = getVal(row, colMap, "TAX_COUNTRY", "TXCTRY", "CTRY", "COUNTRY");
							if (txCtry.isEmpty())
								txCtry = "MU";

							String tin = getVal(row, colMap, "TIN", "ENTITYTIN");
							String addr = getVal(row, colMap, "ADD", "ADDR1", "ADDRESS");
							String city = getVal(row, colMap, "CITY", "CITYLOCATION");

							if (rowIsCrs101) {
								String cpFull = getVal(row, colMap, "CONTROLLINGPERSON", "UBO_FIRST_NAME",
										"CP_FIRST_NAME");
								String cpFirst = cpFull;
								String cpLast = getVal(row, colMap, "UBO_LAST_NAME", "LASTNAME", "CPLASTNAME");
								if (cpLast.isEmpty() && cpFull.contains(" ")) {
									int lastSpace = cpFull.lastIndexOf(" ");
									cpFirst = cpFull.substring(0, lastSpace).trim();
									cpLast = cpFull.substring(lastSpace + 1).trim();
								}

								String cpTax = getVal(row, colMap, "UBOTAX", "UBO_TAX_ID", "CP_TIN");
								String cpCtry = getVal(row, colMap, "UBOCTRY", "UBO_COUNTRY", "CPRESIDENCECOUNTRY");
								if (cpCtry.isEmpty())
									cpCtry = "GB";

								String cpCity = getVal(row, colMap, "UBOCITY", "UBO_CITY", "CPADDRESSCITY");
								String cpAddr = getVal(row, colMap, "UBOADD", "UBO_ADDRESS", "CPADDRESSSTREET");
								Date cpDob = getCellDate(row, colMap, "UBO_DOB", "DOB");

								rows.add(new Object[] { "CRS101", refId, "CRS101", accNum, accNum, curr.toUpperCase(),
										balance, intPaid, isDormant, isClosed, isUndoc, null, entityName, null, null,
										null, null, txCtry.toUpperCase(), tin, addr, city, txCtry.toUpperCase(), null,
										cpFirst, cpLast, cpDob, cpTax, cpAddr, cpCity, cpCtry.toUpperCase(), now,
										effectiveUser });
							} else {
								String crsCode = getVal(row, colMap, "CRS_CODE", "CRSCODE");
								if (crsCode.isEmpty())
									crsCode = "CRS102";

								rows.add(new Object[] { crsCode, refId, crsCode, accNum, accNum, curr.toUpperCase(),
										balance, intPaid, isDormant, isClosed, isUndoc, null, entityName, null, null,
										null, null, txCtry.toUpperCase(), tin, addr, city, txCtry.toUpperCase(), null,
										null, null, null, null, null, null, null, now, effectiveUser });
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
	// =========================================================================
    // RECTIFY DUPLICATE: PROMOTE TO MAIN TABLE & MARK RESOLVED
    // =========================================================================
    @Transactional
    public boolean rectifyDuplicateRecord(
            Long dupId, String accountNumber, String reportType, String taxCountry,
            String tin, String firstName, String lastName, String entityName,
            String address, String city, Double accountBalance, String currencyCode,
            String crsCode, String docRefId) {

        try {
            // 1. Ensure a unique REF_ID for the duplicate record
            String finalRefId = (docRefId != null && !docRefId.trim().isEmpty()) ? docRefId.trim() : ("MU2025" + accountNumber + "_" + dupId);

            // 2. Insert into the main reporting table
            String insertSql = "INSERT INTO BRECON.CRS_REPORT_DATA (" +
                    "  REPORT_TYPE, REF_ID, CRS_CODE, ACCOUNT_NUMBER, IBAN, CURRENCY_CODE, " +
                    "  ACCOUNT_BALANCE, INTEREST_PAID, IS_DORMANT, IS_CLOSED, IS_UNDOCUMENTED, " +
                    "  CUSTOMER_ID, ENTITY_NAME, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, " +
                    "  NATIONALITY, TAX_COUNTRY, TIN, ADDRESS, CITY, COUNTRY, " +
                    "  UBO_CUSTOMER_ID, UBO_FIRST_NAME, UBO_LAST_NAME, UBO_DOB, UBO_TAX_ID, " +
                    "  UBO_ADDRESS, UBO_CITY, UBO_COUNTRY, CREATED_DATE, ENTRY_USER " +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, 0, 'N', 'N', 'N', NULL, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, SYSDATE, 'RECTIFIED')";

            jdbcTemplate.update(insertSql,
                    reportType.toUpperCase(),
                    finalRefId,
                    crsCode,
                    accountNumber,
                    accountNumber,
                    currencyCode.toUpperCase(),
                    accountBalance,
                    entityName,
                    firstName,
                    lastName,
                    taxCountry.toUpperCase(),
                    taxCountry.toUpperCase(),
                    tin,
                    address,
                    city,
                    taxCountry.toUpperCase()
            );

            // 3. Mark the duplicate item as RESOLVED in the queue
            jdbcTemplate.update("UPDATE BRECON.CRS_DUPLICATE_RECORDS SET STATUS = 'RESOLVED' WHERE DUP_ID = ?", dupId);

            return true;
        } catch (Exception e) {
            logger.error("Error rectifying duplicate record ID: {}", dupId, e);
            return false;
        }
    }

		
	// =========================================================================
	// 4. DEDICATED XML TEMPLATE EXPORTERS (INDIVIDUAL, CRS101, CRS102, MASTER)
	// =========================================================================

	public String generateCrsXmlByType(String reportingYear, String reportType) {
		if ("INDIVIDUAL".equalsIgnoreCase(reportType) || "IND".equalsIgnoreCase(reportType)) {
			return generateIndividualCrsXml(reportingYear);
		} else if ("CRS101".equalsIgnoreCase(reportType)) {
			return generateCrs101Xml(reportingYear);
		} else if ("CRS102".equalsIgnoreCase(reportType)) {
			return generateCrs102Xml(reportingYear);
		}
		return generateCrsXml(reportingYear);
	}

	public String generateIndividualCrsXml(String reportingYear) {
		String year = (reportingYear != null && !reportingYear.trim().isEmpty()) ? reportingYear : "2025";
		String sql = "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE (UPPER(REPORT_TYPE) = 'INDIVIDUAL' OR CRS_CODE IS NULL) ORDER BY ACCOUNT_NUMBER ASC";
		List<Map<String, Object>> records = jdbcTemplate.queryForList(sql);
		return buildEnvelope(year, records, "IND");
	}

	public String generateCrs101Xml(String reportingYear) {
		String year = (reportingYear != null && !reportingYear.trim().isEmpty()) ? reportingYear : "2025";
		String sql = "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE UPPER(REPORT_TYPE) = 'CRS101' OR UPPER(CRS_CODE) = 'CRS101' ORDER BY ACCOUNT_NUMBER ASC";
		List<Map<String, Object>> records = jdbcTemplate.queryForList(sql);
		return buildEnvelope(year, records, "CRS101");
	}

	public String generateCrs102Xml(String reportingYear) {
		String year = (reportingYear != null && !reportingYear.trim().isEmpty()) ? reportingYear : "2025";
		String sql = "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE UPPER(REPORT_TYPE) IN ('CRS102', 'CRS103', 'ORGANISATION') OR UPPER(CRS_CODE) IN ('CRS102', 'CRS103') ORDER BY ACCOUNT_NUMBER ASC";
		List<Map<String, Object>> records = jdbcTemplate.queryForList(sql);
		return buildEnvelope(year, records, "CRS102");
	}

	public String generateCrsXml(String reportingYear) {
		String year = (reportingYear != null && !reportingYear.trim().isEmpty()) ? reportingYear : "2025";
		String sql = "SELECT * FROM BRECON.CRS_REPORT_DATA ORDER BY ACCOUNT_NUMBER ASC";
		List<Map<String, Object>> records = jdbcTemplate.queryForList(sql);
		return buildEnvelope(year, records, "ALL");
	}

	// =========================================================================
    // ROBUST SINGLE RECORD XML GENERATOR (HANDLES SINGLE & JOINT ACCOUNTS)
    // =========================================================================
    public String generateSingleCrsXml(String accountOrRefId) {
        if (accountOrRefId == null || accountOrRefId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account Number or Ref ID cannot be empty.");
        }

        // Query by either REF_ID or ACCOUNT_NUMBER
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM BRECON.CRS_REPORT_DATA WHERE REF_ID = ? OR ACCOUNT_NUMBER = ?",
            accountOrRefId.trim(), accountOrRefId.trim()
        );

        if (rows.isEmpty()) {
            // Also check duplicate queue if not yet promoted
            rows = jdbcTemplate.queryForList(
                "SELECT 'INDIVIDUAL' AS REPORT_TYPE, REF_ID, CRS_CODE, ACCOUNT_NUMBER, " +
                "       ACCOUNT_NUMBER AS IBAN, CURRENCY_CODE, ACCOUNT_BALANCE, 0 AS INTEREST_PAID, " +
                "       'N' AS IS_DORMANT, 'N' AS IS_CLOSED, 'N' AS IS_UNDOCUMENTED, " +
                "       HOLDER_NAME AS FIRST_NAME, '' AS LAST_NAME, RESIDENCE_COUNTRY AS TAX_COUNTRY, " +
                "       TIN, '' AS ADDRESS, '' AS CITY, RESIDENCE_COUNTRY AS COUNTRY " +
                "FROM BRECON.CRS_DUPLICATE_RECORDS WHERE ACCOUNT_NUMBER = ?",
                accountOrRefId.trim()
            );
        }

        if (rows.isEmpty()) {
            throw new NoSuchElementException("No CRS record found for: " + accountOrRefId);
        }

        String year = "2025";
        return buildEnvelope(year, rows, "REC");
    }

	private String buildEnvelope(String reportingYear, List<Map<String, Object>> records, String typeTag) {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
		String msgRefId = BANK_COUNTRY + reportingYear + BANK_COUNTRY + BANK_TIN + "_" + typeTag + "_"
				+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

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
		xml.append("            <crs:IN issuedBy=\"").append(BANK_COUNTRY).append("\" INType=\"TIN\">").append(BANK_TIN)
				.append("</crs:IN>\n");
		xml.append("            <crs:Name>").append(escapeXml(BANK_NAME)).append("</crs:Name>\n");
		xml.append("            <crs:Address>\n");
		xml.append("                <cfc:CountryCode>").append(BANK_COUNTRY).append("</cfc:CountryCode>\n");
		xml.append("                <cfc:AddressFree>").append(escapeXml(BANK_ADDRESS)).append("</cfc:AddressFree>\n");
		xml.append("            </crs:Address>\n");
		xml.append("            <crs:DocSpec>\n");
		xml.append("                <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>\n");
		xml.append("                <stf:DocRefId>").append(BANK_COUNTRY).append(reportingYear).append(BANK_COUNTRY)
				.append(BANK_TIN).append("100000</stf:DocRefId>\n");
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

	private void appendAccountReport(StringBuilder xml, Map<String, Object> r, String reportingYear, int seq) {
		String reportType = safeVal(r.get("REPORT_TYPE"));
		String crsCode = safeVal(r.get("CRS_CODE"));
		String accNum = safeVal(r.get("ACCOUNT_NUMBER"));
		String iban = safeVal(r.get("IBAN"));
		String targetAcc = !iban.isEmpty() ? iban : accNum;

		String isClosed = toXmlBoolean(r.get("IS_CLOSED"));
		String isDormant = toXmlBoolean(r.get("IS_DORMANT"));
		String isUndoc = toXmlBoolean(r.get("IS_UNDOCUMENTED"));

		String typeCode = "IND";
		if ("CRS101".equalsIgnoreCase(reportType) || "CRS101".equalsIgnoreCase(crsCode)) {
			typeCode = "CRS101";
		} else if ("CRS102".equalsIgnoreCase(reportType) || "CRS102".equalsIgnoreCase(crsCode)) {
			typeCode = "CRS102";
		}

		// Use the exact database REF_ID if populated, else format with serial sequence
		String docRefId = safeVal(r.get("REF_ID"));
		if (docRefId.isEmpty()) {
			docRefId = BANK_COUNTRY + reportingYear + "-" + typeCode + "-" + String.format("%06d", seq);
		}

		xml.append("            <crs:AccountReport>\n");
		xml.append("                <crs:DocSpec>\n");
		xml.append("                    <stf:DocTypeIndic>OECD1</stf:DocTypeIndic>\n");
		xml.append("                    <stf:DocRefId>").append(docRefId).append("</stf:DocRefId>\n");
		xml.append("                </crs:DocSpec>\n");

		xml.append("                <crs:AccountNumber AcctNumberType=\"OECD602\" ClosedAccount=\"").append(isClosed)
				.append("\" DormantAccount=\"").append(isDormant).append("\" UndocumentedAccount=\"").append(isUndoc)
				.append("\">").append(escapeXml(targetAcc)).append("</crs:AccountNumber>\n");

		xml.append("                <crs:AccountHolder>\n");

		// ---------------------------------------------------------------------
		// INDIVIDUAL (ind.xml compliant)
		// ---------------------------------------------------------------------
		if ("INDIVIDUAL".equalsIgnoreCase(reportType)
				|| (crsCode.isEmpty() && !safeVal(r.get("FIRST_NAME")).isEmpty())) {
			String ctry = safeVal(r.get("TAX_COUNTRY"));
			if (ctry.isEmpty())
				ctry = safeVal(r.get("COUNTRY"));
			if (ctry.isEmpty())
				ctry = "IN";

			String first = safeVal(r.get("FIRST_NAME"));
			String last = safeVal(r.get("LAST_NAME"));
			String tin = safeVal(r.get("TIN"));
			String street = safeVal(r.get("ADDRESS"));
			String city = safeVal(r.get("CITY"));

			xml.append("                    <crs:Individual>\n");
			xml.append("                        <crs:ResCountryCode>").append(ctry).append("</crs:ResCountryCode>\n");
			if (!tin.isEmpty()) {
				xml.append("                        <crs:TIN issuedBy=\"").append(ctry).append("\">")
						.append(escapeXml(tin)).append("</crs:TIN>\n");
			}
			xml.append("                        <crs:Name>\n");
			xml.append("                            <crs:FirstName>").append(escapeXml(first))
					.append("</crs:FirstName>\n");
			xml.append("                            <crs:LastName>").append(escapeXml(last))
					.append("</crs:LastName>\n");
			xml.append("                        </crs:Name>\n");
			xml.append("                        <crs:Address>\n");
			xml.append("                            <cfc:CountryCode>").append(ctry).append("</cfc:CountryCode>\n");
			xml.append("                            <cfc:AddressFix>\n");
			if (!street.isEmpty())
				xml.append("                                <cfc:Street>").append(escapeXml(street))
						.append("</cfc:Street>\n");
			if (!city.isEmpty())
				xml.append("                                <cfc:City>").append(escapeXml(city))
						.append("</cfc:City>\n");
			xml.append("                            </cfc:AddressFix>\n");
			xml.append("                        </crs:Address>\n");

			if (r.get("DATE_OF_BIRTH") != null) {
				String dobStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) r.get("DATE_OF_BIRTH"));
				xml.append("                        <crs:BirthInfo>\n");
				xml.append("                            <crs:BirthDate>").append(dobStr).append("</crs:BirthDate>\n");
				if (!city.isEmpty())
					xml.append("                            <crs:City>").append(escapeXml(city))
							.append("</crs:City>\n");
				xml.append("                            <crs:CountryInfo>\n");
				xml.append("                                <crs:CountryCode>").append(ctry)
						.append("</crs:CountryCode>\n");
				xml.append("                            </crs:CountryInfo>\n");
				xml.append("                        </crs:BirthInfo>\n");
			}
			xml.append("                    </crs:Individual>\n");
		}
		// ---------------------------------------------------------------------
		// ORGANISATION (CRS101 / CRS102)
		// ---------------------------------------------------------------------
		else {
			String orgName = safeVal(r.get("ENTITY_NAME"));
			String ctry = safeVal(r.get("TAX_COUNTRY"));
			if (ctry.isEmpty())
				ctry = safeVal(r.get("COUNTRY"));
			if (ctry.isEmpty())
				ctry = "MU";

			String tin = safeVal(r.get("TIN"));
			String street = safeVal(r.get("ADDRESS"));
			String city = safeVal(r.get("CITY"));

			xml.append("                    <crs:Organisation>\n");
			xml.append("                        <crs:ResCountryCode>").append(ctry).append("</crs:ResCountryCode>\n");
			if (!tin.isEmpty()) {
				xml.append("                        <crs:TIN issuedBy=\"").append(ctry).append("\">")
						.append(escapeXml(tin)).append("</crs:TIN>\n");
			}
			xml.append("                        <crs:Name>").append(escapeXml(orgName)).append("</crs:Name>\n");
			xml.append("                        <crs:Address legalAddressType=\"OECD304\">\n");
			xml.append("                            <cfc:CountryCode>").append(ctry).append("</cfc:CountryCode>\n");
			xml.append("                            <cfc:AddressFix>\n");
			if (!street.isEmpty())
				xml.append("                                <cfc:Street>").append(escapeXml(street))
						.append("</cfc:Street>\n");
			if (!city.isEmpty())
				xml.append("                                <cfc:City>").append(escapeXml(city))
						.append("</cfc:City>\n");
			xml.append("                            </cfc:AddressFix>\n");
			xml.append("                        </crs:Address>\n");
			xml.append("                    </crs:Organisation>\n");

			String crsHolderCode = !crsCode.isEmpty() ? crsCode
					: ("CRS101".equalsIgnoreCase(reportType) ? "CRS101" : "CRS102");
			xml.append("                    <crs:AcctHolderType>").append(crsHolderCode)
					.append("</crs:AcctHolderType>\n");
		}
		xml.append("                </crs:AccountHolder>\n");

		// ---------------------------------------------------------------------
		// CONTROLLING PERSON (CRS101)
		// ---------------------------------------------------------------------
		if ("CRS101".equalsIgnoreCase(reportType) || "CRS101".equalsIgnoreCase(crsCode)) {
			String cpFirst = safeVal(r.get("UBO_FIRST_NAME"));
			String cpLast = safeVal(r.get("UBO_LAST_NAME"));
			String cpCtry = safeVal(r.get("UBO_COUNTRY"));
			if (cpCtry.isEmpty())
				cpCtry = "GB";

			String cpTin = safeVal(r.get("UBO_TAX_ID"));
			String cpStreet = safeVal(r.get("UBO_ADDRESS"));
			String cpCity = safeVal(r.get("UBO_CITY"));

			xml.append("                <crs:ControllingPerson>\n");
			xml.append("                    <crs:Individual>\n");
			xml.append("                        <crs:ResCountryCode>").append(cpCtry).append("</crs:ResCountryCode>\n");
			if (!cpTin.isEmpty()) {
				xml.append("                        <crs:TIN issuedBy=\"").append(cpCtry).append("\">")
						.append(escapeXml(cpTin)).append("</crs:TIN>\n");
			}
			xml.append("                        <crs:Name>\n");
			xml.append("                            <crs:FirstName>").append(escapeXml(cpFirst))
					.append("</crs:FirstName>\n");
			xml.append("                            <crs:LastName>").append(escapeXml(cpLast))
					.append("</crs:LastName>\n");
			xml.append("                        </crs:Name>\n");
			xml.append("                        <crs:Address legalAddressType=\"OECD304\">\n");
			xml.append("                            <cfc:CountryCode>").append(cpCtry).append("</cfc:CountryCode>\n");
			xml.append("                            <cfc:AddressFix>\n");
			if (!cpStreet.isEmpty())
				xml.append("                                <cfc:Street>").append(escapeXml(cpStreet))
						.append("</cfc:Street>\n");
			if (!cpCity.isEmpty())
				xml.append("                                <cfc:City>").append(escapeXml(cpCity))
						.append("</cfc:City>\n");
			xml.append("                            </cfc:AddressFix>\n");
			xml.append("                        </crs:Address>\n");

			if (r.get("UBO_DOB") != null) {
				String cpDobStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) r.get("UBO_DOB"));
				xml.append("                        <crs:BirthInfo>\n");
				xml.append("                            <crs:BirthDate>").append(cpDobStr).append("</crs:BirthDate>\n");
				if (!cpCity.isEmpty())
					xml.append("                            <crs:City>").append(escapeXml(cpCity))
							.append("</crs:City>\n");
				xml.append("                            <crs:CountryInfo>\n");
				xml.append("                                <crs:CountryCode>").append(cpCtry)
						.append("</crs:CountryCode>\n");
				xml.append("                            </crs:CountryInfo>\n");
				xml.append("                        </crs:BirthInfo>\n");
			}
			xml.append("                    </crs:Individual>\n");
			xml.append("                    <crs:CtrlgPersonType>CRS801</crs:CtrlgPersonType>\n");
			xml.append("                </crs:ControllingPerson>\n");
		}

		// Financials (Balance & Payment)
		double bal = r.get("ACCOUNT_BALANCE") instanceof Number ? ((Number) r.get("ACCOUNT_BALANCE")).doubleValue()
				: 0.0;
		String curr = safeVal(r.get("CURRENCY_CODE"));
		if (curr.isEmpty())
			curr = "MUR";

		xml.append("                <crs:AccountBalance currCode=\"").append(curr).append("\">")
				.append(String.format(Locale.US, "%.2f", bal)).append("</crs:AccountBalance>\n");

		double pmt = r.get("INTEREST_PAID") instanceof Number ? ((Number) r.get("INTEREST_PAID")).doubleValue() : 0.0;
		xml.append("                <crs:Payment>\n");
		xml.append("                    <crs:Type>CRS502</crs:Type>\n");
		xml.append("                    <crs:PaymentAmnt currCode=\"").append(curr).append("\">")
				.append(String.format(Locale.US, "%.2f", pmt)).append("</crs:PaymentAmnt>\n");
		xml.append("                </crs:Payment>\n");

		xml.append("            </crs:AccountReport>\n");
	}

	// =========================================================================
	// 5. PARSING UTILITIES
	// =========================================================================
	private String cleanCol(String name) {
		return name.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
	}

	private boolean hasKey(Map<String, Integer> colMap, String keyword) {
		String cleanKey = cleanCol(keyword);
		for (String col : colMap.keySet()) {
			if (col.contains(cleanKey))
				return true;
		}
		return false;
	}

	private String getCsvVal(String[] tokens, Map<String, Integer> colMap, String... keys) {
		for (String k : keys) {
			Integer idx = colMap.get(cleanCol(k));
			if (idx != null && idx >= 0 && idx < tokens.length) {
				String val = tokens[idx].trim().replaceAll("^\"|\"$", "");
				if (!val.isEmpty())
					return val;
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
							if (cell.getCellType() == 0) { // Numeric cell
								double val = cell.getNumericCellValue();
								return String.format(Locale.US, "%.0f", val).trim();
							}
						} catch (Exception ignored) {
						}

						String s = dataFormatter.formatCellValue(cell).trim();
						if (!s.isEmpty())
							return s;
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
								if (!s.isEmpty())
									return Double.parseDouble(s);
							} catch (Exception ignored) {
							}
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
						} catch (Exception ignored) {
						}

						try {
							String s = dataFormatter.formatCellValue(cell).trim();
							if (!s.isEmpty()) {
								return new SimpleDateFormat("yyyy-MM-dd").parse(s);
							}
						} catch (Exception ignored) {
						}
					}
				}
			}
		}
		return null;
	}

	private String getCharBoolean(Row row, Map<String, Integer> colMap, String... keys) {
		for (String k : keys) {
			String cleanKey = cleanCol(k);
			for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
				if (entry.getKey().contains(cleanKey)) {
					Cell cell = row.getCell(entry.getValue(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
					if (cell != null) {
						try {
							return cell.getBooleanCellValue() ? "Y" : "N";
						} catch (Exception e) {
							String s = dataFormatter.formatCellValue(cell).trim().toUpperCase();
							if ("TRUE".equals(s) || "Y".equals(s) || "YES".equals(s) || "1".equals(s)) {
								return "Y";
							}
						}
					}
				}
			}
		}
		return "N";
	}

	private String toXmlBoolean(Object val) {
		if (val == null)
			return "false";
		String s = String.valueOf(val).trim().toUpperCase();
		return ("Y".equals(s) || "YES".equals(s) || "TRUE".equals(s) || "1".equals(s)) ? "true" : "false";
	}

	private String getCellString(Cell cell) {
		if (cell == null)
			return "";
		try {
			return dataFormatter.formatCellValue(cell).trim();
		} catch (Exception ignored) {
			return "";
		}
	}

	private String safeVal(Object obj) {
		if (obj == null)
			return "";
		String s = String.valueOf(obj).trim();
		return "null".equalsIgnoreCase(s) ? "" : s;
	}

	private String escapeXml(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
				"&apos;");
	}
	
}