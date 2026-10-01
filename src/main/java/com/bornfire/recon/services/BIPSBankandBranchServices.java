package com.bornfire.recon.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.recon.config.SequenceGenerator;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.BANK_AND_BRANCH_BEAN;
import com.bornfire.recon.entities.BANK_AND_BRANCH_BEANMOD;
import com.bornfire.recon.entities.BANK_AND_BRANCH_MOD_REPO;
import com.bornfire.recon.entities.BANK_AND_BRANCH_REPO;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.USER_PROFILE_ENTITY;
import com.bornfire.recon.entities.USER_PROFILE_REPO;

@Service
@Transactional
@ConfigurationProperties("output")
public class BIPSBankandBranchServices {

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	BANK_AND_BRANCH_REPO bipsSolRepository;

	@Autowired
	BANK_AND_BRANCH_MOD_REPO bipsSolModRepository;

	@Autowired
	SequenceGenerator sequence;

	@Autowired
	BRECON_AUDIT_REPO AuditRepo;

	@Autowired
	USER_PROFILE_REPO userProfileRep;
	
	@Autowired
	AuidtConfigure audit;

	private static final Logger logger = LoggerFactory.getLogger(BIPSBankandBranchServices.class);

	public String modDetails(BANK_AND_BRANCH_BEAN branchBean, String formmode, String user, String userName) {
		String msg = "";

		try {
			if ("add".equalsIgnoreCase(formmode)) {
				// === ADD ===
				BANK_AND_BRANCH_BEANMOD tempBranch = new BANK_AND_BRANCH_BEANMOD(branchBean);
				tempBranch.setEntity_flg("N");
				tempBranch.setModify_flg("N");
				tempBranch.setDel_flg("N");
				tempBranch.setNew_bank_flg("Y");
				tempBranch.setEntry_user(user);
				tempBranch.setEntry_time(new Date());
				tempBranch.setModify_user(user);
				tempBranch.setModify_time(new Date());

				bipsSolModRepository.save(tempBranch);
				msg = "New entry saved!";

				// === AUDIT ===
				audit.insertAudit(user, userName, "ORGANISATION AND BRANCH CREATION", "BRANCH CREATED SUCCESSFULLY","BIPS_BANK_AND_BRANCH_TABLE", "BANK AND BRANCH MASTER");

			} else if ("edit".equalsIgnoreCase(formmode)) {
				// === EDIT ===
				BANK_AND_BRANCH_BEAN mainBranch = bipsSolRepository.findByIdcustom(branchBean.getSol_id());

				if (mainBranch != null) {
					boolean noChange = Objects.equals(mainBranch.getSol_desc(), branchBean.getSol_desc())
							&& Objects.equals(mainBranch.getSol_type(), branchBean.getSol_type())
							&& Objects.equals(mainBranch.getBank_code(), branchBean.getBank_code())
							&& Objects.equals(mainBranch.getBank_name(), branchBean.getBank_name())
							&& Objects.equals(mainBranch.getParm_1(), branchBean.getParm_1())
							&& Objects.equals(mainBranch.getParm_2(), branchBean.getParm_2())
							&& Objects.equals(mainBranch.getAddr_1(), branchBean.getAddr_1())
							&& Objects.equals(mainBranch.getAddr_2(), branchBean.getAddr_2())
							&& Objects.equals(mainBranch.getCity_code(), branchBean.getCity_code())
							&& Objects.equals(mainBranch.getState_code(), branchBean.getState_code())
							&& Objects.equals(mainBranch.getCountry_code(), branchBean.getCountry_code())
							&& Objects.equals(mainBranch.getZip_code(), branchBean.getZip_code());

					if (noChange) {
						msg = "No Modification done";
					} else {
						BANK_AND_BRANCH_BEANMOD tempBranch = new BANK_AND_BRANCH_BEANMOD(branchBean);
						tempBranch.setEntry_user(mainBranch.getEntry_user());
						tempBranch.setEntry_time(mainBranch.getEntry_time());
						tempBranch.setModify_user(user);
						tempBranch.setModify_time(new Date());
						tempBranch.setEntity_flg("N");
						tempBranch.setModify_flg("Y");
						tempBranch.setDel_flg("N");
						tempBranch.setNew_bank_flg("N");

						bipsSolModRepository.save(tempBranch);

						bipsSolRepository.delete(mainBranch);

						msg = "Branch Updated Successfully!";

						// === AUDIT LOG ===
						StringBuilder changes = new StringBuilder();
						if (!Objects.equals(mainBranch.getSol_desc(), branchBean.getSol_desc()))
							changes.append(
									"Sol Desc+" + mainBranch.getSol_desc() + "+" + branchBean.getSol_desc() + "||");
						if (!Objects.equals(mainBranch.getSol_type(), branchBean.getSol_type()))
							changes.append(
									"Sol Type+" + mainBranch.getSol_type() + "+" + branchBean.getSol_type() + "||");
						if (!Objects.equals(mainBranch.getBank_code(), branchBean.getBank_code()))
							changes.append(
									"Bank Code+" + mainBranch.getBank_code() + "+" + branchBean.getBank_code() + "||");
						if (!Objects.equals(mainBranch.getBank_name(), branchBean.getBank_name()))
							changes.append(
									"Bank Name+" + mainBranch.getBank_name() + "+" + branchBean.getBank_name() + "||");
						if (!Objects.equals(mainBranch.getParm_1(), branchBean.getParm_1()))
							changes.append(
									"Branch Code+" + mainBranch.getParm_1() + "+" + branchBean.getParm_1() + "||");
						if (!Objects.equals(mainBranch.getParm_2(), branchBean.getParm_2()))
							changes.append(
									"Branch Name+" + mainBranch.getParm_2() + "+" + branchBean.getParm_2() + "||");
						if (!Objects.equals(mainBranch.getAddr_1(), branchBean.getAddr_1()))
							changes.append("Address 1+" + mainBranch.getAddr_1() + "+" + branchBean.getAddr_1() + "||");
						if (!Objects.equals(mainBranch.getAddr_2(), branchBean.getAddr_2()))
							changes.append("Address 2+" + mainBranch.getAddr_2() + "+" + branchBean.getAddr_2() + "||");
						if (!Objects.equals(mainBranch.getCity_code(), branchBean.getCity_code()))
							changes.append(
									"City+" + mainBranch.getCity_code() + "+" + branchBean.getCity_code() + "||");
						if (!Objects.equals(mainBranch.getState_code(), branchBean.getState_code()))
							changes.append(
									"State+" + mainBranch.getState_code() + "+" + branchBean.getState_code() + "||");
						if (!Objects.equals(mainBranch.getCountry_code(), branchBean.getCountry_code()))
							changes.append("Country+" + mainBranch.getCountry_code() + "+"
									+ branchBean.getCountry_code() + "||");
						if (!Objects.equals(mainBranch.getZip_code(), branchBean.getZip_code()))
							changes.append(
									"Zip Code+" + mainBranch.getZip_code() + "+" + branchBean.getZip_code() + "||");

						audit.insertAudit(user, userName, "ORGANISATION AND BRANCH MODIFICATION", "BRANCH MODIFIED SUCCESSFULLY","BIPS_BANK_AND_BRANCH_TABLE", "BANK AND BRANCH MASTER");
					}
				} else {
					msg = "Branch not found!";
				}

			} else if ("verify".equalsIgnoreCase(formmode)) {
				// === VERIFY ===
				BANK_AND_BRANCH_BEANMOD tempBranch = bipsSolModRepository.findByIdcustom(branchBean.getSol_id());
				if (tempBranch != null) {
					BANK_AND_BRANCH_BEAN mainBranch = new BANK_AND_BRANCH_BEAN();
					BeanUtils.copyProperties(tempBranch, mainBranch);

					mainBranch.setDel_flg("N");
					mainBranch.setModify_flg("N");
					mainBranch.setEntity_flg("Y");
					mainBranch.setVerify_user(user);
					mainBranch.setVerify_time(new Date());

					bipsSolRepository.save(mainBranch);
					bipsSolModRepository.delete(tempBranch);

					msg = "Branch Verified Successfully!";

					// === AUDIT LOG ===
					audit.insertAudit(user, userName, "ORGANISATION AND BRANCH VERIFICATION", "BRANCH VERIFIED SUCCESSFULLY","BIPS_BANK_AND_BRANCH_TABLE", "BANK AND BRANCH MASTER");

				} else {
					msg = "Branch not found!";
				}

			} else if ("cancel".equalsIgnoreCase(formmode)) {
				// === CANCEL/DELETE ===
				BANK_AND_BRANCH_BEANMOD tempBranch = bipsSolModRepository.findByIdcustom(branchBean.getSol_id());
				if (tempBranch != null) {
					tempBranch.setDel_flg("Y");
					tempBranch.setEntity_flg("N");
					tempBranch.setModify_user(user);
					tempBranch.setModify_time(new Date());
					bipsSolModRepository.save(tempBranch);

					msg = "Branch Deleted Successfully";

					// === AUDIT ===
					audit.insertAudit(user, userName, "ORGANISATION AND BRANCH DELETE", "BRANCH DELETED SUCCESSFULLY","BIPS_BANK_AND_BRANCH_TABLE", "BANK AND BRANCH MASTER");

				} else {
					msg = "Branch not found!";
				}
			}

		} catch (Exception e) {
			msg = "Error occurred. Please contact Administrator.";
			e.printStackTrace();
		}

		return msg;
	}

	public BANK_AND_BRANCH_BEAN getSolID(String solId) {
		BANK_AND_BRANCH_BEAN up = bipsSolRepository.findByIdcustom(solId);
		return up;
	}

	public BANK_AND_BRANCH_BEANMOD getModSolID(String solId) {
		BANK_AND_BRANCH_BEANMOD up = bipsSolModRepository.findByIdcustom(solId);
		return up;
	}

	public List<BANK_AND_BRANCH_BEAN> BankandBranchList(PageRequest of) {
		// TODO Auto-generated method stub

		List<BANK_AND_BRANCH_BEAN> data = bipsSolRepository.findAllByCustom();
		return data;
	}

}
