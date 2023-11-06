package com.gogofnd.kb.domain.delivery.dto.app.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class DeliveryAppEndReq {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String call_id;
    private String end_time;
    private String auto_end;

}
