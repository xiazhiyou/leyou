package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name="tb_spu_detail")
public class SpuDetail {
    @Id   // id没有加上自增主键，因为这张表id不是自增的，而是和spu表的id关联的
    private Long spuId;// 对应的SPU的id
    private String description;// 商品描述
    private String genericSpec;// 商品特殊规格的名称及可选值模板
    private String specialSpec;// 商品的全局规格属性
    private String packingList;// 包装清单
    private String afterService;// 售后服务
}