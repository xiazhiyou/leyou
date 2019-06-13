<font color="green">博客地址:</font> https://blog.csdn.net/qq_41649078/article/details/91366741

# 1. 购物车功能分析

## 1.1 需求

需求描述：

- 用户可以在登录状态下将商品添加到购物车
  - 放入数据库
  - 放入redis（采用）
- 用户可以在未登录状态下将商品添加到购物车
  - 放入localstorage
- 用户可以使用购物车一起结算下单
- 用户可以查询自己的购物车
- 用户可以在购物车中可以修改购买商品的数量。
- 用户可以在购物车中删除商品。
- 在购物车中展示商品优惠信息
- 提示购物车商品价格变化
## 1.2 业务分析
在需求描述中，不管用户知否登录，都需要实现加入购物车功能，那么已登录和未登录下，购物车数据应存放在哪里呢？
>未登录购物车

用户如果**未登录**，将数据保存在服务端存在一些问题：
- 无法确定用户身份，需要借助于客户端存储识别身份
- 服务端存储数据压了增加，而且可能是无效数据

那么我们应该**把数据保存在客户端，这样每个用户保存自己的数据，就不存在身份识别的问题了，而且也解决了服务端数据存储压力问题。**
>已登录购物车

用户**登录**时，数据保存在哪里呢？
我们首先想到的是数据库，不过购物车数据比较特殊，**读和写比较频繁，存储数据库压力会比较大。因此我们可以考虑存入Redis中**。
## 1.3 流程图
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610100306622.png?)
这幅图主要描述了两个功能：新增商品到购物车、查询购物车。

新增商品：

    - 判断是否登录
      - 是：则添加商品到后台Redis中
      - 否：则添加商品到本地的Localstorage

无论哪种新增，完成后都需要查询购物车列表：

- 判断是否登录
  - 否：直接查询localstorage中数据并展示
  - 是：已登录，则需要先看本地是否有数据，
    - 有：需要提交到后台添加到redis，合并数据，而后查询
    - 否：直接去后台查询redis，而后返回

# 2.未登录购物车-- Localstorage


## 2.1 购物车的数据结构

首先分析一下未登录购物车的数据结构。

我们看下页面展示需要什么数据：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610100620315.png?)
因此每一个购物车信息，都是一个对象，包含：

```java
{
    skuId:2131241,
    title:"小米6",
    image:"",
    price:190000,
    num:1,
    ownSpec:"{"机身颜色":"陶瓷黑尊享版","内存":"6GB","机身存储":"128GB"}"
}
```
==注：==**这里**不能根据id查询价格等信息，是因为这些信息都应保存的是刚加入购物车时的数据，若此时从数据库去查，有可能价格等数据信息会变，或者商品下架，或者规格参数变了，去数据库查也不一定能一一对应,因此**应把加入数据库那一刻的信息做一个快照**。
另外，购物车中不止一条数据，因此最终会是对象的数组。即：

```java
[
    {...},{...},{...}
]
```
## 2.2 web本地存储

知道了数据结构，下一个问题，就是如何保存购物车数据。前面我们分析过，可以使用Localstorage来实现。Localstorage是web本地存储的一种，那么，什么是web本地存储呢？

### 2.2.1 什么是web本地存储？
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610100805217.png?)
web本地存储主要有两种方式：

- LocalStorage：localStorage 方法存储的数据没有时间限制。第二天、第二周或下一年之后，数据依然可用。 
- SessionStorage：sessionStorage 方法针对一个 session 进行数据存储。当用户关闭浏览器窗口后，数据会被删除。 

我们采用的当然是**localstorage**，关闭浏览器商品信息依然存在。
### 2.2.2 LocalStorage的用法

语法非常简单：
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019061010101286.png?)
```java
localStorage.setItem("key","value"); // 存储数据
localStorage.getItem("key"); // 获取数据
localStorage.removeItem("key"); // 删除数据
```
注意：**localStorage和SessionStorage都只能保存字符串**。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610103527822.png)

不过，在我们的common.js中，已经对localStorage进行了简单的封装：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610103430308.png?)
示例：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610103512992.png?)
## 2.3 添加购物车
### 2.3.1 页面分析
我们看下商品详情页：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610111944523.png?)
点击加入购物车会跳转到购物车页面：cart.html，用户信息都保存在localstorage中，结果如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610112529394.png?)
刷新购物车页面，却发现报错了：浏览器发了一次请求
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610112843251.png?)
可是，明明localstorage中有完整信息（下图）啊，为什么还要再去查一次？原因：**购物车中加入的信数据信息只是当下那一刻的信息**，但是这些信息胡随着时间变化，我们需要查一下价格是否发生了变化，商品有没有下架等，若商品价格发生变化了，我们可以给与友好提示
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610113048626.png?)
### 2.3.2 实现查询所有sku接口
我们需要在商品微服务中添加该接口：

- 请求方式：get
- 请求路径：/sku/list
- 请求参数：sku的id集合
- 返回结果：sku集合

**GoodsController**

```java
//根据sku的id集合查询所有的sku
@GetMapping("/sku/list/ids")
public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids){
    return ResponseEntity.ok(goodsService.querySkuByIds(ids));
}
```
**GoodsService**
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
刷新页面，完成查询
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610115541260.png?)
之后我们在数据库中修改一下商品价格，刷新页面可以看到友好提示
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019061012044082.png?)
总结：未登录情况下，我们都是在localstorage中来完成，从ly.store可以体现出。
# 3. 登录购物车-- Redis
## 3.1 搭建项目
### 3.1.1 引入依赖

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
    <artifactId>ly--cart</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>com.leyou.service</groupId>
            <artifactId>ly-auth-common</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.leyou.common</groupId>
            <artifactId>ly-common</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
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

### 3.1.2 配置

```yml
server:
  port: 8088
spring:
  application:
    name: cart-service
  redis:
    host: 192.168.184.130
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    registry-fetch-interval-seconds: 5
  instance:
    prefer-ip-address: true
    ip-address: 127.0.0.1
```

### 3.1.3 启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LyCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyCartApplication.class, args);
    }
}
```
### 3.1.4 添加路由
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610151410655.png?)
## 3.2 添加登录校验
当用户在商品详情页选好商品添加到购物车时，经过上面的分析我们知道，商品不是添加到数据库中，而是添加到Redis中，而**Redis是以<key,value>的形式存储**的，那key自然就是用户了，而我们的**登录用户信息key存储在token中**，所以要想知道登录用户必须先解析token，因此当请求到达购物车时，不是刚开始就完成购物车功能，而是应先知道登录的是谁，即**添加登录校验，通过JWT鉴权即可实现**。

不仅加入购物车需要，查询购物车、修改、删除等也需要，凡是和购物车相关的一切逻辑都得先知道当前登录的是谁，那也就意味着获取当前登录信息的逻辑在每个controller里都得写，这样就造成了代码的重复，因此要进行抽取。我们希望每一个controller中的每一个方法（每个业务功能）都可以提前拿到登录用户信息，而这个解析我们又希望是统一解析，而不是我们每一次单独去解析，此时我们想到**SpringMVC的拦截器**——interceptor ，通过拦截器我们就可以统一处理所有请求，处理完请求后放行则会到达controller层中的方法（前置拦截），因此我们可以在拦截器里解析token。

可是，我们在网关里已经写过了统一登录的拦截器了（AuthFilter），用户请求到达AuthFilter已经做了解析了，为什么不把解析好的用户信息直接传递过去？原因：我们网关微服务和购物车微服务不是一个微服务，属于不同的tomcat，不共享request，session 也就取不出来，所以不能从一个tomcat传到另外一个tomcat。网关只是做登录校验的，它不能帮我们把用户信息传递过去，得微服务自己解析。
### 3.2.1 引入JWT相关依赖

我们引入之前写的鉴权工具：ly-common

```java
<dependency>
    <groupId>com.leyou.service</groupId>
    <artifactId>ly-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
### 3.2.2 配置公钥

```yml
ly:
  jwt:
    pubKeyPath: E:/course/JavaProject/javacode/idea/rsa/rsa.pub # 公钥地址
    cookieName: LY_TOKEN
```
### 3.2.3 加载公钥

```java
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String pubKeyPath;// 公钥
    private String cookieName;

    private PublicKey publicKey; // 公钥

    // 对象一旦实例化后，就应该读取公钥和私钥
    @PostConstruct // 构造函数执行完毕后就执行
    public void init(){

        // 获取公钥和私钥
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
### 3.2.4 编写拦截器

>分析：解析完token后需要把user信息传递过去，那通过什么传递呢？

既然要传递，就要思考从拦截器到controller到service再到dao层有什么东西是共享的？——**request、spring容器、thread**
- request ：可行，但是SpringMVC是不推荐这种方案的（request.setAttribute("user",user);）
- spring容器：会有线程安全问题，因为spring容器都单例的
- thread：可行，一个请求一个线程，请求与线程共享。直到整个请求处理完毕，线程才会释放，归还给tomcat。在高并发请求时，请求到达以后存到一个容器threadlocal（线程域）中，是一个map结构，key是thread，value是存储的对象，因为key是线程，这就保证了不同线程之间不共享，只有同一个线程才会共享，避免了线程安全问题。
![在这里插入图片描述](https://img-blog.csdnimg.cn/201906101651111.png?)

UserInterceptor拦截器：
```java
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class UserInterceptor implements HandlerInterceptor {

	@Autowired
    private JwtProperties prop;

    // threadLocal是一个map结构，key是thread，value是存储的值
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        try {
            // 解析token -- 解析token要先获得cookie
            String token = CookieUtils.getCookieValue(request, prop.getCookieName());
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());

            // 保存user -- request和thread是“共享”的，所以可以把user放到这两个中
            tl.set(user); // key是不需要自己给定的，会自己获取

            return true;

        } catch (Exception e) {
            log.error("[购物车异常] 用户身份解析失败！", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        tl.remove();// 用完之后要删除
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
```
==注==：tl 是一个map，讲道理map存数据应该用put(key,value)，而这里用 tl.set(user)；原因：key是当前线程，执行当前行代码的就是当前线程，我们当然能拿到该线程（通过Thread.currentThread()），因此**key是不需要我们给定，会自己获取**。我们取的时候也不需要指定key，tl.get();它会以当前线程作为key去取，只有是同一个线程才可以取得到。
### 3.2.5 配置拦截器
配置SpringMVC，使拦截器生效：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610174750564.png?)
==注==：new 是自己在创建对象，但是拦截器中要使用spring（@EnableConfigurationProperties(JwtProperties.class)），如果使用spring，就不能自己创建，spring要想注入，必须是spring来创建，因此**我们修改拦截器，把自动注入去掉，放到MvcConfig中，并修改成通过构造函数的方式来注入**：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610175319190.png?)
最终代码，**MvcConfig** ：
```java
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer{

    @Autowired
    private JwtProperties prop;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // new 是自己在创建对象，但是拦截器中要使用spring，如果使用spring，就不能自己创建，要用spring来创建
        registry.addInterceptor(new UserInterceptor(prop)).addPathPatterns("/**");
    }
}
```
最终代码，**UserInterceptor 拦截器**：

```java
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    public UserInterceptor(JwtProperties prop) {

        this.prop = prop;
    }

    // threadLocal是一个map结构，key是thread，value是存储的值
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        try {
            // 解析token -- 解析token要先获得cookie
            String token = CookieUtils.getCookieValue(request, prop.getCookieName());
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());

            // 保存user -- request和thread是“共享”的，所以可以把user放到这两个中
            tl.set(user); // key是不需要给定，会自己获取,通过Thread.currentThread()

            return true;

        }catch (Exception e) {
            log.error("[购物车异常] 用户身份解析失败！", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        tl.remove();// 用完之后要删除
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
```
这样拦截器就生效了~
## 3.3 后台购物车设计
### 3.3.1 数据结构设计
已登录的数据结构和未登录的数据结构没什么太大差别，因为都是购物车，最终展示出来的页面都是一样的。所以数据结构是一样的，只是存储位置不同。 

当用户登录时，我们需要把购物车数据保存到后台，可以选择保存在数据库。**但是购物车是一个读写频率很高的数据。因此我们这里选择读写效率比较高的Redis作为购物车存储**。

Redis有5种不同数据结构（String，Hash，List，Set，SortSet），这里选择哪一种比较合适呢？

- 首先不同用户应该有独立的购物车，因此购物车应该以用户的作为key来存储，Value是用户的所有购物车信息。这样看来基本的k-v结构就可以了。
- 但是，我们对购物车中的商品进行增、删、改操作，基本都需要根据商品id进行判断，为了方便后期处理，我们的购物车也应该是k-v结构，key是商品id，value才是这个商品的购物车信息。（redis里也是只能存字符串，所以存进去时得进行序列化操作）

综上所述，我们的**购物车结构是一个双层Map**：Map<String,Map<String,String>>

- 第一层Map，Key是用户id
- 第二层Map，Key是购物车中商品id，值是购物车数据
### 3.3.2 实体类

```java
@Data
public class Cart {

    private Long skuId;// 商品id
    private String title;// 标题
    private String image;// 图片
    private Long price;// 加入购物车时的价格
    private Integer num;// 购买数量
    private String ownSpec;// 商品规格参数
}
```
## 3.4 添加商品到购物车
### 3.4.1 web
- 请求方式：新增，肯定是Post
- 请求路径：/cart ，这个其实是Zuul路由的路径，我们可以不管
- 请求参数：Json对象，包含skuId和num属性
- 返回结果：无

```java
// 新增商品到购物车
@PostMapping
public ResponseEntity<Void> addCart(@RequestBody Cart cart){
    cartService.addCart(cart);
    return ResponseEntity.status(HttpStatus.CREATED).build();
}
```
### 3.4.2 service
这里我们不访问数据库，而是直接操作Redis。基本思路：

- 先查询之前的购物车数据
- 判断要添加的商品是否存在
  - 存在：则直接修改数量后写回Redis
  - 不存在：新建一条数据，然后写入Redis

代码：

```java
// 新增商品到购物车
public void addCart(Cart cart) {
    // 获取登录的用户 -- 从线程中获得
    UserInfo user = UserInterceptor.getUser();

    // redis存储的结构是一个Map<String,Map<String,String>>,第一个key是用户的key，第二个key是商品的key，value是商品信息
    String key = KEY_PREFIX + user.getId();
    String hashKey = cart.getSkuId().toString();
    BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);

    if(operation.hasKey(hashKey)){
            // 如果存在  商品数量新增,新增之前先取出商品信息
            String json = operation.get(hashKey).toString();
            Cart cacheCart = JsonUtils.parse(json, Cart.class);//转成对象
            cacheCart.setNum(cacheCart.getNum() + cart.getNum());
            operation.put(hashKey,JsonUtils.serialize(cacheCart));//写回redis，转回json
        }else{
            // 如果不存在 新增
            operation.put(hashKey, JsonUtils.serialize(cart));
        }
}
```
### 3.4.3 结果
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019061019471769.png?)
## 3.5 查询购物车
由于已登录时的查询购物车功能没有实现，因此上述新增商品到购物车暂时看不到，那我们来实现查询当前登录状态下的所有购物车。
### 3.5.1 web

```java
// 查询购物车
@GetMapping("list")
public ResponseEntity<List<Cart>> queryCartList(){

    return ResponseEntity.ok(cartService.queryCartList());
}
```

### 3.5.2 service

```java
// 查询购物车
public List<Cart> queryCartList() {
    // 获取登录的用户 -- 从线程中获得
    UserInfo user = UserInterceptor.getUser();
    // key
    String key = KEY_PREFIX + user.getId();

    if(!redisTemplate.hasKey(key)){
        throw new LyException(ExceptionEnum.CART_NOT_FOUND);
    }

    // 获取登录用户的所有购物车
    BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);

    List<Cart> carts = operation.values().stream()
            .map(o -> JsonUtils.parse(o.toString(), Cart.class))
            .collect(Collectors.toList());

    return carts;
}
```
### 3.5.3 结果
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190610203450268.png?)
## 3.6 修改商品数量
### 3.6.1 web

```java
// 修改购物车商品数量
@PutMapping
public ResponseEntity<Void> updateCartNum(
        @RequestParam("id") Long skuId,
        @RequestParam("num") Integer num){
    cartService.updateCartNum(skuId, num);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}
```

### 3.6.2 service

```java
// 修改购物车商品数量
public void updateCartNum(Long skuId, Integer num) {
    // 获取登录的用户 -- 从线程中获得
    UserInfo user = UserInterceptor.getUser();
    String key = KEY_PREFIX + user.getId();

    // 获取登录用户的所有购物车
    BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
	//判断是否存在
    if(!operation.hasKey(skuId.toString())){
        throw new LyException(ExceptionEnum.CART_NOT_FOUND);
    }
    // 查询
    String json = operation.get(skuId.toString()).toString();
    Cart cart = JsonUtils.parse(json, Cart.class);
    cart.setNum(num);

    // 写回redis
    operation.put(skuId.toString(), JsonUtils.serialize(cart));
}
```
## 3.7 删除购物车商品
### 3.7.1 web

```java
// 删除购物车中商品
@DeleteMapping("{skuId}")
public ResponseEntity<Void> deleteCart(@PathVariable("skuId") Long skuId){
    cartService.deleteCart(skuId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}
```

### 3.7.2 service

```java
// 删除购物车中商品
public void deleteCart(Long skuId) {
    // 获取登录的用户 -- 从线程中获得
    UserInfo user = UserInterceptor.getUser();
    String key = KEY_PREFIX + user.getId();

    // 删除
    redisTemplate.opsForHash().delete(key, skuId.toString());
}
```
## 3.8 待实现
登录后购物车合并

当跳转到购物车页面，查询购物车列表前，需要判断用户登录状态，

- 如果登录：
  - 首先检查用户的LocalStorage中是否有购物车信息，
  - 如果有，则提交到后台保存，
  - 清空LocalStorage
- 如果未登录，直接查询即可

