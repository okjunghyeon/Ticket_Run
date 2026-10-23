package com.ticketrun.domain.member.exception;

import com.ticketrun.common.exception.CustomException;

public class MemberException extends CustomException {

    public MemberException(MemberExceptionCode errorCode) {
        super(errorCode);
    }
}
