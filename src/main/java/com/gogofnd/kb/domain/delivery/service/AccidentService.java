package com.gogofnd.kb.domain.delivery.service;

import com.gogofnd.kb.domain.delivery.dto.accident.AccidentCreate;
import com.gogofnd.kb.domain.delivery.dto.accident.AccidentDetailRes;
import com.gogofnd.kb.domain.delivery.dto.accident.AccidentRes;
import com.gogofnd.kb.domain.delivery.dto.insure.res.DeliveryInsureAccidentResponseDto;
import com.gogofnd.kb.domain.delivery.repository.AccidentRepository;
import com.gogofnd.kb.domain.delivery.repository.AccidentRepositorySupport;
import com.gogofnd.kb.domain.delivery.repository.CallRepositorySupport;
import com.gogofnd.kb.domain.delivery.repository.CallRepository;
import com.gogofnd.kb.domain.delivery.entity.Accident;
import com.gogofnd.kb.domain.seller.entity.Call;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccidentService {
    private final AccidentRepository accidentRepository;
    private final CallRepositorySupport callRepositorySupport;
    private final AccidentRepositorySupport accidentRepositorySupport;


    //라이더 전화번호로 사고 정보 리턴
    public AccidentRes findAccidentsByPhone(String phone){
        //라이더 사고 조회
        List<Accident> accidents = accidentRepositorySupport.findAccidentsByPhone(phone);

        // dto로 변환
        List<AccidentDetailRes> accidentDetailResList = accidents.stream()
                .map(a -> new AccidentDetailRes(a))
                .collect(Collectors.toList());

        Integer totalCount = accidentRepositorySupport.findAccidentsCountByPhone(phone);

        // 변환한 dto + 카운트 추가해서 dto 반환
        return new AccidentRes(totalCount,accidentDetailResList);
    }

    public List<DeliveryInsureAccidentResponseDto> findAccidents(AccidentCreate dto){
        //콜아이디로 콜 조회
        Call call = callRepositorySupport.findByKbCallId(dto.getCall_id());

        //사고 접수 값 db에 저장
        Accident accident = Accident.create(dto, call);
        accidentRepository.save(accident);

        // kb에서 리턴해줘야할 추가정보들 dto에 추가해서 list로 kb에 반환
        List<DeliveryInsureAccidentResponseDto> calls = callRepositorySupport.findCallsByAppointTime(call);
        log.info("=============================== 사고접수 api호출 ===================================");
        calls.forEach(System.out::println);
        return calls;
    }
}
