package com.gogofnd.kb.domain.insurance.entity;


import com.gogofnd.kb.domain.rider.entity.RiderWeb;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reject_reason")
@Entity
public class WebRejectReason {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "joinWeb_Id")
    @ManyToOne(fetch = FetchType.LAZY)
    private RiderWeb riderWeb;

    @Column(name = "reject_reason")
    private String rejectReason;
    private LocalDateTime reject_date;

    @Column(length = 20)
    private String status;

    public static WebRejectReason create(RiderWeb riderWeb, String rejectReason, String status){
        return builder()
                .rejectReason(rejectReason)
                .status(status)
                .reject_date(LocalDateTime.now())
                .riderWeb(riderWeb)
                .build();
    }
}
