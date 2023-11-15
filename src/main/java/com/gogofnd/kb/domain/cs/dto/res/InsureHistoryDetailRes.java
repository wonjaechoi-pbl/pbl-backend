package com.gogofnd.kb.domain.cs.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsureHistoryDetailRes {
    private String name;
    private LocalDateTime createdDate;
    private String status;
    private String status_name;
    private String reject_message;
    private LocalDateTime effectiveStartDate;
    private LocalDateTime effectiveEndDate;
    private LocalDateTime until;
}
