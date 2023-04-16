package com.kun.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.models.*;
import com.aliyun.teautil.models.*;
import com.kun.msm.service.MsmService;
import com.kun.msm.utils.ConstantPropertiesUtils;
import com.kun.vo.msm.MsmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author jiakun
 * @create 2022-05-23-11:44
 */
@Service
public class MsmServiceImpl implements MsmService {
    //发送短信验证码方法
    @Override
    public boolean send(Map<String, Object> param, String phone) throws Exception {
        if(StringUtils.isEmpty(phone)) return false;
        //阿里云短信发送测试案例
        Config config = new Config();
                // 您的AccessKey ID
                config.setAccessKeyId(ConstantPropertiesUtils.ACCESS_KEY_ID);
                // 您的AccessKey Secret
                config.setAccessKeySecret(ConstantPropertiesUtils.SECRECT);
                // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        Client client = new Client(config);
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName("阿里云短信测试")//签名
                .setTemplateCode("SMS_154950909")//模板
                .setPhoneNumbers(phone)//手机号
                .setTemplateParam(JSONObject.toJSONString(param));//验证码 发送内容
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            //最终发送
            client.sendSmsWithOptions(sendSmsRequest, runtime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //短信通知
    @Override
    public boolean sendNotice(MsmVo msmVo) throws Exception {
        if(!StringUtils.isEmpty(msmVo.getPhone())) {
            return this.send(msmVo.getParam(),msmVo.getPhone());
        }

        return false;
    }

}
