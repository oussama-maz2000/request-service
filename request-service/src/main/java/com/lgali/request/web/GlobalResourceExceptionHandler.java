package com.lgali.request.web;

import com.lgali.common.exception.GlobalException;
import com.lgali.common.web.ResourceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalResourceExceptionHandler {
    @ExceptionHandler({GlobalException.class})
    public ResponseEntity<ResourceResponse<Object>> handleCacheException(final GlobalException exception) {
        final ResourceResponse<Object> accountResponse = ResourceResponse.builder().errorMessage(exception.getMessage()).
                status(HttpStatus.INTERNAL_SERVER_ERROR.name()).build();
        return ResponseEntity.status(HttpStatus.OK).body(accountResponse);
    }
}
