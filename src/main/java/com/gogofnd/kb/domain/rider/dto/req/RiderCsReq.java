package com.gogofnd.kb.domain.rider.dto.req;

import com.gogofnd.kb.global.domain.application.StringToLocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RiderCsReq {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String keyword;

    private String searchField;

    private String cmpcd;

    public RiderCsReq(String keyword,String searchField,String startDate,String endDate){
        this.keyword = keyword;
        this.searchField = searchField;
        this.startDate = StringToLocalDateTime.convertLocalDateTime(startDate);
        this.endDate = StringToLocalDateTime.convertLocalDateTime(endDate);
    }

    public RiderCsReq(String keyword,String searchField,String startDate,String endDate, String cmpcd){
        this.keyword = keyword;
        this.searchField = searchField;
        this.cmpcd = cmpcd;
        this.startDate = StringToLocalDateTime.convertLocalDateTime(startDate);
        this.endDate = StringToLocalDateTime.convertLocalDateTime(endDate);
    }
}
