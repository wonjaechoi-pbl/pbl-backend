package com.gogofnd.kb.business.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateStatusReq {
    private String status;
    private String driverId;
}
