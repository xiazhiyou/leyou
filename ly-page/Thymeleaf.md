Thymeleaf是用来开发Web和独立环境项目的现代服务器端Java模板引擎。<br>

Thymeleaf的主要目标是为我们的开发工作流程带来优雅的*自然模板* - HTML（前后端人员都可以运行）。可以在直接浏览器中正确显示，并且可以作为静态原型，从而在开发团队中实现更强大的协作。

Spring官方支持的服务的渲染模板中，并不包含jsp。而是Thymeleaf和Freemarker等，而Thymeleaf与SpringMVC的视图技术，及SpringBoot的自动化配置集成非常完美。

**Thymeleaf作为一个模板引擎，当用户的请求到达后台时，Thymeleaf会把请求接收到，然后去查询数据，查完数据以后，再去拿到模板item.html，再把model中的数据一一渲染到item.html中，渲染完成以后，Thymeleaf再把渲染得到的html内容写到用户的respose中，自然也就返回到了浏览器。**

特点：

- 动静结合：Thymeleaf 在有网络和无网络的环境下皆可运行，即它可以让美工在浏览器查看页面的静态效果，也可以让程序员在服务器查看带数据的动态页面效果。这是由于它支持 html 原型，然后在 html 标签里增加额外的属性来达到模板+数据的展示方式。浏览器解释 html 时会忽略未定义的标签属性，所以 thymeleaf 的模板可以静态地运行；当有数据返回到页面时，Thymeleaf 标签会动态地替换掉静态内容，使页面动态显示。
- 开箱即用：它提供标准和spring标准两种方言，可以直接套用模板实现JSTL、 OGNL表达式效果，避免每天套模板、该jstl、改标签的困扰。同时开发人员也可以扩展和创建自定义的方言。
- 多方言支持：Thymeleaf 提供spring标准方言和一个与 SpringMVC 完美集成的可选模块，可以快速的实现表单绑定、属性编辑器、国际化等功能。
- 与SpringBoot完美整合，SpringBoot提供了Thymeleaf的默认配置，并且为Thymeleaf设置了视图解析器，我们可以像以前操作jsp一样来操作Thymeleaf。代码几乎没有任何区别，就是在模板语法上有区别。
