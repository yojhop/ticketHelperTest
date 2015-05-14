import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


public class Register {
	static DefaultHttpClient client = new DefaultHttpClient();
	static HttpResponse response;
	public static void main(String args[]){
		ArrayList<String> users=Utils.readFile("register.properties");
		for(String user:users){
			
			if(register(user)){
				System.out.println(user+",cainiao97");
			}
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static boolean register(String username){
		Date date = new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("MMdd");   
		String dateStr=sdf.format(date); 
		 HttpPost loginpost = new HttpPost(  
                "http://shop.snh48.com/user.php?act=register"); 
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();  
        nvps.add(new BasicNameValuePair("username", username)); 
        nvps.add(new BasicNameValuePair("email", username+dateStr+"@126.com")); 
        nvps.add(new BasicNameValuePair("password", "cainiao97")); 
        nvps.add(new BasicNameValuePair("confirm_password", "cainiao97")); 
        nvps.add(new BasicNameValuePair("extend_field5", "1")); 
        nvps.add(new BasicNameValuePair("remember", "13189800778")); 
        nvps.add(new BasicNameValuePair("sel_question", "friend_birthday")); 
        nvps.add(new BasicNameValuePair("passwd_answer", "0618")); 
        nvps.add(new BasicNameValuePair("extend_field2", "")); 
        nvps.add(new BasicNameValuePair("agreement", "1")); 
        nvps.add(new BasicNameValuePair("act", "act_register"));
        nvps.add(new BasicNameValuePair("back_act", ""));
        nvps.add(new BasicNameValuePair("Submit", ""));
        try {
			loginpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			try {
				response = client.execute(loginpost);
				String retStr=inputStreamToString(response.getEntity().getContent());
				EntityUtils.consume(response.getEntity());
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				loginpost = new HttpPost(  
		                "http://shop.snh48.com/flow.php?step=consignee"); 
				nvps = new ArrayList<NameValuePair>();  
				nvps.add(new BasicNameValuePair("country", "1")); 
				 nvps.add(new BasicNameValuePair("city", "321"));
				 nvps.add(new BasicNameValuePair("province","25"));
				 nvps.add(new BasicNameValuePair("district", "2707")); 
				 nvps.add(new BasicNameValuePair("consignee", "蔡远雁")); 
				 nvps.add(new BasicNameValuePair("address", "浦东新区紫薇路198弄6号楼601室")); 
				 nvps.add(new BasicNameValuePair("zipcode", "")); 
				 nvps.add(new BasicNameValuePair("sign_building", ""));  
				 nvps.add(new BasicNameValuePair("best_time", "")); 
		        nvps.add(new BasicNameValuePair("email", username+dateStr+"@126.com")); 
		        nvps.add(new BasicNameValuePair("tel", "13917488547")); 
		        nvps.add(new BasicNameValuePair("mobile", "")); 
		        nvps.add(new BasicNameValuePair("Submit", "配送至这个地址")); 
		        nvps.add(new BasicNameValuePair("step", "consignee")); 
		        nvps.add(new BasicNameValuePair("act", "checkout")); 
		        nvps.add(new BasicNameValuePair("address_id", "")); ; 
		        /*country:1
province:25
city:321
district:2707
consignee:蔡远雁
email:drawer0502@126.com
address:浦东新区紫薇路198弄6号楼601室   
zipcode:
tel:13917488547
mobile:
sign_building:
best_time:
Submit:配送至这个地址
step:consignee
act:checkout
address_id:18158*/
		        loginpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		        response = client.execute(loginpost);
		        EntityUtils.consume(response.getEntity());
				if(retStr.contains("注册成功")){
					return true;
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
    	
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
			e.printStackTrace();
		}
	    
	    // Return full string
	    return total.toString();
	}
}
