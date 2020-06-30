package com.gulimall.gulimallseckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-29-18:55
 */
@Component
@Slf4j
public class HelloSchedule {

//    @Async
//    @Scheduled(cron = "* * * ? * 1")
//    public void Hello() throws InterruptedException {
//      log.info("print hello...");
//      Thread.sleep(3000);
//    }
}
