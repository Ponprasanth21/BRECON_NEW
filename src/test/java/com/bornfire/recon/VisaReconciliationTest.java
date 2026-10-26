package com.bornfire.recon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VisaReconciliationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.bornfire.recon.services.SourceDataUploadService sourceDataUploadService;

    @Test
    public void testSqlLoaderResolution() {
        boolean available = sourceDataUploadService.isSqlLoaderAvailable();
        System.out.println(">>> SQL*Loader Available on this system: " + available);
    }

    @Test
    public void testVisaReconciliationExecution() {
        // Verify procedure is valid
        String status = jdbcTemplate.queryForObject(
            "SELECT STATUS FROM ALL_OBJECTS WHERE OBJECT_NAME = 'SP_EXECUTE_VISA_RECON' AND OWNER = 'BRECON'",
            String.class
        );
        assertEquals("VALID", status, "SP_EXECUTE_VISA_RECON procedure must be VALID");

        // Verify counts across tables
        Integer cbsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRECON.CBS_TXN", Integer.class);
        Integer posCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRECON.SWITCH_POS", Integer.class);
        Integer atmCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRECON.SWITCH_ATM", Integer.class);
        Integer reconCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BRECON.RECON_TXN", Integer.class);

        assertNotNull(cbsCount);
        assertNotNull(posCount);
        assertNotNull(atmCount);
        System.out.println(">>> COUNTS: CBS=" + cbsCount + ", POS=" + posCount + ", ATM=" + atmCount + ", RECON=" + reconCount);
        assertTrue(cbsCount >= 0, "CBS_TXN count should be non-negative");
        assertTrue(posCount >= 0, "SWITCH_POS count should be non-negative");
        assertTrue(atmCount >= 0, "SWITCH_ATM count should be non-negative");
        assertTrue(reconCount >= 0, "RECON_TXN count should be non-negative");

        // Verify failed refund records count query succeeds
        Integer failedRefundCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM BRECON.RECON_TXN WHERE RECON_STATUS = 'FAILED_REFUND'",
            Integer.class
        );
        assertNotNull(failedRefundCount);
        assertTrue(failedRefundCount >= 0);
    }

    @Test
    public void testSampleAtmRecord() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT HEADX_CRD_PAN, AUTHX_SEQ_NUM, AUTHX_AUTH_ID_RESP, AUTHX_AMT_1, TXN_DT, AUTHX_RESP_CDE_R_RESP_BYTE_1, AUTHX_TYP " +
            "FROM BRECON.SWITCH_ATM WHERE AUTHX_AUTH_ID_RESP IS NOT NULL AND ROWNUM <= 3"
        );
        for (Map<String, Object> r : rows) {
            System.out.println("SAMPLE_ATM -> PAN: " + r.get("HEADX_CRD_PAN") + 
                               ", RRN: " + r.get("AUTHX_SEQ_NUM") + 
                               ", AUTH: " + r.get("AUTHX_AUTH_ID_RESP") + 
                               ", AMT: " + r.get("AUTHX_AMT_1") + 
                               ", DT: " + r.get("TXN_DT") + 
                               ", RESP: " + r.get("AUTHX_RESP_CDE_R_RESP_BYTE_1") + 
                               ", TYP: " + r.get("AUTHX_TYP"));
        }
    }

    @Test
    public void testThreeWaySettlementVerification() {
        String testCard = "4627530231044150";
        String testRrn = "627306776652";
        String testAuth = "092831";
        String testAmt = "1500.00";
        String testDate = "2026-10-16";

        // Clean any existing test records
        jdbcTemplate.update("DELETE FROM BRECON.SWITCH_POS WHERE AUTHX_SEQ_NUM = ?", testRrn);
        jdbcTemplate.update("DELETE FROM BRECON.CBS_TXN WHERE PSTD_USER_ID = ?", testRrn);
        jdbcTemplate.update("DELETE FROM BRECON.VISA_EPIN WHERE AUTH_CODE = ?", testAuth);
        jdbcTemplate.update("DELETE FROM BRECON.VISA_EPIN745 WHERE RET_REF_NUM = ?", testRrn);
        jdbcTemplate.update("DELETE FROM BRECON.RECON_TXN WHERE RRN = ?", testRrn);

        // 1. Insert Switch POS record
        jdbcTemplate.update(
            "INSERT INTO BRECON.SWITCH_POS (HEADX_CRD_CARD_CRD_NUM, AUTHX_SEQ_NUM, AUTHX_APPRV_CDE, AUTHX_AMT_1, TRAN_DT, AUTHX_RESP_CDE, AUTHX_TYP, HEADX_REC_TYP, DEL_FLAG, ENTITY_FLAG, MODIFY_FLAG, ENTRY_TIME, ENTRY_USER) " +
            "VALUES (?, ?, ?, ?, ?, '00', '200', '01', 'N', 'Y', 'N', SYSTIMESTAMP, 'TEST')",
            testCard, testRrn, testAuth, testAmt, testDate
        );

        // 2. Insert CBS record (Auth ID starts as NULL to verify automatic Switch-enrichment via RRN!)
        jdbcTemplate.update(
            "INSERT INTO BRECON.CBS_TXN (TRAN_DATE, TRAN_ID, PART_TRAN_SRL_NUM, DEL_FLG, TRAN_AMT, TRAN_CRNCY_CODE, PSTD_USER_ID, PRNT_ADVC_IND, AUTH_ID) " +
            "VALUES ('16-10-2026', 'TXN_TEST_001', '1', 'N', ?, 'INR', ?, ?, NULL)",
            testAmt, testRrn, testCard
        );

        // 3. Insert VISA EPIN record (shares common Auth ID)
        jdbcTemplate.update(
            "INSERT INTO BRECON.VISA_EPIN (ACCT_NO, AUTH_CODE, DEST_AMT, PURCHASE_DATE, ENTRY_TIME, ENTRY_USER) " +
            "VALUES (?, ?, ?, TO_DATE('2026-10-16', 'YYYY-MM-DD'), SYSDATE, 'TEST')",
            testCard, testAuth, Double.parseDouble(testAmt)
        );

        // Also insert VISA EPIN745 record (shares common RRN)
        jdbcTemplate.update(
            "INSERT INTO BRECON.VISA_EPIN745 (CARD_NUM, RET_REF_NUM, TRAN_AMOUNT, TRAN_DATE, RSP_CD, ENTRY_TIME, ENTRY_USER) " +
            "VALUES (?, ?, ?, '1610', '00', SYSDATE, 'TEST')",
            testCard, testRrn, Double.parseDouble(testAmt)
        );

        // Deploy updated SP_EXECUTE_VISA_RECON procedure
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/sql/SP_EXECUTE_VISA_RECON.sql"));
            String sql = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            int procIdx = sql.indexOf("CREATE OR REPLACE PROCEDURE");
            if (procIdx >= 0) {
                String procBody = sql.substring(procIdx).trim();
                jdbcTemplate.execute(procBody);
            }
        } catch (Exception e) {
            System.err.println("Procedure deployment warning: " + e.getMessage());
        }

        // 4. Run reconciliation stored procedure
        sourceDataUploadService.executeVisaReconciliation(testDate);

        // 5. Verify CBS was enriched with Auth ID from Switch
        String cbsAuth = jdbcTemplate.queryForObject(
            "SELECT AUTH_ID FROM BRECON.CBS_TXN WHERE PSTD_USER_ID = ?",
            String.class,
            testRrn
        );
        assertEquals(testAuth, cbsAuth, "CBS AUTH_ID should be enriched from Switch");

        // 6. Verify 3-way match in RECON_TXN
        Map<String, Object> recon = jdbcTemplate.queryForMap(
            "SELECT RECON_STATUS, SOURCE_CBS, SOURCE_VISA, SOURCE_SWITCH, REASON, RECON_KEY " +
            "FROM BRECON.RECON_TXN WHERE RRN = ?",
            testRrn
        );
        System.out.println(">>> 3-WAY RECON RESULT: " + recon);
        assertEquals("SETTLED", recon.get("RECON_STATUS"), "3-way matched transaction must be SETTLED");
        assertEquals("Y", recon.get("SOURCE_CBS"));
        assertEquals("Y", recon.get("SOURCE_VISA"));
        assertEquals("Y", recon.get("SOURCE_SWITCH"));
    }

    @Test
    public void testInspectDataTypes() {
        for (String table : new String[]{"CBS_TXN", "SWITCH_POS", "SWITCH_ATM", "VISA_EPIN", "VISA_EPIN745"}) {
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH FROM ALL_TAB_COLUMNS WHERE OWNER = 'BRECON' AND TABLE_NAME = ? ORDER BY COLUMN_ID",
                table
            );
            System.out.println("=== TABLE: " + table + " (" + cols.size() + " columns) ===");
            for (Map<String, Object> c : cols) {
                String dt = (String) c.get("DATA_TYPE");
                if (!"VARCHAR2".equalsIgnoreCase(dt)) {
                    System.out.println("   " + c.get("COLUMN_NAME") + " -> " + dt + " (" + c.get("DATA_LENGTH") + ")");
                }
            }
        }
    }
}
