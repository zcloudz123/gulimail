package com.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.member.entity.MemberEntity;
import com.gulimall.member.exception.PhoneExistException;
import com.gulimall.member.exception.UserNameExistException;
import com.gulimall.member.vo.MemberRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 23:11:24
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo memberRegistVo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UserNameExistException;
}

