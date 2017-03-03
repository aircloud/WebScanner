## 基于java的端口扫描&目录爆破器
相关实现如果要在实际环境中使用，请用Zenmap或者DirBuster等比较成熟的工具，本文档写的非常详细，适合想自己尝试制作的同学参考，另外也供自己事后回顾。

*Email:networknxt@gmail.com*


>写在前面：文档中所涉及到的代码和技术细节如果没有特殊声明，均为本人原创整理，遵循“署名-相同方式共享4.0国际协议”。          
>2.个人认为这是一个具有一定广度的初中级项目，总共代码量约为1400行, 所有代码均开源至GitHub,文章中使用的测试IP地址均为样例,请勿利用本文成果进行进一步有危害的操作.   
>3.关于上线测试，目前仍在配置，会第一时间在GitHub上进行同步(考虑网速原因，目前搭建的新加坡AWS使用体验较差，因此还在进行考虑)  


Key words:   **java**、**servlet**、**websocket**、**Tomcat 8**、**nginx**、 **javascript**


**目录**

* 0.软件背景
* 1.软件功能
* 2.技术架构
* 3.实现细节
* 4.心得体会
* 附录一：找寻ssh端口初步探究


---

### 软件背景

* 端口扫描   
实质上，端口扫描包括向每个端口发送消息，一次只发送一个消息。接收到的回应类型表示是否在使用该端口并且可由此探寻弱点。
* 目录爆破   
目录爆破指的是对一个网站或者服务器进行持续更换子目录进行请求，从而判断出该网站/服务器的一些非公开目录或者资源目录，之后进行信息安全的排查或者进一步攻击。

笔者并非是信息安全专业的学生，也没有上过信息安全方面的专业课程，但是业余对javascript/nodejs/go等语言进行的前端/服务端开发感兴趣并有所学习，而网络安全是大多数web前端/服务端开发程序员特别是新手程序员(比如我)比较薄弱的一环，所以需要对这方面的知识进行学习。

端口扫描+定向目录爆破是网络攻击中比较常用的一个基础步骤，目的是找到这台机器的潜在可攻击点或者资源获取点，而同时也是网站运营者进行自我排查的一个工具。现在我们大多数使用的工具都是本地工具(zenmap\dirbuster等)，配置起来较为麻烦，而笔者希望提供这样线上一个小工具，方便实用。

目前我开发的这个工具同zenmap等专业工具还有非常大的差距，而这也是自己短时间内难以赶超的，当然，对于java初学者来说，从中学习、理解、收获，重视过程，应当才是最重要的。


### 1.软件功能



笔者实现了一个基于web和tomcat的端口扫描和目录爆破器，主要界面如下图:

![](https://www.10000h.top/images/java1.png)

* 端口扫描   
端口扫面支持以下几种选项功能之一:
	* 选择所有端口进行扫描
	* 选择高可能性端口进行扫描(包括以下端口:21, 22, 23, 25, 26, 69, 80, 110, 143,443, 465, 995, 1080, 1158, 1433, 1521, 2100, 3128, 3306, 3389,7001, 8080, 8081, 9080, 9090,43958,50000)	
	* 自定义扫描端口的起始端口和结束端口
	* 支持实时进度条
	

端口扫描效果:

![](https://www.10000h.top/images/java2.png)

端口扫描实时进度(蓝色发光进度条):

![](https://www.10000h.top/images/java3.png)

端口扫描支持域名(注意:如果前面加了http或者https实际上是默认了80或443端口,这样端口扫描是没有意义的):

![](https://www.10000h.top/images/java5.png)


* 目录爆破
	* 系统内置了多种不同的字典文件，可以选择一个字典文件对目录进行爆破
	* 暂时不支持用户自定义上传字典文件
	* 支持实时进度条
	* 支持代理


(注:字典大小不同，字典值从几万到几百万不等，一般而言，目录爆破需要经历较长时间，并且新找到目录的效率会随着进度而降低)

选择字典:

![](https://www.10000h.top/images/java6.png)

目录爆破：

![](https://www.10000h.top/images/java7.png)



### 2.技术架构

技术架构如下图:

![](https://www.10000h.top/images/java8.png)

采用nginx入口配置静态文件(可以相对提高速度),并且配置文件过期时间，后端采用Tomcat下的java servlet，Tomcat 8完全支持websocket，因此实现较为方便。

* 编程思想   
采用了前后端分离的编程思想，前后端通过websocket进行通信。
对于后端java代码的组织，采用了**函数式编程**的思想，基于java面向对象的特征，实现了不同模块之间的**低耦合，高聚合**。

### 3.实现细节

#### 环境配置

基于上述技术架构，我们首先实现环境配置，这里以centOS配置为例(笔者本机采用的macOS系统，配置过程类似centOS、没有对windows进行研究)

首先，在第一次使用一个centOS服务器的时候，最好先对yum进行一下升级:

```
yum update
```

这里默认采用的是非root账户进行登陆，实际上，我们不应该给root账户提供登陆权限。   
在安装tomcat前，需要先安装java，一行命令搞定：

```
 sudo yum install java-1.7.0-openjdk-devel
```

之后，我们可以安装tomcat，镜像源可以在tomcat的<a href="https://tomcat.apache.org/download-80.cgi">官网</a>进行找到(https://tomcat.apache.org/download-80.cgi).

这里我的安装命令是：

```
[ec2-user@ip-172-31-27-121 ~]$ cd ~
[ec2-user@ip-172-31-27-121 ~]$ wget http://mirrors.tuna.tsinghua.edu.cn/apache/tomcat/tomcat-8/v8.5.9/bin/apache-tomcat-8.5.9.tar.gz
```

然后对内容进行解压，这里用到一点点Linux的基础知识：

```
[ec2-user@ip-172-31-27-121 ~]$ sudo mkdir /opt/tomcat
[ec2-user@ip-172-31-27-121 ~]$ sudo tar xvf apache-tomcat-8.5.9.tar.gz -C /opt/tomcat --strip-components=1
```

然后进行一些简单配置，这一步实际上是可选的:

```
[ec2-user@ip-172-31-27-121 ~]$ cd /opt/tomcat
[ec2-user@ip-172-31-27-121 tomcat]$ sudo chmod g+rwx conf
[ec2-user@ip-172-31-27-121 tomcat]$ sudo chown ec2-user webapps/ work/ temp/ logs/
```

到这里就安装完成了，可以通过如下命令启动:

```
sudo  /opt/tomcat/bin/startup.sh
```

可以通过如下命令停止:

```
sudo  /opt/tomcat/bin/shutdown.sh
```

#### Servlet实例 与 WebSocket

Servlet 是服务 HTTP 请求并实现 javax.servlet.Servlet 接口的 Java 类。Web 应用程序开发人员通常编写 Servlet 来扩展 javax.servlet.http.HttpServlet，并实现 Servlet 接口的抽象类专门用来处理 HTTP 请求。

本项目中Servlet只承担websocket服务端和进行端口扫描和目录扫描，不涉及直接提供网页和用户交互的过程，因此我们可以不配置web.xml文件。(我这里配置了几项内容是供调试Servlet使用)

本次Servlet用到的所有文件的关系如下:

```

 └─ ChatEndpoint.class (websocket入口)
   ├─ Request.class (目录爆破请求)
   ├─ Response.class (目录爆破响应)
   └─ PortScan.class (端口扫描)
      ├─ $ScanMethod1.class (方式一)
      └─ $ScanMethod2.class (方式二) 

```

我们进行websocket编程较为简单，主要是完善几个内置函数, 这里我先列出ChatEndPoint.java的框架:

```

@ServerEndpoint(value="/chatendpoint")
public class ChatEndpoint {
    
    //公共变量部分
    
	@OnOpen
	public void start(Session session){
	  //...
	}
	
	@OnMessage
	public void process(Session session, String message){
	  //...
	}
	
	@OnClose
	public void end(Session session){
	  //...
	}
	
	@OnError
	public void error(Session session, java.lang.Throwable throwable){
		end(session);
	}
	
}

```

其中`@ServerEndpoint(value="/chatendpoint")`这一行是路由功能，至关重要，自己在一开始的时候没有理解。

另外，自己参考Stack Overflow上的答案, 完善了一个boardcast函数，虽然后来自己并没有用到这个函数(本项目并没有这个函数的使用场景):

```
void broadcast(String message){
		RemoteEndpoint.Basic remote = null;
		Set<Map.Entry<String,Session>> set = sessionMap.entrySet();
		for(Map.Entry<String,Session> i: set){
			remote = i.getValue().getBasicRemote();
			try {
				remote.sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
```


我们通过一些内置库函数可以获取到websocket通信时前端请求的信息，之后我们将要发送的信息组织成有分隔符的字符串，采用`sendText`发送。

这时候容易出现这样一个错误:

>The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method

经过查阅，这个错误是在多线程情况下同时调用websocket通信造成的，对于该错误的出现，可以有以下解决办法:


```
String websocketsesssion_id = map_id.get(username);
WebSocketSession wss = map_users.get(websocketsesssion_id);
synchronized(wss) {
 wss.sendMessage(new TextMessage(new java.util.Date()));
}
```

当然，最好就是不要出现多线程同时请求的情况，所以后来自己对代码进行了更改，避免出现这种情况。

#### 实现端口扫描

我们通过建立socket判断响应信息从而判断端口的开放状态。

另外，自己在尝试判断端口具体提供什么服务的时候，虽然最终没有找到很好的解决方案，但是却尝试出了一种magic方法，详细见附录一。

虽然自己写了两个子类，但是本质上实现端口扫描采用的是一种方法: **多线程线程池**，采用**多线程线程池**的方法有如下好处:

* 网络IO通常有较高的等待延迟，采用多线程的方式可以防止资源浪费，极大提高效率。
* 采用线程池，意味着自己可以控制线程的数量，防止线程过多导致程序异常或反而产生较大消耗。

具体线程池的实现代码关键部分如下:

```
 public void scanLargePorts(String ip, Set<Integer> portSet,
                               int threadNumber, int timeout,int totalcout) {

        this.totalCount =totalcout;
        Count=0;

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < threadNumber; i++) {
            ScanMethod2 scanMethod2 = new ScanMethod2(ip, portSet,
                    threadNumber, i, timeout);
            threadPool.execute(scanMethod2);
        }
        threadPool.shutdown();
        while (true) {
            try {
                myRemote.sendText("percentage|"+percentage+"|");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (threadPool.isTerminated()) {
                try {
                    myRemote.sendText("扫描结束");
                    this.totalCount=0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
```

#### 实现目录爆破

在实现目录爆破的过程中，我使用了java提供的Proxy(目前的测试代码还有些不完善)。

* Proxy

从JDK1.5开始，Java在java.net包下提供了Proxy和ProxySelector两个类，其中Proxy代表一个代理服务器，可以在打开URLConnection连接时指定所用的Proxy实例，从而达到突破自身IP限制，对外隐藏自身IP地址等目的。

实现一个代理的创建过程很简单:

```
Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);  
```

有了Proxy，我们就可以通过Proxy去初始化http信息

```
private void init() {
        try{

            if(this.proxy == null) {

                this.con= (HttpURLConnection) url.openConnection();
            } else {
                this.con= (HttpURLConnection) this.url
                        .openConnection(this.proxy);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
}
```

之后发送请求，然后可以通过如下方法来获取响应状态码和响应体:

```
//响应体
public String getBody(String charset) {
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(this.con
                    .getInputStream(), charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (String temp = buf.readLine(); temp != null; temp = buf
                    .readLine()) {
                sb.append(temp);
                sb.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
//状态码
public int getResponseCode() {
        int temp = -1 ;
        try {
            temp = this.con.getResponseCode() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp ;
    }
```

实际上，要想基本的实现一个请求是十分容易的，但是要考虑很多细节以及让代码变得完善是不容易的，这里自己查阅了Google、Stack Overflow还有GitHub，最后实现了一个较为完善的版本(request.java&response.java)

#### 前端发起响应和请求

前端(html/css/js)不作为本次内容的重点，这里仅仅重点提示javascript创建web socket的关键代码:

```
 function connectEndpoint(){
        window.WebSocket = window.WebSocket || window.MozWebSocket;
        if (!window.WebSocket){
            alert("WebSocket not supported by this browser");
            return;
        }

        ws = new WebSocket(wsuri);
        ws.onmessage = function(evt) {
            //...            
        };

        ws.onclose = function(evt) {
            //alert("close");
            //...
        };

        ws.onopen = function(evt) {
            //alert("open");
            //...
        };
    }
```

### 4.心得体会

这次大作业给了自己一个探索的契机，而最后也相当于有了一点点小的收获。

但是作为一个计算机专业学生，感觉一学期从头到位一直在忙，所谓大作业，留下的时间实际上也没有多到哪去，实际上期末有很多大作业，而我又是一个希望自己能够走出舒适区，多去接触接触自己不是十分熟悉额领域，因此更加没有足够的时间做出足够完善的内容，诶。

希望寒假有时间能继续探究做好这个项目，虽然应该是没有时间的。

但是我还是希望寒假有时间......

[另外其实相对于其他老师,楼sir讲的干货还是蛮多的,虽然有的课的确给人压力山大...但仍要🙏感谢老师]

---

### 附录一：找寻ssh端口初步探究

目前没有找到用java找寻ssh端口的比较好的方式，但是下面提供一个比较简单粗暴的方法(实际上就是用java去调用SHELL,在centOS 7上是可以的，但没有办法跨平台):

```
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * Created by nicholas on 11/19/2016.
 */
public class GetProcessInfo {

    private static final String[] SHELL = {"sh", "-c", "lsof -Pnl +M -i4 | grep sshd"};

    private static final String KEY = "(LISTEN)";

    private static String runCommand(String[] shell) throws IOException, InterruptedException {
        String result = "";
        Process pos = Runtime.getRuntime().exec(shell);
        pos.waitFor();
        InputStreamReader ir = new InputStreamReader(pos.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        String ln = "";
        while ((ln = input.readLine()) != null) {
            String temp = ln.toString();
            if (temp.contains(KEY)) {
                result = temp;
            }
        }
        input.close();
        ir.close();
        return result;
    }

    public static int getPort() throws IOException, InterruptedException {
        String result = runCommand(SHELL);
        if ("".equals(result)) {
            System.out.println("get sshd services fail....");
            return 0;
        }
        String[] split = result.split(":")[1].split(" ");
        int port = Integer.valueOf(split[0]);
        return port;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("sshd port = " + getPort());
    }
}

```
