package com.gogofnd.kb.domain.rider.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginRes {
    private String token;
    private String driverId;
    private String insuranceStatus;
}
