package com.gulimall.order.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-28-16:44
 */
@Configuration
public class AliPayConfig {

    @PostConstruct
    public void configFactory() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipaydev.com";
        config.signType = "RSA2";

        config.appId = "2016102200741429";

        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        config.merchantPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDhDyPiurYVqI98ziLrkvaw9picVtVLO0E+BqWTBJdhh+cR709AXlAG05VOOJa+rFMWZZdmSEHsiJNIHXcVRWxNKA7E92jbyprxWcrxpVb1oYwlvkNwG8vBy5sTbUy4FfaK30HxjQl1cpDsbMhf/bHlhSighTSA8H23riRKlixgLsyntZbp6oWmUNjfkxe6aXaCxzRvn/JRa3qXvKKVbtKS6dlvXuCFCAHNh5hrJgjjtzlxjxXtqMqYKZWH2PuWnYcQOQjrFL6fXY7EwNxDromGu6x8qC3Cd1nIeDKhvl+ADXtOx29aSrEZnQiLiCHpbhNOBCfBzUcvBIOBe7do33RAgMBAAECggEAHrzIPrA0BQ3ya1IuFA9PcTi3EKz3sKeIWdH2vMvYuvz+5FKr+tceVIpNO4PI+4R97Z8+km+YlajfdXExuqY0JK9tB4G0Gl9/5aJEY6nM/KLdx/txB/LCyZX/Fpbu0441Wisx1KBRTcpytHGSsTCsJ6d++SA354GzUpRpRJxGEG31Cd7AMobYyevDs0+7ACeLDv66DZi5ciYXTgQBMnRY6fraw5K0k8BRGJWUwuH38o0yd1u6sW/Hqz1lEIAaSJ9uE4ZNUzeeW6XyUVSnAeT2fijbM7ABYrHzSrOxfrJZb8lOYAijYfVa+tXvD9WYk3/e+WfxRFvOTEBXrw/4evo8sQKBgQC60Ru5L7HEFpw3rskFbs/dH/qM7C7icGMK13Fw3n9g0OUahzGELVu57P+mjnMxR1QpOb+MmUIrJ1CxEPAh1t7DeKT0bDrf1R2sreUXONIapP2jxWmacDgNunBZDWoVr/SS/SfkPCAYODeLa+afraH3LnacZQTO8EqgYumCIYwfVQKBgQC0OHeqAulbqqPrUF66x+EJ0RUsmL/qctDL0c6VRLkQtld6oLdAYLjrZNO4HS+PdxxIdmw8kZrXVBaOCHHyzsSKQpkQbs6zhZJc4D7qJ5/HC90bYb5FudYBq5t7XT9AILKidtGZw7nORRoz6OS0BtDjLXK9NY/3w3jQ0/swd4VMjQKBgEZNoSg8qaJIe/t8fEveGr11Zl2YGEDA2JTg5OD76mWt1zn3fIvoYeeDJ+YvH2yBEkv9XndzyXHh9rzbyfKCIQzgnvsYq8NGOUednWPyzyaVxpnWFdq3tnB8JAFvSeflcp3KRTPf6JgUt9pw/bZDqnsR8jMC1R9nUD4xWvzM0SWNAoGABkZ5FDhpu8FjV8xCoCpZkafwHs9qz1UUJCS4FsZ/0y8mOKTj/J/hkIDARobtowc4uuU0u58uTxUke+mbXiMJAO90uPfCxsl94TIFvs2GaL3nAw8SOwdPhltdXJxQmYEJ91I/1iTUkXQih3AylnouR9rLRf9Zdf0eddcLxhJDV/kCgYEAghxd6f6PyWwi4x3tJaOyb64So/cRIHplZxhcCJGMbUG4HOQsn8ek7RxBy8U8lLzCjt47tN53TfDlpucWJ3MrTtpqQWRhyIvNMdJvnDgtRjLdUQoDoCxS4oF2DF8it4eUEs51bX/oK57sst8cbab/y8ejRkXOEMmXrgumJMn0OMs=";

        //注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
//        config.merchantCertPath = "<-- 请填写您的应用公钥证书文件路径，例如：/foo/appCertPublicKey_2019051064521003.crt -->";
//        config.alipayCertPath = "<-- 请填写您的支付宝公钥证书文件路径，例如：/foo/alipayCertPublicKey_RSA2.crt -->";
//        config.alipayRootCertPath = "<-- 请填写您的支付宝根证书文件路径，例如：/foo/alipayRootCert.crt -->";

        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
        config.alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz5Ihy6IOAcLJAgXjcF5e1RSpUTS+q+WIY1/0FpSCJ5/ISUsnPMI/Whi1X0BK015Pdco/kFfZxHH7VrpLmNaRExEUHFJlGhUvoFmFHJfA28qmK1sr+xkOf+1cdbfTzYFtklX1ifjiFnRlQBBEi/lC0hdbvGmg4hUt1tWfoPeWcbPTa0CyCEY40gdN78LHvRgdv4hTs0Jar4I2bpe+zSaVsj9ia8xwamwMUBWQYF+HnJszM4jyMpZRtvWMoSdaqKzIhbO2xvBKLXDzsjfnDFzMlJ6+NzD/Wu/h/51W/rHIUYVTwFK508iWfAGqTJhFqe772FmEv+y6ejcc72pqwnDMIQIDAQAB";

        //可设置异步通知接收服务地址（可选）
        config.notifyUrl = "http://jbf5nb.natappfree.cc/payed/notify";

        //可设置AES密钥，调用AES加解密相关接口时需要（可选）
//        config.encryptKey = "<-- 请填写您的AES密钥，例如：aa4BtZ4tspm2wnXLb1ThQA== -->";
        Factory.setOptions(config);
    }
}
