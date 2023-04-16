package com.kun.order.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author jiakun
 * @create 2023-03-13-20:23
 */
@Component
public class AliPayConstantPropertiesUtils implements InitializingBean {

    // @Value("${weixin.pay.appid}")
    @Value("${ali.pay.app-id}")
    private String appid;

    @Value("${ali.pay.alipay-public-key}")
    private String aliPublicKey;

    @Value("${ali.pay.charset}")
    private String charSet;

    @Value("${ali.pay.gateway-url}")
    private String gatewayUrl;

    @Value("${ali.pay.merchant-private-key}")
    private String merchantPrivateKey;

    @Value("${ali.pay.notify-url}")
    private String notifyUrl;

    @Value("${ali.pay.return-url}")
    private String returnUrl;

    @Value("${ali.pay.sign-type}")
    private String signType;



    // @Value("${weixin.pay.partner}")
   // private String partner;

    // @Value("${weixin.pay.partnerkey}")
    //private String partnerkey;



    public static String APPID;
    public static String ALIPUBLICKEY;
    public static String CHARSET;
    public static String GATEWAYURL;
    public static String MERCHANTPRIVATEKEY;
    public static String NOTIFYURL;
    public static String RETURNURL;
    public static String SIGNTYPE;
    //public static String PARTNER;
    //public static String PARTNERKEY;
   // public static String CERT;

    @Override
    public void afterPropertiesSet() throws Exception {
        APPID = appid;
        ALIPUBLICKEY = aliPublicKey;
        CHARSET = charSet;
        GATEWAYURL = gatewayUrl;
        MERCHANTPRIVATEKEY = merchantPrivateKey;
        NOTIFYURL = notifyUrl;
        RETURNURL = returnUrl;
        SIGNTYPE = signType;

        // PARTNER = partner;
        // PARTNERKEY = partnerkey;
        // CERT =  getClass().getResource("/").getPath()+"cert/apiclient_cert.p12";
        // System.out.println("-----------------------"+CERT);
    }
}

