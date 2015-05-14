import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Utils {
	public static ArrayList<String> readFile(String filePath){
		ArrayList<String> retList=new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
			String str;
			
			try {
				while((str=br.readLine())!=null){
					retList.add(str);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retList;
	}
}
