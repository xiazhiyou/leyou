package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    // 分页查询spu
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "key",required = false)String key
    ){

        return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));

    }

    //商品新增
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){ //json结构，加上@RequestBody注解
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();//没有返回值
    }


    //根据spu的id查询详情detail
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id")Long id){
        return ResponseEntity.ok(goodsService.queryDetailById(id));
    }

    //根据spu查询下面所有的sku
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long spuId){
        return ResponseEntity.ok(goodsService.querySkuBySpuId(spuId));
    }

    //商品修改
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //根据sku的id集合查询所有的sku
    @GetMapping("/sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }

    // 根据spu的id查询spu
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long spuId){
        return ResponseEntity.ok(goodsService.querySpuById(spuId));
    }

    //减库存
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> cartDTOS){
        goodsService.decreaseStock(cartDTOS);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
