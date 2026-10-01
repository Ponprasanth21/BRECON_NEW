package com.bornfire.recon.services.upload;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Service
@ConfigurationProperties("output")
@Transactional
public class ProcessDownloadService {
	
	public byte[] generateReconCombinedExcel(List<Object[]> destUnmatchData, List<Object[]> sourceUnmatchData,
			List<Object[]> reconMatchedData, String recon_process_date, String type) {
		try {
			JRMapCollectionDataSource destDataSource;
			JRMapCollectionDataSource sourceDataSource;
			JRMapCollectionDataSource matchedDataSource;

			InputStream matchedJrxml;
			InputStream sourceJrxml;
			InputStream destJrxml;

			String[] sheetNames;

// ✅ Conditional logic for DEBIT, CREDIT, UPI
			if ("DEBIT".equalsIgnoreCase(type)) {

				destDataSource = mapDrDestData(destUnmatchData);
				sourceDataSource = mapDrSourceData(sourceUnmatchData);
				matchedDataSource = mapDrMatchedData(reconMatchedData);

				matchedJrxml = getClass().getResourceAsStream("/static/jasper/DR_RECON_MATCHED.jrxml");
				sourceJrxml = getClass().getResourceAsStream("/static/jasper/DR_SOURCE_UNMATCH.jrxml");
				destJrxml = getClass().getResourceAsStream("/static/jasper/DR_DEST_UNMATCH.jrxml");

				sheetNames = new String[] { "DR Matched Data", "DR Source Unmatched Data",
						"DR Destination Unmatched Data" };

			} else if ("CREDIT".equalsIgnoreCase(type)) {

				destDataSource = mapCrDestData(destUnmatchData);
				sourceDataSource = mapCrSourceData(sourceUnmatchData);
				matchedDataSource = mapCrMatchedData(reconMatchedData);

				matchedJrxml = getClass().getResourceAsStream("/static/jasper/CR_RECON_MATCHED.jrxml");
				sourceJrxml = getClass().getResourceAsStream("/static/jasper/CR_SOURCE_UNMATCH.jrxml");
				destJrxml = getClass().getResourceAsStream("/static/jasper/CR_DEST_UNMATCH.jrxml");

				sheetNames = new String[] { "CR Matched Data", "CR Source Unmatched Data",
						"CR Destination Unmatched Data" };

			} else if ("UPI".equalsIgnoreCase(type)) {

				destDataSource = mapUpiDestData(destUnmatchData);
				sourceDataSource = mapUpiSourceData(sourceUnmatchData);
				matchedDataSource = mapUpiMatchedData(reconMatchedData);

				matchedJrxml = getClass().getResourceAsStream("/static/jasper/UPI_RECON_MATCHED.jrxml");
				sourceJrxml = getClass().getResourceAsStream("/static/jasper/UPI_SOURCE_UNMATCH.jrxml");
				destJrxml = getClass().getResourceAsStream("/static/jasper/UPI_DEST_UNMATCH.jrxml");

				sheetNames = new String[] { "UPI Matched Data", "UPI Source Unmatched Data",
						"UPI Destination Unmatched Data" };

			} else {
				throw new IllegalArgumentException("Invalid type specified: " + type);
			}

// ✅ Compile reports
			JasperReport matchedReport = JasperCompileManager.compileReport(matchedJrxml);
			JasperReport sourceReport = JasperCompileManager.compileReport(sourceJrxml);
			JasperReport destReport = JasperCompileManager.compileReport(destJrxml);

// ✅ Parameters
			Map<String, Object> params = new HashMap<>();
			params.put("SYS_DATE", recon_process_date);
			params.put("REPORT_TYPE", type.toUpperCase());

// ✅ Fill reports
			JasperPrint jpMatched = JasperFillManager.fillReport(matchedReport, params, matchedDataSource);
			JasperPrint jpSource = JasperFillManager.fillReport(sourceReport, params, sourceDataSource);
			JasperPrint jpDest = JasperFillManager.fillReport(destReport, params, destDataSource);

// ✅ Combine reports into Excel
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JRXlsxExporter exporter = new JRXlsxExporter();

			exporter.setExporterInput(SimpleExporterInput.getInstance(Arrays.asList(jpMatched, jpSource, jpDest)));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));

			SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
			configuration.setOnePagePerSheet(true);
			configuration.setDetectCellType(true);
			configuration.setWhitePageBackground(false);
			configuration.setCollapseRowSpan(false);
			configuration.setSheetNames(sheetNames);

			exporter.setConfiguration(configuration);
			exporter.exportReport();

			System.out.println("✅ " + type.toUpperCase() + " Combined Excel generated successfully for date: "
					+ recon_process_date);
			return baos.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	/*** Helper method to map Object[] data to JRMapCollectionDataSource ***/
	
	/******************************** Debit ************************************/
	private JRMapCollectionDataSource mapDrDestData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }

	    Collection<Map<String, ?>> mappedData = new ArrayList<>();
	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("ARN", safeToString(row[0]));
	        map.put("MCC", safeToString(row[1]));
	        map.put("AUTH_AMNT", safeToBigDecimal(row[2]));
	        map.put("ACCT_AMNT", safeToBigDecimal(row[3]));
	        map.put("AUTH_CURRENCY", safeToString(row[4]));
	        map.put("TRANSACTION_DATE", convertToDate(row[5]));
	        map.put("TRANSACTION_AMOUNT", safeToBigDecimal(row[6]));
	        map.put("TRANSACTION_CURRENCY", safeToString(row[7]));
	        map.put("TRANSACTION_TYPE", safeToString(row[8]));
	        map.put("CBS_ACCOUNT_NUMBER", safeToString(row[9]));
	        map.put("CARD_NUMBER", safeToString(row[10]));
	        map.put("CARD_TYPE", safeToString(row[11]));
	        map.put("MERCHANT_DESC", safeToString(row[12]));
	        map.put("COUNTRY_NAME", safeToString(row[13]));
	        map.put("APPROVAL_CODE", safeToString(row[14]));
	        map.put("RECONCILE_AMOUNT", safeToBigDecimal(row[15]));
	        map.put("RECONCILE_CUR", safeToString(row[16]));
	        map.put("FEES", safeToBigDecimal(row[17]));
	        map.put("I_FEE_IND", safeToString(row[18]));
	        map.put("REFNUM", safeToString(row[19]));
	        map.put("DR_CR", safeToString(row[20]));
	        map.put("MARKUP_VALUE", safeToBigDecimal(row[21]));
	        map.put("MARKUP_CURRENCY", safeToString(row[22]));
	        map.put("RECON_FLG", safeToString(row[34]));
	        map.put("RECON_TRAN_DATE", convertToDate(row[35]));
	        map.put("RECON_TYPE", safeToString(row[36]));
	        map.put("RECON_PROCESS_DATE", convertToDate(row[37]));

	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}

	private JRMapCollectionDataSource mapDrSourceData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }
	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("TRN_DT", convertToDate(row[0]));
	        map.put("VALUE_DT", convertToDate(row[1]));
	        map.put("TRN_REF", safeToString(row[2]));
	        map.put("CTRY", safeToString(row[3]));
	        map.put("RELATED_ACCOUNT", safeToString(row[4]));
	        map.put("TRN_DESC", safeToString(row[5]));
	        map.put("ADDL_TEXT", safeToString(row[6]));
	        map.put("AC_CCY", safeToString(row[7]));
	        map.put("DR_ORG", safeToString(row[8]));
	        map.put("CR_ORG", safeToString(row[9]));
	        map.put("RECON_FLG", safeToString(row[21]));
	        map.put("RECON_TRAN_DATE", convertToDate(row[22]));
	        map.put("RECON_TYPE", safeToString(row[23]));
	        map.put("RECON_PROCESS_DATE", convertToDate(row[24]));
	        map.put("REF1", safeToString(row[25]));
	        map.put("REF2", safeToString(row[26]));

	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}

	private JRMapCollectionDataSource mapDrMatchedData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }

	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        // ---- Source Columns ----
	        map.put("S_TRN_DT", convertToDate(row[0]));
	        map.put("S_VALUE_DT", convertToDate(row[1]));
	        map.put("S_TRN_REF", safeToString(row[2]));
	        map.put("S_CTRY", safeToString(row[3]));
	        map.put("S_RELATED_ACCOUNT", safeToString(row[4]));
	        map.put("S_TRN_DESC", safeToString(row[5]));
	        map.put("S_ADDL_TEXT", safeToString(row[6]));
	        map.put("S_AC_CCY", safeToString(row[7]));
	        map.put("S_DR_ORG", safeToString(row[8]));
	        map.put("S_CR_ORG", safeToString(row[9]));
	        map.put("S_RECON_TRAN_DATE", convertToDate(row[10]));
	        map.put("S_RECON_TYPE", safeToString(row[11]));
	        map.put("S_RECON_FLG", safeToString(row[12]));

	        // ---- Destination Columns ----
	        map.put("D_ARN", safeToString(row[13]));
	        map.put("D_MCC", safeToString(row[14]));
	        map.put("D_AUTH_AMNT", safeToBigDecimal(row[15]));
	        map.put("D_ACCT_AMNT", safeToBigDecimal(row[16]));
	        map.put("D_AUTH_CURRENCY", safeToString(row[17]));
	        map.put("D_TRANSACTION_DATE", convertToDate(row[18]));
	        map.put("D_TRANSACTION_AMOUNT", safeToBigDecimal(row[19]));
	        map.put("D_TRANSACTION_CURRENCY", safeToString(row[20]));
	        map.put("D_TRANSACTION_TYPE", safeToString(row[21]));
	        map.put("D_CBS_ACCOUNT_NUMBER", safeToString(row[22]));
	        map.put("D_CARD_NUMBER", safeToString(row[23]));
	        map.put("D_CARD_TYPE", safeToString(row[24]));
	        map.put("D_MERCHANT_DESC", safeToString(row[25]));
	        map.put("D_COUNTRY_NAME", safeToString(row[26]));
	        map.put("D_APPROVAL_CODE", safeToString(row[27]));
	        map.put("D_RECONCILE_AMOUNT", safeToBigDecimal(row[28]));
	        map.put("D_RECONCILE_CUR", safeToString(row[29]));
	        map.put("D_FEES", safeToBigDecimal(row[30]));
	        map.put("D_I_FEE_IND", safeToString(row[31]));
	        map.put("D_REFNUM", safeToString(row[32]));
	        map.put("D_DR_CR", safeToString(row[33]));
	        map.put("D_MARKUP_VALUE", safeToBigDecimal(row[34]));
	        map.put("D_MARKUP_CURRENCY", safeToString(row[35]));
	        map.put("D_RECON_TRAN_DATE", convertToDate(row[36]));
	        map.put("D_RECON_TYPE", safeToString(row[37]));
	        map.put("D_RECON_FLG", safeToString(row[38]));
	        map.put("S_REF1", safeToString(row[50]));
	        map.put("S_REF2", safeToString(row[51]));
	        map.put("D_RECON_PROCESS_DATE", convertToDate(row[52]));
	        map.put("S_RECON_PROCESS_DATE", convertToDate(row[53]));

	        mappedData.add(map);
	    }

	    return new JRMapCollectionDataSource(mappedData);
	}
	
	/******************************** Credit ************************************/
	
	private JRMapCollectionDataSource mapCrDestData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }

	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("BO_UTRNNO", safeToString(row[0]));
	        map.put("TAA_DATE", safeToString(row[1]));
	        map.put("TRXN_DATE", safeToString(row[2]));
	        map.put("TRXN_TIME", safeToString(row[3]));
	        map.put("POST_DATE", safeToString(row[4]));
	        map.put("CARD_NUMBER", safeToString(row[5]));
	        map.put("TRXN_TYPE", safeToString(row[6]));
	        map.put("ACCT_CURRENCY", safeToString(row[7]));
	        map.put("SOURCE_CURR", safeToString(row[8]));
	        map.put("SOURCE_AMT", safeToBigDecimal(row[9]));
	        map.put("SETT_CURR", safeToString(row[10]));
	        map.put("SETT_AMT", safeToBigDecimal(row[11]));
	        map.put("BILL_CURR", safeToString(row[12]));
	        map.put("MC_BILL_AMT", safeToBigDecimal(row[13]));
	        map.put("SV_BILL_AMT", safeToBigDecimal(row[14]));
	        map.put("TRXN_FEE", safeToBigDecimal(row[15]));
	        map.put("TRANSACTION_IND", safeToString(row[16]));
	        map.put("I_FEE", safeToBigDecimal(row[17]));
	        map.put("I_FEE_IND", safeToString(row[18]));
	        map.put("AUTH_CODE", safeToString(row[19]));
	        map.put("MERCHANT_NAME", safeToString(row[20]));
	        map.put("EXCEPTION", safeToString(row[21]));
	        map.put("ARN", safeToString(row[22]));
	        map.put("CYCLE_NUMBER", safeToString(row[23]));
	        map.put("MCC", safeToString(row[24]));
	        map.put("COUNTRY", safeToString(row[25]));
	        map.put("REFNUM", safeToString(row[26]));
	        map.put("RECON_FLG", safeToString(row[38]));
	        map.put("RECON_TRAN_DATE", convertToDate(row[39]));
	        map.put("RECON_TYPE", safeToString(row[40]));
	        map.put("CCARD_NUM", safeToString(row[41]));
	        map.put("RECON_PROCESS_DATE", convertToDate(row[42]));

	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}

	private JRMapCollectionDataSource mapCrSourceData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }

	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("ARN", safeToString(row[0]));
	        map.put("TR_HIS_ID", safeToString(row[1]));
	        map.put("TRAN_DATE", safeToString(row[41]));       // TO_CHAR(S_TRXN_DATE, 'DD-MM-YY')
	        map.put("TRXN_TIME", safeToString(row[3]));
	        map.put("POST_DATE", safeToString(row[42]));       // TO_CHAR(S_POSTING_DATE, 'DD-MM-YY')
	        map.put("CARD_NUMBER", safeToString(row[5]));
	        map.put("ACCT_CURRENCY", safeToString(row[6]));
	        map.put("SOURCE_CURR", safeToString(row[7]));
	        map.put("SOURCE_AMT", safeToBigDecimal(row[8]));
	        map.put("BILL_CURR", safeToString(row[9]));
	        map.put("MC_BILL_AMT", safeToBigDecimal(row[10]));
	        map.put("SV_BILL_AMT", safeToBigDecimal(row[11]));
	        map.put("SETT_CURR", safeToString(row[12]));
	        map.put("SETT_AMT", safeToBigDecimal(row[13]));
	        map.put("MARKUP_AMNT_ICCR", safeToBigDecimal(row[14]));
	        map.put("TRANSACTION_IND", safeToString(row[15]));
	        map.put("TRANSACTION_FEE", safeToBigDecimal(row[16]));
	        map.put("I_FEE_IND", safeToString(row[17]));
	        map.put("SIGN_IFEE", safeToBigDecimal(row[18]));
	        map.put("TERM_TYPE", safeToString(row[19]));
	        map.put("TRANSACTION_TYPE", safeToString(row[20]));
	        map.put("TRANSACTION_DESCRIPTION", safeToString(row[21]));
	        map.put("TYPE_OF_CARD", safeToString(row[22]));
	        map.put("MERCHANT_NAME", safeToString(row[23]));
	        map.put("MERCHANT_COUNTRY", safeToString(row[24]));
	        map.put("RECON_FLG", safeToString(row[36]));
	        map.put("RECON_TRAN_DATE", convertToDate(row[37]));
	        map.put("RECON_TYPE", safeToString(row[38]));
	        map.put("ABS_ACCT_NUMBER", safeToString(row[39]));
	        map.put("RECON_PROCESS_DATE", convertToDate(row[40]));
	        
	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}

	private JRMapCollectionDataSource mapCrMatchedData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }
	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	     // ---- SOURCE FIELDS (Prefix: S_) ----
	        map.put("S_ARN", safeToString(row[0]));
	        map.put("S_TR_HIS_ID", safeToString(row[1]));
	        map.put("TRAN_DATE", safeToString(row[74])); 
	        map.put("S_TRXN_TIME", safeToString(row[3]));
	        map.put("POST_DATE", safeToString(row[75]));  
	        map.put("S_CARD_NUMBER", safeToString(row[5]));
	        map.put("S_ACCT_CURRENCY", safeToString(row[6]));
	        map.put("S_SOURCE_CURR", safeToString(row[7]));
	        map.put("S_SOURCE_AMT", safeToBigDecimal(row[8]));
	        map.put("S_BILL_CURR", safeToString(row[9]));
	        map.put("S_MC_BILL_AMT", safeToBigDecimal(row[10]));
	        map.put("S_SV_BILL_AMT", safeToBigDecimal(row[11]));
	        map.put("S_SETT_CURR", safeToString(row[12]));
	        map.put("S_SETT_AMT", safeToBigDecimal(row[13]));
	        map.put("S_MARKUP_AMNT_ICCR", safeToBigDecimal(row[14]));
	        map.put("S_TRANSACTION_IND", safeToString(row[15]));
	        map.put("S_TRANSACTION_FEE", safeToBigDecimal(row[16]));
	        map.put("S_I_FEE_IND", safeToString(row[17]));
	        map.put("S_SIGN_IFEE", safeToBigDecimal(row[18]));
	        map.put("S_TERM_TYPE", safeToString(row[19]));
	        map.put("S_TRANSACTION_TYPE", safeToString(row[20]));
	        map.put("S_TRANSACTION_DESCRIPTION", safeToString(row[21]));
	        map.put("S_TYPE_OF_CARD", safeToString(row[22]));
	        map.put("S_MERCHANT_NAME", safeToString(row[23]));
	        map.put("S_MERCHANT_COUNTRY", safeToString(row[24]));
	        map.put("S_RECON_TRAN_DATE", convertToDate(row[25]));
	        map.put("S_RECON_TYPE", safeToString(row[26]));

	        // ---- DESTINATION FIELDS (Prefix: D_) ----
	        map.put("D_BO_UTRNNO", safeToString(row[27]));
	        map.put("D_TAA_DATE", safeToString(row[28]));
	        map.put("D_TRXN_DATE", safeToString(row[29]));
	        map.put("D_TRXN_TIME", safeToString(row[30]));
	        map.put("D_POST_DATE", safeToString(row[31]));
	        map.put("D_CARD_NUMBER", safeToString(row[32]));
	        map.put("D_TRXN_TYPE", safeToString(row[33]));
	        map.put("D_ACCT_CURRENCY", safeToString(row[34]));
	        map.put("D_SOURCE_CURR", safeToString(row[35]));
	        map.put("D_SOURCE_AMT", safeToBigDecimal(row[36]));
	        map.put("D_SETT_CURR", safeToString(row[37]));
	        map.put("D_SETT_AMT", safeToBigDecimal(row[38]));
	        map.put("D_BILL_CURR", safeToString(row[39]));
	        map.put("D_MC_BILL_AMT", safeToBigDecimal(row[40]));
	        map.put("D_SV_BILL_AMT", safeToBigDecimal(row[41]));
	        map.put("D_TRXN_FEE", safeToBigDecimal(row[42]));
	        map.put("D_TRANSACTION_IND", safeToString(row[43]));
	        map.put("D_I_FEE", safeToBigDecimal(row[44]));
	        map.put("D_I_FEE_IND", safeToString(row[45]));
	        map.put("D_AUTH_CODE", safeToString(row[46]));
	        map.put("D_MERCHANT_NAME", safeToString(row[47]));
	        map.put("D_EXCEPTION", safeToString(row[48]));
	        map.put("D_ARN", safeToString(row[49]));
	        map.put("D_CYCLE_NUMBER", safeToString(row[50]));
	        map.put("D_MCC", safeToString(row[51]));
	        map.put("D_COUNTRY", safeToString(row[52]));
	        map.put("D_REFNUM", safeToString(row[53]));
	        map.put("D_RECON_TRAN_DATE", convertToDate(row[54]));
	        map.put("D_RECON_TYPE", safeToString(row[55]));

	        // ---- META & FLAGS ----
	        map.put("D_RECON_FLG", safeToString(row[68]));
	        map.put("S_PROCESS_DATE", convertToDate(row[69]));
	        map.put("D_CCARD_NUM", safeToString(row[70]));
	        map.put("S_ABS_ACCT_NUMBER", safeToString(row[71]));
	        map.put("D_RECON_PROCESS_DATE", convertToDate(row[72]));
	        map.put("S_RECON_PROCESS_DATE", convertToDate(row[73]));

	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}
	
	/******************************** Upi Debit ************************************/
	
	private JRMapCollectionDataSource mapUpiDestData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }

	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("STTL_DATE", safeToString(row[0]));
	        map.put("ARN", safeToString(row[1]));
	        map.put("MCC", safeToString(row[2]));
	        map.put("AUTH_AMNT", safeToBigDecimal(row[3]));
	        map.put("ACCT_AMNT", safeToBigDecimal(row[4]));
	        map.put("AUTH_CURRENCY", safeToString(row[5]));
	        map.put("TRANSACTION_DATE", safeToString(row[6]));
	        map.put("REFNUM", safeToString(row[7]));
	        map.put("RECONCILE_AMOUNT", safeToBigDecimal(row[8]));
	        map.put("RECONCILE_CUR", safeToString(row[9]));
	        map.put("TRANSACTION_AMOUNT", safeToBigDecimal(row[10]));
	        map.put("TRANSACTION_CURRENCY", safeToString(row[11]));
	        map.put("TRANSACTION_TYPE", safeToString(row[12]));
	        map.put("CBS_ACC_NO", safeToString(row[13]));
	        map.put("CARD_NUMBER", safeToString(row[14]));
	        map.put("CARD_TYPE", safeToString(row[15]));
	        map.put("MERCHANT_DESC", safeToString(row[16]));
	        map.put("TRN_COUNTRY", safeToString(row[17]));
	        map.put("APPROVAL_CODE", safeToString(row[18]));
	        map.put("INTERCHANGE_FEES", safeToBigDecimal(row[19]));
	        map.put("SERVICE_FEE", safeToBigDecimal(row[20]));
	        map.put("SERVICE_IND", safeToString(row[21]));
	        map.put("I_FEE_IND", safeToString(row[22]));
	        map.put("DR_CR", safeToString(row[23]));
	        map.put("MARKUP_VALUE", safeToBigDecimal(row[24]));
	        map.put("MARKUP_CURRENCY", safeToString(row[25]));
	        map.put("RECON_FLG", safeToString(row[37]));
	        map.put("RECON_TRAN_DATE", convertToDate(row[38]));
	        map.put("RECON_TYPE", safeToString(row[39]));
	        map.put("RECON_PROCESS_DATE", convertToDate(row[40]));

	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}
	
	private JRMapCollectionDataSource mapUpiSourceData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }
	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("TRN_DT", convertToDate(row[0]));
	        map.put("VALUE_DT", convertToDate(row[1]));
	        map.put("CTRY", safeToString(row[2]));
	        map.put("REF", safeToString(row[3]));
	        map.put("TRN_DESC", safeToString(row[4]));
	        map.put("ADDL2", safeToString(row[5]));
	        map.put("ADDL3", safeToString(row[6]));
	        map.put("AC_CCY", safeToString(row[7]));
	        map.put("DR_MOV", safeToBigDecimal(row[8]));
	        map.put("CR_MOV", safeToBigDecimal(row[9]));
	        map.put("RECON_FLG", safeToString(row[21]));
	        map.put("RECON_TRAN_DATE", convertToDate(row[22]));
	        map.put("RECON_TYPE", safeToString(row[23]));
	        map.put("RECON_PROCESS_DATE", convertToDate(row[24]));
	        map.put("TRN_TYPE", safeToString(row[25]));
	        map.put("ADDL_TEXT", safeToString(row[26]));

	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}

	private JRMapCollectionDataSource mapUpiMatchedData(List<Object[]> rawData) {
	    if (rawData == null || rawData.isEmpty()) {
	        return new JRMapCollectionDataSource(Collections.emptyList());
	    }

	    Collection<Map<String, ?>> mappedData = new ArrayList<>();

	    for (Object[] row : rawData) {
	        Map<String, Object> map = new HashMap<>();

	        // --- Mapped exactly in the same order you listed ---
	        map.put("S_TRN_DT", convertToDate(row[0]));
	        map.put("S_VALUE_DT", convertToDate(row[1]));
	        map.put("S_CTRY", safeToString(row[2]));
	        map.put("S_REF", safeToString(row[3]));
	        map.put("S_TRN_DESC", safeToString(row[4]));
	        map.put("S_ADDL2", safeToString(row[5]));
	        map.put("S_ADDL3", safeToString(row[6]));
	        map.put("S_AC_CCY", safeToString(row[7]));
	        map.put("S_DR_MOV", safeToBigDecimal(row[8]));
	        map.put("S_CR_MOV", safeToBigDecimal(row[9]));
	        map.put("S_RECON_FLG", safeToString(row[10]));
	        map.put("S_RECON_TRAN_DATE", convertToDate(row[11]));
	        map.put("S_RECON_TYPE", safeToString(row[12]));
	        map.put("S_RECON_PROCESS_DATE", convertToDate(row[13]));
	        map.put("D_STTL_DATE", safeToString(row[14]));
	        map.put("D_ARN", safeToString(row[15]));
	        map.put("D_MCC", safeToString(row[16]));
	        map.put("D_AUTH_AMNT", safeToBigDecimal(row[17]));
	        map.put("D_ACCT_AMNT", safeToBigDecimal(row[18]));
	        map.put("D_AUTH_CURRENCY", safeToString(row[19]));
	        map.put("D_TRANSACTION_DATE", safeToString(row[20]));
	        map.put("D_REFNUM", safeToString(row[21]));
	        map.put("D_RECONCILE_AMOUNT", safeToBigDecimal(row[22]));
	        map.put("D_RECONCILE_CUR", safeToString(row[23]));
	        map.put("D_TRANSACTION_AMOUNT", safeToBigDecimal(row[24]));
	        map.put("D_TRANSACTION_CURRENCY", safeToString(row[25]));
	      
	        map.put("D_CBS_ACC_NO", safeToString(row[26]));
	        map.put("D_CARD", safeToString(row[27]));
	        map.put("D_CARD_NUMBER", safeToString(row[28]));
	        map.put("D_CARD_TYPE", safeToString(row[29]));
	        map.put("D_MERCHANT_DESC", safeToString(row[30]));
	        map.put("D_TRN_COUNTRY", safeToString(row[31]));
	        map.put("D_APPROVAL_CODE", safeToString(row[32]));
	        map.put("D_INTERCHANGE_FEES", safeToBigDecimal(row[33]));
	        map.put("D_SERVICE_FEE", safeToBigDecimal(row[34]));
	        map.put("D_SERVICE_IND", safeToString(row[35]));
	        map.put("D_I_FEE_IND", safeToString(row[36]));
	        map.put("D_DR_CR", safeToString(row[37]));
	        map.put("D_MARKUP_VALUE", safeToBigDecimal(row[38]));
	        map.put("D_MARKUP_CURRENCY", safeToString(row[39]));
	        map.put("D_RECON_FLG", safeToString(row[40]));
	        map.put("D_RECON_TRAN_DATE", convertToDate(row[41]));
	        map.put("D_RECON_TYPE", safeToString(row[42]));
	        map.put("D_RECON_PROCESS_DATE", convertToDate(row[43]));
	        map.put("S_TRN_TYPE", safeToString(row[55]));
	        map.put("S_ADDL_TEXT", safeToString(row[56]));
	        map.put("D_TRANSACTION_TYPE", safeToString(row[57]));

	        mappedData.add(map);
	    }
	    return new JRMapCollectionDataSource(mappedData);
	}

	/* ---------------- Helper Methods ---------------- */

	private java.util.Date convertToDate(Object value) {
	    if (value instanceof java.sql.Timestamp) {
	        return new java.util.Date(((java.sql.Timestamp) value).getTime());
	    } else if (value instanceof java.sql.Date) {
	        return new java.util.Date(((java.sql.Date) value).getTime());
	    } else {
	        return null;
	    }
	}

	private String safeToString(Object value) {
	    return value != null ? value.toString() : "";
	}

	private BigDecimal safeToBigDecimal(Object value) {
	    if (value instanceof BigDecimal) {
	        return (BigDecimal) value;
	    } else if (value instanceof Number) {
	        return BigDecimal.valueOf(((Number) value).doubleValue());
	    } else {
	        return BigDecimal.ZERO;
	    }
	}
}
