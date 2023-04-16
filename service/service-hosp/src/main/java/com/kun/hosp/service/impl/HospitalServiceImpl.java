package com.kun.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.kun.client.DictFeignClient;
import com.kun.enums.DictEnum;
import com.kun.model.hosp.Hospital;
import com.kun.hosp.repository.HospitalRepository;
import com.kun.hosp.service.HospitalService;
import com.kun.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医院业务操作mongo数据库
 * @author jiakun
 * @create 2023-03-02-14:43
 */
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {
        //map->Jsonstring->Object
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Hospital.class);
        //判断医院是否已经存在->是修改->否增加
        Hospital hospitalOld = getByHoscode(hospital.getHoscode());
        if(hospitalOld != null){
            //修改
            if(null == hospital.getStatus()){
                hospital.setStatus(0);
            }
            hospital.setId(hospitalOld.getId());
            hospital.setCreateTime(hospitalOld.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else {
            //新增
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }

    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {

        //查询参数转换
        Hospital hospital = new Hospital();
        if(hospitalQueryVo != null){
            BeanUtils.copyProperties(hospitalQueryVo,hospital);
        }
        //创建匹配器 ---忽略大小写+模糊匹配
        ExampleMatcher matching = ExampleMatcher.matching()
                .withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Hospital> e = Example.of(hospital, matching);
        //分页
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pages = PageRequest.of(page - 1, limit, sort);
        //stream流从新封装内容
        Page<Hospital> result = hospitalRepository.findAll(e, pages);
        result.getContent().stream().forEach(item->{
            this.packageHospital(item);
        });

        return result;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if(status == 0 || status ==1){
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Map<String, Object> show(String id) {

        Map<String, Object> map = new HashMap<>();
        Hospital hospital = hospitalRepository.findById(id).get();
        Hospital hospital1 = this.packageHospital(hospital);
        map.put("hospital",hospital1);
        map.put("bookingRule",hospital.getBookingRule());


        return map;
    }

    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    @Override
    public Map<String, Object> showByhoscode(String hoscode) {
        Map<String, Object> map = new HashMap<>();
        Hospital hospital = this.packageHospital(this.getByHoscode(hoscode));
        map.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        map.put("hospital",hospital);
        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(null == hospital){
            return "";
        }
        return hospital.getHosname();
    }

    private Hospital packageHospital(Hospital hospital) {

        //获取DictCode为"Hostype"下的医院等级名称
        String gradeName = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hospital.getHostype());

        //获取省
        String provinceName = dictFeignClient.getName(hospital.getCityCode());
        //获取城市
        String cityName = dictFeignClient.getName(hospital.getCityCode());
        //地区
        String regionName = dictFeignClient.getName(hospital.getDistrictCode());

        //封装到map
        hospital.getParam().put("hostypeString",gradeName);
        hospital.getParam().put("fullAddress",provinceName+cityName+regionName+hospital.getAddress());
        return hospital;

    }
}
