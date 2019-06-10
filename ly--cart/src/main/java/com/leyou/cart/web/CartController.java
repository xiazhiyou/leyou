package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    // 新增商品到购物车
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 查询购物车
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(){

        return ResponseEntity.ok(cartService.queryCartList());
    }

    // 修改购物车商品数量
    @PutMapping
    public ResponseEntity<Void> updateCartNum(
            @RequestParam("id") Long skuId,
            @RequestParam("num") Integer num){
        cartService.updateCartNum(skuId, num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 删除购物车中商品
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") Long skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}