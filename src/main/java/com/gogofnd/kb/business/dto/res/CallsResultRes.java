package com.gogofnd.kb.business.dto.res;

import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CallsResultRes {
    private Long riderId;
    private String callId;
    private Long balance;
    private LocalDateTime appointTime;
    private LocalDateTime pickupTime;
    private LocalDateTime completeTime;
    private String deliveryAddress;
    private String deliveryStatus;
}
