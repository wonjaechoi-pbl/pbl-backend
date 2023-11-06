package com.gogofnd.kb.domain.delivery.entity;

import com.gogofnd.kb.domain.delivery.dto.accident.AccidentCreate;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.global.domain.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Table(name = "accident")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Accident extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number",length = 64)
    private String claimNumber; // 최대 64글자
    private LocalDateTime claim_time; // 사고 접수시간
    private LocalDateTime accident_time;  //사고 발생시간

    @JoinColumn(name = "call_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Call call;

    // 보상금. 컬럼 만들어놓으라고 팀장님이 시키셔서 만들어 놨는데 아직 보상금 로직을 모름
    private Integer compensation;


    @Builder
    public Accident(String claimNumber, LocalDateTime claim_time, LocalDateTime accident_time, Call call,Integer compensation) {
        this.claimNumber = claimNumber;
        this.claim_time = claim_time;
        this.accident_time = accident_time;
        this.call = call;
        this.compensation = compensation;
    }

    public static Accident create(AccidentCreate dto, Call call){
        return Accident.builder()
                .claim_time(LocalDateTime.ofInstant(Instant.ofEpochSecond(dto.getClaim_time()), TimeZone.getDefault().toZoneId()))
                .claimNumber(dto.getClaim_number())
                .accident_time(LocalDateTime.ofInstant(Instant.ofEpochSecond(dto.getAccident_time()), TimeZone.getDefault().toZoneId()))
                .call(call)
                .build();
    }

}
