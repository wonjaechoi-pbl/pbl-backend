package com.gogofnd.kb.business.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DrivingStartRes {
    private String server_result;
    private String insure_status;
    private String insure_message;
}
