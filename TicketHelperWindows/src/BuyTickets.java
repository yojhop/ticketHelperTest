import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jodd.util.Base64;
import jodd.util.URLDecoder;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;


public class BuyTickets extends Thread{
	 DefaultHttpClient client = new DefaultHttpClient();
	 HttpResponse response;
	 BufferedImage  image;
	 String username;
	 String password;
	 PrintWriter pw;
	 HttpPost loginpost;
	 Proxy proxy;
	 goodRound good;
	 boolean useProxy;
	 Proxy lastSucProxy;
	 int succeedTime=0;
	 ImageProcessor imageProcessor;
	 private static Logger logger = Logger.getLogger(BuyTickets.class); 
	 ArrayList<goodRound> goods = new  ArrayList<goodRound>();
	 public BuyTickets(){
		 try {
			pw=new PrintWriter(new FileWriter("ticket.log",true));
			
			HttpHost proxy = new HttpHost("101.226.249.237", 80);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 4000); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(pw);
			pw.flush();
		}
	 }
	 public BuyTickets(Proxy p,goodRound g,boolean usePorxy,User user){
		 try {
			 	username=user.getUsername();
			 	password=user.getPassword();
			 	this.useProxy=usePorxy;
			 	imageProcessor=new ImageProcessor();
				pw=new PrintWriter(new FileWriter("logs/"+user.getUsername()+"-"+g.getId()+".log",false),false);
				client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 4000); 
				proxy=p;
				good=g;
				if(usePorxy){
					HttpHost proxyHost = new HttpHost(p.getIp(), p.getPort());
					client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
				}
				else{
					pw.println("Starting connection without proxy.");
				}
			} catch (IOException e) {
				e.printStackTrace(pw);
				pw.flush();
				// TODO Auto-generated catch block
				//logger.error("",e);
			}
	 }
	 public void run(){
		 //waitUtil1659();
		 pw.println("started at "+getNow());
		 pw.flush();
		 boolean loginRet=login();
		 if(loginRet){
			 buyCurrentGood();
		 }
	 }
	 public boolean resetClient(){
		 pw.println("reseting client");
		 pw.flush();
		 client = new DefaultHttpClient();
		 client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 4000); 
		 if(useProxy){
			 	if(proxy!=null){
			 		HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
			 		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
			 	}
			 	else{
			 		pw.println("No more proxy available, thread will exit.");
			 		this.interrupted();
			 		return false;
			 	}
			}
			else{
				pw.println("Starting connection without proxy.");
			}
		 return login();
	 }
	public static void main(String args[]){
		Proxy p=new Proxy();
		Proxy p2=new Proxy();
		Proxy p3=new Proxy();
		ArrayList<Proxy> pList=new ArrayList<Proxy>();
		p.insWeight(1);
		p2.desWeight();
		p3.insWeight(3);
		pList.add(p);
		pList.add(p2);
		pList.add(p3);
		Collections.sort(pList);
		System.out.println();
		//bt.resetClient();
		//loadGoods();
		
		//System.out.println();
	}
	public  void buyAllGoods(){
		while(goods.size()>0){
			int addToCartRet=addToCart(goods.get(0).id,goods.get(0).count);
			if(addToCartRet==0){
				if(confirmCart()){
					goods.remove(0);
				}
			}
			else{
				if(addToCartRet==1){
					goods.remove(0);
				}
			}
			
			try{
				commonSleep();
				 pw.println("retry buy all goods at "+getNow());
				 pw.flush();
			}
			catch(IllegalStateException ie){
				ie.printStackTrace(pw);
				pw.flush();
		        resetClient();
		    }
			catch(Exception e){
				e.printStackTrace(pw);
				pw.flush();
			}
			//System.out.println("first try");
		}
	}
	public  void buyCurrentGood(){
		 while(true){
			int addToCartRet=addToCart(good.getId(),good.getCount());
			//int addToCartRet=0;
			if(addToCartRet!=4&&addToCartRet!=7){
				succeedTime++;
				lastSucProxy=proxy;
				pw.println("add to cart is not 4(can download image), setting lastSucProxy to "+proxy.getIp()+":"+proxy.getPort());
			}
			if(addToCartRet==0){
				 while(true){
					pw.println("Trying to confirm cart.");
					pw.flush();
					if(confirmCart()){
						//MainClass.returnProxy(proxy);
						//pw.println("Ticket bought sucessfully!");
						//pw.flush();
						break ;
					}
					commonSleep();
				}
			}
			else{
				if(addToCartRet==1){
					while(true){
						pw.println("Trying to confirm cart.");
						pw.flush();
						if(confirmCart()){
							//MainClass.returnProxy(proxy);
							//pw.println("Ticket bought sucessfully!");
							//pw.flush();
							break ;
						}
						commonSleep();
					}
				}
				else{
					if(addToCartRet==4){
						MainClass.proxyEnd(proxy, succeedTime);
						pw.println("Current proxy "+proxy.getIp()+" is not working, will try to use another proxy at "+ getNow());
		        		pw.flush();
						Proxy tempProxy=MainClass.getProxy();
						while(tempProxy==null){
							commonSleep();
							tempProxy=MainClass.getProxy();
						}
						succeedTime=0;
						proxy=tempProxy;
						pw.println("Trying to use new proxy "+proxy.getIp());
		        		pw.flush();
						if(!resetClient()){
							
						}
					}
					else{
						if(addToCartRet==5){
							MainClass.returnProxy(proxy);
							pw.println("Ticket bought sucessfully!");
							pw.flush();
							break;
						}
						else{
							if(addToCartRet==6){
								MainClass.returnProxy(proxy);
								pw.println("Ticket sold out, didn't buy ticket, returned proxy.");
								pw.flush();
								break;
							}
							else{
								if(addToCartRet==7){
									pw.println("Trying to relogin.");
									pw.flush();
									login();
								}
							}
						}
					}
				}
			}
			try{
				commonSleep();
				pw.println("retry buy good "+good.getId()+" at "+getNow());
				pw.flush();
			}
			catch(IllegalStateException ie){
				ie.printStackTrace(pw);
				pw.flush();
		        //logger.error("",e);
		        if(!resetClient()){
		        	
		        }
		    }
			catch(Exception e){
				e.printStackTrace(pw);
				pw.flush();
			}
			//System.out.println("first try");
		}
	}
	public  void waitUtil1659(){
		Date d=new Date();
		int hour = d.getHours();
		int minute = d.getMinutes();
		int second = d.getSeconds();
		Date d2=new Date();
		d2.setHours(19);
		d2.setMinutes(59);
		d2.setSeconds(30);
		pw.println("waiting to start until 19:59:30 ");
		pw.flush();
		try {
			long l=d2.getTime()-d.getTime();
			if(l>0){
				Thread.sleep(d2.getTime()-d.getTime());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(pw);
			pw.flush();
			waitUtil1659();
		}
	}
	public  void loadGoodsFromWeb(){
		try {
			 pw.println("start loading goods at "+getNow());
				pw.flush();
			BufferedReader br = new BufferedReader(new FileReader(new File("lastGood")));
			String line;
			int i;
			
			if((line=br.readLine())!=null){
				i=Integer.parseInt(line);
				for(int j=1;j<=50;j++){
					getNGood(i+j);
				}
				/*for(int j=1;j<=50;j++){
					getSGood(i+j);
				}*/
			}
			pw.println("goods to be bought(size:"+goods.size()+"):");
			pw.flush();
			for(goodRound g:goods){
				pw.println("goods to be bought:"+g.id+":"+g.count);
				pw.flush();
			}
			if(goods.size()<=0){
				pw.println("Didn't load goods, will retry.");
				pw.flush();
				commonSleep();
				loadGoodsFromWeb();
			}
		}
		catch(IllegalStateException ie){
			ie.printStackTrace(pw);
			pw.flush();
        	resetClient();
        	loadGoodsFromWeb();
        }
		catch (Exception e) {
			e.printStackTrace(pw);
			pw.flush();
			loadGoodsFromWeb();
		}
	}
	public void commonSleep(){
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void getNGood(int goodId){
		//HttpClient httpClient = new HttpClient();  
        HttpGet getMethod = null;  
        //for (int i = 0; i < 30; i++) {  
            getMethod = new HttpGet("http://shop.snh48.com/goods-"+goodId+".html");  
            try {  
                // 鎵цgetMethod  
            	HttpResponse response = client.execute(getMethod)  ;
                if (response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK) {  
                    System.err.println("Method failed: "  
                            + response.getStatusLine().getStatusCode());  
                }  
                // 璇诲彇鍐呭 
                String content=inputStreamToString(response.getEntity().getContent());
                if(content.contains("N队公演普通站票</p>")){
                	goods.add(new goodRound(goodId,1,"N"));
                }
                EntityUtils.consume(response.getEntity());
            }
            catch(IllegalStateException ie){
            	ie.printStackTrace(pw);
				pw.flush();
            	resetClient();
            	getNGood(goodId);
            }
            catch (Exception e) {  
            	e.printStackTrace(pw);
				pw.flush();
            	login();
            	getNGood(goodId);
            } finally {  
                // 閲婃斁杩炴帴  
                //getMethod.releaseConnection();  
            }  
        }
	public void getSGood(int goodId){
		//HttpClient httpClient = new HttpClient();  
        HttpGet getMethod = null;  
        //for (int i = 0; i < 30; i++) {  
            getMethod = new HttpGet("http://shop.snh48.com/goods-"+goodId+".html");  
            try {  
                // 鎵цgetMethod  
            	HttpResponse response = client.execute(getMethod)  ;
                if (response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK) {  
                    System.err.println("Method failed: "  
                            + response.getStatusLine().getStatusCode());  
                }  
                // 璇诲彇鍐呭 
                String content=inputStreamToString(response.getEntity().getContent());
                if(content.contains("S队公演普通站票</p>")){
                	goods.add(new goodRound(goodId,3,"S"));
                }
                EntityUtils.consume(response.getEntity());
                
            }
            catch(IllegalStateException ie){
            	ie.printStackTrace(pw);
				pw.flush();
            	resetClient();
            	getSGood(goodId);
            }
            catch (Exception e) {  
            	e.printStackTrace(pw);
				pw.flush();
            	login();
            	getSGood(goodId);
            } finally {  
                // 閲婃斁杩炴帴  
                //getMethod.releaseConnection();  
            }  
        }
	public  boolean login(){
		if(loginpost!=null){
			loginpost.releaseConnection();
		}
		loginpost = new HttpPost(  
                "http://shop.snh48.com/user.php"); 
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
        nvps.add(new BasicNameValuePair("username", username)); 
        nvps.add(new BasicNameValuePair("password", password)); 
        nvps.add(new BasicNameValuePair("act", "act_login")); 
        nvps.add(new BasicNameValuePair("back_act", "./index.php")); 
        nvps.add(new BasicNameValuePair("remember", "1")); 
        try{
        	loginpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        	response = client.execute(loginpost); 
        	
        	EntityUtils.consume(response.getEntity());
        	if(useProxy){
        		pw.println("login sucessfully with proxy "+proxy.getIp()+" at "+getNow());
        	}
        	else{
        		pw.println("login sucessfully without proxy at "+getNow());
        	}
			pw.flush();
        	return true;
        }
        catch(IllegalStateException ie){
        	ie.printStackTrace(pw);
			pw.flush();
        	return resetClient();
        }
        catch(ConnectException|NoHttpResponseException|ConnectTimeoutException ce){
        	ce.printStackTrace(pw);
			pw.flush();
        	commonSleep();
        	if(useProxy){
        		pw.println("Current proxy "+proxy.getIp()+" is not working, will try to use another proxy at "+getNow());
        		pw.flush();
        		MainClass.proxyEnd(proxy, succeedTime);
        		Proxy tempProxy=MainClass.getProxy();
				while(tempProxy==null){
					commonSleep();
					pw.println("waiting util a proxy is available.");
	        		pw.flush();
					tempProxy=MainClass.getProxy();
				}
        		succeedTime=0;
        		proxy=tempProxy;
        		pw.println("Trying to use new proxy "+proxy.getIp());
        		pw.flush();
    			HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
    			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
    			return login();
        	}
        	else{
        		return login();
        	}
        }
        catch(Exception e){
        	e.printStackTrace(pw);
			pw.flush();
        	commonSleep();
        	if(useProxy){
        		pw.println("Current proxy "+proxy.getIp()+" is not working, will try to use another proxy at "+getNow());
        		pw.flush();
        		MainClass.proxyEnd(proxy, succeedTime);
        		Proxy tempProxy=MainClass.getProxy();
				while(tempProxy==null){
					commonSleep();
					pw.println("waiting util a proxy is available.");
	        		pw.flush();
					tempProxy=MainClass.getProxy();
				}
        		succeedTime=0;
        		proxy=tempProxy;
        		pw.println("Trying to use new proxy "+proxy.getIp());
        		pw.flush();
    			HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
    			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
    			return login();
        	}
        	else{
        		return login();
        	}
        }
        
	}
	public  String getNow(){
		Date d= new Date();
		return d.toLocaleString();
	}
	public int addToCart(int good_id, int number){
		downloadImage();
		if(image==null){
			pw.println("cannot download image");
			return 4;
		}
		HttpPost addpost = new HttpPost(  
                "http://shop.snh48.com/flow.php?step=add_to_cart");  
        JSONObject param = new JSONObject();    
        param.element( "quick", 1 );  
        param.element( "spec", new ArrayList() );  
        param.element( "goods_id", good_id );
        String code=imageProcessor.getCode(image);
        if(code.length()!=5){
        	pw.println("Cannot download full image code is incorrect as "+code);
			return 4;
        }
        else{
        	pw.println("Download full image code is correct as "+code);
        }
        param.element("captcha",code);
        param.element( "number", number );  
        param.element( "parent", 0);
        try{
	        StringEntity se = new StringEntity("goods="+param.toString());     
	        addpost.addHeader("content-type", "application/x-www-form-urlencoded");
	        addpost.setEntity(se); 
	        response= client.execute(addpost); 
	        String retStr=inputStreamToString(response.getEntity().getContent());
	        pw.println(retStr);
	        if(retStr.contains("\"error\":0")){
	        	EntityUtils.consume(response.getEntity());
	        	pw.println("addToCart("+good_id+","+number+") sucessfully at "+getNow());
				pw.flush();
	        	return 0;
	        }
	        else{
	        	if(retStr.contains("\\u60a8\\u4e4b\\u524d\\u8ba2\\u5355\\u4e2d\\u5df2\\u7ecf\\u5b58\\u5728")){
	        		pw.println("Already bought, will exit.");
					pw.flush();
		        	EntityUtils.consume(response.getEntity());
		        	return 5;
	        	}
	        	if(retStr.contains("\\u60a8\\u7684\\u8d2d\\u7269\\u8f66\\u4e2d\\u5df2\\u7ecf\\u5b58\\u5728")){
	        		EntityUtils.consume(response.getEntity());
		        	pw.println("Already added to cart, will need to confirm cart.");
					pw.flush();
		        	return 1;
	        	}
	        	if(retStr.contains("\\u8be5\\u5546\\u54c1\\u5df2\\u7ecf\\u5e93\\u5b58\\u4e0d\\u8db3\\u6682\\u505c\\u9500\\u552e")){
	        		EntityUtils.consume(response.getEntity());
		        	pw.println("Already sold out, will exit.");
					pw.flush();
		        	return 6;
	        	}
	        	if(retStr.contains("\\u8bf7\\u767b\\u9646\\u540e\\u8d2d\\u4e70\\uff01")){
	        		EntityUtils.consume(response.getEntity());
		        	pw.println("Not login yet. Need to relogin.");
					pw.flush();
		        	return 7;
	        	}
	        	if(retStr.contains("\\u60a8\\u8f93\\u5165\\u7684\\u9a8c\\u8bc1\\u7801\\u4e0d\\u6b63\\u786e\\uff0c\\u8bf7\\u91cd\\u65b0\\u8f93\\u5165\\uff01")){
	        		EntityUtils.consume(response.getEntity());
		        	pw.println("Cannnot download correct image, will need to use another proxy at "+getNow());
					pw.flush();
		        	return 4;
	        	}
	        	else{
	        		//ImageIO.write(image, "PNG", new File("chat"+good_id+"_"+code+".png")); 
		        	pw.println("addToCart("+good_id+","+number+") failed at "+getNow()+", will readd");
					pw.flush();
		        	EntityUtils.consume(response.getEntity());
		        	return 2;
	        	}
	        }
        }
        catch(IllegalStateException ie){
        	ie.printStackTrace(pw);
			pw.flush();
        	if(resetClient()){
        		return addToCart(good_id,number);
        	}
        	else{
        		return 3;
        	}
        }
        catch(Exception e){
        	pw.println("get an exception:"+e.getMessage()+" when addToCart("+good_id+","+number+"), readding at "+getNow());
			pw.flush();
        	if(login()){
        		return addToCart(good_id,number);
        	}
        	else{
        		return 3;
        	}
        }
	}
	public  boolean confirmCart(){
		boolean ret=false;
		try{
	        HttpPost confirmpost = new HttpPost(  
	                "http://shop.snh48.com/flow.php"); 
	        List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
	        nvps.add(new BasicNameValuePair("step", "done")); 
	        nvps.add(new BasicNameValuePair("postscript", "")); 
	        nvps.add(new BasicNameValuePair("how_oos", "0")); 
	        nvps.add(new BasicNameValuePair("payment", "25")); 
	        nvps.add(new BasicNameValuePair("shipping", "5"));
	        confirmpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	        response = client.execute(confirmpost); 
	        String retStr=inputStreamToString(response.getEntity().getContent());
	        if(retStr.contains("感谢您在本店购物")||retStr.contains("您的购物车中没有商品")){
	        	ret=true;
	        	pw.println(retStr);
	        	pw.println("confirmed Cart sucessfully at "+getNow());
	        }
	        else{
	        	pw.println(retStr);
	        	pw.println("confirmed Cart failed at "+getNow()+", will readd to cart.");
	        }
	        pw.flush();
	        EntityUtils.consume(response.getEntity());
        }
        catch(IllegalStateException ie){
        	ie.printStackTrace(pw);
			pw.flush();
        	login();
        	return false;
        }
		catch(Exception e){
			e.printStackTrace(pw);
			pw.flush();
        	login();
        	return false;
		}
		return ret;
	}
	private  String inputStreamToString(InputStream is) {
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
			e.printStackTrace(pw);
			pw.flush();
		}
	    
	    // Return full string
	    return total.toString();
	}
	public  boolean confirmCartWithShipping(){
		boolean ret=false;
		try{
	        HttpPost confirmpost = new HttpPost(  
	                "http://shop.snh48.com/flow.php"); 
	        List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
	        nvps.add(new BasicNameValuePair("step", "done")); 
	        nvps.add(new BasicNameValuePair("postscript", "")); 
	        nvps.add(new BasicNameValuePair("how_oos", "0")); 
	        nvps.add(new BasicNameValuePair("payment", "25")); 
	        nvps.add(new BasicNameValuePair("shipping", "5")); 
	        confirmpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	        response = client.execute(confirmpost); 
	        if(inputStreamToString(response.getEntity().getContent()).contains("感谢您在本店购物")){	
	        	ret=true;
	        }
	        //writeLog(inputStreamToString(response.getEntity().getContent()));
	        EntityUtils.consume(response.getEntity());
        }
		catch(IllegalStateException ie){
			ie.printStackTrace(pw);
			pw.flush();
        	resetClient();
        }
        catch(Exception e){
        	e.printStackTrace(pw);
			pw.flush();
        }
		return ret;
	}
	/*public  void loadGoods(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("goods")));
			String line;
			int i;
			while((line=br.readLine())!=null){
				i=line.indexOf(",");
				goods.add(new goodRound(Integer.parseInt(line.substring(0,i)),Integer.parseInt(line.substring(i+1))));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	public  void downloadImage() {  
        //HttpClient httpClient = new HttpClient();  
        HttpGet getMethod = null;  
        //for (int i = 0; i < 30; i++) {  
            getMethod = new HttpGet("http://shop.snh48.com/captcha.php?is_login=1&t=0.169177433941"+good.getId()  
                    );  
            try {  
            	HttpResponse response = client.execute(getMethod)  ;
                if (response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK) {  
                    System.err.println("Method failed: "  
                            + response.getStatusLine().getStatusCode());  
                }     
                InputStream inputStream = response.getEntity().getContent();
                 
                image = ImageIO.read(inputStream);
                if(image==null){
                	return;
                }
                //ImageIO.write(image, "PNG", new File("chat.png")); 
                EntityUtils.consumeQuietly(response.getEntity());
                //System.out.println(i + "OK!");  
            }
            catch(IllegalStateException ie){
            	ie.printStackTrace(pw);
				pw.flush();
            	resetClient();
            }
            catch (Exception e) {  
            	e.printStackTrace(pw);
				pw.flush();
            } finally {    
                //getMethod.releaseConnection();  
            }  
        } 
	public void writeLog(String str){
		pw.println(str);
		pw.flush();
	}
    //} 
}
