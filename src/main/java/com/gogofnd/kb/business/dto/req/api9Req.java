package com.gogofnd.kb.business.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class api9Req {
    private Long id;
    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String vcno_hngl_nm;
    private String name;
    private String ssn;
    private String policy_number;
}
