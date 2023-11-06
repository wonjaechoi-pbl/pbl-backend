package com.gogofnd.kb.global.domain.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StringToLocalDateTime {

    public static LocalDateTime convertLocalDateTime(String date){
        if(date == null || date.equals("")){
            return null;
        }
        return  LocalDateTime.of(LocalDate.parse(date, DateTimeFormatter.ISO_DATE), LocalTime.MIN);
    }
}
