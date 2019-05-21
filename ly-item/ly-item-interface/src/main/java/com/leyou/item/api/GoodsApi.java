package com.leyou.item.api;


import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {

    //根据spu的id查询详情detail
    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id")Long id);

    //根据spu查询下面所有的sku
    @GetMapping("/sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);

    //分页查询spu
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "key",required = false)String key
    );

    // 根据spu的id查询spu
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long spuId);

    @GetMapping("/sku/list/ids")
    List<Sku> querySkuByIds(@RequestParam("ids") List<Long> ids);

    // 减库存
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDTO> cartDTOS);
}
