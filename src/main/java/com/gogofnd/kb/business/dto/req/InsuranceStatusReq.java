package com.gogofnd.kb.business.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InsuranceStatusReq {
    @Size(max = 30)
    private String driver_id;

    @NotBlank
    private String seller_code;
}
