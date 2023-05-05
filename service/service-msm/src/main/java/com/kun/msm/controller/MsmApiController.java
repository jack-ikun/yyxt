package com.kun.msm.controller;

import com.alibaba.excel.util.StringUtils;
import com.kun.common.result.Result;
import com.kun.common.utils.RandomUtil;
import com.kun.msm.service.MsmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jiakun
 * @create 2023-03-07-1:07
 */
@RestController

public class MsmApiController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * @description  发送手机验证码
     * @param phone
     * @return
     * @throws
     * @time: 2023/4/16 15:51
     */
    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable String phone)  {
        //从redis获取验证码，如果获取获取到，返回ok
        // key 手机号  value 验证码
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)) {

            return Result.ok();
        }
        //如果从redis获取不到，
        // 生成验证码，
        code = RandomUtil.getSixBitRandom();
        //调用service方法，通过整合短信服务进行发送
        Map<String, Object> param = new HashMap<>(10);
        param.put("code",code);
        boolean isSend = false;
        try {
            isSend = msmService.send(param,phone);
        } catch (Exception e) {
            return Result.fail().message("发送短信失败");
        }
        //生成验证码放到redis里面，设置有效时间
        if(isSend) {
            redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);
            return Result.ok();
        } else {
            return Result.fail().message("发送短信失败");
        }
    }


}
