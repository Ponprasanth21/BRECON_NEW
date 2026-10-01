package com.bornfire.recon.services.upload;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.bornfire.recon.entities.upload.RECON_PRINT_ENQUIRY_ENTITY;
import com.bornfire.recon.entities.upload.RECON_PRINT_ENQUIRY_REPO;
import com.bornfire.recon.services.upload.DownloadService.ReportConfig;

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
public class DownloadServiceBulk {

	private static final Logger logger = Logger.getLogger(DownloadServiceBulk.class.getName());

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment env;
	
	
	public void exportCurrencyReport(String currency, String recon_from_date,String recon_to_date, String type, HttpServletResponse response,
			String report) {
		try (Connection conn = dataSource.getConnection()) {
			logger.info("Download currency report start 1");

			// Null-safe: default report if null
			if (report == null) {
				report = "DEFAULT";
			}

			// Get report configuration
			ReportConfig config = getReportConfig(currency,recon_from_date, recon_to_date, type, report);
			logger.info("Download currency report start 2");

			List<String> sheetNames;
			List<JasperPrint> jasperPrintList = new ArrayList<>();

		 
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
	private ReportConfig getReportConfig(String currency,String recon_from_date, String recon_to_date, String type, String report) {
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
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/BulkCredit/CR_REPORT_USD.jrxml");
				} else if (report.equals("DETAIL")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/BulkCredit/CRDETAIL_REPORT_USD.jrxml");
				}
			}
			params.put("CURR", "USD");
			params.put("FROMDATE", recon_from_date);
			params.put("TODATE", recon_to_date);
			fileName = "CDSS_Report_Usd_" + rptType + "_" + recon_to_date.replace("-", "") +".xlsx";
			break;

		case "mur":
			if (rptType.equals("CREDIT") || rptType.equalsIgnoreCase("CR")) {
				if (report.equals("SUMMARY")) {
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/BulkCredit/CR_REPORT_MUR.jrxml");
				} else if (report.equals("DETAIL")) {
					logger.info(report);
					jasperFile = this.getClass().getResourceAsStream("/static/jasper/BulkCredit/CRDETAIL_REPORT_MUR.jrxml");
				}
			} 
			params.put("CURR", "MUR");
			params.put("FROMDATE", recon_from_date);
			params.put("TODATE", recon_to_date);
			fileName = "CDSS_Report_Mur_" + rptType + "_" + recon_to_date.replace("-", "") +".xlsx";
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
	
	@Autowired
	RECON_PRINT_ENQUIRY_REPO rECON_PRINT_ENQUIRY_REPO;
	
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
		print.setSrl_no(rECON_PRINT_ENQUIRY_REPO.getSeq());
		logger.info("Dowload currency report start 13");
		rECON_PRINT_ENQUIRY_REPO.save(print);
		logger.info("Dowload currency report start 14");

	}

}
