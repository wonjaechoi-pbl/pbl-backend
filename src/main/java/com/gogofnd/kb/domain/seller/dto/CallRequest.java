package com.gogofnd.kb.domain.seller.dto;

import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallRequest {

    private String driver_id;
    private String call_id;
    private String pickUp_Address;
    private String delivery_Address;
    private String companyName;
    private Long call_request_time;
    private LocalDateTime call_appoint_time;

}
