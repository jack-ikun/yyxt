package com.kun.common.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 图片进行Base64编码
 * @author jiakun
 * @create 2023-03-02-17:18
 */
public class ImageBase64Util {
    public static void main(String[] args) {
        String imageFile= "C:\\Users\\qinkk\\Desktop\\相册\\logo\\nfyy.png";// 待处理的图片
        System.out.println(getImageString(imageFile));
    }

    public static String getImageString(String imageFile){
        InputStream is = null;
        try {
            byte[] data = null;
            is = new FileInputStream(new File(imageFile));
            data = new byte[is.available()];
            is.read(data);
            return new String(Base64.encodeBase64(data));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                    is = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

}
