package com.gogofnd.kb.domain.seller.entity;

import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.seller.dto.CallRequest;
import com.gogofnd.kb.domain.seller.dto.Kb10thRequest;
import com.google.common.base.CharMatcher;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.swing.text.DateFormatter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.time.LocalDateTime.now;

@EntityListeners(AuditingEntityListener.class)
@Table(name = "calls")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
@Entity
public class Call{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="call_id", length = 30)
    private String callId;

    @JoinColumn(name = "rider_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rider rider;

    private long balance;

    @Column(length = 16)
    private String kb_call_id;

    private String companyName;

    @Column(length = 100)
    private String pickUp_Address;
    @Column(length = 100)
    private String delivery_Address;

    @Column(length = 20)
    private String delivery_status;
    @Column(name = "request_time")
    private LocalDateTime callRequestTime; // 주문 요청시간
    @Column(name = "appoint_time")
    private LocalDateTime callAppointTime; // 배차시간

    @Column(name = "pickup_time")
    private LocalDateTime callPickUpTime;

    @Column(name = "complete_time")
    private LocalDateTime callCompleteTime;

    @Column(name = "group_id", length = 30)
    private String groupId;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    private int totalTime;

    private long dailyTotalRiding;

    public static Call create(Rider rider, Kb10thRequest dto, LocalDateTime callRequestTime , LocalDateTime callAppointTime){

        dto.setDriver_deliveryaddress(CharMatcher.anyOf("|¶").removeFrom(dto.getDriver_deliveryaddress()));
        dto.setDriver_pickupaddress(CharMatcher.anyOf("|¶").removeFrom(dto.getDriver_pickupaddress()));

        return Call
                .builder()
                .rider(rider)
                .delivery_Address(dto.getDriver_deliveryaddress())
                .pickUp_Address(dto.getDriver_pickupaddress())
                .callId(dto.getCall_id())
                .companyName(dto.getDriver_client())
                .callRequestTime(callRequestTime)
                .callAppointTime(callAppointTime)
                .delivery_status("요청")
                .build();
    }

    public static Call create(Rider rider, Kb10thRequest dto){

        dto.setDriver_deliveryaddress(CharMatcher.anyOf("|¶").removeFrom(dto.getDriver_deliveryaddress()));
        dto.setDriver_pickupaddress(CharMatcher.anyOf("|¶").removeFrom(dto.getDriver_pickupaddress()));

        return Call
                .builder()
                .rider(rider)
                .delivery_Address(dto.getDriver_deliveryaddress())
                .pickUp_Address(dto.getDriver_pickupaddress())
                .callId(dto.getCall_id())
                .companyName(dto.getDriver_client())
                .callRequestTime(now())
                .callAppointTime(now())
                .delivery_status("요청")
                .build();
    }

    public void updateCallPickUpTime(String pickUpTime) {
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
       this.callAppointTime = LocalDateTime.parse(pickUpTime, formatter);
   }

    public void complete(LocalDateTime callCompleteTime) {
        this.callCompleteTime = callCompleteTime;
    }

    public void updateDelivery_status(String delivery_status) {
        this.delivery_status = delivery_status;
    }

    public void updateGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void createKbCallId(String leftPadCallId) {
        this.kb_call_id = leftPadCallId;
    }

}
