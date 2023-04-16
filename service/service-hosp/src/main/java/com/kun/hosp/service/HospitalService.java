package com.kun.hosp.service;

import com.kun.model.hosp.Hospital;
import com.kun.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-02-14:42
 */
public interface HospitalService {

    /**
     * 上传医院信息
     *
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    /**
     * 查询医院
     *
     * @param hoscode
     * @return
     */
    Hospital getByHoscode(String hoscode);

    /**
     * 医院管理：分页条件查询
     *
     * @param page
     * @param limit
     * @param hospitalQueryVo
     * @return
     */
    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    /**
     * 更新医院状态：上线1 下线0
     */
    void updateStatus(String id, Integer status);

    /**
     * 后台
     * 医院详情
     *
     * @param id
     * @return
     */
    Map<String, Object> show(String id);

    /**
     * 根据医院名称获取医院列表 模糊查询
     */
    List<Hospital> findByHosname(String hosname);

    /**
     * 网页
     * 医院详情
     *
     * @param hoscode
     * @return
     */
    Map<String, Object> showByhoscode(String hoscode);

    String getHospName(String hoscode);
}
