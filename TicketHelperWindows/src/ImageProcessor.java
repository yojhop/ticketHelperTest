import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

public class ImageProcessor {  
  
    private  Map<String,BufferedImage> trainMap = null;  
    private  int index = 0;  
    private boolean isInited=false;
    public  int isBlack(int colorInt) {  
        Color color = new Color(colorInt);  
        if (color.getRed() ==0&& color.getGreen() ==0 &&color.getBlue() == 0) {  
            return 1;  
        }  
        return 0;  
    }  
    
    public  int isWhite(int colorInt) {  
        Color color = new Color(colorInt);  
        if (color.getRed()==255 && color.getGreen()==255 && color.getBlue() ==255) {  
            return 1;  
        }  
        return 0;  
    }  
  
    public  BufferedImage removeBackgroud(String picFile)  
            throws Exception {  
        BufferedImage img = ImageIO.read(new File(picFile));  
        return img;  
    }  
  
    public  BufferedImage removeBlank(BufferedImage img) throws Exception {  
        int width = img.getWidth();  
        int height = img.getHeight();  
        int start = 0;  
        int end = 0;  
        Label1: for (int y = 0; y < height; ++y) {  
            int count = 0;  
            for (int x = 0; x < width; ++x) {  
                if (isWhite(img.getRGB(x, y)) == 1) {  
                    count++;  
                }  
                if (count >= 1) {  
                    start = y;  
                    break Label1;  
                }  
            }  
        }  
        Label2: for (int y = height - 1; y >= 0; --y) {  
            int count = 0;  
            for (int x = 0; x < width; ++x) {  
                if (isWhite(img.getRGB(x, y)) == 1) {  
                    count++;  
                }  
                if (count >= 1) {  
                    end = y;  
                    break Label2;  
                }  
            }  
        }  
        return img.getSubimage(0, start, width, end - start + 1);  
    }  
  
    public  List<BufferedImage> splitImage(BufferedImage img,String filename)  
            throws Exception {  
        List<BufferedImage> subImgs = new ArrayList<BufferedImage>();  
        int width = img.getWidth();  
        int height = img.getHeight();  
        List<ImageBound> weightlist = new ArrayList<ImageBound>();
        //ImageIO.write(img, "PNG", new File("test.png")); 
        boolean hasWhite;
        int start=0;
        int end=0;
        for (int x = 0; x < width; x++) {  
            hasWhite=false; 
            
            for (int y = 0; y < height; y++) {  
                if (isWhite(img.getRGB(x, y)) == 1) {  
                	hasWhite=true; 
                	break;
                }  
            }  
            if(hasWhite){
            	if(start==0){
            		start=x;
            	}
            	if(x>end){
            		end=x;
            	}
            }
            else{
            	if(start>0||end>0){
            		//System.out.println("start:"+start+",end:"+end);
            		weightlist.add(new ImageBound(start,end,0,0));
            	}
            	start=0;
            	end=0;
            } 
        }  
        for(int i=0;i<weightlist.size();i++){
        	int left=weightlist.get(i).getLeft();
        	int right=weightlist.get(i).getRight();
        	start=0;
        	end=0;
        	for(int y = 0; y < height; y++){
        		 hasWhite=false; 
                 
                 for (int j=left;j<=right;j++) {  
                     if (isWhite(img.getRGB(j, y)) == 1) {  
                     	hasWhite=true; 
                     	break;
                     }  
                 }  
                 if(hasWhite){
                 	if(start==0){
                 		start=y;
                 	}
                 	if(y>end){
                 		end=y;
                 	}
                 }
                 else{
                 	if(start>0||end>0){
                 		//System.out.println("top:"+start+",bottom:"+end);
                 		weightlist.get(i).setTop(start);
                 		weightlist.get(i).setBottom(end);
                 	}
                 	start=0;
                 	end=0;
                 } 
        	}
        }
        for(int i=0;i<weightlist.size();i++){
        	subImgs.add(img.getSubimage(weightlist.get(i).getLeft(), weightlist.get(i).getTop(), weightlist.get(i).getRight()-weightlist.get(i).getLeft()+1, weightlist.get(i).getBottom()-weightlist.get(i).getTop()+1));
        	//ImageIO.write(subImgs.get(i), "PNG", new File(  "images\\"+filename+i+".png")); 
        	
        }
        return subImgs;  
    }  
    
    public  Map<String,BufferedImage > loadTrainData() throws Exception {  
        if (trainMap == null) {  
            Map<String,BufferedImage > map = new HashMap<String,BufferedImage >();  
            File dir = new File("resources");  
            File[] files = dir.listFiles();  
            for (File file : files) {  
                map.put(file.getName(),ImageIO.read(file) );  
            }  
            trainMap = map;  
        }  
        return trainMap;  
    }  
  
    public  String getSingleCharOcr(BufferedImage img,  
            Map<BufferedImage, String> map) {  
        String result = "";  
        int width = img.getWidth();  
        int height = img.getHeight();  
        int min = width * height;  
        for (BufferedImage bi : map.keySet()) {  
            int count = 0;  
            int widthmin = width < bi.getWidth() ? width : bi.getWidth();  
            int heightmin = height < bi.getHeight() ? height : bi.getHeight();  
            Label1: for (int x = 0; x < widthmin; ++x) {  
                for (int y = 0; y < heightmin; ++y) {  
                    if (isWhite(img.getRGB(x, y)) != isWhite(bi.getRGB(x, y))) {  
                        count++;  
                        if (count >= min)  
                            break Label1;  
                    }  
                }  
            }  
            if (count < min) {  
                min = count;  
                result = map.get(bi);  
            }  
        }  
        return result;  
    }  
  
    /*public  String getAllOcr(String file) throws Exception {  
        BufferedImage img = removeBackgroud(file);  
        List<BufferedImage> listImg = splitImage(img);  
        Map<BufferedImage, String> map = loadTrainData();  
        String result = "";  
        for (BufferedImage bi : listImg) {  
            result += getSingleCharOcr(bi, map);  
        }  
        ImageIO.write(img, "JPG", new File("result2//" + result + ".jpg"));  
        return result;  
    } */ 
  
    public  void downloadImage() {  
        HttpClient httpClient = new HttpClient();  
        GetMethod getMethod = null;  
        for (int i = 0; i < 30; i++) {  
            getMethod = new GetMethod("http://www.pkland.net/img.php?key="  
                    + (2000 + i));  
            try {  
                // 执行getMethod  
                /*int statusCode = httpClient.executeMethod(getMethod);  
                if (statusCode != HttpStatus.SC_OK) {  
                    System.err.println("Method failed: "  
                            + getMethod.getStatusLine());  
                } */ 
                // 读取内容  
                String picName = "captcha.png";  
                InputStream inputStream = getMethod.getResponseBodyAsStream();  
                OutputStream outStream = new FileOutputStream(picName);  
                IOUtils.copy(inputStream, outStream);  
                outStream.close();  
                System.out.println(i + "OK!");  
            } catch (Exception e) {  
                e.printStackTrace();  
            } finally {  
                // 释放连接  
                getMethod.releaseConnection();  
            }  
        }  
    }  
    public  BufferedImage getBlankImage(BufferedImage img){
    	int width = img.getWidth();  
    	 Color color = new Color(255,255,255);
    	 Color b = new Color(0,0,0);
        int height = img.getHeight();  
        for (int x = 0; x < width; ++x) {  
            for (int y = 0; y < height; ++y) {  
                if (isBlack(img.getRGB(x, y)) == 1 ) {  
                	img.setRGB(x, y, color.getRGB());
                }
                else{
                	
                	if(isWhite(img.getRGB(x, y)) == 0){
                		img.setRGB(x, y, b.getRGB());
                	}
                }
            }    
        }
        return img;
    }
    public  String getChar(BufferedImage img){
    	String s="";
    	int width=img.getWidth();
    	int height=img.getHeight();
    	
    	for (String valueStr : trainMap.keySet()) {
    		BufferedImage bi = trainMap.get(valueStr);
            int count = 0;  
            int widthmin = width < bi.getWidth() ? width : bi.getWidth();  
            int heightmin = height < bi.getHeight() ? height : bi.getHeight();
            boolean found=true;
            label1:for (int x = 0; x < widthmin; ++x) {  
                for (int y = 0; y < heightmin; ++y) {  
                    if (isWhite(img.getRGB(x, y)) != isWhite(bi.getRGB(x, y))) {  
                        found=false; 
                        break label1;
                    }  
                }  
            }  
            if(found){
            	s=valueStr;
            	break;
            }
        }
    	return s;
    }
    public  void loadData(){
    	
    }
/*    public  void trainData() throws Exception {  
        File dir = new File("temp");  
        File[] files = dir.listFiles();  
        for (File file : files) {  
            BufferedImage img = removeBackgroud("temp//" + file.getName());  
            List<BufferedImage> listImg = splitImage(img);  
            if (listImg.size() == 4) {  
                for (int j = 0; j < listImg.size(); ++j) {  
                    ImageIO.write(listImg.get(j), "JPG", new File("train2//"  
                            + file.getName().charAt(j) + "-" + (index++)  
                            + ".jpg"));  
                }  
            }  
        }  
    }*/  
  
    /** 
     * @param args 
     * @throws Exception 
     */  
    public  String getChars(List<BufferedImage> imgs){
    	String str="";
    	for(int i=0;i<imgs.size();i++){
    		BufferedImage img = imgs.get(i);
    		str+=getChar(img);
    	}
    	return str;
    }
    public  String getCode(BufferedImage img){
    	
    	List<BufferedImage> imgs;
		try {
				if(!isInited){
					loadTrainData();
				}
				isInited=true;
			imgs = splitImage( getBlankImage(img),"");
			return getChars(imgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    }
    public  static void main(String[] args) throws Exception {  
        // downloadImage();  
        //for (int i = 0; i < 30; ++i) {  
           // String text = getAllOcr("captcha.png");  
            //BufferedImage img = ImageIO.read(new File("captcha.png"));
            //getBlankImage(img);
           // List<BufferedImage> imgs= splitImage( img);
            /*for (int i=0;i<imgs.size();i++){
            	ImageIO.write(imgs.get(i), "PNG", new File(  "captcha"+i+".png"));  
            }*/
    		String imgPath = "captcha.png";  
    		ImageProcessor ip=new ImageProcessor();
    		BufferedImage image = ImageIO.read(new FileInputStream(imgPath));
    		ip.loadTrainData();
    		List<BufferedImage> imgs=ip.splitImage( ip.getBlankImage(image),"");
    		for(int i=0;i<imgs.size();i++){
    			ImageIO.write(imgs.get(i), "PNG", new File(  "captcha"+i+".png"));  
    		}
    		System.out.println( ip.getChars(ip.splitImage( image,"")));
           
           // for(int i)
                 //splitImage(i); 
                 //ImageIO.write(i, "PNG", new File(  "images\\"+file.getName())); 
                 
           // }  
            //ImageIO.write(img, "PNG", new File(  "captcha2.png"));  
           // List<BufferedImage> listImg = splitImage(img);   
       // }  
    }  
}  