package com.gogofnd.kb.domain.insurance.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenAndUrlRes {
    private String token;
    private String url;

    public void updateUrlAndTokenAndReturnUrl(String url,String token) {
        this.url = url;
        this.token = token;
    }
}
