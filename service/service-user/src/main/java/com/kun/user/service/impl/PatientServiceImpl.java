package com.kun.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.client.DictFeignClient;
import com.kun.user.mapper.PatientMapper;
import com.kun.user.service.PatientService;
import com.kun.enums.DictEnum;
import com.kun.model.user.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jiakun
 * @create 2023-03-12-17:13
 */
@Service
public class PatientServiceImpl extends  ServiceImpl<PatientMapper,Patient> implements PatientService  {

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAllUserId(Long userId) {

        //根据userid查询所有病历信息列表
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> patientList = baseMapper.selectList(wrapper);
        //通过远程调用，得到编码对应具体内容，查询数据字典表内容
        //其他参数封装
        patientList.forEach(this::packPatient);
        return patientList;
    }

    @Override
    public Patient getPatientById(Long id) {
        Patient patient = baseMapper.selectById(id);
        return this.packPatient(patient);
    }

    private Patient packPatient(Patient patient) {

        String certificatesTypeString = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());

        String contactsCertificatesTypeString = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getContactsCertificatesType());
        //省
        String provinceString = dictFeignClient.getName(patient.getProvinceCode());
        //市
        String cityString = dictFeignClient.getName(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getName(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;


    }

}
