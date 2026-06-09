package com.wj.aisoulmatechat.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class AgeCulUtil {
    //通过生日计算年龄
    public static int getAge(String birth){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(birth, formatter);
        LocalDate now = LocalDate.now();

        if (birthDate.isAfter(now)) {
            return 0;
        }
        int age = Period.between(birthDate, now).getYears();
        return age ;
    }
}
