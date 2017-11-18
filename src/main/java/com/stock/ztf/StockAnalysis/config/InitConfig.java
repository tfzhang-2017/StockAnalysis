package com.stock.ztf.StockAnalysis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import com.stock.ztf.StockAnalysis.mappers.ProxyDataMapper;
import com.stock.ztf.StockAnalysis.utils.HttpUtil;

@Configuration
public class InitConfig implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private ProxyDataMapper proxyDataMapper;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		HttpUtil.setProxys(proxyDataMapper.getProxyData());
	}

}
