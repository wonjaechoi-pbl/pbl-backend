package com.gogofnd.kb.domain.insurance.service;


import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.insurance.dto.CountDto;
import com.gogofnd.kb.domain.insurance.dto.KbApiRiderDto;
import com.gogofnd.kb.domain.insurance.dto.KbApiRiderVrDto;
import com.gogofnd.kb.domain.insurance.dto.res.KbApiUnderWritingResult;
import com.gogofnd.kb.domain.insurance.entity.History;
import com.gogofnd.kb.domain.insurance.entity.RejectReason;
import com.gogofnd.kb.domain.insurance.repository.*;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import com.gogofnd.kb.domain.rider.repository.RiderInsuranceHistoryRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.AES256;
import com.gogofnd.kb.global.provider.AES_Encryption;
import com.gogofnd.kb.global.provider.KB_AES_Encryption2;
import com.gogofnd.kb.global.utils.FindAge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class UnderWritingService {
    private final HistoryRepository historyRepository;
    private final HistoryRepositorySupport historyRepositorySupport;
    private final RejectReasonRepository rejectReasonRepository;
    private final RiderInsuranceHistoryRepository riderInsuranceHistoryRepository;
    private final RiderRepositorySupport riderRepositorySupport;
    private final FindAge findAge;
    private final WebHistoryRepository webHistoryRepository;
    private final WebHistoryRepositorySupport webHistoryRepositorySupport;



    //2번
    @Async// 2번 12번 api 비동기로 동시에 배치되게 설정
    @Scheduled(cron = "0 20 04 * * *") //초 분 시 일 월 요일
    public void underWritingRequest() throws Exception {

        // 새로 가입신청 한사람들
        List<History> newRequests = historyRepositorySupport.findRequestsByInsuranceStatusYesterday("021");

        //이력 기반으로 라이더들 리스트 만듬
        List<Rider> riders = newRequests.stream().map(History::getRider).collect(Collectors.toList());

        List<KbApiRiderDto> riderDtoList = new ArrayList<>();
        List<RiderInsuranceHistory> insuranceHistories = new ArrayList<>();
        // 신규 가입 신청
        riders.forEach(r -> {
            // 보험 상태 컬럼 생성
            RiderInsuranceHistory riderInsuranceHistory = RiderInsuranceHistory.create(r);
            insuranceHistories.add(riderInsuranceHistory);

            KbApiRiderDto dto;
            //암호화 시작
            if(r.getSeller().getCmpcd().equals("G02") || r.getSeller().getCmpcd().equals("G05")){
                // 부릉, 일보험 - 자동기명등재요청대상
                dto = new KbApiRiderVrDto(r);
            } else {
                // 온나, 딜버 - 기존언더라이팅요청
                dto = new KbApiRiderDto(r);
            }

            String ssn = dto.getSsn();

            //암호화되서 저장된 주민등록번호를 복호화 한 뒤, kb에서 정한 방식으로 암호화한다.
            try {
                updateSsn(dto, ssn);
                riderDtoList.add(dto);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        });

        log.info("우리쪽 카운트 : {}", riderDtoList.size());
        KBRetrofitConfig<KbApiRiderDto> KBRetrofitConfig = new KBRetrofitConfig<>();

        //레트로핏에서 execute까지 있으면 함수실행, 바디 까지 찍으면 값 응답 값 추출가능
        KBRetrofitConfig.create(KbApi.class).kbApi2Retrofit(riderDtoList).execute().body();
        riderInsuranceHistoryRepository.saveAll(insuranceHistories);
    }

    //테스트용 당일 값 전송.
    public void underWritingRequestTest() throws Exception {
        List<History> underWritingRequests = historyRepositorySupport.findRequestsByInsuranceStatusToday("021");
        List<Rider> riders = underWritingRequests.stream().map(History::getRider).collect(Collectors.toList());

        System.out.println("riders Count : "+riders.size());
        System.out.println("underWritingRequests : "+underWritingRequests.size());

        List<KbApiRiderDto> riderDtoList = new ArrayList<>();

        riders.forEach(r -> {

            KbApiRiderDto dto;
            //암호화 시작
            if(r.getSeller().getCmpcd().equals("G02")){
                dto = new KbApiRiderVrDto(r);
            } else {
                dto = new KbApiRiderDto(r);
            }

            String ssn = dto.getSsn();

            //암호화되서 저장된 주민등록번호를 복호화 한 뒤, kb에서 정한 방식으로 암호화한다.
            try {
                updateSsn(dto, ssn);
                riderDtoList.add(dto);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        });

        log.info("=================================================== 우리쪽 카운트 : {}", riderDtoList.size());
        KBRetrofitConfig<KbApiRiderDto> KBRetrofitConfig = new KBRetrofitConfig<>();
        KBRetrofitConfig.create(KbApi.class).kbApi2Retrofit(riderDtoList).execute().body();
    }

    private void insuranceValidationCheck(Rider rider) {
        String insuranceStatus = rider.getInsuranceStatus();
        if(!(insuranceStatus.contains("012")||insuranceStatus.contains("033"))){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    // 운영사에서 보내주는 암호화 된 주민등록번호 값받아서 복호화 한후 kb에 보내주기로한 방식으로 암호화
    private void updateSsn(KbApiRiderDto dto, String ssn) throws Exception {
        String rawSsn = ssnDecode(ssn);
        System.out.println("rawSsn = " + rawSsn);
        String kbEncodeSsn = kbSsnEncode2(rawSsn);
        System.out.println("kbencodeSsn = " + kbEncodeSsn);
        dto.updateSsn(kbEncodeSsn);
    }

    //3번
    public CountDto underWritingResult(List<KbApiUnderWritingResult> dto){
        List<String> riderIds = dto.stream().map(KbApiUnderWritingResult::getDriver_id).collect(Collectors.toList());
        List<Rider> riderList = riderRepositorySupport.findAllByDriverId(riderIds);
        List<History> histories = new ArrayList<>();
        List<RejectReason> rejectReasons = new ArrayList<>();
        AtomicReference<String> resultTmp = new AtomicReference<>("");

        riderList.forEach(e->{
            dto.forEach(d->{
                if(d.getDriver_id().equals(e.getDriver_id())) {
                    log.info("언더라이팅 결과 Result --> " + d.getResult());
                    // 승인이 아닌경우
                    if (!d.getResult().equals("accepted")) {
                        // 리뷰중이거나, 조건부 승인인경우
                        if(d.getResult().equals("in_review") || d.getResult().equals("accepted_noinsure")){
                            History history = untilChange(History.create(e, "034", e.getSeller()), d);
                            histories.add(history);
                            RejectReason rejectReason = RejectReason.create(e, d.getResult(),"034");
                            rejectReasons.add(rejectReason);
                            e.updateInsuranceStatus("034");
                        }
                        // 승인 - 자동기명등재요청 대상 일 경우
                        else if (d.getResult().equals("accepted_endorsed")) {
                            History history = untilChange(History.create(e, "051", e.getSeller()), d);
                            histories.add(history);
                            RejectReason rejectReason = RejectReason.create(e, d.getResult(),"051");
                            rejectReasons.add(rejectReason);
                            e.updateInsuranceStatus("051");

                            RiderInsuranceHistory riderInsuranceHistory =
                                    riderInsuranceHistoryRepository.findByRiderId(e.getId());

                            //널이아니면 라이더 인셔런스 히스토리 승인 시간 업데이트
                            if(riderInsuranceHistory != null){
                                riderInsuranceHistory.updateUnderWritingCompleteTime();
                            }
                        }else{
                            // 승인이 거절당한경우
                            History history = untilChange(History.create(e, "033",e.getSeller()), d);
                            histories.add(history);
                            RejectReason rejectReason = RejectReason.create(e, d.getResult(),"033");
                            rejectReasons.add(rejectReason);
                            e.updateInsuranceStatus("033");
                        }
                    }
                    //승인된 경우
                    else {
                        History history = untilChange(History.create(e, "032", e.getSeller()), d);
                        histories.add(history);
                        RejectReason rejectReason = RejectReason.create(e, d.getResult(),"032");
                        rejectReasons.add(rejectReason);
                        e.updateInsuranceStatus("032");

                        RiderInsuranceHistory riderInsuranceHistory =
                                riderInsuranceHistoryRepository.findByRiderId(e.getId());

                        //널이아니면 라이더 인셔런스 히스토리 승인 시간 업데이트
                        if(riderInsuranceHistory != null){
                            riderInsuranceHistory.updateUnderWritingCompleteTime();
                        }
                    }
                }
                resultTmp.set(d.getResult());
            });
            log.info("언더라이팅 결과 Result --> " + e.getDriver_id());
            log.info("================================push 알림 나간 리스트 ===========================================");
        });
        historyRepository.saveAll(histories);
        rejectReasonRepository.saveAll(rejectReasons);

        return new CountDto(riderList.size());
    }

    public void ssnCheck(String number){
        hyphenCheck(number);
        ssnLengthCheck(number);
    }
    private void hyphenCheck(String number) {
        if (number.contains("-") || (!number.matches("[+-]?\\d*(\\.\\d+)?"))){
            throw new BusinessException(ErrorCode.INVALID_TYPE_VALUE2);
        }
    }

    private void ssnLengthCheck(String number) {
        if (number.length() != 13) {
            throw new BusinessException(ErrorCode.INVALID_LENGTH_VALUE);
        }
    }


    private String aes256Decode(String encText) throws InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        AES256 aes256 = new AES256();
        return aes256.AES256_DECRYPT(encText);
    }


    // kb에서 수신한 유효기간값 시간변환하여 보험 가입진행 내역에 업데이트
    public History untilChange(History history, KbApiUnderWritingResult dto){
        LocalDateTime changedTime = LocalDateTime.ofEpochSecond(dto.getUntil(), 0, ZoneOffset.ofTotalSeconds(60 * 60 * 9));
        history.changeUntil(changedTime);
        return history;
    }

    //kb와 정한값으로 주민등록번호 암호화
    private String kbSsnEncode2(String ssn) throws Exception {
        KB_AES_Encryption2 aes = new KB_AES_Encryption2();
        String encrypt = aes.encrypt(ssn);
        return encrypt;
    }

    //운엳사와 정한 값으로 주민등록번호 복호화
    private String ssnDecode(String ssn) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(ssn);
        return decrypt;
    }

}
