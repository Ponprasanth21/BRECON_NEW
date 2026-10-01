package com.bornfire.recon.config;

import java.text.DecimalFormat;

public class IndianNumberFormatter {

    public static String format(Number amount) {
        if (amount == null) {
            return "0.00"; // Return 0.00 instead of empty
        }

        // Format the number with two decimal places
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(amount);

        // Split integer part and decimal part
        String[] parts = formattedAmount.split("\\.");
        String intPart = parts[0];
        String decimalPart = parts.length > 1 ? "." + parts[1] : "";

        // Handle negative numbers
        boolean negative = false;
        if (intPart.startsWith("-")) {
            negative = true;
            intPart = intPart.substring(1);
        }

        StringBuilder result = new StringBuilder();

        int length = intPart.length();
        if (length > 3) {
            String lastThree = intPart.substring(length - 3);
            String rest = intPart.substring(0, length - 3);

            // Build the rest part with commas every 2 digits, from right to left
            StringBuilder restBuilder = new StringBuilder(rest);
            StringBuilder restWithCommas = new StringBuilder();

            int count = 0;
            for (int i = restBuilder.length() - 1; i >= 0; i--) {
                restWithCommas.insert(0, restBuilder.charAt(i));
                count++;

                if (count == 2 && i != 0) {
                    restWithCommas.insert(0, ',');
                    count = 0;
                }
            }

            // Combine everything
            result.append(restWithCommas);
            result.append(',');
            result.append(lastThree);

        } else {
            // Numbers with length <= 3 don't need commas
            result.append(intPart);
        }

        // Add negative sign back if needed
        if (negative) {
            result.insert(0, '-');
        }

        // Append decimal part
        result.append(decimalPart);

        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(IndianNumberFormatter.format(null));       // Output: 0.00
        System.out.println(IndianNumberFormatter.format(0));          // Output: 0.00
        System.out.println(IndianNumberFormatter.format(2900000.00)); // Output: 29,00,000.00
    }
}
