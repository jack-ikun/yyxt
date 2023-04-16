package com.kun.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.model.hosp.HospitalSet;
import com.kun.vo.order.SignInfoVo;

/**
 * @author jiakun
 * @create 2023-02-05-13:54
 */
public interface HospitalSetService extends IService<HospitalSet> {

    String getSignKey(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
