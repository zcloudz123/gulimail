package com.gulimall.coupon.service.impl;

import com.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gulimall.common.utils.PageUtils;
import com.gulimall.common.utils.Query;

import com.gulimall.coupon.dao.SeckillSessionDao;
import com.gulimall.coupon.entity.SeckillSessionEntity;
import com.gulimall.coupon.service.SeckillSessionService;
import org.springframework.util.CollectionUtils;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        //计算时间区间
        List<SeckillSessionEntity> sessions = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));

        if (!CollectionUtils.isEmpty(sessions)) {
            sessions.forEach(seckillSessionEntity -> seckillSessionEntity.setSkuRelationEntities(seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>()
                    .eq("promotion_session_id", seckillSessionEntity.getId()))));
        }

        return sessions;
    }

    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;

        LocalDateTime start = LocalDateTime.of(now, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getEndTime() {
        LocalDate plus2 = LocalDate.now().plusDays(2);
        LocalTime max = LocalTime.MAX;

        LocalDateTime end = LocalDateTime.of(plus2, max);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}