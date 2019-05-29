package com.leyou.item.web;

import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.SpecificationService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    @Autowired
    private BrandService brandService;

    // 根据分类id查询规格组
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>>  queryGroupByCid(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }

    // 查询规格参数集合
    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParams(@RequestParam(value = "gid",required = false)Long gid,
                                                           @RequestParam(value = "cid",required = false)Long cid,
                                                           @RequestParam(value = "searching",required = false)Boolean searching){
        return ResponseEntity.ok(specificationService.querySpecParams(gid,cid,searching));
    }

    //根据cid查询规格组及其规格参数
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryListByCid(cid));
    }

}
