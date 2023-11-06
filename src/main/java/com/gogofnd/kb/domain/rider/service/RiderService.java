package com.gogofnd.kb.domain.rider.service;


import com.gogofnd.kb.business.dto.req.*;
import com.gogofnd.kb.business.dto.res.CallsResultRes;
import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.insurance.dto.req.KbSignUpReq;
import com.gogofnd.kb.domain.insurance.entity.History;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepository;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepositorySupport;
import com.gogofnd.kb.domain.insurance.repository.RejectMessageRepositorySupport;
import com.gogofnd.kb.domain.rider.dto.req.LoginReq;
import com.gogofnd.kb.domain.rider.dto.req.RiderCsReq;
import com.gogofnd.kb.domain.rider.dto.res.*;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import com.gogofnd.kb.domain.rider.repository.RiderInsuranceHistoryRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.dto.response.PagingResponse;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.*;
import com.gogofnd.kb.global.utils.FindAge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RiderService {

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
//        String rawSsn = aes256Decode(rider.getSsn());
        String rawSsn = ssnDecode(rider.getSsn());
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


    //밍 기명등재와 기명취소가 동시에 동작이 안되는걸 로그로 확인하였음. 기명취소에 관하여 30초의 텀을 두고 진행 하기로 함.
    //기명 취소를 한 라이더들을 모아서 배치로 kb측으로 송신해주는 함수
    @Async
    @Scheduled(cron = "30 50 19 * * *") //초 분 시 일 월 요일
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

    //db에 저장되는 주민등록번호
    private String aesEncode(String str) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String encrypt = aes.encrypt(str);
        return encrypt;
    }

    // 라이더 보
    public RegistrationStatusRes getRiderInsuranceStatus(RegistrationStatusReq req, String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!req.getSeller_code().equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 주민등록번호 형태 체크 (13자리인지. 하이픈 없는지)
        String rawSsn = aes256Decode(req.getDriver_ssn());
        ssnCheck(rawSsn);

        log.debug("rawSsn : {}", rawSsn);
        System.out.println("rawSsn = " + rawSsn);
        String aes128Ssn = aesEncode(rawSsn);
        System.out.println("aes128Ssn = " + aes128Ssn);

        System.out.println(rawSsn +" :: " + aesEncode(rawSsn) +" :: " + req.getDriver_ssn());

        Rider rider = riderRepositorySupport.findByRiderSsn(rawSsn, req.getName());


        String rider_loginId = "";
        rider_loginId = rider.getLoginId();

        String rider_sellerName = "";
        rider_sellerName = rider.getSeller().getName();




        return new RegistrationStatusRes(rider_loginId,rider_sellerName);
    }

    //시간제 가입 상태 List
    public PagingResponse<RiderCsRes> getRiderCsList(Pageable pageable, RiderCsReq riderCsReq) throws Exception {

        return new PagingResponse<>(riderRepositorySupport.findRiderCsList(pageable, riderCsReq));
    }

    //시간제 라이더 메모
    public void putCsMemo(UpdateCsMemoReq param) throws Exception {

        riderRepositorySupport.updateCsMemo(param);
    }

    //시간제 가입 상태 List 엑셀다운로드
    public String outputCsListExcel(HttpServletResponse response, RiderCsReq req) {

        List<RiderCsRes> resList = riderRepositorySupport.findAllRiderCsList(req);
        createExcelDownloadResponse(response, resList);

        return "완료되었습니다.";
    }

    //시간제 가입 상태 List 엑셀 생성
    private void createExcelDownloadResponse(HttpServletResponse response, List<RiderCsRes> cs) {

        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("가입자현황");

            //파일명
            String todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            final String fileName = todayDate + "_보험_가입자";

            //헤더
            final String[] header = {"업체", "아이디", "라이더이름", "생년월일", "전화번호", "의무보험만기일자", "가입일자", "가입상태", "상태명", "거절사유", "사용여부"};
            Row row = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(header[i]);
            }

            //바디
            for (int i = 0; i < cs.size(); i++) {
                //헤더 이후로 데이터가 출력되어야하니 +1
                row = sheet.createRow(i + 1);

                RiderCsRes res = cs.get(i);

                Cell cell = null;
                cell = row.createCell(0);
                cell.setCellValue(res.getGroup_name());

                cell = row.createCell(1);
                cell.setCellValue(res.getLoginId());

                cell = row.createCell(2);
                cell.setCellValue(res.getName());

                cell = row.createCell(3);
                cell.setCellValue(res.getBirthDate());

                cell = row.createCell(4);
                cell.setCellValue(res.getPhone());

                cell = row.createCell(5);
                cell.setCellValue(res.getMtdt());

                cell = row.createCell(6);
                cell.setCellValue(res.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                cell = row.createCell(7);
                cell.setCellValue(res.getInsuranceStatus());

                cell = row.createCell(8);
                cell.setCellValue(res.getStatus_name());

                cell = row.createCell(9);
                cell.setCellValue(res.getReject_message());

                cell = row.createCell(10);
                cell.setCellValue(res.getUseYn());
            }


            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");

            workbook.write(response.getOutputStream());
            response.getOutputStream().close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<RiderGogoraRes> getRiderGoGoraInsuranceList(String cmpcd) throws Exception {
        return riderRepositorySupport.findRiderGoGoraInsuranceList(cmpcd);
    }

    public String deleteRider(Long riderId) throws BusinessException {
        Rider rider = riderRepository.findByIdAndUseYn(riderId, "Y").orElseThrow(() -> {return new BusinessException(ErrorCode.NOT_FOUND_USER); });
        Long riderCnt = riderRepository.countRiderByPhoneAndUseYn(rider.getPhone(), "N");

        if(rider.getSeller().getCmpcd().equals("G02") || rider.getSeller().getCmpcd().equals("G05") ) {
            postDeleteGogoraRider(rider.getDriver_id(), rider.getSeller().getCmpcd());
        }


        rider.delete(riderCnt);
        riderRepository.save(rider);

        return "Y";
    }

    public void putRiderLoginId(UpdateLoginIdReq updateLoginIdReq) throws Exception {

        riderRepositorySupport.updateRiderLoginId(updateLoginIdReq);
    }

    public String addVroongBalance(AddBalanceReq req) {

        Long result = riderRepositorySupport.updateVroongBalance(req);

        return "현재 예치금 잔액 : " + result;
    }

    public String updateRiderStatus(UpdateStatusReq req) {

        riderRepositorySupport.updateRiderStatus(req);

        return "OK";
    }

    public String updateInsureDate(UpdateInsureDateReq req) throws BusinessException {

        Rider rider = riderRepositorySupport.findByDriverId(req.getDriverId());

        if(Objects.equals(rider.getSeller().getInsureType(), "D")) {
            riderRepositorySupport.updateInsureStartEndDate(req);
        }
        else {
            throw new BusinessException(ErrorCode.INCORRECT_INSURE);
        }

        return "OK";
    }

    public String findRiderInsureStatus(String loginId)  {

        int result = riderRepositorySupport.findRiderInsureCount(loginId);

        if(result > 0) {
            return "Y";
        }
        return "N";
    }

    public String addHistory(AddHistoryReq req) throws BusinessException {

        Rider rider = riderRepositorySupport.findById(req.getRiderId());

        // 인수심사
        if(req.getReqType().equals("U")) {
            int resultSize = historyRepositorySupport.findHistoryApplicable(req.getRiderId(), "021");

            if (resultSize == 0) {
                History history = History.create(rider,"021", rider.getSeller());

                rider.updateInsuranceStatus("021");
                historyRepository.save(history);
            }
            else {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
        // 기명요청
        else if (req.getReqType().equals("E")) {
            int resultSize = historyRepositorySupport.findHistoryApplicable(req.getRiderId(), "051");

            if (resultSize == 0) {
                History history = History.create(rider,"051", rider.getSeller());

                rider.updateInsuranceStatus("051");
                historyRepository.save(history);
            }
            else {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
        else {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return "Y";
    }

    public void postDeleteGogoraRider(String driverId, String cmpcd) {
        String baseUrl;

        if(Objects.equals(cmpcd, "G02")) {
            baseUrl = "https://gogora.co.kr:9500/api/goGoRa";
        }
        else {
            baseUrl = "https://gogora.co.kr:9600/api/gogoon";
        }


        log.info("라이더 ID :: " + driverId);

        // webClient 기본 설정
        WebClient webClient =
                WebClient
                        .builder()
                        .baseUrl(baseUrl)
                        .build();

        // api 요청
        Map<String, Object> response =
                webClient
                        .post()
                        .uri("/web/retryKB?driverId=" + driverId)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

        // 결과 확인
        log.info(response.toString());
    }
}
