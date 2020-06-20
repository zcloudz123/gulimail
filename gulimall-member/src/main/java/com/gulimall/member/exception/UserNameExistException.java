package com.gulimall.member.exception;

import com.gulimall.common.exception.BizCodeEnum;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-19-23:03
 */
public class UserNameExistException extends RuntimeException {

    public UserNameExistException() {
        super(BizCodeEnum.USERNAME_UNIQUE_EXCEPTION.getMsg());
    }
}
