package com.kun.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kun.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author jiakun
 * @create 2023-02-25-21:47
 */
public interface DictService extends IService<Dict> {
    //根据id查询子数据列表
    List<Dict> findChildData(Long id);

    /**
     * 导出 == 下载
     * @param response
     */
    void exportData(HttpServletResponse response);

    /**
     * 导入
     * @param file
     */
    void importDictData(MultipartFile file);

    /**
     * 根据上级编码与值获取数据字典名称
     * @param parentDictCode
     * @param value
     * @return
     */
    String getNameByParentDictCodeAndValue(String parentDictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}
