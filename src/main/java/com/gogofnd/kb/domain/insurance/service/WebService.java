package com.gogofnd.kb.domain.insurance.service;

import com.gogofnd.kb.domain.insurance.api.GogoSafeApi;
import com.gogofnd.kb.domain.insurance.api.KaKaoApi;
import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.insurance.dto.KaKaoChannelApi;
import com.gogofnd.kb.domain.insurance.dto.KbApiRiderWebDto;
import com.gogofnd.kb.domain.insurance.dto.TokenAndUrlRes;
import com.gogofnd.kb.domain.insurance.dto.req.GogoSafeSignUpReq;
import com.gogofnd.kb.domain.insurance.dto.req.KbApiWebReq;
import com.gogofnd.kb.domain.insurance.dto.req.KbSignUpReq;
import com.gogofnd.kb.domain.insurance.entity.WebHistory;
import com.gogofnd.kb.domain.insurance.repository.WebHistoryRepository;
import com.gogofnd.kb.domain.insurance.repository.WebHistoryRepositorySupport;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderWeb;
import com.gogofnd.kb.domain.rider.repository.*;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.domain.seller.repository.SellerRepository;
import com.gogofnd.kb.global.config.retrofit.GogoSafeRetrofitConfig;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.config.retrofit.KaKaoRetrofitConfig;
import com.gogofnd.kb.global.dto.response.AppResponse;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.AES256;
import com.gogofnd.kb.global.provider.AES_Encryption;
import com.gogofnd.kb.global.provider.SellerAES_Encryption;
import com.gogofnd.kb.global.provider.Sha256;
import com.gogofnd.kb.global.utils.FindAge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class WebService {
    @Value("${apikey}")
    private String kakaoApiKEY;

    private final RiderWebRepositorySupport riderWebRepositorySupport;
    private final RiderWebInsuranceHistoryRepository riderWebInsuranceHistoryRepository;
    private final RiderInsuranceHistoryRepositorySupport riderInsuranceHistoryRepositorySupport;
    private final RiderWebRepository riderWebRepository;
    private final WebHistoryRepositorySupport WebHistoryRepositorySupport;
    private final WebHistoryRepository WebHistoryRepository;
    private final SellerRepository sellerRepository;

    private final FindAge findAge;

    public String kbRiderWeb(KbApiWebReq req, String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        // 셀러코드가 api키를 복호환 값임.
        if(!req.getSeller_code().equals(decryptedApiKey)){
            log.info("decrypte :: " +decryptedApiKey);
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        req.setDriver_vcnum(req.getDriver_vcnum().replaceAll(" ",""));
        // 차량 번호 11 예외 처리
//        if(req.getDriver_vcnum().length() != 11 ){
//            throw new BusinessException(ErrorCode.INVALID_DRIVER_VCNUM);
//        }

        // 해당 지점 찾기

        // 주민등록번호 형태 체크 (13자리인지. 하이픈 없는지)
        String rawSsn = aesDecode(req.getDriver_ssn());

        ssnCheck(rawSsn);
        log.info(rawSsn);
        System.out.println("rawSsn = " + rawSsn);

        int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(6,13));

        String ageYn = findAge.CheckOverAge(AmericanAge);


        // 라이더 정보 저장 (주민번호 aes128로 다시 암호화해서 저장)
        Seller seller = sellerRepository.findSellerBySellerCode(req.getSeller_code());
        log.debug("rawSsn : {}", rawSsn);
        System.out.println("rawSsn = " + rawSsn);
        String aes128Ssn = aesEncode(rawSsn);
        System.out.println("aes128Ssn = " + aes128Ssn);
        req.encSsn(aes128Ssn);
        // api1 진행중에 라이더 생성하여 라이더 테이블에 저장하도록 객체 생성
        System.out.println("req.getDriver_ssn() = " + req.getDriver_ssn());
        RiderWeb riderWeb = RiderWeb.create(req, seller, rawSsn);

//        System.out.println("riderWeb.getPhone() :: " + riderWeb.getPhone());
//        System.out.println("riderWebInsuranceHistoryRepositorySupport.findByRiderWebPhone :: " + riderInsuranceHistoryRepositorySupport.findByRiderPhone(riderWeb.getPhone()));

//        라이더 디비에 저장하기 전에 이미 요청한 내역있는지 체크
        if(riderInsuranceHistoryRepositorySupport.findByRiderPhone(riderWeb.getPhone()) != null){
            log.info("already join in progress");
            throw new BusinessException(ErrorCode.ALREADY_REQUESTED);
        }

        Optional<RiderWeb> riderWebRepositoryByPhone = riderWebRepository.findByPhone(riderWeb.getPhone());
        String rStatus = "";

        if(!riderWebRepositoryByPhone.isEmpty()) {
            // 데이터값이 있을 때
            rStatus = riderWebRepositoryByPhone.get().getInsuranceStatus();
            System.out.println("rstatus :: " + rStatus);
            if (!rStatus.equals("011")) {
                return "이미 가입된 아이디 입니다.";
            }else{
                riderWeb = riderWebRepository.findByPhone(req.getDriver_phone()).get();
                riderWeb.updateRequestData(req);
            }
        }
        log.info("riderRepository.save(rider)");
        // 라이더레포지토리 저장에 라이더 객체를 넣어서 저장
        if(!rStatus.equals("011")) {
            System.out.println("InsuranceStatus 011 : "+rStatus);
            // 011 아닐 때
            riderWebRepository.save(riderWeb);

            String driverId = "GG" + padLeft(riderWeb.getId().toString(), 10);
            riderWeb.createDriverId(driverId);

            System.out.println("InsuranceStatus 011");
        }
        log.info("riderRepository.save(rider) done");


        //kb에 보낼 데이터 만들기
        String ssnBirth = createBirth(rawSsn);

        System.out.println("check driverId : " + riderWeb.getDriver_id());

        KbApiRiderWebDto dto = new KbApiRiderWebDto(riderWeb);

        dto.updateSsn_birthdate(ssnBirth);

        KBRetrofitConfig<KbApiRiderWebDto> KBRetrofitConfig = new KBRetrofitConfig<>();


        log.debug("dto {}", dto);
        //레트로핏 함수 실행부분
        log.debug("send to webretrofit");
        TokenAndUrlRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbRiderWebRetrfit(dto).execute().body(); /* 커넥션 타임아웃 발생 */
        log.debug("sent to webretrofit");
//        log.debug("urlResponse {}", urlResponse);
//
        if (urlResponse == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
//
//        // kb 에서 정해준 방식으로 웹뷰 url 생성
        Sha256 sha256 = new Sha256();
        String encToken = sha256.encrypt(urlResponse.getToken());
        String url = urlResponse.getUrl();

        String cmpcd = riderWebRepositorySupport.findBySellerCmpcd(req.getSeller_code());

        String totalWebViewUrl = url + "?proxy_driv_coorp_cmpcd=" + cmpcd + "&enc_token=" + encToken + "&driver_id=" + riderWeb.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return&tel="+ riderWeb.getPhone() + "&apcno=" +  riderWeb.getSeller().getApplication_number();


        riderWeb.updateTotalWebViewUrl(totalWebViewUrl.replace("https","http"));

        WebHistory history = WebHistory.create(riderWeb,"011", riderWeb.getSeller(),ageYn);
        riderWeb.updateInsuranceStatus("011");
        log.info("RiderWebHistoryRepository.save(riderWebHistory)");
        WebHistoryRepository.save(history);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //가입 신청한 유저 핸드폰으로 카카오톡 전송
        KaKaoChannelApi kDto = new KaKaoChannelApi();

        kDto.setTemplateCode("11665");
        kDto.setReserve("N");

        //카카오톡 보낼때 필요한 값들 세팅. 전화번호, 시간 , 웹용 url 모바일용 url
        kDto.makeList(req.getDriver_phone(),List.of(riderWeb.getName(),LocalDateTime.now().format(formatter)
                ,"gogora.co.kr:9888/api/goplan/kakao1?phone="+riderWeb.getPhone(),"gogora.co.kr:9888/api/goplan/kakao1?phone="+riderWeb.getPhone()));

        KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();

        kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();

        return riderWeb.getDriver_id();
    }

    public String kbRiderWeb2(String cmpcd, String riderId, String apiKey) throws Exception {

        List<KbApiWebReq> riderList = riderWebRepositorySupport.findAllRiderInfo(cmpcd, riderId);

        riderList.forEach( req -> {
            //복호화 . sellecode랑 비교
            String decryptedApiKey = null;
            try {
                decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.info("decrypt : {}",decryptedApiKey);
            // 셀러코드가 api키를 복호환 값임.
            if(!req.getSeller_code().equals(decryptedApiKey)){
                log.info("decrypte :: " +decryptedApiKey);
                throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
            }
            req.setDriver_vcnum(req.getDriver_vcnum().replaceAll(" ",""));
            // 차량 번호 11 예외 처리
//        if(req.getDriver_vcnum().length() != 11 ){
//            throw new BusinessException(ErrorCode.INVALID_DRIVER_VCNUM);
//        }

            // 해당 지점 찾기

            // 주민등록번호 형태 체크 (13자리인지. 하이픈 없는지)
            System.out.println(req.getDriver_ssn());

            String rawSsn = null;
            try {
                rawSsn = aesDecode(req.getDriver_ssn());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ssnCheck(rawSsn);
            log.info(rawSsn);
            System.out.println("rawSsn = " + rawSsn);

            int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(6,13));

            String ageYn = findAge.CheckOverAge(AmericanAge);


            // 라이더 정보 저장 (주민번호 aes128로 다시 암호화해서 저장)
            Seller seller = sellerRepository.findSellerBySellerCode(req.getSeller_code());
            log.debug("rawSsn : {}", rawSsn);
            System.out.println("rawSsn = " + rawSsn);
            String aes128Ssn = null;
            try {
                aes128Ssn = aesEncode(rawSsn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("aes128Ssn = " + aes128Ssn);
            req.encSsn(aes128Ssn);
            // api1 진행중에 라이더 생성하여 라이더 테이블에 저장하도록 객체 생성
            System.out.println("req.getDriver_ssn() = " + req.getDriver_ssn());
            RiderWeb riderWeb = RiderWeb.create(req, seller, rawSsn);

//        System.out.println("riderWeb.getPhone() :: " + riderWeb.getPhone());
//        System.out.println("riderWebInsuranceHistoryRepositorySupport.findByRiderWebPhone :: " + riderInsuranceHistoryRepositorySupport.findByRiderPhone(riderWeb.getPhone()));

//        라이더 디비에 저장하기 전에 이미 요청한 내역있는지 체크
            if(riderInsuranceHistoryRepositorySupport.findByRiderPhone(riderWeb.getPhone()) != null){
                log.info("already join in progress");
                throw new BusinessException(ErrorCode.ALREADY_REQUESTED);
            }

            Optional<RiderWeb> riderWebRepositoryByPhone = riderWebRepository.findByPhone(riderWeb.getPhone());
            String rStatus = "";

            if(!riderWebRepositoryByPhone.isEmpty()) {
                // 데이터값이 있을 때
                rStatus = riderWebRepositoryByPhone.get().getInsuranceStatus();
                System.out.println("rstatus :: " + rStatus);
                if (!rStatus.equals("011")) {
//                    return "이미 가입된 아이디 입니다.";
                    System.out.println("이미 가입된 아이디 입니다.");
                }else{
                    riderWeb = riderWebRepository.findByPhone(req.getDriver_phone()).get();
                    riderWeb.updateRequestData(req);
                }
            }
            log.info("riderRepository.save(rider)");
            // 라이더레포지토리 저장에 라이더 객체를 넣어서 저장
            if(!rStatus.equals("011")) {
                System.out.println("InsuranceStatus 011 : "+rStatus);
                // 011 아닐 때
                riderWebRepository.save(riderWeb);

                String driverId = "GG" + padLeft(riderWeb.getId().toString(), 10);
                riderWeb.createDriverId(driverId);

                System.out.println("InsuranceStatus 011");
            }
            log.info("riderRepository.save(rider) done");


            //kb에 보낼 데이터 만들기
            String ssnBirth = createBirth(rawSsn);

            System.out.println("check driverId : " + riderWeb.getDriver_id());

            KbApiRiderWebDto dto = new KbApiRiderWebDto(riderWeb);

            dto.updateSsn_birthdate(ssnBirth);

            KBRetrofitConfig<KbApiRiderWebDto> KBRetrofitConfig = new KBRetrofitConfig<>();


            log.debug("dto {}", dto);
            //레트로핏 함수 실행부분
            log.debug("send to webretrofit");
            TokenAndUrlRes urlResponse = null; /* 커넥션 타임아웃 발생 */
            try {
                urlResponse = KBRetrofitConfig.create(KbApi.class).kbRiderWebRetrfit(dto).execute().body();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log.debug("sent to webretrofit");
//        log.debug("urlResponse {}", urlResponse);
//
            if (urlResponse == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
//
//        // kb 에서 정해준 방식으로 웹뷰 url 생성
            Sha256 sha256 = new Sha256();
            String encToken = null;
            try {
                encToken = sha256.encrypt(urlResponse.getToken());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            String url = urlResponse.getUrl();
            String totalWebViewUrl = url + "?enc_token=" + encToken + "&driver_id=" + riderWeb.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return&tel="+ riderWeb.getPhone() + "&proxy_driv_coorp_cmpcd=" + cmpcd;

            riderWeb.updateTotalWebViewUrl(totalWebViewUrl.replace("https","http"));



            WebHistory history = WebHistory.create(riderWeb,"011", riderWeb.getSeller(),ageYn);
            riderWeb.updateInsuranceStatus("011");
            log.info("RiderWebHistoryRepository.save(riderWebHistory)");
            WebHistoryRepository.save(history);


            // kb db에 라이더 정보 저장하면서 동시에 고고세이프 디비에도 정보 저장
            GogoSafeRetrofitConfig gogoRetrofit = new GogoSafeRetrofitConfig();
            AppResponse body = null;
            try {
                body = gogoRetrofit.create(GogoSafeApi.class).getAppVer().execute().body();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log.info("String appVer = body => {}", body);
            assert body != null;
            log.info("String appVer = body.getData() => {}", body.getData());
            log.info("String appVer = abody.getData().get(\"appVer\") => {}", body.getData().get("appVer"));
            String appVer = body.getData().get("appVer");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String agreeDate = LocalDateTime.now().format(formatter);
            String runDate = LocalDateTime.now().format(formatter);

            System.out.println("req.getDriver_phone() = " + req.getDriver_phone());
            System.out.println("req.getDriver_phone() = " + req.getBikeModel());

            GogoSafeSignUpReq gogoSafeSignUpReq = GogoSafeSignUpReq.builder()
                    .phoneNumber(req.getDriver_phone())
                    .agree("Y")
                    .agreeDate(agreeDate)
                    .bikeModel(req.getBikeModel())
                    .name(req.getDriver_name())
                    .bikeNumber(req.getDriver_vcnum())
                    .mobileType(req.getMobileType())
                    .runDate(runDate)
                    .runMeter(0L)
                    .runStatus("")
                    .appVer(appVer)
                    .use("Y")
                    .powerType("시거잭")
                    .build();


            log.info("고고세이프 회원가입 실제 실행 {}");
            try {
                gogoRetrofit.create(GogoSafeApi.class).gogosafeSignUp(gogoSafeSignUpReq).execute().body(); // gosafe가 안들어감
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            log.info("고고세이프 회원가입 실행 종료 {}");

        //가입 신청한 유저 핸드폰으로 카카오톡 전송
        KaKaoChannelApi kDto = new KaKaoChannelApi();

        kDto.setTemplateCode("11665");
        kDto.setReserve("N");

//        카카오톡 보낼때 필요한 값들 세팅. 전화번호, 시간 , 웹용 url 모바일용 url
        kDto.makeList(req.getDriver_phone().substring(0, 11),List.of(riderWeb.getName(),LocalDateTime.now().format(formatter)
                ,"gogora.co.kr:9888/api/goplan/kakao1?phone="+riderWeb.getPhone(),"gogora.co.kr:9888/api/goplan/kakao1?phone="+riderWeb.getPhone()));
//                ,"112.175.41.179:9888/api/goplan/kakao1?phone="+riderWeb.getPhone(),"112.175.41.179:9888/api/goplan/kakao1?phone="+riderWeb.getPhone()));

        KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();

            try {
                kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("driverId - " + riderWeb.getDriver_id() +  ":: totalWebViewUrl - " + totalWebViewUrl);
        });

        return "완료되었습니다.";
    }


    public String kbSignedUrl(String loginId) throws Exception {

        System.out.println("loginId :: "+ loginId);
        String setting_phone = "010" + loginId.substring(3,11);
        System.out.println("setting_phone :: " + setting_phone);

        //        라이더 디비에 저장하기 전에 이미 요청한 내역있는지 체크
        if(!riderWebRepository.findByPhone(setting_phone).isPresent()){
            log.info("already join in progress");
            return "http://cf.navers.co.kr:3100/404";
        }

        Optional<RiderWeb> riderWebRepositoryByPhone = riderWebRepository.findByPhone(setting_phone);
        String rStatus = "";


        return riderWebRepositoryByPhone.get().getTotalWebViewUrl();
    }
    private String createBirth(String ssn) {
        String ssnBirth = ssn.substring(0, 6);
        return ssnBirth;
    }

//    //가입 동의 카카오톡 날아가고 나서, 라이더가 동의 완료하면 이 함수 호출하는 컨트롤러로 리다이렉트 됨
//    @SneakyThrows
//    public String api1Return(String phone){
//        // 보험 상태 히스토리값 생성해서 저장하고 라이더 보험상태 컬럼 변경
//
//        Rider rider = webRepositorySupport.findByPhone(phone);
//        System.out.println("phone = " + rider.getPhone());
//        System.out.println("getSsn = " + rider.getSsn());
//        String rawSsn = aesDecode(rider.getSsn());
//        System.out.println("rawSsn = " + rawSsn);
//        ssnCheck(rawSsn);
//        System.out.println("=============1===============");
//        int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
//        System.out.println("=============2===============");
//        String ageYn = findAge.CheckOverAge(AmericanAge);
//        System.out.println("=============3===============");
//        Seller seller = rider.getSeller();
//        System.out.println("=============4===============");
//
//        RiderWebHistory riderWebHistory = RiderWebHistory.create(rider,"021",seller,ageYn);
//        rider.updateInsuranceStatus("021");
//        riderWebHistoryRepository.save(riderWebHistory);
//
//        return "Y";
//    }

    private String aes256Decode(String encText) throws InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        AES256 aes256 = new AES256();
        return aes256.AES256_DECRYPT(encText);
    }

    private String aesEncode(String str) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String encrypt = aes.encrypt(str);
        return encrypt;
    }

    private String aesDecode(String str) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(str);
        return decrypt;
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

    private static String padLeft(String s, int n) {
        return String.format("%0" + n + "d", Integer.parseInt(s));
    }
}
