package com.bornfire.recon.services.upload;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.controllers.BRECONUploadController;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.RATE_MAIN_REPO;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_CRMAIN_REPO;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRREPORT_USD_REPO;
import com.bornfire.recon.services.AuidtConfigure;

@Service
@ConfigurationProperties("output")
@Transactional
public class CrUploadService {
	private static final Logger logger = LoggerFactory.getLogger(CrUploadService.class);

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	DateParser DateParser;

	@Autowired
	RECON_CRFILE_SOURCE_REPO fileSourceRepo;

	@Autowired
	RECON_CRFILE_DESTINATION_REPO fileDestinationRepo;

	@Autowired
	BRECON_AUDIT_REPO breconAuditRepo;

	@Autowired
	RECON_CRMAIN_REPO reconProRepo;

	@Autowired
	RECON_CRREPORT_USD_REPO RECON_REPORTUSD_REPO;

	@Autowired
	RECON_CRREPORT_MUR_REPO RECON_REPORTMUR_REPO;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private SequenceGenerator sequence;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	AuidtConfigure audit;

	@Autowired
	DataSource dataSource;

	@Autowired
	RATE_MAIN_REPO RATE_MAIN_REPO;

//	get the date
	Date onlyDate;

	{
		try {
			Date utilDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			onlyDate = sdf.parse(sdf.format(utilDate));
		} catch (ParseException e) {
			e.printStackTrace();
			onlyDate = new Date(); // fallback
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int delteDEST(List<String> duplicateArns) {
		return fileDestinationRepo.deltearn(duplicateArns);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int delteSOURCE(List<String> duplicateTr) {
		return fileSourceRepo.deltearn(duplicateTr);
	}

	private static final List<String> SOURCE_HEADERS = Arrays.asList("ARN", "TR_HIS_ID", "TRXN_DATE", "TRXN_TIME",
			"POSTING_DATE", "CARD_NUMBER", "ABS_ACCT_NUMBER", "ACCT_CURRENCY", "SOURCE_CURR", "SOURCE_AMT", "BILL_CURR",
			"MC_BILL_AMT", "SV_BILL_AMT", "SETT_CURR", "SETT_AMT", "MARKUP_AMNT_ICCR", "TRANSACTION_IND",
			"TRANSACTION_FEE", "I_FEE_IND", "SIGN_IFEE", "TERM_TYPE", "TRANSACTION_TYPE", "TRANSACTION_DESCRIPTION",
			"TYPE_OF_CARD", "MERCHANT_NAME", "MERCHANT_COUNTRY");
	
	public Map<String, Object> saveCrSourceFile(String fileInput, MultipartFile[] files, String userID, String USERNAME,
			boolean overwrite, String Fromdate) throws IOException, SQLException {
		int successCount = 0, failureCount = 0, ignoreCount = 0, download = 0,duplicateCount = 0,totalProcessed = 0;
		Map<String, Object> resultMap = new LinkedHashMap<>();
		for (MultipartFile file : files) {
			String fileName = file.getOriginalFilename();
			String fileExt = "";
			int i = fileName.lastIndexOf('.');
			if (i > 3)
				fileExt = fileName.substring(i + 1);

			if (!(fileExt.equals("xlsx") || fileExt.equals("xls"))) {
				resultMap.put("status", "error");
				resultMap.put("message", "Invalid file type. Please upload an Excel file (.xls or .xlsx).");
				return resultMap;
			}
			int rateCount = RATE_MAIN_REPO.getRateCount(Fromdate);
			System.out.println(rateCount);
			if (rateCount == 0) {
				resultMap.put("status", "norate");
				resultMap.put("message", "Upload failed — rate maintenance required for the selected date.");
				return resultMap; // ❌ Stop processing here
			}

			try (Workbook workbook = WorkbookFactory.create(file.getInputStream()))
			/*
			 * Workbook workbook = WorkbookFactory.create(file.getInputStream(), "ABC$123"))
			 */
			{
				Sheet sheet = workbook.getSheetAt(0);

				// ✅ Validate headers before processing
				Row headerRow = sheet.getRow(2); // since you skip first 2 rows
				if (headerRow == null) {
					resultMap.put("status", "error");
					resultMap.put("message", "Missing header row in the file.");
					return resultMap;
				}

				List<String> actualHeaders = new ArrayList<>();
				DataFormatter formatter = new DataFormatter();
				for (int j = 0; j < SOURCE_HEADERS.size(); j++) {
					Cell cell = headerRow.getCell(j);
					String headerValue = formatter.formatCellValue(cell).trim();
					actualHeaders.add(headerValue);
				}

				// Compare headers
				if (!SOURCE_HEADERS.equals(actualHeaders)) {
					List<String> invalidColumns = new ArrayList<>();

					for (int i1 = 0; i1 < SOURCE_HEADERS.size(); i1++) {
						String expected = SOURCE_HEADERS.get(i1);
						String actual = (i1 < actualHeaders.size()) ? actualHeaders.get(i1) : "MISSING";

						if (!expected.equalsIgnoreCase(actual)) {
							invalidColumns.add(actual);
						}
					}

					resultMap.put("status", "error");
					resultMap.put("message",
							"Invalid Excel format. Invalid columns: " + String.join(", ", invalidColumns));
					return resultMap;
				}

				List<HashMap<Integer, String>> mapList = new ArrayList<>();
				for (Sheet s : workbook) {
					for (Row r : s) {
						if (!isRowEmpty(r)) {
							if (r.getRowNum() < 3)
								continue;
							HashMap<Integer, String> map = new HashMap<>();
							for (int j = 0; j < 200; j++) {
								Cell cell = r.getCell(j);
								DataFormatter formatter1 = new DataFormatter();
								String text = formatter1.formatCellValue(cell);
								map.put(j, text);
							}
							mapList.add(map);
						}
					}
				}
				
				// ✅ Pre-check duplicates
				List<String> duplicateArns = new ArrayList<>();
				for (HashMap<Integer, String> item : mapList) {
					String arn = item.get(0);// <-- taking ARN from column index 23
					String sourceAmt = item.get(9);
					BigDecimal amt = parseBigDecimal(sourceAmt);
					RECON_CRFILE_SOURCE_ENTITY checkArn = fileSourceRepo.findByArnAndAmount(arn,amt);
					if (checkArn != null) {
						duplicateArns.add(arn);
					}
				}
				if (!duplicateArns.isEmpty() && !overwrite) {
					resultMap.put("status", "duplicate");
					resultMap.put("arns", duplicateArns);
					return resultMap;
				}
				if (!duplicateArns.isEmpty() && overwrite) {
					// delete existing before inserting
					delteSOURCE(duplicateArns);
				}
				
				Set<String> processedArnsInFile = new HashSet<>();
				// upload start
				for (HashMap<Integer, String> item : mapList) {
					try {
						String tr_his_id = item.get(1); // SOURCE unique key
						String transactionType = item.get(21);

						// skip empty key
						if (tr_his_id == null || tr_his_id.trim().isEmpty()) {
							ignoreCount++;
							continue;
						}

						String arn = item.get(0);
						String sourceAmt = item.get(9);
						
						  // 🔁 DUPLICATE INSIDE EXCEL → SAME ARN + SAME SOURCE_AMT
				        if (arn != null && !arn.trim().isEmpty()
				                && sourceAmt != null && !sourceAmt.trim().isEmpty()) {

				            String compositeKey = arn.trim() + "|" + sourceAmt.trim();

				            if (!processedArnsInFile.add(compositeKey)) {
				                duplicateCount++;
				                continue; // 🚫 skip duplicate row
				            }
				        }
				        
						if ("Bill payment transaction".equalsIgnoreCase(transactionType)) {
							ignoreCount++;
							continue;
						}
						RECON_CRFILE_SOURCE_ENTITY up = new RECON_CRFILE_SOURCE_ENTITY();

						up.setArn(item.get(0));
						up.setTr_his_id(item.get(1));
						up.setTrxn_date(DateParser.parseDateSafe(item.get(2)));
						up.setTrxn_time(item.get(3));
						up.setPosting_date(DateParser.parseDateSafe(item.get(4)));
						up.setCard_number(item.get(5));
						up.setAbs_acct_number(item.get(6));
						up.setAcct_currency(item.get(7));
						up.setSource_curr(item.get(8));
						up.setSource_amt(DateParser.parseBigDecimal(item.get(9)));
						up.setBill_curr(item.get(10));
						up.setMc_bill_amt(DateParser.parseBigDecimal(item.get(11)));
						up.setSv_bill_amt(DateParser.parseBigDecimal(item.get(12)));
						up.setSett_curr(item.get(13));
						up.setSett_amt(DateParser.parseBigDecimal(item.get(14)));
						up.setMarkup_amnt_iccr(DateParser.parseBigDecimal(item.get(15)));
						up.setTransaction_ind(item.get(16));
						up.setTransaction_fee(DateParser.parseBigDecimal(item.get(17)));
						up.setI_fee_ind(item.get(18));
						up.setSign_ifee(DateParser.parseBigDecimal(item.get(19)));
						up.setTerm_type(item.get(20));
						up.setTransaction_type(item.get(21));
						up.setTransaction_description(item.get(22));
						up.setType_of_card(item.get(23));
						up.setMerchant_name(item.get(24));
						up.setMerchant_country(item.get(25));

						up.setSrl_no(fileSourceRepo.getSeq());
						up.setEntry_user(userID);
						up.setEntry_time(new Date());
						up.setEntity_flg("N");
						up.setDel_flg("N");
						up.setRecon_flg("N");
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
						Date parsedDate = sdf.parse(Fromdate);

						up.setRecon_process_date(parsedDate);
						up.setRecon_tran_date(new Date());
						up.setRecon_type("AUTO");

						fileSourceRepo.save(up);
						successCount++;
						System.out.println("FINAL COUNTS -> Succeeded: " + successCount + ", Failed: " + failureCount);
					} catch (Exception ex) {
						failureCount++;
						ex.printStackTrace();
					}
					finally {
						totalProcessed++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("status", "error");
				resultMap.put("message", "File upload failed: " + e.getMessage());
			}
		}

		// insert and update start
		System.out.println("indiee");
		fileDestinationRepo.flush();
		reconProRepo.insertValueReconTb(userID, Fromdate);
		entityManager.flush();
		fileDestinationRepo.UpdateValueDestTb(Fromdate);
		entityManager.flush();
		fileDestinationRepo.UpdateValueSourTb(Fromdate);
		entityManager.flush();
		entityManager.clear();

		boolean hasY = fileSourceRepo.existsAnyY() > 0;

		if (!hasY) {
			System.out.println(">>> NOT GENERATE RECON PROCEDURE <<<");
			download = 0;
		} else {
			// Call USD procedure
			fileDestinationRepo.runReconUsd(Fromdate, "USD");
			System.out.println(">>> RECON_CRREPORT_USD_PROCEDURE executed successfully <<<");
			// Call MUR procedure
			fileDestinationRepo.runReconMur(Fromdate, "MUR");
			// call mur detail procedure
			fileDestinationRepo.runReconDetailMur(Fromdate, "MUR");
			// call usd detail procedure
			fileDestinationRepo.runReconDetailUsd(Fromdate, "USD");
			System.out.println(">>> RECON_CRREPORT_MUR_PROCEDURE executed successfully <<<");
			System.out.println(">>> Recon Process Completed <<<");
			entityManager.flush();
			download = 1;
		}
		// audit start
		audit.insertAudit(userID, USERNAME, "FILE UPLOAD", "CREDIT SOURCE FILE UPLOAD SUCCESSFULLY",
				"BRECON_CRFILE_SOURCE_TABLE", "UPLOAD");

		resultMap.put("status", "success");
		resultMap.put("TotalSucceeded", successCount);
		resultMap.put("TotalFailed", failureCount);
		resultMap.put("TotalProcessed", totalProcessed);
		resultMap.put("Ignored", ignoreCount);
		resultMap.put("Download", download);
		resultMap.put("Duplicates", duplicateCount);
		
		System.out.println("Upload Summary -> " + "TotalSucceeded: " + successCount + ", TotalFailed: " + failureCount +",Ignored: " +ignoreCount
				+ ", TotalProcessed: " + totalProcessed + ",DuplicateExcel: " +duplicateCount);

		return resultMap;
	}

	private static final List<String> DESTINATION_HEADERS = Arrays.asList("BO_UTRNNO", "TAA_DATE", "TRXN_DATE",
			"TRXN_TIME", "POST_DATE", "CARD_NUMBER", "CCARD_NUM", "TRXN_TYPE", "ACCT_CURRENCY", "SOURCE_CURR",
			"SOURCE_AMT", "SETT_CURR", "SETT_AMT", "BILL_CURR", "MC_BILL_AMT", "SV_BILL_AMT", "TRXN_FEE",
			"TRANSACTION_IND", "I_FEE", "I_FEE_IND", "AUTH_CODE", "MERCHANT_NAME", "EXCEPTION", "ARN", "CYCLE_NUMBER",
			"MCC", "COUNTRY", "REFNUM");

	@Transactional
	public Map<String, Object> saveCrDestinationFile(String fileInput, MultipartFile[] files, String userID,
			String USERNAME, Boolean overwrite, String Fromdate) throws SQLException, IOException {
		int successCount = 0, failureCount = 0, ignoreCount = 0, duplicateCount = 0, download = 0,totalProcessed = 0;
		logger.info("Start 1");
		Map<String, Object> resultMap = new LinkedHashMap<>();

		for (MultipartFile file : files) {
			String fileName = file.getOriginalFilename();
			String fileExt = "";

			int i = fileName.lastIndexOf('.');
			if (i > 3)
				fileExt = fileName.substring(i + 1);

			if (!(fileExt.equals("xlsx") || fileExt.equals("xls"))) {
				resultMap.put("status", "error");
				resultMap.put("message", "Invalid file type. Please upload an Excel file (.xls or .xlsx).");
				return resultMap;
			}
			logger.info("Start 1.1");
			int rateCount = RATE_MAIN_REPO.getRateCount(Fromdate);
			System.out.println(rateCount);
			if (rateCount == 0) {
				resultMap.put("status", "norate");
				resultMap.put("message", "Upload failed — rate maintenance required for the selected date.");
				return resultMap; // ❌ Stop processing here
			}

			logger.info("Start 2");

			try (Workbook workbook = WorkbookFactory.create(file.getInputStream()))
			/*
			 * Workbook workbook = WorkbookFactory.create(file.getInputStream(), "ABC$123"))
			 */
			{
				logger.info("Start 2.1");
				Sheet sheet = workbook.getSheetAt(0);

				// ✅ Validate headers before processing
				Row headerRow = sheet.getRow(2); // since you skip first 2 rows
				if (headerRow == null) {
					resultMap.put("status", "error");
					resultMap.put("message", "Missing header row in the file.");
					return resultMap;
				}

				List<String> actualHeaders = new ArrayList<>();
				DataFormatter formatter = new DataFormatter();
				for (int j = 0; j < DESTINATION_HEADERS.size(); j++) {
					Cell cell = headerRow.getCell(j);
					String headerValue = formatter.formatCellValue(cell).trim();
					actualHeaders.add(headerValue);
				}

				// Compare headers
				if (!DESTINATION_HEADERS.equals(actualHeaders)) {
					List<String> invalidColumns = new ArrayList<>();

					for (int i1 = 0; i1 < DESTINATION_HEADERS.size(); i1++) {
						String expected = DESTINATION_HEADERS.get(i1);
						String actual = (i1 < actualHeaders.size()) ? actualHeaders.get(i1) : "MISSING";

						if (!expected.equalsIgnoreCase(actual)) {
							invalidColumns.add(actual);
						}
					}
					resultMap.put("status", "error");
					resultMap.put("message",
							"Invalid Excel format. Invalid columns: " + String.join(", ", invalidColumns));
					return resultMap;
				}

				List<HashMap<Integer, String>> mapList = new ArrayList<>();
				for (Sheet s : workbook) {
					for (Row r : s) {
						if (!isRowEmpty(r)) {
							if (r.getRowNum() < 3)
								continue;

							HashMap<Integer, String> map = new HashMap<>();
							for (int j = 0; j < 200; j++) {
								Cell cell = r.getCell(j);
								DataFormatter formatter1 = new DataFormatter();
								String text = formatter1.formatCellValue(cell);
								map.put(j, text);
							}
							mapList.add(map);
						}
					}
				}
				logger.info("Start 3");
				// ✅ Pre-check duplicates
				List<String> duplicateArns = new ArrayList<>();
				for (HashMap<Integer, String> item : mapList) {
					String arn = item.get(23); // <-- taking ARN from column index 23
					String sourceAmt = item.get(10);
					BigDecimal amt = parseBigDecimal(sourceAmt);
					RECON_CRFILE_DESTINATION_ENTITY checkArn = fileDestinationRepo.findByArnAndAmount(arn,amt);
					if (checkArn != null) {
						duplicateArns.add(arn);
					}
				}
				logger.info("Start 4");
				if (!duplicateArns.isEmpty() && !overwrite) {
					resultMap.put("status", "duplicate");
					resultMap.put("arns", duplicateArns);
					return resultMap;
				}
				if (!duplicateArns.isEmpty() && overwrite) {
					// delete existing before inserting
					delteDEST(duplicateArns);
				}
				logger.info("Start 5");
				
				Set<String> processedArnAmountInFile = new HashSet<>();
				// upload function
				for (HashMap<Integer, String> item : mapList) {
					try {
						 String arn = item.get(23); // DESTINATION unique key
						 String sourceAmt = item.get(10);

					        // Skip empty ARN
					        if (arn == null || arn.trim().isEmpty()) {
					            ignoreCount++;
					            continue;
					        }

					        // 🔁 DUPLICATE INSIDE EXCEL → SAME ARN + SAME SOURCE_AMT
					        if (arn != null && !arn.trim().isEmpty()
					                && sourceAmt != null && !sourceAmt.trim().isEmpty()) {

					            String compositeKey = arn.trim() + "|" + sourceAmt.trim();

					            if (!processedArnAmountInFile.add(compositeKey)) {
					                duplicateCount++;
					                continue; // 🚫 skip duplicate row
					            }
					        }
				        
						if ("Bill payment transaction".equalsIgnoreCase(item.get(7))) {
							ignoreCount++;
							continue;
						}
						logger.info("Start 6");
						RECON_CRFILE_DESTINATION_ENTITY up = new RECON_CRFILE_DESTINATION_ENTITY();
						up.setBo_utrnno(item.get(0));
						up.setTaa_date(item.get(1));
						up.setTrxn_date(item.get(2));
						up.setTrxn_time(item.get(3));
						up.setPost_date(item.get(4));
						up.setCard_number(item.get(5));
						up.setCcard_num(item.get(6));
						up.setTrxn_type(item.get(7));
						up.setAcct_currency(item.get(8));
						up.setSource_curr(item.get(9));
						up.setSource_amt(DateParser.parseBigDecimal(item.get(10)));
						up.setSett_curr(item.get(11));
						up.setSett_amt(DateParser.parseBigDecimal(item.get(12)));
						up.setBill_curr(item.get(13));
						up.setMc_bill_amt(DateParser.parseBigDecimal(item.get(14)));
						up.setSv_bill_amt(DateParser.parseBigDecimal(item.get(15)));
						up.setTrxn_fee(DateParser.parseBigDecimal(item.get(16)));
						up.setTransaction_ind(item.get(17));
						up.setI_fee(DateParser.parseBigDecimal(item.get(18)));
						up.setI_fee_ind(item.get(19));
						up.setAuth_code(item.get(20));
						up.setMerchant_name(item.get(21));
						up.setException(item.get(22));
						up.setArn(item.get(23));
						up.setCycle_number(item.get(24));
						up.setMcc(item.get(25));
						up.setCountry(item.get(26));
						up.setRefnum(item.get(27));
						logger.info("Start 7", Fromdate);
						up.setSrl_no(fileDestinationRepo.getSeq());
						up.setEntry_user(userID);
						up.setEntry_time(new Date());
						up.setEntity_flg("N");
						up.setDel_flg("N");
						up.setRecon_flg("N");
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
						Date parsedDate = sdf.parse(Fromdate);
						System.out.println(Fromdate);
						up.setRecon_process_date(parsedDate);
						up.setRecon_tran_date(new Date());
						up.setRecon_type("AUTO");
						fileDestinationRepo.save(up);
						logger.info("Start 8");
						successCount++;	
					} catch (Exception rowEx) {
						failureCount++;
						logger.info("Start FAILED");
						rowEx.printStackTrace();
					}
					finally {
						totalProcessed++;
					}
				}	
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Start 22");
				resultMap.put("status", "error");
				resultMap.put("message", "File upload failed: " + e.getMessage());
			}
		}
		// insert and update start
		logger.info("Start 9");
		fileDestinationRepo.flush();
		logger.info("Start 10");

		reconProRepo.insertValueReconTb(userID, Fromdate);
		logger.info("Start 11");
		entityManager.flush();
		logger.info("Start 12");
		fileDestinationRepo.UpdateValueDestTb(Fromdate);
		logger.info("Start 13");
		entityManager.flush();
		logger.info("Start 14");
		fileDestinationRepo.UpdateValueSourTb(Fromdate);
		logger.info("Start 15");
		entityManager.flush();
		logger.info("Start 16");
		entityManager.clear();
		logger.info("Start 17");
		logger.info("Start 18");
		logger.info("Start 19");
		boolean hasY = fileSourceRepo.existsAnyY() > 0;
		if (!hasY) {
			System.out.println(">>> NOT GENERATE RECON PROCEDURE <<<");
			download = 0;
		} else {
			// Call USD procedure
			System.out.println(">>>   " + hasY + "    <<<< " + Fromdate);
			fileDestinationRepo.runReconUsd(Fromdate, "USD");
			logger.info(">>> RECON_CRREPORT_USD_PROCEDURE executed successfully <<<");
			// Call MUR procedure
			fileDestinationRepo.runReconMur(Fromdate, "MUR");
			// call mur detail procedure
			fileDestinationRepo.runReconDetailMur(Fromdate, "MUR");
			// call usd detail procedure
			fileDestinationRepo.runReconDetailUsd(Fromdate, "USD");
			logger.info(">>> RECON_CRREPORT_MUR_PROCEDURE executed successfully <<<");
			logger.info(">>> Recon Process Completed <<<");
			entityManager.flush();
			logger.info("Start 20");
			download = 1;
		}
		// audit start
		audit.insertAudit(userID, USERNAME, "FILE UPLOAD", "CREDIT DESTINATION FILE UPLOAD SUCCESSFULLY",
				"BRECON_CRFILE_DESTINATION_TABLE", "UPLOAD");
		logger.info("Start 21");
		resultMap.put("status", "success");
		resultMap.put("TotalSucceeded", successCount);
		resultMap.put("TotalFailed", failureCount);
		resultMap.put("TotalProcessed", totalProcessed);
		resultMap.put("Ignored", ignoreCount);
		resultMap.put("Duplicates", duplicateCount);
		resultMap.put("Download", download);
		
		System.out.println("Upload Summary -> " + "TotalSucceeded: " + successCount + ", TotalFailed: " + failureCount +",Ignored: " +ignoreCount
				+ ", TotalProcessed: " + totalProcessed + ",DuplicateExcel: " +duplicateCount);
		
		return resultMap;
	}

	private boolean isRowEmpty(Row row) {
		boolean isEmpty = true;
		DataFormatter dataFormatter = new DataFormatter();

		if (row != null) {
			for (Cell cell : row) {
				if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
					isEmpty = false;
					break;
				}
			}
		}
		return isEmpty;
	}

	public void CrRefershMethod(String userID, String username) {
		// start procedure
		Date utilDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = sdf.format(utilDate);
		reconProRepo.insertValueReconTb(userID, formattedDate);
		entityManager.flush();
		fileDestinationRepo.UpdateValueDestTb(formattedDate);
		entityManager.flush();
		fileDestinationRepo.UpdateValueSourTb(formattedDate);
		entityManager.flush();
		entityManager.clear();

		boolean hasY = fileSourceRepo.existsAnyY() > 0;

		if (!hasY) {
			System.out.println(">>> NOT GENERATE RECON PROCEDURE <<<");
		} else {
			// Call USD procedure
			fileDestinationRepo.runReconUsd(formattedDate, "USD");
			System.out.println(">>> RECON_CRREPORT_USD_PROCEDURE executed successfully <<<");
			// Call MUR procedure
			fileDestinationRepo.runReconMur(formattedDate, "MUR");
			System.out.println(">>> RECON_CRREPORT_MUR_PROCEDURE executed successfully <<<");
			System.out.println(">>> Recon Process Completed <<<");
			entityManager.flush();
		}

		RECON_REPORTMUR_REPO.flush();

		RECON_CRREPORT_MUR_ENTITY mur = RECON_REPORTMUR_REPO.getReconDate(formattedDate, "MUR");
		mur.setEntry_user(username);
		RECON_REPORTMUR_REPO.save(mur);

		RECON_REPORTUSD_REPO.flush();

		RECON_CRREPORT_USD_ENTITY usd = RECON_REPORTUSD_REPO.getReconDate(formattedDate, "USD");
		mur.setEntry_user(username);
		RECON_REPORTUSD_REPO.save(usd);

		// audit start
		audit.insertAudit(userID, username, "RECON PROCESS", "Data synced successfully!..", "BRECON_CRMAIN_TABLE",
				"REFERSH");
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

}
