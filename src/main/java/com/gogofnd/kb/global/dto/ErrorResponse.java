package com.gogofnd.kb.global.dto;

import com.gogofnd.kb.global.dto.response.CE;
import com.gogofnd.kb.global.error.model.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.BindingResult;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

    private String server_message;
    private int server_status;
    private String code;


    public ErrorResponse(final ErrorCode code) {
        this.server_message = code.getMessage();
        this.server_status = code.getStatus();
        this.code = code.getCode();
    }
    public ErrorResponse(final ErrorCode code,String error) {
        this.server_message = code.getMessage();
        this.server_status = code.getStatus();
        this.code = code.getCode();
    }
    public ErrorResponse( CE code) {
        this.server_message = code.getMessage();
        this.server_status = code.getStatus();
        this.code = code.getCode();
    }

    public static ErrorResponse of(final ErrorCode code) {
        return new ErrorResponse(code);
    }

    public static ErrorResponse of(final ErrorCode code,String error) {
        return new ErrorResponse(code,error);
    }
    
    public static ErrorResponse of(MethodArgumentTypeMismatchException e) {
        final String value = e.getValue() == null ? "" : e.getValue().toString();
        return new ErrorResponse(ErrorCode.INVALID_TYPE_VALUE, "BAD_REQUEST");
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        private FieldError(final String field, final String value, final String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        public static List<FieldError> of(final String field, final String value, final String reason) {
            List<FieldError> fieldErrors = new ArrayList<>();
            fieldErrors.add(new FieldError(field, value, reason));
            return fieldErrors;
        }

        private static List<FieldError> of(final BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }
}
