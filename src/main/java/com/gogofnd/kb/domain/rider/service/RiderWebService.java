package com.gogofnd.kb.domain.rider.service;


import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.insurance.dto.req.KbSignUpReq;
import com.gogofnd.kb.domain.insurance.entity.History;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepository;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepositorySupport;
import com.gogofnd.kb.domain.insurance.repository.RejectMessageRepositorySupport;
import com.gogofnd.kb.domain.rider.dto.req.LoginReq;
import com.gogofnd.kb.domain.rider.dto.res.InsuranceStatusRes;
import com.gogofnd.kb.domain.rider.dto.res.LoginRes;
import com.gogofnd.kb.domain.rider.dto.res.RiderStatusRes;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import com.gogofnd.kb.domain.rider.repository.RiderInsuranceHistoryRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.*;
import com.gogofnd.kb.global.utils.FindAge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RiderWebService {

    private final RiderRepository riderRepository;
    private final RiderRepositorySupport riderRepositorySupport;
    private final RiderInsuranceHistoryRepository riderInsuranceHistoryRepository;
    private final HistoryRepository historyRepository;
    private final HistoryRepositorySupport historyRepositorySupport;

    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final FindAge findAge;
    private final RejectMessageRepositorySupport rejectMessageRepositorySupport;


    // 라이더 보험상태 체크하는 함수(032 같은거) , 보험적용상태도 같이
    public RiderStatusRes getRiderInsuranceStatus(String phone){
        Rider rider = riderRepositorySupport.findByPhone(phone);
        return new RiderStatusRes(rider);
    }

    // 라이더 보험 상태 파싱해서 리턴해줌
    public InsuranceStatusRes getRiderInsuranceStatus(String loginId, String sellerCode, String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!sellerCode.equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }


        Rider rider = riderRepositorySupport.findByLoginId(loginId);


        String status =  rider.getInsuranceStatus();

        String st =  status.substring(1,2);
        log.info("st",st);
        String resultProgress = null;

        String resultCode = status.substring(status.length() - 1);

        String resultMessage = "요청 진행중";

        if(st.equals("1")){
            resultProgress = "가입동의";
        }
        if(st.equals("2") || st.equals("3")){
            resultProgress = "언더라이팅";
        }
        if(st.equals("4")){
            resultProgress = "계약체결이해동의";
        }
        if(st.equals("5") || st.equals("6")){
            resultProgress = "기명요청";
        }
        if(st.equals("7") || st.equals("8")){
            resultProgress ="기명 취소";
        }
        if(resultCode.equals("3") || resultCode.equals("4")){

            String rejectReason = historyRepositorySupport.findByRiderAndStatusOrderCrd(rider.getId(), status).getRejectReason();
            resultMessage =  rejectMessageRepositorySupport.findRejectMessage(rejectReason).getRejectMessage();;

        }
        if(resultCode.equals("2")){
            resultMessage ="성공";
        }
        return new InsuranceStatusRes(resultProgress,resultCode,resultMessage);
    }

    //기명취소 요청하는 함수.
    public String sellerWithDrawRider(String loginId, String sellerCode, String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!sellerCode.equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        Rider rider = riderRepositorySupport.findByLoginIdAndSellerCode(loginId,sellerCode);

        //라이더 찾아와서 기명취소 할 수 있는 상태인지(기명 완료가 된 라이더인지) 체크
        insuranceValidationCheck(rider);
        String rawSsn = aes256Decode(rider.getSsn());
        ssnCheck(rawSsn);

        int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
        String ageYn = findAge.CheckOverAge(AmericanAge);


        //TODO 애매함. 기명 취소 결과 승인된 사람들한테 넣는게 나을듯
        //기명 취소 요청하면 어플리케이션 넘버 공백으로 바꿈. 그런데 기명 취소 요청 승인 안나면 공백으로 바꾸면 안되지않나?????
        rider.updateApplicationNumber("");

        // 기명 취소 요청했다는 내역 만들어서 저장하고, 라이더 테이블 보험 상태 컬럼 업데이트
        History history = History.create(rider,"071", rider.getSeller(),ageYn);
        rider.updateInsuranceStatus("071");
        rider.withdraw();
        historyRepository.save(history);

        return "Y";
    }

    private String aes256Decode(String encText) throws InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        AES256 aes256 = new AES256();
        return aes256.AES256_DECRYPT(encText);
    }


    //기명 취소를 한 라이더들을 모아서 배치로 kb측으로 송신해주는 함수
//    @Async
//    @Scheduled(cron = "00 50 19 * * *") //초 분 시 일 월 요일
    public void withDrawRiderBatch() throws Exception {

        // 기명 취소한 내역 다 조회해온 다음에 내역 기반으로 라이더 조회해서 리스트로 만듬
        List<History> underWritingRequests = historyRepositorySupport.findRequestsByInsuranceStatusYesterday("071");
        List<Rider> riders = underWritingRequests.stream().map(History::getRider).collect(Collectors.toList());
        List<KbSignUpReq> signUpRequests = new ArrayList<>();

        riders.forEach( r-> {

            // 라이더 기명취소 시간 업데이트
            RiderInsuranceHistory riderInsuranceHistory = riderInsuranceHistoryRepository.findById(r.getId()).orElse(null);
            if (riderInsuranceHistory != null){
                riderInsuranceHistory.updateWithdrawRequestTime();
            }

            KbSignUpReq kbSignUpReq = new KbSignUpReq(r);

            // kb에 보낼 dto 만들어서 리스트에 저장
            String ssn = kbSignUpReq.getSsn();
            try {
                String rawSsn = ssnDecode(ssn);
                System.out.println("rawSsn = " + rawSsn);
                String encodeSsn = kbSsnEncode2(rawSsn);
                System.out.println("encodeSsn = " + encodeSsn);
                kbSignUpReq.updateSsn(encodeSsn);
                signUpRequests.add(kbSignUpReq);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

        });

        log.info("우리쪽 카운트 : {}", signUpRequests.size());

        //레트로핏으로 kb 측으로 전달
        KBRetrofitConfig<KbSignUpReq> KBRetrofitConfig = new KBRetrofitConfig<>();
        KBRetrofitConfig.create(KbApi.class).kbApi7Retrofit(signUpRequests).execute().body();
    }

    // 기명취소한 사람 배치 테스트 (당일 요청한사람)
    public void withDrawRiderTest() throws Exception {

        List<History> underWritingRequests = historyRepositorySupport.findRequestsByInsuranceStatusToday("071");
        List<Rider> riders = underWritingRequests.stream().map(History::getRider).collect(Collectors.toList());
        List<KbSignUpReq> signUpRequests = new ArrayList<>();

        riders.forEach( r-> {
            KbSignUpReq kbSignUpReq = new KbSignUpReq(r);

            String ssn = kbSignUpReq.getSsn();
            try {
                String rawSsn = ssnDecode(ssn);
                System.out.println("rawSsn = " + rawSsn);
                String encodeSsn = kbSsnEncode2(rawSsn);
                System.out.println("encodeSsn = " + encodeSsn);
                kbSignUpReq.updateSsn(encodeSsn);
                signUpRequests.add(kbSignUpReq);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

        });

        log.info("우리쪽 카운트 : {}", signUpRequests.size());
        KBRetrofitConfig<KbSignUpReq> KBRetrofitConfig = new KBRetrofitConfig<>();
        KBRetrofitConfig.create(KbApi.class).kbApi7Retrofit(signUpRequests).execute().body();
    }

    private void insuranceValidationCheck(Rider rider) {
        String insuranceStatus = rider.getInsuranceStatus();
        if(!insuranceStatus.contains("062")){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    //아마 안쓸것 같은 로그임 함수
    @Transactional(readOnly = true)
    public LoginRes login(LoginReq dto, HttpServletResponse res){
        Rider rider = findByPhone(dto.getPhone());
        matchPassword(dto.getPassword(),rider.getPassword());
        String token = createToken(rider);
        res.setHeader("Authorization",token);
        String driver_id = rider.getDriver_id();
        String insuranceStatus = rider.getInsuranceStatus();

        return new LoginRes(token,driver_id,insuranceStatus);
    }

    public String duplicateCheck(String phone){
        Rider rider = riderRepositorySupport.findByPhone(phone);
        if (rider != null) throw new BusinessException(ErrorCode.DUPLICATE_MEMBER);
        return "Y";
    }

    public Rider findByPhone(String phone){
        return riderRepository.findByPhone(phone).orElseThrow(() ->{
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        });
    }

    
    //jwt 토큰생성
    public String createToken(Rider rider){
        return tokenProvider.createToken(String.valueOf(rider.getId()),rider.getRoles());
    }

    // 패스워드 확인. 안쓰일듯 패스워드가 없으니
    public void matchPassword(String reqPassword, String userPassword){
        boolean matches = passwordEncoder.matches(reqPassword, userPassword);

        if(!matches){
            throw new BusinessException(ErrorCode.NOT_MATCH_PASSWORD);
        }
    }

    public void ssnCheck(String number){
        hyphenCheck(number);
        ssnLengthCheck(number);
    }

    //하이픈 포함 체크
    private void hyphenCheck(String number) {
        if (number.contains("-") || (!number.matches("[+-]?\\d*(\\.\\d+)?"))){
            throw new BusinessException(ErrorCode.INVALID_TYPE_VALUE2);
        }
    }

    //주민등록번호 길이 13자리 맞는지 체크
    private void ssnLengthCheck(String number) {
        if (number.length() != 13) {
            throw new BusinessException(ErrorCode.INVALID_LENGTH_VALUE);
        }
    }

    private String ssnDecode(String ssn) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(ssn);
        return decrypt;
    }

    private String kbSsnEncode2(String ssn) throws Exception {
        KB_AES_Encryption2 aes = new KB_AES_Encryption2();
        String encrypt = aes.encrypt(ssn);
        return encrypt;
    }

}
