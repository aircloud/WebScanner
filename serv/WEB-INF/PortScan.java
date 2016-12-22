package org.xt.webhm;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.text.DecimalFormat;


public class PortScan {

    public RemoteEndpoint.Basic myRemote;

    public PortScan(RemoteEndpoint.Basic remote0){
        this.myRemote = remote0;
    }

    public static int totalCount,Count = 0;
    public static DecimalFormat df = new DecimalFormat("0.00");
    public static String percentage;

    /**
     * 多线程扫描目标主机一个段的端口开放情况
     *
     * @param ip
     *            待扫描IP或域名,eg:10.214.47.99 www.baidu.com
     * @param startPort
     *            起始端口
     * @param endPort
     *            结束端口
     * @param threadNumber
     *            线程数
     * @param timeout
     *            连接超时时间
     * */
    public void scanLargePorts(String ip, int startPort, int endPort,
                               int threadNumber, int timeout,int totalcout) {

        this.totalCount =totalcout;
        Count=0;

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < threadNumber; i++) {
            ScanMethod1 scanMethod1 = new ScanMethod1(ip, startPort, endPort,
                    threadNumber, i, timeout);
            threadPool.execute(scanMethod1);
        }
        threadPool.shutdown();
        // 每秒中查看一次是否已经扫描结束

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

    /**
     * 多线程扫描目标主机指定Set端口集合的开放情况
     *
     * @param ip
     *            待扫描IP或域名,eg:10.214.47.99 www.baidu.com
     * @param portSet
     *            待扫描的端口的Set集合
     * @param threadNumber
     *            线程数
     * @param timeout
     *            连接超时时间
     * */
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

    /**
     * 扫描方式一：针对起始结束端口，进行逐个扫描
     *
     * */
    class ScanMethod1 implements Runnable {
        private String ip; // 目标IP
        private int startPort, endPort, threadNumber, serial, timeout; // 起始和结束端口，线程数，这是第几个线程，超时时间

        /**
         * 初始化
         *
         * @param ip
         *            待扫描IP或域名
         * @param startPort
         *            起始端口
         * @param endPort
         *            结束端口
         * @param threadNumber
         *            线程数
         * @param serial
         *            标记是第几个线程
         * @param timeout
         *            连接超时时间
         * */
        public ScanMethod1(String ip, int startPort, int endPort,
                           int threadNumber, int serial, int timeout) {
            this.ip = ip;
            this.startPort = startPort;
            this.endPort = endPort;
            this.threadNumber = threadNumber;
            this.serial = serial;
            this.timeout = timeout;
        }

        public void run() {
            int port = 0;
            try {
                InetAddress address = InetAddress.getByName(ip);
                Socket socket;
                SocketAddress socketAddress;
                for (port = startPort + serial; port <= endPort; port += threadNumber) {
                    socket = new Socket();
                    socketAddress = new InetSocketAddress(address, port);
                    try {

                        Count+=1;
                        percentage = df.format((((double)Count/(double)totalCount)));
//                        try {
//                            myRemote.sendText("percentage"+percentage);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        System.out.println("___________________________________");
                        System.out.println("attention1 percentage"+percentage+"|"+Count+"|"+totalCount);
                        System.out.println("___________________________________");

                        socket.connect(socketAddress, timeout); // 超时时间
                        socket.close();
                        try {
                            System.out.println("percentage|"+percentage+"|端口 " + port + " ：开放");
                            myRemote.sendText("percentage|"+percentage+"|端口 " + port + " ：开放");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        // System.out.println("端口 " + port + " ：关闭");
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 扫描方式二：针对一个待扫描的端口的Set集合进行扫描
     *
     * */
    private class ScanMethod2 implements Runnable {
        private String ip; // 目标IP
        private Set<Integer> portSet; // 待扫描的端口的Set集合
        private int threadNumber, serial, timeout; // 线程数，这是第几个线程，超时时间

        public ScanMethod2(String ip, Set<Integer> portSet, int threadNumber,
                           int serial, int timeout) {
            this.ip = ip;
            this.portSet = portSet;
            this.threadNumber = threadNumber;
            this.serial = serial;
            this.timeout = timeout;
        }

        public void run() {
            int port = 0;
            Integer[] ports = portSet.toArray(new Integer[portSet.size()]); // Set转数组
            try {
                InetAddress address = InetAddress.getByName(ip);
                Socket socket;
                SocketAddress socketAddress;
                if (ports.length < 1)
                    return;
                for (port = 0 + serial; port <= ports.length - 1; port += threadNumber) {
                    socket = new Socket();
                    socketAddress = new InetSocketAddress(address, ports[port]);

                    Count+=1;
                    percentage = df.format((((double)Count/(double)totalCount)));
                    try {

//                        try {
//                            myRemote.sendText("percentage"+percentage);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        System.out.println("___________________________________");
                        System.out.println("attention2 percentage"+percentage+"|"+Count+"|"+totalCount);
                        System.out.println("___________________________________");

                        socket.connect(socketAddress, timeout);
                        int level = socket.getTrafficClass();
                        socket.close();
                        try {
                            System.out.println("percentage|"+percentage+"|端口 " + ports[port] + " ：开放,level:"+level);
                            myRemote.sendText("percentage|"+percentage+"|端口 " + ports[port] + " ：开放,level:"+level);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {

                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }

    }
}
