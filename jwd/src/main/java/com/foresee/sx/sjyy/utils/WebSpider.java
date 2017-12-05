package com.foresee.sx.sjyy.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;

import com.foresee.fbrp.util.xml.VtdXmlOperateDelegate;


/**
 * 
 * @author hanbaoshan
 *
 */
public class WebSpider {
    private static final Log log = LogFactory.getLog(WebSpider.class);
    /**
     * 通过url得到该url返回网页的源代码
     * @param url 网页的地址
     * @param charset 网页的字符集
     * @return
     */
    public String getURLContent(String url,String charset){
        StringBuilder result = new StringBuilder("");
        BufferedReader in = null ;
        try {
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
            Map<String, List<String>> map = connection.getHeaderFields();
            String contentEncoding = getContentEncoding(map);
            connection.connect();
            InputStream urlStream = null;
            if("gzip".equals(contentEncoding)){
                urlStream = new GZIPInputStream(connection.getInputStream());
            }else{
                urlStream = new BufferedInputStream(connection.getInputStream());
            }

            in = new BufferedReader(new InputStreamReader(urlStream,charset));
            String line = "";
            while ((line = in.readLine()) != null) {

                result.append(line);
                result.append("\r\n");
            }
            System.out.println(result);

        } catch (MalformedURLException e) {
            System.out.print("发送GET请求出现异常！" + e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.print("字符编码异常！" + e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return result.toString();
    }


//    public String getPoseURLContent(String url,String charset,String parms){
//        String result = "";
//        BufferedReader in = null ;
//        try {
//            URL realUrl = new URL(url);
//            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setDoOutput(true);
//
//            Map<String, List<String>> map = connection.getHeaderFields();
//            String contentEncoding = getContentEncoding(map);
//
//            //    写入的POST数据
//            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
//            osw.write(parms);
//            osw.flush();
//            osw.close();
//
//            connection.connect();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    public String getUrlContentNew(String url,String charset){
        String html=null;
        try {
            html = new String(Jsoup.connect(url)
                    .userAgent("Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)")
                    .timeout(5000)
                    .execute()
                    .bodyAsBytes(),charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  html;
    }


    private String getContentEncoding(Map<String, List<String>> head){
        String contentEncoding = "";
        List<String> list = head.get("Content-Encoding");
        if(null==list||list.size()==0){
            //使用普通的流
        }else{
            contentEncoding = list.get(0);
        }
        return contentEncoding;
    }

    
    /**
     * 根据地址名称匹配经纬度
     * @param name
     * @return
     */
    public  String getLngandLatInfo(String addressname,String city){
    	String content = null;
    	try {
		/*
		 * 
		 * 可以替换大部分空白字符， 不限于空格 
          \s 可以匹配空格、制表符、换页符等空白字符的其中任意一个 */
		 	addressname =URLEncoder.encode(addressname, "utf-8");
			city =URLEncoder.encode(city, "utf-8");
/*			System.out.println("city====="+city+"======addressname========"+addressname);*/	
			//String url1 = "http://api.map.baidu.com/geocoder/v2/?address="+addressname.replaceAll("\\s*", "")+"&city="+city+"&output=xml&ak=zwS8h1TlrBqMK4DhxKytXPAQwehAju3f&callback=showLocation";
			String url1 = "http://api.map.baidu.com/geocoder/v2/?address="+addressname.replaceAll("\\s*", "")+"&city="+city+"&output=xml&ak=yGWrsVFBVqGFGcGnZSruZwPwhX8ejZvG&callback=showLocation";
			//String url1 = "http://api.map.baidu.com/geocoder/v2/?address="+addressname.replaceAll("\\s*", "")+"&city="+city+"&output=xml&ak=vbahod25TZiD9R3Cv6RRo9H74VVaWz18&callback=showLocation";

    		content = this.getUrlContentNew(url1,"utf-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//    	log.debug("conten=="+content);
    	return content;
    }
    
    /**
     * 根据经纬度匹配地址名称
     * @param name
     * @return
     */
    public String getAddressByLngandLat(String LngandLat){
        String url1 = "http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&location="+LngandLat+"&output=xml&pois=1&ak=yGWrsVFBVqGFGcGnZSruZwPwhX8ejZvG&callback=showLocation";
        WebSpider spider = new WebSpider();
        String content = spider.getUrlContentNew(url1,"utf-8");
/*    	log.debug("conten=="+content);
*/    	return content;
    }

    //地址转换经纬度
    public static void main(String[] args) throws Exception{
        String url1 = "http://api.map.baidu.com/geocoder/v2/?address=西安市西安市莲湖区劳动南路14号老虎公寓1单元26层A2607、2605、2604、2603、2602、2601、2621、2620、2619室&city=西安市&output=xml&ak=yGWrsVFBVqGFGcGnZSruZwPwhX8ejZvG&callback=showLocation";
       // getLngandLatInfo("陕西省西安市碑林区");
//        URL obj = new URL(url);
//        URLConnection conn = obj.openConnection();
//        Map<String, List<String>> map = conn.getHeaderFields();
//
//        System.out.println("显示响应Header信息\n");
//
//        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//            System.out.println("Key : " + entry.getKey() +
//                    " ,Value : " + entry.getValue());
//        }
        WebSpider spider = new WebSpider();
        String content = spider.getUrlContentNew(url1,"utf-8");
        log.debug(content);
      //  JSONObject job = JSONObject.parseObject(content );
		VtdXmlOperateDelegate vtd = new VtdXmlOperateDelegate(content);
		String status = vtd.getNodeDataByXpath("//GeocoderSearchResponse/status");
		String lng = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lng");
		String lat = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lat");
		String level = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/level");
        log.debug(vtd);
        log.debug("===status=="+status);
        log.debug("====lng="+lng);
        log.debug("====lat="+lat);
        log.debug("====level="+level);

    }
    

    /*//经纬度转换地址
    public static void main(String[] args) throws Exception{
        String url1 = "http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&location=34.330062721,108.953194543&output=xml&pois=1&ak=yGWrsVFBVqGFGcGnZSruZwPwhX8ejZvG&callback=showLocation";
        WebSpider spider = new WebSpider();
        String content = spider.getUrlContentNew(url1,"utf-8");
        log.debug(content);
		VtdXmlOperateDelegate vtd = new VtdXmlOperateDelegate(content);
		String status = vtd.getNodeDataByXpath("//GeocoderSearchResponse/status");
		String lng = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lng");
		String lat = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/location/lat");
		String formatted_address = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/formatted_address");
		String business = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/business");

		//addressComponent
		String country = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/country");
		String country_code = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/country_code");
		String province = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/province");
		String city = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/city");
		String adcode = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/adcode");
		String street = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/street");
		String street_number = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/street_number");
		String addressComponentdirection = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/direction");
		String distance = vtd.getNodeDataByXpath("//GeocoderSearchResponse/result/addressComponent/distance");
        log.debug(vtd);
        log.debug("===status=="+status);
        log.debug("====lng="+lng);
        log.debug("====lat="+lat);
        log.debug("====formatted_address="+formatted_address);
        log.debug("====business="+business);
        //addressComponent
        log.debug("====country="+country);
        log.debug("====country_code="+country_code);
        log.debug("====province="+province);
        log.debug("====city="+city);
        log.debug("====street="+street);
        log.debug("====adcode="+adcode);
        log.debug("====country="+country);
        log.debug("====street_number="+street_number);
        log.debug("====direction="+addressComponentdirection);
        log.debug("====distance="+distance);
		//list
		List<VtdSelectResultWrapper> ls = (List<VtdSelectResultWrapper>) vtd.selectByXpath("//GeocoderSearchResponse/result/pois/poi");
		for (int i = 0; i < ls.size(); i++) {
			VtdXmlOperateDelegate vtddetil = new VtdXmlOperateDelegate(ls.get(i).getText());
			String poiaddr = vtddetil.getNodeDataByXpath("/poi/addr");
			String poicp = vtddetil.getNodeDataByXpath("/poi/cp");
			String poidirection = vtddetil.getNodeDataByXpath("/poi/direction");
			String poiname = vtddetil.getNodeDataByXpath("/poi/name");
			String poipoiType = vtddetil.getNodeDataByXpath("/poi/poiType");
			String poix = vtddetil.getNodeDataByXpath("/poi/point/x");
			String poiy = vtddetil.getNodeDataByXpath("/poi/point/y");
			String poitag = vtddetil.getNodeDataByXpath("/poi/tag");
			String poitel = vtddetil.getNodeDataByXpath("/poi/tel");
			String poiuid = vtddetil.getNodeDataByXpath("/poi/uid");
			String poizip = vtddetil.getNodeDataByXpath("/poi/zip");

			String parent_poiname = vtddetil.getNodeDataByXpath("/poi/parent_poi/name");
			String parent_poitag = vtddetil.getNodeDataByXpath("/poi/parent_poi/tag");
			String parent_poiaddr = vtddetil.getNodeDataByXpath("/poi/parent_poi/addr");
			String parent_poix = vtddetil.getNodeDataByXpath("/poi/parent_poi/point/x");
			String parent_poiy = vtddetil.getNodeDataByXpath("/poi/parent_poi/point/y");
			String parent_poidirection = vtddetil.getNodeDataByXpath("/poi/parent_poi/direction");
			String parent_poidistance = vtddetil.getNodeDataByXpath("/poi/parent_poi/distance");
			String parent_poiuid = vtddetil.getNodeDataByXpath("/poi/parent_poi/uid");
			log.debug("xmm==" + ls.get(i).getText());
			log.debug("addr=======" + poiaddr);
		}
        
    }*/
    
/*    public static void main(String[] args) {
    	//getAddressByLngandLat("34.5343660453,108.254658649");
	}*/
    
    /**
     * 正则表达式过滤特殊字符 	
     * @Title: StringFilter 
     * @Description: TODO
     * @param str
     * @return
     * @throws PatternSyntaxException
     * @return: String
     */
    	public static String StringFilter(String str) throws PatternSyntaxException {
    		// 清除掉所有特殊字符
    		String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    		Pattern p = Pattern.compile(regEx);
    		Matcher m = p.matcher(str);
    		return m.replaceAll("").trim();
    	}
}
