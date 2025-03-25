package com.springboot.banking_app.utils;

import java.time.Year;

public class AccountUtils {

    public static String gererateAccountNumber() {
        Year currentYear = Year.now();
        int min = 1000000;
        int max = 9999999;

        int random = (int)Math.floor(Math.random()* (max - min + 1)+min);

        String year = String.valueOf(currentYear);
        String randomNumber = String.valueOf(random);

        return year + randomNumber;
    }
}
