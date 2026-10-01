package com.bornfire.recon.services.upload;


import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_CRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_DRFILE_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_DRFILE_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRFILE_SOURCE_REPO;
import com.bornfire.recon.entities.upload.RECON_PRINT_ENQUIRY_ENTITY;
import com.bornfire.recon.entities.upload.RECON_PRINT_ENQUIRY_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_REPO;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Service
@ConfigurationProperties("output")
@Transactional
public class DownloadService {
	private static final Logger logger = Logger.getLogger(DownloadService.class.getName());

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment env;

	@Autowired
	RECON_CRFILE_SOURCE_REPO file_Source_Repo;

	@Autowired
	RECON_CRFILE_DESTINATION_REPO File_Destination_Repo;

	@Autowired
	RECON_DRFILE_DESTINATION_REPO RECON_DRFILE_DESTINATION_REPO;

	@Autowired
	RECON_DRFILE_SOURCE_REPO RECON_DRFILE_SOURCE_REPO;

	@Autowired
	BRECON_AUDIT_REPO BRECON_Audit_Rep;

	@Autowired
	RECON_PRINT_ENQUIRY_REPO RECON_PRINT_ENQUIRY_REPO;

	@Autowired
	RECON_UPI_SOURCE_REPO RECON_UPI_SOURCE_REPO;

	@Autowired
	RECON_UPI_DESTINATION_REPO RECON_UPI_DESTINATION_REPO;

	@Autowired
	DateParser DateParser;

	public void exportCurrencyReport(String currency, String reconDate, String type, HttpServletResponse response,
			String report) {
		try (Connection conn = dataSource.getConnection()) {
			logger.info("Download currency report start 1");

			// Null-safe: default report if null
			if (report == null) {
				report = "DEFAULT";
			}

			// Get report configuration
			ReportConfig config = getReportConfig(currency, reconDate, type, report);
			logger.info("Download currency report start 2");

			List<String> sheetNames;
			List<JasperPrint> jasperPrintList = new ArrayList<>();

			// Null-safe equals check
			if ("IB".equalsIgnoreCase(report)) {
				sheetNames = Arrays.asList("CHF", "MUR", "EUR", "GBP", "EUR 1", "ZAR", "USD");
			} else if ("LOCAL".equalsIgnoreCase(report)) {
				sheetNames = Arrays.asList("EUR 1", "EUR", "MUR", "USD","USD 1");
			} else {
				sheetNames = Arrays.asList("USD IB", "MUR L", "MUR1 L", "EUR L", "EUR1 L");
			}

			if (config.jasperFiles != null && !config.jasperFiles.isEmpty()) {
				for (int i = 0; i < config.jasperFiles.size(); i++) {
					InputStream jf = config.jasperFiles.get(i);
					JasperReport jasperReport = JasperCompileManager.compileReport(jf);
					JasperPrint jp = JasperFillManager.fillReport(jasperReport, config.params, conn);

					// set sheet name using index
//					if (i < sheetNames.size()) {
//						jp.setName(sheetNames.get(i));
//					}
					jasperPrintList.add(jp);
				}
			} else {
				// SINGLE jasper file case
				JasperReport jasperReport = JasperCompileManager.compileReport(config.jasperFile);
				JasperPrint jp = JasperFillManager.fillReport(jasperReport, config.params, conn);
				//jp.setName(jasperReport.getName());
				jasperPrintList.add(jp);
			}

			logger.info("Download currency report start 3");

			// Configure response headers
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=" + config.fileName);

			// Export to XLSX
			JRXlsxExporter exporter = new JRXlsxExporter();
			exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrintList));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

			SimpleXlsxReportConfiguration xlsxConfig = new SimpleXlsxReportConfiguration();
			xlsxConfig.setDetectCellType(true);
			xlsxConfig.setCollapseRowSpan(false);
			xlsxConfig.setOnePagePerSheet(false);
			xlsxConfig.setWhitePageBackground(false);

//			if ("MARKUP".equalsIgnoreCase(type)) {
//				xlsxConfig.setSheetNames(sheetNames.toArray(new String[0]));
//			}

			exporter.setConfiguration(xlsxConfig);
			exporter.exportReport();

			logger.info("Download currency report finished");

		} catch (Exception e) {
			logger.info("Error generating report");
			throw new RuntimeException("Error generating report", e);
		}
	}

	// Helper method: Only decides files, params, filename
	private ReportConfig getReportConfig(String currency, String reconDate, String type, String report) {
		InputStream jasperFile = null;
		List<InputStream> jasperFiles = null;
		Map<String, Object> params = new HashMap<>();
		String fileName = null;

		String cur = currency.toLowerCase();
		String rptType = type.toUpperCase();
		switch (cur) {
		case "usd":
			if (rptType.equals("CREDIT") || rptType.equalsIgnoreCase("CR")) {
				if (report.equals("SUMMARY")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/CR_REPORT_USD.jrxml");
				} else if (report.equals("DETAIL")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/CRDETAIL_REPORT_USD.jrxml");
				}
			} else if (rptType.equals("UPI")) {
				if (report.equals("SUMMARY")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/UPI_REPORT_USD.jrxml");
				} else if (report.equals("DETAIL")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/UPIDETAIL_REPORT.jrxml");
				}
			} else if (rptType.equals("DEBIT") || rptType.equalsIgnoreCase("DR")) {
				if (report.equals("SUMMARY")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/DR_REPORT_USD.jrxml");
				} else if (report.equals("DETAIL")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/DRDETAIL_REPORT.jrxml");
				}
			}
			params.put("CURR", "USD");
			params.put("DATE", reconDate);
			if (report.equals("SUMMARY")) {
			fileName = report + "_Report_Usd_" + rptType + "_" + reconDate.replace("-", "") + ".xlsx";
			} else if (report.equals("DETAIL")) {
				fileName = "CDSS_Report_Usd_" + rptType + "_" + reconDate.replace("-", "") + ".xlsx";			}
		
			break;

		case "mur":
			if (rptType.equals("CREDIT") || rptType.equalsIgnoreCase("CR")) {
				if (report.equals("SUMMARY")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/CR_REPORT_MUR.jrxml");
				} else if (report.equals("DETAIL")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/CRDETAIL_REPORT_MUR.jrxml");
				}
			} else if (rptType.equals("UPI")) {
				if (report.equals("SUMMARY")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/UPI_REPORT_MUR.jrxml");
				} else if (report.equals("DETAIL")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/UPIDETAIL_REPORT.jrxml");
				}
			} else if (rptType.equals("DEBIT") || rptType.equalsIgnoreCase("DR")) {
				if (report.equals("SUMMARY")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/DR_REPORT_MUR.jrxml");
				} else if (report.equals("DETAIL")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/DRDETAIL_REPORT.jrxml");
				}
			}
			params.put("CURR", "MUR");
			params.put("DATE", reconDate);
			if (report.equals("SUMMARY")) {
				fileName = report + "_Report_Mur_" + rptType + "_" + reconDate.replace("-", "") + ".xlsx";
				} else if (report.equals("DETAIL")) {
					fileName = "CDSS_Report_Mur_" + rptType + "_" + reconDate.replace("-", "") + ".xlsx";			}
			
			break;
		case "markup":
			DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			String formattedDate = LocalDate.parse(reconDate, inFmt).format(outFmt);
			if (rptType.equals("UPI")  ) {
				System.out.println(rptType);
			jasperFiles = Arrays.asList(this.getClass().getResourceAsStream("/static/jasper/DebitReport/RateMaintenance.jrxml")
					,
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_USD_LOCAL.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_MUR_LOCAL.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_MUR_LOCAL1.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_EUR_LOCAL1.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_EUR_LOCAL.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_USD_LOCAL1.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_USD_IB1.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_ZAR_IB.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_MUR_IB.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_EUR_IB.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_USD_IB.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_CHF_IB.jrxml"),
					this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_UPI_GBP_IB.jrxml")
			      );
			
			params.put("REPORT_DATE", formattedDate); // "09-09-2025"
			fileName =  "Report_Markup_" + rptType + "_" + formattedDate + ".xlsx";
			}if (rptType.equals("DEBIT") || rptType.equalsIgnoreCase("DR") ) {
				
				if (report.equals("LOCAL") ) {
					
					/*
					 * jasperFiles = Arrays.asList(this.getClass().
					 * getResourceAsStream("/static/jasper/MARKUP-LOCAL (CAD).jrxml"),
					 * this.getClass().getResourceAsStream("/static/jasper/MARKUP-LOCAL (EUR).jrxml"
					 * ),
					 * this.getClass().getResourceAsStream("/static/jasper/MARKUP-LOCAL (MUR).jrxml"
					 * ),
					 * this.getClass().getResourceAsStream("/static/jasper/MARKUP-LOCAL (USD).jrxml"
					 * ) );
					 */
					
					jasperFiles = Arrays.asList(this.getClass().getResourceAsStream("/static/jasper/DebitReport/RateMaintenance.jrxml"),
							this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_EUR_LOCAL.jrxml"),
							this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_EUR_LOCAL1.jrxml"),
							this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_MUR_LOCAL.jrxml"),
							this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_USD_LOCAL.jrxml"),
							this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_USD_LOCAL1.jrxml"),
							this.getClass().getResourceAsStream("/static/jasper/UPIReport/MARKUP_DR_MUR_LOCAL1.jrxml")
					      );
					
					    params.put("REPORT_DATE", formattedDate);
					    fileName =  "Report_Markup_Local_" + rptType + "_" + formattedDate + ".xlsx";
					    
					} else if  (report.equalsIgnoreCase("IB")) {
						
						/*jasperFiles = Arrays.asList(this.getClass().getResourceAsStream("/static/jasper/MARKUP-IB (CHF).jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/MARKUP-IB (MUR).jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/MARKUP-IB (EUR).jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/MARKUP-IB (GBP).jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/MARKUP-IB (EUR 1).jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/MARKUP-IB (ZAR).jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/MARKUP-IB (USD).jrxml")
								
						      );*/
						
						jasperFiles = Arrays.asList(this.getClass().getResourceAsStream("/static/jasper/DebitReport/RateMaintenance.jrxml")
								,
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_CHF_IB.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_ZAR_IB.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_EUR_IB.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_EUR_IB1.jrxml"),

								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_USD_IB.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_USD_IB1.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_MUR_IB.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_GBP_IB.jrxml"),

								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_MUR_IB1.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_AED_IB.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_CAD_IB.jrxml"),
								this.getClass().getResourceAsStream("/static/jasper/DebitReport/MARKUP_DR_AUD_IB.jrxml")
//								
						      );
						
						    params.put("REPORT_DATE", formattedDate);
						    fileName =  "Report_Markup_IB_" + rptType + "_" + formattedDate + ".xlsx";
					}
				}

			
			break;
		default:
			throw new IllegalArgumentException("Unsupported currency: " + currency);
		}

		return new ReportConfig(jasperFile, jasperFiles, params, fileName);
	}

	// Holder class for config
	class ReportConfig {
		InputStream jasperFile;
		List<InputStream> jasperFiles;
		Map<String, Object> params;
		String fileName;

		ReportConfig(InputStream jasperFile, List<InputStream> jasperFiles, Map<String, Object> params,
				String fileName) {
			this.jasperFile = jasperFile;
			this.jasperFiles = jasperFiles;
			this.params = params;
			this.fileName = fileName;
		}
	}

	public void CrExportExcel(String type, String userID, String userName, String auditRefNo,
			HttpServletResponse response) {

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Data");
			int rowIdx = 0;

			if ("source".equalsIgnoreCase(type)) {
				List<RECON_CRFILE_SOURCE_ENTITY> dataList = file_Source_Repo.findAll();

				// Header
				Row header = sheet.createRow(rowIdx++);
				String[] headers = { "ARN", "TR_HIS_ID", "TRXN_DATE", "TRXN_TIME", "POSTING_DATE", "CARD_NUMBER",
						"ABS_ACCT_NUMBER", "ACCT_CURRENCY", "SOURCE_CURR", "SOURCE_AMT", "BILL_CURR", "MC_BILL_AMT",
						"SV_BILL_AMT", "SETT_CURR", "SETT_AMT", "MARKUP_AMNT_ICCR", "TRANSACTION_IND",
						"TRANSACTION_FEE", "I_FEE_IND", "SIGN_IFEE", "TERM_TYPE", "TRANSACTION_TYPE",
						"TRANSACTION_DESCRIPTION", "TYPE_OF_CARD", "MERCHANT_NAME", "MERCHANT_COUNTRY" };
				for (int i = 0; i < headers.length; i++) {
					header.createCell(i).setCellValue(headers[i]);
				}

				for (RECON_CRFILE_SOURCE_ENTITY row : dataList) {
					Row excelRow = sheet.createRow(rowIdx++);
					excelRow.createCell(0).setCellValue(row.getArn());
					excelRow.createCell(1).setCellValue(row.getTr_his_id());
					excelRow.createCell(2).setCellValue(DateParser.getCurrentDateWithoutTimePass(row.getTrxn_date()));
					excelRow.createCell(3).setCellValue(row.getTrxn_time());
					excelRow.createCell(4)
							.setCellValue(DateParser.getCurrentDateWithoutTimePass(row.getPosting_date()));
					excelRow.createCell(5).setCellValue(row.getCard_number());
					excelRow.createCell(6).setCellValue(row.getAbs_acct_number());
					excelRow.createCell(7).setCellValue(row.getAcct_currency());
					excelRow.createCell(8).setCellValue(row.getSource_curr());
					excelRow.createCell(9).setCellValue(String.valueOf(row.getSource_amt()));
					excelRow.createCell(10).setCellValue(row.getBill_curr());
					excelRow.createCell(11).setCellValue(String.valueOf(row.getMc_bill_amt()));
					excelRow.createCell(12).setCellValue(String.valueOf(row.getSv_bill_amt()));
					excelRow.createCell(13).setCellValue(row.getSett_curr());
					excelRow.createCell(14).setCellValue(String.valueOf(row.getSett_amt()));
					excelRow.createCell(15).setCellValue(String.valueOf(row.getMarkup_amnt_iccr()));
					excelRow.createCell(16).setCellValue(row.getTransaction_ind());
					excelRow.createCell(17).setCellValue(String.valueOf(row.getTransaction_fee()));
					excelRow.createCell(18).setCellValue(row.getI_fee_ind());
					excelRow.createCell(19).setCellValue(String.valueOf(row.getSign_ifee()));
					excelRow.createCell(20).setCellValue(row.getTerm_type());
					excelRow.createCell(21).setCellValue(row.getTransaction_type());
					excelRow.createCell(22).setCellValue(row.getTransaction_description());
					excelRow.createCell(23).setCellValue(row.getType_of_card());
					excelRow.createCell(24).setCellValue(row.getMerchant_name());
					excelRow.createCell(25).setCellValue(row.getMerchant_country());
				}

				saveAudit(userID, userName, "Credit Source File Download!", "BRECON_CRFILE_SOURCE_TABLE", auditRefNo);
				response.setHeader("Content-Disposition", "inline; filename=source_data.xlsx");

			} else if ("destination".equalsIgnoreCase(type)) {
				List<RECON_CRFILE_DESTINATION_ENTITY> dataList = File_Destination_Repo.findAll();

				Row header = sheet.createRow(rowIdx++);
				String[] headers = { "BO_UTRNNO", "TAA_DATE", "TRXN_DATE", "TRXN_TIME", "POST_DATE", "CARD_NUMBER",
						"CCARD_NUM", "TRXN_TYPE", "ACCT_CURRENCY", "SOURCE_CURR", "SOURCE_AMT", "SETT_CURR", "SETT_AMT",
						"BILL_CURR", "MC_BILL_AMT", "SV_BILL_AMT", "TRXN_FEE", "TRANSACTION_IND", "I_FEE", "I_FEE_IND",
						"AUTH_CODE", "MERCHANT_NAME", "EXCEPTION", "ARN", "CYCLE_NUMBER", "MCC", "COUNTRY", "REFNUM" };
				for (int i = 0; i < headers.length; i++) {
					header.createCell(i).setCellValue(headers[i]);
				}

				for (RECON_CRFILE_DESTINATION_ENTITY row : dataList) {
					Row excelRow = sheet.createRow(rowIdx++);
					excelRow.createCell(0).setCellValue(row.getBo_utrnno());
					excelRow.createCell(1).setCellValue(row.getTaa_date());
					excelRow.createCell(2).setCellValue(row.getTrxn_date());
					excelRow.createCell(3).setCellValue(row.getTrxn_time());
					excelRow.createCell(4).setCellValue(row.getPost_date());
					excelRow.createCell(5).setCellValue(row.getCard_number());
					excelRow.createCell(6).setCellValue(row.getCcard_num());
					excelRow.createCell(7).setCellValue(row.getTrxn_type());
					excelRow.createCell(8).setCellValue(row.getAcct_currency());
					excelRow.createCell(9).setCellValue(row.getSource_curr());
					excelRow.createCell(10).setCellValue(String.valueOf(row.getSource_amt()));
					excelRow.createCell(11).setCellValue(row.getSett_curr());
					excelRow.createCell(12).setCellValue(String.valueOf(row.getSett_amt()));
					excelRow.createCell(13).setCellValue(row.getBill_curr());
					excelRow.createCell(14).setCellValue(String.valueOf(row.getMc_bill_amt()));
					excelRow.createCell(15).setCellValue(String.valueOf(row.getSv_bill_amt()));
					excelRow.createCell(16).setCellValue(String.valueOf(row.getTrxn_fee()));
					excelRow.createCell(17).setCellValue(row.getTransaction_ind());
					excelRow.createCell(18).setCellValue(String.valueOf(row.getI_fee()));
					excelRow.createCell(19).setCellValue(row.getI_fee_ind());
					excelRow.createCell(20).setCellValue(row.getAuth_code());
					excelRow.createCell(21).setCellValue(row.getMerchant_name());
					excelRow.createCell(22).setCellValue(row.getException());
					excelRow.createCell(23).setCellValue(row.getArn());
					excelRow.createCell(24).setCellValue(row.getCycle_number());
					excelRow.createCell(25).setCellValue(row.getMcc());
					excelRow.createCell(26).setCellValue(row.getCountry());
					excelRow.createCell(27).setCellValue(row.getRefnum());
				}

				saveAudit(userID, userName, "Credit Destination File Download!", "BRECON_CRFILE_DESTINATION_TABLE",
						auditRefNo);
				response.setHeader("Content-Disposition", "inline; filename=destination_data.xlsx");

			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid type parameter");
				return;
			}

			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			workbook.write(response.getOutputStream());

		} catch (Exception e) {
			try {
				if (!response.isCommitted()) {
					response.reset();
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.setContentType("text/plain");
					response.getWriter().write("Error generating Excel: " + e.getMessage());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void saveAudit(String userID, String userName, String remarks, String table, String refNo) {
		BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
		audit.setAudit_date(new Date());
		audit.setEntry_time(new Date());
		audit.setEntry_user(userID);
		audit.setFunc_code("DOWNLOAD");
		audit.setRemarks(remarks);
		audit.setAudit_table(table);
		audit.setAudit_screen("UPLOAD");
		audit.setEvent_id(userID);
		audit.setEvent_name(userName);
		audit.setModi_details("-");
		audit.setAudit_ref_no(refNo);
		BRECON_Audit_Rep.save(audit);
	}

	public void GenerateList(String userID, String userName) {
		logger.info("Dowload currency report start 11");
		RECON_PRINT_ENQUIRY_ENTITY print = new RECON_PRINT_ENQUIRY_ENTITY();
		print.setRpt_code("CREDIT");
		print.setRpt_name("CREDIT USD REPORT");
		print.setRpt_location("C:\\Users\\Admin\\Downloads");
		print.setRpt_date(new Date());
		print.setGen_by(userID);
		print.setGen_date(new Date());
		print.setPrint_date(new Date());
		print.setPrint_by(userID);
		print.setDel_flg("N");
		print.setEntry_time(new Date());
		print.setEntry_user(userID);
		logger.info("Dowload currency report start 12");
		print.setSrl_no(RECON_PRINT_ENQUIRY_REPO.getSeq());
		logger.info("Dowload currency report start 13");
		RECON_PRINT_ENQUIRY_REPO.save(print);
		logger.info("Dowload currency report start 14");

	}

	public void DrExportExcel(String type, String userID, String userName, String auditRefNo,
			HttpServletResponse response) {

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Data");
			int rowIdx = 0;

			if ("source".equalsIgnoreCase(type)) {
				List<RECON_DRFILE_SOURCE_ENTITY> dataList = RECON_DRFILE_SOURCE_REPO.findAll();

				// Header
				Row header = sheet.createRow(rowIdx++);
				String[] headers = { "TRN_DT", "VALUE_DT", "TRN_REF", "CTRY", "RELATED_ACCOUNT", "TRN_DESC",
						"ADDL_TEXT", "AC_CCY", "DR_ORG", "CR_ORG", };
				for (int i = 0; i < headers.length; i++) {
					header.createCell(i).setCellValue(headers[i]);
				}

				for (RECON_DRFILE_SOURCE_ENTITY row : dataList) {
					Row excelRow = sheet.createRow(rowIdx++);
					excelRow.createCell(0).setCellValue(DateParser.getCurrentDateWithoutTimePass(row.getTrn_dt()));
					excelRow.createCell(1).setCellValue(DateParser.getCurrentDateWithoutTimePass(row.getValue_dt()));
					excelRow.createCell(2).setCellValue(row.getTrn_ref());
					excelRow.createCell(3).setCellValue(row.getCtry());
					excelRow.createCell(4).setCellValue(row.getRelated_account());
					excelRow.createCell(5).setCellValue(row.getTrn_desc());
					excelRow.createCell(6).setCellValue(row.getAddl_text());
					excelRow.createCell(7).setCellValue(row.getAc_ccy());
					excelRow.createCell(8).setCellValue(row.getDr_org());
					excelRow.createCell(9).setCellValue(row.getCr_org());
				}

				saveAudit(userID, userName, "Debit Source File Download!", "BRECON_DRFILE_SOURCE_TABLE", auditRefNo);
				response.setHeader("Content-Disposition", "inline; filename=source_data.xlsx");

			} else if ("destination".equalsIgnoreCase(type)) {
				List<RECON_DRFILE_DESTINATION_ENTITY> dataList = RECON_DRFILE_DESTINATION_REPO.findAll();

				Row header = sheet.createRow(rowIdx++);
				String[] headers = { "ARN", "MCC", "AUTH_AMNT", "ACCT_AMNT", "AUTH_CURRENCY", "TRANSACTION_DATE",
						"TRANSACTION_AMOUNT", "TRANSACTION_CURRENCY", "TRANSACTION_TYPE", "CBS_ACCOUNT_NUMBER",
						"CARD_NUMBER", "CARD_TYPE", "MERCHANT_DESC", "COUNTRY_NAME", "APPROVAL_CODE",
						"RECONCILE_AMOUNT", "RECONCILE_CUR", "FEES", "I_FEE_IND", "REFNUM", "DR_CR", "MARKUP_VALUE",
						"MARKUP_CURRENCY" };
				for (int i = 0; i < headers.length; i++) {
					header.createCell(i).setCellValue(headers[i]);
				}

				for (RECON_DRFILE_DESTINATION_ENTITY row : dataList) {
					Row excelRow = sheet.createRow(rowIdx++);
					excelRow.createCell(0).setCellValue(row.getArn());
					excelRow.createCell(1).setCellValue(row.getMcc());
					excelRow.createCell(2).setCellValue(String.valueOf(row.getAuth_amnt()));
					excelRow.createCell(3).setCellValue(String.valueOf(row.getAcct_amnt()));
					excelRow.createCell(4).setCellValue(row.getAuth_currency());
					excelRow.createCell(5).setCellValue(DateParser.getFormatdd_mm_yyyy(row.getTransaction_date()));
					excelRow.createCell(6).setCellValue(String.valueOf(row.getTransaction_amount()));
					excelRow.createCell(7).setCellValue(row.getTransaction_currency());
					excelRow.createCell(8).setCellValue(row.getTransaction_type());
					excelRow.createCell(9).setCellValue(row.getCbs_account_number());
					excelRow.createCell(10).setCellValue(row.getCard_number());
					excelRow.createCell(11).setCellValue(row.getCard_type());
					excelRow.createCell(12).setCellValue(row.getMerchant_desc());
					excelRow.createCell(13).setCellValue(row.getCountry_name());
					excelRow.createCell(14).setCellValue(row.getApproval_code());
					excelRow.createCell(15).setCellValue(String.valueOf(row.getReconcile_amount()));
					excelRow.createCell(16).setCellValue(row.getReconcile_cur());
					excelRow.createCell(17).setCellValue(String.valueOf(row.getFees()));
					excelRow.createCell(18).setCellValue(row.getI_fee_ind());
					excelRow.createCell(19).setCellValue(row.getRefnum());
					excelRow.createCell(20).setCellValue(row.getDr_cr());
					excelRow.createCell(21).setCellValue(String.valueOf(row.getMarkup_value()));
					excelRow.createCell(22).setCellValue(row.getMarkup_currency());
				}

				saveAudit(userID, userName, "Debit Destination File Download!", "BRECON_DRFILE_DESTINATION_TABLE",
						auditRefNo);
				response.setHeader("Content-Disposition", "inline; filename=destination_data.xlsx");

			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid type parameter");
				return;
			}

			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			workbook.write(response.getOutputStream());

		} catch (Exception e) {
			try {
				if (!response.isCommitted()) {
					response.reset();
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.setContentType("text/plain");
					response.getWriter().write("Error generating Excel: " + e.getMessage());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void UpiExportExcel(String type, String userID, String userName, String auditRefNo,
			HttpServletResponse response) {

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Data");
			int rowIdx = 0;

			if ("source".equalsIgnoreCase(type)) {
				List<RECON_UPI_SOURCE_ENTITY> dataList = RECON_UPI_SOURCE_REPO.findAll();

// Header
				Row header = sheet.createRow(rowIdx++);
				String[] headers = { "TRN_DT", "VALUE_DT", "REF", "CTRY", "TRN_DESC", "TRN_TYPE", "ADDL_TEXT", "ADDL2",
						"ADDL3", "AC_CCY", "DR_MOV", "CR_MOV" };
				for (int i = 0; i < headers.length; i++) {
					header.createCell(i).setCellValue(headers[i]);
				}

				for (RECON_UPI_SOURCE_ENTITY row : dataList) {
					Row excelRow = sheet.createRow(rowIdx++);
					excelRow.createCell(0).setCellValue(DateParser.getCurrentDateWithoutTimePass(row.getTrn_dt()));
					excelRow.createCell(1).setCellValue(DateParser.getCurrentDateWithoutTimePass(row.getValue_dt()));
					excelRow.createCell(2).setCellValue(row.getRef());
					excelRow.createCell(3).setCellValue(row.getCtry());
					excelRow.createCell(4).setCellValue(row.getTrn_desc());
					excelRow.createCell(5).setCellValue(row.getTrn_type());
					excelRow.createCell(6).setCellValue(row.getAddl_text());
					excelRow.createCell(7).setCellValue(row.getAddl2());
					excelRow.createCell(8).setCellValue(row.getAddl3());
					excelRow.createCell(9).setCellValue(row.getAc_ccy());
					excelRow.createCell(10).setCellValue(String.valueOf(row.getDr_mov()));
					excelRow.createCell(11).setCellValue(String.valueOf(row.getCr_mov()));
				}

				saveAudit(userID, userName, "UPI Debit Source File Download!", "BRECON_UPI_SOURCE_TABLE", auditRefNo);
				response.setHeader("Content-Disposition", "inline; filename=source_data.xlsx");

			} else if ("destination".equalsIgnoreCase(type)) {
				List<RECON_UPI_DESTINATION_ENTITY> dataList = RECON_UPI_DESTINATION_REPO.findAll();

// Header
				Row header = sheet.createRow(rowIdx++);
				String[] headers = { "STTL_DATE", "ARN", "MCC", "AUTH_AMNT", "ACCT_AMNT", "AUTH_CURRENCY",
						"TRANSACTION_DATE", "TRANSACTION_AMOUNT", "TRANSACTION_CURRENCY", "TRANSACTION_TYPE",
						"CBS_ACC_NO", "CARD_NUMBER", "CARD_TYPE", "MERCHANT_DESC", "TRN_COUNTRY", "APPROVAL_CODE",
						"RECONCILE_AMOUNT", "RECONCILE_CUR", "INTERCHANGE_FEES", "SERVICE_FEE", "SERVICE_IND",
						"I_FEE_IND", "REFNUM", "DR_CR", "MARKUP_VALUE", "MARKUP_CURRENCY" };

				for (int i = 0; i < headers.length; i++) {
					header.createCell(i).setCellValue(headers[i]);
				}

				for (RECON_UPI_DESTINATION_ENTITY row : dataList) {
					Row excelRow = sheet.createRow(rowIdx++);
					excelRow.createCell(0).setCellValue(row.getSttl_date());
					excelRow.createCell(1).setCellValue(row.getArn());
					excelRow.createCell(2).setCellValue(row.getMcc());
					excelRow.createCell(3).setCellValue(String.valueOf(row.getAuth_amnt()));
					excelRow.createCell(4).setCellValue(String.valueOf(row.getAcct_amnt()));
					excelRow.createCell(5).setCellValue(row.getAuth_currency());
					excelRow.createCell(6).setCellValue(row.getTransaction_date());
					excelRow.createCell(10).setCellValue(String.valueOf(row.getTransaction_amount()));
					excelRow.createCell(11).setCellValue(row.getTransaction_currency());
					excelRow.createCell(12).setCellValue(row.getTransaction_type());
					excelRow.createCell(13).setCellValue(row.getCbs_acc_no());
					excelRow.createCell(15).setCellValue(row.getCard_number());
					excelRow.createCell(16).setCellValue(row.getCard_type());
					excelRow.createCell(17).setCellValue(row.getMerchant_desc());
					excelRow.createCell(18).setCellValue(row.getTrn_country());
					excelRow.createCell(19).setCellValue(row.getApproval_code());
					excelRow.createCell(8).setCellValue(String.valueOf(row.getReconcile_amount()));
					excelRow.createCell(9).setCellValue(row.getReconcile_cur());
					excelRow.createCell(20).setCellValue(String.valueOf(row.getInterchange_fees()));
					excelRow.createCell(21).setCellValue(String.valueOf(row.getService_fee()));
					excelRow.createCell(22).setCellValue(row.getService_ind());
					excelRow.createCell(23).setCellValue(row.getI_fee_ind());
					excelRow.createCell(7).setCellValue(row.getRefnum());
					excelRow.createCell(24).setCellValue(row.getDr_cr());
					excelRow.createCell(25).setCellValue(String.valueOf(row.getMarkup_value()));
					excelRow.createCell(26).setCellValue(row.getMarkup_currency());

				}

				saveAudit(userID, userName, "UPI Debit Destination File Download!", "BRECON_UPI_DESTINATION_TABLE",
						auditRefNo);
				response.setHeader("Content-Disposition", "inline; filename=destination_data.xlsx");

			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid type parameter");
				return;
			}

			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			workbook.write(response.getOutputStream());

		} catch (Exception e) {
			try {
				if (!response.isCommitted()) {
					response.reset();
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.setContentType("text/plain");
					response.getWriter().write("Error generating Excel: " + e.getMessage());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	}
