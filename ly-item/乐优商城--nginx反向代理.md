<font color="green">博客地址:</font> https://blog.csdn.net/qq_41649078/article/details/90453488

# nginx
# 1. 使用域名访问本地项目
写在nginx之前的一些内容~
为了项目优雅起见，我们都采用域名来访问。
例：一级域名：www.leyou.com
&ensp;&ensp;&ensp;&ensp;二级域名：manage.leyou.com

在浏览器输入域名时，浏览器会根据域名找到对应的ip和端口（域名解析）

**域名解析**

一个域名一定会被解析为一个或多个ip。这一般会包含两步：

- 本地域名解析
  浏览器会首先在本机的hosts文件中查找域名映射的IP地址，如果查找到就返回IP ，没找到则进行域名服务器解析，一般本地解析都会失败，因为默认这个文件是空的。

- 域名服务器解析（DNS）
  本地解析失败，才会进行域名服务器解析，域名服务器就是网络中的一台计算机，里面记录了所有注册备案的域名和ip映射关系，一般只要域名是正确的，并且备案通过，一定能找到。

**解决域名解析问题**

我们不可能去购买一个域名，因此我们可以伪造本地的hosts文件，实现对域名的解析。修改本地的host为：
```java
   127.0.0.1 api.leyou.com  #我们的网关Zuul
   127.0.0.1 manage.leyou.com #我们的后台系统地址
```

这样就实现了域名的关系映射了。
# 2. nginx解决端口问题
虽然域名解决了，但是现在如果我们要访问，还得自己加上端口：http://manage.leyou.com:9001。

但我们希望的是直接域名访问：http://manage.leyou.com。这种情况下端口默认是80，因此请求要用到**反向代理工具：Nginx**把请求转移到9001端口
## 2.1 什么是Nginx
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190522222224553.png?)
nginx可以作为web服务器，但更多的时候，我们把它作为网关，因为它具备网关必备的功能：

- 反向代理
- 负载均衡
- 动态路由
- 请求过滤

## 2.2 nginx作为web服务器

Web服务器分2类：

- web应用服务器，如：
  - tomcat
  - resin
  - jetty
- web服务器，如：
  - Apache 服务器
  - Nginx
  - IIS

区分：web服务器不能解析jsp等页面，只能处理js、css、html等静态资源

并发：web服务器的并发能力远高于web应用服务器

Nginx（静态） + tomcat（动态）
## 2.3 nginx作为反向代理

 什么是反向代理？

- 代理：通过客户机的配置，实现让一台服务器代理客户机，客户的所有请求都交给代理服务器处理。
- 反向代理：用一台服务器，代理真实服务器，用户访问时，不再是访问真实服务器，而是代理服务器。

nginx可以当做反向代理服务器来使用：

- 我们需要提前在nginx中配置好反向代理的规则，不同的请求，交给不同的真实服务器处理
- 当请求到达nginx，nginx会根据已经定义的规则进行请求的转发，从而实现路由功能



利用反向代理，就可以解决我们前面所说的端口问题，如图
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190522222526439.png?)
***反向代理配置***

示例：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190522222710269.png?)
nginx中的每个server就是一个反向代理配置，可以有多个server

**整个项目完整配置：**
（虚拟机ip：192.168.184.130，本机ip：192.168.1.109）
```java
user root;
#user  nobody;
worker_processes  1;

events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;
    client_max_body_size  10m;
	
	user_agent" "$http_x_forwarded_for"';

    sendfile        on;

    keepalive_timeout  65;

   
    server {
        listen       80;
        server_name  www.leyou.com;

        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Server $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

 	location /item {
        	# 先找本地
        	root html;
        	if (!-f $request_filename) { #请求的文件不存在，就反向代理
            	proxy_pass http://192.168.1.109:8084;
            	break;
        }
    }

 	location / {
		proxy_pass http://192.168.1.109:9002;
		proxy_connect_timeout 600;
		proxy_read_timeout 600;
      }
    }
	 server {
        listen       80;
        server_name  manage.leyou.com;

        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Server $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        location / {
			proxy_pass http://192.168.1.109:9001;
			proxy_connect_timeout 600;
			proxy_read_timeout 600;
        }
    }
	server {
        listen       80;
        server_name  api.leyou.com;

        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Server $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 	  proxy_set_header Host $host;	   

	  location /api/upload {			
			rewrite "^/(.*)$" /zuul/$1; 
        }

        location / {
			proxy_pass http://192.168.1.109:10010;
			proxy_connect_timeout 600;
			proxy_read_timeout 600;
        }
    }

	server {
        listen       80;
        server_name  image.leyou.com;

    	# 监听域名中带有group的，交给FastDFS模块处理
        location ~/group([0-9])/ {
            ngx_fastdfs_module;
        }

        location / {
            root   /leyou/static;
            index  index.html index.htm;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
        
    }		
			
	server {
		
		listen       9001;
		server_name  manage.leyou.com;
		
		location / {
			proxy_pass http://192.168.1.109:9001;
			proxy_connect_timeout 600;
			proxy_read_timeout 600;
			
			proxy_http_version 1.1;
			proxy_set_header Upgrade $http_upgrade;
			proxy_set_header Connection "upgrade";
		}

	}
	include vhost/*.conf;
}

```
之后，启动nginx，然后就可以用域名访问后台管理系统：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190522224350412.png?)
其中间流程为：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190523101323440.png?)
1. 浏览器准备发起请求，访问http://mamage.leyou.com，但需要进行域名解析
2. 优先进行本地域名解析，因为我们修改了hosts，所以解析成功，得到地址：192.168.184.130（虚拟机ip）
3. 请求被发往解析得到的ip，并且默认使用80端口：http://192.168.184.130:80虚拟机的nginx一直监听80端口，因此捕获这个请求
4. nginx中配置了反向代理规则，将manage.leyou.com代理到192.168.1.109:9001，因此请求被转发
5. 后台系统的webpack server监听的端口是9001，得到请求并处理，完成后将响应返回到nginx
6. nginx将得到的结果返回到浏览器

