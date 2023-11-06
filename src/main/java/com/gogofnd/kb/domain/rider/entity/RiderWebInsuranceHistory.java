package com.gogofnd.kb.domain.rider.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "rider_insurance_history")
public class RiderWebInsuranceHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "riderWeb_id",unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private RiderWeb riderWeb;

    // 언더라이팅 요청시간
    @Column(name = "underwriting_request_time")
    private LocalDateTime underWritingRequestTime;

    // 언더라이팅 완료시간
    @Column(name = "underwriting_complete_time")
    private LocalDateTime underWritingCompleteTime;

    // 기명 요청시간
    @Column(name = "endorsement_request_time")
    private LocalDateTime endorsementRequestTime;

    //기명 완료시간
    @Column(name = "endorsement_complete_time")
    private LocalDateTime endorsementCompleteTime;

    //기명 취소요청 시간
    @Column(name = "withdraw_request_time")
    private LocalDateTime withdrawRequestTime;

    //기명취소 완료 시간
    @Column(name = "withdraw_complete_time")
    private LocalDateTime withdrawCompleteTime;

    @Builder
    public RiderWebInsuranceHistory(RiderWeb riderWeb, LocalDateTime underWritingRequestTime, LocalDateTime underWritingCompleteTime, LocalDateTime endorsementRequestTime, LocalDateTime endorsementCompleteTime, LocalDateTime withdrawRequestTime, LocalDateTime withdrawCompleteTime) {
        this.riderWeb = riderWeb;
        this.underWritingRequestTime = underWritingRequestTime;
        this.underWritingCompleteTime = underWritingCompleteTime;
        this.endorsementRequestTime = endorsementRequestTime;
        this.endorsementCompleteTime = endorsementCompleteTime;
        this.withdrawRequestTime = withdrawRequestTime;
        this.withdrawCompleteTime = withdrawCompleteTime;
    }

    public static RiderWebInsuranceHistory create(RiderWeb riderWeb){
        return builder()
                .riderWeb(riderWeb)
                .underWritingRequestTime(LocalDateTime.now())
                .build();
    }

    public void updateUnderWritingRequestTime() {
        this.underWritingRequestTime = LocalDateTime.now();
    }

    public void updateUnderWritingCompleteTime() {
        this.underWritingCompleteTime = LocalDateTime.now();
    }

    public void updateEndorsementRequestTime() {
        this.endorsementRequestTime = LocalDateTime.now();
    }

    public void updateEndorsementCompleteTime() {
        this.endorsementCompleteTime = LocalDateTime.now();
    }

    public void updateWithdrawRequestTime() {
        this.withdrawRequestTime = LocalDateTime.now();
    }

    public void updateWithdrawCompleteTime() {
        this.withdrawCompleteTime = LocalDateTime.now();
    }
}
