package com.kun.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.model.user.Patient;

import java.util.List;

/**
 * @author jiakun
 * @create 2023-03-12-17:12
 */
public interface PatientService extends IService<Patient> {
    //获取就诊人列表
    List<Patient> findAllUserId(Long userId);
    //根据id获取就诊人信息
    Patient getPatientById(Long id);


}
