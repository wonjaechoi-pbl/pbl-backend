package com.gogofnd.kb.domain.insurance.dto.req;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApi8thReq {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String vcno_hngl_nm;
    private String name;
    private String result;
    private String policy_number;
    private List<Integer> effective_time;
    private String auto_cancel;
}
