package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("category")
//这个注解会将 HTTP 请求映射到 MVC 和 REST 控制器的处理方法上。
//就是控制方法的URL地址

public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 根据pid查询商品种类
    @GetMapping("list")
    //@GetMapping是一个组合注解，是@RequestMapping(method = RequestMethod.GET)的缩写。
    // ResponseEntity Entity是一个实体，可返回状态码,由于状态码在响应行里，因此不能用@ResponseBody注解（把Java对象序列化放到响应体里）
    public ResponseEntity<List<Category>> queryCategoryListByPid(@RequestParam("pid")Long pid){
        return ResponseEntity.ok(categoryService.queryCategoryListByPid(pid));
    }

    // 根据商品分类cid列表查询分类集合
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }

    /**
     * 根据bid（品牌id）查询商品的信息
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByBid(@PathVariable("bid")Long bid){
        List<Category> list = this.categoryService.queryCategoryListByPid(bid);
        if (list==null||list.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }


}
