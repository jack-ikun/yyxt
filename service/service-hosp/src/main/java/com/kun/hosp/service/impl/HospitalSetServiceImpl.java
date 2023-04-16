package com.kun.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.common.exception.MyYyxtException;
import com.kun.common.result.ResultCodeEnum;
import com.kun.hosp.mapper.HospitalSetMapper;
import com.kun.model.hosp.HospitalSet;
import com.kun.hosp.service.HospitalSetService;
import com.kun.vo.order.SignInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jiakun
 * @create 2023-02-05-13:54
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper,HospitalSet> implements HospitalSetService {
    @Autowired
    private HospitalSetMapper hospitalSetMapper;


    @Override
    public String getSignKey(String hoscode) {
        //根据医院编码获取医院设置，方法封装
        HospitalSet hospitalSet = this.getByHoscode(hoscode);
        if(hospitalSet == null){
            throw  new MyYyxtException(ResultCodeEnum.HOSPITAL_OPEN);//医院系统未开放
        }
        if(hospitalSet.getStatus() == 0){
            throw new MyYyxtException(ResultCodeEnum.HOSPITAL_LOCK);//医院系统被锁定
        }
        return hospitalSet.getSignKey();//返回医院设置里的签名
    }

    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(null == hospitalSet) {
            throw new MyYyxtException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;

    }

    private HospitalSet getByHoscode(String hoscode) {
        HospitalSet hospitalSet = hospitalSetMapper.selectOne(new QueryWrapper<HospitalSet>().eq("hoscode", hoscode));

        return hospitalSet;
    }
}
