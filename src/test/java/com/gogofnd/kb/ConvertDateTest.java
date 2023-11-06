package com.gogofnd.kb;

import com.gogofnd.kb.global.provider.KB_AES_Encryption2;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;

@SpringBootTest
public class ConvertDateTest {



//    @Test
//    void tt() throws IOException, FirebaseMessagingException {
//        service.sendAndroid();
//    }
//    @Test
//    void test(){
//        String date = "20220415";
//        String convertDate = convertDate(date);
//        System.out.println("convertDate = " + convertDate);
//    }
    
//    @Test
//    void phoneSubstring(){
//        String str = "01092494441";
//        System.out.println("str.substring(3) = " + str.substring(3));
//    }
//    @Test
//    void testString(){
//        String date = "012";
//        System.out.println(date.substring(1,2));
//    }


//    @Test
//    void cutcut(){
//        String str = "https://mdirect.kbinsure.co.kr/partners/gogo.html#/gogoDesignAgree?enc_token=77f7db34afff0744346fbd9470c8da64522873cd9744525abff80f7f7975fe0e&driver_id=GG92494441&return_url=http:%2F%2Fgogora.co.kr:9888%2Fapi%2Fgoplan%2F1%2Freturn";
//        str = str.replace("https","http");
//
//        System.out.println("str = " + str);
//    }


//    @Test
//    void now(){
//        System.out.println(LocalDateTime.now());
//    }


//    @Test
//    void testCallIdConvert(){
//        String date = "202206141544";
//        String convertDate = convertCallIdToDate(date);
//        System.out.println("convertDate = " + convertDate);
//    }

//
//    @Test
//    void test2() throws Exception {
//        String key = "1vE3ePU5VGb8u0wN99yzy0r8DAgxs5vZ";
//        String key_ = key.substring(0,16);
//
//        KB_AES_Encryption2 kb_aes_encryption2 = new KB_AES_Encryption2();
//        String encrypt = kb_aes_encryption2.encrypt("9608221111111");
//        System.out.println("encrypt = " + encrypt);
//        String decrypt = kb_aes_encryption2.decrypt(encrypt);
//        System.out.println("decrypt = " + decrypt);
//        System.out.println("key_ = " + key_);
//    }




    String convertDate(String date){
        String substring1 = date.substring(0, 4);
        String substring2 = date.substring(4,6);
        String substring3 = date.substring(6);

        return substring1+"-"+substring2+"-"+substring3;
    }

    String convertCallIdToDate(String date){
        String substring1 = date.substring(0, 4);
        String substring2 = date.substring(4,6);
        String substring3 = date.substring(6,8);
        String substring4 = date.substring(8,10);
        String substring5 = date.substring(10);

        return substring1+"-"+substring2+"-"+substring3+ " "+ substring4 +":" + substring5;
    }
}
