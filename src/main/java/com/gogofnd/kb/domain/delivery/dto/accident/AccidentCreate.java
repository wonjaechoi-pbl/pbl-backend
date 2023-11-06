package com.gogofnd.kb.domain.delivery.dto.accident;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccidentCreate {
    private String proxy_driv_coorp_cmpcd;
    @NotEmpty
    private String call_id;
    private String claim_number;
    private Long claim_time;
    private Long accident_time;
}
