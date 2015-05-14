

import java.io.*;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class Login 
{
	private static final String LOGINURL = "http://login.sina.com.cn/sso/login.php?";
	private static final String LOGOUTURL = "http://t.sina.com.cn/logout.php?";
	private String email;
	private String password;
	HttpClient client=new HttpClient();

	public Login(String email,String password)
	{
		this.email = email;
		this.password = password;
	}
	
	public HttpClient login()
	{
		PostMethod method = new PostMethod(LOGINURL);
		NameValuePair emailpair = new NameValuePair("username", email);
		NameValuePair passwordpair = new NameValuePair("password", password);
		method.addParameters(new NameValuePair[]{emailpair,passwordpair});
		
		int statuscode = 0;
		
		try {
			statuscode = client.executeMethod(method);
			System.out.println(method.getResponseBodyAsString());
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(statuscode == HttpStatus.SC_OK)
		{
     		/*try {
				method.releaseConnection();
				GetMethod get_method = new GetMethod("http://t.sina.com.cn/");
				client.executeMethod(get_method);
				
				InputStream in1 = get_method.getResponseBodyAsStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in1, "utf-8"));
				String s;
				String s1 = new String();
				while((s =br.readLine())!=null)
				{
					System.out.println(s);
					s1 += s + "\n";
				}
				
				get_method.releaseConnection();
				//
				// write to the file
				//
		        File f = new File("my_tsina.html");
		        if (!f.exists()) {
		         f.createNewFile();
		        }
		        
		        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
		        BufferedWriter output = new BufferedWriter(write);
		        output.write(s1);
		        output.close();
		        
			} catch (IOException e1) {
				e1.printStackTrace();
			}
*/
			
			method.releaseConnection();
			return client;
		}
		
		method.releaseConnection();
		
		if(statuscode == HttpStatus.SC_MOVED_TEMPORARILY || statuscode == HttpStatus.SC_MOVED_TEMPORARILY)
		{
			Header head = method.getResponseHeader("location");
			String headvalue = head.getValue();
			GetMethod getmethod = new GetMethod(headvalue);
			try {
				int statuscodel = client.executeMethod(getmethod);
				if(statuscodel== HttpStatus.SC_OK)
					return client;
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void logout(HttpClient client)
	{
		GetMethod method = new GetMethod(LOGOUTURL);
		try {
			 client.executeMethod(method);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		method.releaseConnection();
	}

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		HttpClient client = new HttpClient();
		Login logger = new Login("yuanyan.cai@gmail.com", "cainiao97");
		client=logger.login( );
		//System.out.println(logger.getFans("http://weibo.com/p/1005053669102477/follow?relate=fans&page=2#place"));
		
		//logger.logout( client);
	}
	public String getFans(String URL){
		GetMethod method = new GetMethod(URL);
		try {
			//method.set
			client.executeMethod(method);
			
			return method.getResponseCharSet();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}