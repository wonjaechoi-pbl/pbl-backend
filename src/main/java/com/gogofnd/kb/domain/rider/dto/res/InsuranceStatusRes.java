package com.gogofnd.kb.domain.rider.dto.res;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceStatusRes {

    private String driver_step;

    private String driver_status;

    private String driver_message;

}
