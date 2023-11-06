package com.gogofnd.kb.domain.delivery.service;


import com.gogofnd.kb.domain.delivery.dto.app.res.BalanceStatusRes;
import com.gogofnd.kb.domain.delivery.dto.insure.req.BalanceInsureReq;
import com.gogofnd.kb.domain.delivery.dto.insure.res.ResultDto;
import com.gogofnd.kb.domain.delivery.entity.KbBalancesHistory;
import com.gogofnd.kb.domain.delivery.repository.BalanceHistoriesRepository;
import com.gogofnd.kb.domain.delivery.repository.KbBalanceHistoriesRepository;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.domain.seller.repository.SellerRepository;
import com.gogofnd.kb.global.dto.response.PagingResponse;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.SellerAES_Encryption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BalanceService {
    private final RiderRepositorySupport riderRepositorySupport;
    private final BalanceHistoriesRepository balanceHistoriesRepository;
    private final KbBalanceHistoriesRepository kbBalanceHistoriesRepository;
    private final SellerRepository sellerRepository;


    //13. 예납금 잔액
//    public ResultDto balanceTotal(BalanceInsureReq balanceInsureReq){
//
//        //리스폰스는 인터셉터에서 바디가 안찍혀서 로그 이렇게 찍어놓음;;
//        log.info("======================================= 13번 api 호출 ================================");
//        log.info("date : {}", balanceInsureReq.getDate());
//        log.info("balance : {}", balanceInsureReq.getBalance());
//        log.info("cmpcd : {}", balanceInsureReq.getProxy_driv_coorp_cmpcd());
//
//        //로컬데이트타임 형태로 변환해서 저장
//        String convertDate = convertDate(balanceInsureReq.getDate());
//
//        KbBalancesHistory kbBalancesHistory = KbBalancesHistory.saveKbBalancesHistory(balanceInsureReq,convertDate);
//        kbBalanceHistoriesRepository.save(kbBalancesHistory);
//
//        //정상응답 리턴
//        ResultDto resultDto = new ResultDto();
//        resultDto.setResult("ok");
//        return resultDto;
//    }

    public ResultDto balanceListTotal(List<BalanceInsureReq> balanceInsureReqList){
        List<KbBalancesHistory> kbBalancesHistories = balanceInsureReqList.stream().map((balanceInsureReq) -> {
            log.info("======================================= 13번 api 호출 ================================");
            log.info("date : {}", balanceInsureReq.getDate());
            log.info("balance : {}", balanceInsureReq.getBalance());
            log.info("cmpcd : {}", balanceInsureReq.getProxy_driv_coorp_cmpcd());
            log.info("useAmt : {}", balanceInsureReq.getUse_amt());

            if(balanceInsureReq.getBalance().isBlank()){
                balanceInsureReq.setBalance("0");
            }

            if(balanceInsureReq.getUse_amt().isBlank()){
                balanceInsureReq.setUse_amt("0");
            }

            String convertDate = convertDate(balanceInsureReq.getDate());
            return KbBalancesHistory.saveKbBalancesHistory(balanceInsureReq,convertDate);
        })
        .collect(Collectors.toList());

        kbBalanceHistoriesRepository.saveAll(kbBalancesHistories);

        ResultDto resultDto = new ResultDto();
        resultDto.setResult("ok");
        return resultDto;
    }


    //운영사 잔액 조회
    @Transactional(readOnly = true)
    public Integer getBalanceOfSeller(String sellerCode,String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!sellerCode.equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        Seller seller = sellerRepository.findSellerBySellerCode(sellerCode);
        return Long.valueOf(seller.getBalance()).intValue();

    }

    public String convertDate(String date){
        String substring1 = date.substring(0, 4); // 2023-03-27 :: 2023-
        String substring2 = date.substring(4,6); // 2023-03-27 :: -0
        String substring3 = date.substring(6); // 2023-03-27 :: -0

        return substring1+"-"+substring2+"-"+substring3;
    }
}
