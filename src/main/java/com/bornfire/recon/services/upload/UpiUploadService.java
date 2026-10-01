package com.bornfire.recon.services.upload;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.RATE_MAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIMAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_REPO;
import com.bornfire.recon.services.AuidtConfigure;
import com.bornfire.recon.services.ReportServices;

@Service
@ConfigurationProperties("output")
@Transactional
public class UpiUploadService {

	private static final Logger logger = LoggerFactory.getLogger(UpiUploadService.class);

	@Autowired
	SequenceGenerator sequence;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	RECON_UPI_SOURCE_REPO RECON_UPI_SOURCE_REPO;

	@Autowired
	DateParser DateParser;

	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;

	@Autowired
	RECON_UPI_DESTINATION_REPO RECON_UPI_DESTINATION_REPO;

	@Autowired
	RECON_UPIMAIN_REPO RECON_UPIMAIN_REPO;

	@Autowired
	AuidtConfigure audit;

	@Autowired
	RECON_UPIREPORT_MUR_REPO RECON_UPIREPORT_MUR_REPO;

	@Autowired
	RECON_UPIREPORT_USD_REPO RECON_UPIREPORT_USD_REPO;

	@Autowired
	RATE_MAIN_REPO RATE_MAIN_REPO;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int delteDest(List<String> duplicateArns) {
		return RECON_UPI_DESTINATION_REPO.delterefnum(duplicateArns);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int delteSOURCE(List<String> duplicateAddl) {
		return RECON_UPI_SOURCE_REPO.delteaddl(duplicateAddl);
	}

	private static final List<List<String>> SOURCE_HEADERS = Arrays.asList(
			// Format with CTRY and empty column after TRN_DESC
			Arrays.asList("TRN_DT", "VALUE_DT", "REF", "CTRY", "TRN_DESC", "", "ADDL_TEXT", "AC_CCY", "DR_MOV",
					"CR_MOV"),

			// Format without CTRY
			Arrays.asList("TRN_DT", "VALUE_DT", "REF", "", "TRN_DESC", "", "ADDL_TEXT", "AC_CCY", "DR_MOV", "CR_MOV"));

	private static final List<String> DESTINATION_HEADERS = Arrays.asList("ROWNUM", "STTL_DATE", "ARN", "MCC",
			"AUTH_AMNT", "ACCT_AMNT", "AUTH_CURRENCY", "TRANSACTION_DATE", "TRANSACTION_AMOUNT", "TRANSACTION_CURRENCY",
			"TRANSACTION_TYPE", "CBS_ACC_NO", "CARD_NUMBER", "CARD_TYPE", "MERCHANT_DESC", "TRN_COUNTRY",
			"APPROVAL_CODE", "RECONCILE_AMOUNT", "RECONCILE_CUR", "INTERCHANGE_FEES", "SERVICE_FEE", "SERVICE_IND",
			"I_FEE_IND", "REFNUM", "DR_CR", "MARKUP_VALUE", "MARKUP_CURRENCY");

	@Transactional
	public Map<String, Object> SaveUpiSourceFiles(String fileInput, MultipartFile[] files, String userID,
			String USERNAME, boolean overwrite, String fromDate) throws SQLException, IOException {

		Map<String, Object> resultMap = new LinkedHashMap<>();

		// Check rate maintenance first
		int rateCount = RATE_MAIN_REPO.getRateCount(fromDate);
		if (rateCount == 0) {
			resultMap.put("status", "norate");
			resultMap.put("message", "Upload failed — rate maintenance required for the selected date.");
			return resultMap;
		}

		int totalSuccess = 0, totalFailure = 0, totalProcessed = 0, download = 0, reversibleCount = 0;
		int ignoredExcelCount = 0, ignoredDbCount = 0,duplicateExcelCount = 0;

		for (MultipartFile file : files) {
			if (file == null || file.isEmpty())
				continue;

			String fileName = file.getOriginalFilename();
			if (fileName == null)
				continue;

			String fileExt = "";
			int i = fileName.lastIndexOf('.');
			if (i > 0)
				fileExt = fileName.substring(i + 1);
			if (!fileExt.equalsIgnoreCase("xlsx") && !fileExt.equalsIgnoreCase("xls")) {
				totalFailure++;
				continue;
			}

			int successCount = 0, failureCount = 0;
			
			DataFormatter formatter = new DataFormatter();


			// Fixed column indices (0-based)
			final int IDX_TRN_DT = 0;
			final int IDX_VALUE_DT = 1;
			final int IDX_REF = 2;
			final int IDX_CTRY = 3; // even if header missing, still exists
			final int IDX_TRN_DESC = 4;
			final int IDX_TRN_TYPE = 5;
			final int IDX_ADDL_TEXT = 6;
			final int IDX_AC_CCY = 7;
			final int IDX_DR_MOV = 8;
			final int IDX_CR_MOV = 9;
			
			try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

				Sheet sheet = workbook.getSheetAt(0);
				Row headerRow = sheet.getRow(0);

				if (headerRow == null) {
				    resultMap.put("status", "error");
				    resultMap.put("message", "Missing header row in Excel file");
				    return resultMap;
				}

				List<String> actualHeaders = new ArrayList<>();

				for (int col = 0; col <= IDX_CR_MOV; col++) {
				    Cell cell = headerRow.getCell(col);
				    String header = (cell == null)
				            ? ""
				            : formatter.formatCellValue(cell).trim();
				    actualHeaders.add(header);
				}

				boolean headerMatched = false;
				for (List<String> expected : SOURCE_HEADERS) {
				    if (expected.equals(actualHeaders)) {
				        headerMatched = true;
				        break;
				    }
				}

				if (!headerMatched) {
				    resultMap.put("status", "error");
				    resultMap.put(
				        "message",
				        "Invalid Excel header format. Expected one of: " + SOURCE_HEADERS
				    );
				    return resultMap;
				}


				// Parse Excel into mapList
				List<HashMap<Integer, String>> mapList = new ArrayList<>();
				for (Row row : sheet) {
					if (row == null || row.getRowNum() == 0)
						continue; // skip header
					if (isRowEmpty(row))
						continue;

					HashMap<Integer, String> map = new HashMap<>();
					for (int j = 0; j <= IDX_CR_MOV; j++) {
						Cell cell = row.getCell(j);
						String cellValue = (cell == null) ? "" : formatter.formatCellValue(cell).trim();
						map.put(j, cellValue);
					}
					mapList.add(map);
				}

				// Group by Addl3 and calculate sums
				Map<String, BigDecimal> addl3Sums = new HashMap<>();
				for (HashMap<Integer, String> item : mapList) {
					String addlText = item.get(IDX_ADDL_TEXT);
					if (addlText != null && !addlText.trim().isEmpty()) {
						String[] parts = addlText.trim().split("\\s+");
						String addl3 = parts.length > 2 ? parts[2] : null;
						if (addl3 != null) {
							BigDecimal crVal = parseBigDecimal(item.get(IDX_CR_MOV));
							addl3Sums.merge(addl3, crVal, BigDecimal::add);
						}
					}
				}

				// Find reversible accounts
				Set<String> reversibleAddl3 = new HashSet<>();
				for (Map.Entry<String, BigDecimal> entry : addl3Sums.entrySet()) {
					if (entry.getValue().compareTo(BigDecimal.ZERO) == 0) {
						reversibleCount++;
						reversibleAddl3.add(entry.getKey());
					}
				}

				// Check DB duplicates
				List<String> duplicateAddl = new ArrayList<>();
				for (HashMap<Integer, String> item : mapList) {
					String addlText = item.get(IDX_ADDL_TEXT);
					if (addlText != null && !addlText.trim().isEmpty()) {
						String[] parts = addlText.trim().split("\\s+");
						String addl3 = parts.length > 2 ? parts[2] : null;
						String crMovStr = item.get(IDX_CR_MOV);
						BigDecimal amt = parseBigDecimal(crMovStr);
						if (addl3 != null) {
							RECON_UPI_SOURCE_ENTITY checkAdl = RECON_UPI_SOURCE_REPO.findByRefAndAmount(addl3,amt);
							if (checkAdl != null)
								duplicateAddl.add(addl3);
						}
					}
				}

				if (!duplicateAddl.isEmpty() && !overwrite) {
					resultMap.put("status", "duplicate");
					resultMap.put("addl", duplicateAddl);
					return resultMap;
				} else if (!duplicateAddl.isEmpty() && overwrite) {
					delteSOURCE(duplicateAddl);
				}

				Set<String> processedRefCrMovInFile = new HashSet<>();
				// Insert rows
				for (HashMap<Integer, String> item : mapList) {
					try {
						String addlText = item.get(IDX_ADDL_TEXT);
						String[] parts = addlText != null ? addlText.trim().split("\\s+") : new String[0];
						String addl3 = parts.length > 2 ? parts[2] : null;
						
						String ref = item.get(IDX_REF);          // REFNUM
						String crMovStr = item.get(IDX_CR_MOV);  // CR_MOV

						if (ref != null && !ref.trim().isEmpty()
						        && crMovStr != null && !crMovStr.trim().isEmpty()) {

						    BigDecimal crMov = parseBigDecimal(crMovStr);

						    // Composite key: REFNUM + CR_MOV
						    String compositeKey =
						            ref.trim() + "|" + crMov.stripTrailingZeros().toPlainString();

						    // 🔁 Duplicate inside Excel ONLY when BOTH match
						    if (!processedRefCrMovInFile.add(compositeKey)) {
						        duplicateExcelCount++;
						        ignoredExcelCount++;
						        continue; // skip duplicate row
						    }
						}

						RECON_UPI_SOURCE_ENTITY up = new RECON_UPI_SOURCE_ENTITY();
						up.setTrn_dt(DateParser.parseDateSafe(item.get(IDX_TRN_DT)));
						up.setValue_dt(DateParser.parseDateSafe(item.get(IDX_VALUE_DT)));
						up.setRef(item.get(IDX_REF));
						up.setCtry(item.get(IDX_CTRY));
						up.setTrn_desc(item.get(IDX_TRN_DESC));
						up.setTrn_type(item.get(IDX_TRN_TYPE));
						setAddlFields(up, item.get(IDX_ADDL_TEXT));
						up.setAc_ccy(item.get(IDX_AC_CCY));
						up.setDr_mov(parseBigDecimal(item.get(IDX_DR_MOV)));
						up.setCr_mov(parseBigDecimal(item.get(IDX_CR_MOV)));

						// system values
						up.setEntity_flg("Y");
						up.setAuth_flg("N");
						up.setModify_flg("N");
						up.setDel_flg("N");
						up.setRecon_flg("N");

						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
						Date parsedDate = sdf.parse(fromDate);
						up.setRecon_tran_date(new Date());
						up.setRecon_process_date(parsedDate);
						up.setRecon_type("AUTO");

						up.setEntry_user(userID);
						up.setEntry_time(new Date());
						up.setModify_user(userID);
						up.setModify_time(new Date());
						up.setAuth_user(userID);
						up.setAuth_time(new Date());

						up.setSrl_no(RECON_UPI_SOURCE_REPO.getSeq());

						RECON_UPI_SOURCE_REPO.save(up);
						successCount++;

					} catch (Exception rowEx) {
						failureCount++;
						rowEx.printStackTrace();
					} finally {
						totalProcessed++;
					}
				}

				audit.insertAudit(userID, USERNAME, "FILE UPLOAD", "UPI DEBIT SOURCE FILE UPLOAD SUCCESSFULLY",
						"BRECON_UPI_SOURCE_TABLE", "UPLOAD");

				totalSuccess += successCount;
				totalFailure += failureCount;

				System.out.println(
						"File processed: " + fileName + " | Success: " + successCount + " | Failed: " + failureCount);

			} catch (Exception e) {
				e.printStackTrace();
				totalFailure++;
			}
		}

		// Final processing queries
		RECON_UPI_DESTINATION_REPO.flush();
		RECON_UPIMAIN_REPO.insertValueReconTb(USERNAME, fromDate);
		entityManager.flush();
		RECON_UPI_DESTINATION_REPO.UpdateValueDestTb(fromDate);
		entityManager.flush();
		RECON_UPI_DESTINATION_REPO.UpdateValueSourTb(fromDate);
		entityManager.flush();
		entityManager.clear();

		boolean hasY = RECON_UPI_SOURCE_REPO.existsAnyY() > 0;

		if (!hasY) {
			System.out.println(">>> NOT GENERATE RECON PROCEDURE <<<");
			download = 0;
		} else {
			RECON_UPI_DESTINATION_REPO.runReconUsd(fromDate, "USD");
			RECON_UPI_DESTINATION_REPO.runReconMur(fromDate, "MUR");
			RECON_UPI_DESTINATION_REPO.runReconDetail(fromDate, "USD");
			RECON_UPI_DESTINATION_REPO.runReconDetail(fromDate, "MUR");
			RECON_UPI_DESTINATION_REPO.runMarkupPro(fromDate);
			System.out.println(">>> Recon Process Completed <<<");
			entityManager.flush();
			download = 1;
		}

		Map<String, Object> result = new HashMap<>();
		result.put("status", "success");
		result.put("TotalSucceeded", totalSuccess);
		result.put("TotalFailed", totalFailure);
		result.put("TotalProcessed", totalProcessed);
		result.put("Ignored", ignoredExcelCount + ignoredDbCount);
		result.put("IgnoredExcel", ignoredExcelCount);
		result.put("IgnoredDB", ignoredDbCount);
		result.put("Download", download);
		result.put("DuplicateExcel", duplicateExcelCount);
		result.put("ReversibleAccounts", reversibleCount);

		System.out.println("Upload Summary -> TotalSucceeded: " + totalSuccess + ", TotalFailed: " + totalFailure
				+ ", TotalProcessed: " + totalProcessed + ", ReversibleAccounts: " + reversibleCount
				+ ", IgnoredExcel: " + ignoredExcelCount + ", IgnoredDB: " + ignoredDbCount + ", TotalIgnored: "
				+ (ignoredExcelCount + ignoredDbCount) + ",DuplicateExcel: " +duplicateExcelCount);

		return result;
	}

	@Transactional
	public Map<String, Object> SaveUpiDestFiles(String fileInput, MultipartFile[] files, String userID, String USERNAME,
			boolean overwrite, String fromDate) throws SQLException, IOException {

		Map<String, Object> resultMap1 = new LinkedHashMap<>();
		// ✅ Check rate maintenance first
		int rateCount = RATE_MAIN_REPO.getRateCount(fromDate);
		if (rateCount == 0) {
			resultMap1.put("status", "norate");
			resultMap1.put("message", "Upload failed — rate maintenance required for the selected date.");
			return resultMap1; // ❌ Stop processing here
		}

		int totalSuccess = 0,totalFailure = 0,totalProcessed = 0,ignored = 0; // ✅ added ignored counter
		int download = 0,duplicateExcelCount = 0;

		Map<String, Object> resultMap = new LinkedHashMap<>();
		List<String> duplicateAddl = new ArrayList<>();
		List<String> skippedReasons = new ArrayList<>(); // ✅ collect skipped reasons

		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}

			String fileName = file.getOriginalFilename();
			String fileExt = "";
			int i = fileName.lastIndexOf('.');
			if (i > 3) {
				fileExt = fileName.substring(i + 1);
			}

			if (fileExt.equalsIgnoreCase("xlsx") || fileExt.equalsIgnoreCase("xls")) {
				int successCount = 0;
				int failureCount = 0;

				try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
					Sheet sheet = workbook.getSheetAt(0);

					// ✅ Validate headers before processing
					Row headerRow = sheet.getRow(2); // since you skip first 2 rows
					if (headerRow == null) {
						resultMap.put("status", "error");
						resultMap.put("message", "Missing header row in the file.");
						return resultMap;
					}

					DataFormatter formatter = new DataFormatter();
					List<String> actualHeaders = new ArrayList<>();
					for (int j = 0; j < DESTINATION_HEADERS.size(); j++) {
						Cell cell = headerRow.getCell(j);
						String headerValue = formatter.formatCellValue(cell).trim();
						actualHeaders.add(headerValue);
					}

					// Compare headers
					if (!DESTINATION_HEADERS.equals(actualHeaders)) {
						List<String> invalidColumns = new ArrayList<>();
						for (int idx = 0; idx < DESTINATION_HEADERS.size(); idx++) {
							String expected = DESTINATION_HEADERS.get(idx);
							String actual = (idx < actualHeaders.size()) ? actualHeaders.get(idx) : "MISSING";
							if (!expected.equalsIgnoreCase(actual)) {
								invalidColumns.add(actual);
							}
						}

						resultMap.put("status", "error");
						resultMap.put("message",
								"Invalid Excel format. Invalid columns: " + String.join(", ", invalidColumns));
						return resultMap;
					}

					// ✅ Parse all rows
					List<HashMap<Integer, String>> mapList = new ArrayList<>();

					for (Sheet sh : workbook) {
						for (Row row : sh) {
							if (!isRowEmpty(row)) {
								if (row.getRowNum() < 3)
									continue; // skip first 2 rows

								HashMap<Integer, String> map = new HashMap<>();
								for (int j = 0; j < 50; j++) {
									Cell cell = row.getCell(j);
									String cellValue = "";
									if (cell != null) {
										switch (cell.getCellTypeEnum()) {
										case NUMERIC:
											if (DateUtil.isCellDateFormatted(cell)) {
												cellValue = formatter.formatCellValue(cell);
											} else {
												cellValue = String.valueOf(cell.getNumericCellValue());
											}
											break;
										case STRING:
											cellValue = cell.getStringCellValue().trim();
											break;
										case FORMULA:
											cellValue = formatter.formatCellValue(cell);
											break;
										case BOOLEAN:
											cellValue = String.valueOf(cell.getBooleanCellValue());
											break;
										case BLANK:
											cellValue = "";
											break;
										default:
											cellValue = formatter.formatCellValue(cell);
										}
									}
									map.put(j, cellValue);
								}
								mapList.add(map);
							}
						}
					}
					
					// Check DB duplicates
					for (HashMap<Integer, String> item : mapList) {
						String ref = item.get(23);
						String amtStr = item.get(8); 
						BigDecimal amt = parseBigDecimal(amtStr);
						if (ref != null && !ref.trim().isEmpty()) {
							if (ref != null) {
								RECON_UPI_DESTINATION_ENTITY checkAdl = RECON_UPI_DESTINATION_REPO.findByRefAndAmount(ref,amt);
								if (checkAdl != null)
									duplicateAddl.add(ref);
							}
						}
					}

					if (!duplicateAddl.isEmpty() && !overwrite) {
						resultMap.put("status", "duplicate");
						resultMap.put("addl", duplicateAddl);
						return resultMap;
					} else if (!duplicateAddl.isEmpty() && overwrite) {
						delteDest(duplicateAddl);
					}

					Set<String> processedRefAmountInFile = new HashSet<>();
					// ✅ Save records
					for (HashMap<Integer, String> item : mapList) {
						try {
							String ref = item.get(23);        // REFNUM
							String txnAmt = item.get(8);     // TRANSACTION_AMOUNT
							String txntype = item.get(10);     // TRANSACTION_AMOUNT

							if (ref != null && !ref.trim().isEmpty()
							        && txnAmt != null && !txnAmt.trim().isEmpty()
							        && txntype != null && !txntype.trim().isEmpty()) {

							    String compositeKey = ref.trim() + "|" + txnAmt.trim()+ "|" + txntype.trim();

							    // 🔁 DUPLICATE INSIDE EXCEL → SAME REFNUM + SAME AMOUNT
							    if (!processedRefAmountInFile.add(compositeKey)) {
							        duplicateExcelCount++;
							        continue; // skip duplicate row
							    }
							}
							RECON_UPI_DESTINATION_ENTITY up = new RECON_UPI_DESTINATION_ENTITY();

							up.setSttl_date(item.get(1));
							up.setArn(item.get(2));
							up.setMcc(item.get(3));
							up.setAuth_amnt(parseBigDecimal(item.get(4)));
							up.setAcct_amnt(parseBigDecimal(item.get(5)));
							up.setAuth_currency(item.get(6));
							up.setTransaction_date(item.get(7));
							up.setRefnum(item.get(23));
							up.setReconcile_amount(parseBigDecimal(item.get(17)));
							up.setReconcile_cur(item.get(18));
							up.setTransaction_amount(parseBigDecimal(item.get(8)));
							up.setTransaction_currency(item.get(9));
							up.setTransaction_type(item.get(10));
							up.setCbs_acc_no(item.get(11));
							up.setCard_number(item.get(12));
							up.setCard_type(item.get(13));
							up.setMerchant_desc(item.get(14));
							up.setTrn_country(item.get(15));
							up.setApproval_code(item.get(16));
							up.setInterchange_fees(parseBigDecimal(item.get(19)));
							up.setService_fee(parseBigDecimal(item.get(20)));
							up.setService_ind(item.get(21));
							up.setI_fee_ind(item.get(22));
							up.setDr_cr(item.get(24));
							up.setMarkup_value(parseBigDecimal(item.get(25)));
							up.setMarkup_currency(item.get(26));

							// ✅ restriction check
							boolean skipRecord = false;
							String skipReason = "";

							String cbsAcc = up.getCbs_acc_no();
							if (cbsAcc != null && cbsAcc.startsWith("CREDIT_")) {
								skipRecord = true;
								skipReason = "Skipped due to CREDIT_ prefix in CBS_ACCOUNT_NUMBER (value: " + cbsAcc
										+ ")";
							}

							List<String> restrictedCardTypes = Arrays.asList("ABC Credit Card", "ABC Staff Credit Card",
									"ABC Credit Card Supplementary", "ABC Corporate Suplementary Credit");

							String cardType = up.getCard_type();
							if (!skipRecord && cardType != null && restrictedCardTypes.contains(cardType.trim())) {
								skipRecord = true;
								skipReason = "Skipped due to restricted CARD_TYPE: " + cardType;
							}

							logger.info("File processed: " + fileName + " | Success: " + totalSuccess + " | Failed: "
									+ totalFailure);

							if (skipRecord) {
								ignored++;
								skippedReasons.add("File: " + fileName + " - " + skipReason);
								continue; // ✅ skip saving this record
							}

							// ✅ system values
							up.setEntity_flg("Y");
							up.setAuth_flg("N");
							up.setModify_flg("N");
							up.setDel_flg("N");
							up.setRecon_flg("N");
							SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
							Date parsedDate = sdf.parse(fromDate);
							up.setRecon_tran_date(new Date());
							up.setRecon_process_date(parsedDate);
							up.setRecon_type("AUTO");
							up.setEntry_user(userID);
							up.setEntry_time(new Date());
							up.setModify_user(userID);
							up.setModify_time(new Date());
							up.setAuth_user(userID);
							up.setAuth_time(new Date());
							up.setSrl_no(RECON_UPI_DESTINATION_REPO.getSeq());

							RECON_UPI_DESTINATION_REPO.save(up);
							successCount++;
						} catch (Exception rowEx) {
							failureCount++;
							rowEx.printStackTrace();
						} finally {
							totalProcessed++;
						}
					}

					totalSuccess += successCount;
					totalFailure += failureCount;

					System.out.println("File processed: " + fileName + " | Success: " + totalSuccess + " | Failed: "
							+ totalFailure);

				} catch (Exception e) {
					e.printStackTrace();
					totalFailure++;
				}
			} else {
				totalFailure++;
			}
		}
		audit.insertAudit(userID, USERNAME, "FILE UPLOAD", "UPI DEBIT DESTINATION FILE UPLOAD SUCCESSFULLY",
				"BRECON_UPI_DESTINATION_TABLE", "UPLOAD");

		// ✅ Final processing queries after file inserts
		RECON_UPI_DESTINATION_REPO.flush();
		RECON_UPIMAIN_REPO.insertValueReconTb(USERNAME, fromDate);
		entityManager.flush();
		RECON_UPI_DESTINATION_REPO.UpdateValueDestTb(fromDate);
		entityManager.flush();
		RECON_UPI_DESTINATION_REPO.UpdateValueSourTb(fromDate);
		entityManager.flush();
		RECON_UPI_DESTINATION_REPO.UpdateBalanceEnquiry(fromDate);
		entityManager.flush();
		entityManager.clear();

		boolean hasY = RECON_UPI_SOURCE_REPO.existsAnyY() > 0;

		if (!hasY) {
			System.out.println(">>> NOT GENERATE RECON PROCEDURE <<<");
			download = 0;
		} else {
			RECON_UPI_DESTINATION_REPO.runReconUsd(fromDate, "USD");
			System.out.println(">>> RECON_UPIREPORT_USD_PROCEDURE executed successfully <<<");

			RECON_UPI_DESTINATION_REPO.runReconMur(fromDate, "MUR");

			// call mur detail procedure
			RECON_UPI_DESTINATION_REPO.runReconDetail(fromDate, "USD");
			// call usd detail procedure
			RECON_UPI_DESTINATION_REPO.runReconDetail(fromDate, "MUR");
			// call markup procedure
			RECON_UPI_DESTINATION_REPO.runMarkupPro(fromDate);
			System.out.println(">>> Recon Process Completed <<<");
			entityManager.flush();
			download = 1;
		}

		Map<String, Object> result = new HashMap<>();
		result.put("status", "success");
		result.put("TotalSucceeded", totalSuccess);
		result.put("TotalFailed", totalFailure);
		result.put("TotalProcessed", totalProcessed);
		result.put("Ignored", ignored); // ✅ added ignored count
		result.put("Download", download);
		result.put("DuplicateExcel", duplicateExcelCount);
		result.put("SkippedReasons", skippedReasons); // ✅ added skipped reasons

		System.out.println("Upload Summary -> TotalSucceeded: " + totalSuccess + ", TotalFailed: " + totalFailure
				+ ", TotalProcessed: " + totalProcessed + ",Ignored: " +ignored + ",SkippedReasons: " +skippedReasons+ ",DuplicateExcel: " +duplicateExcelCount);

		return result;
	}

	private BigDecimal parseBigDecimal(String numberStr) {
		try {
			if (numberStr == null || numberStr.trim().isEmpty()) {
				return BigDecimal.ZERO;
			}

			// Remove all characters except digits, dot, and minus sign
			String cleaned = numberStr.replaceAll("[^0-9.\\-]", "").trim();

			// Count number of dots
			int dotCount = 0;
			for (char c : cleaned.toCharArray()) {
				if (c == '.')
					dotCount++;
			}

			// If more than one dot, remove all except the last one
			if (dotCount > 1) {
				cleaned = cleaned.replaceAll("\\.(?=.*\\.)", "");
			}

			// Handle cases like "." or "-" safely
			if (cleaned.equals(".") || cleaned.equals("-") || cleaned.isEmpty()) {
				return BigDecimal.ZERO;
			}

			return new BigDecimal(cleaned);
		} catch (NumberFormatException e) {
			System.out.println("Skipping non-numeric value: '" + numberStr + "'");
			return BigDecimal.ZERO;
		}
	}

	private boolean isRowEmpty(Row row) {
		if (row == null) {
			return true;
		}
		DataFormatter formatter = new DataFormatter();
		for (Cell cell : row) {
			String text = formatter.formatCellValue(cell);
			if (text != null && !text.trim().isEmpty()) {
				return false; // row has some data
			}
		}
		return true;
	}

	public void UpiRefershMethod(String userID, String username) {
		// start procedure
		Date utilDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = sdf.format(utilDate);
		RECON_UPIMAIN_REPO.insertValueReconTb(userID, formattedDate);
		entityManager.flush();
		RECON_UPI_DESTINATION_REPO.UpdateValueDestTb(formattedDate);
		entityManager.flush();
		RECON_UPI_DESTINATION_REPO.UpdateValueSourTb(formattedDate);
		entityManager.flush();
		entityManager.clear();

		boolean hasY = RECON_UPI_SOURCE_REPO.existsAnyY() > 0;

		if (!hasY) {
			System.out.println(">>> NOT GENERATE RECON PROCEDURE <<<");
		} else {
			// Call USD procedure
			RECON_UPI_DESTINATION_REPO.runReconUsd(formattedDate, "USD");
			System.out.println(">>> RECON_UPIREPORT_USD_PROCEDURE executed successfully <<<");
			// Call MUR procedure
			RECON_UPI_DESTINATION_REPO.runReconMur(formattedDate, "MUR");
			RECON_UPI_DESTINATION_REPO.runReconDetail(formattedDate, "USD");
			RECON_UPI_DESTINATION_REPO.runReconDetail(formattedDate, "MUR");
			RECON_UPI_DESTINATION_REPO.runMarkupPro(formattedDate);
			System.out.println(">>> RECON_UPIREPORT_MUR_PROCEDURE executed successfully <<<");
			System.out.println(">>> Recon Process Completed <<<");
			entityManager.flush();
		}

		RECON_UPIREPORT_MUR_REPO.flush();

		RECON_UPIREPORT_MUR_ENTITY mur = RECON_UPIREPORT_MUR_REPO.getReconDate(formattedDate, "MUR");
		mur.setEntry_user(username);
		RECON_UPIREPORT_MUR_REPO.save(mur);

		RECON_UPIREPORT_USD_REPO.flush();

		RECON_UPIREPORT_USD_ENTITY usd = RECON_UPIREPORT_USD_REPO.getReconDate(formattedDate, "USD");
		mur.setEntry_user(username);
		RECON_UPIREPORT_USD_REPO.save(usd);

		// audit start
		audit.insertAudit(userID, username, "UPI DEBIT RECON PROCESS", "Data synced successfully!..",
				"RECON_UPIMAIN_REPO", "REFERSH");

	}

	/**
	 * Helper to set addl_text, addl2, addl3 according to rules: - parts.length >= 3
	 * -> addl_text = p0, addl2 = p1, addl3 = p2 - parts.length == 2 -> addl_text =
	 * p0, addl2 = p1, addl3 = p1 - parts.length == 1 -> addl_text = p0, addl2 = p0,
	 * addl3 = p0
	 */
	private void setAddlFields(RECON_UPI_SOURCE_ENTITY up, String addlText) {
		if (addlText == null || addlText.trim().isEmpty()) {
			up.setAddl_text(null);
			up.setAddl2(null);
			up.setAddl3(null);
			return;
		}
		String[] parts = addlText.trim().split("\\s+");
		if (parts.length >= 3) {
			up.setAddl_text(parts[0]);
			up.setAddl2(parts[1]);
			up.setAddl3(parts[2]);
		} else if (parts.length == 2) {
			up.setAddl_text(parts[0]);
			up.setAddl2(parts[1]);
			up.setAddl3(parts[1]);
		} else { // parts.length == 1
			up.setAddl_text(parts[0]);
			up.setAddl2(parts[0]);
			up.setAddl3(parts[0]);
		}
	}

}
