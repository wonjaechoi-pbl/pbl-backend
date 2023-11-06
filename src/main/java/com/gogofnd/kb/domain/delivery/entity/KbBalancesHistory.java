package com.gogofnd.kb.domain.delivery.entity;


import com.gogofnd.kb.domain.delivery.dto.insure.req.BalanceInsureReq;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name="kb_balance_history")
@Builder
@Entity
public class KbBalancesHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // kb에서 배치로 보내준 금액
    private int balance;

    // 배치로 받은 날짜
    private LocalDate date;

    // 각 회사별 예치금 차감금액
    private String cmpcd;

    private int useAmt;

    public static KbBalancesHistory saveKbBalancesHistory(BalanceInsureReq balanceInsureReq, String convertDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(convertDate, formatter);

        return KbBalancesHistory.builder()
                .balance(Integer.parseInt(balanceInsureReq.getBalance()))
                .date(date)
                .cmpcd(balanceInsureReq.getProxy_driv_coorp_cmpcd())
                .useAmt(Integer.parseInt(balanceInsureReq.getUse_amt()))
                .build();
    }

}

