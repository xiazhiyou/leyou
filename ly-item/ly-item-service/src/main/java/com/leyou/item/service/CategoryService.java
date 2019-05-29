package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


   // 根据pid查询商品种类
    public List<Category> queryCategoryListByPid(Long pid) {
        //查询条件，mapper会把对象中的非空属性作为查询条件
        Category t = new Category();
        t.setParentId(pid);
        List<Category> list = categoryMapper.select(t);

        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        return list;
    }

    // 根据商品分类cid列表查询分类集合
    public List<Category> queryByIds(List<Long> cids){

        List<Category> idList = categoryMapper.selectByIdList(cids);

        if(CollectionUtils.isEmpty(idList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        return idList;
    }
}
