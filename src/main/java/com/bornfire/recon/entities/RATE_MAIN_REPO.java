package com.bornfire.recon.entities;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.recon.entities.upload.RECON_UPI_SOURCE_ENTITY;

@Repository
public interface RATE_MAIN_REPO extends CrudRepository<RATE_MAIN_ENTITY,String> {
	
	@Query(value = "SELECT NVL(MAX(version), 0) FROM RATE_MAINTENANCE", nativeQuery = true)
	BigDecimal findMaxversion();

	@Query(value = "SELECT rm.* " + "FROM RATE_MAINTENANCE rm " + "JOIN ( "
			+ "   SELECT fxd_crncy, var_crncy, MAX(version) AS max_ver " + "   FROM RATE_MAINTENANCE "
			+ "   WHERE VERIFY_FLG = 'Y' " + "   GROUP BY fxd_crncy, var_crncy "
			+ ") latest ON rm.fxd_crncy = latest.fxd_crncy " + "AND rm.var_crncy = latest.var_crncy "
			+ "AND rm.version = latest.max_ver " + "WHERE rm.VERIFY_FLG = 'Y'", nativeQuery = true)
	List<RATE_MAIN_ENTITY> fetchMaxVersionRates();
	
	@Query(value = "SELECT BRECON_RATE.NEXTVAL FROM DUAL", nativeQuery = true)
	String getNextAuditSrlNo();

	@Query(value = "SELECT rm.* " +
	        "FROM RATE_MAINTENANCE rm " +
	        "JOIN ( " +
	        "    SELECT MAX(rm2.VERSION) AS VERSION, rm2.FXD_CRNCY, rm2.VAR_CRNCY " +
	        "    FROM RATE_MAINTENANCE rm2 " +
	        "    WHERE TRUNC(rm2.ENTRY_TIME) = ( " +
	        "        SELECT MAX(TRUNC(ENTRY_TIME)) " +
	        "        FROM RATE_MAINTENANCE " +
	        "        WHERE TRUNC(ENTRY_TIME) < TRUNC(SYSDATE) " +
	        "    ) " +
	        "    AND rm2.VERIFY_FLG = 'Y' " +
	        "    GROUP BY rm2.FXD_CRNCY, rm2.VAR_CRNCY " +
	        ") sub ON rm.VERSION = sub.VERSION " +
	        "     AND rm.FXD_CRNCY = sub.FXD_CRNCY " +
	        "     AND rm.VAR_CRNCY = sub.VAR_CRNCY " +
	        "WHERE TRUNC(rm.ENTRY_TIME) = ( " +
	        "    SELECT MAX(TRUNC(ENTRY_TIME)) " +
	        "    FROM RATE_MAINTENANCE " +
	        "    WHERE TRUNC(ENTRY_TIME) < TRUNC(SYSDATE) " +
	        ") " +
	        "AND rm.VERIFY_FLG = 'Y'", nativeQuery = true)
	List<RATE_MAIN_ENTITY> findLatestVerifiedRatesExcludingToday();

	@Query(value = "SELECT * FROM RATE_MAINTENANCE WHERE TRUNC(RATE_DATE) = TO_DATE(:entryDate, 'dd-MM-yyyy') UNION ALL "
			+ "SELECT * FROM RATE_MAINTENANCE_MOD WHERE  TRUNC(RATE_DATE) = TO_DATE(:entryDate, 'dd-MM-yyyy')", nativeQuery = true)
	List<RATE_MAIN_ENTITY> findByEntryDate(@Param("entryDate") String entryDate);

	/*
	 * @Query(value = "SELECT * FROM RATE_MAINTENANCE", nativeQuery = true)
	 * List<RATE_MAIN_ENTITY> getList();
	 */
	
	@Query(value = "SELECT * FROM RATE_MAINTENANCE WHERE entity_flg = 'Y' AND del_flg = 'N' UNION ALL SELECT * FROM RATE_MAINTENANCE_MOD WHERE entity_flg = 'N' AND del_flg = 'N'", nativeQuery = true)
	List<RATE_MAIN_ENTITY> getList();

	@Query(value = "SELECT COALESCE(MAX(SRL_NO), 0) FROM RATE_MAINTENANCE", nativeQuery = true)
	BigDecimal getNextSrl();

	@Query("FROM RATE_MAIN_ENTITY WHERE unique_id = :unique_id")
	RATE_MAIN_ENTITY findByRateCode(String unique_id);
	
	// ✅ Check if combination exists
	@Query(value = "SELECT CASE WHEN COUNT(1) > 0 THEN 1 ELSE 0 END " + "FROM RATE_MAINTENANCE r "
			+ "WHERE r.fxd_crncy = :fxd AND r.var_crncy = :var AND to_char(EFF_DATE,'DD-MM-YYYY') = :date", nativeQuery = true)
	int existsByFxdAndVar(@Param("fxd") String fxd, @Param("var") String var ,  @Param("date") String date);

	// ✅ Delete existing combination
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM RATE_MAINTENANCE r "
			+ "WHERE r.fxd_crncy = :fxd AND r.var_crncy = :var AND to_char(EFF_DATE,'DD-MM-YYYY') = :date", nativeQuery = true)
	void deleteByFxdAndVar(@Param("fxd") String fxd, @Param("var") String var,  @Param("date") String date);
	
	@Query(value = "SELECT COUNT(*) FROM rate_maintenance WHERE trunc(EFF_DATE) = TO_DATE(:fromDate,'DD-MM-YYYY')", nativeQuery = true)
	int getRateCount(@Param("fromDate") String fromDate);
}
