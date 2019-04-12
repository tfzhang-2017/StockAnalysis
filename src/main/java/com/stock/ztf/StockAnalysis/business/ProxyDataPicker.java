package com.stock.ztf.StockAnalysis.business;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.stock.ztf.StockAnalysis.beans.ProxyData;
import com.stock.ztf.StockAnalysis.mappers.ProxyDataMapper;
import com.stock.ztf.StockAnalysis.utils.FnUtils;

/**
 * 采集股票基础数据
 * 
 * @author ztf
 *
 */
@Service
public class ProxyDataPicker {

	private final static Logger logger = LoggerFactory.getLogger(ProxyDataPicker.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;
	
	@Autowired
	private ProxyDataMapper proxyDataMapper;

//	@Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	private void getProxyList(){		  
		//文档对象，用来接收html页面
		 Document document = null;
		 try {
		     for (int i = 1; i < 50; i++) {
		    	//获取指定网址的页面内容
			     document = Jsoup.connect("http://www.xicidaili.com/wt/"+i).timeout(50000).get();
			   //通过Document的select方法获取class为abc的Elements结点集合
			     Elements trs = document.select("#ip_list > tbody > tr:gt(0)");
			     for (Element element : trs) {
			    	//得到结点的第一个对象
			    	 Elements tds = element.select("td");
			    	 ProxyData proxyData=new ProxyData();
			    	 proxyData.setIp(tds.get(1).text());
			    	 proxyData.setPort(Integer.parseInt(tds.get(2).text()));
			    	 proxyData.setType(tds.get(5).text());
			    	 proxyData.setSpeed(tds.get(6).select("div").attr("title"));
			    	 proxyData.setTimeout(tds.get(7).select("div").attr("title"));
			    	 proxyData.setLive(tds.get(8).text());
			    	 if (FnUtils.isHostConnectable(proxyData.getIp(),proxyData.getPort(), oneSecond)) {
			    		 proxyDataMapper.addProxyData(proxyData);	
					}			    	 
//			    	 System.out.println(tds.get(1).text()+":"+tds.get(2).text()+":"+tds.get(5).text()+":"+tds.get(6).select("div").attr("title")+":"+tds.get(7).select("div").attr("title")+":"+tds.get(8).text());			    
				}
//			     Thread.sleep(5*1000);
			}
		     
		 } catch (IOException e) {
		     e.printStackTrace();
//		 } catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}
	

}
