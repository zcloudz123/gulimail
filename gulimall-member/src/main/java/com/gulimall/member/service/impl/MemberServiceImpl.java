package com.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gulimall.common.utils.HttpUtils;
import com.gulimall.member.entity.MemberLevelEntity;
import com.gulimall.member.exception.PhoneExistException;
import com.gulimall.member.exception.UserNameExistException;
import com.gulimall.member.service.MemberLevelService;
import com.gulimall.member.vo.MemberLoginVo;
import com.gulimall.member.vo.MemberRegistVo;
import com.gulimall.member.vo.WeiBoSocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
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

        memberEntity.setNickname(memberRegistVo.getUserName());
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

    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        String loginAcct = memberLoginVo.getLoginAcct();
        MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginAcct)
                .or().eq("mobile", loginAcct));
        if (member == null) {
            return null;
        }
        String passworddb = member.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (bCryptPasswordEncoder.matches(memberLoginVo.getPassword(), passworddb)) {
            return member;
        } else {
            return null;
        }
    }

    /**
     * 集成登录和注册功能
     *
     * @param weiBoSocialUser
     * @return
     */
    @Override
    public MemberEntity login(WeiBoSocialUser weiBoSocialUser){
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", weiBoSocialUser.getUid()));
        if (memberEntity != null) {
            //用户已注册
            MemberEntity updataMem = new MemberEntity();
            updataMem.setId(memberEntity.getId());
            updataMem.setAccessToken(weiBoSocialUser.getAccess_token());
            updataMem.setExpiresIn(weiBoSocialUser.getExpires_in());
            this.updateById(updataMem);

            memberEntity.setAccessToken(weiBoSocialUser.getAccess_token());
            memberEntity.setExpiresIn(weiBoSocialUser.getExpires_in());
            return memberEntity;
        } else {
            //用户未注册，执行注册
            memberEntity = new MemberEntity();
            //查询社交用户的社交账号信息
            try {
                Map<String, String> querys = new HashMap<>();
                querys.put("access_token", weiBoSocialUser.getAccess_token());
                querys.put("uid", weiBoSocialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com",
                        "/2/users/show.json",
                        "GET",
                        new HashMap<>(),
                        querys);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseJSON = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(responseJSON);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    memberEntity.setNickname(name);
                    memberEntity.setGender("m".equals(gender) ? 0 : 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            MemberLevelEntity memberLevelEntity = memberLevelService.getDefaultLevel();
            memberEntity.setLevelId(memberLevelEntity.getId());
            memberEntity.setSocialUid(weiBoSocialUser.getUid());
            memberEntity.setAccessToken(weiBoSocialUser.getAccess_token());
            memberEntity.setExpiresIn(weiBoSocialUser.getExpires_in());
            memberEntity.setCreateTime(new Date());
            this.save(memberEntity);

            return memberEntity;
        }

    }

}