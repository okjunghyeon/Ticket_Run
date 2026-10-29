package com.whattoeat.domain.member.exception;

import com.whattoeat.common.exception.CustomException;

public class MemberException extends CustomException {

    public MemberException(MemberExceptionCode errorCode) {
        super(errorCode);
    }
}
