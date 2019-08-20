package com.vc.door.web;

import com.vc.door.core.constant.BizErrorEnum;
import io.github.vincent0929.common.dto.ResultDTO;
import io.github.vincent0929.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResultDTO handleBizException(BizException e) {
        log.error(e.getMessage(), e);
        return ResultDTO.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResultDTO handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResultDTO.fail(BizErrorEnum.SYSTEM_ERROR.getCode(), BizErrorEnum.SYSTEM_ERROR.getDesc());
    }
}
