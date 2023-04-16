package com.kun.hosp.service;

import com.kun.model.hosp.Department;
import com.kun.vo.hosp.DepartmentQueryVo;
import com.kun.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author jiakun
 * @create 2023-03-02-18:01
 */
public interface DepartmentService {
    /**
     * 上传科室信息
     * @param paramMap
     */
    void save(Map<String,Object> paramMap);

    /**
     * 分页条件查询
     * @param page
     * @param limit
     * @param departmentQueryVo
     * @return
     */
    Page<Department> selectPage(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo);

    /**
     * 根据hoscode和depcode删除科室
     * @param hoscode
     * @param depcode
     */
    void remove(String hoscode,String depcode);

    //根据医院编号，查询医院所有科室列表
    List<DepartmentVo> findDeptTree(String hoscode);

    String getDeptName(String hoscode, String depcode);

    /**
     * 获取部门
     */
    Department getDepartment(String hoscode, String depcode);
}
