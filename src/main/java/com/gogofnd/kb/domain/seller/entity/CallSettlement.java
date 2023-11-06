package com.gogofnd.kb.domain.seller.entity;

import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.seller.dto.Kb10thRequest;
import com.google.common.base.CharMatcher;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EntityListeners(AuditingEntityListener.class)
@Table(name = "calls_settlement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
@Entity
public class CallSettlement{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="group_id", length = 30)
    private String groupId;

    private long balance;

    @Column(name = "pickup_time")
    private LocalDateTime callPickUpTime;

    @Column(name = "complete_time")
    private LocalDateTime callCompleteTime;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @JoinColumn(name = "rider_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rider rider;

    private int totalTime;

    private long dailyTotalRiding;

    @Column(name = "settlement_status", length = 1)
    private String settlementStatus;

    public static CallSettlement create(Rider rider, String groupId){
        return CallSettlement
                .builder()
                .rider(rider)
                .groupId(groupId)
                .settlementStatus("N")
                .build();
    }

    public void updateCallPickUpTime(String pickUpTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.callPickUpTime = LocalDateTime.parse(pickUpTime, formatter);
    }

    public void updateDailyTotalRiding(long totalTime){
        this.dailyTotalRiding = totalTime;
    }

    public void updateCompleteTime(LocalDateTime callCompleteTime){
        this.callCompleteTime = callCompleteTime;
    }

    public void updateBalance(long balance) {
        this.balance = balance;
    }

    public void updateTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
}
