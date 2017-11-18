package com.stock.ztf.StockAnalysis.business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.stock.ztf.StockAnalysis.beans.TradeBaseDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeZJLSDataInfo;
import com.stock.ztf.StockAnalysis.mappers.StockBaseDataMapper;
import com.stock.ztf.StockAnalysis.utils.FileUtils;
import com.stock.ztf.StockAnalysis.utils.JacksonJsonUtil;
import com.stock.ztf.StockAnalysis.utils.ZLibUtils;

/**
 * 采集大盘基础数据
 * 
 * @author ztf
 *
 */
@Component
public class DaPanDataPicker {

	private final static Logger logger = LoggerFactory.getLogger(DaPanDataPicker.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private StockBaseDataMapper stockBaseDataMapper;

	/**
	 * 老方法，暂时不用
	 * 获取股票交易基本数据 开盘价，收盘价，最高价，最低价，成交量，成交额
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 10 * oneMinute)
	public void pickerStockTradeBaseData_backup() {
		String url = "http://hq2fls.eastmoney.com/EM_Quote2010PictureApplication/Flash.aspx?"
				+ "Type=CHD&ID=#{code}1&lastnum=300&r={random}";
		logger.debug("Insert DaPan Base data start");
		logger.debug(url);
		// HttpHeaders headers = new HttpHeaders();
		// headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		// HttpEntity<String> entity = new HttpEntity<String>(headers);
		// ResponseEntity<byte[]> response =
		// restTemplate.exchange(url,HttpMethod.GET, entity, byte[].class);
		// byte[] result = ZLibUtils.decompress(response.getBody());
		// System.out.println(result);

		String code = "000001";
		try {
			byte[] stockTradeDatabyte = null;
			int flag = 0;
			while (stockTradeDatabyte == null) {
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
					String random = Math.random() + "";
					logger.debug("get Code:" + code + " day trade data. random:" + random);
					stockTradeDatabyte = restTemplate.getForObject(url, byte[].class, code, random);
					flag++;
				} catch (Exception e) {
					flag++;
					logger.error("get url[" + url + "] error:" + e.getLocalizedMessage());
				}
			}
			if (stockTradeDatabyte == null) {
				logger.debug("get Code:" + code + " day DaPan data failed");
				return;
			}
			byte[] result = ZLibUtils.decompress(stockTradeDatabyte);
			String stockTradeDataStr = new String(result);
			String[] stockTradeDatas = stockTradeDataStr.split("\n");
			List<String> years = new ArrayList<String>();
			List<TradeBaseDataInfo> stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();
			LinkedHashMap<String, List<TradeBaseDataInfo>> stockTradeDataInfoMap = new LinkedHashMap<String, List<TradeBaseDataInfo>>();
			/**
			 * 按照年份分组数据
			 */
			for (String data : stockTradeDatas) {
				data = data.trim();
				String[] datas = data.split(",");
				String year = datas[0].substring(0, 4);
				if (!stockTradeDataInfoMap.containsKey(year)) {
					if (stockTradeDataInfoMap.size() != 0) {
						logger.debug("Day Trade data:" + stockTradeDataInfoMap.keySet().toString() + ":"
								+ stockTradeDataInfos.size());
					}
					stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();
					stockTradeDataInfoMap.put(year, stockTradeDataInfos);
				}
				/**
				 * `code`,tradeDate,opened,closing,maximum,minimum,volume,turnVolume
				 */
				TradeBaseDataInfo dBaseDataInfo = new TradeBaseDataInfo();
				dBaseDataInfo.setCode(code);
				dBaseDataInfo.setTradeDate(datas[0]);
				dBaseDataInfo.setDateType("day");
//				dBaseDataInfo.setOpened(Float.parseFloat(datas[1]));
//				dBaseDataInfo.setClosing(Float.parseFloat(datas[2]));
//				dBaseDataInfo.setMaximum(Float.parseFloat(datas[3]));
//				dBaseDataInfo.setMinimum(Float.parseFloat(datas[4]));
//				dBaseDataInfo.setVolume(Long.parseLong(datas[5]));
//				dBaseDataInfo.setTurnVolume(Long.parseLong(datas[6]));
				stockTradeDataInfos.add(dBaseDataInfo);
			}
			for (String dataYear : stockTradeDataInfoMap.keySet()) {
				String tblName = "tbl_trade_base_data";
				logger.debug("Insert Day Trade data to " + tblName + "_" + dataYear);
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
			logger.error("get Code:" + code + " day trade data error:" + e.getLocalizedMessage());
		}
		logger.debug("Insert Trade Base data end");
	}
	
	/**
	 * 获取股票交易基本数据 开盘价，收盘价，最高价，最低价，成交量，成交额
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 10 * oneMinute)
	public void pickerStockTradeBaseData() {
		String url = "http://hq2fls.eastmoney.com/EM_Quote2010PictureApplication/Flash.aspx?"
				+ "Type=CHD&ID=#{code}1&lastnum=300&r={random}";
		logger.debug("Insert DaPan Base data start");
		logger.debug(url);
		// HttpHeaders headers = new HttpHeaders();
		// headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		// HttpEntity<String> entity = new HttpEntity<String>(headers);
		// ResponseEntity<byte[]> response =
		// restTemplate.exchange(url,HttpMethod.GET, entity, byte[].class);
		// byte[] result = ZLibUtils.decompress(response.getBody());
		// System.out.println(result);

		String code = "000001";
		try {
			byte[] stockTradeDatabyte = null;
			int flag = 0;
			while (stockTradeDatabyte == null) {
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
					String random = Math.random() + "";
					logger.debug("get Code:" + code + " day trade data. random:" + random);
					stockTradeDatabyte = restTemplate.getForObject(url, byte[].class, code, random);
					flag++;
				} catch (Exception e) {
					flag++;
					logger.error("get url[" + url + "] error:" + e.getLocalizedMessage());
				}
			}
			if (stockTradeDatabyte == null) {
				logger.debug("get Code:" + code + " day DaPan data failed");
				return;
			}
			byte[] result = ZLibUtils.decompress(stockTradeDatabyte);
			String stockTradeDataStr = new String(result);
			String[] stockTradeDatas = stockTradeDataStr.split("\n");
			List<String> years = new ArrayList<String>();
			List<TradeBaseDataInfo> stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();
			LinkedHashMap<String, List<TradeBaseDataInfo>> stockTradeDataInfoMap = new LinkedHashMap<String, List<TradeBaseDataInfo>>();
			/**
			 * 按照年份分组数据
			 */
			for (String data : stockTradeDatas) {
				data = data.trim();
				String[] datas = data.split(",");
				String year = datas[0].substring(0, 4);
				if (!stockTradeDataInfoMap.containsKey(year)) {
					if (stockTradeDataInfoMap.size() != 0) {
						logger.debug("Day Trade data:" + stockTradeDataInfoMap.keySet().toString() + ":"
								+ stockTradeDataInfos.size());
					}
					stockTradeDataInfos = new ArrayList<TradeBaseDataInfo>();
					stockTradeDataInfoMap.put(year, stockTradeDataInfos);
				}
				/**
				 * `code`,tradeDate,opened,closing,maximum,minimum,volume,turnVolume
				 */
				TradeBaseDataInfo dBaseDataInfo = new TradeBaseDataInfo();
				dBaseDataInfo.setCode(code);
				dBaseDataInfo.setTradeDate(datas[0]);
				dBaseDataInfo.setDateType("day");
//				dBaseDataInfo.setOpened(Float.parseFloat(datas[1]));
//				dBaseDataInfo.setClosing(Float.parseFloat(datas[2]));
//				dBaseDataInfo.setMaximum(Float.parseFloat(datas[3]));
//				dBaseDataInfo.setMinimum(Float.parseFloat(datas[4]));
//				dBaseDataInfo.setVolume(Long.parseLong(datas[5]));
//				dBaseDataInfo.setTurnVolume(Long.parseLong(datas[6]));
				stockTradeDataInfos.add(dBaseDataInfo);
			}
			for (String dataYear : stockTradeDataInfoMap.keySet()) {
				String tblName = "tbl_trade_base_data";
				logger.debug("Insert Day Trade data to " + tblName + "_" + dataYear);
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
			logger.error("get Code:" + code + " day trade data error:" + e.getLocalizedMessage());
		}
		logger.debug("Insert Trade Base data end");
	}

	/**
	 * 从文件读取股票代码数据，插入数据库
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 60 * oneMinute)
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
//	@Scheduled(initialDelay = 5 * oneSecond, fixedRate = 60 * oneMinute)
	public void pickerStockZJLSData() {
		String url = "http://ff.eastmoney.com/EM_CapitalFlowInterface/api/js?"
				+ "type=hff&rtntype=2&js=(x)&cb=&check=TMLBMSPROCR"
				+ "&acces_token=1942f5da9b46b069953c873404aad4b5&id={code}2&_=1506611327422";
		logger.debug("Insert Trade ZJLS data start");
		logger.debug(url);
		List<String> codeDatas = stockBaseDataMapper.getStockCodeData("^0|^6");
		for (String code : codeDatas) {
			try {
				List<String> stockTradeZJLSDatas = null;
				int flag = 0;
				while (stockTradeZJLSDatas == null) {
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
						logger.debug("get Code:" + code + " day trade ZJLS data.");
						String stockTradeZJLSJson = restTemplate.getForObject(url, String.class, code);
						stockTradeZJLSDatas = JacksonJsonUtil.json2GenericObject(stockTradeZJLSJson,
								new TypeReference<List<String>>() {
								});
						flag++;
					} catch (Exception e) {
						flag++;
						logger.error("get url[" + url + "] error:" + e.getLocalizedMessage());
					}
				}
				if (stockTradeZJLSDatas == null) {
					logger.debug("get Code:" + code + " day trade ZJLS data failed");
					continue;
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
			} catch (Exception e) {
				logger.error("get Code:" + code + " trade data error:" + e.getLocalizedMessage());
			}
			logger.debug("Insert Trade ZJLS data end");
		}
	}
}
