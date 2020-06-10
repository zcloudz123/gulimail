package com.gulimall.product.exception;

import com.gulimall.common.exception.BizCodeEnum;
import com.gulimall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-10-18:22
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.gulimall.product.controller")
public class GuliMallExceptionAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{},异常类型:{}",e.getMessage(),e.getClass());
        Map<String, String> map = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((item)->{
            String field = item.getField();
            String message = item.getDefaultMessage();
            map.put(field,message);
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),
                BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data",map);
    }

    @ExceptionHandler(value = {Exception.class})
    public R handleException(Exception e){
        log.error("数据校验出现问题{},异常类型:{}",e.getMessage(),e.getClass());
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),
                BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
