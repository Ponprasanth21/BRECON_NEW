package com.bornfire.recon.services.upload;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.bornfire.recon.entities.upload.RECON_DREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRFILE_DESTINATION_REPO;
import com.bornfire.recon.entities.upload.RECON_DRREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_DRREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_DRREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_DREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_DRREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_DRREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_DRREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_TEMP_UPIREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_MUR_REPO;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_ENTITY;
import com.bornfire.recon.entities.upload.RECON_UPIREPORT_USD_REPO;
import com.bornfire.recon.entities.upload.RECON_UPI_DESTINATION_REPO;
import com.bornfire.recon.services.AuidtConfigure;

@Service
@ConfigurationProperties("output")
@Transactional
public class NostroService {
	
	private static final Logger logger = Logger.getLogger(DownloadService.class.getName());

	@Autowired
	RECON_TEMP_UPIREPORT_USD_REPO RECON_TEMP_UPIREPORT_USD_REPO;

	@Autowired
	RECON_UPIREPORT_USD_REPO RECON_UPIREPORT_USD_REPO;

	@Autowired
	RECON_TEMP_UPIREPORT_MUR_REPO RECON_TEMP_UPIREPORT_MUR_REPO;

	@Autowired
	RECON_UPIREPORT_MUR_REPO RECON_UPIREPORT_MUR_REPO;

	@Autowired
	RECON_TEMP_DRREPORT_USD_REPO RECON_TEMP_DRREPORT_USD_REPO;

	@Autowired
	RECON_DRREPORT_USD_REPO RECON_DRREPORT_USD_REPO;

	@Autowired
	RECON_TEMP_DRREPORT_MUR_REPO RECON_TEMP_DRREPORT_MUR_REPO;

	@Autowired
	RECON_DRREPORT_MUR_REPO RECON_DRREPORT_MUR_REPO;
	
	@Autowired
	RECON_DRFILE_DESTINATION_REPO BRECON_DRFILE_DESTINATION_REPO;
	
	@Autowired
	RECON_UPI_DESTINATION_REPO RECON_UPI_DESTINATION_REPO;

	@Autowired
	AuidtConfigure audit;

	/* UPI DEBIT */
	public String editUpiUsd(RECON_UPIREPORT_USD_ENTITY nostroUsd, String formmode, String userId, String userName) {
		String msg = "";
		try {
			if (formmode.equals("ModiReportUSD")) {
				// Convert recon_date to string for lookup
				String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroUsd.getRecon_date());
				Optional<RECON_UPIREPORT_USD_ENTITY> mainRecordOpt = Optional
						.ofNullable(RECON_UPIREPORT_USD_REPO.getReconDate(formattedDate, nostroUsd.getCurrency()));
				if (mainRecordOpt.isPresent()) {
					RECON_UPIREPORT_USD_ENTITY mainRecord = mainRecordOpt.get();

					// ✅ Preserve all existing column values in the main table
					RECON_UPIREPORT_USD_ENTITY updatedRecord = new RECON_UPIREPORT_USD_ENTITY();

					updatedRecord.setAtm_intermdiary(mainRecord.getAtm_intermdiary());
					updatedRecord.setAtm_ifee(mainRecord.getAtm_ifee());
					updatedRecord.setAtm_sfee(mainRecord.getAtm_sfee());
					updatedRecord.setAtm_payable(mainRecord.getAtm_payable());
					updatedRecord.setPos_intermdiary(mainRecord.getPos_intermdiary());
					updatedRecord.setPos_ifee(mainRecord.getPos_ifee());
					updatedRecord.setPos_sfee(mainRecord.getPos_sfee());
					updatedRecord.setPos_payable(mainRecord.getPos_payable());
					updatedRecord.setNostro_upi_cardpay(mainRecord.getNostro_upi_cardpay());
					updatedRecord.setNostro_scb(mainRecord.getNostro_scb());
					updatedRecord.setNostro_upi_cardrev(mainRecord.getNostro_upi_cardrev());
					updatedRecord.setEpos_intermdiary(mainRecord.getEpos_intermdiary());
					updatedRecord.setEpos_ifee(mainRecord.getEpos_ifee());
					updatedRecord.setEpos_sfee(mainRecord.getEpos_sfee());
					updatedRecord.setEpos_payable(mainRecord.getEpos_payable());
					updatedRecord.setCurrency(mainRecord.getCurrency());
					updatedRecord.setAmount(mainRecord.getAmount());
					updatedRecord.setAtm_inquiry_ifee(mainRecord.getAtm_inquiry_ifee());
					updatedRecord.setAtm_inquiry_sfee(mainRecord.getAtm_inquiry_sfee());
					updatedRecord.setAtm_inquiry_payable(mainRecord.getAtm_inquiry_payable());
					updatedRecord.setRecon_date(mainRecord.getRecon_date());
					updatedRecord.setDel_flg(mainRecord.getDel_flg());
					updatedRecord.setEntry_user(mainRecord.getEntry_user());
					updatedRecord.setEntry_time(mainRecord.getEntry_time());
					updatedRecord.setVerify_user(mainRecord.getVerify_user());
					updatedRecord.setVerify_time(mainRecord.getVerify_time());
					// ✅ Update required fields
					updatedRecord.setEntity_flg("N");
					updatedRecord.setModify_user(userId);
					updatedRecord.setModify_time(new Date());

					RECON_UPIREPORT_USD_REPO.save(updatedRecord);
				}

				// ✅ Save new record in TEMP table
				RECON_TEMP_UPIREPORT_USD_ENTITY tempRecord = new RECON_TEMP_UPIREPORT_USD_ENTITY();
				RECON_UPIREPORT_USD_ENTITY mainRecord = mainRecordOpt.get();

				// Copy all relevant financial fields from nostroData
				tempRecord.setAtm_intermdiary(mainRecord.getAtm_intermdiary());
				tempRecord.setAtm_ifee(mainRecord.getAtm_ifee());
				tempRecord.setAtm_sfee(mainRecord.getAtm_sfee());
				tempRecord.setAtm_payable(mainRecord.getAtm_payable());
				tempRecord.setPos_intermdiary(mainRecord.getPos_intermdiary());
				tempRecord.setPos_ifee(mainRecord.getPos_ifee());
				tempRecord.setPos_sfee(mainRecord.getPos_sfee());
				tempRecord.setPos_payable(mainRecord.getPos_payable());
				tempRecord.setNostro_upi_cardpay(nostroUsd.getNostro_upi_cardpay());
				tempRecord.setNostro_scb(nostroUsd.getNostro_scb());
				tempRecord.setNostro_upi_cardrev(nostroUsd.getNostro_upi_cardrev());
				tempRecord.setEpos_intermdiary(mainRecord.getEpos_intermdiary());
				tempRecord.setEpos_ifee(mainRecord.getEpos_ifee());
				tempRecord.setEpos_sfee(mainRecord.getEpos_sfee());
				tempRecord.setEpos_payable(mainRecord.getEpos_payable());
				tempRecord.setAmount(nostroUsd.getAmount());
				tempRecord.setAtm_inquiry_ifee(mainRecord.getAtm_inquiry_ifee());
				tempRecord.setAtm_inquiry_sfee(mainRecord.getAtm_inquiry_sfee());
				tempRecord.setAtm_inquiry_payable(mainRecord.getAtm_inquiry_payable());

				// Currency and Date
				tempRecord.setCurrency(mainRecord.getCurrency());
				tempRecord.setRecon_date(mainRecord.getRecon_date());

				// Common flags and audit fields
				tempRecord.setDel_flg("N");
				tempRecord.setEntity_flg("Y");
				tempRecord.setEntry_user(userId);
				tempRecord.setEntry_time(new Date());
				tempRecord.setModify_user(userId);
				tempRecord.setModify_time(new Date());

				// ✅ Save the record into the TEMP repository
				RECON_TEMP_UPIREPORT_USD_REPO.save(tempRecord);

				// ✅ Insert into audit trail
				audit.insertAudit(userId, userName, "UPI-USD REPORT EDIT", "NOSTRO EDITED SUCCESSFULLY",
						"BRECON_TEMP_UPIREPORT_USD", "UPI SUMMARY REPORT - USD");

				msg = "Updated successfully!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg = "Error Occurred. Please contact Administrator.";
		}
		return msg;
	}

	public String editUpiMur(RECON_UPIREPORT_MUR_ENTITY nostroMur, String formmode, String userId, String userName) {
		String msg = "";
		try {
			if (formmode.equals("ModiReportMUR")) {
				// Convert recon_date to string for lookup
				String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroMur.getRecon_date());
				System.out.println(formattedDate);
				Optional<RECON_UPIREPORT_MUR_ENTITY> mainRecordOpt = Optional
						.ofNullable(RECON_UPIREPORT_MUR_REPO.getReconDate(formattedDate, nostroMur.getCurrency()));
				if (mainRecordOpt.isPresent()) {
					RECON_UPIREPORT_MUR_ENTITY mainRecord = mainRecordOpt.get();

					// ✅ Preserve all existing column values in the main table
					RECON_UPIREPORT_MUR_ENTITY updatedRecord = new RECON_UPIREPORT_MUR_ENTITY();

					updatedRecord.setAtm_intermdiary(mainRecord.getAtm_intermdiary());
					updatedRecord.setAtm_ifee(mainRecord.getAtm_ifee());
					updatedRecord.setAtm_sfee(mainRecord.getAtm_sfee());
					updatedRecord.setAtm_payable(mainRecord.getAtm_payable());
					updatedRecord.setAtm_inquiry_ifee(mainRecord.getAtm_inquiry_ifee());
					updatedRecord.setAtm_inquiry_sfee(mainRecord.getAtm_inquiry_sfee());
					updatedRecord.setAtm_inquiry_payable(mainRecord.getAtm_inquiry_payable());
					updatedRecord.setPos_intermdiary(mainRecord.getPos_intermdiary());
					updatedRecord.setPos_ifee(mainRecord.getPos_ifee());
					updatedRecord.setPos_sfee(mainRecord.getPos_sfee());
					updatedRecord.setPos_payable(mainRecord.getPos_payable());
					updatedRecord.setNostro_mastercard(mainRecord.getNostro_mastercard());
					updatedRecord.setNostro_mcb(mainRecord.getNostro_mcb());
					updatedRecord.setNostro_upi(mainRecord.getNostro_upi());
					updatedRecord.setEpos_intermdiary(mainRecord.getEpos_intermdiary());
					updatedRecord.setEpos_ifee(mainRecord.getEpos_ifee());
					updatedRecord.setEpos_sfee(mainRecord.getEpos_sfee());
					updatedRecord.setEpos_payable(mainRecord.getEpos_payable());
					updatedRecord.setCurrency(mainRecord.getCurrency());
					updatedRecord.setRecon_date(mainRecord.getRecon_date());
					updatedRecord.setDel_flg(mainRecord.getDel_flg());
					updatedRecord.setEntry_user(mainRecord.getEntry_user());
					updatedRecord.setEntry_time(mainRecord.getEntry_time());
					updatedRecord.setVerify_user(mainRecord.getVerify_user());
					updatedRecord.setVerify_time(mainRecord.getVerify_time());

					// ✅ Update required fields
					updatedRecord.setEntity_flg("N");
					updatedRecord.setModify_user(userId);
					updatedRecord.setModify_time(new Date());

					RECON_UPIREPORT_MUR_REPO.save(updatedRecord);
				}

				// ✅ Save new record in TEMP table
				RECON_TEMP_UPIREPORT_MUR_ENTITY tempRecord = new RECON_TEMP_UPIREPORT_MUR_ENTITY();
				RECON_UPIREPORT_MUR_ENTITY mainRecord = mainRecordOpt.get();

				// Copy all relevant financial fields from nostroData
				// Copy all relevant financial fields from nostroData
				tempRecord.setAtm_intermdiary(mainRecord.getAtm_intermdiary());
				tempRecord.setAtm_ifee(mainRecord.getAtm_ifee());
				tempRecord.setAtm_sfee(mainRecord.getAtm_sfee());
				tempRecord.setAtm_payable(mainRecord.getAtm_payable());
				tempRecord.setAtm_inquiry_ifee(mainRecord.getAtm_inquiry_ifee());
				tempRecord.setAtm_inquiry_sfee(mainRecord.getAtm_inquiry_sfee());
				tempRecord.setAtm_inquiry_payable(mainRecord.getAtm_inquiry_payable());
				tempRecord.setPos_intermdiary(mainRecord.getPos_intermdiary());
				tempRecord.setPos_ifee(mainRecord.getPos_ifee());
				tempRecord.setPos_sfee(mainRecord.getPos_sfee());
				tempRecord.setPos_payable(mainRecord.getPos_payable());
				tempRecord.setNostro_mastercard(nostroMur.getNostro_mastercard());
				tempRecord.setNostro_mcb(nostroMur.getNostro_mcb());
				tempRecord.setNostro_upi(nostroMur.getNostro_upi());
				tempRecord.setEpos_intermdiary(mainRecord.getEpos_intermdiary());
				tempRecord.setEpos_ifee(mainRecord.getEpos_ifee());
				tempRecord.setEpos_sfee(mainRecord.getEpos_sfee());
				tempRecord.setEpos_payable(mainRecord.getEpos_payable());
				tempRecord.setAmount(nostroMur.getAmount());

				// Currency and Date
				tempRecord.setCurrency(mainRecord.getCurrency());
				tempRecord.setRecon_date(mainRecord.getRecon_date());

				// Common flags and audit fields
				tempRecord.setDel_flg("N");
				tempRecord.setEntity_flg("Y");
				tempRecord.setEntry_user(userId);
				tempRecord.setEntry_time(new Date());
				tempRecord.setModify_user(userId);
				tempRecord.setModify_time(new Date());

				// ✅ Save the record into the TEMP repository
				RECON_TEMP_UPIREPORT_MUR_REPO.save(tempRecord);

				// ✅ Insert into audit trail
				audit.insertAudit(userId, userName, "UPI-MUR REPORT EDIT", "NOSTRO EDITED SUCCESSFULLY",
						"BRECON_TEMP_UPIREPORT_MUR", "UPI SUMMARY REPORT - MUR");

				msg = "Updated successfully!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg = "Error Occurred. Please contact Administrator.";
		}
		return msg;
	}

	// ✅ Verify logic
	public String verifyUpiUsd(RECON_UPIREPORT_USD_ENTITY nostroUsd, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroUsd.getRecon_date());
			String currency = nostroUsd.getCurrency();

			// Fetch main record
			RECON_UPIREPORT_USD_ENTITY mainRecord = RECON_UPIREPORT_USD_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_UPIREPORT_USD_ENTITY tempRecord = RECON_TEMP_UPIREPORT_USD_REPO.getReconDate(formattedDate,
					currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for verification!";
			}

			// ✅ Replace main table data with temp data
			mainRecord.setAtm_intermdiary(tempRecord.getAtm_intermdiary());
			mainRecord.setAtm_ifee(tempRecord.getAtm_ifee());
			mainRecord.setAtm_sfee(tempRecord.getAtm_sfee());
			mainRecord.setAtm_payable(tempRecord.getAtm_payable());
			mainRecord.setPos_intermdiary(tempRecord.getPos_intermdiary());
			mainRecord.setPos_ifee(tempRecord.getPos_ifee());
			mainRecord.setPos_sfee(tempRecord.getPos_sfee());
			mainRecord.setPos_payable(tempRecord.getPos_payable());
			mainRecord.setNostro_upi_cardpay(tempRecord.getNostro_upi_cardpay());
			mainRecord.setNostro_scb(tempRecord.getNostro_scb());
			mainRecord.setNostro_upi_cardrev(tempRecord.getNostro_upi_cardrev());
			mainRecord.setEpos_intermdiary(tempRecord.getEpos_intermdiary());
			mainRecord.setEpos_ifee(tempRecord.getEpos_ifee());
			mainRecord.setEpos_sfee(tempRecord.getEpos_sfee());
			mainRecord.setEpos_payable(tempRecord.getEpos_payable());
			mainRecord.setCurrency(tempRecord.getCurrency());
			mainRecord.setRecon_date(tempRecord.getRecon_date());
			mainRecord.setDel_flg(tempRecord.getDel_flg());
			mainRecord.setEntry_user(mainRecord.getEntry_user());
			mainRecord.setEntry_time(mainRecord.getEntry_time());
			mainRecord.setModify_user(mainRecord.getModify_user());
			mainRecord.setModify_time(mainRecord.getModify_time());
			mainRecord.setAmount(tempRecord.getAmount());
			mainRecord.setAtm_inquiry_ifee(tempRecord.getAtm_inquiry_ifee());
			mainRecord.setAtm_inquiry_sfee(tempRecord.getAtm_inquiry_sfee());
			mainRecord.setAtm_inquiry_payable(tempRecord.getAtm_inquiry_payable());
			// ✅ Update required fields
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());
			mainRecord.setEntity_flg("Y");

			// ✅ Save the updated record in main table
			RECON_UPIREPORT_USD_REPO.save(mainRecord);
			
			logger.info("Running detailed report for UPI-USD");
			RECON_UPI_DESTINATION_REPO.runReconDetail(formattedDate, "USD");

			// ✅ Delete from temp table
			RECON_TEMP_UPIREPORT_USD_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "UPI-USD REPORT VERIFY", "NOSTRO VERIFIED SUCCESSFULLY",
					"BRECON_TEMP_UPIREPORT_USD", "UPI SUMMARY REPORT - USD");

			return "Verified Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during verification: " + e.getMessage();
		}
	}

	// ✅ Verify logic
	public String verifyUpiMur(RECON_UPIREPORT_MUR_ENTITY nostroMur, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroMur.getRecon_date());
			String currency = nostroMur.getCurrency();

			// Fetch main record
			RECON_UPIREPORT_MUR_ENTITY mainRecord = RECON_UPIREPORT_MUR_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_UPIREPORT_MUR_ENTITY tempRecord = RECON_TEMP_UPIREPORT_MUR_REPO.getReconDate(formattedDate,currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for verification!";
			}

			// ✅ Replace main table data with temp data
			// ✅ Replace main table data with temp data
			mainRecord.setAtm_intermdiary(tempRecord.getAtm_intermdiary());
			mainRecord.setAtm_ifee(tempRecord.getAtm_ifee());
			mainRecord.setAtm_sfee(tempRecord.getAtm_sfee());
			mainRecord.setAtm_payable(tempRecord.getAtm_payable());
			mainRecord.setAtm_inquiry_ifee(tempRecord.getAtm_inquiry_ifee());
			mainRecord.setAtm_inquiry_sfee(tempRecord.getAtm_inquiry_sfee());
			mainRecord.setAtm_inquiry_payable(tempRecord.getAtm_inquiry_payable());
			mainRecord.setPos_intermdiary(tempRecord.getPos_intermdiary());
			mainRecord.setPos_ifee(tempRecord.getPos_ifee());
			mainRecord.setPos_sfee(tempRecord.getPos_sfee());
			mainRecord.setPos_payable(tempRecord.getPos_payable());
			mainRecord.setNostro_mastercard(tempRecord.getNostro_mastercard());
			mainRecord.setNostro_mcb(tempRecord.getNostro_mcb());
			mainRecord.setNostro_upi(tempRecord.getNostro_upi());
			mainRecord.setEpos_intermdiary(tempRecord.getEpos_intermdiary());
			mainRecord.setEpos_ifee(tempRecord.getEpos_ifee());
			mainRecord.setEpos_sfee(tempRecord.getEpos_sfee());
			mainRecord.setEpos_payable(tempRecord.getEpos_payable());
			mainRecord.setCurrency(tempRecord.getCurrency());
			mainRecord.setRecon_date(tempRecord.getRecon_date());
			mainRecord.setDel_flg(tempRecord.getDel_flg());
			mainRecord.setEntry_user(mainRecord.getEntry_user());
			mainRecord.setEntry_time(mainRecord.getEntry_time());
			mainRecord.setModify_user(mainRecord.getModify_user());
			mainRecord.setModify_time(mainRecord.getModify_time());
			mainRecord.setAmount(tempRecord.getAmount());

			// ✅ Update required fields
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());
			mainRecord.setEntity_flg("Y");

			// ✅ Save the updated record in main table
			RECON_UPIREPORT_MUR_REPO.save(mainRecord);
			
			logger.info("Running detailed report for UPI-MUR");
			RECON_UPI_DESTINATION_REPO.runReconDetail(formattedDate, "MUR");

			// ✅ Delete from temp table
			RECON_TEMP_UPIREPORT_MUR_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "UPI-MUR REPORT VERIFY", "NOSTRO VERIFIED SUCCESSFULLY",
					"BRECON_TEMP_UPIREPORT_MUR", "UPI SUMMARY REPORT - MUR");

			return "Verified Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during verification: " + e.getMessage();
		}
	}

	// ✅ Cancel logic
	public String cancelVerifyUpiUsd(RECON_UPIREPORT_USD_ENTITY nostroUsd, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroUsd.getRecon_date());
			String currency = nostroUsd.getCurrency();

			// Fetch main record
			RECON_UPIREPORT_USD_ENTITY mainRecord = RECON_UPIREPORT_USD_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_UPIREPORT_USD_ENTITY tempRecord = RECON_TEMP_UPIREPORT_USD_REPO.getReconDate(formattedDate,
					currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for cancellation!";
			}

			// ✅ Mark main table record as cancelled
			mainRecord.setEntity_flg("Y");
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());

			RECON_UPIREPORT_USD_REPO.save(mainRecord);

			// ✅ Delete from temp table
			RECON_TEMP_UPIREPORT_USD_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "UPI-USD REPORT CANCEL", "NOSTRO CANCELLED SUCCESSFULLY",
					"BRECON_TEMP_UPIREPORT_USD", "UPI SUMMARY REPORT - USD");

			return "Verification Cancelled Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during cancellation: " + e.getMessage();
		}
	}

	// ✅ Cancel logic
	public String cancelVerifyUpiMur(RECON_UPIREPORT_MUR_ENTITY nostroMur, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroMur.getRecon_date());
			String currency = nostroMur.getCurrency();

			// Fetch main record
			RECON_UPIREPORT_MUR_ENTITY mainRecord = RECON_UPIREPORT_MUR_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_UPIREPORT_MUR_ENTITY tempRecord = RECON_TEMP_UPIREPORT_MUR_REPO.getReconDate(formattedDate,
					currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for cancellation!";
			}

			// ✅ Mark main table record as cancelled
			mainRecord.setEntity_flg("Y");
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());

			RECON_UPIREPORT_MUR_REPO.save(mainRecord);

			// ✅ Delete from temp table
			RECON_TEMP_UPIREPORT_MUR_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "UPI-MUR REPORT CANCEL", "NOSTRO CANCELLED SUCCESSFULLY",
					"BRECON_TEMP_UPIREPORT_MUR", "UPI SUMMARY REPORT - MUR");

			return "Verification Cancelled Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during cancellation: " + e.getMessage();
		}
	}

	/* DEBIT */
	public String editDrUsd(RECON_DREPORT_USD_ENTITY nostroUsd, String formmode, String userId, String userName) {
		String msg = "";
		try {
			if (formmode.equals("ModiReportUSD")) {
				// Convert recon_date to string for lookup
				String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroUsd.getRecon_date());
				Optional<RECON_DREPORT_USD_ENTITY> mainRecordOpt = Optional
						.ofNullable(RECON_DRREPORT_USD_REPO.getReconDate(formattedDate, nostroUsd.getCurrency()));
				if (mainRecordOpt.isPresent()) {
					RECON_DREPORT_USD_ENTITY mainRecord = mainRecordOpt.get();

					// ✅ Preserve all existing column values in the main table
					RECON_DREPORT_USD_ENTITY updatedRecord = new RECON_DREPORT_USD_ENTITY();

					updatedRecord.setAtm1(mainRecord.getAtm1());
					updatedRecord.setAtm2(mainRecord.getAtm2());
					updatedRecord.setAtm_payable1(mainRecord.getAtm_payable1());
					updatedRecord.setAtm_payable2(mainRecord.getAtm_payable2());
					updatedRecord.setPos1(mainRecord.getPos1());
					updatedRecord.setPos2(mainRecord.getPos2());
					updatedRecord.setPos3(mainRecord.getPos3());
					updatedRecord.setPos4(mainRecord.getPos4());
					updatedRecord.setPos_payable1(mainRecord.getPos_payable1());
					updatedRecord.setPos_payable2(mainRecord.getPos_payable2());
					updatedRecord.setChg1(mainRecord.getChg1());
					updatedRecord.setChg2(mainRecord.getChg2());
					updatedRecord.setNostro_scb1(mainRecord.getNostro_scb1());
					updatedRecord.setNostro_scb2(mainRecord.getNostro_scb2());
					updatedRecord.setNostro_ent1(mainRecord.getNostro_ent1());
					updatedRecord.setNostro_ent2(mainRecord.getNostro_ent2());
					updatedRecord.setCurrency(mainRecord.getCurrency());
					updatedRecord.setRecon_date(mainRecord.getRecon_date());
					updatedRecord.setDel_flg(mainRecord.getDel_flg());
					updatedRecord.setEntry_user(mainRecord.getEntry_user());
					updatedRecord.setEntry_time(mainRecord.getEntry_time());
					updatedRecord.setVerify_user(mainRecord.getVerify_user());
					updatedRecord.setVerify_time(mainRecord.getVerify_time());
					// ✅ Update required fields
					updatedRecord.setEntity_flg("N");
					updatedRecord.setModify_user(userId);
					updatedRecord.setModify_time(new Date());

					RECON_DRREPORT_USD_REPO.save(updatedRecord);
				}

				// ✅ Save new record in TEMP table
				RECON_TEMP_DREPORT_USD_ENTITY tempRecord = new RECON_TEMP_DREPORT_USD_ENTITY();
				RECON_DREPORT_USD_ENTITY mainRecord = mainRecordOpt.get();

				// Copy all relevant financial fields from nostroData
				tempRecord.setAtm1(mainRecord.getAtm1());
				tempRecord.setAtm2(mainRecord.getAtm2());
				tempRecord.setAtm_payable1(mainRecord.getAtm_payable1());
				tempRecord.setPos1(mainRecord.getPos1());
				tempRecord.setPos2(mainRecord.getPos2());
				tempRecord.setPos3(mainRecord.getPos3());
				tempRecord.setPos4(mainRecord.getPos4());
				tempRecord.setPos_payable1(mainRecord.getPos_payable1());
				tempRecord.setChg1(mainRecord.getChg1());
				tempRecord.setChg2(mainRecord.getChg2());
				tempRecord.setNostro_scb1(nostroUsd.getNostro_scb1());
				tempRecord.setNostro_scb2(nostroUsd.getNostro_scb2());
				tempRecord.setNostro_ent1(nostroUsd.getNostro_ent1());
				tempRecord.setNostro_ent2(nostroUsd.getNostro_ent2());
				tempRecord.setAmount(nostroUsd.getAmount());
				tempRecord.setAtm_payable2(mainRecord.getAtm_payable2());
				tempRecord.setPos_payable2(mainRecord.getPos_payable2());

				// Currency and Date
				tempRecord.setCurrency(mainRecord.getCurrency());
				tempRecord.setRecon_date(mainRecord.getRecon_date());

				// Common flags and audit fields
				tempRecord.setDel_flg("N");
				tempRecord.setEntity_flg("Y");
				tempRecord.setEntry_user(userId);
				tempRecord.setEntry_time(new Date());
				tempRecord.setModify_user(userId);
				tempRecord.setModify_time(new Date());

				// ✅ Save the record into the TEMP repository
				RECON_TEMP_DRREPORT_USD_REPO.save(tempRecord);

				// ✅ Insert into audit trail
				audit.insertAudit(userId, userName, "DR-USD REPORT EDIT", "NOSTRO EDITED SUCCESSFULLY",
						"BRECON_TEMP_DRREPORT_USD", "DR SUMMARY REPORT - USD");

				msg = "Updated successfully!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg = "Error Occurred. Please contact Administrator.";
		}
		return msg;
	}

	public String editDrMur(RECON_DRREPORT_MUR_ENTITY nostroMur, String formmode, String userId, String userName) {
		String msg = "";
		try {
			if (formmode.equals("ModiReportMUR")) {
				// Convert recon_date to string for lookup
				String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroMur.getRecon_date());
				System.out.println(formattedDate);
				Optional<RECON_DRREPORT_MUR_ENTITY> mainRecordOpt = Optional
						.ofNullable(RECON_DRREPORT_MUR_REPO.getReconDate(formattedDate, nostroMur.getCurrency()));
				if (mainRecordOpt.isPresent()) {
					RECON_DRREPORT_MUR_ENTITY mainRecord = mainRecordOpt.get();

					// ✅ Preserve all existing column values in the main table
					RECON_DRREPORT_MUR_ENTITY updatedRecord = new RECON_DRREPORT_MUR_ENTITY();

					updatedRecord.setAtm1(mainRecord.getAtm1());
					updatedRecord.setAtm2(mainRecord.getAtm2());
					updatedRecord.setAtm_payable1(mainRecord.getAtm_payable1());
					updatedRecord.setAtm_payable2(mainRecord.getAtm_payable2());
					updatedRecord.setPos1(mainRecord.getPos1());
					updatedRecord.setPos2(mainRecord.getPos2());
					updatedRecord.setPos3(mainRecord.getPos3());
					updatedRecord.setPos4(mainRecord.getPos4());
					updatedRecord.setPos_payable1(mainRecord.getPos_payable1());
					updatedRecord.setPos_payable2(mainRecord.getPos_payable2());
					updatedRecord.setChg1(mainRecord.getChg1());
					updatedRecord.setChg2(mainRecord.getChg2());
					updatedRecord.setFgn1(mainRecord.getFgn1());
					updatedRecord.setFgn2(mainRecord.getFgn2());
					updatedRecord.setFgn3(mainRecord.getFgn3());
					updatedRecord.setNostro_mcb1(mainRecord.getNostro_mcb1());
					updatedRecord.setNostro_mcb2(mainRecord.getNostro_mcb2());
					updatedRecord.setNostro_mcb3(mainRecord.getNostro_mcb3());
					updatedRecord.setNostro_ent1(mainRecord.getNostro_ent1());
					updatedRecord.setNostro_ent2(mainRecord.getNostro_ent2());
					updatedRecord.setCurrency(mainRecord.getCurrency());
					updatedRecord.setRecon_date(mainRecord.getRecon_date());
					updatedRecord.setDel_flg(mainRecord.getDel_flg());
					updatedRecord.setEntry_user(mainRecord.getEntry_user());
					updatedRecord.setEntry_time(mainRecord.getEntry_time());
					updatedRecord.setVerify_user(mainRecord.getVerify_user());
					updatedRecord.setVerify_time(mainRecord.getVerify_time());

					// ✅ Update required fields
					updatedRecord.setEntity_flg("N");
					updatedRecord.setModify_user(userId);
					updatedRecord.setModify_time(new Date());

					RECON_DRREPORT_MUR_REPO.save(updatedRecord);
				}

				// ✅ Save new record in TEMP table
				RECON_TEMP_DRREPORT_MUR_ENTITY tempRecord = new RECON_TEMP_DRREPORT_MUR_ENTITY();
				RECON_DRREPORT_MUR_ENTITY mainRecord = mainRecordOpt.get();

				// Copy all relevant financial fields from nostroData
				tempRecord.setAtm1(mainRecord.getAtm1());
				tempRecord.setAtm2(mainRecord.getAtm2());
				tempRecord.setAtm_payable1(mainRecord.getAtm_payable1());
				tempRecord.setAtm_payable2(mainRecord.getAtm_payable2());
				tempRecord.setPos1(mainRecord.getPos1());
				tempRecord.setPos2(mainRecord.getPos2());
				tempRecord.setPos3(mainRecord.getPos3());
				tempRecord.setPos4(mainRecord.getPos4());
				tempRecord.setPos_payable1(mainRecord.getPos_payable1());
				tempRecord.setPos_payable2(mainRecord.getPos_payable2());
				tempRecord.setChg1(mainRecord.getChg1());
				tempRecord.setChg2(mainRecord.getChg2());
				tempRecord.setFgn1(nostroMur.getFgn1());
				tempRecord.setFgn2(nostroMur.getFgn2());
				tempRecord.setFgn3(nostroMur.getFgn3());
				tempRecord.setNostro_mcb1(nostroMur.getNostro_mcb1());
				tempRecord.setNostro_mcb2(nostroMur.getNostro_mcb2());
				tempRecord.setNostro_mcb3(nostroMur.getNostro_mcb3());
				tempRecord.setNostro_ent1(nostroMur.getNostro_ent1());
				tempRecord.setNostro_ent2(nostroMur.getNostro_ent2());
				tempRecord.setAmount(nostroMur.getAmount());

				// Currency and Date
				tempRecord.setCurrency(mainRecord.getCurrency());
				tempRecord.setRecon_date(mainRecord.getRecon_date());

				// Common flags and audit fields
				tempRecord.setDel_flg("N");
				tempRecord.setEntity_flg("Y");
				tempRecord.setEntry_user(userId);
				tempRecord.setEntry_time(new Date());
				tempRecord.setModify_user(userId);
				tempRecord.setModify_time(new Date());

				// ✅ Save the record into the TEMP repository
				RECON_TEMP_DRREPORT_MUR_REPO.save(tempRecord);

				// ✅ Insert into audit trail
				audit.insertAudit(userId, userName, "DR-MUR REPORT EDIT", "NOSTRO EDITED SUCCESSFULLY",
						"BRECON_TEMP_DRREPORT_MUR", "DR SUMMARY REPORT - MUR");

				msg = "Updated successfully!";
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg = "Error Occurred. Please contact Administrator.";
		}
		return msg;
	}

	// ✅ Verify logic
	public String verifyDrUsd(RECON_DREPORT_USD_ENTITY nostroUsd, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroUsd.getRecon_date());
			String currency = nostroUsd.getCurrency();

			// Fetch main record
			RECON_DREPORT_USD_ENTITY mainRecord = RECON_DRREPORT_USD_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_DREPORT_USD_ENTITY tempRecord = RECON_TEMP_DRREPORT_USD_REPO.getReconDate(formattedDate,
					currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for verification!";
			}

			// ✅ Replace main table data with temp data
			mainRecord.setAtm1(tempRecord.getAtm1());
			mainRecord.setAtm2(tempRecord.getAtm2());
			mainRecord.setAtm_payable1(tempRecord.getAtm_payable1());
			mainRecord.setPos1(tempRecord.getPos1());
			mainRecord.setPos2(tempRecord.getPos2());
			mainRecord.setPos3(tempRecord.getPos3());
			mainRecord.setPos4(tempRecord.getPos4());
			mainRecord.setPos_payable1(tempRecord.getPos_payable1());
			mainRecord.setChg1(tempRecord.getChg1());
			mainRecord.setChg2(tempRecord.getChg2());
			mainRecord.setNostro_scb1(tempRecord.getNostro_scb1());
			mainRecord.setNostro_scb2(tempRecord.getNostro_scb2());
			mainRecord.setNostro_ent1(tempRecord.getNostro_ent1());
			mainRecord.setNostro_ent2(tempRecord.getNostro_ent2());
			mainRecord.setCurrency(tempRecord.getCurrency());
			mainRecord.setRecon_date(tempRecord.getRecon_date());
			mainRecord.setDel_flg(tempRecord.getDel_flg());
			mainRecord.setEntry_user(mainRecord.getEntry_user());
			mainRecord.setEntry_time(mainRecord.getEntry_time());
			mainRecord.setModify_user(mainRecord.getModify_user());
			mainRecord.setModify_time(mainRecord.getModify_time());
			mainRecord.setAmount(tempRecord.getAmount());
			mainRecord.setPos_payable2(tempRecord.getPos_payable2());
			mainRecord.setAtm_payable2(tempRecord.getAtm_payable2());

			// ✅ Update required fields
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());
			mainRecord.setEntity_flg("Y");

			// ✅ Save the updated record in main table
			RECON_DRREPORT_USD_REPO.save(mainRecord);
			
			logger.info("Running detailed report for DR-USD");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(formattedDate, "USD");
			
			// ✅ Delete from temp table
			RECON_TEMP_DRREPORT_USD_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "DR-USD REPORT VERIFY", "NOSTRO VERIFIED SUCCESSFULLY",
					"BRECON_TEMP_DRREPORT_USD", "DR SUMMARY REPORT - USD");

			return "Verified Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during verification: " + e.getMessage();
		}
	}

	// ✅ Verify logic
	public String verifyDrMur(RECON_DRREPORT_MUR_ENTITY nostroMur, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroMur.getRecon_date());
			String currency = nostroMur.getCurrency();

			// Fetch main record
			RECON_DRREPORT_MUR_ENTITY mainRecord = RECON_DRREPORT_MUR_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_DRREPORT_MUR_ENTITY tempRecord = RECON_TEMP_DRREPORT_MUR_REPO.getReconDate(formattedDate,
					currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for verification!";
			}

			// ✅ Replace main table data with temp data
			mainRecord.setAtm1(tempRecord.getAtm1());
			mainRecord.setAtm2(tempRecord.getAtm2());
			mainRecord.setAtm_payable1(tempRecord.getAtm_payable1());
			mainRecord.setPos1(tempRecord.getPos1());
			mainRecord.setPos2(tempRecord.getPos2());
			mainRecord.setPos3(tempRecord.getPos3());
			mainRecord.setPos4(tempRecord.getPos4());
			mainRecord.setPos_payable1(tempRecord.getPos_payable1());
			mainRecord.setChg1(tempRecord.getChg1());
			mainRecord.setChg2(tempRecord.getChg2());
			mainRecord.setFgn1(tempRecord.getFgn1());
			mainRecord.setFgn2(tempRecord.getFgn2());
			mainRecord.setFgn3(tempRecord.getFgn3());
			mainRecord.setNostro_mcb1(tempRecord.getNostro_mcb1());
			mainRecord.setNostro_mcb2(tempRecord.getNostro_mcb2());
			mainRecord.setNostro_mcb3(tempRecord.getNostro_mcb3());
			mainRecord.setNostro_ent1(tempRecord.getNostro_ent1());
			mainRecord.setNostro_ent2(tempRecord.getNostro_ent2());
			mainRecord.setCurrency(tempRecord.getCurrency());
			mainRecord.setRecon_date(tempRecord.getRecon_date());
			mainRecord.setDel_flg(tempRecord.getDel_flg());
			mainRecord.setEntry_user(mainRecord.getEntry_user());
			mainRecord.setEntry_time(mainRecord.getEntry_time());
			mainRecord.setModify_user(mainRecord.getModify_user());
			mainRecord.setModify_time(mainRecord.getModify_time());
			mainRecord.setAmount(tempRecord.getAmount());
			mainRecord.setPos_payable2(tempRecord.getPos_payable2());
			mainRecord.setAtm_payable2(tempRecord.getAtm_payable2());

			// ✅ Update required fields
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());
			mainRecord.setEntity_flg("Y");

			// ✅ Save the updated record in main table
			RECON_DRREPORT_MUR_REPO.save(mainRecord);
			
			logger.info("Running detailed report for DR-MUR");
			BRECON_DRFILE_DESTINATION_REPO.runReconDetail(formattedDate, "MUR");

			// ✅ Delete from temp table
			RECON_TEMP_DRREPORT_MUR_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "DR-MUR REPORT VERIFY", "NOSTRO VERIFIED SUCCESSFULLY",
					"BRECON_TEMP_DRREPORT_MUR", "DR SUMMARY REPORT - MUR");

			return "Verified Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during verification: " + e.getMessage();
		}
	}

	// ✅ Cancel logic
	public String cancelVerifyDrUsd(RECON_DREPORT_USD_ENTITY nostroUsd, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroUsd.getRecon_date());
			String currency = nostroUsd.getCurrency();

			// Fetch main record
			RECON_DREPORT_USD_ENTITY mainRecord = RECON_DRREPORT_USD_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_DREPORT_USD_ENTITY tempRecord = RECON_TEMP_DRREPORT_USD_REPO.getReconDate(formattedDate,
					currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for cancellation!";
			}

			// ✅ Mark main table record as cancelled
			mainRecord.setEntity_flg("Y");
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());

			RECON_DRREPORT_USD_REPO.save(mainRecord);

			// ✅ Delete from temp table
			RECON_TEMP_DRREPORT_USD_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "DR-USD REPORT CANCEL", "NOSTRO CANCELLED SUCCESSFULLY",
					"BRECON_TEMP_DRREPORT_USD", "DR SUMMARY REPORT - USD");

			return "Verification Cancelled Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during cancellation: " + e.getMessage();
		}
	}

	// ✅ Cancel logic
	public String cancelVerifyDrMur(RECON_DRREPORT_MUR_ENTITY nostroMur, String userId, String userName) {
		try {
			String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(nostroMur.getRecon_date());
			String currency = nostroMur.getCurrency();

			// Fetch main record
			RECON_DRREPORT_MUR_ENTITY mainRecord = RECON_DRREPORT_MUR_REPO.getReconDate(formattedDate, currency);
			// Fetch temp record
			RECON_TEMP_DRREPORT_MUR_ENTITY tempRecord = RECON_TEMP_DRREPORT_MUR_REPO.getReconDate(formattedDate,
					currency);

			if (mainRecord == null || tempRecord == null) {
				return "Record not found for cancellation!";
			}

			// ✅ Mark main table record as cancelled
			mainRecord.setEntity_flg("Y");
			mainRecord.setVerify_user(userId);
			mainRecord.setVerify_time(new Date());

			RECON_DRREPORT_MUR_REPO.save(mainRecord);

			// ✅ Delete from temp table
			RECON_TEMP_DRREPORT_MUR_REPO.delete(tempRecord);

			// ✅ Insert into audit trail
			audit.insertAudit(userId, userName, "DR-MUR REPORT CANCEL", "NOSTRO CANCELLED SUCCESSFULLY",
					"BRECON_TEMP_DRREPORT_MUR", "DR SUMMARY REPORT - MUR");

			return "Verification Cancelled Successfully!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during cancellation: " + e.getMessage();
		}
	}
}
