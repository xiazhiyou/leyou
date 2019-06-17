<font color="green">博客地址:</font> https://blog.csdn.net/qq_41649078/article/details/91402802

# 1. 分析
在购物车页面的最下方，有一个去结算按钮：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610213330361.png?)
当点击结算，我们应该跳转到订单结算页，而不是直接付款
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610213456433.png?)
可以看到，此处页面需要渲染的内容主要包含3部分：

- 收货人信息
- 支付方式
- 商品信息

点击提交订单按钮，然后需要把商品信息发到后台去完成订单创建，因此应创建订单微服务
# 2. 创建订单微服务
## 2.1 引入依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>leyou</artifactId>
        <groupId>com.leyou.parent</groupId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.leyou.service</groupId>
    <artifactId>ly-order</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>tk.mybatis</groupId>
            <artifactId>mapper-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.leyou.service</groupId>
            <artifactId>ly-item-interface</artifactId>
            <version>${leyou.latest.version}</version>
        </dependency>
        <dependency>
            <groupId>com.leyou.common</groupId>
            <artifactId>ly-common</artifactId>
            <version>${leyou.latest.version}</version>
        </dependency>
        <dependency>
            <groupId>com.leyou.service</groupId>
            <artifactId>ly-auth-common</artifactId>
            <version>${leyou.latest.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency><dependency>
        <groupId>com.github.pagehelper</groupId>
        <artifactId>pagehelper-spring-boot-starter</artifactId>
    </dependency>
        <dependency>
            <groupId>com.github.wxpay</groupId>
            <artifactId>wxpay-sdk</artifactId>
            <version>0.0.3</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
            <version>2.9.5</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## 2.2 配置

```yml
server:
  port: 8089
spring:
  application:
    name: order-service
  datasource:
      url: jdbc:mysql://localhost:3306/yun6
      username: root
      password: root
      driver-class-name: com.mysql.jdbc.Driver
  jackson:
    default-property-inclusion: non_null
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    registry-fetch-interval-seconds: 5
  instance:
    prefer-ip-address: true
    ip-address: 127.0.0.1
mybatis:
  type-aliases-package: com.leyou.order.pojo
ly:
  jwt:
    pubKeyPath: E:/course/JavaProject/javacode/idea/rsa/rsa.pub # 公钥地址
    cookieName: LY_TOKEN
```

## 2.3 启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.leyou.order.mapper")
public class LyOrderApplication {
    public static void main(String[] args) {

        SpringApplication.run(LyOrderApplication.class);
    }
}
```
## 2.4 添加路由
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610214257924.png?)
# 3. 准备
## 3.1 实体类与数据表
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611095201316.png?)
**订单表Order ：**
```java
@Data
@Table(name = "tb_order")
public class Order {

    @Id
    private Long orderId;// id
    private Long totalPay;// 总金额
    private Long actualPay;// 实付金额
    private Integer paymentType; // 支付类型，1、在线支付，2、货到付款

    private String promotionIds; // 参与促销活动的id
    private Long postFee = 0L;// 邮费
    private Date createTime;// 创建时间
    private String shippingName;// 物流名称
    private String shippingCode;// 物流单号
    private Long userId;// 用户id
    private String buyerMessage;// 买家留言
    private String buyerNick;// 买家昵称
    private Boolean buyerRate;// 买家是否已经评价

    private String receiver; // 收货人全名
    private String receiverMobile; // 移动电话
    private String receiverState; // 省份
    private String receiverCity; // 城市
    private String receiverDistrict; // 区/县
    private String receiverAddress; // 收货地址，如：xx路xx号
    private String receiverZip; // 邮政编码,如：310001

    private Integer invoiceType = 0;// 发票类型，0无发票，1普通发票，2电子发票，3增值税发票
    private Integer sourceType = 1;// 订单来源 1:app端，2：pc端，3：M端，4：微信端，5：手机qq端

    @Transient
    private OrderStatus orderStatus;

    @Transient
    private List<OrderDetail> orderDetails;
}
```

```sql
CREATE TABLE `tb_order` (
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `total_pay` bigint(20) NOT NULL COMMENT '总金额，单位为分',
  `actual_pay` bigint(20) NOT NULL COMMENT '实付金额。单位:分。如:20007，表示:200元7分',
  `promotion_ids` varchar(256) COLLATE utf8_bin DEFAULT '',
  `payment_type` tinyint(1) unsigned zerofill NOT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
  `post_fee` bigint(20) NOT NULL COMMENT '邮费。单位:分。如:20007，表示:200元7分',
  `create_time` datetime DEFAULT NULL COMMENT '订单创建时间',
  `shipping_name` varchar(20) COLLATE utf8_bin DEFAULT NULL COMMENT '物流名称',
  `shipping_code` varchar(20) COLLATE utf8_bin DEFAULT NULL COMMENT '物流单号',
  `user_id` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '用户id',
  `buyer_message` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '买家留言',
  `buyer_nick` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '买家昵称',
  `buyer_rate` tinyint(1) DEFAULT NULL COMMENT '买家是否已经评价,0未评价，1已评价',
  `receiver_state` varchar(128) COLLATE utf8_bin DEFAULT '' COMMENT '收获地址（省）',
  `receiver_city` varchar(256) COLLATE utf8_bin DEFAULT '' COMMENT '收获地址（市）',
  `receiver_district` varchar(256) COLLATE utf8_bin DEFAULT '' COMMENT '收获地址（区/县）',
  `receiver_address` varchar(256) COLLATE utf8_bin DEFAULT '' COMMENT '收获地址（街道、住址等详细地址）',
  `receiver_mobile` varchar(11) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
  `receiver_zip` varchar(16) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人邮编',
  `receiver` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
  `invoice_type` int(1) DEFAULT '0' COMMENT '发票类型(0无发票1普通发票，2电子发票，3增值税发票)',
  `source_type` int(1) DEFAULT '2' COMMENT '订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端',
  PRIMARY KEY (`order_id`),
  KEY `create_time` (`create_time`),
  KEY `buyer_nick` (`buyer_nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin
```

**订单详情表OrderDetail ：**

```java
@Data
@Table(name = "tb_order_detail")
public class OrderDetail {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    private Long orderId;// 订单id

    private Long skuId;// 商品id

    private Integer num;// 商品购买数量

    private String title;// 商品标题

    private Long price;// 商品单价

    private String ownSpec;// 商品规格数据

    private String image;// 图片
}
```

```sql
CREATE TABLE `tb_order_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单详情id ',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `sku_id` bigint(20) NOT NULL COMMENT 'sku商品id',
  `num` int(11) NOT NULL COMMENT '购买数量',
  `title` varchar(256) NOT NULL COMMENT '商品标题',
  `own_spec` varchar(1024) DEFAULT '' COMMENT '商品动态属性键值集',
  `price` bigint(20) NOT NULL COMMENT '价格,单位：分',
  `image` varchar(128) DEFAULT '' COMMENT '商品图片',
  PRIMARY KEY (`id`),
  KEY `key_order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8 COMMENT='订单详情表'
```

**订单状态表OrderStatus：**
```java
@Data
@Table(name = "tb_order_status")
public class OrderStatus {

    @Id
    private Long orderId;
    
    private Integer status;

    private Date createTime;// 创建时间

    private Date paymentTime;// 付款时间

    private Date consignTime;// 发货时间

    private Date endTime;// 交易结束时间

    private Date closeTime;// 交易关闭时间

    private Date commentTime;// 评价时间
}
```

```sql
CREATE TABLE `tb_order_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单详情id ',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `sku_id` bigint(20) NOT NULL COMMENT 'sku商品id',
  `num` int(11) NOT NULL COMMENT '购买数量',
  `title` varchar(256) NOT NULL COMMENT '商品标题',
  `own_spec` varchar(1024) DEFAULT '' COMMENT '商品动态属性键值集',
  `price` bigint(20) NOT NULL COMMENT '价格,单位：分',
  `image` varchar(128) DEFAULT '' COMMENT '商品图片',
  PRIMARY KEY (`id`),
  KEY `key_order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8 COMMENT='订单详情表'
```
## 3.2 mapper

```java
public interface OrderMapper extends BaseMapper<Order> {
}
```

```java
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
```

```java
public interface OrderStatusMapper extends Mapper<OrderStatus>{
}
```

## 3.3 登录校验
订单微服务和购物车微服务一样，都需要知道当前登录的用户是谁，那也就意味着订单微服务也需要我们在购物车微服务中写的拦截器`UserInterceptor`，这个拦截器的目的就是解析登录的用户，因此也需要`JwtProperties`、`MvcConfig`（注册拦截器，使其生效）以及公钥地址。我们可以从购物车微服务中将这些copy过来，这里就不写啦
![在这里插入图片描述](https://img-blog.csdnimg.cn/201906110956255.png?)
由于**订单数据量较大，将来一定会做分库分表，因此采用水平拆分**（表字段一样，只是数据做了切分）。但**水平拆分，表的自增长会出现问题，因为MySQL中每个表是独立增长，订单编号会存在重复现象**，因此不能用自增长。我们必须得有一个全局唯一的id才行。

- redis中有一个incr()方法，其作用就是自增长，我们**利用redis的自增长完全可以实现一个全局唯一的id，但是，redis形成id的速度较慢**（redis本身集成速度快，但生成id需要靠网络，处理速度有限）
- **雪花算法**

# 4. 创建订单接口

当点击 提交订单 按钮，会看到控制台发起请求：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611103538777.png?)
订单信息共有3张表，内容很多，但是前台提交的数据却很少，也就是说我们需要自己填充很多数据
## 4.1 页面分析
可以通过页面看到接口信息：

- 请求方式：POST
- 请求路径：/order
- 请求参数：包含订单、订单详情等数据的json对象。
- 返回结果：订单编号

**请求参数：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611105252618.png)
==注==：
- 根据收货人id去用户中心可查出收货人的详细信息；
 - **收货清单中不能传价格参数**，因为url地址对外暴露的，防止有人利用insomnia等工具篡改价格再进行提交，应自己根据id去查
 - json格式，我们采用对象接收，因此我们封装一个对象

## 4.2 封装接收对象

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    @NotNull //数据校验不能为空
    private Long addressId; // 收获人地址id

    private Integer paymentType;// 付款类型

    private List<CartDTO> carts;// 订单详情
}
```
注：
- dto: orderDataTransferObject
- 三个字段正好对应前端页面http://api.leyou.com/api/order-service/order 的三个字段
- carts又是一个集合  包含了商品信息，所以又定义一个cartDTO（放在ly-common中，供后续商品微服务调用）

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long skuId; //商品skuId
    private Integer num; //数量
}

```
## 4.3 web

```java
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单 返回订单ID
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        // 创建订单
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }
}
```


- `@ResponseBody`：把响应结果转成序列化以后放到响应体里，不一定转成json。现在转成json，是Springmvc在转换过程中会用消息转换器，默认情况下是转成json。

- `@RequestBody`：把json默认转成对象，反序列化。

- `@RestController`：把返回结果当成json处理。

## 4.4 service
创建订单逻辑比较复杂，需要组装订单数据，基本步骤如下：

- 获取登录用户信息
- 生成订单编号，初始化订单基本信息
- 查询收货人信息
- 查询商品信息
- 封装OrderDetail信息
- 计算总金额，实付金额
- 保存订单状态信息
- *删除购物车中已购买商品(暂时不做)*
- 减库存
### 4.4.1 生成订单编号
>订单id的特殊性

订单数据非常庞大，将来一定会做分库分表。那么这种情况下， 要保证id的唯一，就不能靠数据库自增，而是自己来实现算法，生成唯一id。

**雪花算法**

这里的订单id是通过一个工具类生成的：（通过位运算生成id）
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611113046463.png?)
而工具类所采用的生成id算法，是由Twitter公司开源的snowflake（雪花）算法。

>简单原理

雪花算法会生成一个64位的二进制数据，为一个Long型。(转换成字符串后长度最多19) ，其基本结构：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611113103957.png?)
第一位：为未使用

第二部分：41位为毫秒级时间(41位的长度可以使用69年)（**解决1ms产生多个订单的问题**）

第三部分：5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点）(**解决多台机器同时产生的问题**：一台机器并发能力有限，在1ms内产生多个订单，很有可能不是一台机器产生的，而是搭了集群，订单微服务压力大，n台机器一起产生订单，很有可能在同一毫秒内产生多个)

第四部分：最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）（**解决同一时刻同一机器产生多个订单的问题**）

snowflake生成的ID整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞（由datacenter和workerId作区分），并且效率较高。经测试snowflake每秒能够产生26万个ID。
>配置

为了保证不重复，我们给每个部署的节点都配置机器id：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611120209729.png?)
加载属性：

```java
@Data
@ConfigurationProperties(prefix = "ly.worker")
public class IdWorkerProperties {

    private long workerId;// 当前机器id

    private long dataCenterId;// 序列号
}
```
编写配置类：（实体类）把IdWorker注册成一个bean注入到Spring中：通过new IdWorker对象的方式去初始化

```java
@Configuration
@EnableConfigurationProperties(IdWorkerProperties.class)
public class IdWorkerConfig {

    // 注册IdWorker
    @Bean
    public IdWorker idWorker(IdWorkerProperties prop) {
        return new IdWorker(prop.getWorkerId(), prop.getDataCenterId());
    }
}
```
使用：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611150332186.png)
### 4.4.2 查询sku接口
创建订单过程中，肯定需要查询sku信息，因此我们需要在商品微服务中提供根据skuId集合查询sku的接口：

GoodClient
```java
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi{
}
```

GoodApi：

```java
//根据sku的id集合查询所有的sku
@GetMapping("/sku/list/ids")
List<Sku> querySkuByIds(@RequestParam("ids") List<Long> ids);
```

GoodController：

```java
//根据sku的id集合查询所有的sku
@GetMapping("/sku/list/ids")
public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids){
    return ResponseEntity.ok(goodsService.querySkuByIds(ids));
}
```

GoodService：

```java
//根据sku的id集合查询所有的sku
 public List<Sku> querySkuByIds(List<Long> ids) {
     List<Sku> skus = skuMapper.selectByIdList(ids);
     if(CollectionUtils.isEmpty(skus)){
         throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
     }

     //查询库存
     List<Stock> stockList = stockMapper.selectByIdList(ids);
     if(CollectionUtils.isEmpty(stockList))
         throw new LyException(ExceptionEnum.STOCK_NOT_FOUND);

     //把stock变成一个map，其key：skuId,值：库存值
     Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
     skus.forEach(s ->s.setStock(stockMap.get(s.getId())));

     return skus;
 }
```

### 4.4.3 准备物流假数据

我们前端页面传来的是addressId，我们需要根据id查询物流信息，但是因为还没做物流地址管理，所以我们准备一些假数据。

首先是实体类：

```java
@Data
public class AddressDTO {
    private Long id;
    private String name;
    private String phone;
    private String state;
    private String city;
    private String district;
    private String address;
    private String zipCode;
    private Boolean isDefault;
}
```
然后是一个常量类：

```java
public abstract class AddressClient {
    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO>(){
        {
            AddressDTO address = new AddressDTO();
            address.setId(1L);
            address.setAddress("太白南路");
            address.setCity("西安");
            address.setDistrict("雁塔区");
            address.setName("mushrooom");
            address.setPhone("186****7292");
            address.setState("陕西");
            address.setZipCode("7100710");
            address.setIsDefault(true);
            add(address);

            AddressDTO address2 = new AddressDTO();
            address2.setId(2L);
            address2.setAddress("盛世商都");
            address2.setCity("西安");
            address2.setDistrict("长安区");
            address2.setName("mushroom");
            address2.setPhone("186****7292");
            address2.setState("陕西");
            address2.setZipCode("03500150");
            address2.setIsDefault(false);
            add(address2);
        }
    };

    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList) {
            if(addressDTO.getId() == id){
                return addressDTO;
            }
        }
        return null;
    }
}
```
结构：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611160319815.png?)
### 4.4.4 订单状态
订单状态一般以用数字表示，但是不方便开发，此处我们为订单状态定义了枚举
```java
public enum  OrderStatusEnum {
    UN_PAY(1, "初始化，未付款"),
    PAYED(2, "已付款，未发货"),
    DELIVERED(3, "已发货，未确认"),
    SUCCESS(4, "已确认,未评价"),
    CLOSED(5, "已关闭，交易失败"),
    RATED(6, "已评价，交易结束")
    ;

    private int code;
    private String msg;

    OrderStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int value(){
        return this.code;
    }

    public String msg(){
        return msg;
    }
}
```
### 4.4.5 减库存
减库存应该在商品微服务里去实现，因此我们写一个接口，在商品微服务里实现，在订单微服务里远程调用商品微服务的减库存功能：

GoodApi：

```java
// 减库存
@PostMapping("stock/decrease")
void decreaseStock(@RequestBody List<CartDTO> cartDTOS);
```

GoodController：

```java
//减库存
@PostMapping("stock/decrease")
public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> cartDTOS){
    goodsService.decreaseStock(cartDTOS);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}
```

GoodService：

```java
@Transactional
public void decreaseStock(List<CartDTO> cartDTOS) {
    // 不能用if判断来实现减库存，当线程很多的时候，有可能引发超卖问题
    // 加锁也不可以  性能太差，只有一个线程可以执行，当搭了集群时synchronized只锁住了当前一个tomcat
    for (CartDTO cartDTO : cartDTOS) {
        int count = stockMapper.decreaseStock(cartDTO.getSkuId(), cartDTO.getNum());
        if(count != 1){
            throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
        }
    }
}
```
StockMapper：
```java
public interface StockMapper extends BaseMapper<Stock> {
    @Update("UPDATE tb_stock SET stock = stock - #{num} WHERE sku_id = #{id} AND stock >= #{num}")
    int decreaseStock(@Param("id") Long id, @Param("num") Integer num);
}
```

==分析==：减库存的业务实现

**减库存**可以采用同步调用（商品微服务提供接口，通过Feign调用），也可以采用异步调用（通过RabbitMq），我们**采用同步调用**，原因分析：[采用同步调用方式减库存](https://blog.csdn.net/qq_41649078/article/details/91492383)


### 4.4.6 完整代码
```java
// 创建订单，返回订单ID
@Transactional
public Long createOrder(OrderDTO orderDTO) {

    // 1 新增订单
    Order order = new Order();
    // 1.1 订单编号自己生成(保证全局唯一)，基本信息 -- 订单ID，snowflake（雪花）算法生成全局唯一id,64位二进制数据
    long orderId = idWorker.nextId();
    order.setOrderId(orderId);
    order.setCreateTime(new Date());
    order.setPaymentType(orderDTO.getPaymentType());

    // 1.2 用户信息
    UserInfo user = UserInterceptor.getUser();
    order.setUserId(user.getId());
    order.setBuyerNick(user.getUsername());
    order.setBuyerRate(false);//用户评价

    // 1.3 收货人地址信息 -- orderDTO中只有地址ID（addressID），要根据地址ID去数据库中查询(假数据)
    AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
    order.setReceiver(addr.getName());//收货人
    order.setReceiverMobile(addr.getPhone());//收货人手机号码
    order.setReceiverAddress(addr.getAddress());//收货所在街道
    order.setReceiverState(addr.getState());//收货人所在省
    order.setReceiverCity(addr.getCity());//收货人所在城市
    order.setReceiverDistrict(addr.getDistrict());//收货人所在区
    order.setReceiverZip(addr.getZipCode());//收货人邮编

    // 1.4 金额
    // 把cartDTO转为一个map，key是sku的id，值是num
    Map<Long, Integer> numMap = orderDTO.getCarts()
            .stream().collect(Collectors.toMap(CartDTO::getSkuId,CartDTO::getNum));
    // 获取所有sku的id
    Set<Long> ids = numMap.keySet();
    // 根据id查询sku
    List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));

    // 准备orderDetail集合
    List<OrderDetail> details = new ArrayList<>();

    Long totalPrice = 0L;
    for (Sku sku : skus) {
    	//计算商品总价
        totalPrice += sku.getPrice() * numMap.get(sku.getId());

        //封装orderDetail
        OrderDetail detail = new OrderDetail();
        detail.setImage(StringUtils.substringBefore(sku.getImages(),","));
        detail.setNum(numMap.get(sku.getId()));
        detail.setOrderId(orderId);
        detail.setOwnSpec(sku.getOwnSpec());
        detail.setPrice(sku.getPrice());
        detail.setSkuId(sku.getId());
        detail.setTitle(sku.getTitle());

        details.add(detail);
    }
    order.setTotalPay(totalPrice);
    order.setActualPay(totalPrice + order.getPostFee() - 0 );// 实付金额= 总金额 + 邮费 - 优惠金额

    // 1.5 写入数据库
    int count = orderMapper.insertSelective(order);
    if(count != 1){
        log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
        throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
    }

    // 2 新增订单详情
    count = orderDetailMapper.insertList(details);
    if(count != details.size()){
        log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
        throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
    }

    // 3 新增订单状态
    OrderStatus orderStatus = new OrderStatus();
    orderStatus.setOrderId(orderId);
    orderStatus.setCreateTime(order.getCreateTime());
    orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
    count = orderStatusMapper.insertSelective(orderStatus);
    if(count != 1){
        log.error("[创建订单] 创建订单失败，orderID:{}", orderId);
        throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
    }

    // 4 减库存 -- 需要调用商品微服务的减库存功能,选同步调用(同步调用：商品微服务提供接口，通过feign调用)，(异步调用：通过rabbitmq)
    // 传递商品id和数量两个参数
    List<CartDTO> cartDTOS = orderDTO.getCarts();
    goodsClient.decreaseStock(cartDTOS);

    return orderId;
}
```
## 4.5 测试
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190611201032213.png)
当商品库存为1，执行上述代码时会失败，抛出异常，事务回滚。刷新数据库，商品库存不变还是1，不会发生超卖现象
# 5. 查询订单接口
当完成订单业务时，会跳转到支付页面，从页面可以看到需要 订单编号、支付金额以及二维码这三项，同时向后台发起了一个请求，接下来我们就来实现这个功能：查询订单
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190612114625636.png?)
## 5.1 web
请求方式：GET
请求路径：/order/订单id
请求参数：订单id
返回结果：order对象,订单的json对象

```java
// 通过订单编号查询订单
@GetMapping("{id}")
public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
    return ResponseEntity.ok(orderService.queryOrderById(id));
}
```
## 5.2 service

```java
// 通过订单编号查询订单
public Order queryOrderById(Long id) {
    //查询订单
    Order order = orderMapper.selectByPrimaryKey(id);
    if (order == null) {
        throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
    }

    // 查询订单详情
    OrderDetail detail = new OrderDetail();
    detail.setOrderId(id);
    List<OrderDetail> orderDetails = orderDetailMapper.select(detail);
    if(CollectionUtils.isEmpty(orderDetails)){
        throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUNT);
    }
    order.setOrderDetails(orderDetails);

    // 查询订单状态
    OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
    if(orderStatus == null){
        throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
    }
    order.setOrderStatus(orderStatus);

    return order;
}
```
注:
- 在实际需求当中，我们往往也会根据订单编号查询订单详情及订单状态，因此在这里我们一并都做了。


刷新页面，可以看到订单编号以及支付金额：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190612120831735.png?)

# 6. 微信支付
但是还不够，页面还差一个二维码，现在没有是因为二维码属于微信支付，我们应发请求去查微信支付链接，生成二维码靠链接生成，接下里我们要做的就是[微信支付](https://blog.csdn.net/qq_41649078/article/details/91547051)
