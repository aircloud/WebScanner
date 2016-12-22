## java+tomcat 服务端开发进阶之路

作为一个用过node/php/python做后端开发的前端程序员，从来没接触过java，本来就不想弄java了，这学期有一门课要用java写东西，哭，只好含着泪学java配环境。

### 基本配置

自己下了tomcat，配置地址在`/usr/local/tomcat`,启动方式和停止方式：

```
/usr/local/tomcat/bin/startup.sh
/usr/local/tomcat/bin/shutdown.sh
```

### Servlet 实例

Servlet 是服务 HTTP 请求并实现 javax.servlet.Servlet 接口的 Java 类。Web 应用程序开发人员通常编写 Servlet 来扩展 javax.servlet.http.HttpServlet，并实现 Servlet 接口的抽象类专门用来处理 HTTP 请求。

这里有一个从网上搞到的helloworld级别的代码：

```
// 导入必需的 java 库
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 扩展 HttpServlet 类
public class HelloWorld extends HttpServlet {
 
  private String message;

  public void init() throws ServletException
  {
      // 执行必需的初始化
      message = "Hello World";
  }

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // 设置响应内容类型
      response.setContentType("text/html");

      // 实际的逻辑是在这里
      PrintWriter out = response.getWriter();
      out.println("<h1>" + message + "</h1>");
  }
  
  public void destroy()
  {
      // 什么也不做
  }
}
```

然后javac得到class文件，默认情况下，Servlet 应用程序位于路径 <Tomcat-installation-directory>/webapps/ROOT 下，且**<u>类文件放在 <Tomcat-installation-directory>/webapps/ROOT/WEB-INF/classes</u>** 中。


如果有一个完全合格的类名称 com.myorg.MyServlet，那么这个 Servlet 类必须位于 WEB-INF/classes/com/myorg/MyServlet.class 中。
现在，让我们**把 HelloWorld.class 复制到 <Tomcat-installation-directory>/webapps/ROOT/WEB-INF/classes 中**，并在位于 <Tomcat-installation-directory>/webapps/ROOT/WEB-INF/ 的 web.xml 文件中创建以下条目：


```
<web-app>      
    <servlet>
        <servlet-name>HelloWorld</servlet-name>
        <servlet-class>HelloWorld</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>HelloWorld</servlet-name>
        <url-pattern>/HelloWorld</url-pattern>
    </servlet-mapping>
</web-app> 
```

上面的条目要被创建在 web.xml 文件中的 <web-app>...</web-app> 标签内。在该文件中可能已经有各种可用的条目，但不要在意。   
到这里，基本上已经完成了，现在启动 tomcat 服务器，最后在浏览器的地址栏中输入 `http://localhost:8080/serv/HelloWorld`。

终于出现了久违的hello world，但是自己现在还不知道如何进行java后端开发，Servlet也不是很清楚，还要继续努力学习。

### 第二次总结

现在已经基本上把作业的初版完成了，之前并不是很明白servlet怎么用，现在大概有些明白了，现在有几点注意事项自己写在下面：

* 如果是用websocket编程，直接把javac编译好的class文件放在classes文件夹下面就可以了，不需要在web.xml中再写一些什么别的东西...最好是向cd WE这样直接指定classes目录为目标目录：

```
javac Response.java Request.java ChatEndpoint.java PortScan.java -d ./classes/

```
* 上面的代码也展示了同时编译多个class文件如何编译，另外，自己如果在编译的时候写class中的class，javac编译的时候也会一并处理好。
* 再多线程的处理中，可以通过传递参数到构造函数中的方式来进行参数传递，传递的参数甚至是一个复杂形式的对象实例，这个自己一开始多虑了，详见自己端口扫描部分socket的反馈方式。
* 总之，这是自己第一次用tomcat、用java，写的代码肯定也很初级，真是感觉一入java深似海啊，这个期末之前再给它优化一下，增强下用户体验，应该就没问题了，这段时间先写别的大作业...


### 第三次总结

出现错误

The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method

这种错误的造成原因就是多个线程同时试图使用socket,stackoverflow上面有一些解决方案，比如

```
String websocketsesssion_id = map_id.get(username);
WebSocketSession wss = map_users.get(websocketsesssion_id);
synchronized(wss) {
 wss.sendMessage(new TextMessage(new java.util.Date()));
}
```

