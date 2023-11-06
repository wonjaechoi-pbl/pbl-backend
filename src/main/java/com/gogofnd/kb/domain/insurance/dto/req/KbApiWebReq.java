package com.gogofnd.kb.domain.insurance.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter

@NoArgsConstructor
@AllArgsConstructor
public class KbApiWebReq {
    private String driver_id;
    private String driver_phone;
    private String driver_name;
    private String driver_ssn;
    private int driver_gender;
    private String seller_code;
    private String driver_vcnum;
    private String driver_region;
    private String insuranceStatus;

    // KB쪽 DB tb_rider_info 테이블에 추가로 필요한 컬럼
    private String powerType;
    private int bikeModel;
    private String mobileType;
    private Long runMeter;
    private String oprn_purp;
    private String mtdt;

    //고고세이프용
//    private int bike_model;
//    private String power_type;
//    private String mobile_type;
//    private Long run_meter;

    public void encSsn(String encSsn) {
        this.driver_ssn = encSsn;
    }


    public void setDriver_vcnum(String driver_vcnum) {
        this.driver_vcnum = driver_vcnum;
    }
}
