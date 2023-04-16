package com.kun.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author jiakun
 * @create 2023-03-06-22:45
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.kun")
public class UserService {

    public static void main(String[] args) {
        SpringApplication.run(UserService.class,args);
    }
}
