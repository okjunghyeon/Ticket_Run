package com.whattoeat.domain.place.exception;

import com.whattoeat.common.exception.CustomException;

public class PlaceException extends CustomException {

    public PlaceException(PlaceExceptionCode errorCode) {
        super(errorCode);
    }
}
