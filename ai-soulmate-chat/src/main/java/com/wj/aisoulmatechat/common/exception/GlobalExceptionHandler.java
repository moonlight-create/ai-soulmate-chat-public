package com.wj.aisoulmatechat.common.exception;

import com.wj.aisoulmatechat.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //系统异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail("服务器内部错误，请稍后重试");
    }

    //校验异常
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleParamConstraint(ConstraintViolationException ex) {
        var violations = ex.getConstraintViolations();
        if (violations.isEmpty()) {
            return Result.fail("请求参数非法");
        }
        String msg = violations.iterator().next().getMessage();
        return Result.fail(msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleParamValidError(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errMsg = fieldError != null ? fieldError.getDefaultMessage() : "请求参数不能为空";
        return Result.fail(errMsg);
    }

}
