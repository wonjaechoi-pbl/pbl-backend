package com.gogofnd.kb.business.dto.res;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Data
public class CallsEndRes {
    private String call_id;
    private String driver_id;
    private String seller_code;
    private String driver_enddate;
}