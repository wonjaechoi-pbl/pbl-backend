package com.gogofnd.kb;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PadTest {
    public String padLeft(String s, int n) {
        return String.format("%0" + n + "d", Integer.parseInt(s));
    }

    @Test
    public void leftPad(){
        String test1 = padLeft("144", 15);
        System.out.println("test1 = " + test1);
        String test2 = padLeft("144456768", 15);
        System.out.println("test2 = " + test2);

    }

    @Test
    public void cuttttt(){
        String str = "https://www.naver.com";
        cut(str);
    }


    void cut(String str){
        String substring = str.substring(8);
        System.out.println("substring = " + substring);
    }

    @Test
    public void dateFormat(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        System.out.println("now = " + now);
    }
}
