package com.gogofnd.kb.domain.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApiSignResultDto{

    private String proxy_driv_coorp_cmpcd;
    private String  driver_id;
    private String vcno_hngl_nm;
    private String result;
    private String policy_number;
    private List<Integer> effective_time;
    private Integer underwriting_after;
}
