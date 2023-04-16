package com.kun.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.kun.cmn.mapper.DictMapper;
import com.kun.model.cmn.Dict;
import com.kun.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

/**
 * 监听导入的表格每一行进行读取
 * @tip easyexcel的监听器不交给spring管理
 * @author jiakun
 * @create 2023-02-25-23:13
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {
    private DictMapper dictMapper;

    //构造器注入
    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //一行一行插入
        Dict dict = new Dict();

        BeanUtils.copyProperties(dictEeVo,dict);

        dictMapper.insert(dict);
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
