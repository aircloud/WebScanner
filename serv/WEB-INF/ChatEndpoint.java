package org.xt.webhm;

//import org.apache.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import org.xt.webhm.*;

import java.text.DecimalFormat;


@ServerEndpoint(value="/chatendpoint")
public class ChatEndpoint {
    //static final Logger logger = Logger.getLogger(ChatEndpoint.class);
	static Map<String,Session> sessionMap = new Hashtable<String,Session>();

	public String[] fileurl = {"./DirBuster-0.12/directory-list-lowercase-2.3-small.txt",
			"./DirBuster-0.12/directory-list-2.3-small.txt",
			"./DirBuster-0.12/directory-list-lowercase-2.3-medium.txt",
			"./DirBuster-0.12/directory-list-2.3-medium.txt",
			"./DirBuster-0.12/directory-list-lowercase-2.3-big.txt",
			"./DirBuster-0.12/directory-list-2.3-big.txt"};

	public int[] filecount = {81640,87640,207640,220550,1185250,1273830};

	@OnOpen
	public void start(Session session){
		System.out.println("Guest"+session.getId()+" join");
		sessionMap.put(session.getId(), session);
        RemoteEndpoint.Basic remote0 = session.getBasicRemote();
        try {
            remote0.sendText(" the scan is ready(the processing may use a long time,please wait)");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@OnMessage
	public void process(Session session, String message){
		System.out.println(session.getId()+" say: " + message);
		RemoteEndpoint.Basic remote0 = session.getBasicRemote();

		String[] resultMessage = message.split(",");

		DecimalFormat df = new DecimalFormat("0.00");
		int type = Integer.parseInt(resultMessage[0]);

		int count = 0,totalcount;
		String percentage;

        File directory = new File("");//设定为当前文件夹
        try{
            System.out.println("！@！@##！@#！@#！@#！@#！！@#");//获取标准的路径
            System.out.println(directory.getCanonicalPath());//获取标准的路径
            System.out.println(directory.getAbsolutePath());//获取绝对路径
            System.out.println("！@！@##！@#！@#！@#！@#！！@#");//获取标准的路径

        }catch(IOException e){}

        if(type==2){

			int fileindex = Integer.parseInt(resultMessage[2]);

			File file = new File(fileurl[fileindex]);
			totalcount = filecount[fileindex];
			BufferedReader reader = null;
			try {
//				System.out.println("以行为单位读取文件内容，一次读一整行：");
				reader = new BufferedReader(new FileReader(file));
				String tempString = null;

				int line = 1;
				// 一次读入一行，直到读入null为文件结束
				while ((tempString = reader.readLine()) != null) {
					count=count+1;
                    percentage = df.format((((double)count/(double)totalcount)));
                    if(count%10==0){
                        try {
                            remote0.sendText("percentage|"+percentage+"|");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
					}
					// 显示行号
					if(tempString.indexOf("#")<0){
						//增加容错性，判断结尾的符号的属性
						if(resultMessage[1].charAt(resultMessage[1].length()-1)!='/')resultMessage[1]=resultMessage[1]+"/";
						Request r = new Request(resultMessage[1]+tempString);

						r.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36").setMethod("HEAD");

						r.setInstanceFollowRedirects(false);

						Response  ps =	r.getResponse();
						if(ps.getResponseCode()!=404&&ps.getResponseCode()!=302&&ps.getResponseCode()!=-1) {
							try {
								remote0.sendText("percentage|"+percentage+"|"+resultMessage[1] + tempString + "  " + "status:" + ps.getResponseCode());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if(line%500==0)
							try {
								remote0.sendText("percentage|"+percentage+"|the number of tests:"+line);
							} catch (IOException e) {
								e.printStackTrace();
							}
					}
					line++;
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e1) {
					}
				}
			}
			try {
				remote0.sendText("扫描结束");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		else if(type==1){

			String tempIpAddress = resultMessage[1];
			PortScan myPorScan = new PortScan(remote0);

			if(Integer.parseInt(resultMessage[4])==1){

                totalcount = 30;

				Set<Integer> portSet = new LinkedHashSet<Integer>();
				Integer[] ports = new Integer[] { 21, 22, 23, 25, 26, 69, 80, 110, 143,
						443, 465, 995, 1080, 1158, 1433, 1521, 2100, 3128, 3306, 3389,
						7001, 8080, 8081, 8090, 9080, 9090, 43958, 50000,60000,50002};
				portSet.addAll(Arrays.asList(ports));
				myPorScan.scanLargePorts(tempIpAddress, portSet, 15, 800,totalcount);
			}

			else{

                totalcount = Integer.parseInt(resultMessage[3]) - Integer.parseInt(resultMessage[2]);

				int begin = Integer.parseInt(resultMessage[2]);
				int end = Integer.parseInt(resultMessage[3]);
				myPorScan.scanLargePorts(tempIpAddress, begin, end, 100,600,totalcount);

			}

		}

        // broadcast("Guest"+session.getId()+" [say]: "+message);
	}

	//socket测试:先测试socket通信
	@OnClose
	public void end(Session session){
//		System.out.println("Guest"+session.getId()+" out.");
		sessionMap.remove(session.getId());
//		broadcast("Guest"+session.getId()+ " out.");
	}

	//socket测试:先测试socket通信
	@OnError
	public void error(Session session, java.lang.Throwable throwable){
//		System.err.println("Guest" + session.getId() + " error: " + throwable);
		end(session);
	}

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

}