package com.kun.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kun.model.user.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jiakun
 * @create 2023-03-12-17:14
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}
