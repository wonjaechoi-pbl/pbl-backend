package com.gogofnd.kb.domain.insurance.service;

import com.gogofnd.kb.business.dto.res.InsuranceStatusListRes;
import com.gogofnd.kb.domain.insurance.api.KaKaoApi;
import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.insurance.api.GogoSafeApi;
import com.gogofnd.kb.domain.insurance.dto.*;
import com.gogofnd.kb.domain.insurance.dto.req.GogoSafeSignUpReq;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi1Req;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi4thReq;
import com.gogofnd.kb.domain.insurance.dto.res.KbApi4thRes;
import com.gogofnd.kb.domain.insurance.entity.History;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepository;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepositorySupport;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.repository.RiderInsuranceHistoryRepositorySupport;
import com.gogofnd.kb.domain.rider.repository.RiderInsuranceHistoryRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepository;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.domain.seller.repository.SellerRepository;
import com.gogofnd.kb.global.config.retrofit.GogoSafeRetrofitConfig;
import com.gogofnd.kb.global.config.retrofit.KaKaoRetrofitConfig;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.dto.response.AppResponse;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.*;
import com.gogofnd.kb.global.utils.FindAge;
import com.gogofnd.kb.global.utils.ResourceUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceService {
    @Value("${apikey}")
    private String kakaoApiKEY;

    private final RiderRepositorySupport riderRepositorySupport;
    private final RiderInsuranceHistoryRepositorySupport riderInsuranceHistoryRepositorySupport;
    private final HistoryRepositorySupport historyRepositorySupport;
    private final RiderRepository riderRepository;
    private final HistoryRepository historyRepository;
    private final SellerRepository sellerRepository;

    private final FindAge findAge;

    //운영사에서 고고에프앤디측으로 가입요청하는 api.   운영사 -> 고고 -> kb
    public String kbApi1(KbApi1Req req, MultipartFile image, String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        // 셀러코드가 api키를 복호환 값임.
        if(!req.getSeller_code().equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        req.setDriver_vcnum(req.getDriver_vcnum().replaceAll(" ",""));
        // 차량 번호 11 예외 처리
//        if(req.getDriver_vcnum().length() != 11 ){
//            throw new BusinessException(ErrorCode.INVALID_DRIVER_VCNUM);
//        }

        if(req.getMtdt() !=null && req.getOprn_purp() != null)
            log.info(req.getMtdt() + " mtdt :: oprn " + req.getOprn_purp());

        // 해당 지점 찾기

        // 주민등록번호 형태 체크 (13자리인지. 하이픈 없는지)
        String rawSsn = aes256Decode(req.getDriver_ssn());

        ssnCheck(rawSsn);
        log.info(rawSsn);
        log.info("rawSsn = " + rawSsn);

        int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(6,13));

        String ageYn = findAge.CheckOverAge(AmericanAge);


        // 라이더 정보 저장 (주민번호 aes128로 다시 암호화해서 저장)
        Seller seller = sellerRepository.findSellerBySellerCode(req.getSeller_code());
        log.debug("rawSsn : {}", rawSsn);
        log.info("rawSsn = " + rawSsn);
        String aes128Ssn = aesEncode(rawSsn);
        log.info("aes128Ssn = " + aes128Ssn);
        req.encSsn(aes128Ssn);
        // api1 진행중에 라이더 생성하여 라이더 테이블에 저장하도록 객체 생성
        log.info("req.getDriver_ssn() = " + req.getDriver_ssn());
        Rider rider = Rider.create(req, seller, rawSsn);

        
        //라이더 디비에 저장하기 전에 이미 요청한 내역있는지 체크
        if(riderInsuranceHistoryRepositorySupport.findByRiderPhone(rider.getPhone()) != null){
            log.info("already join in progress");
            throw new BusinessException(ErrorCode.ALREADY_REQUESTED);
        }
        int totalYoungCount = historyRepositorySupport.findTotalYoungRiderCount();
        int totalOlderCount = historyRepositorySupport.findTotalOlderRiderCount();

        int sellerYoungCount = historyRepositorySupport.findYoungRiderCount(seller.getSellerCode());
        int sellerOlderCount = historyRepositorySupport.findOlderRiderCount(seller.getSellerCode());

        if(totalYoungCount > 100){
            log.info("too many 21~24 age rider over 100people");
            throw new BusinessException(ErrorCode.REJECTED_TOO_MANY_RIDER);
        }

        if(totalYoungCount > totalOlderCount){
            //밍 위아래 두개가 비교가 안되서 로그에서 확인가능하게 만듦
            log.info("totalYoungCount > totalOlderCount too many 21~24 age rider");
            throw new BusinessException(ErrorCode.REJECTED_TOO_MANY_RIDER);
        }
        if(sellerYoungCount > sellerOlderCount){
            //밍 위아래 두개가 비교가 안되서 로그에서 확인가능하게 만듦
            log.info("sellerYoungCount > sellerOlderCount too many 21~24 age rider");
            throw new BusinessException(ErrorCode.REJECTED_TOO_MANY_RIDER);
        }

        Optional<Rider> riderRepositoryByPhone = riderRepository.findByPhone(rider.getPhone());
        String rStatus = "";
        if(riderRepositoryByPhone.isPresent()) {
            // 데이터값이 있을 때
            rStatus = riderRepositoryByPhone.get().getInsuranceStatus();
            if (!rStatus.equals("011")) {
                return "이미 가입된 아이디 입니다.";
            }else {
                //011 이중 처리로 라이더 인증이 안될 경우. 처리하는 방안
                return riderRepositoryByPhone.get().getTotalWebViewUrl();
            }
        }

        // 라이더레포지토리 저장에 라이더 객체를 넣어서 저장
        riderRepository.save(rider);

        String driverId = "GG" + padLeft(rider.getId().toString(), 10);
        rider.createDriverId(driverId);

        log.info("riderRepository.save(rider) done");


        log.info("============================1=========================");
        //보험 증서 이미지 저장
        if(image != null) {
            log.info("============================2=========================");
                String folderPath = "/"+rider.getId() + "/";
                String filePath = folderPath + image.getOriginalFilename();

                try{
                    log.info("============================3=========================");
                    ResourceUtil.saveFile(image,filePath,folderPath);
                    rider.updateImage(filePath);
                }catch (IOException e){
                    log.info("============================4=========================");
                    log.error(e.getMessage());
                }
        }
        log.info("============================5=========================");

         //kb에 보낼 데이터 만들기
        String ssnBirth = createBirth(rawSsn);

        System.out.println("check driverId : " + rider.getDriver_id());

        KbDto dto = new KbDto(rider);

        dto.updateSsn_birthdate(ssnBirth);

        KBRetrofitConfig<KbDto> KBRetrofitConfig = new KBRetrofitConfig<>();


        log.debug("dto {}", dto);
        //레트로핏 함수 실행부분
        log.debug("send to KBretrofit");
        TokenAndUrlRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbApi1Retrofit(dto).execute().body(); /* 커넥션 타임아웃 발생 */
        log.debug("sent to KBretrofit");
//        log.debug("urlResponse {}", urlResponse);
//
        if (urlResponse == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
//
//        // kb 에서 정해준 방식으로 웹뷰 url 생성
        Sha256 sha256 = new Sha256();
        String encToken = sha256.encrypt(urlResponse.getToken());
//
        String url = urlResponse.getUrl();
        String totalWebViewUrl = url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return&tel="+ rider.getPhone() + "&proxy_driv_coorp_cmpcd=" + rider.getSeller().getCmpcd();
//        String totalWebViewUrl = url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://112.175.41.179:9888/api/goplan/1/return";
        rider.updateTotalWebViewUrl(totalWebViewUrl.replace("https","http"));



        History history = History.create(rider,"011", rider.getSeller(),ageYn);
        rider.updateInsuranceStatus("011");
        log.info("historyRepository.save(history)");
        historyRepository.save(history);


        // kb db에 라이더 정보 저장하면서 동시에 고고세이프 디비에도 정보 저장
        GogoSafeRetrofitConfig gogoRetrofit = new GogoSafeRetrofitConfig();
        AppResponse body = gogoRetrofit.create(GogoSafeApi.class).getAppVer().execute().body();
        log.info("String appVer = body => {}", body);
        assert body != null;
        log.info("String appVer = body.getData() => {}", body.getData());
        log.info("String appVer = body.getData().get(\"appVer\") => {}", body.getData().get("appVer"));
        String appVer = body.getData().get("appVer");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String agreeDate = LocalDateTime.now().format(formatter);
        String runDate = LocalDateTime.now().format(formatter);

        log.info("req.getDriver_phone() = " + req.getDriver_phone());
        log.info("req.getDriver_phone() = " + req.getBikeModel());

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
        gogoRetrofit.create(GogoSafeApi.class).gogosafeSignUp(gogoSafeSignUpReq).execute().body(); // gosafe가 안들어감
        log.info("고고세이프 회원가입 실행 종료 {}");

        //가입 신청한 유저 핸드폰으로 카카오톡 전송
        KaKaoChannelApi kDto = new KaKaoChannelApi();

        kDto.setTemplateCode("11665");
        kDto.setReserve("N");

        //카카오톡 보낼때 필요한 값들 세팅. 전화번호, 시간 , 웹용 url 모바일용 url
        kDto.makeList(req.getDriver_phone(),List.of(rider.getName(),LocalDateTime.now().format(formatter)
                ,"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone()));
//                ,"112.175.41.179:9888/api/goplan/kakao1?phone="+rider.getPhone(),"112.175.41.179:9888/api/goplan/kakao1?phone="+rider.getPhone()));

        KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();

        kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();

        return totalWebViewUrl;
    }

    private String createBirth(String ssn) {
        String ssnBirth = ssn.substring(0, 6);
        return ssnBirth;
    }

    //가입 동의 카카오톡 날아가고 나서, 라이더가 동의 완료하면 이 함수 호출하는 컨트롤러로 리다이렉트 됨
    @SneakyThrows
    public String api1Return(String phone){
        // 보험 상태 히스토리값 생성해서 저장하고 라이더 보험상태 컬럼 변경

        String phoneNumber = phone.substring(0, 11);

        Rider rider = riderRepositorySupport.findByPhone(phoneNumber);

        int statusCount = historyRepositorySupport.findRequestsByRiderInsuranceStatusToday("021", rider.getId());

        if(statusCount > 0) {
            log.info("이미 오늘 날짜로 만들어진 status가 있습니다. :: " + statusCount);
            return "Y";
        }

        System.out.println("phone = " + rider.getPhone());
        System.out.println("getSsn = " + rider.getSsn());
        String rawSsn = aesDecode(rider.getSsn());
        log.info("rawSsn = " + rawSsn);
        ssnCheck(rawSsn);
        log.info("=============1===============");
        int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
        log.info("=============2===============");
        String ageYn = findAge.CheckOverAge(AmericanAge);
        log.info("=============3===============");
        Seller seller = rider.getSeller();
        log.info("=============4===============");

        History history = History.create(rider,"021",seller,ageYn);
        rider.updateInsuranceStatus("021");
        historyRepository.save(history);

        return "Y";
    }

    @Async
    @Scheduled(cron = "0 0 09 * * *")
    public String kbApi4th() throws Exception {
        log.info("테스트2");
        // 언더라이팅 승인 받은 사람들 리스트로 뽑아와서,
        List<History> histories = historyRepositorySupport.findRequestsByInsuranceStatusTodayForAPi4();
        log.info("테스트3");
        // 스트림으로 라이더 리스트로 변환
        List<Rider> riders = histories.stream().map(History::getRider).collect(Collectors.toList());


        log.info("테스트1");

        // atomic이라고 써진애들은 전부 스트림안에서 동시성 문제가 발생해서 만들어준거고 , 이 경우에 String이랑 똑같음
        AtomicReference<String> totalWebViewUrl = new AtomicReference<>("");

        log.info("테스트4");
        riders.forEach(rider-> {
            log.info("테스트5");
            //라이더 리스트 각 라이더들 dto로 만들어서 레트로핏으로 kb에 전달
            KbApi4thReq dto = new KbApi4thReq(rider);
            log.info("dto = " + dto);

            KBRetrofitConfig<KbApi4thReq> KBRetrofitConfig = new KBRetrofitConfig<>();
            log.info("테스트6");
            try {
                log.info("테스트7");
                KbApi4thRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbApi4Retrofit(dto).execute().body();
                log.info("urlResponse = " + urlResponse);

                // kb에서 준 url값 null이면 익셉션
                if(urlResponse == null){
                    log.info("테스트8");
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
                log.info("테스트9");

                Sha256 sha256 = new Sha256();
                String encToken = sha256.encrypt(urlResponse.getToken());

                log.info("테스트10");

                String url = urlResponse.getUrl();

                log.info("테스트11");

                // kb에서 요청한 방식으로 url 생성
                totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/4/return&tel="+rider.getPhone() + "&proxy_driv_coorp_cmpcd=" + rider.getSeller().getCmpcd() + "");
//                totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/4/return");
                rider.updateTotalWebViewUrl(totalWebViewUrl.get().replace("https","http"));

                log.info("테스트12 ::" + totalWebViewUrl.toString());

//                String rawSsn = null;
//                try {
//                    rawSsn = aesDecode(rider.getSsn());
//                } catch (InvalidAlgorithmParameterException e) {
//                    throw new RuntimeException(e);
//                } catch (NoSuchPaddingException e) {
//                    throw new RuntimeException(e);
//                } catch (IllegalBlockSizeException e) {
//                    throw new RuntimeException(e);
//                } catch (BadPaddingException e) {
//                    throw new RuntimeException(e);
//                } catch (InvalidKeyException e) {
//                    throw new RuntimeException(e);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//                ssnCheck(rawSsn);

//                int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
//                String ageYn = findAge.CheckOverAge(AmericanAge);
                Seller seller = rider.getSeller();

                log.info("테스트13");


                // api4번 요청한걸로 가입히스토리 내역 만들고 라이더 보험상태 컬럼값 업데이트
                History history = History.create(rider,"041",seller);
                rider.updateInsuranceStatus("041");

                //라이더한테 카카오톡 보냄
                KaKaoChannelApi kDto = new KaKaoChannelApi();

                log.info("테스트14");

                kDto.setTemplateCode("11666");
                kDto.setReserve("N");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                kDto.makeList(rider.getPhone(),List.of(rider.getName(),LocalDateTime.now().format(formatter),
                        "gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone()));
//                "gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone()));

                KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();

                kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();

                historyRepository.save(history);

            } catch (IOException | NoSuchAlgorithmException e) {
                log.error("계약체결 이행동의 api 에러 발생 : {}",e);
            }
        });

        return totalWebViewUrl.get();
    }

    public String kbApi4thSpecific(Long riderId) {
        // 언더라이팅 승인 받은 특정라이더 추출
//        History history = historyRepositorySupport.findByInsuranceStatusAPi4Specific(riderId);
        // 스트림으로 라이더 리스트로 변환
//        List<Rider> riders = histories.stream().map(History::getRider).collect(Collectors.toList());
//        Rider rider = history.getRider();

        Rider rider = riderRepository.findById(riderId).orElseThrow(() -> {return new BusinessException(ErrorCode.NOT_FOUND_USER); });

        // atomic이라고 써진애들은 전부 스트림안에서 동시성 문제가 발생해서 만들어준거고 , 이 경우에 String이랑 똑같음
        AtomicReference<String> totalWebViewUrl = new AtomicReference<>("");
        KbApi4thReq dto = new KbApi4thReq(rider);
        log.info("dto = " + dto);

        KBRetrofitConfig<KbApi4thReq> KBRetrofitConfig = new KBRetrofitConfig<>();
        try {
            KbApi4thRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbApi4Retrofit(dto).execute().body();
            log.info("urlResponse = " + urlResponse);

            // kb에서 준 url값 null이면 익셉션
            if(urlResponse == null){
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            Sha256 sha256 = new Sha256();
            String encToken = sha256.encrypt(urlResponse.getToken());
            String url = urlResponse.getUrl();

            // kb에서 요청한 방식으로 url 생성
            totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/4/return&tel="+rider.getPhone() + "&proxy_driv_coorp_cmpcd=" + rider.getSeller().getCmpcd() + "");
            rider.updateTotalWebViewUrl(totalWebViewUrl.get().replace("https","http"));

            log.info("totalWebViewUrl = " + totalWebViewUrl.toString());
            Seller seller = rider.getSeller();


            // api4번 요청한걸로 가입히스토리 내역 만들고 라이더 보험상태 컬럼값 업데이트
            History history = History.create(rider,"041",seller);
            rider.updateInsuranceStatus("041");

            //라이더한테 카카오톡 보냄
            KaKaoChannelApi kDto = new KaKaoChannelApi();

            kDto.setTemplateCode("11666");
            kDto.setReserve("N");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            kDto.makeList(rider.getPhone(),List.of(rider.getName(),LocalDateTime.now().format(formatter),
                    "gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone()));

            KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();
            kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();

            historyRepository.save(history);

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("계약체결 이행동의 api 에러 발생 : {}",e);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return totalWebViewUrl.get();

//        riders.forEach(rider-> {
//            //라이더 리스트 각 라이더들 dto로 만들어서 레트로핏으로 kb에 전달
//            KbApi4thReq dto = new KbApi4thReq(rider);
//            log.info("dto = " + dto);
//
//            KBRetrofitConfig<KbApi4thReq> KBRetrofitConfig = new KBRetrofitConfig<>();
//            try {
//                KbApi4thRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbApi4Retrofit(dto).execute().body();
//                log.info("urlResponse = " + urlResponse);
//
//                // kb에서 준 url값 null이면 익셉션
//                if(urlResponse == null){
//                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
//                }
//
//                Sha256 sha256 = new Sha256();
//                String encToken = sha256.encrypt(urlResponse.getToken());
//                String url = urlResponse.getUrl();
//
//                // kb에서 요청한 방식으로 url 생성
//                totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/4/return&tel="+rider.getPhone() + "&proxy_driv_coorp_cmpcd=" + rider.getSeller().getCmpcd() + "");
//                rider.updateTotalWebViewUrl(totalWebViewUrl.get().replace("https","http"));
//
//                log.info("totalWebViewUrl = " + totalWebViewUrl.toString());
//                Seller seller = rider.getSeller();
//
//
//                // api4번 요청한걸로 가입히스토리 내역 만들고 라이더 보험상태 컬럼값 업데이트
//                History history = History.create(rider,"041",seller);
//                rider.updateInsuranceStatus("041");
//
//                //라이더한테 카카오톡 보냄
//                KaKaoChannelApi kDto = new KaKaoChannelApi();
//
//                kDto.setTemplateCode("11666");
//                kDto.setReserve("N");
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//                kDto.makeList(rider.getPhone(),List.of(rider.getName(),LocalDateTime.now().format(formatter),
//                    "gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao4?phone="+rider.getPhone()));
//
//                KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();
//                kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();
//
//                historyRepository.save(history);
//
//            } catch (IOException | NoSuchAlgorithmException e) {
//                log.error("계약체결 이행동의 api 에러 발생 : {}",e);
//            } catch (Exception e){
//                throw new RuntimeException(e);
//            }
//        });
    }

    //2번째 가입 동의 카카오톡 날아가고 나서, 라이더가 동의 완료하면 이 함수 호출하는 컨트롤러로 리다이렉트 됨
    public String api4Return(String phone){
        Rider rider = riderRepositorySupport.findByPhone(phone);
//        String rawSsn = null;
//        try {
//            rawSsn = aesDecode(rider.getSsn());
//        } catch (InvalidAlgorithmParameterException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchPaddingException e) {
//            throw new RuntimeException(e);
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        } catch (IllegalBlockSizeException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        } catch (BadPaddingException e) {
//            throw new RuntimeException(e);
//        } catch (InvalidKeyException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        ssnCheck(rawSsn);
//
//        int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
//        String ageYn = findAge.CheckOverAge(AmericanAge);

        History history = History.create(rider,"051", rider.getSeller());
        rider.updateInsuranceStatus("051");
        historyRepository.save(history);
        return "Y";
    }
    
    public String getTotalUrlApi1(String phone){
        Rider rider = riderRepositorySupport.findByPhone(phone);
//        if(!rider.getInsuranceStatus().equals("011")) return "api1Success";
        return rider.getTotalWebViewUrl();
    }

    public String getTotalUrlApi4(String phone){
        Rider rider = riderRepositorySupport.findByPhone(phone);

//        if(rider.getInsuranceStatus().contains("05") || rider.getInsuranceStatus().contains("06")) return "api4Success";
        return rider.getTotalWebViewUrl();
    }

    private String aes256Encode(String plainText) throws InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        AES256 aes256 = new AES256();
        return aes256.AES256_ENCRYPT(plainText);
    }

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

    private String kbSsnEncode(String ssn) throws Exception {
        KB_AES_Encryption aes = new KB_AES_Encryption();
        String encrypt = aes.encrypt(ssn);
        return encrypt;
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



    //정책 바뀌어서 뺐는데, 혹시 몰라서 남겨놓음. 원래는 kb측에서 리턴으로 주는 값이 갯수가 고정되어있었는데, 추가 분이 계속 생길 수 있다고해서 현재는 case문 안 걸고
    //kb에서 준 값 그대로 db에 저장하는 상태
    private String verify4thResult(String result){

        switch (result) {
            case "ready": {
                return "성공";
            }
            case "incorrect_application_numbers": {
                throw new BusinessException(ErrorCode.INCORRECT_APPLICATION_NUMBER);
            }
            case "underwriting_needed": {
                throw new BusinessException(ErrorCode.UNDERWRITING_NEEDED);
            }
            case "insure_needed": {
                throw new BusinessException(ErrorCode.INSURE_NEEDED);
            }
            case "rejected_nomodel": {
                throw new BusinessException(ErrorCode.REJECTED_NO_MODEL);
            }
            case "rejected_nouse": {
                throw new BusinessException(ErrorCode.REJECTED_NO_USE);
            }
            case "rejected_rider":{
                log.info("rejected_rider");
                throw new BusinessException(ErrorCode.REJECTED_TOO_MANY_RIDER);
            }
        }

        return result;
    }


    // 이미 디비에 가입요청했는대 카카오 웹뷰 안들어간사람들 다시 카카오웹뷰 보낼때 쓸려고 만든 함수
    // 운영에 쓰이는건 아니고 제가 포스트맨 다시 쏴서 일일히 가입시키기 귀찮아서 만듬
    public String kakaoBatch(String insuranceStatus) throws Exception {
        List<Rider> riders = historyRepositorySupport.findKakaoBatch(insuranceStatus);
        log.info("찾아온 라이더 수 : {}",riders.size());

        AtomicReference<String> totalWebViewUrl = new AtomicReference<>("");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        riders.forEach(rider -> {
            //라이더 디비에 저장하기 전에 이미 요청한 내역있는지 체크
            if(riderInsuranceHistoryRepositorySupport.findByRiderPhone(rider.getPhone()) != null){
                throw new BusinessException(ErrorCode.ALREADY_REQUESTED);
            }

            try{

                //kb에 보낼 데이터 만들기
                String ssnBirth = createBirth(aesDecode(rider.getSsn()));

                KbDto dto = new KbDto(rider);
                dto.updateSsn_birthdate(ssnBirth);

                KBRetrofitConfig<KbDto> KBRetrofitConfig = new KBRetrofitConfig<>();

                log.info("dto = " + dto);
            //레트로핏 함수 실행부분
            TokenAndUrlRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbApi1Retrofit(dto).execute().body();
            log.info("urlResponse = " + urlResponse);

            if (urlResponse == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            // kb 에서 정해준 방식으로 웹뷰 url 생성
            Sha256 sha256 = new Sha256();
            String encToken = "";


                encToken = sha256.encrypt(urlResponse.getToken());

                String url = urlResponse.getUrl();
                totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://112.175.41.179:9888/api/goplan/1/return&tel="+rider.getPhone());
//                totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return");
                rider.updateTotalWebViewUrl(totalWebViewUrl.get().replace("https","http"));
                log.info("totalWebViewUrl = " + totalWebViewUrl);

                String rawSsn = aesDecode(rider.getSsn());
                ssnCheck(rawSsn);

                int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
                String ageYn = findAge.CheckOverAge(AmericanAge);


                History history = History.create(rider,"011", rider.getSeller(), ageYn);
                historyRepository.save(history);

                //가입 신청한 유저 핸드폰으로 카카오톡 전송
                KaKaoChannelApi kDto = new KaKaoChannelApi();

                kDto.setTemplateCode("11665");
                kDto.setReserve("N");

                //카카오톡 보낼때 필요한 값들 세팅. 전화번호, 시간 , 웹용 url 모바일용 url
                kDto.makeList(rider.getPhone(),List.of(rider.getName(),LocalDateTime.now().format(formatter)
                        ,"112.175.41.179:9888/api/goplan/kakao1?phone="+rider.getPhone(),"112.175.41.179:9888/api/goplan/kakao1?phone="+rider.getPhone()  + "&proxy_driv_coorp_cmpcd=" + rider.getSeller().getCmpcd()));
//                ,"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone()));

                KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();
                kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();
            }
            catch (IOException | NoSuchAlgorithmException e) {
                log.error("계약체결 이행동의 api 에러 발생 : {}",e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return totalWebViewUrl.get();
    }

    
    //이미 가입동의 했는데 카카오 접속 안한사람들 드라이버 아이디로 찾아오는 함수. 원래 리스트 말고 개별로 받아야하는데 복붙하느라 리스트로 받아옴
    public String kakaoBatchByDriverId(String driverId) throws Exception {
//        Rider rider = historyRepositorySupport.findKakaoBatchByDriverId(driverId);
        Rider rider = riderRepository.findByDriver_id(driverId).orElseThrow(() -> {
            return new BusinessException(ErrorCode.NOT_FOUND_USER);
        });
//        riderRepository.save(rider);
//        log.info("찾아온 라이더 수 : {}",riders.size());

        String totalWebViewUrl = "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if(riderInsuranceHistoryRepositorySupport.findByRiderPhone(rider.getPhone()) != null){
            throw new BusinessException(ErrorCode.ALREADY_REQUESTED);
        }

        try{
            //kb에 보낼 데이터 만들기
            String ssnBirth = createBirth(aesDecode(rider.getSsn()));

            KbDto dto = new KbDto(rider);
            dto.updateSsn_birthdate(ssnBirth);

            KBRetrofitConfig<KbDto> KBRetrofitConfig = new KBRetrofitConfig<>();

            log.info("dto = " + dto);
            //레트로핏 함수 실행부분
            TokenAndUrlRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbApi1Retrofit(dto).execute().body();
            log.info("urlResponse = " + urlResponse);

            if (urlResponse == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            // kb 에서 정해준 방식으로 웹뷰 url 생성
            Sha256 sha256 = new Sha256();
            String encToken = "";

            encToken = sha256.encrypt(urlResponse.getToken());

            String url = urlResponse.getUrl();

            if(rider.getSeller().getSellerCode().equals("1444432576f")) {
                totalWebViewUrl = url + "?proxy_driv_coorp_cmpcd=G02" + "&enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return&tel="+ rider.getPhone() + "&apcno=" +  rider.getSeller().getApplication_number();
            }
            else {
                totalWebViewUrl = url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return&tel="+ rider.getPhone() + "&proxy_driv_coorp_cmpcd=" + rider.getSeller().getCmpcd();
            }

            rider.updateTotalWebViewUrl(totalWebViewUrl.replace("https","http"));
            log.info("total : " + totalWebViewUrl);

            String rawSsn = aesDecode(rider.getSsn());
            ssnCheck(rawSsn);

            int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
            String ageYn = findAge.CheckOverAge(AmericanAge);

            rider.updateInsuranceStatus("011");
//            riderRepository.save(rider);

            History history = History.create(rider,"011", rider.getSeller(), ageYn);
            historyRepository.save(history);

            //가입 신청한 유저 핸드폰으로 카카오톡 전송
            KaKaoChannelApi kDto = new KaKaoChannelApi();

            kDto.setTemplateCode("11665");
            kDto.setReserve("N");

            //카카오톡 보낼때 필요한 값들 세팅. 전화번호, 시간 , 웹용 url 모바일용 url
            kDto.makeList(rider.getPhone(),List.of(rider.getName(),LocalDateTime.now().format(formatter)
                    ,"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone()));

            KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();
            kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();
        }
        catch (IOException | NoSuchAlgorithmException e) {
            log.error("가입설계동의 api 에러 발생 : {}",e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        riders.forEach(rider -> {
//            //라이더 디비에 저장하기 전에 이미 요청한 내역있는지 체크
//            if(riderInsuranceHistoryRepositorySupport.findByRiderPhone(rider.getPhone()) != null){
//                throw new BusinessException(ErrorCode.ALREADY_REQUESTED);
//            }
//
//            try{
//
//                //kb에 보낼 데이터 만들기
//                String ssnBirth = createBirth(aesDecode(rider.getSsn()));
//
//                KbDto dto = new KbDto(rider);
//                dto.updateSsn_birthdate(ssnBirth);
//
//                KBRetrofitConfig<KbDto> KBRetrofitConfig = new KBRetrofitConfig<>();
//
//                log.info("dto = " + dto);
//                //레트로핏 함수 실행부분
//                TokenAndUrlRes urlResponse = KBRetrofitConfig.create(KbApi.class).kbApi1Retrofit(dto).execute().body();
//                log.info("urlResponse = " + urlResponse);
//
//                if (urlResponse == null) {
//                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
//                }
//
//                // kb 에서 정해준 방식으로 웹뷰 url 생성
//                Sha256 sha256 = new Sha256();
//                String encToken = "";
//
//                encToken = sha256.encrypt(urlResponse.getToken());
//
//                String url = urlResponse.getUrl();
//                totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return&tel="+rider.getPhone() + "&proxy_driv_coorp_cmpcd=" + rider.getSeller().getCmpcd());
////                totalWebViewUrl.set(url + "?enc_token=" + encToken + "&driver_id=" + rider.getDriver_id() + "&return_url=" + "http://gogora.co.kr:9888/api/goplan/1/return");
//                rider.updateTotalWebViewUrl(totalWebViewUrl.get().replace("https","http"));
//                log.info("total : " + totalWebViewUrl);
//
//                String rawSsn = aesDecode(rider.getSsn());
//                ssnCheck(rawSsn);
//
//                int AmericanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(7,13));
//                String ageYn = findAge.CheckOverAge(AmericanAge);
//
//
//                History history = History.create(rider,"011", rider.getSeller(), ageYn);
//                historyRepository.save(history);
//
//                //가입 신청한 유저 핸드폰으로 카카오톡 전송
//                KaKaoChannelApi kDto = new KaKaoChannelApi();
//
//                kDto.setTemplateCode("11665");
//                kDto.setReserve("N");
//
//                //카카오톡 보낼때 필요한 값들 세팅. 전화번호, 시간 , 웹용 url 모바일용 url
//                kDto.makeList(rider.getPhone(),List.of(rider.getName(),LocalDateTime.now().format(formatter)
//                        ,"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone(),"gogora.co.kr:9888/api/goplan/kakao1?phone="+rider.getPhone()));
//
//                KaKaoRetrofitConfig kakao = new KaKaoRetrofitConfig();
//                kakao.create(KaKaoApi.class).kakaoPost(kakaoApiKEY,kDto).execute().body();
//
//            }
//            catch (IOException | NoSuchAlgorithmException e) {
//                log.error("가입설계동의 api 에러 발생 : {}",e);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });

        return totalWebViewUrl;
    }
    public List<InsuranceStatusListRes> riderInsStatus() {
        return historyRepositorySupport.findByInsuranceStatus();
    }

    private static String padLeft(String s, int n) {
        return String.format("%0" + n + "d", Integer.parseInt(s));
    }
}
