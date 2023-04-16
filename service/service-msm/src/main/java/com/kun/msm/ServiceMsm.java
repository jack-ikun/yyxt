package com.kun.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author jiakun
 * @create 2023-03-07-0:31
 */
@ComponentScan({"com.kun"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceMsm {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMsm.class,args);
    }
}
