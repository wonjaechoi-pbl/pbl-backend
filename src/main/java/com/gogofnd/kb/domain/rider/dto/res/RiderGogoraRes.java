package com.gogofnd.kb.domain.rider.dto.res;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RiderGogoraRes {
    private String driverId;
    private LocalDateTime createdDate;
    private String insuranceStatus;
    private String status_name;
    private String reject_message;
}
