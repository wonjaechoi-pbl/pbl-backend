package com.gogofnd.kb.domain.insurance.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class GogoSafeSignUpReq {
    private String phoneNumber;
    private String name;
    private int bikeModel;
    private String powerType;
    private Long runMeter;
    private String mobileType;
    private String sensorMac;
    private String agree;
    private String appVer;
    private String agreeDate;
    private String runStatus;
    private String bikeNumber;
    private String runDate;
    private String use;

    @Builder
    public GogoSafeSignUpReq(String phoneNumber, String name, int bikeModel, String powerType, Long runMeter, String mobileType, String sensorMac, String agree, String appVer, String agreeDate, String runStatus, String bikeNumber, String runDate, String use) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.bikeModel = bikeModel;
        this.powerType = powerType;
        this.runMeter = runMeter;
        this.mobileType = mobileType;
        this.sensorMac = sensorMac;
        this.agree = agree;
        this.appVer = appVer;
        this.agreeDate = agreeDate;
        this.runStatus = runStatus;
        this.bikeNumber = bikeNumber;
        this.runDate = runDate;
        this.use = use;
    }
}
