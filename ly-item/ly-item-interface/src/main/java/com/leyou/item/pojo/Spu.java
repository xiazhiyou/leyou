package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "tb_spu")
public class Spu {
    @Id
    @KeySql(useGeneratedKeys=true)
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String title;// 标题
    private String subTitle;// 子标题
    private Boolean saleable;// 是否上架
    @JsonIgnore     //在json序列化时将java bean中的一些属性忽略掉，序列化和反序列化都受影响。
    private Boolean valid;// 是否有效，逻辑删除用
    private Date createTime;// 创建时间
    @JsonIgnore
    private Date lastUpdateTime;// 最后修改时间

    @Transient
    private String cname;
    @Transient
    private String bname;
    @Transient
    private List<Sku> skus;
    @Transient
    private SpuDetail spuDetail;
}

/*
* 注：
   - po：persisent object，持久对象；它的字段必须和数据库字段完全一致
   - 由于数据库里没有商品分类和品牌的name字段，应该写成专门的vo对象（专门返回页面上的）
   - 查出来的是spu po，应该转成vo。但将来从页面接收到vo转成spu po才能往数据库传，可能会有各种对象转换的错误
   - 为了方便，我们把 商品分类和品牌的name字段 等字段添加到spu里去，由于这些字段数据库中没有，因此都应逐个添加`@Transient`注解（javax.persistence包下的）
   - 不想返回到界面的字段 可以加一个`@JsonIgnore`注解
* */