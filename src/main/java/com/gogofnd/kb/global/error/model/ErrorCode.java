package com.gogofnd.kb.global.error.model;

public enum ErrorCode {
    INVALID_INPUT_VALUE(400, "C001", "올바르지 않은 형식입니다."),
    METHOD_NOT_ALLOWED(405, "C002", "지원하지 않는 메소드입니다."),
    ENTITY_NOT_FOUND(400, "C003", "해당 엔티티를 찾을 수가 없습니다."),
    INTERNAL_SERVER_ERROR(500, "C004", "알 수 없는 에러 (서버 에러)","INTERNAL_SERVER_ERROR"),
    INVALID_TYPE_VALUE(400, "C005", "타입이 올바르지 않습니다."),
    INVALID_TYPE_VALUE2(400, "C006","하이픈이나 문자를 포함할 수 없습니다." ),
    INVALID_LENGTH_VALUE(400, "C007","유효하지 않은 길이입니다." ),
    DUPLICATE_MEMBER(400, "C008", "중복된 회원입니다."),
    ALREADY_REQUESTED(400, "C008", "이미 가입신청이 진행중입니다."),
    INVALID_REQUEST(400, "C009","현재 접근할 수 없는 상태입니다." ),
    INVALID_CANCEL_REQUEST(400, "C009","기명 취소 요청한 이용자가 아닙니다." ),
    HANDLE_ACCESS_DENIED(403, "C006", "권한이 없습니다."),
    HANDLE_INVALID_TOKEN(401, "C007", "토큰이 없거나 올바르지 않습니다."),

    NOT_MATCH_PASSWORD(509, "P001", "비밀번호가 일치하지 않습니다.","BAD_Request"),
    DRIVER_ERROR(509,"D001","보험이 유효하지 않습니다."),
    NOT_FOUND_USER(509, "L007", "존재하지 않는 회원입니다.","BAD_Request"),
    LOGIN_ID_OR_SELLERCODE_MISMATCH(509, "L007", "아이디 또는 운영사코드가 알맞지 않습니다.","BAD_Request"),
    INCORRECT_APPLICATION_NUMBER(509,"N001"," 보험 청약번호(혹은 설계번호) 중 일부/전체가 올바르지 않습니다."),
    UNDERWRITING_NEEDED(509,"N002","언더라이팅을 아직 진행하지 않았거나, 다시 진행해야 합니다."),
    INSURE_NEEDED(509,"N003","KB손해보험에 유효한 이륜차보험이 확인되지 않음"),
    REJECTED_NO_MODEL(509,"N004","이륜차 가입가능 차종이 아닙니다."),
    REJECTED_NO_USE(509,"N005","이륜차 가입가능 운행요도 아닙니다."),
    REJECTED_TOO_MANY_RIDER(509,"N006","21~24세 가입자가 많습니다."),
    No_MONEY(509,"N007","예납금이 부족합니다. 충전 부탁드립니다."),
    UNREADY_INSURANCE(509,"N008","보험 가입해주세요."),
    INVALID_DRIVER_VCNUM(501,"N009","차량번호 확인해주세요."),
    USED_INSURANCE(509,"N010","이미 보험이 적용중입니다."),
    REJECT_AGE_INSURANCE(509,"N010","만 21세부터 23세는 센서가 있어야 합니다."),
    ENDTIME_REJECTED(511,"N011","완료시간은 시작시간보다 빠를 수 없습니다."),
    COMPLETE_REJECTED(511,"N012","완료되지 않은 시간제 보험이 있습니다."),
    ALREADY_COMPLETE(511,"N013","이미 종료된 보험입니다"),
    DUPE_CALL_ID(511,"N14","중복된 call_id 입니다."),
    INCORRECT_INSURE(600, "M001", "올바르지 않은 보험입니다.")
    ;


    private final int status;
    private final String code;

    //완료되지 않은 요청을 위해서 final 수정
    private String message;

    private final String error;
    ErrorCode(final int status, final String code, final String message, String error) {
        this.status = status;
        this.message = message +" "+ error;
        this.code = code;
        this.error = error;
    }
    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.error = "";
    }
    public String getMessage() {
        return this.message;
    }
    public void setMessage(String message) {this.message = this.message.split("/")[0] +"/"+ message; }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
    public String  getError() {
        return error;
    }

}
