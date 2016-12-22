import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Refresh")

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
        response.setIntHeader("Refresh", 5);
        // 设置响应内容类型
        response.setContentType("text/html;charset=UTF-8");
       
        //使用默认时区和语言环境获得一个日历  
        Calendar cale = Calendar.getInstance();  
        //将Calendar类型转换成Date类型  
        Date tasktime=cale.getTime();  
        //设置日期输出的格式  
        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        //格式化输出  
        String nowTime = df.format(tasktime);
        PrintWriter out = response.getWriter();
        String title = "自动刷新 Header 设置 - 菜鸟教程实例";
        String docType =
        "<!DOCTYPE html>\n";
        out.println(docType +
          "<html>\n" +
          "<head><title>" + title + "</title></head>\n"+
          "<body bgcolor=\"#f0f0f0\">\n" +
          "<h1 align=\"center\">" + title + "</h1>\n" +
          "<p>当前时间是：" + nowTime + "</p>\n");
  }
  
  public void destroy()
  {
      // 什么也不做
  }
}