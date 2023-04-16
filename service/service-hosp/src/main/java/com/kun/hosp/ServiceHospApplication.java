package com.kun.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author jiakun
 * @create 2023-02-05-13:52
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.kun")
@EnableCaching
@EnableFeignClients(basePackages = "com.kun")
@MapperScan("com.kun.hosp.mapper")
public class ServiceHospApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class,args);
    }
}
