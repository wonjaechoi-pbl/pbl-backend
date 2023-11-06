package com.gogofnd.kb.business.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RegistrationStatusReq {
    @Size(max = 30)
    private String name;
    private String driver_ssn;
    @NotBlank
    private String seller_code;
}
