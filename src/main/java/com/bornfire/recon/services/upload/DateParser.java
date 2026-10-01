package com.bornfire.recon.services.upload;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class DateParser {
// to support all format
    private static final String[] patterns = {
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "dd-MMM-yyyy",
        "dd/MMM/yyyy",
        "MM-dd-yyyy",
        "MM/dd/yyyy",
        "yyyy-MM-dd",
        "MMM d, yyyy",
        "dd-MMM-yy" 
    };

    public Date parseDate(String trxn_date) throws ParseException {
        if (trxn_date == null || trxn_date.trim().isEmpty()) {
            throw new ParseException("Transaction date is null or empty", 0);
        }

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
                sdf.setLenient(false);
                return sdf.parse(trxn_date);
            } catch (ParseException e) {
                // Try next pattern
            }
        }

        throw new ParseException("Unparseable date: \"" + trxn_date + "\"", 0);
    }

    public String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        return targetFormat.format(date);
    }
    // to parse string to bigdeciaml
    
    public BigDecimal parseBigDecimal(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) {
        	return null;
        }
        try {
            return new BigDecimal(numberStr.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + numberStr);
            return null;
        }
    }
    
    // to parse string to date
    
    public Date parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            System.out.println("Date is null");
            return null;
        }

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                // Try next pattern
            }
        }

        System.out.println("Unparseable date: " + dateStr);
        return null;
    }
    // to get dd-mm-yyyy
    public Date getCurrentDateWithoutTime() {
        try {
            Date utilDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.parse(sdf.format(utilDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // fallback to current date instead of null
        }
    }
    public String getCurrentDateWithoutTimePass(Date val) {
        if (val == null) {
            return ""; // or return null, depending on your requirement
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(val);
    }
    public String getFormatdd_mm_yyyy(Date val) {
        if (val == null) {
            return ""; // or return null, depending on your requirement
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(val);
    }
}
