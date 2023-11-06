package com.gogofnd.kb.business;

import com.gogofnd.kb.business.dto.req.AddHistoryReq;
import com.gogofnd.kb.business.dto.req.UpdateCsMemoReq;
import com.gogofnd.kb.business.dto.req.CallsListReq;
import com.gogofnd.kb.business.dto.res.CallsResultRes;
import com.gogofnd.kb.domain.rider.dto.req.RiderCsReq;
import com.gogofnd.kb.domain.rider.dto.res.RiderCsRes;
import com.gogofnd.kb.domain.rider.service.RiderService;
import com.gogofnd.kb.global.dto.request.MyPageRequest;
import com.gogofnd.kb.global.dto.response.ApiPagingResponse;
import com.gogofnd.kb.global.dto.response.ApiResponse;
import com.gogofnd.kb.global.provider.AES_Encryption;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

// CS 화면을 위한 controller
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/cs")
public class CsApi {

    private final RiderService riderService;

    @GetMapping("/insurance/list")
    @ApiOperation(value = "시간제 가입 상태 List", notes="시간제 가입 상태 List 조회")
    public ApiPagingResponse<RiderCsRes> RiderCsList (
            @ApiParam(value = "현재 페이지 default 1") @RequestParam(defaultValue = "1",required = false) int page,
            @ApiParam(value = "페이지 Limit default 10") @RequestParam(defaultValue = "10",required = false) int limit,
            @ApiParam(value = "검색조건 null 가능") @RequestParam(required = false,name = "searchField") String searchField,
            @ApiParam(value = "검색어 null 가능") @RequestParam(required = false,name = "keyword") String keyword,
            @ApiParam(value = "시작 날짜 null 가능") @RequestParam(required = false) String startDate,
            @ApiParam(value = "끝나는 날짜 null 가능") @RequestParam(required = false) String endDate
    ) throws Exception{
        return new ApiPagingResponse<>(riderService.getRiderCsList(new MyPageRequest(page,limit).of(),new RiderCsReq(keyword, searchField, startDate,endDate)));
    }

    @PutMapping("/update/memo")
    @ApiOperation(value = "시간제 라이더 메모", notes="시간제 가입 상태 List 화면에서 메모를 작성 할 수 있는 기능")
    public void updateMemo(@RequestBody UpdateCsMemoReq param) throws Exception {
        riderService.putCsMemo(param);
    }

    @GetMapping("/download/excel")
    @ApiOperation(value = "시간제 가입 상태 List 엑셀다운로드", notes="시간제 가입 상태 List를 일괄 엑셀다운로드 하는 기능")
    public ResponseEntity getUsersPointStats(
        HttpServletResponse response,
         @ApiParam(value = "검색조건 null 가능") @RequestParam(required = false,name = "searchField") String searchField,
         @ApiParam(value = "검색어 null 가능") @RequestParam(required = false,name = "keyword") String keyword,
         @ApiParam(value = "시작 날짜 null 가능") @RequestParam(required = false) String startDate,
        @ApiParam(value = "끝나는 날짜 null 가능") @RequestParam(required = false) String endDate,
        @ApiParam(value = "업체구분코드 null 가능") @RequestParam(required = false) String cmpcd
        ){
        return ResponseEntity.ok(riderService.outputCsListExcel(response, new RiderCsReq(keyword, searchField, startDate,endDate,cmpcd)));
    }

    @DeleteMapping("/rider/{riderId}")
    @ApiOperation(value="라이더 삭제")
    public ApiResponse<String> deleteRider(@PathVariable Long riderId){
        return new ApiResponse<>(riderService.deleteRider(riderId));
    }
    @GetMapping("/decodessn")
    @ApiOperation(value = "생년월일 복호화")
    public String getDecodeSsn(String ssn) throws Exception {

        return aesDecode(ssn);
    }

    @PostMapping("/add/history")
    @ApiOperation(value = "history 추가")
    public ApiResponse<String> addUnderwritingHistory(@RequestBody AddHistoryReq req) {

        return new ApiResponse<>(riderService.addHistory(req));
    }

    private String aesDecode(String str) throws Exception {
        AES_Encryption aes = new AES_Encryption();
        String decrypt = aes.decrypt(str);
        return decrypt;
    }
}
