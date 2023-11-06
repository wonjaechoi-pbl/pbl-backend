package com.gogofnd.kb.domain.delivery.service;

import com.gogofnd.kb.business.dto.req.ServiceSensorInitReqDto;
import com.gogofnd.kb.business.dto.req.ServiceSensorReqDto;
import com.gogofnd.kb.business.dto.res.*;
import com.gogofnd.kb.domain.delivery.dto.insure.req.DeliveryInsureHistoryReq;
import com.gogofnd.kb.domain.delivery.dto.insure.res.*;
import com.gogofnd.kb.domain.delivery.repository.*;

import com.gogofnd.kb.domain.insurance.api.GogoSafeApi;
import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.domain.seller.dto.SellerCodeAndDateReq;
import com.gogofnd.kb.domain.seller.dto.SellerCodeReq;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.CallSettlement;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.domain.seller.repository.SellerRepository;
import com.gogofnd.kb.global.config.retrofit.GogoSafeRetrofitConfig;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.AES_Encryption;
import com.gogofnd.kb.global.provider.SellerAES_Encryption;
import com.gogofnd.kb.business.dto.req.RiderPhoneReq;
import com.gogofnd.kb.global.utils.FindAge;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;


import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@ToString
public class DeliveryService {
    private final CallRepository callRepository;
    private final CallSettlementRepository callSettlementRepository;
    private final CallRepositorySupport callRepositorySupport;
    private final CallSettlementRepositorySupport  callSettlementRepositorySupport ;
    private final RiderRepositorySupport riderRepositorySupport;
    private final AccidentRepository accidentRepository;
    private final AccidentRepositorySupport accidentRepositorySupport;
    private final SellerRepository sellerRepository;
    private final FindAge findAge;

    // 12.운행 이력
    //하루 동안 운행한 이력을 전달해야함
    //pk값으로 SELECT 후 KB로 데이터 전송해야함
    @Async// 2번 12번 api 비동기로 동시에 배치되게 설정
    @Scheduled(cron = "0 30 06 * * *")
    public CountDto getTotalDelivery(){
        
        // 딜버, 온나 운행이력 범위
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0)).minusDays(2);
        LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0)).minusDays(1);

        // 부릉, 일보험 운행이력 범위
        LocalDateTime vrStartDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(6,0,0)).minusDays(2);
        LocalDateTime vrEndDate = LocalDateTime.of(LocalDate.now(),LocalTime.of(6,0,0)).minusDays(1);

        // 운행이력 구함(요청시간 기반)
        List<CallSettlement> calls  = callSettlementRepositorySupport.findCallSettlementList(startDate, endDate);
        List<CallSettlement> vrCalls  = callSettlementRepositorySupport.findVrCallSettlementList(vrStartDate, vrEndDate);

        List<DeliveryInsureHistoryReq> deliveryInsureHistoryReqList = new ArrayList<>();

        // 딜버, 온나
        calls.forEach(c ->{
            log.info("운행(딜버/온나) :: " + c.getGroupId());
            DeliveryInsureHistoryReq dto = new DeliveryInsureHistoryReq(c);

            dto.TotalTimeSetting(callSettlementRepositorySupport.getDaysRiderTotalTime(dto.getDriver_id(),startDate,endDate));
            callSettlementRepositorySupport.updateSettlementStatus(c.getGroupId(), "Y");

            deliveryInsureHistoryReqList.add(dto);
        });

        // 부릉, 일보험
        vrCalls.forEach(c ->{
            log.info("운행(부릉/일보험) :: " + c.getGroupId());
            DeliveryInsureHistoryReq dto = new DeliveryInsureHistoryReq(c);

            dto.TotalTimeSetting(callSettlementRepositorySupport.getDaysRiderTotalTime(dto.getDriver_id(),vrStartDate,vrEndDate));
            callSettlementRepositorySupport.updateSettlementStatus(c.getGroupId(), "Y");

            deliveryInsureHistoryReqList.add(dto);
        });

        log.info("deliveryInsureHistoryReqList :: "  + deliveryInsureHistoryReqList);

        CountDto countDto = new CountDto();

        //레트로핏 만들어서 kb에 전달
        KBRetrofitConfig<DeliveryInsureHistoryReq> KBRetrofitConfig = new KBRetrofitConfig<>();

        try {
            countDto = KBRetrofitConfig.create(KbApi.class).kbApi12Retrofit(deliveryInsureHistoryReqList).execute().body();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        deliveryInsureHistoryReqList.stream().forEach(d-> log.info("운행이력 :: " + d.getCall_id()));

        return countDto;
    }

    @Transactional(readOnly = true)
    //손해율
    public RateInfoRes getRateInfo(SellerCodeReq dto,String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!dto.getSeller_code().equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // 운영사 조회후, 운영사가 kb와 계약한 시간당 깎이는 요금 분으로 나눠서 balance에 담음
        Seller seller = sellerRepository.findSellerBySellerCode(dto.getSeller_code());
        String balance = String.valueOf((seller.getChargingPerHour()) / 60);

        // 운영사 사고들 총 보상금 조회해옴
        Integer totalCompensation = accidentRepositorySupport.findTotalCompensation(dto.getSeller_code());
        System.out.println("totalCompensation = " + totalCompensation);
        System.out.println("sellerBalance = " + seller.getBalance());

        // 손해율 = (총 보상금) / (운영사가 입금한 예납금) * 100
        double lossRate = ((double)totalCompensation / seller.getBalance())*100;
        String strLossRate = String.format("%.2f", lossRate);
        return new RateInfoRes(strLossRate,balance);
    }

    private LocalDate convertDate(String date){
        return  LocalDate.parse(date,DateTimeFormatter.ISO_DATE);
    }

    public List<DrivingHistoryResponse> findHistoryBySellerCode(SellerCodeAndDateReq req,String apiKey) throws Exception {
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!req.getSeller_code().equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        //운영사 운행기록 날짜 받아와서 조회
        List<Call> callList = callRepositorySupport.findCallsBySellerCode(req.getSeller_code()
                ,LocalDateTime.of(convertDate(req.getSeller_startdate()),LocalTime.MIN)
                ,LocalDateTime.of(convertDate(req.getSeller_enddate()),LocalTime.MAX));

        //dto로 변환해서 리턴
        return callList.stream().map(DrivingHistoryResponse::new).collect(Collectors.toList());
    }



    // 14. 운행 이력 조회
    // 72시간 동안 운행한 이력 반환

    public List<DeliveryInsureHistorykbResponseDto> historyKb(DeliveryInsureHistoryReq deliveryInsureHistoryReq){
        //kb --> 우리 db 조회

        //72시간 구함
        LocalDateTime startDatetime = LocalDateTime.now();
        LocalDateTime endDatetime = LocalDateTime.now().minusHours(72L);

        String driver_id = deliveryInsureHistoryReq.getDriver_id();
        // driver_id로 rider를 구한다.
        Rider rider =  riderRepositorySupport.findByDriverId(driver_id);
        //조회한 rider의 id를 넣는다

        Long rider_id = rider.getId();
        String name = rider.getName();
        System.out.println("14번 api 호출 ======================================================");
        System.out.println("rider_id :"+rider_id);
        System.out.println("driver_id :"+driver_id);
        System.out.println("name :"+name);


        //유닉스 타임으로 변환 해야함
        List<CallSettlement> callList = callSettlementRepository.findAllByCallPickUpTimeBetweenAndRiderId(endDatetime,startDatetime,rider_id);
        List<DeliveryInsureHistorykbResponseDto> deliveryInsureHistorykbResponseDtoList = new ArrayList<>();
        callList.forEach(call ->{
            DeliveryInsureHistorykbResponseDto dto = new DeliveryInsureHistorykbResponseDto(call);
            System.out.println("dto = " + dto);
            deliveryInsureHistorykbResponseDtoList.add(dto);
        });

        return deliveryInsureHistorykbResponseDtoList;
    }

    // 앱에서 일 라이더 운행 현황 조회 api
    public List<DailyDrivingRes> findDailyDrive(RiderPhoneReq req){
        Rider rider = riderRepositorySupport.findByPhone(req.getPhone());
        return callRepositorySupport.findDailyCalls(rider.getId());
    }

    //당일 라이더 운행이력 조회
    public DailyDrivingTotalRes findDailyDriveTotal(RiderPhoneReq req){
        Rider rider = riderRepositorySupport.findByPhone(req.getPhone());

        //라이더 아이디로 당일 운행기록조회
        List<DailyDrivingRes> dailyCalls = callRepositorySupport.findDailyCalls(rider.getId());

        //운행횟수
        Integer totalCount = dailyCalls.size();

        //총 차감금액
        Long totalBalance = dailyCalls.stream().mapToLong(DailyDrivingRes::getBalance).sum();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        AtomicLong totalMinute = new AtomicLong();

        dailyCalls.forEach(c -> {
            LocalDateTime startDate = LocalDateTime.parse(c.getStart_date(), formatter);
            LocalDateTime endDate = LocalDateTime.parse(c.getEnd_date(), formatter);

            Duration between = Duration.between(startDate, endDate);

            //총 운행한 minute
            totalMinute.addAndGet(between.toMinutes());
        });

        //조회한 값들로 dto 만들어서 반환
        return new DailyDrivingTotalRes(totalCount.toString(),totalMinute.toString(),totalBalance.toString());
    }

    // 당일 운행이력 합
//    public CountDto getTotalDeliveryTest(){
//
//        LocalDateTime startDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0,0));
//        LocalDateTime endDatetime = LocalDateTime.of(LocalDate.now(),LocalTime.of(23,59,59));
//
//        System.out.println(startDatetime + " :: " + endDatetime);
//
//
////        List<Call> calls  = callRepository.findAllByCallRequestTimeBetween(startDatetime,endDatetime);
//        List<Call> calls  = callRepository.findAllByCallPickUpTimeBetween(startDatetime,endDatetime);
//
//        System.out.println("call :: " + calls.size());
//        //call 테이블에서 오늘 날짜 기준으로 데이터 가져옴
//        List<DeliveryInsureHistoryReq> deliveryInsureHistoryReqList = new ArrayList<>();
//        calls.forEach(c ->{
//            //call 테이블에서 기사 테이블 조인후
//            //기사 테이블에서 활동 지점 테이블 조인해야함
//            DeliveryInsureHistoryReq dto = new DeliveryInsureHistoryReq(c);
//            //밍 이건 수정할 필요가 있음 리소스 너무 잡아먹어
//            dto.TotalTimeSetting(callRepositorySupport.todayRider(dto.getDriver_id()));
//
//            dto.updateTarget_location(c.getDelivery_Address());
//            deliveryInsureHistoryReqList.add(dto);
//        });
//
//        String total;
//        total = Integer.toString((int)Math.ceil ((double) (deliveryInsureHistoryReqList.get(0).getStart_time()-deliveryInsureHistoryReqList.get(0).getEnd_time())/60));
//
//        System.out.println("calls : "+calls);
//
//        CountDto countDto = new CountDto();
//        KBRetrofitConfig<DeliveryInsureHistoryReq> KBRetrofitConfig = new KBRetrofitConfig<>();
//        try {
//            countDto = KBRetrofitConfig.create(KbApi.class).kbApi12Retrofit(deliveryInsureHistoryReqList).execute().body();
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
//        }
//
//        deliveryInsureHistoryReqList.stream().forEach(d-> System.out.println(d.getTarget_location()));
//        return countDto;
//    }


    // 특정일 운행이력 합
//    public CountDto getSpecificTotalDeliveryTest(LocalDate localDate){
//
//        LocalDateTime startDatetime = LocalDateTime.of(localDate, LocalTime.of(0,0,0));
//        LocalDateTime endDatetime = LocalDateTime.of(localDate,LocalTime.of(23,59,59));
//
//        System.out.println(startDatetime + " :: " + endDatetime);
//
//
////        List<Call> calls  = callRepository.findAllByCallRequestTimeBetween(startDatetime,endDatetime);
//        List<Call> calls  = callRepository.findAllByCallPickUpTimeBetween(startDatetime,endDatetime);
//
//        System.out.println("call :: " + calls.size());
//        //call 테이블에서 오늘 날짜 기준으로 데이터 가져옴
//        List<DeliveryInsureHistoryReq> deliveryInsureHistoryReqList = new ArrayList<>();
//        calls.forEach(c ->{
//            //call 테이블에서 기사 테이블 조인후
//            //기사 테이블에서 활동 지점 테이블 조인해야함
//            DeliveryInsureHistoryReq dto = new DeliveryInsureHistoryReq(c);
//            //밍 이건 수정할 필요가 있음 리소스 너무 잡아먹어
//            dto.TotalTimeSetting(callRepositorySupport.specificDateRider(localDate,dto.getDriver_id()));
//
//            dto.updateTarget_location(c.getDelivery_Address());
//            deliveryInsureHistoryReqList.add(dto);
//        });
//
//        String total;
//        total = Integer.toString((int)Math.ceil ((double) (deliveryInsureHistoryReqList.get(0).getStart_time()-deliveryInsureHistoryReqList.get(0).getEnd_time())/60));
//
//        System.out.println("calls : "+calls);
//
//        CountDto countDto = new CountDto();
//        KBRetrofitConfig<DeliveryInsureHistoryReq> KBRetrofitConfig = new KBRetrofitConfig<>();
//        try {
//            countDto = KBRetrofitConfig.create(KbApi.class).kbApi12Retrofit(deliveryInsureHistoryReqList).execute().body();
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
//        }
//
//        deliveryInsureHistoryReqList.stream().forEach(d-> System.out.println(d.getTarget_location()));
//        return countDto;
//    }


    // 특정일 라이더 한명 운행이력 합
//    public CountDto getCallIdDeliveryTest(String id){
//
//        Call call  = callRepositorySupport.findByKbCallId(id);
//
//        if(call.getKb_call_id().isBlank())
//            throw new BusinessException(ErrorCode.DRIVER_ERROR);
//
//
//        System.out.println(call);
//        System.out.println("call_id() " + call.getKb_call_id());
//
//        //call 테이블에서 오늘 날짜 기준으로 데이터 가져옴
//        List<DeliveryInsureHistoryReq> deliveryInsureHistoryReqList = new ArrayList<>();
//
//
//        //call 테이블에서 기사 테이블 조인후
//        //기사 테이블에서 활동 지점 테이블 조인해야함
//        DeliveryInsureHistoryReq dto = new DeliveryInsureHistoryReq(call);
//        //밍 이건 수정할 필요가 있음 리소스 너무 잡아먹어
//        dto.TotalTimeSetting(callRepositorySupport.specificDateRider(dto.getDriver_id(),call.getCallPickUpTime()));
//
//        dto.updateTarget_location(call.getDelivery_Address());
//        deliveryInsureHistoryReqList.add(dto);
//
//
//
//        String total;
//        total = Integer.toString((int)Math.ceil ((double) (deliveryInsureHistoryReqList.get(0).getStart_time()-deliveryInsureHistoryReqList.get(0).getEnd_time())/60));
//
//        System.out.println("calls : "+call);
//
//        CountDto countDto = new CountDto();
//        KBRetrofitConfig<DeliveryInsureHistoryReq> KBRetrofitConfig = new KBRetrofitConfig<>();
//        try {
//            countDto = KBRetrofitConfig.create(KbApi.class).kbApi12Retrofit(deliveryInsureHistoryReqList).execute().body();
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
//        }
//
//        deliveryInsureHistoryReqList.stream().forEach(d-> System.out.println(d.getTarget_location()));
//        return countDto;
//    }

    public ServiceSensorResDto getGogosafeStatus(ServiceSensorReqDto dto, String apiKey) throws Exception {

        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!dto.getSeller_code().equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String rider_id = dto.getDriver_id();
        Rider rider = riderRepositorySupport.findByLoginId(dto.getDriver_id());

        GogoSafeRetrofitConfig gogosafeRetrofit = new GogoSafeRetrofitConfig();
        HashMap<String, Long> map = new HashMap<>();

        //여기 getSensorActiveInfo라는 함수에서 호출하는 url이 고고세이프 api에 구현이 되어있는데, 1분(5분? 기억이 안남)간 센서 데이터가 들어오지 않으면
        //앱-센서 링크가 끊긴걸로 인식. 아니면 앱-센서랑 연결된걸로 인식
        try {
            Response<HashMap<String,Long>> execute = gogosafeRetrofit.create(GogoSafeApi.class).getSensorActiveInfo(rider.getPhone()).execute();

            if(execute.isSuccessful()){
                map = execute.body();
            }

            Long data = map.get("data");
            System.out.println("data = " + data);
            //고고세이프 api를 호출해서 data 라는 변수에 값을 받아온다.
            String appLinked = "N";
            String sever_message = "센서 연결해주세요";
            String insurance_status = "N";

            //받아온 값이 있으면 앱이 연결된 것으로 알고 appLinked를 y로 변경한다.
            if(data != null && data != 0){
                appLinked = "Y";
                sever_message ="동작중";
            }
            String insuranceStatus = rider.getInsuranceStatus();

            if(insuranceStatus.equals("062")){
                insurance_status="Y";
            }
//            String rawSsn = ssnDecode(rider.getSsn());
//            int americanAge = findAge.getAmericanAge(rawSsn.substring(0,6),rawSsn.substring(6,13));
//            String checkOverAge = findAge.CheckOverAge(americanAge);
//
//            if(checkOverAge.equals("Y") && appLinked.equals("Y")){
//                insurance_status = "Y";
//            }
//            if(checkOverAge.equals("N")){
//                insurance_status ="Y";
//            }

            System.out.println("appLinked = " + appLinked);
            //

            return new ServiceSensorResDto(appLinked,sever_message,insurance_status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private String ssnDecode(String ssn) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(ssn);
        return decrypt;
    }

    public void initGogosafeData(ServiceSensorInitReqDto dto, String apiKey) {

        String rider_id = dto.getDriver_id();
        Rider rider = riderRepositorySupport.findByDriverId(rider_id);
        GogoSafeRetrofitConfig gogosafeRetrofit = new GogoSafeRetrofitConfig();

        try {
            Response<HashMap<String,Long>> execute = gogosafeRetrofit.create(GogoSafeApi.class).getSensorActiveInfo(rider.getPhone()).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
