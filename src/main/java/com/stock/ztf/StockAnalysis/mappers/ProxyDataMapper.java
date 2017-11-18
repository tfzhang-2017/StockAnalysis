package com.stock.ztf.StockAnalysis.mappers;

import java.util.List;

import com.stock.ztf.StockAnalysis.beans.ProxyData;

public interface ProxyDataMapper {

	List<ProxyData> getProxyData();
	
	int addProxyData(ProxyData proxyData);
}
