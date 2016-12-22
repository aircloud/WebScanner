package org.xt.webhm;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;

public class Request {

    private URL url;
    private HttpURLConnection con;
    private Proxy proxy = null;

    /**
     * 构造方法
     */
    public Request(String url) {
        try{
            this.url= new URL(url);
            this.init();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param url URL 信息
     * @param proxy 代理
     */
    public Request(String url, Proxy proxy) {
        try{
            this.url= new URL(url);
            this.proxy= proxy;
            this.init();
        } catch(MalformedURLException e) {
//			e.printStackTrace();
        }
    }

    public Request(URL url) {
        this.url= url;
        this.init();
    }

    public URL getUrl() {
        return url;
    }

    public HttpURLConnection getCon() {
        return con;
    }

    /**
     * 初始化 HTTP 信息
     */
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

    /**
     * 得到Response
     *
     * @return
     */
    public Response getResponse() {
        try{
            this.con.getInputStream();// 发起请求
        } catch (IOException e) {
//			e.printStackTrace();
        }

        Response res = new Response(this.con);
        return res;
    }

    /**
     * 设置请求方法
     *
     * @param method
     */
    public void setMethod(String method) {
        try{
            this.con.setRequestMethod(method);
        } catch (ProtocolException e){
            e.printStackTrace();
        }
    }

    /**
     * 设置请求头
     *
     * @param h
     *           头
     * @param v
     *           值
     */
    public Request setHeader(String h, String v) {

        this.con.addRequestProperty(h,v);
        return this;
    }

    /**
     *设置请求头内容
     *
     * @param data
     */
    public void setData(String data) {
        this.con.setDoOutput(true);
        OutputStream os = null;
        try{
            os= this.con.getOutputStream();
            os.write(data.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**
     * 设置是否执行302跳转
     * @param set
     */
    @SuppressWarnings("static-access")
    public void setInstanceFollowRedirects (boolean flag) {
        this.con.setInstanceFollowRedirects (flag);
    }
}
