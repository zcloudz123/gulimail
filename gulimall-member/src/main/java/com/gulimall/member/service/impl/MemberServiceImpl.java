package com.gulimall.member.service.impl;

import com.gulimall.member.entity.MemberLevelEntity;
import com.gulimall.member.exception.PhoneExistException;
import com.gulimall.member.exception.UserNameExistException;
import com.gulimall.member.service.MemberLevelService;
import com.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.member.dao.MemberDao;
import com.gulimall.member.entity.MemberEntity;
import com.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo memberRegistVo) {
        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity memberLevelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());
        //检查用户名和手机号是否唯一,通过异常上报
        this.checkUserNameUnique(memberRegistVo.getUserName());
        this.checkPhoneUnique(memberRegistVo.getPhone());

        memberEntity.setUsername(memberRegistVo.getUserName());
        memberEntity.setMobile(memberRegistVo.getPhone());

        //密码需要加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(bCryptPasswordEncoder.encode(memberRegistVo.getPassword()));

        memberEntity.setCreateTime(new Date());
        this.save(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        if (this.count(new QueryWrapper<MemberEntity>().eq("mobile", phone)) > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        if (this.count(new QueryWrapper<MemberEntity>().eq("username", userName)) > 0) {
            throw new UserNameExistException();
        }
    }

}