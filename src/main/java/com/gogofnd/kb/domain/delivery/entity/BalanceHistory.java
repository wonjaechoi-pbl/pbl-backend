package com.gogofnd.kb.domain.delivery.entity;

import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rider_balance_history")
@Entity
@Builder
public class BalanceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int balance;

    @JoinColumn(name = "call_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Call call;

    @JoinColumn(name = "rider_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Rider rider;

    @CreatedDate
    private LocalDateTime createdDate;
    private String balanceStatus;

    public static BalanceHistory create(Rider rider,int balance, Call call, String balanceStatus){
        return BalanceHistory.builder()
                .rider(rider)
                .balance(balance)
                .call(call)
                .balanceStatus(balanceStatus)
                .createdDate(LocalDateTime.now())
                .build();
    }
}
