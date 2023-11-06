package com.gogofnd.kb.business.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Kb11thRequest {
    @NotBlank
    private String call_id;
    private String driver_id;
    private String driver_enddate;
    @NotBlank
    private String seller_code;
}
