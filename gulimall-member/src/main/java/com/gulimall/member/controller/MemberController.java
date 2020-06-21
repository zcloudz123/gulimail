package com.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.gulimall.common.exception.BizCodeEnum;
import com.gulimall.member.exception.PhoneExistException;
import com.gulimall.member.exception.UserNameExistException;
import com.gulimall.member.feign.CouponFeignService;
import com.gulimall.member.vo.MemberLoginVo;
import com.gulimall.member.vo.MemberRegistVo;
import com.gulimall.member.vo.WeiBoSocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gulimall.member.entity.MemberEntity;
import com.gulimall.member.service.MemberService;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.R;



/**
 * 会员
 *
 * @author zhangyangyang
 * @email sunlightcs@gmail.com
 * @date 2020-06-04 23:11:24
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo memberLoginVo){

        MemberEntity memberEntity = memberService.login(memberLoginVo);
        if(memberEntity != null){
            return R.ok().setData(memberEntity);
        }else{
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }

    }


    @PostMapping("/oauth/login")
    public R oauthlogin(@RequestBody WeiBoSocialUser weiBoSocialUser){

        MemberEntity memberEntity = memberService.login(weiBoSocialUser);
        if(memberEntity != null){
            return R.ok().setData(memberEntity);
        }else{
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }

    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegistVo){
        try {
            memberService.regist(memberRegistVo);
        } catch (PhoneExistException e) {
            R.error(BizCodeEnum.PHONE_UNIQUE_EXCEPTION.getCode(),BizCodeEnum.PHONE_UNIQUE_EXCEPTION.getMsg());
        } catch (UserNameExistException e){
            R.error(BizCodeEnum.USERNAME_UNIQUE_EXCEPTION.getCode(),BizCodeEnum.USERNAME_UNIQUE_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        return R.ok().put("member",memberEntity).put("coupons",couponFeignService.membercoupons().get("coupons"));
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
