package com.stock.ztf.StockAnalysis.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.stock.ztf.StockAnalysis.beans.TradeHYStockDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeMRHYDataInfo;
import com.stock.ztf.StockAnalysis.mappers.StockBaseDataMapper;
import com.stock.ztf.StockAnalysis.utils.HttpUtil;
import com.stock.ztf.StockAnalysis.utils.JacksonJsonUtil;

@Service
public class IndustryData {
	
	private final static Logger logger = LoggerFactory.getLogger(IndustryData.class);
	
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private StockBaseDataMapper stockBaseDataMapper;

	@Autowired
	private HttpUtil httpUtil;
	
	/**
	 * 获取行业当日交易数据
	 */
	public void pickerTradeThatDayData() {
		String url = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&cmd=C._BKHY&sty=FPGBKI&st=c&sr=-1&p=1&ps=5000&cb=&js=[(x)]&token=7bc05d0d4c3c22ef9fca8c2a912d779c&v=0.9668453476452248";
		logger.debug("get Trade MRHY data start");
		logger.debug("url:" + url);
		try {
			List<String> stockTradeDataList = null;

			String stockTradeDataJson = httpUtil.getUrlStr(url, String.class);
			try {
				stockTradeDataList = JacksonJsonUtil.json2GenericObject(stockTradeDataJson.replaceAll("[)(]", ""),
						new TypeReference<List<String>>() {
						});
			} catch (Exception e) {
				logger.error("json2Object error:" + e.getMessage(), e);
			}

			if (stockTradeDataList == null || stockTradeDataList.isEmpty()) {
				logger.debug("get trade MRHY data failed");
				return;
			}
			logger.debug("get Trade MRHY data end");

			List<TradeMRHYDataInfo> stockTradeDataInfos = new ArrayList<TradeMRHYDataInfo>();

			for (String data : stockTradeDataList) {
				String[] datas = data.split(",");
				TradeMRHYDataInfo mrhyDataInfo = new TradeMRHYDataInfo();
				mrhyDataInfo.setCode(datas[1]);
				mrhyDataInfo.setZhName(datas[2]);
				mrhyDataInfo.setChg(Float.parseFloat(datas[3]));
				mrhyDataInfo.setValue(Long.parseLong(datas[4]));
				mrhyDataInfo.setRate(Float.parseFloat(datas[5]));
				mrhyDataInfo.setUp(Integer.parseInt(datas[6].split("[|]")[0]));
				mrhyDataInfo.setDown(Integer.parseInt(datas[6].split("[|]")[2]));
				stockTradeDataInfos.add(mrhyDataInfo);
			}

			logger.debug("Insert Trade real data start");

			/**
			 * 插入基本交易数据
			 */
			stockBaseDataMapper.insertOrUpdateTradeMRHYData(stockTradeDataInfos);
		} catch (Exception e) {
			logger.error("get trade MRHY data error:" + e.getMessage(), e);
		}
		logger.debug("Insert Trade MRHY data end");
	}

	/**
	 * 获取指定行业股票列表数据
	 */
	@Async
	public void pickerStockList(String hyCode) {
		String url = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&"
				+ "cmd=C.{hyCode}1&sty=FCOIATA&sortType=C&sortRule=-1&page=2&pageSize=500&"
				+ "js=[(x)]&token=7bc05d0d4c3c22ef9fca8c2a912d779c&jsName=quote_123&_g=0.9954481305384073";
		logger.debug("get Trade HYStock data start");
		logger.debug("url:" + url);
		try {
			List<String> stockTradeDataList = null;
			try {
				logger.debug("get hyCode:" + hyCode + " stocklist data.");
				String stockTradeDataJson = httpUtil.getUrlStr(url, hyCode);
				stockTradeDataList = JacksonJsonUtil.json2GenericObject(stockTradeDataJson.replaceAll("[)(]", ""),
						new TypeReference<List<String>>() {
						});
			} catch (Exception e) {
				logger.error("json2Object error:" + e.getMessage(), e);
			}
			if (stockTradeDataList == null || stockTradeDataList.isEmpty()) {
				logger.warn("get hyCode:" + hyCode + " stocklist data failed.");
				return;
			}
			logger.warn("get hyCode:" + hyCode + " stocklist data end.");

			List<TradeHYStockDataInfo> stockTradeDataInfos = new ArrayList<TradeHYStockDataInfo>();

			/**
			 * 解析行业股票列表数据
			 */
			for (String data : stockTradeDataList) {
				String[] datas = data.split(",");
				if (datas[1].startsWith("9") || datas[1].startsWith("2")) {
					continue;
				}
				TradeHYStockDataInfo hyStockDataInfo = new TradeHYStockDataInfo();
				hyStockDataInfo.setHyCode(hyCode);
				hyStockDataInfo.setCode(datas[1]);
				hyStockDataInfo.setZhName(datas[2]);
				hyStockDataInfo.setMarket(datas[0]);
				stockTradeDataInfos.add(hyStockDataInfo);
			}

			logger.debug("Insert Trade HYStock data start");

			/**
			 * 插入行业股票数据
			 */
			stockBaseDataMapper.insertOrUpdateTradeHYStockData(stockTradeDataInfos);
		} catch (Exception e) {
			logger.error("get trade HYStock data error:" + e.getMessage(), e);
		}
		logger.debug("Insert Trade HYStock data end");
	}
	
	/**
	 * 获取指定行业股票投资评级数据
	 */
	public void pickerStocCTData(String hyCode) {		
		String url = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&cmd=C.{hyCode}1"
		+ "&sty=GEMCPF&st=(BNum)&sr=-1&p=1&ps=500&cb=&js=[(x)]&token=3a965a43f705cf1d9ad7e1a3e429d622&rt=50318324";
		logger.debug("url:" + url);
		try {
			logger.debug("get hyCode:" + hyCode + " CT data from url start");
			List<String> stockCTDataList = null;
			try {
				logger.debug("get hyCode:" + hyCode + " CT data.");
				String stockCTDataJson = httpUtil.getUrlStr(url, hyCode);
				stockCTDataList = JacksonJsonUtil.json2GenericObject(stockCTDataJson,
						new TypeReference<List<String>>() {
						});
			} catch (Exception e) {
				logger.error("json2Object error:" + e.getMessage(), e);
			}
			if (stockCTDataList == null || stockCTDataList.isEmpty()) {
				logger.warn("get hyCode:" + hyCode + " CT data failed.");
				return;
			}
			logger.debug("get hyCode:" + hyCode + " CT data from url end");

			List<Map<String, Object>> stockCTDataInfos = new ArrayList<Map<String, Object>>();

			/**
			 * 解析行业股票投资评级数据
			 */
			for (String data : stockCTDataList) {
				String[] datas = data.split(",");
				if (datas[1].startsWith("9") || datas[1].startsWith("2")) {
					continue;
				}
				Map<String, Object> stockCTDataInfo = new HashMap<String,Object>();
				stockCTDataInfo.put("code",datas[1]);
				if (datas[5].contains("-")) {
					continue;
				}
				stockCTDataInfo.put("yanbaoshu",Integer.parseInt(datas[5]));
				stockCTDataInfo.put("mairu",Integer.parseInt(datas[6]));
				stockCTDataInfo.put("zengchi",Integer.parseInt(datas[7]));
				stockCTDataInfo.put("zhongxing",Integer.parseInt(datas[8]));
				stockCTDataInfo.put("jianchi",Integer.parseInt(datas[9]));
				stockCTDataInfo.put("maichu",Integer.parseInt(datas[10]));
				stockCTDataInfos.add(stockCTDataInfo);
			}

			logger.debug("Insert HYStock CT data start");

			/**
			 * 插入行业股票投资评级数据
			 */
			stockBaseDataMapper.insertOrUpdateStockCTData(stockCTDataInfos);
		} catch (Exception e) {
			logger.error("get HYStock CT data error:" + e.getMessage(), e);
		}
		logger.debug("Insert HYStock CT data end");
	}

}
