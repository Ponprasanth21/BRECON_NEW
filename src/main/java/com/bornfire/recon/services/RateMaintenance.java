package com.bornfire.recon.services;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.*;

@Service
@ConfigurationProperties("output")
@Transactional
public class RateMaintenance {
	private static final Logger logger = LoggerFactory.getLogger(RateMaintenance.class);

	@Autowired
	RATE_MAIN_REPO rate_main_Repo;

	@Autowired
	RATE_MAINMOD_REPO rate_mainmod_Repo;

	@Autowired
	private SequenceGenerator sequence;

	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;

	@Autowired
	AuidtConfigure audit;

	public void saveRates(List<RATE_MAINMOD_ENTITY> rates, String user, String username) {
		String auditRefNo = sequence.generateRequestUUId();

		for (RATE_MAINMOD_ENTITY rate : rates) {
			// Populate rate entity
			rate.setEntry_time(new Date());
			rate.setDel_flg("N");
			rate.setEntity_flg("N");
			rate.setVerify_flg("N");
			rate.setEntry_user(user);
			rate.setModify_user(user);
			rate.setModify_time(new Date());
			rate.setModify_flg("N");
			rate.setUpload_flg("N");
			rate.setUnique_id(rate_mainmod_Repo.getNextAuditSrlNo());

			audit.insertAudit(user, username, "NEW RATE ADD", "RATE SAVED SUCCESSFULLY", "RATE_MAINTENANCE",
					"RATE MAINTENANCE");
			// Save
			rate_mainmod_Repo.save(rate);
		}
	}

	public void submitVersions(List<RATE_MAINMOD_ENTITY> versions, String user, String username) {
		String auditRefNo = sequence.generateRequestUUId();

		for (RATE_MAINMOD_ENTITY v : versions) {
			// 🔍 Find current max version for Fixed + Variable Currency combination
			BigDecimal maxVer = rate_mainmod_Repo.findMaxVersionByCurrencyPair(v.getFxd_crncy(), v.getVar_crncy());
			if (maxVer == null) {
				maxVer = BigDecimal.ZERO;
			}
			v.setVersion(maxVer.add(BigDecimal.ONE));

			// Populate entity fields
			v.setModify_flg("Y");
			v.setDel_flg("N");
			v.setEntity_flg("N");
			v.setVerify_flg("N");
			v.setEntry_user(user);
			v.setEntry_time(new Date());
			v.setModify_user(user);
			v.setModify_time(new Date());
			v.setUpload_flg("N");
			v.setUnique_id(rate_mainmod_Repo.getNextAuditSrlNo());

			audit.insertAudit(user, username, "RATE VERSION ADD", "RATE VERSION SAVED SUCCESSFULLY", "RATE_MAINTENANCE",
					"RATE MAINTENANCE");
			// Save
			rate_mainmod_Repo.save(v);
		}
	}

	public void submitLists(List<RATE_MAINMOD_ENTITY> listEntries, String user, String username) {
		String auditRefNo = sequence.generateRequestUUId();

		// Get the current max version
		BigDecimal version = rate_main_Repo.findMaxversion();
		if (version == null) {
			version = BigDecimal.ZERO;
		}

		for (RATE_MAINMOD_ENTITY entry : listEntries) {
			// Increment version for each entry
			version = version.add(BigDecimal.ONE);
			entry.setVersion(version);

			// Populate entity fields
			entry.setDel_flg("N");
			entry.setEntity_flg("N");
			entry.setVerify_flg("N");
			entry.setEntry_user(user);
			entry.setEntry_time(new Date());
			entry.setModify_user(user);
			entry.setModify_time(new Date());
			entry.setModify_flg("N");
			entry.setUpload_flg("N");
			entry.setUnique_id(rate_mainmod_Repo.getNextAuditSrlNo());

			audit.insertAudit(user, username, "RATE LIST ADD", "RATE LIST ENTRIES SAVED SUCCESSFULLY",
					"RATE_MAINTENANCE", "RATE MAINTENANCE");
			// Save
			rate_mainmod_Repo.save(entry);
		}
	}

	private static final List<String> HEADERS = Arrays.asList("rate_code_desc", "fxd_crncy", "var_crncy", "rate","rate_date", "eff_date");

	public Map<String, Object> uploadRateExcel(MultipartFile file, String user, String username, boolean overwrite)
			throws Exception {

		Map<String, Object> response = new HashMap<>();

		try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();

			if (!rows.hasNext()) {
				response.put("status", "error");
				response.put("message", "Excel file is empty.");
				return response;
			}

			Row headerRow = rows.next();
			if (!validateHeaders(headerRow)) {
				response.put("status", "error");
				response.put("message", "Invalid Excel headers. Expected headers: " + HEADERS);
				return response;
			}

			BigDecimal version = rate_main_Repo.findMaxversion();
			if (version == null)
				version = BigDecimal.ZERO;

			List<RATE_MAIN_ENTITY> entries = new ArrayList<>();
			List<String> duplicateRefs = new ArrayList<>();

			while (rows.hasNext()) {
				Row row = rows.next();
				if (row == null)
					continue;

				String fxd = getCellString(row, 1);
				String var = getCellString(row, 2);
				String date = getCellString(row, 5);
				
				Date effdate = parseDate(date);
				DateFormat dateFormat = new SimpleDateFormat("DD-MM-YYYY");
				String effdateformat = dateFormat.format(effdate);
				logger.info(effdateformat);
				if (fxd == null || var == null)
					continue;

				boolean exists = rate_main_Repo.existsByFxdAndVar(fxd, var , effdateformat) == 1;
				if (exists) {
					duplicateRefs.add(fxd + "-" + var);
					if (!overwrite)
						continue;
					rate_main_Repo.deleteByFxdAndVar(fxd, var,effdateformat);
				}

				RATE_MAIN_ENTITY entry = new RATE_MAIN_ENTITY();
				version = version.add(BigDecimal.ONE);
				entry.setVersion(version);
				entry.setRate_code_desc(getCellString(row, 0));
				entry.setFxd_crncy(fxd);
				entry.setVar_crncy(var);
				entry.setRate(getCellString(row, 3));
				entry.setRate_date(getCellDate(row, 4));
				entry.setEff_date(getCellDate(row, 5));

				// Default values
				entry.setDel_flg("N");
				entry.setEntity_flg("Y");
				entry.setModify_flg("N");
				entry.setUpload_flg("Y");
				entry.setVerify_flg("Y");
				entry.setEntry_user(user);
				entry.setEntry_time(new Date());
				entry.setModify_user(user);
				entry.setModify_time(new Date());
				entry.setUnique_id(rate_main_Repo.getNextAuditSrlNo());

				entries.add(entry);
				ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
				logger.info("Now: " + now);
			}

			if (!duplicateRefs.isEmpty() && !overwrite) {
				response.put("status", "duplicate");
				response.put("addl", duplicateRefs);
				return response;
			}

			if (!entries.isEmpty()) {
				rate_main_Repo.saveAll(entries);
			}

			audit.insertAudit(user, username, "UPLOAD", "UPLOADED AND SAVED SUCCESSFULLY", "RATE_MAINTENANCE",
					"RATE MAINTENANCE");

			response.put("status", "success");
			response.put("message", "Uploaded and saved successfully!");
			return response;

		} catch (Exception e) {
			e.printStackTrace();
			response.put("status", "error");
			response.put("message", "Upload failed. Please try again.");
			return response;
		}
	}

	// ✅ Case-insensitive header validation
	private boolean validateHeaders(Row headerRow) {
		if (headerRow == null)
			return false;

		for (int i = 0; i < HEADERS.size(); i++) {
			Cell cell = headerRow.getCell(i);
			if (cell == null)
				return false;

			String actual = cell.getStringCellValue().trim();
			String expected = HEADERS.get(i);

			if (!actual.equalsIgnoreCase(expected)) {
				return false; // mismatch found
			}
		}
		return true;
	}

	public Map<String, Object> uploadRatePex(MultipartFile file, String user, String username, boolean overwrite) {
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> dataList = new ArrayList<>();
		List<RATE_MAIN_ENTITY> entries = new ArrayList<>();
		List<String> duplicateRefs = new ArrayList<>();
		String effDate = null;
		BigDecimal version = BigDecimal.ZERO;

		try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) {
				if (row == null)
					continue;

				// 1️⃣ Get effective date
				if (row.getCell(0) != null && row.getCell(0).toString().matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
					effDate = row.getCell(0).toString().trim();
					continue;
				}

				// 2️⃣ Skip empty and footer rows
				if (row.getCell(0) == null || row.getCell(0).toString().trim().isEmpty())
					continue;
				String firstCell = row.getCell(0).toString().trim();
				if (firstCell.startsWith("Prepared By") || firstCell.startsWith("Authorized By"))
					continue;

				// 3️⃣ Process three conversion blocks per row
				for (int i = 0; i <= 4; i += 2) {
					Cell descCell = row.getCell(i);
					Cell rateCell = row.getCell(i + 1);

					if (descCell != null && rateCell != null && !descCell.toString().trim().isEmpty()) {
						String desc = descCell.toString().trim();
						String rateDesc, fxd, var;

						if (i == 0) { // OTHER CURRENCY TO MUR
							String[] mainParts = desc.split(" ", 2);
							if (mainParts.length < 2)
								continue;
							rateDesc = mainParts[0].trim();
							String[] curParts = mainParts[1].split("-");
							if (curParts.length < 2)
								continue;
							fxd = curParts[0].trim();
							var = curParts[1].trim();
						} else { // MUR TO OTHER and USD TO OTHER
							String[] curParts = desc.split("-");
							if (curParts.length < 2)
								continue;
							fxd = curParts[0].trim(); // e.g., MUR or USD
							String[] varAndRateDesc = curParts[1].trim().split(" "); // e.g., "AUD 036"
							if (varAndRateDesc.length < 2)
								continue;
							var = varAndRateDesc[0].trim(); // AUD
							rateDesc = varAndRateDesc[1].trim(); // 036
						}

						Double rateVal = null;
						try {
							rateVal = Double.parseDouble(rateCell.toString().trim());
						} catch (NumberFormatException e) {
						}

						// Map for frontend
						Map<String, Object> rowMap = new HashMap<>();
						rowMap.put("rate_desc", rateDesc);
						rowMap.put("fxd_crncy", fxd);
						rowMap.put("var_crncy", var);
						rowMap.put("rate", rateVal);
						rowMap.put("rate_date", effDate);
						rowMap.put("eff_date", effDate);
						dataList.add(rowMap);
						
						Date edate = parseDate(effDate);
						DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
						String effdateformat = dateFormat.format(edate);
						logger.info(effDate+" "+edate +" "+effdateformat);
						ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
						logger.info("Now: " + now);

						// ✅ Duplicate check
						boolean exists = rate_main_Repo.existsByFxdAndVar(fxd, var , effdateformat) == 1;
						if (exists) {
							duplicateRefs.add(fxd + "-" + var);
							if (!overwrite) {
								// Skip this record (user must confirm overwrite)
								continue;
							} else {
								// Delete old record before inserting new
								rate_main_Repo.deleteByFxdAndVar(fxd, var,effdateformat);
							}
						}

						// Save entity
						RATE_MAIN_ENTITY entry = new RATE_MAIN_ENTITY();
						version = version.add(BigDecimal.ONE);
						entry.setVersion(version);
						entry.setRate_code_desc(rateDesc);
						entry.setFxd_crncy(fxd);
						entry.setVar_crncy(var);
						entry.setRate(rateVal != null ? rateVal.toString() : null);
						entry.setRate_date(parseDate(effDate));
						entry.setEff_date(parseDate(effDate));
						entry.setDel_flg("N");
						entry.setEntity_flg("Y");
						entry.setModify_flg("N");
						entry.setUpload_flg("Y");
						entry.setVerify_flg("Y");
						entry.setEntry_user(user);
						entry.setEntry_time(new Date());
						entry.setModify_user(user);
						entry.setModify_time(new Date());
						entry.setUnique_id(rate_main_Repo.getNextAuditSrlNo());

						entries.add(entry);
					}
				}
			}

			// 6️⃣ Save all entities in batch
			if (!entries.isEmpty()) {
				rate_main_Repo.saveAll(entries);
			}

			// 7️⃣ Prepare response
			if (!duplicateRefs.isEmpty() && !overwrite) {
				response.put("status", "duplicate");
				response.put("addl", duplicateRefs);
			} else {
				response.put("status", "success");
				response.put("data", dataList);
				response.put("message", "Uploaded and saved successfully");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.put("status", "error");
			response.put("message", e.getMessage());
		}

		return response;
	}

	// Helper method to parse date from dd.MM.yyyy string
	private Date parseDate(String dateStr) {
		try {
			return new SimpleDateFormat("dd.MM.yyyy").parse(dateStr);
		} catch (Exception e) {
			return null;
		}
	}

	// Get string safely
	private String getCellString(Row row, int index) {
		if (row.getCell(index) == null)
			return "";
		return row.getCell(index).toString().trim();
	}

	// Parse date safely
	private Date getCellDate(Row row, int index) {
		try {
			if (row.getCell(index) == null)
				return null;

			String value = row.getCell(index).toString().trim();
			if (value.isEmpty())
				return null;

			// Check if numeric (Excel numeric date)
			if (value.matches("\\d+(\\.\\d+)?")) {
				double numericValue = Double.parseDouble(value);
				long milliseconds = (long) ((numericValue - 25569) * 86400 * 1000); // Excel base date: Jan 1, 1900
				return new Date(milliseconds);
			}

			// Try multiple string formats
			String[] patterns = { "dd-MM-yyyy", "dd.MM.yyyy" };
			for (String pattern : patterns) {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
					LocalDate localDate = LocalDate.parse(value, formatter);
					return java.sql.Date.valueOf(localDate);
				} catch (Exception ignored) {
					// try next format
				}
			}

			// If no format matches
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String verifyRate(String uniqueId, String loginUserId, String username) {
		String msg = "";
		String auditRefNo = sequence.generateRequestUUId();
		try {
			// === VERIFY ===
			Optional<RATE_MAINMOD_ENTITY> rateOpt = rate_mainmod_Repo.findById(uniqueId);

			if (rateOpt.isPresent()) {
				RATE_MAINMOD_ENTITY tempRate = rateOpt.get();
				RATE_MAIN_ENTITY verifiedRate = new RATE_MAIN_ENTITY();
				// Copy properties from MOD → MAIN
				BeanUtils.copyProperties(tempRate, verifiedRate);
				// ❌ Prevent same user from verifying their own entry
				if (loginUserId != null && loginUserId.equals(tempRate.getEntry_user())) {
					return "Same user cannot verify their own entry.";
				}

				// ✅ Update verification fields
				verifiedRate.setVerify_user(loginUserId);
				verifiedRate.setVerify_time(new Date());
				verifiedRate.setVerify_flg("Y");
				verifiedRate.setEntity_flg("Y");
				// Save into MAIN
				rate_main_Repo.save(verifiedRate);
				// Delete from MOD
				rate_mainmod_Repo.delete(tempRate);

				audit.insertAudit(loginUserId, username, "VERIFY", "RATE VERIFIED SUCCESSFULLY", "RATE_MAINTENANCE",
						"RATE MAINTENANCE");
				msg = "Rate Verified Successfully!";
			} else {
				msg = "Rate Code not found!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg = "Error while verifying rate: " + e.getMessage();
		}
		return msg;
	}
}