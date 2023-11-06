package com.gogofnd.kb.domain.insurance.service;


import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.insurance.dto.*;
import com.gogofnd.kb.domain.insurance.dto.req.KBSignUpVrReq;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi8thReq;
import com.gogofnd.kb.domain.insurance.dto.req.KbSignUpReq;
import com.gogofnd.kb.domain.insurance.entity.History;

import com.gogofnd.kb.domain.insurance.entity.RejectReason;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepository;

import com.gogofnd.kb.domain.insurance.repository.HistoryRepositorySupport;
import com.gogofnd.kb.domain.insurance.repository.RejectReasonRepository;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import com.gogofnd.kb.domain.rider.repository.RiderInsuranceHistoryRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.AES256;
import com.gogofnd.kb.global.provider.AES_Encryption;
import com.gogofnd.kb.global.provider.KB_AES_Encryption2;
import com.gogofnd.kb.global.utils.FindAge;
import com.google.firebase.messaging.Message;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class SignUpService {
    private final RiderRepositorySupport riderRepositorySupport;
    private final RejectReasonRepository rejectReasonRepository;
    private final RiderInsuranceHistoryRepository riderInsuranceHistoryRepository;

    private final HistoryRepository historyRepository;
    private final HistoryRepositorySupport historyRepositorySupport;

    private final FindAge findAge;

    //5번째
    @Async // api 5번 7번 같은시각에 배치되게 비동기. 스레드풀 설정은 아직 따로 안했는데 해야하는지 안해도되는지 모르겠음
    @Scheduled(cron = "00 50 19 * * *") //초 분 시 일 월 요일
    public void signUpRequest() throws Exception {

        List<History> underWritingRequests = historyRepositorySupport.findRequestsByInsuranceStatusYesterday("051");
        List<Rider> riders = underWritingRequests.stream().map(History::getRider).collect(Collectors.toList());

        List<KbSignUpReq> signUpRequests = new ArrayList<>();
        List<RiderInsuranceHistory> insuranceHistories = new ArrayList<>();

        riders.forEach(r -> {
            //보험 상태 업데이트
            RiderInsuranceHistory riderInsuranceHistory = riderInsuranceHistoryRepository.findById(r.getId()).orElse(null);
            if (riderInsuranceHistory != null){
                riderInsuranceHistory.updateEndorsementRequestTime();
                insuranceHistories.add(riderInsuranceHistory);
            }

            //암호화 시작
            KbSignUpReq kbSignUpReq = new KbSignUpReq(r);

            String ssn = kbSignUpReq.getSsn();
            String rawSsn = "";
            try {
                rawSsn = ssnDecode(ssn);
                String encodeSsn = kbSsnEncode2(rawSsn);
                kbSignUpReq.updateSsn(encodeSsn);
                signUpRequests.add(kbSignUpReq);
                System.out.println("kbSignUpRequest = " + kbSignUpReq);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }});

        log.info("우리쪽 카운트 : {}", signUpRequests.size());
        KBRetrofitConfig<KbSignUpReq> KBRetrofitConfig = new KBRetrofitConfig<>();
        KBRetrofitConfig.create(KbApi.class).kbApi5Retrofit(signUpRequests).execute().body();

        riderInsuranceHistoryRepository.saveAll(insuranceHistories);
    }


    private void insuranceValidationCheck(Rider rider) {
        String insuranceStatus = rider.getInsuranceStatus();
        if(!(insuranceStatus.contains("042") || insuranceStatus.contains("063")|| insuranceStatus.contains("082"))){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    //기명 요청 결과 api (kb -> 고고)
    public CountDto signUpResult(List<KbApiSignResultDto> dto){
        //원재 - 기명등재 수신 이슈때문에 추가한 log
        log.info("dto --> " + dto);

        // dto 값에서 드라이버 아이디 추출해서 리스트로 만듬
        List<String> driverIds = dto.stream().map(KbApiSignResultDto::getDriver_id).collect(Collectors.toList());

        //원재 - 기명등재 수신 이슈때문에 추가한 log
        log.info("driverIds --> " + driverIds);

        //라이더들 다 찾아옴
        List<Rider> riders = riderRepositorySupport.findAllByDriverId(driverIds);

        List<History> histories = new ArrayList<>();
        List<RejectReason> rejectReasons = new ArrayList<>();
        AtomicReference<String> resultTmp = new AtomicReference<>("");

        //라이더 별로
        riders.forEach(e->{
            dto.forEach(d->{
                if(d.getDriver_id().equals(e.getDriver_id())) {
                    log.info("proxy_driv_coorp_cmpcd : {}",d.getProxy_driv_coorp_cmpcd());
                    log.info("driver_id : {}",d.getDriver_id());
                    log.info("Vcno_hngl_nm : {}",d.getVcno_hngl_nm());
                    log.info("result : {}",d.getResult());
                    log.info("effective_time : {}",d.getEffective_time());
                    log.info("underwriting_after : {}",d.getUnderwriting_after());
                    System.out.println("d = " + d);

                    // 기명 요청이 승인되지 않았을경우
                    if (!d.getResult().equals("endorsed")) {
                        if(d.getResult().equals("already_endorsed_driver_id")) {
                            History history = timeChange6(History.create(e, "062",e.getSeller()),d);
                            RejectReason rejectReason = RejectReason.create(e, d.getResult(),"062");
                            histories.add(history);
                            rejectReasons.add(rejectReason);
                            log.info(d.getResult());

                            //라이더 테이블에도 상태값 변경해줌
                            e.updateInsuranceStatus("062");
                        }
                        else {
                            History history = timeChange6(History.create(e, "063",e.getSeller()), d);
                            RejectReason rejectReason = RejectReason.create(e, d.getResult(),"063");
                            histories.add(history);
                            rejectReasons.add(rejectReason);
                            log.info(d.getResult());

                            //라이더 보험 상태 컬럼 업데이트
                            e.updateInsuranceStatus("063");
                        }
                    }
                    // 승인된 경우
                    else {
                        History history = timeChange6(History.create(e, "062",e.getSeller()),d);

                        //승인 내역 저장후 보험 적용 기간을 업데이트해줌
                        history.updateEffectiveDate(convertDate(d.getEffective_time().get(0)), convertDate(d.getEffective_time().get(1)));
                        history.updatePolicy_number(d.getPolicy_number());
                        histories.add(history);

                        //라이더 테이블에도 상태값 변경해줌
                        e.updateInsuranceStatus("062");
                        e.updateEffectiveDate(convertDate(d.getEffective_time().get(0)), convertDate(d.getEffective_time().get(1)));

                        // 라이더 인셔런스 히스토리(cms 조회용) 기명 승인 시간도 업데이트 쳐줌
                        RiderInsuranceHistory riderInsuranceHistory
                                = riderInsuranceHistoryRepository.findByRiderId(e.getId());

                        if(riderInsuranceHistory != null){
                            riderInsuranceHistory.updateEndorsementCompleteTime();
                        }
                    }
                }
                resultTmp.set(d.getResult());
            });

        });

        historyRepository.saveAll(histories);
        rejectReasonRepository.saveAll(rejectReasons);
        return new CountDto(riders.size());
    }

    private String aes256Decode(String encText) throws InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        AES256 aes256 = new AES256();
        return aes256.AES256_DECRYPT(encText);
    }

    // 기명 취소 결과
    public CountDto kbApi8th(List<KbApi8thReq> dto){
        List<String> driverIds = dto.stream().map(KbApi8thReq::getDriver_id).collect(Collectors.toList());
        List<Rider> riders = riderRepositorySupport.findAllByDriverId(driverIds);
        List<RejectReason> rejectReasons = new ArrayList<>();
        List<History> histories = new ArrayList<>();

        //람다안에서 동시성 이슈땨ㅐ문에 아토믹 . 스트링이랑 똑같음
        AtomicReference<String> resultTmp = new AtomicReference<>("");

        riders.forEach(e->{
            dto.forEach(d->{
                if(d.getDriver_id().equals(e.getDriver_id())) {
                    System.out.println("d = " + d);
                    log.info("Proxy_driv_coorp_cmpcd : {}",d.getProxy_driv_coorp_cmpcd());
                    log.info("Driver_id : {}",d.getDriver_id());
                    log.info("result : {}",d.getResult());
                    log.info("Policy_number : {}",d.getPolicy_number());
                    log.info("Effective_time : {}",d.getEffective_time());
                    log.info("Auto_cancel : {}",d.getAuto_cancel());

                    // 기명취소가 승인된게 아니면
                    if (!d.getResult().equals("canceled")) {
                        //083으로 내역과 거절사유를 생성해서 리스트에 담는다.
                        History history = History.create(e, "083", e.getSeller(), "-");
                        RejectReason rejectReason = RejectReason.create(e, d.getResult(),"083");
                        histories.add(history);
                        rejectReasons.add(rejectReason);

                        //라이더 테이블에 보험상태 컬럼을 기명취소 거절로 만든다.
                        e.updateInsuranceStatus("083");

                        //기명 취소가 승인되었으면
                    } else {
                        //승인 내역 만듬.
                        History history = History.create(e, "082",e.getSeller(), "-");
                        //기명 취소 전까지 실제 보험적용기간을 kb로부터 받아서 내역에 update쳐줌
                        history.updateEffectiveDate(convertDate(d.getEffective_time().get(0)), convertDate(d.getEffective_time().get(1)));
                        histories.add(history);

                        //라이더 테이블에도 똑같이 상태값 바꿔줌
                        e.updateInsuranceStatus("082");
                        e.updateUseYnStatus("N");
                        e.updateEffectiveDate(convertDate(d.getEffective_time().get(0)), convertDate(d.getEffective_time().get(1)));

                        RiderInsuranceHistory riderInsuranceHistory = riderInsuranceHistoryRepository.findByRiderId(e.getId());

                        if(riderInsuranceHistory != null){
                            riderInsuranceHistory.updateWithdrawCompleteTime();
                        }
                    }
                }
                resultTmp.set(d.getResult());
            });
        });

        //리스트들 db에 저장
        historyRepository.saveAll(histories);
        rejectReasonRepository.saveAll(rejectReasons);
        return new CountDto(riders.size());
    }

    private void insuranceValidationCheckApi8(Rider rider) {
        String insuranceStatus = rider.getInsuranceStatus();
        if(!insuranceStatus.contains("071")){
            throw new BusinessException(ErrorCode.INVALID_CANCEL_REQUEST);
        }
    }


    private LocalDateTime convertDate(Integer date){

        return LocalDateTime.ofEpochSecond(date, 0, ZoneOffset.ofTotalSeconds(60*60*9));
    }

    //kb는 유닉스 타임형태로 시간을 주기 때문에 우리가 파싱해서 로컬데이트타임 형태로 디비에 저장해야함
    public History timeChange6(History history, KbApiSignResultDto dto){
        LocalDateTime changedTime = LocalDateTime.ofEpochSecond(dto.getUnderwriting_after(), 0, ZoneOffset.ofTotalSeconds(60 * 60 * 9));
        history.changeUntil(changedTime);
        return history;
    }

    private String kbSsnEncode2(String ssn) throws Exception {
        KB_AES_Encryption2 aes = new KB_AES_Encryption2();
        String encrypt = aes.encrypt(ssn);
        return encrypt;
    }

    //주민등록번호 복호화
    private String ssnDecode(String ssn) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(ssn);
        return decrypt;
    }

}


