package com.gogofnd.kb.domain.insurance.service;

import com.gogofnd.kb.global.provider.AES_Encryption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceServiceTest {

    @Test
    void decryptSavedSsn() throws Exception {
        String s = aesDecode("9jFCkeck/Ycbta640TeMkuJ8FigvlcSfFcjytpaNu0t0Era64/SrmjBd978qd7lkSrD2KUZYx5kg0MBEx0SdqWbkq2KcBHTmtZ6o9zfDdSZvdcf8eI/+Nzg+FQEl9FMVGXznxseQlOY1Bp9D07RSfKm3OfjVO8IBuvexzLMUUZ5cCBQE1V4+uiwwCkNNe/ei");
        System.out.println("s = " + s);
    }

    private String aesEncode(String str) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String encrypt = aes.encrypt(str);
        return encrypt;
    }

    private String aesDecode(String str) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(str);
        return decrypt;
    }

}