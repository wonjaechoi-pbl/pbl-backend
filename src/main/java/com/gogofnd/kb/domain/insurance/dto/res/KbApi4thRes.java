package com.gogofnd.kb.domain.insurance.dto.res;


import com.gogofnd.kb.domain.insurance.dto.KbDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApi4thRes extends KbDto {
    private String result;
    private String url;
    private String token;
    private String return_url;

    public void updateUrlAndTokenAndReturnUrl(String url,String token,String return_url) {
        this.url = url;
        this.token = token;
        this.return_url = return_url;
    }
}
