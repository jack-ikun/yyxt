package com.kun.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.kun.model.hosp.Department;
import com.kun.hosp.repository.DepartmentRepository;
import com.kun.hosp.service.DepartmentService;
import com.kun.vo.hosp.DepartmentQueryVo;
import com.kun.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科室业务操作mongo数据库
 * @author jiakun
 * @create 2023-03-02-18:01
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;


    @Override
    public void save(Map<String, Object> paramMap) {
        //map->Jsonstring->Object
        Department department = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Department.class);

        //判断科室是否已经存在->是修改->否增加
        Department departmentOld = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        if(departmentOld != null){
            //修改
            department.setId(departmentOld.getId());
            department.setCreateTime(departmentOld.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else {
            //新增
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public  Page<Department>  selectPage(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo) {
        //查询参数转换
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);

        //创建匹配器 ---忽略大小写+模糊匹配
        ExampleMatcher matching = ExampleMatcher.matching()
                .withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Department> e = Example.of(department, matching);
        //分页
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pages = PageRequest.of(page - 1, limit, sort);

        return departmentRepository.findAll(e, pages);
    }

    @Override
    public void remove(String hoscode, String depcode) {
        //先根据参数查询
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department != null){
            //根据id删除
            departmentRepository.deleteById(department.getId());
        }


    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //封装最终数据集合
        List<DepartmentVo> result = new ArrayList<>();

        Department department = new Department();
        department.setHoscode(hoscode);

        List<Department> all = departmentRepository.findAll(Example.of(department));

        //根据大科室编号分组
        Map<String, List<Department>> collectMap = all.stream().collect(Collectors.groupingBy(Department::getBigcode));

        for(Map.Entry<String,List<Department>> entry : collectMap.entrySet()){
            //大科室编号
            String bigCode = entry.getKey();
            //所有科室
            List<Department> departments = entry.getValue();
            //封装每个大科室编号下的大科室
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(bigCode);//大科室的科室编号 为大科室编号
            departmentVo.setDepname(departments.get(0).getBigname());//大科室的名字 为大科室名字

            //封装其他小科室
            List<DepartmentVo> childrens = new ArrayList<>();
            for (int i = 0; i < departments.size(); i++) {
                DepartmentVo departmentVo1 = new DepartmentVo();
                departmentVo1.setDepname(departments.get(i).getDepname());
                departmentVo1.setDepcode(departments.get(i).getDepcode());
                childrens.add(departmentVo1);
            }
            departmentVo.setChildren(childrens);
            //最终放到result
            result.add(departmentVo);
        }
        return result;
    }

    @Override
    public String getDeptName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department != null){
            return department.getDepname();
        }
        return "";
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
