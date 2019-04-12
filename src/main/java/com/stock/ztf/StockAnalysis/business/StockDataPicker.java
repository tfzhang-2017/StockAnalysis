package com.stock.ztf.StockAnalysis.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.stock.ztf.StockAnalysis.beans.TradeBaseDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeMRHYDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeZJLSDataInfo;
import com.stock.ztf.StockAnalysis.beans.UrlDataInfo;
import com.stock.ztf.StockAnalysis.beans.ZhiBiaoData;
import com.stock.ztf.StockAnalysis.beans.ZhiBiaoEnum;
import com.stock.ztf.StockAnalysis.mappers.StockBaseDataMapper;
import com.stock.ztf.StockAnalysis.utils.DateUtil;
import com.stock.ztf.StockAnalysis.utils.FileUtils;
import com.stock.ztf.StockAnalysis.utils.HttpUtil;
import com.stock.ztf.StockAnalysis.utils.JacksonJsonUtil;
import com.stock.ztf.StockAnalysis.utils.TimeUtils;
import com.stock.ztf.StockAnalysis.utils.ZhiBiaoDataFactory;

/**
 * 采集股票基础数据
 * 
 * @author ztf
 *
 */
@Service
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

			logger.debug("Insert Trade data to tbl_trade_base_data start");
			/**
			 * 插入基本交易数据
			 */
			stockBaseDataMapper.insertOrUpdateTradeBaseData(stockTradeDataInfos);
		} catch (Exception e) {
			logger.error("get Code:" + code + " trade real data error:" + e.getMessage(), e);
		}
		logger.debug("Insert Trade Real data end");
	}

	/**
	 * 获取行业交易每日数据
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
	 * 获取股票交易历史数据
	 */
//	@Async
	public void pickerStockTradeBaseData(String code, String market, String kType) {
		String url = "http://pdfm2.eastmoney.com/EM_UBG_PDTI_Fast/api/js?"
				+ "id={code}{market}&TYPE={kType}&js=(x)&rtntype=5&isCR=false&authorityType=fa&r={random}";
		logger.debug(String.format("get Code:%1$s Trade Base %2$s data start", code,kType));
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
			logger.debug(String.format("get Code:%1$s Trade Base %2$s data end", code,kType));
			List<String> stockTradeDataList = urlData.getData();
			List<TradeBaseDataInfo> stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();
			/*
			 * 解析数据
			 */
			for (String stockData : stockTradeDataList) {
				/**
				 * "2017-10-24,8.74开,8.67收,8.76高,8.60低,31305量,2714万额,1.83%"
				 */
				String[] stockDatas = stockData.split(",");
				TradeBaseDataInfo dBaseDataInfo = new TradeBaseDataInfo();
				dBaseDataInfo.setCode(code);
				dBaseDataInfo.setTradeDate(stockDatas[0]);
				dBaseDataInfo.setDateType(kType);
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
				dBaseDataInfo.setAmplitude(Float.parseFloat(stockDatas[7].replaceAll("[%-]", "0")));
				stockTradeDataInfos.add(dBaseDataInfo);
			}
			
			logger.debug(String.format("get Code:%1$s Trade Base %2$s data count:%3$d", code,kType,stockTradeDataInfos.size()));
			logger.debug("Insert Trade data to tbl_trade_base_data start");
			/**
			 * 插入基本交易数据
			 */
			stockBaseDataMapper.insertOrUpdateTradeBaseData(stockTradeDataInfos);
			logger.debug("Insert Trade data to tbl_trade_base_data end");
		} catch (Exception e) {
			logger.error("get Code:" + code + " trade data error:" + e.getMessage(), e);
		}
	}

	/**
	 * 获取股票交易指标数据
	 */
//	@Async
	public void pickerStockTradeZhiBiaoData(String code, String market, String kType, String extend,
			ZhiBiaoData zhiBiaoData) {
		String url = "http://pdfm2.eastmoney.com/EM_UBG_PDTI_Fast/api/js?"
				+ "id={code}{market}&TYPE={kType}&js=(x)&rtntype=4&extend={extend}"
				+ "&isCR=false&check=kte&authorityType=fa&r={random}";
		logger.debug(String.format("开始获取股票(%1$s)的(%2$s)指标(%3$s)数据", code,extend,kType));
		logger.debug("数据接口url:" + url);
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
				return;
			}
			logger.debug("get Trade " + extend + " data end");
			List<ZhiBiaoData> tradeDataInfos = new ArrayList<ZhiBiaoData>();
			
			for (Map<String, String> stockData : stockTradeDataMap) {				
				/**
				 * {"time":"2010-11-18","open":"5.27","close":"4.92",
				 * "high":"5.39","low":"4.86","volume":"69760","amount":"2.42亿",
				 * "amplitude":"9.78%"}
				 */
				tradeDataInfos.add(zhiBiaoData.build(code, stockData.get("time"),kType, stockData.get(extend)));
			}
			logger.debug(String.format("Insert Trade %1$s data count:%2$d start", extend,tradeDataInfos.size()));			
			switch (ZhiBiaoEnum.getZhiBiaoEnum(extend)) {
				case cma:
					stockBaseDataMapper.insertOrUpdateTradeCMAData(tradeDataInfos);
					break;
				case macd:
					stockBaseDataMapper.insertOrUpdateTradeMACDData(tradeDataInfos);
					break;
				case boll:
					stockBaseDataMapper.insertOrUpdateTradeBollData(tradeDataInfos);
					break;
				default:
					break;
			}
			logger.debug("Insert Trade " + extend + " data end");
			

		} catch (Exception e) {
			logger.error("get Code:" + code + " trade " + extend + " error:" + e.getMessage(), e);
		}
		return;
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
//	@Async
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
			
			List<TradeZJLSDataInfo> stockTradeDataInfos = new ArrayList<TradeZJLSDataInfo>();
			/**
			 * 解析数据
			 */
			for (String data : stockTradeZJLSDatas) {
				data = data.replaceAll("%", "").trim();
				String[] datas = data.split(",");
				/**
				 * 股票代码，交易日，净流入额，净流入占比（主力：超大单：大单：中单：小单），收盘价，涨跌幅
				 */
				TradeZJLSDataInfo tradeZJLSDataInfo = new TradeZJLSDataInfo();
				tradeZJLSDataInfo.setCode(code);
				tradeZJLSDataInfo.setTradeDate(datas[0]);
				tradeZJLSDataInfo.setDateType("K");
				tradeZJLSDataInfo.setZhuInflows(Float.parseFloat("-".equals(datas[1]) ? "0" : datas[1]));
				tradeZJLSDataInfo.setZhuInflowsRatio(Float.parseFloat("-".equals(datas[2]) ? "0" : datas[2]));
				tradeZJLSDataInfo.setChaoInflows(Float.parseFloat("-".equals(datas[3]) ? "0" : datas[3]));
				tradeZJLSDataInfo.setChaoInflowsRatio(Float.parseFloat("-".equals(datas[4]) ? "0" : datas[4]));
				tradeZJLSDataInfo.setDaInflows(Float.parseFloat("-".equals(datas[5]) ? "0" : datas[5]));
				tradeZJLSDataInfo.setDaInflowsRatio(Float.parseFloat("-".equals(datas[6]) ? "0" : datas[6]));
				tradeZJLSDataInfo.setZhongInflows(Float.parseFloat("-".equals(datas[7]) ? "0" : datas[7]));
				tradeZJLSDataInfo.setZhongInflowsRatio(Float.parseFloat("-".equals(datas[8]) ? "0" : datas[8]));
				tradeZJLSDataInfo.setXiaoInflows(Float.parseFloat("-".equals(datas[9]) ? "0" : datas[9]));
				tradeZJLSDataInfo.setXiaoInflowsRatio(Float.parseFloat("-".equals(datas[10]) ? "0" : datas[10]));
				tradeZJLSDataInfo.setClosing(Float.parseFloat("-".equals(datas[11]) ? "0" : datas[11]));
				tradeZJLSDataInfo.setChg(Float.parseFloat("-".equals(datas[12]) ? "0" : datas[12]));
				tradeZJLSDataInfo.setWs(DateUtil.getWeekdayIndex(datas[0]));
				stockTradeDataInfos.add(tradeZJLSDataInfo);
			}
			
			/**
			 * 插入历史资金数据
			 */
			stockBaseDataMapper.insertOrUpdateTradeZJLSData(stockTradeDataInfos);
		} catch (Exception e) {
			logger.error("get Code:" + code + " trade data error:" + e.getMessage(), e);
		}
		logger.debug("Insert code " + code + " Trade ZJLS data end");
	}

	/**
	 * 计算股票资金数据
	 */
	public void calZJData(String code) {

		logger.debug("cal Code:{} ZJ data start",code);
		try {
			/**
			 * 计算资金数据
			 */
			logger.debug("cal Code:{} ZJ data affectRows: {}",code ,stockBaseDataMapper.calZJData(code));
		} catch (Exception e) {
			logger.error("cal Code:" + code + " ZJ data error:" + e.getMessage(), e);
		}
		logger.debug("cal Code:{} ZJ data end",code);
	}
	
	/**
	 * 采集股票基础数据
	 * @param code
	 * @param market
	 */
	public void pickerStockBaseData(String code, String market) {

		String dataTypes = "K|WK|MK";
		String zhiBiaoTypes = "cma|macd|boll";

		// logger.debug("start get " + code + " day Trade real data ");
		// stockDataPicker.pickerStockTradeRealData(code, market);

		logger.debug(String.format("采集股票(%1$s)所有数据开始 ", code));
		// 采集股票K线数据
		for (String dataType : dataTypes.split("[|]")) {
			logger.debug(String.format("开始采集股票(%1$s) %2$s线数据 ", code, dataType));
			pickerStockTradeBaseData(code, market, dataType);
		}

		// 采集股票各指标数据
		for (String zhiBiaoType : zhiBiaoTypes.split("[|]")) {
			for (String dataType : dataTypes.split("[|]")) {
				logger.debug(String.format("开始采集股票(%1$s) %2$s %3$s线数据 ", code, zhiBiaoType, dataType));
				pickerStockTradeZhiBiaoData(code, market, dataType, zhiBiaoType, ZhiBiaoDataFactory.build(zhiBiaoType));
			}
		}

		// 采集股票历史资金数据
		pickerStockZJLSData(code, market);
		// 计算股票历史资金数据
		calZJData(code);

		logger.debug(String.format("采集股票(%1$s)所有数据完成 ", code));
	}
}
