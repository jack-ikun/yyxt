package com.kun.msm.service;

import com.kun.vo.msm.MsmVo;

import java.util.Map;

/**
 * @author jiakun
 * @create 2022-05-23-11:44
 */
public interface MsmService {
    boolean send(Map<String, Object> param, String phone) throws Exception;

    boolean sendNotice(MsmVo msmVo) throws Exception;
}
