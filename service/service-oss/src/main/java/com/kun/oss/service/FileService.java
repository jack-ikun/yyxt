package com.kun.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author jiakun
 * @create 2023-03-12-0:01
 */
public interface FileService {
    //上传文件到阿里云oss
    String upload(MultipartFile file);
}