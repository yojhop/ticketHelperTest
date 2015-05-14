import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;


public class MainClass {
	private static Logger logger = Logger.getLogger(MainClass.class); 
	private static List<Proxy> proxies=new ArrayList<Proxy>();
	private static ArrayList<goodRound> goods=new ArrayList<goodRound>();
	private static ArrayList<User> users=new ArrayList<User>();
	static DefaultHttpClient client = new DefaultHttpClient();
	//replace regex (中国.*\r\n *.[0-9]* *) ->(\r\n)
	//\\u->&#x
	public static void main(String args[]){
/*		 HttpGet  getMethod = new HttpGet("http://pachong.org/");
		 try {
			HttpResponse response = client.execute(getMethod)  ;
			if (response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK) {  
                System.err.println("Method failed: "  
                        + response.getStatusLine().getStatusCode());  
            }  
            // 读取内容 
            String content=inputStreamToString(response.getEntity().getContent());*/
			
            loadProxies();
            loadGoods();
            loadUsers();
            for(int i=0;i<goods.size();i++){
            	for(int j=0;j<users.size();j++){
            		if(j==0&&i==0){
            			BuyTickets bt=new BuyTickets(new Proxy(),goods.get(0),false,users.get(0));
        	            bt.start();
            		}
            		else{
            			Proxy p=getProxy();
                    	if(p!=null){
                    		BuyTickets bt=new BuyTickets(p,goods.get(i),true,users.get(j));
                    		bt.start();
                    	}
            		}
            	}
            }
/*		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	private static void loadGoods(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("conf/goods.properties")));
			String str;
			try {
				while((str=br.readLine())!=null){
					String[] parts=str.trim().split(",");
					if(parts!=null&&parts.length==2){
						if(parts[0]!=null&&parts[0].length()>0&&parts[1]!=null&&parts[1].length()>0){
							goodRound g=new goodRound(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),"");
							goods.add(g);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void proxyFailed(Proxy p){
		synchronized(proxies){
			for(Proxy proxy:proxies){
				if(proxy.equals(p)){
					proxy.setLocated(false);
					p.desWeight();
					break;
				}
			}
		}
	}
	public static void proxyEnd(Proxy p,int sucessTime){
		synchronized(proxies){
			for(Proxy proxy:proxies){
				if(proxy.equals(p)){
					proxy.setLocated(false);
					p.insWeight(sucessTime);
					p.desWeight();
					break;
				}
			}
		}
	}
	public static Proxy getProxy(){
		synchronized(proxies){
			Collections.sort(proxies);
			for(Proxy proxy:proxies){
				if(!proxy.isLocated()){
					proxy.setLocated(true);
					return proxy;
				}
			}
		}
		return null;
	}
	public static void returnProxy(Proxy p){
		synchronized(proxies){
			for(Proxy proxy:proxies){
				if(proxy.equals(p)){
					proxy.setLocated(false);
					break;
				}
			}
		}
	}
	private static String inputStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    // Read response until the end
	    try {
			while ((line = rd.readLine()) != null) { 
			    total.append(line); 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("",e);
		}
	    
	    // Return full string
	    return total.toString();
	}
	public static void loadUsers(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("conf/users.properties")));
			String str;
			try {
				while((str=br.readLine())!=null){
					String[] parts=str.trim().split(",");
					if(parts!=null&&parts.length==2){
						if(parts[0]!=null&&parts[0].length()>0&&parts[1]!=null&&parts[1].length()>0){
							User u=new User(parts[0],parts[1]);
							users.add(u);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void loadProxies(){
		try {
			logger.info("Loading proxies started at "+getNow() );
			BufferedReader br = new BufferedReader(new FileReader(new File("conf/proxy.properties")));
			String str;
			try {
				while((str=br.readLine())!=null){
					String[] parts=str.trim().split(" |\t");
					if(parts!=null&&parts.length>=2){
						if(parts[0]!=null&&parts[0].length()>0&&parts[1]!=null&&parts[1].length()>0){
							Proxy p = new Proxy();
							p.setIp(parts[0]);
							p.setPort(Integer.parseInt(parts[1]));
							p.setLocated(false);
							//if(isProxyGood(p)){
								proxies.add(p);
							//}
							/*else{
								logger.info("Proxy "+p.getIp()+" is not good, will not be used at "+getNow());
							}*/
						}
					}
				}
				//Collections.sort(proxies);
				logger.info("Loading proxies ended at "+getNow() );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public  static String getNow(){
		Date d= new Date();
		return d.toLocaleString();
	}
	private static boolean isProxyGood(Proxy p){
		logger.info("Testing "+p.getIp()+" at "+getNow() );
		HttpHost proxy = new HttpHost(p.getIp(), p.getPort());
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        HttpGet getMethod = new HttpGet("http://shop.snh48.com");  
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
        getMethod.setConfig(requestConfig);
        try {
        	long reqTime=(new Date()).getTime();
			HttpResponse response = client.execute(getMethod)  ;
			long resTime=(new Date()).getTime();
			p.setRrt(resTime-reqTime);
			/*String content=inputStreamToString(response.getEntity().getContent());
			if(content.contains("网络信号弱")){
				return false;
			}*/
			EntityUtils.consumeQuietly(response.getEntity());
			logger.info("Proxy "+p.getIp()+" is good at "+getNow());
		} catch (ClientProtocolException e) {
			logger.error("", e);
			return false;
		}
        catch(IllegalStateException ie){
        	logger.error("", ie);
        	logger.info("Will reset httpclient");
        	client = new DefaultHttpClient();
        	return isProxyGood(p);
        }
        catch (IOException e) {
			logger.error("", e);
			return false;
		}
        return true;
	}

}
