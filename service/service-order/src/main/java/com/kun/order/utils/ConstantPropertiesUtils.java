package com.kun.order.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author jiakun
 * @create 2023-03-13-20:23
 */
@Component
public class ConstantPropertiesUtils implements InitializingBean {

    @Value("${weixin.pay.appid}")
    private String appid;


    @Value("${weixin.pay.partner}")
   private String partner;

    @Value("${weixin.pay.partnerkey}")
    private String partnerkey;



    public static String PARTNER;
    public static String APPID;
    public static String PARTNERKEY;
   public static String CERT;

    @Override
    public void afterPropertiesSet() throws Exception {
        APPID = appid;


        PARTNER = partner;
        PARTNERKEY = partnerkey;
        CERT =  getClass().getResource("/").getPath()+"cert/apiclient_cert.p12";
        System.out.println("-----------------------"+CERT);
    }
}

