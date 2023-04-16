package com.kun.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kun.cmn.mapper.DictMapper;
import com.kun.cmn.listener.DictListener;
import com.kun.cmn.service.DictService;
import com.kun.model.cmn.Dict;
import com.kun.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiakun
 * @create 2023-02-25-21:48
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService  {

    @Autowired
    DictMapper dictMapper;


    // @Cacheable(value="dict",key = "#root.methodName+'-'+#id")
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    @Override
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        //判断其子结点是否有子结点
        for (Dict dict : dictList) {
            Long dictId = dict.getId();
            boolean hasChildren = isChildren(dictId);
            dict.setHasChildren(hasChildren);
        }

        return dictList;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        //设置响应类型
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition","attachment;filename="+fileName+".xlsx");
            List<DictEeVo> dictEeVos= new ArrayList<>();
            List<Dict> dicts = dictMapper.selectList(null);
            for (Dict dict : dicts) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictEeVo,DictEeVo.class);
                dictEeVos.add(dictEeVo);
            }
            //easyexcel封装的导出
            EasyExcel.write(response.getOutputStream(),DictEeVo.class).sheet("数据字典").doWrite(dictEeVos);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @CacheEvict(value = "dict", allEntries=true)
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(dictMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        if(StringUtils.isEmpty(parentDictCode)){
            Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if(dict != null){
                return dict.getName();
            }
        }else {
            Dict parentDict = this.getByDictsCode(parentDictCode);

            if(parentDict == null)return "";

            Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id", parentDict.getId()).eq("value", value));
            if(dict!=null){
                return dict.getName();
            }
        }
        return "";

    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict dict = this.getByDictsCode(dictCode);
        if(null == dict) return null;

        return this.findChildData(dict.getId());
    }

    private Dict getByDictsCode(String parentDictCode) {
        Dict parentDict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", parentDictCode));

        return parentDict;
    }

    //判断id下面是否有子结点
    private boolean isChildren(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count>0;
    }


}
