package com.gulimall.product.web;

import com.gulimall.product.entity.CategoryEntity;
import com.gulimall.product.service.CategoryService;
import com.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-15-15:43
 */
@Controller
public class IndexController {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        //查出一级分类
//        long start = System.currentTimeMillis();
        List<CategoryEntity> category = categoryService.getLevel1Category();
//        long end = System.currentTimeMillis();
//        System.out.println("消耗的时间："+ (end - start));
        model.addAttribute("categories",category);

        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogJson(){

        return categoryService.getCatalogJson();
    }

    @ResponseBody
    @RequestMapping("/hello")
    public String hello() throws InterruptedException {

        RLock lock = redissonClient.getLock("my-lock");

        lock.lock();//阻塞式
        try {
            System.out.println("加锁成功，执行业务...");
            Thread.sleep(60000);
        } finally {
            lock.unlock();
        }

        return "hello";
    }

    @ResponseBody
    @RequestMapping("/write")
    public String write() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");

        RLock rLock = lock.writeLock();
        rLock.lock();
        String uuid = null;
        try {
            System.out.println("写锁加锁成功..");
            uuid = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",uuid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放..");
        }

        return uuid;
    }

    @ResponseBody
    @RequestMapping("/read")
    public String read() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");

        RLock rLock = lock.readLock();
        rLock.lock();
        String writeValue = "";
        try {
            System.out.println("读锁加锁成功..");
            Thread.sleep(30000);
            writeValue = redisTemplate.opsForValue().get("writeValue");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放..");
        }

        return writeValue;
    }

    @ResponseBody
    @RequestMapping("/park")
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");

        park.acquire();

        return "ok";
    }

    @ResponseBody
    @RequestMapping("/go")
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");

        park.release();

        return "ok";
    }

    @ResponseBody
    @RequestMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch peopleNum = redissonClient.getCountDownLatch("PeopleNum");

        peopleNum.await();

        return "锁门了";
    }

    @ResponseBody
    @RequestMapping("/leave")
    public String leave() {
        RCountDownLatch peopleNum = redissonClient.getCountDownLatch("PeopleNum");

        peopleNum.countDown();

        return "人走了1个";
    }
}
