package com.kun.client;

import com.kun.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author jiakun
 * @create 2023-03-13-16:27
 */
@FeignClient(value = "service-user")
@Repository
public interface PatientFeignClient {

    //获取就诊人
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatientById(@PathVariable("id") Long id);

}
