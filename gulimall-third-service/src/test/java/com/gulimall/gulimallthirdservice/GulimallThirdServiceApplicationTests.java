package com.gulimall.gulimallthirdservice;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.PutObjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class GulimallThirdServiceApplicationTests {

    @Autowired
    OSSClient ossClient;

    @Test
    void testUpload() {
        ossClient.putObject(new PutObjectRequest("gulimall-zyy", "1234.jpg",new File("E://JAVA学习教程/谷粒商城/分布式基础篇/docs/pics/23cd65077f12f7f5.jpg")));
    }

    @Test
    void contextLoads() {
    }

}
