package com.gogofnd.kb.domain.insurance.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KbApiUnderWritingResult {
    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String vcno_hngl_nm;
    private String result;
    private Integer until;

    public void changeUntil(Integer until) {
        this.until = until;
    }
}
