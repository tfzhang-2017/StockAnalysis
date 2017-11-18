package com.stock.ztf.StockAnalysis.business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.stock.ztf.StockAnalysis.beans.TradeBaseDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeHYStockDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeMRHYDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeZJLSDataInfo;
import com.stock.ztf.StockAnalysis.beans.UrlDataInfo;
import com.stock.ztf.StockAnalysis.beans.ZhiBiaoEnum;
import com.stock.ztf.StockAnalysis.mappers.StockBaseDataMapper;
import com.stock.ztf.StockAnalysis.utils.DateUtil;
import com.stock.ztf.StockAnalysis.utils.FileUtils;
import com.stock.ztf.StockAnalysis.utils.HttpUtil;
import com.stock.ztf.StockAnalysis.utils.JacksonJsonUtil;
import com.stock.ztf.StockAnalysis.utils.TimeUtils;

/**
 * 采集股票基础数据
 * 
 * @author ztf
 *
 */
@Component
public class StockDataPicker {

	private final static Logger logger = LoggerFactory.getLogger(StockDataPicker.class);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private StockBaseDataMapper stockBaseDataMapper;

	@Autowired
	private HttpUtil httpUtil;

	/**
	 * 获取股票交易实时数据
	 */
	@Async
	public void pickerStockTradeRealData(String code, String market) {
		String url = "http://nuff.eastmoney.com/EM_Finance2015TradeInterface/JS.ashx?"
				+ "id={code}{market}&cb=&token=4f1862fc3b5e77c150a2b985b12db0fd&_={time}";
		logger.debug("get Trade Real data start");
		logger.debug("url:" + url);
		try {
			Map<String, List<String>> stockTradeDataMap = null;
			String time = TimeUtils.getTime() + "";
			logger.debug("get Code:" + code + " day trade data. timeFloor:" + time);
			String stockTradeDataJson = httpUtil.getUrlStr(url, code, market, time);
			try {
				stockTradeDataMap = JacksonJsonUtil.json2GenericObject(stockTradeDataJson.replaceAll("[)(]", ""),
						new TypeReference<Map<String, List<String>>>() {
						});
			} catch (Exception e) {
				logger.error("json2Object error:" + e.getMessage(), e);
			}
			if (stockTradeDataMap == null || stockTradeDataMap.isEmpty()) {
				logger.debug("get Code:" + code + " trade real data failed");
				return;
			}
			logger.debug("get Trade real data end");

			List<TradeBaseDataInfo> stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();

			List<String> tradeDatas = stockTradeDataMap.get("Value");

			/**
			 * {"time":"2010-11-18","open":"5.27","close":"4.92",
			 * "high":"5.39","low":"4.86","volume":"69760","amount":"2.42亿",
			 * "amplitude":"9.78%"}
			 */
			TradeBaseDataInfo dBaseDataInfo = new TradeBaseDataInfo();
			dBaseDataInfo.setCode(tradeDatas.get(1));
			dBaseDataInfo.setTradeDate(tradeDatas.get(49).split(" ")[0]);
			dBaseDataInfo.setDateType("day");
			dBaseDataInfo.setOpen(Float.parseFloat(tradeDatas.get(28)));
			dBaseDataInfo.setClose(Float.parseFloat(tradeDatas.get(25)));
			dBaseDataInfo.setHigh(Float.parseFloat(tradeDatas.get(30)));
			dBaseDataInfo.setLow(Float.parseFloat(tradeDatas.get(32)));
			dBaseDataInfo.setVolume(Long.parseLong(tradeDatas.get(31)));
			if (tradeDatas.get(35).endsWith("万")) {
				dBaseDataInfo.setAmount(Float.parseFloat(tradeDatas.get(35).replaceAll("[\u4e00-\u9fa5]", "")));
			} else if (tradeDatas.get(35).endsWith("亿")) {
				dBaseDataInfo.setAmount(Float.parseFloat(tradeDatas.get(35).replaceAll("[\u4e00-\u9fa5]", "")) * 10000);
			} else {
				dBaseDataInfo.setAmount(Float.parseFloat(tradeDatas.get(35)) / 10000);
			}
			// dBaseDataInfo.setAmount(Float.parseFloat(tradeDatas.get(35)));
			dBaseDataInfo.setAmplitude(Float.parseFloat(tradeDatas.get(50)));
			stockTradeDataInfos.add(dBaseDataInfo);

			logger.debug("Insert Trade real data start");

			String tblName = "tbl_trade_base_data";
			String dataYear = DateUtil.getYear();
			logger.debug("Insert Trade data to " + tblName + "_" + dataYear);
			/**
			 * 创建对应年份的表
			 */
			if (stockBaseDataMapper.getStockTradeTblCount(tblName + "_" + dataYear) == 0) {
				stockBaseDataMapper.createStockTradeTbl(tblName, dataYear);
			}
			/**
			 * 插入基本交易数据
			 */
			stockBaseDataMapper.insertOrUpdateTradeBaseData(tblName + "_" + dataYear, stockTradeDataInfos);
		} catch (Exception e) {
			logger.error("get Code:" + code + " trade real data error:" + e.getMessage(), e);
		}
		logger.debug("Insert Trade Real data end");
	}

	/**
	 * 获取股票交易每日行业数据
	 */
	public void pickerStockTradeMRHYData() {
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
	 * 获取行业股票列表 数据
	 */
	@Async
	public void pickerStockTradeHYStockData(String hyCode) {
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
	 * 获取股票交易历史数据
	 */
	@Async
	public void pickerStockTradeBaseData(String code, String market, String dataType, String kType) {
		String url = "http://pdfm2.eastmoney.com/EM_UBG_PDTI_Fast/api/js?"
				+ "id={code}{market}&TYPE={kType}&js=(x)&rtntype=5&isCR=false&authorityType=fa&r={random}";
		logger.debug("get Trade Base data start");
		logger.debug("url:" + url);
		try {
			UrlDataInfo urlData = null;
			String random = Math.random() + "";
			logger.debug("get Code:" + code + " day trade data. random:" + random);
			String stockTradeDataJson = httpUtil.getUrlStr(url, code, market, kType, random);
			try {
				urlData = JacksonJsonUtil.json2GenericObject(stockTradeDataJson, new TypeReference<UrlDataInfo>() {
				});
			} catch (Exception e) {
				logger.error("json2Object error:" + e.getMessage(), e);
			}
			if (urlData == null) {
				logger.debug("get Code:" + code + " trade data failed");
				return;
			}
			logger.debug("get Trade Base data end");
			List<String> stockTradeDataList = urlData.getData();
			List<String> years = new ArrayList<String>();
			List<TradeBaseDataInfo> stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();
			LinkedHashMap<String, List<TradeBaseDataInfo>> stockTradeDataInfoMap = new LinkedHashMap<String, List<TradeBaseDataInfo>>();
			/**
			 * 按照年份分组数据
			 */
			for (String stockData : stockTradeDataList) {
				/**
				 * "2017-10-24,8.74开,8.67收,8.76高,8.60低,31305量,2714万额,1.83%"
				 */
				String[] stockDatas = stockData.split(",");
				String year = stockDatas[0].substring(0, 4);
				/**
				 * 2013年前数据不关注
				 */
				if (!(year.compareTo("2013") > 0)) {
					continue;
				}
				if (!stockTradeDataInfoMap.containsKey(year)) {
					if (stockTradeDataInfoMap.size() != 0) {
						logger.debug("Day Trade data:" + stockTradeDataInfoMap.keySet().toString() + ":"
								+ stockTradeDataInfos.size());
					}
					stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();
					stockTradeDataInfoMap.put(year, stockTradeDataInfos);
				}

				TradeBaseDataInfo dBaseDataInfo = new TradeBaseDataInfo();
				dBaseDataInfo.setCode(code);
				dBaseDataInfo.setTradeDate(stockDatas[0]);
				dBaseDataInfo.setDateType(dataType);
				dBaseDataInfo.setOpen(Float.parseFloat(stockDatas[1]));
				dBaseDataInfo.setClose(Float.parseFloat(stockDatas[2]));
				dBaseDataInfo.setHigh(Float.parseFloat(stockDatas[3]));
				dBaseDataInfo.setLow(Float.parseFloat(stockDatas[4]));
				dBaseDataInfo.setVolume(Long.parseLong(stockDatas[5].replaceAll("\\.\\d+", "")));
				/*
				 * 将成交额统一换算成以万为单位
				 */
				if (stockDatas[6].endsWith("万")) {
					dBaseDataInfo.setAmount(Float.parseFloat(stockDatas[6].replaceAll("[\u4e00-\u9fa5]", "")));
				} else if (stockDatas[6].endsWith("亿")) {
					dBaseDataInfo.setAmount(Float.parseFloat(stockDatas[6].replaceAll("[\u4e00-\u9fa5]", "")) * 10000);
				} else {
					dBaseDataInfo.setAmount(Float.parseFloat(stockDatas[6]) / 10000);
				}
				// dBaseDataInfo.setAmount(Float.parseFloat(stockData.get("amount").replaceAll("[\u4e00-\u9fa5]",
				// "")));
				dBaseDataInfo.setAmplitude(Float.parseFloat(stockDatas[7].replaceAll("[%-]", "0")));
				stockTradeDataInfos.add(dBaseDataInfo);
			}
			logger.debug("Insert Trade Base data start");
			for (String dataYear : stockTradeDataInfoMap.keySet()) {
				String tblName = "tbl_trade_base_data";
				logger.debug("Insert Trade data to " + tblName + "_" + dataYear);
				/**
				 * 创建对应年份的表
				 */
				if (!years.contains(dataYear)) {
					if (stockBaseDataMapper.getStockTradeTblCount(tblName + "_" + dataYear) == 0) {
						stockBaseDataMapper.createStockTradeTbl(tblName, dataYear);
					}
				}
				/**
				 * 插入基本交易数据
				 */
				stockBaseDataMapper.insertOrUpdateTradeBaseData(tblName + "_" + dataYear,
						stockTradeDataInfoMap.get(dataYear));
			}
		} catch (Exception e) {
			logger.error("get Code:" + code + " trade data error:" + e.getMessage(), e);
		}
		logger.debug("Insert Trade Base data end");
	}

	/**
	 * 获取股票交易指标数据
	 */
	@Async
	public <T> T pickerStockTradeZhiBiaoData(String code, String market, String kType, String extend, String dataType,
			Class<T> tClass) {
		String url = "http://pdfm2.eastmoney.com/EM_UBG_PDTI_Fast/api/js?"
				+ "id={code}{market}&TYPE={kType}&js=(x)&rtntype=4&extend={extend}"
				+ "&isCR=false&check=kte&authorityType=fa&r={random}";
		logger.debug("get Trade " + extend + " data start");
		logger.debug("url:" + url);
		try {
			List<Map<String, String>> stockTradeDataMap = null;

			String random = Math.random() + "";
			logger.debug("get Code:" + code + " day trade data. random:" + random);
			String stockTradeDataJson = httpUtil.getUrlStr(url, code, market, kType, extend, random);
			try {
				stockTradeDataMap = JacksonJsonUtil.json2GenericObject(stockTradeDataJson,
						new TypeReference<List<Map<String, String>>>() {
						});
			} catch (Exception e) {
				logger.error("json2Object error:" + e.getMessage(), e);
			}

			if (stockTradeDataMap == null) {
				logger.debug("get Code:" + code + " trade " + extend + " data failed");
				return null;
			}
			logger.debug("get Trade " + extend + " data end");
			List<String> years = new ArrayList<String>();
			List<T> tradeDataInfos = new ArrayList<T>();
			LinkedHashMap<String, List<T>> stockTradeDataInfoMap = new LinkedHashMap<String, List<T>>();
			/**
			 * 按照年份分组数据
			 */
			for (Map<String, String> stockData : stockTradeDataMap) {

				String year = stockData.get("time").substring(0, 4);
				/**
				 * 2013年前数据不关注
				 */
				if (!(year.compareTo("2013") > 0)) {
					continue;
				}
				if (!stockTradeDataInfoMap.containsKey(year)) {
					if (stockTradeDataInfoMap.size() != 0) {
						logger.debug("Day Trade data:" + stockTradeDataInfoMap.keySet().toString() + ":"
								+ tradeDataInfos.size());
					}
					tradeDataInfos = new ArrayList<T>();
					stockTradeDataInfoMap.put(year, tradeDataInfos);
				}
				/**
				 * {"time":"2010-11-18","open":"5.27","close":"4.92",
				 * "high":"5.39","low":"4.86","volume":"69760","amount":"2.42亿",
				 * "amplitude":"9.78%"}
				 */
				T tradeDataInfo = tClass.newInstance();
				tClass.getMethod("setCode", String.class).invoke(tradeDataInfo, code);
				tClass.getMethod("setTradeDate", String.class).invoke(tradeDataInfo, stockData.get("time"));
				tClass.getMethod("setDateType", String.class).invoke(tradeDataInfo, dataType);
				switch (ZhiBiaoEnum.getZhiBiaoEnum(extend)) {
				case cma:
					String[] cmas = stockData.get(extend).replaceAll("[^\\d\\.\\-,]", "").replaceAll("-", "0")
							.split(",");
					tClass.getMethod("setMA5", float.class).invoke(tradeDataInfo, Float.parseFloat(cmas[0]));
					tClass.getMethod("setMA10", float.class).invoke(tradeDataInfo, Float.parseFloat(cmas[1]));
					tClass.getMethod("setMA20", float.class).invoke(tradeDataInfo, Float.parseFloat(cmas[2]));
					tClass.getMethod("setMA30", float.class).invoke(tradeDataInfo, Float.parseFloat(cmas[3]));
					break;
				case macd:
					String[] macds = stockData.get(extend).replaceAll("[^\\d\\.\\-,]", "").split(",");
					tClass.getMethod("setDIFF", float.class).invoke(tradeDataInfo, Float.parseFloat(macds[0]));
					tClass.getMethod("setDEA", float.class).invoke(tradeDataInfo, Float.parseFloat(macds[1]));
					tClass.getMethod("setMACD", float.class).invoke(tradeDataInfo, Float.parseFloat(macds[2]));
					break;

				default:
					break;
				}
				tradeDataInfos.add(tradeDataInfo);
			}
			logger.debug("Insert Trade " + extend + " data start");
			for (String dataYear : stockTradeDataInfoMap.keySet()) {
				String tblName = "tbl_trade_" + extend + "_data";
				logger.debug("Insert Trade data to " + tblName + "_" + dataYear);
				/**
				 * 创建对应年份的表
				 */
				if (!years.contains(dataYear)) {
					if (stockBaseDataMapper.getStockTradeTblCount(tblName + "_" + dataYear) == 0) {
						stockBaseDataMapper.createStockTradeTbl(tblName, dataYear);
					}
				}
				/**
				 * 插入基本交易数据
				 */
				switch (ZhiBiaoEnum.getZhiBiaoEnum(extend)) {
				case cma:
					stockBaseDataMapper.insertOrUpdateTradeCMAData(tblName + "_" + dataYear,
							stockTradeDataInfoMap.get(dataYear));
					break;
				case macd:
					stockBaseDataMapper.insertOrUpdateTradeMACDData(tblName + "_" + dataYear,
							stockTradeDataInfoMap.get(dataYear));
					break;

				default:
					break;
				}

			}
		} catch (Exception e) {
			logger.error("get Code:" + code + " trade " + extend + " error:" + e.getMessage(), e);
		}
		logger.debug("Insert Trade " + extend + " data end");
		return null;
	}

	/**
	 * 从文件读取股票代码数据，插入数据库
	 */
	public void pickerStockCodeData() {
		logger.debug("Insert Stock Code data start");
		String filePath = "D:\\Desktop\\Table.txt";
		logger.debug(filePath);

		List<String> stockCodeDatas = FileUtils.readline(filePath);
		for (String codeData : stockCodeDatas) {
			String[] datas = codeData.split("\t");
			stockBaseDataMapper.insertOrUpdateStockCode(datas[0], datas[1]);
		}
		logger.debug("Insert Stock Code data end");
	}

	/**
	 * 获取股票历史资金数据
	 */
	@Async
	public void pickerStockZJLSData(String code, String market) {
		String url = "http://ff.eastmoney.com/EM_CapitalFlowInterface/api/js?"
				+ "type=hff&rtntype=2&js=(x)&cb=&check=TMLBMSPROCR"
				+ "&acces_token=1942f5da9b46b069953c873404aad4b5&id={code}{market}&_=1506611327422";
		logger.debug("Insert Trade ZJLS data start");
		logger.debug(url);

		try {
			List<String> stockTradeZJLSDatas = null;
			logger.debug("get Code:" + code + " day trade ZJLS data.");
			String stockTradeZJLSJson = httpUtil.getUrlStr(url, code, market);
			try {
				stockTradeZJLSDatas = JacksonJsonUtil.json2GenericObject(stockTradeZJLSJson,
						new TypeReference<List<String>>() {
						});
			} catch (Exception e) {
				logger.error("json2Object error:" + e.getMessage(), e);
			}

			if (stockTradeZJLSDatas == null) {
				logger.debug("get Code:" + code + " day trade ZJLS data failed");
				return;
			}
			List<String> years = new ArrayList<String>();
			LinkedHashMap<String, List<TradeZJLSDataInfo>> stockTradeDataInfoMap = new LinkedHashMap<String, List<TradeZJLSDataInfo>>();
			List<TradeZJLSDataInfo> stockTradeDataInfos = new ArrayList<TradeZJLSDataInfo>();
			/**
			 * 按照年份分组数据
			 */
			for (String data : stockTradeZJLSDatas) {
				data = data.replaceAll("%", "").trim();
				String[] datas = data.split(",");
				String year = datas[0].substring(0, 4);
				if (!stockTradeDataInfoMap.containsKey(year)) {
					if (stockTradeDataInfoMap.size() != 0) {
						logger.debug("Day Trade data:" + stockTradeDataInfoMap.keySet().toString() + ":"
								+ stockTradeDataInfos.size());
					}
					stockTradeDataInfos = new ArrayList<TradeZJLSDataInfo>();
					stockTradeDataInfoMap.put(year, stockTradeDataInfos);
				}
				/**
				 * 股票代码，交易日，净流入额，净流入占比（主力：超大单：大单：中单：小单），收盘价，涨跌幅
				 */
				TradeZJLSDataInfo tradeZJLSDataInfo = new TradeZJLSDataInfo();
				tradeZJLSDataInfo.setCode(code);
				tradeZJLSDataInfo.setTradeDate(datas[0]);
				tradeZJLSDataInfo.setDateType("day");
				tradeZJLSDataInfo.setZhuInflows(Float.parseFloat(datas[1]));
				tradeZJLSDataInfo.setZhuInflowsRatio(Float.parseFloat(datas[2]));
				tradeZJLSDataInfo.setChaoInflows(Float.parseFloat(datas[3]));
				tradeZJLSDataInfo.setChaoInflowsRatio(Float.parseFloat(datas[4]));
				tradeZJLSDataInfo.setDaInflows(Float.parseFloat(datas[5]));
				tradeZJLSDataInfo.setDaInflowsRatio(Float.parseFloat(datas[6]));
				tradeZJLSDataInfo.setZhongInflows(Float.parseFloat(datas[7]));
				tradeZJLSDataInfo.setZhongInflowsRatio(Float.parseFloat(datas[8]));
				tradeZJLSDataInfo.setXiaoInflows(Float.parseFloat(datas[9]));
				tradeZJLSDataInfo.setXiaoInflowsRatio(Float.parseFloat(datas[10]));
				tradeZJLSDataInfo.setClosing(Float.parseFloat(datas[11]));
				tradeZJLSDataInfo.setChg(Float.parseFloat(datas[12]));
				stockTradeDataInfos.add(tradeZJLSDataInfo);
			}
			for (String dataYear : stockTradeDataInfoMap.keySet()) {
				String tblName = "tbl_trade_zjls_data";
				logger.debug("Insert Trade ZJLS data:" + tblName + "_" + dataYear);
				/**
				 * 创建对应年份的表
				 */
				if (!years.contains(dataYear)) {
					if (stockBaseDataMapper.getStockTradeTblCount(tblName + "_" + dataYear) == 0) {
						stockBaseDataMapper.createStockTradeTbl(tblName, dataYear);
					}
				}
				/**
				 * 插入基本交易数据
				 */
				stockBaseDataMapper.insertOrUpdateTradeZJLSData(tblName + "_" + dataYear,
						stockTradeDataInfoMap.get(dataYear));
			}
		} catch (

		Exception e) {
			logger.error("get Code:" + code + " trade data error:" + e.getMessage(), e);
		}
		logger.debug("Insert code " + code + " Trade ZJLS data end");
	}

}
