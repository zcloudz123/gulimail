package com.gulimall.member.exception;

import com.gulimall.common.exception.BizCodeEnum;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-23:05
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super(BizCodeEnum.PHONE_UNIQUE_EXCEPTION.getMsg());
    }
}
