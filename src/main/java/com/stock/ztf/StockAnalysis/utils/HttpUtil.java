package com.stock.ztf.StockAnalysis.utils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.type.TypeReference;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.stock.ztf.StockAnalysis.beans.ProxyData;

/**
 * HttpUtil
 * 
 * @author ztf
 *
 */
@Component
public class HttpUtil {

	private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;
	
	/**
	 * 代理列表
	 */
	private static List<ProxyData> proxys = new ArrayList<ProxyData>();
	private static Random random = new Random();
	
	@Autowired
	RestTemplate restTemplate;
	
	public static void setProxys(List<ProxyData> proxyList ){
		proxys=proxyList;
	}

//	@Scheduled(initialDelay = 5 * oneSecond, fixedRate = 5 * oneSecond)
	public void testUrlStr(){		  
		System.out.println(getUrlStr("http://lobert.iteye.com/blog/1604122",false));
	}
	
	public String getUrlStr(String url,Boolean useProxy, Object... uriVariables){		
		HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
        headers.set("Upgrade-Insecure-Requests", "1");
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.set("x-forwarded-for", FnUtils.getRandomIp());
//        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(headers);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(60*1000);//ms
      	factory.setConnectTimeout(60*1000);//ms     
        String urlRet =null;
        int flag=0;
        while (urlRet == null ||urlRet.isEmpty()) {
			/**
			 * 失败尝试三次获取数据
			 */
			if (flag > 3) {
				break;
			}
			try {
				if (flag != 0) {
					Thread.sleep(5 * oneSecond);
				}
				if (useProxy&&!proxys.isEmpty()) {
		      		/**
		      		 * 随机设置代理
		      		 */
		      		ProxyData pData=proxys.get(random.nextInt(proxys.size()-1));
		      		logger.debug("Set Proxy IP:"+pData.getIp()+" Port:"+ pData.getPort());
		      		SocketAddress address = new InetSocketAddress(pData.getIp(), pData.getPort());
		          	Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
		          	factory.setProxy(proxy);
				}
		      	
		        restTemplate.setRequestFactory(factory);
		        urlRet = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, uriVariables).getBody();
				flag++;
			} catch (Exception e) {
				flag++;
				logger.error("GET url[" + url + "] error:" + e.getMessage(),e);
			}
		}
        return urlRet;
	}
	
	public String getUrlStr(String url, Object... uriVariables){		
		HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
        headers.set("Upgrade-Insecure-Requests", "1");
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.set("x-forwarded-for", FnUtils.getRandomIp());
//        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(headers);
        String urlRet =null;
        int flag=0;
        while (urlRet == null ||urlRet.isEmpty()) {
			/**
			 * 失败尝试三次获取数据
			 */
			if (flag > 3) {
				break;
			}
			try {
				if (flag != 0) {
					Thread.sleep(10 * oneSecond);
				}
				urlRet = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, uriVariables).getBody();
				flag++;
			} catch (Exception e) {
				flag++;
				logger.error("GET url[" + url + "] error:" + e.getMessage(),e);
			}
		}
        return urlRet;
	}
	
}
