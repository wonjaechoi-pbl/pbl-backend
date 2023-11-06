package com.gogofnd.kb.business.dto.req;

import com.gogofnd.kb.global.domain.application.StringToLocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallsListReq {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String keyword;

    private String searchField;

    public CallsListReq(String keyword,String searchField,String startDate,String endDate){
        this.keyword = keyword;
        this.searchField = searchField;
        this.startDate = StringToLocalDateTime.convertLocalDateTime(startDate);
        this.endDate = StringToLocalDateTime.convertLocalDateTime(endDate);
    }
}
