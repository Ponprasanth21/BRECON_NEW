package com.bornfire.recon.services.upload;

import java.io.IOException;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.*;
import com.bornfire.recon.entities.upload.*;
import com.bornfire.recon.services.AuidtConfigure;

@Service
@ConfigurationProperties("output")
@Transactional
public class DrUploadService {

	@Autowired
	RECON_DRFILE_SOURCE_REPO RECON_DRFILE_SOURCE_REPO;

	@Autowired
	private DateParser dateParser;

	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;

	@Autowired
	RECON_DRFILE_DESTINATION_REPO BRECON_DRFILE_DESTINATION_REPO;

	@Autowired
	SequenceGenerator sequence;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	RECON_DRMAIN_REPO RECON_DRMAIN_REPO;

	@Autowired
	DateParser DateParser;

	@Autowired
	AuidtConfigure audit;
	
	@Autowired
	RECON_DRREPORT_MUR_REPO RECON_DRREPORT_MUR_REPO;

	@Autowired
	RECON_DRREPORT_USD_REPO RECON_DRREPORT_USD_REPO;
	
	@Autowired
	RATE_MAIN_REPO RATE_MAIN_REPO;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int delteSOURCE(List<String> duplicateRef) {
		return RECON_DRFILE_SOURCE_REPO.delterefnum(duplicateRef);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int delteDest(List<String> duplicateRef) {
		return BRECON_DRFILE_DESTINATION_REPO.delterefnum(duplicateRef);
	}

	private static final List<String> SOURCE_HEADERS = Arrays.asList("TRN_DT", "VALUE_DT", "TRN_REF", "CTRY",
			"RELATED ACCOUNT", "TRN_DESC", "ADDL_TEXT", "AC_CCY", "DR_ORG", "CR_ORG");

	private static final List<String> DESTINATION_HEADERS = Arrays.asList("ROWNUM", "ARN", "MCC", "AUTH_AMNT",
			"ACCT_AMNT", "AUTH_CURRENCY", "TRANSACTION_DATE", "TRANSACTION_AMOUNT", "TRANSACTION_CURRENCY",
			"TRANSACTION_TYPE", "CBS_ACCOUNT_NUMBER", "CARD_NUMBER", "CARD_TYPE", "MERCHANT_DESC", "COUNTRY_NAME",
			"APPROVAL_CODE", "RECONCILE_AMOUNT", "RECONCILE_CUR", "FEES", "I_FEE_IND", "REFNUM", "DR_CR",
			"MARKUP_VALUE", "MARKUP_CURRENCY");

	@Transactional
	public Map<String, Object> SaveDrSourceFiles(String fileInput, MultipartFile[] files, String userID,
			String USERNAME, boolean overwrite, String fromDate) throws SQLException, IOException {

		Map<String, Object> resultMap = new LinkedHashMap<>();

		// ✅ Check rate maintenance first
		int rateCount = RATE_MAIN_REPO.getRateCount(fromDate);
		if (rateCount == 0) {
			resultMap.put("status", "norate");
			resultMap.put("message", "Upload failed — rate maintenance required for the selected date.");
			return resultMap; // ❌ Stop processing here
		}

		int totalSuccess = 0,totalFailure = 0,totalProcessed = 0,download = 0,reversibleCount = 0;

		// ✅ Extra counters
		int ignoredExcelCount = 0,ignoredDbCount = 0,duplicateExcelCount = 0;

		for (MultipartFile file : files) {
			if (file == null || file.isEmpty())
				continue;

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

					// ✅ Validate headers (row 0 for DR file)
					Row headerRow = sheet.getRow(0);
					if (headerRow == null) {
						resultMap.put("status", "error");
						resultMap.put("message", "Missing header row in the file.");
						return resultMap;
					}

					DataFormatter formatter = new DataFormatter();
					List<String> actualHeaders = new ArrayList<>();
					for (int j = 0; j < SOURCE_HEADERS.size(); j++) {
						Cell cell = headerRow.getCell(j);
						String headerValue = formatter.formatCellValue(cell).trim();
						actualHeaders.add(headerValue);
					}

					if (!SOURCE_HEADERS.equals(actualHeaders)) {
						List<String> invalidColumns = new ArrayList<>();
						for (int idx = 0; idx < SOURCE_HEADERS.size(); idx++) {
							String expected = SOURCE_HEADERS.get(idx);
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

					// ✅ Parse Excel into mapList
					List<HashMap<Integer, String>> mapList = new ArrayList<>();
					for (Sheet sh : workbook) {
						for (Row row : sh) {
							if (!isRowEmpty(row)) {
								if (row.getRowNum() < 1)
									continue; // skip header
								HashMap<Integer, String> map = new HashMap<>();
								for (int j = 0; j < 50; j++) {
									Cell cell = row.getCell(j);
									String cellValue;
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
									} else {
										cellValue = "";
									}
									map.put(j, cellValue);
								}
								mapList.add(map);
							}
						}
					}

					// ✅ Group by Ref2 and calculate sums
					Map<String, BigDecimal> ref2Sums = new HashMap<>();
					for (HashMap<Integer, String> item : mapList) {
						if (item.get(6) != null && !item.get(6).trim().isEmpty()) {
							String[] parts = item.get(6).trim().split("\\s+");
							String ref2 = parts.length > 2 ? parts[2] : null;
							if (ref2 != null) {
								BigDecimal crVal = parseBigDecimal(item.get(9));
								ref2Sums.merge(ref2, crVal, BigDecimal::add);
							}
						}
					}

					// ✅ Reversible accounts
					Set<String> reversibleRef2 = new HashSet<>();
					for (Map.Entry<String, BigDecimal> entry : ref2Sums.entrySet()) {
						if (entry.getValue().compareTo(BigDecimal.ZERO) == 0) {
							reversibleCount++;
							reversibleRef2.add(entry.getKey());
						}
					}

					// ✅ DB duplicates
					List<String> duplicateRef = new ArrayList<>();
					for (HashMap<Integer, String> item : mapList) {
						String[] parts = item.get(6) != null ? item.get(6).trim().split("\\s+") : new String[0];
						String ref2 = parts.length > 2 ? parts[2] : null; 
					    String crOrg = item.get(9); // CR_ORG column
					    
						if (ref2 != null && !ref2.trim().isEmpty()) {
							if (ref2 != null) {
								RECON_DRFILE_SOURCE_ENTITY checkAdl = RECON_DRFILE_SOURCE_REPO.findByRefAndAmount(ref2,crOrg);
								if (checkAdl != null)
									duplicateRef.add(ref2);
							}
						}
					}

					if (!duplicateRef.isEmpty() && !overwrite) {
						resultMap.put("status", "duplicate");
						resultMap.put("ref2", duplicateRef);
						return resultMap;
					}

					if (!duplicateRef.isEmpty() && overwrite) {
						delteSOURCE(duplicateRef);
					}
                    
					Set<String> processedRef2InFile = new HashSet<>();
					// ✅ Insert rows
					for (HashMap<Integer, String> item : mapList) {
						try {
							String[] parts = item.get(6) != null ? item.get(6).trim().split("\\s+") : new String[0];
							String ref2 = parts.length > 2 ? parts[2] : null;  // REF2
							String crOrg = item.get(9);                        // CR_ORG (adjust index if needed)

							if (ref2 != null && !ref2.trim().isEmpty() && crOrg != null && !crOrg.trim().isEmpty()) {

							    String compositeKey = ref2.trim() + "|" + crOrg.trim();

							    // 🔁 DUPLICATE INSIDE EXCEL → SAME REF2 + SAME CR_ORG
							    if (!processedRef2InFile.add(compositeKey)) {
							        duplicateExcelCount++;
							        continue; // skip duplicate row
							    }
							}
							
							// Skip reversible
							if (ref2 != null && reversibleRef2.contains(ref2)) {
								ignoredExcelCount++;
								continue;
							}

							RECON_DRFILE_SOURCE_ENTITY up = new RECON_DRFILE_SOURCE_ENTITY();
							up.setTrn_dt(DateParser.parseDateSafe(item.get(0)));
							up.setValue_dt(DateParser.parseDateSafe(item.get(1)));
							up.setTrn_ref(item.get(2));
							up.setCtry(item.get(3));
							up.setRelated_account(item.get(4));
							up.setTrn_desc(item.get(5));

							if (item.get(6) != null && !item.get(6).trim().isEmpty()) {
							    String[] part = item.get(6).trim().split("\\s+"); // split by spaces

							    if (part.length >= 3) {
							        up.setAddl_text(part[0]);
							        up.setRef1(part[1]);
							        up.setRef2(part[2]);
							    } else if (part.length == 2) {
							        up.setAddl_text(part[0]);
							        up.setRef1(part[1]);
							        up.setRef2(part[1]);
							    } else if (part.length == 1) {
							        up.setAddl_text(part[0]);
							        up.setRef1(part[0]);
							        up.setRef2(part[0]);
							    }
							}

							up.setAc_ccy(item.get(7));
							up.setDr_org(item.get(8));
							up.setCr_org(item.get(9));

							// system values
							up.setEntity_flg("Y");
							up.setAuth_flg("N");
							up.setModify_flg("N");
							up.setDel_flg("N");
							up.setRecon_flg("N");
							up.setRecon_tran_date(new Date());
							SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
							Date parsedDate = sdf.parse(fromDate);
							up.setRecon_process_date(parsedDate);
							up.setRecon_type("AUTO");
							up.setEntry_user(userID);
							up.setEntry_time(new Date());
							up.setModify_user(userID);
							up.setModify_time(new Date());
							up.setAuth_user(userID);
							up.setAuth_time(new Date());
							up.setSrl_no(RECON_DRFILE_SOURCE_REPO.getSeq());

							RECON_DRFILE_SOURCE_REPO.save(up);
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

					System.out.println("File processed: " + fileName + " | Success: " + successCount + " | Failed: "
							+ failureCount);

				} catch (Exception e) {
					e.printStackTrace();
					totalFailure++;
				}
			} else {
				totalFailure++;
			}
		}

		// ✅ Final processing
		BRECON_DRFILE_DESTINATION_REPO.flush();
		RECON_DRMAIN_REPO.insertValueReconTb(USERNAME, fromDate);
		entityManager.flush();
		BRECON_DRFILE_DESTINATION_REPO.UpdateValueDestTb(fromDate);
		entityManager.flush();
		BRECON_DRFILE_DESTINATION_REPO.UpdateValueSourTb(fromDate);
		entityManager.flush();
		entityManager.clear();

		boolean hasY = RECON_DRFILE_SOURCE_REPO.existsAnyY() > 0;
		// If you want to re-enable recon procedure, uncomment and adjust:

		if (hasY) {
			BRECON_DRFILE_DESTINATION_REPO.runReconUsd(fromDate, "USD");
			BRECON_DRFILE_DESTINATION_REPO.runReconMur(fromDate, "MUR");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(fromDate, "USD");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(fromDate, "MUR");
			BRECON_DRFILE_DESTINATION_REPO.runMarkupPro(fromDate,"LOCAL");
			BRECON_DRFILE_DESTINATION_REPO.runMarkupPro(fromDate,"IB");
			entityManager.flush();
			download = 1;
		} else {
			download = 0;
		}

		audit.insertAudit(userID, USERNAME, "FILE UPLOAD", "DEBIT SOURCE FILE UPLOAD SUCCESSFULLY",
				"BRECON_DRFILE_SOURCE_TABLE", "UPLOAD");

		// ✅ Final JSON response
		Map<String, Object> result = new HashMap<>();
		result.put("status", "success");
		result.put("TotalSucceeded", totalSuccess);
		result.put("TotalFailed", totalFailure);
		result.put("TotalProcessed", totalProcessed);
		result.put("Ignored",ignoredExcelCount + ignoredDbCount + duplicateExcelCount);
		result.put("IgnoredExcel", ignoredExcelCount);
		result.put("IgnoredDB", ignoredDbCount);
		result.put("DuplicateExcel", duplicateExcelCount);
		result.put("Download", download);
		result.put("ReversibleAccounts", reversibleCount);

		System.out.println("Upload Summary -> " + "TotalSucceeded: " + totalSuccess + ", TotalFailed: " + totalFailure
				+ ", TotalProcessed: " + totalProcessed + ", ReversibleAccounts: " + reversibleCount
				+ ", IgnoredExcel: " + ignoredExcelCount + ", IgnoredDB: " + ignoredDbCount + ", TotalIgnored: "
				+ (ignoredExcelCount + ignoredDbCount) + ",DuplicateExcel: " +duplicateExcelCount);

		return result;
	}

	@Transactional
	public Map<String, Object> SaveDrDestFiles(String fileInput, MultipartFile[] files, String userID, String USERNAME,
			boolean overwrite, String fromDate) throws SQLException, IOException {

		Map<String, Object> result = new LinkedHashMap<>();

		// ✅ Check rate maintenance first
		int rateCount = RATE_MAIN_REPO.getRateCount(fromDate);
		if (rateCount == 0) {
			result.put("status", "norate");
			result.put("message", "Upload failed — rate maintenance required for the selected date.");
			return result;
		}

		int totalSuccess = 0,totalFailure = 0,totalProcessed = 0,ignored = 0,download = 0,duplicateExcelCount = 0;

		List<String> duplicateRefs = new ArrayList<>();
		List<String> skippedReasons = new ArrayList<>();
		DataFormatter formatter = new DataFormatter();

		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}

			String fileName = file.getOriginalFilename();
			String fileExt = "";
			int i = (fileName != null) ? fileName.lastIndexOf('.') : -1;
			if (i > 0) {
				fileExt = fileName.substring(i + 1);
			}

			if (!("xlsx".equalsIgnoreCase(fileExt) || "xls".equalsIgnoreCase(fileExt))) {
				totalFailure++;
				continue;
			}

			int successCount = 0,failureCount = 0;

			try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
				Sheet sheet = workbook.getSheetAt(0);
				if (sheet == null) {
					totalFailure++;
					continue;
				}

				// ✅ Try both possible header rows
				Row row3 = sheet.getRow(3);
				Row row2 = sheet.getRow(2);

				List<String> actualHeadersRow3 = getHeaders(row3, DESTINATION_HEADERS.size());
				List<String> actualHeadersRow2 = getHeaders(row2, DESTINATION_HEADERS.size());

				boolean matchRow3 = headersMatch(actualHeadersRow3, DESTINATION_HEADERS);
				boolean matchRow2 = headersMatch(actualHeadersRow2, DESTINATION_HEADERS);

				Row headerRow;
				if (matchRow3) {
					headerRow = row3;
				} else if (matchRow2) {
					headerRow = row2;
				} else {
					result.put("status", "error");
					result.put("message",
							"Invalid header row. Found row3: " + actualHeadersRow3 + " | row2: " + actualHeadersRow2);
					return result;
				}

				int headerRowNum = headerRow.getRowNum();

				// ✅ Collect rows starting after header
				List<HashMap<Integer, String>> mapList = new ArrayList<>();
				for (int r = headerRowNum + 1; r <= sheet.getLastRowNum(); r++) {
					Row row = sheet.getRow(r);
					if (row == null || isRowEmpty(row)) {
						continue;
					}

					HashMap<Integer, String> map = new HashMap<>();
					for (int j = 0; j < 50; j++) {
						Cell cell = row.getCell(j);
						String cellValue = (cell == null) ? "" : formatter.formatCellValue(cell).trim();
						map.put(j, cellValue);
					}

					// Duplicate check (REFNUM at index 20)
					String refCandidate = map.get(20);
					if (refCandidate != null && !refCandidate.trim().isEmpty()) {
						String ref = refCandidate.trim();
						String txnAmt = map.get(7); 
						BigDecimal amt = parseBigDecimal(txnAmt);
						RECON_DRFILE_DESTINATION_ENTITY checkRef = BRECON_DRFILE_DESTINATION_REPO.findByRefAndAmount(ref,amt);
						if (checkRef != null) {
							duplicateRefs.add(checkRef.getRefnum());
						}
					}
					mapList.add(map);
				}

				// ✅ Handle duplicates
				if (!duplicateRefs.isEmpty() && !overwrite) {
					result.put("status", "duplicate");
					result.put("ref2", duplicateRefs);
					return result;
				}
				if (!duplicateRefs.isEmpty() && overwrite) {
					delteDest(duplicateRefs);
				}

				Set<String> processedRefAmountInFile = new HashSet<>();
				// ✅ Save records
				for (HashMap<Integer, String> item : mapList) {
					try {
						   String ref = item.get(20); // REFNUM column
						   String txnAmt = item.get(7);  // TRANSACTION_AMOUNT column
						   String txntype = item.get(9);  // TRANSACTION_AMOUNT column

						   // 🔁 DUPLICATE INSIDE EXCEL → SAME REFNUM + SAME AMOUNT
						   if (ref != null && !ref.trim().isEmpty()
						           && txnAmt != null && !txnAmt.trim().isEmpty()
						           && txntype != null && !txntype.trim().isEmpty()) {

						       String compositeKey = ref.trim() + "|" + txnAmt.trim()+ "|" + txntype.trim();

						       if (!processedRefAmountInFile.add(compositeKey)) {
						           duplicateExcelCount++;
						           continue; // 🚫 skip duplicate row
						       }
						   }
						RECON_DRFILE_DESTINATION_ENTITY up = new RECON_DRFILE_DESTINATION_ENTITY();

						up.setArn(item.get(1));
						up.setMcc(item.get(2));
						up.setAuth_amnt(parseBigDecimal(item.get(3)));
						up.setAcct_amnt(parseBigDecimal(item.get(4)));
						up.setAuth_currency(item.get(5));
						up.setTransaction_date(parseDateSafe(item.get(6)));
						up.setTransaction_amount(parseBigDecimal(item.get(7)));
						up.setTransaction_currency(item.get(8));
						up.setTransaction_type(item.get(9));
						up.setCbs_account_number(item.get(10));
						up.setCard_number(item.get(11));
						up.setCard_type(item.get(12));
						up.setMerchant_desc(item.get(13));
						up.setCountry_name(item.get(14));
						up.setApproval_code(item.get(15));
						up.setReconcile_amount(parseBigDecimal(item.get(16)));
						up.setReconcile_cur(item.get(17));
						up.setFees(parseBigDecimal(item.get(18)));
						up.setI_fee_ind(item.get(19));
						up.setRefnum(item.get(20));
						up.setDr_cr(item.get(21));
						up.setMarkup_value(parseBigDecimal(item.get(22)));
						up.setMarkup_currency(item.get(23));

						// ✅ restriction checks
						boolean skipRecord = false;
						String skipReason = "";

						String cbsAcc = up.getCbs_account_number();
						if (cbsAcc != null && cbsAcc.startsWith("CREDIT_")) {
							skipRecord = true;
							skipReason = "Skipped due to CREDIT_ prefix in CBS_ACCOUNT_NUMBER (value: " + cbsAcc + ")";
						}

						List<String> restrictedCardTypes = Arrays.asList("ABC Credit Card", "ABC Staff Credit Card",
								"ABC Credit Card Supplementary", "ABC Corporate Suplementary Credit");

						String cardType = up.getCard_type();
						if (!skipRecord && cardType != null && restrictedCardTypes.contains(cardType.trim())) {
							skipRecord = true;
							skipReason = "Skipped due to restricted CARD_TYPE: " + cardType;
						}

						if (skipRecord) {
							ignored++;
							skippedReasons.add("File: " + fileName + " - " + skipReason);
							continue;
						}

						// ✅ system values
						up.setEntity_flg("Y");
						up.setAuth_flg("N");
						up.setModify_flg("N");
						up.setDel_flg("N");
						up.setRecon_flg("N");

						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
						Date parsedDate = sdf.parse(fromDate);
						up.setRecon_process_date(parsedDate);
						up.setRecon_tran_date(new Date());
						up.setRecon_type("AUTO");
						up.setEntry_user(userID);
						up.setEntry_time(new Date());
						up.setModify_user(userID);
						up.setModify_time(new Date());
						up.setAuth_user(userID);
						up.setAuth_time(new Date());
						up.setSrl_no(BRECON_DRFILE_DESTINATION_REPO.getSeq());

						BRECON_DRFILE_DESTINATION_REPO.save(up);
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

				System.out.println("File processed: " + fileName + " | FileSuccess: " + successCount + " | FileFailed: "
						+ failureCount);

			} catch (Exception e) {
				e.printStackTrace();
				totalFailure++;
			}
		}

		// ✅ Final queries after inserts
		BRECON_DRFILE_DESTINATION_REPO.flush();
		RECON_DRMAIN_REPO.insertValueReconTb(USERNAME, fromDate);
		entityManager.flush();
		BRECON_DRFILE_DESTINATION_REPO.UpdateValueDestTb(fromDate);
		entityManager.flush();
		BRECON_DRFILE_DESTINATION_REPO.UpdateValueSourTb(fromDate);
		entityManager.flush();
		entityManager.clear();
		
		boolean hasY = RECON_DRFILE_SOURCE_REPO.existsAnyY() > 0;
		// If you want to re-enable recon procedure, uncomment and adjust:

		if (hasY) {
			BRECON_DRFILE_DESTINATION_REPO.runReconUsd(fromDate, "USD");
			BRECON_DRFILE_DESTINATION_REPO.runReconMur(fromDate, "MUR");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(fromDate, "USD");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(fromDate, "MUR");
			BRECON_DRFILE_DESTINATION_REPO.runMarkupPro(fromDate,"LOCAL");
			BRECON_DRFILE_DESTINATION_REPO.runMarkupPro(fromDate,"IB");
			entityManager.flush();
			download = 1;
		} else {
			download = 0;
		}

		audit.insertAudit(userID, USERNAME, "FILE UPLOAD", "DEBIT DESTINATION FILE UPLOAD SUCCESSFULLY",
				"BRECON_DRFILE_DESTINATION_TABLE", "UPLOAD");

		result.put("status", "success");
		result.put("TotalSucceeded", totalSuccess);
		result.put("TotalFailed", totalFailure);
		result.put("TotalProcessed", totalProcessed);
		result.put("Ignored", ignored);
		result.put("Download", download);
		result.put("DuplicateExcel", duplicateExcelCount);
		result.put("SkippedReasons", skippedReasons);

		System.out.println("Upload Summary -> " + "TotalSucceeded: " + totalSuccess + ", TotalFailed: " + totalFailure +",Ignored: " +ignored
				+ ", TotalProcessed: " + totalProcessed + ",DuplicateExcel: " +duplicateExcelCount + ",SkippedReasons: " +skippedReasons);

		return result;
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

	private BigDecimal parseBigDecimal(String numberStr) {
		try {
			if (numberStr == null || numberStr.trim().isEmpty()) {
				return BigDecimal.ZERO;
			}
			// Remove commas, spaces
			String cleaned = numberStr.replace(",", "").trim();
			return new BigDecimal(cleaned);
		} catch (Exception e) {
			System.out.println("Skipping non-numeric value: " + numberStr);
			return BigDecimal.ZERO;
		}
	}

	private Date parseDateSafe(String value) {
		try {
			if (value == null || value.trim().isEmpty()) {
				return null; // blank cell
			}

			// Excel date format: "Aug 2, 2025"
			SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);

			// Parse into Date object
			Date parsedDate = inputFormat.parse(value.trim());

			// Return Date object directly (entity.setTransaction_date expects Date)
			return parsedDate;

		} catch (Exception e) {
			System.err.println("Unparseable date: " + value);
			return null;
		}
	}

	public void DrRefershMethod(String userID, String username) {

		Date utilDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = sdf.format(utilDate);
		RECON_DRMAIN_REPO.insertValueReconTb(username, formattedDate);
		entityManager.flush();
		BRECON_DRFILE_DESTINATION_REPO.UpdateValueDestTb(formattedDate);
		entityManager.flush();
		BRECON_DRFILE_DESTINATION_REPO.UpdateValueSourTb(formattedDate);
		entityManager.flush();
		entityManager.clear();

		boolean hasY = RECON_DRFILE_SOURCE_REPO.existsAnyY() > 0;

		if (!hasY) {
			System.out.println(">>> NOT GENERATE RECON PROCEDURE <<<");
		} else {
			// Call USD procedure
			BRECON_DRFILE_DESTINATION_REPO.runReconUsd(formattedDate, "USD");
			System.out.println(">>> RECON_UPIREPORT_USD_PROCEDURE executed successfully <<<");
			// Call MUR procedure
			BRECON_DRFILE_DESTINATION_REPO.runReconMur(formattedDate, "MUR");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(formattedDate, "USD");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(formattedDate, "MUR");
			BRECON_DRFILE_DESTINATION_REPO.runMarkupPro(formattedDate,"LOCAL");
			BRECON_DRFILE_DESTINATION_REPO.runMarkupPro(formattedDate,"IB");
			System.out.println(">>> RECON_UPIREPORT_MUR_PROCEDURE executed successfully <<<");
			System.out.println(">>> Recon Process Completed <<<");
			entityManager.flush();
		}

		RECON_DRREPORT_MUR_REPO.flush();

		RECON_DRREPORT_MUR_ENTITY mur = RECON_DRREPORT_MUR_REPO.getReconDate(formattedDate, "MUR");
		mur.setEntry_user(username);
		RECON_DRREPORT_MUR_REPO.save(mur);

		RECON_DRREPORT_USD_REPO.flush();

		RECON_DREPORT_USD_ENTITY usd = RECON_DRREPORT_USD_REPO.getReconDate(formattedDate, "USD");
		mur.setEntry_user(username);
		RECON_DRREPORT_USD_REPO.save(usd);

		// audit start
		audit.insertAudit(userID, username, "DEBIT RECON PROCESS", "Data synced successfully!..", "RECON_DRMAIN_REPO","REFERSH");
	}

	private List<String> getHeaders(Row row, int expectedSize) {
		List<String> headers = new ArrayList<>();
		DataFormatter formatter = new DataFormatter();
		if (row == null)
			return headers;

		for (int j = 0; j < expectedSize; j++) {
			Cell cell = row.getCell(j);
			String value = (cell == null) ? "" : formatter.formatCellValue(cell).trim();
			headers.add(value);
		}
		return headers;
	}

	private boolean headersMatch(List<String> actual, List<String> expected) {
		if (actual.size() < expected.size())
			return false;
		for (int i = 0; i < expected.size(); i++) {
			if (!expected.get(i).equalsIgnoreCase(actual.get(i).trim())) {
				return false;
			}
		}
		return true;
	}

}
