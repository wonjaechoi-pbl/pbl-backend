package com.gogofnd.kb.domain.insurance.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class KbApi12thReq {

    private String proxy_driv_coorp_cmpcd;
    private String driver_id;
    private String call_id;
    private String policy_number;
    private Long start_time;
    private Long end_time;
    private String target_location;
    //밍 시간제보험 call 보낼때, total_time으로 보내기 여기서 total_time은 운행한 총 시간
    private String total_time;
}
