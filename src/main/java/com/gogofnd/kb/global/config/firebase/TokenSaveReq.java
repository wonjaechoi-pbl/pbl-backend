package com.gogofnd.kb.global.config.firebase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TokenSaveReq {
    private String token;
    private String phone;
}
