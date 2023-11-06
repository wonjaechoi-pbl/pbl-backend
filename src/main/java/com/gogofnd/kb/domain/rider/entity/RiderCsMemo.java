package com.gogofnd.kb.domain.rider.entity;

import com.gogofnd.kb.business.dto.req.RiderCsMemoReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "rider_cs_memo")
@Entity
public class RiderCsMemo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_num")
    private Long id;
    @JoinColumn(name = "rider_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rider rider;
    @Column(name = "content")
    private String content;
    @Column(name = "writer")
    private String writer;
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    public static RiderCsMemo create(RiderCsMemoReq req, Rider rider) {

        return RiderCsMemo.builder()
                .rider(rider)
                .content(req.getContent())
                .writer(req.getWriter())
                .createdDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .build();
    }
}
