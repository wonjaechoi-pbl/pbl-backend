package com.gogofnd.kb.domain.rider.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RiderSaveReq {
    private String name;
    private String phone;
    private String vcNumber;
    private String sellerCode;
    private String region;
    private String ssn;
    @NotNull
    private String password;

}
