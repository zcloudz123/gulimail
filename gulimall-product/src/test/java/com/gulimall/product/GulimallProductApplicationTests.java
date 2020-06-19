package com.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gulimall.product.entity.BrandEntity;
import com.gulimall.product.service.AttrGroupService;
import com.gulimall.product.service.BrandService;
import com.gulimall.product.service.CategoryService;
import com.gulimall.product.service.SkuSaleAttrValueService;
import com.gulimall.product.vo.ItemSaleAttrsVo;
import com.gulimall.product.vo.SkuItemVo;
import com.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;


    @Test
    void testSaleAttrs() {
        List<ItemSaleAttrsVo> saleAttrsBySpuId = skuSaleAttrValueService.getSaleAttrsBySpuId(4L);
        System.out.println(saleAttrsBySpuId);
    }

    @Test
    void testAttrGroup() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsByspuId = attrGroupService.getAttrGroupWithAttrsByspuId(400L, 225L);
        System.out.println(attrGroupWithAttrsByspuId);
    }

    @Test
    void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello" ,"world_" + UUID.randomUUID());
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    void contextLoads() {
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        for (BrandEntity brand :
                list) {
            System.out.println(brand);
        }
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setName("华为");
//        brandEntity.setDescript("华为");
//
//        brandService.save(brandEntity);
//        brandService.updateById(brandEntity);
    }

    @Test
    void testCategoryPath() {
        Long[] categoryPath = categoryService.findCategoryPath(225L);
        log.info("完整路径:" + Arrays.asList(categoryPath));

    }

    @Test
    void dellock() {
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del',KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
        stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),Arrays.asList("lock"),"a");
    }

    @Test
    void testRedisson() {
        System.out.println(redissonClient);
    }
}
