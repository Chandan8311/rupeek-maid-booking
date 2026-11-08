package com.rupeek.maidbooking.exception;

import com.rupeek.maidbooking.constants.ErrorCode;
import lombok.Getter;

public class BusinessException extends RuntimeException {
    @Getter
    private final ErrorCode code;
    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }
}
