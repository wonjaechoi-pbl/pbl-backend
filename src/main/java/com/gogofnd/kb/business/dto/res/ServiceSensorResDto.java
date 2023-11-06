package com.gogofnd.kb.business.dto.res;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServiceSensorResDto {

    private String sensor_status;
    private String sever_message;
    private String insurance_status;

    public ServiceSensorResDto(String sensor_status, String sever_message, String insurance_status) {
        this.sensor_status = sensor_status;
        this.sever_message = sever_message;
        this.insurance_status = insurance_status;
    }
}
