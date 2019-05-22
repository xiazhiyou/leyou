
# RESTFul风格
 RESTFul风格: 就是将资源的状态以适合客户端或服务端的形式从服务端转移到客户端（或者反过来）。在 REST 中，资源通过 URL（统一资源定位符） 进行识别和定位，然后通过行为(即 HTTP 方法)来定义 REST 来完成怎样的功能


- 表述性（REpresentational）： REST 资源实际上可以用各种形式来进行表述，包括 XML、JSON 甚至 HTML——最适合资源使用者的任意形式；

- 状态（State）： 当使用 REST 的时候，我们更关注资源的状态而不是对资源采取的行为；

- 转义（Transfer）： REST 涉及到转移资源数据，它以某种表述性形式从一个应用转移到另一个应用。

REST具有严格的规范，其主要就体现在对路径与返回的状态码必须严格遵循规则。
规则如下：
  - 1）对路径有要求：（请求规范）
  &ensp;&ensp;遵循REST规范的URI定义：

  ​          查询用户： http://localhost/user/{id}		        -GET

   	  添加用户： http://localhost/user/		               -POST

  ​          修改用户： http://localhost/user/{id}	        &ensp;&ensp;&ensp;&ensp;-PUT
  &ensp;        删除用户： http://localhost/user/{id}		        -DELETE

  - &ensp;&ensp;查询用户： http://localhost/user/{id}		        &ensp;&ensp;&ensp;&ensp;-GET


总结：即要求路径不允许有动词，必须是统一的名词，用路径占位符传参。通过请求方式判别增删改查业务。
(而之前查询用户这样写：http://localhost/user/query?id=1)
- 2）响应规范：
 &ensp;&ensp;必须严格返回状态码。如：查询成功(200),参数列表有误(400),资源或服务未汇未找到(404)


在使用 RESTful 风格之前，我们如果想要增加一条商品数据通常是这样的:

```java
/addCategory?name=xxx
```
但是使用了 RESTful 风格之后就会变成:

```java
/category
```
这就变成了使用同一个 URL ，通过约定不同的 HTTP 方法来实施不同的业务，这就是 RESTful 风格所做的事情了


# SpringBoot 中使用 RESTful

（User为实体类）
```java
@RestController 
@RequestMapping(value="/users")     // 通过这里配置使下面的映射都在/users下 
public class UserController { 
 
    // 创建线程安全的Map 
    static Map<Long, User> users = Collections.synchronizedMap(new HashMap<Long, User>()); 
 
    @RequestMapping(value="/", method=RequestMethod.GET) 
    public List<User> getUserList() { 
        // 处理"/users/"的GET请求，用来获取用户列表 
        // 还可以通过@RequestParam从页面中传递参数来进行查询条件或者翻页信息的传递 
        List<User> r = new ArrayList<User>(users.values()); 
        return r; 
    } 
 
    @RequestMapping(value="/", method=RequestMethod.POST) 
    public String postUser(@ModelAttribute User user) { 
        // 处理"/users/"的POST请求，用来创建User 
        // 除了@ModelAttribute绑定参数之外，还可以通过@RequestParam从页面中传递参数 
        users.put(user.getId(), user); 
        return "success"; 
    } 
 
    @RequestMapping(value="/{id}", method=RequestMethod.PUT) 
    public String putUser(@PathVariable Long id, @ModelAttribute User user) { 
        // 处理"/users/{id}"的PUT请求，用来更新User信息 
        User u = users.get(id); 
        u.setName(user.getName()); 
        u.setAge(user.getAge()); 
        users.put(id, u); 
        return "success"; 
    } 
 
    @RequestMapping(value="/{id}", method=RequestMethod.DELETE) 
    public String deleteUser(@PathVariable Long id) { 
        // 处理"/users/{id}"的DELETE请求，用来删除User 
        users.remove(id); 
        return "success"; 
    } 
 
}
```

