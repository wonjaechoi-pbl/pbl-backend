package com.gogofnd.kb.domain.insurance.entity;


import com.gogofnd.kb.domain.rider.entity.Rider;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reject_reason")
@Entity
public class RejectReason {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "rider_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rider rider;

    @Column(name = "reject_reason")
    private String rejectReason;
    private LocalDateTime reject_date;

    @Column(length = 20)
    private String status;

    public static RejectReason create(Rider rider,String rejectReason,String status){
        return builder()
                .rejectReason(rejectReason)
                .status(status)
                .reject_date(LocalDateTime.now())
                .rider(rider)
                .build();
    }
}
