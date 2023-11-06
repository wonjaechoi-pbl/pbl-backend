package com.gogofnd.kb.domain.delivery.dto.app.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class DeliveryAppStartReq {

    private String proxy_drive_coorp_cmpcd;
    private String driver_id;
    private String call_id;
    private String policy_number;
    private Long start_time;
    private String restart;

}

