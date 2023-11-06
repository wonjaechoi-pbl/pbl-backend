package com.gogofnd.kb.domain.seller.service;


import com.gogofnd.kb.business.dto.res.CallsEndRes;
import com.gogofnd.kb.domain.delivery.entity.BalanceHistory;
import com.gogofnd.kb.domain.delivery.repository.*;
import com.gogofnd.kb.domain.insurance.api.KbApi;
import com.gogofnd.kb.domain.insurance.api.GogoSafeApi;
import com.gogofnd.kb.domain.insurance.dto.*;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi10thReq;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi11Req;
import com.gogofnd.kb.domain.insurance.dto.req.KbApi9thReq;
import com.gogofnd.kb.domain.insurance.dto.req.KbSignUpReq;
import com.gogofnd.kb.domain.insurance.repository.HistoryRepositorySupport;
import com.gogofnd.kb.domain.rider.dto.res.InsuranceStatusRes;
import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.entity.RiderInsuranceHistory;
import com.gogofnd.kb.domain.rider.repository.RiderRepositorySupport;
import com.gogofnd.kb.domain.seller.dto.Kb10thRequest;
import com.gogofnd.kb.domain.seller.entity.Call;
import com.gogofnd.kb.domain.seller.entity.CallSettlement;
import com.gogofnd.kb.domain.seller.entity.Seller;
import com.gogofnd.kb.domain.seller.repository.SellerRepository;
import com.gogofnd.kb.global.config.retrofit.GogoSafeRetrofitConfig;
import com.gogofnd.kb.global.config.retrofit.KBRetrofitConfig;
import com.gogofnd.kb.global.error.exception.BusinessException;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.gogofnd.kb.global.provider.AES256;
import com.gogofnd.kb.global.provider.AES_Encryption;
import com.gogofnd.kb.global.provider.SellerAES_Encryption;
import com.gogofnd.kb.business.dto.req.Kb11thRequest;
import com.gogofnd.kb.business.dto.res.DrivingEndRes;
import com.gogofnd.kb.business.dto.res.DrivingStartRes;
import com.gogofnd.kb.global.utils.FindAge;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.type.DateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import retrofit2.Response;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallService {
    private final RiderRepositorySupport riderRepositorySupport;
    private final CallRepositorySupport callRepositorySupport;
    private final CallRepository callRepository;
    private final CallSettlementRepository callSettlementRepository;
    private final CallSettlementRepositorySupport callSettlementRepositorySupport;

    private final BalanceHistoriesRepository balanceHistoriesRepository;
    private final SellerRepository sellerRepository;
    private final HistoryRepositorySupport historyRepositorySupport;

    private final FindAge findAge;

    //9번째 , 이거 안씀. kb에서 9번째 api 운행이력조회는 안쓸확률 매우 높다고함.
    public String kbApi9th(String driverId) throws IOException {
        Rider rider = riderRepositorySupport.findByDriverId(driverId);
//        Seller seller = riderRepositorySupport.findBySellerId(rider.getSeller());
//
//        System.out.println("증권번호 호출 : " + seller.getPolicy_number());

        KBRetrofitConfig<ResultDto> KBRetrofitConfig = new KBRetrofitConfig<>();
        ResultDto dto = KBRetrofitConfig.create(KbApi.class).kbApi9Retrofit(new KbApi9thReq(rider)).execute().body();
        if (dto == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (!dto.getResult().equals("ok")) {
            throw new BusinessException(ErrorCode.DRIVER_ERROR);
        }
        return "ok";
    }

    // 시간 포맷 변환
    public LocalDateTime convertDate(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(date,formatter);
    }


    // 운행시작
    public DrivingStartRes kb10th(Kb10thRequest dto,String apiKey) throws Exception {
        log.info("운행시작 call_id :: " + dto.getCall_id());

        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!dto.getSeller_code().equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        Rider rider = riderRepositorySupport.findByLoginIdAndSellerCode(dto.getDriver_id(),dto.getSeller_code());

        if(rider.getStatus() == 0){
            throw new BusinessException(ErrorCode.No_MONEY);
        }

        String seller_code = dto.getSeller_code();
        Seller seller = sellerRepository.findSellerBySellerCode(seller_code);

        // 같은 call_id로 배달을 2건 이상 요청한경우
        Call recentcall = callRepositorySupport.findByCallId2(dto.getCall_id());
        if(recentcall != null) throw new BusinessException(ErrorCode.DUPE_CALL_ID);

        // 062 상태 인지 확인 start ---------------------------------------------------------------------------------
        //라이더 보험 상태값 --> 딜버 박병규 요청 사항
        String status =  rider.getInsuranceStatus();
        String st =  status.substring(1,2);
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
            resultMessage = historyRepositorySupport.findByRiderAndStatusOrderCrd(rider.getId(),status).getRejectReason();
        }
        if(resultCode.equals("2")){
            resultMessage ="성공";
        }

        InsuranceStatusRes insuranceStatusRes = new InsuranceStatusRes(resultProgress, resultCode, resultMessage);

        System.out.println("rider.getInsuranceStatus() = " + rider.getInsuranceStatus());
        if(!rider.getInsuranceStatus().equals("062")){
            throw new BusinessException(ErrorCode.UNREADY_INSURANCE);
        }
        // 062 상태 인지 확인 end ------------------------------------------------------------------------------------

        // 업체별 예치금 확인
        if(rider.getPayStatus().equals("N")){
            throw new BusinessException(ErrorCode.No_MONEY);
        }

        //운영사에서 넘겨준 dto값을 기반으로 call을 만든다. getCall_requesttime,getCall_appointtime null로 받아도 상관 없게 만드는중
        Call call;
        if( Optional.ofNullable(dto.getCall_requesttime()).isEmpty() || Optional.ofNullable(dto.getCall_appointtime()).isEmpty()){
            call = Call.create(rider,dto);
        }else{
            call = Call.create(rider,dto, convertDate(dto.getCall_requesttime()), convertDate(dto.getCall_appointtime()));
        }

        // 부릉 운행 종료 누락 건 체크
        if(dto.getSeller_code().equals("1444432576f")) {
            List<String> result = callRepositorySupport.findNullCompleteTimeCallId(dto.getDriver_id());

            log.info("부릉 누락 건 : " + result);

            if(!result.isEmpty()) {
                String nullEndTimeCallIds = String.join(",", result);

                updateCompleteTime(nullEndTimeCallIds);
            }
        }

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String nowDate = now.format(formatter);

        CallSettlement callSettlement;


        long dailyTotal = 0;
        String driverId = dto.getDriver_id();
        String appointTime = dto.getCall_appointtime();
        int riderFirstCallCount = callRepositorySupport.selectFirstRiderCall(driverId);

        // 라이더의 첫 운행시작일 경우
        if(riderFirstCallCount == 0) {
            String newGroupId = "GR"+  nowDate + "-" + rider.getId().toString()  + "-0001";
            call.updateGroupId(newGroupId);

            callSettlement = CallSettlement.create(rider, call.getGroupId());

            call.updateCallPickUpTime(appointTime);
            callSettlement.updateCallPickUpTime(appointTime);
            callSettlement.updateDailyTotalRiding(dailyTotal);

            callSettlementRepository.save(callSettlement);
        }
        else {
            //  운행 그룹핑
            int countComplateCalls = callSettlementRepositorySupport.selectNullCompleteTimeCallCount(driverId);

            // 해당 라이더의 운행종료가 안된 call이 있는지 확인. (묶음 배송 미완료 건 확인)
            if(countComplateCalls == 0) {
                int count = callSettlementRepositorySupport.selectNewDateCallCount(driverId);

                if(count == 0) {
                    // 일자 기준 첫 call 일 경우 GRYYYYMMDD-ID-000001 부터 채번
                    String newGroupId = "GR"+  nowDate + "-" + rider.getId().toString()  + "-0001";
                    call.updateGroupId(newGroupId);
                }
                else {
                    // 일자 기준 첫 call이 아닐 경우. MAX(group_id)+1
                    String groupId = callSettlementRepositorySupport.findCallsByLastGroupId(rider.getId().toString(), nowDate);

                    // 기존 groupId로 인한 empty 값 일때를 위한 예외처리 --삭제예정
                    if(groupId.isEmpty()) {
                        String newGroupId = "GR"+  nowDate + "-" + rider.getId().toString()  + "-0001";
                        call.updateGroupId(newGroupId);
                    }
                    else {
                        String groupNum = groupId.substring(groupId.lastIndexOf('-') + 1);

                        int number = Integer.parseInt(groupNum);
                        number++; // +1
                        String plusGroupNum = String.format("%04d", number);

                        String newGroupId = "GR"+  nowDate + "-" + rider.getId().toString()  + "-" + plusGroupNum;
                        call.updateGroupId(newGroupId);
                    }
                }

                callSettlement = CallSettlement.create(rider, call.getGroupId());

                dailyTotal = callSettlementRepositorySupport.findByComplete(driverId);
                callSettlement.updateDailyTotalRiding(dailyTotal);

                call.updateCallPickUpTime(appointTime);
                callSettlement.updateCallPickUpTime(appointTime);

                callSettlementRepository.save(callSettlement);
            }
            else {
                // 묶음 배송 건이 있을 경우 Group_ID 추출
                String groupId = callSettlementRepositorySupport.findCallsByDupeGroupId(driverId);
                call.updateGroupId(groupId);
                call.updateCallPickUpTime(appointTime);
            }
        }

        call.updateDelivery_status("픽업");
        callRepository.save(call);

        // kb_call_id 생성
        String padId = "C" + padLeft(call.getId().toString(), 15);
        call.createKbCallId(padId);

        // kb 측으로 운행 정보를 전달한다.
        KbApi10thReq request = new KbApi10thReq(call.getRider(), call.getKb_call_id(), call.getCallAppointTime());
        KBRetrofitConfig<KbApi10thReq> KBRetrofitConfig = new KBRetrofitConfig<>();

        ResultDto body = KBRetrofitConfig.create(KbApi.class).kbApi10Retrofit(request).execute().body();
        if(body != null && body.getResult().equals("ok")) rider.insuranceApply();

        return new DrivingStartRes("ok",insuranceStatusRes.getDriver_status(),insuranceStatusRes.getDriver_message());
    }

    // 운행종료
    public DrivingEndRes kb11th(Kb11thRequest dto,String apiKey) throws Exception {
        log.info("운행종료 call_id :: " + dto.getCall_id());
        //복호화 . sellecode랑 비교
        String decryptedApiKey = SellerAES_Encryption.decrypt(apiKey);
        log.info("decrypt : {}",decryptedApiKey);
        if(!dto.getSeller_code().equals(decryptedApiKey)){
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        //운행정보를 조회한다.
        Call call = callRepositorySupport.findByCallId(dto.getCall_id());
        if(call ==null){
            throw new BusinessException(ErrorCode.DRIVER_ERROR);
        }
        LocalDateTime endDate = convertDate(dto.getDriver_enddate());

        CallSettlement callResult = callSettlementRepositorySupport.findCalls(call.getGroupId());

        // 이미 종료된 보험입니다.
        if(call.getDelivery_status().equals("완료"))
            throw new BusinessException(ErrorCode.ALREADY_COMPLETE,"balance:"+callResult.getBalance()+"|runDate:"+
                    Math.ceil((double) (
                            Duration.between(
                                    callResult.getCallPickUpTime(),
                                    callResult.getCallCompleteTime()).getSeconds())/60));

        // 운행정보 컬럼값들을 배달 완료로 업데이트한다.
        call.complete(endDate);
        call.updateDelivery_status("완료");

        // 062 상태 인지 확인 start ---------------------------------------------------------------------------------
        Rider rider = call.getRider();
        //라이더 보험 상태값 --> 딜버 박병규 요청 사항

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
            resultMessage = historyRepositorySupport.findByRiderAndStatusOrderCrd(rider.getId(),status).getRejectReason();
        }
        if(resultCode.equals("2")){
            resultMessage ="성공";
        }

        InsuranceStatusRes insuranceStatusRes = new InsuranceStatusRes(resultProgress, resultCode, resultMessage);
        // 062 상태 인지 확인 end ------------------------------------------------------------------------------------

        // 해당 call의 group_id 추출
        String groupId = callSettlementRepositorySupport.findByGroupId(dto.getCall_id());

        // 추출한 group_id로 정산(그룹핑) 테이블 row 추출
        CallSettlement callSettlement = callSettlementRepositorySupport.findByCallSettlement(groupId);

        int pickupCount = callSettlementRepositorySupport.findCallsByPickupStatusCount(dto.getDriver_id(), groupId);

        double minute = 0;
        long balance = 0;

        // 해당 call이 묶음배송 마지막 call일 경우
        if(pickupCount == 0) {
            // 정산테이블 운행종료시간 update
            callSettlement.updateCompleteTime(endDate);
            callSettlementRepository.save(callSettlement);

            Seller seller = rider.getSeller();

            //날짜 차이를 계산하여 보험료 차감값을 계산한다.
            long seconds = Duration.between(callSettlement.getCallPickUpTime(), callSettlement.getCallCompleteTime()).getSeconds();

            if(seconds<0)
                throw new BusinessException(ErrorCode.ENDTIME_REJECTED); // 완료시간은 시작시간보다 빠를 수 없습니다.

            // 1분단위로 금액을 차감하는데, 65초면 2분으로 계산함
            // ceil 메소드 파라미터, 리턴 타입때문에 더블로 형변환
            minute = Math.ceil((double) seconds/60);
            System.out.println("minute = " + minute);

            // 시간제, 일보험 구분하여 balance 책정
            if (Objects.equals(rider.getSeller().getInsureType(), "D")) {
                balance = seller.getFlat_rate().longValue();
            }
            else {
                double firstBdFee = ((seller.getFirst_bd_fee()/60) * minute * seller.getDiscount_rate()); // 대인 1
                double secondBdFee = ((seller.getSecond_bd_fee()/60) * minute * seller.getDiscount_rate()); // 대인 2
                double pdFee = ((seller.getPd_fee()/60) * minute * seller.getDiscount_rate()); // 대물

                balance = (long) ((long) Math.floor(firstBdFee) + Math.floor(secondBdFee) + Math.floor(pdFee));
            }

            // 누적운행시간 300분 초과 (정액제 balance 책정)
            if(callSettlement.getDailyTotalRiding() > 300){
                // 누적운행시간이 300분 초과일 경우.
                balance = 0;
            }else{
                // 해당 call로 인해 누적운행시간이 300분 초과가 됐을 경우.
                if((callSettlement.getDailyTotalRiding()+minute) > 300) {
                    long dailyBalance = getDailyBalance(callSettlement, seller);

                    balance = (long) (seller.getFlat_rate() - (dailyBalance));

                    if(balance < 0) {
                        balance = 0;
                    }
                }
            }

            // seller의 balance를 차감
            seller.dischargeBalance(balance);

            // end를 두번눌러 보험금이 이중으로 출금되는 걸 방지
            if (callSettlement.getBalance() > 0) {
                seller.refundBalance(callSettlement.getBalance());
            }

            // DB callSettlement에 밸런스 등록
            log.info("차감액 ::  " + balance);
            log.info("운행시간(분) ::  " + minute);
            callSettlement.updateBalance(balance);
            callSettlement.updateTotalTime((int) minute);

            if(Objects.equals(dto.getSeller_code(), "1444432576f")) {
                // 고고라 라이더 포인트 실시간 차감
                PostGogoraPoint(balance, call.getRider().getDriver_id(), callSettlement.getGroupId(), callSettlement.getTotalTime());
            }
        }

        callRepository.save(call);

        // kb에 레트로핏으로 운행이 종료됬음을 전달한다.
        KbApi11Req request = new KbApi11Req(call);
        KBRetrofitConfig<KbApi11Req> KBRetrofitConfig = new KBRetrofitConfig<>();
        ResultDto body = KBRetrofitConfig.create(KbApi.class).kbApi11Retrofit(request).execute().body();

//         kb의 응답 값이 ok이면 보험 적용상태를 end로 변경한다.
        if(body != null && body.getResult().equals("ok")) rider.insuranceEnd();

        if(Objects.equals(dto.getSeller_code(), "1444432576f")) {
            return new DrivingEndRes(call.getCallCompleteTime(),insuranceStatusRes.getDriver_status(),insuranceStatusRes.getDriver_message());
        }
        else {
            return new DrivingEndRes(minute, (int) balance,call.getCallCompleteTime(),insuranceStatusRes.getDriver_status(),insuranceStatusRes.getDriver_message(),callSettlement);
        }
    }

    private static long getDailyBalance(CallSettlement callSettlement, Seller seller) {
        double dailyFirstBdFee = ((seller.getFirst_bd_fee() /60) * callSettlement.getDailyTotalRiding() * seller.getDiscount_rate()); // 대인 1
        double dailySecondBdFee = ((seller.getSecond_bd_fee()/60) * callSettlement.getDailyTotalRiding() * seller.getDiscount_rate()); // 대인 2
        double dailyPdFee = ((seller.getPd_fee()/60) * callSettlement.getDailyTotalRiding() * seller.getDiscount_rate()); // 대물

        long dailyBalance = (long) ((long) Math.floor(dailyFirstBdFee) + Math.floor(dailySecondBdFee) + Math.floor(dailyPdFee));
        return dailyBalance;
    }

    //고고라 라이더 포인트 실시간 차감
    public void PostGogoraPoint(Long balance, String driverId, String groupId, int runningTime) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("balance", balance);
        bodyMap.put("driverId", driverId);
        bodyMap.put("runningTime", runningTime);
        bodyMap.put("groupId", groupId);

        // webClient 기본 설정
        WebClient webClient =
                WebClient
                        .builder()
                        .baseUrl("https://gogora.co.kr:9500")
                        .build();

        // api 요청
        Map<String, Object> response =
                webClient
                        .post()
                        .uri("/api/goGoRa/web/point")
                        .bodyValue(bodyMap)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

        // 결과 확인
        log.info(response.toString());
    }

    private String padLeft(String s, int n) {
        return String.format("%0" + n + "d", Integer.parseInt(s));
    }

    public void updateCompleteTime(String callIds) {
        String apiURL = "https://apigateway-lastmile.vroong.com/gogofnd/v1/lapses?lapseIds=";
        String apiKey = "jJ42fiOsPGaqKPh8vesazgqM6xMBs3GAvBxfkilJOb4=";
        StringBuffer response = new StringBuffer();

        try {
            // 외부 API URL 생성 및 파라미터 추가
            String urlString = apiURL + callIds;
            log.info("요청 url : " + urlString);
            // URL 객체 생성
            URL url = new URL(urlString);

            // HttpURLConnection 객체 생성 및 설정
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("x-api-key", apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            // 응답 코드 확인
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
            }
            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            log.info("부릉 누락 건 response :: " + response);

            br.close();

            List<CallsEndRes> result = extractDriverEndDates(String.valueOf(response));

            result.forEach(c -> {
                callRepositorySupport.updateNullCompleteTime(c.getCall_id(), convertDate(c.getDriver_enddate())) ;

                log.info("누락 건 call_id :: " + c.getCall_id());
                log.info("누락 건 complete_time :: " + c.getDriver_enddate());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CallsEndRes> extractDriverEndDates(String jsonResponse) {

        List<CallsEndRes> callsEndRes = new ArrayList<>();

        try {
            JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {

                CallsEndRes result;

                JsonObject entry = jsonArray.get(i).getAsJsonObject();
                int lapseType = entry.get("lapseType").getAsInt();

                if (lapseType == 1) {
                    String requestContent = entry.get("requestContent").getAsString();
                    Gson gson = new Gson();

                    result = gson.fromJson(requestContent, CallsEndRes.class);

                    callsEndRes.add(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return callsEndRes;
    }

}


