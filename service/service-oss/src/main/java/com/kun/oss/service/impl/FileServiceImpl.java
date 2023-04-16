package com.kun.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.kun.oss.service.FileService;
import com.kun.oss.utils.ConstantOssPropertiesUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author jiakun
 * @create 2023-03-12-0:01
 */
@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile file) {
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String secrect = ConstantOssPropertiesUtils.SECRECT;
        String bucket = ConstantOssPropertiesUtils.BUCKET;
        String endpoint = ConstantOssPropertiesUtils.EDNPOINT;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, secrect);
        String url = null;
        try {
            InputStream inputStream = file.getInputStream();
            String originalName = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString().replaceAll("-","")+originalName;

            ossClient.putObject(bucket,fileName,inputStream);

            ossClient.shutdown();

            url = "https://"+bucket+"."+endpoint+"/"+fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        return url;
    }
}
