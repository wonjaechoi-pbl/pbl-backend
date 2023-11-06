package com.gogofnd.kb.business.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
public class RiderPhoneReq {
    @NotEmpty
    private String phone;
}
