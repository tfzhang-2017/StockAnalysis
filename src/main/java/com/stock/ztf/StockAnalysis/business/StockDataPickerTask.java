package com.stock.ztf.StockAnalysis.business;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stock.ztf.StockAnalysis.beans.TradeCMADataInfo;
import com.stock.ztf.StockAnalysis.mappers.StockBaseDataMapper;

/**
 * 采集股票基础数据
 * 
 * @author ztf
 *
 */
@Component
public class StockDataPickerTask {

	private final static Logger logger = LoggerFactory.getLogger(StockDataPickerTask.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;

	@Autowired
	private StockBaseDataMapper stockBaseDataMapper;

	@Autowired
	private StockDataPicker stockDataPicker;


	/**
	 * 获取股票交易历史基本数据
	 */
//	 @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	// @Scheduled(cron = "0 0 9-15 * * ?")
	public void timeTaskPickerTradeBaseData1() {
		List<Map<String, Object>> codeDatas = stockBaseDataMapper.getHYStockCodeData("^0|^6");
		for (Map<String, Object> codes : codeDatas) {
			String code = (String) codes.get("code");
			String market = (String) codes.get("market");
			// logger.debug("start get " + code + " day Trade real data ");
			// pickerStockTradeRealData(code, market);
			logger.debug("start get " + code + " day Trade Base data ");
			stockDataPicker.pickerStockTradeBaseData(code, market, "day", "K");
			// logger.debug("start get "+code+" weekday Trade Base data ");
			// pickerStockTradeBaseData(code,market,"weekday","wk");
			// logger.debug("start get "+code+" month Trade Base data ");
			// pickerStockTradeBaseData(code,market,"month","mk");
			logger.debug("start get " + code + " day Trade cam data ");
			stockDataPicker.pickerStockTradeZhiBiaoData(code, market, "K", "cma", "day", TradeCMADataInfo.class);
			// logger.debug("start get " + code + " day Trade macd data ");
			// pickerStockTradeZhiBiaoData(code, market, "K", "macd", "day",
			// TradeMACDDataInfo.class);
		}
	}

	/**
	 * 获取股票交易历史基本数据
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	// @Scheduled(cron = "0 0 9-15 * * ?")
	public void timeTaskPickerTradeBaseData() {
		// List<String> codeDatas =
		// stockBaseDataMapper.getStockCodeData("^0|^6");
		// for (String code : codeDatas) {
		// String market = "1";
		// if (code.startsWith("00")) {
		// market = "2";
		// }
		//// logger.debug("start get " + code + " day Trade real data ");
		//// pickerStockTradeRealData(code, market);
		// logger.debug("start get " + code + " day Trade Base data ");
		// pickerStockTradeBaseData(code, market, "day", "K");
		// // logger.debug("start get "+code+" weekday Trade Base data ");
		// // pickerStockTradeBaseData(code,market,"weekday","wk");
		// // logger.debug("start get "+code+" month Trade Base data ");
		// // pickerStockTradeBaseData(code,market,"month","mk");
		// logger.debug("start get " + code + " day Trade cam data ");
		// pickerStockTradeZhiBiaoData(code, market, "K", "cma", "day",
		// TradeCMADataInfo.class);
		//// logger.debug("start get " + code + " day Trade macd data ");
		//// pickerStockTradeZhiBiaoData(code, market, "K", "macd", "day",
		// TradeMACDDataInfo.class);
		// }
		// List<String> hyCodeDatas = stockBaseDataMapper.getHYCode();
		// for (String hyCode : hyCodeDatas) {
		//// logger.debug("start get " + hyCode + " day Trade Base data ");
		//// pickerStockTradeBaseData(hyCode, "1", "day", "K");
		//// logger.debug("start get " + hyCode + " day Trade cam data ");
		//// pickerStockTradeZhiBiaoData(hyCode, "1", "K", "cma", "day",
		// TradeCMADataInfo.class);
		//// logger.debug("start get " + hyCode + " day Trade macd data ");
		//// pickerStockTradeZhiBiaoData(hyCode, "1", "K", "macd", "day",
		// TradeMACDDataInfo.class);
		// }
	}


	/**
	 * 获取行业股票列表数据
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	public void pickerStockTradeHYStocksData() {
		List<String> hyCodes = stockBaseDataMapper.getHYCode();
		for (String hyCode : hyCodes) {
			stockDataPicker.pickerStockTradeHYStockData(hyCode);
		}
	}


	/**
	 * 从文件读取股票代码数据，插入数据库
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 6000 * oneMinute)
	public void pickerStockCodeData() {
		logger.debug("Insert Stock Code data start");
		stockDataPicker.pickerStockCodeData();		
		logger.debug("Insert Stock Code data end");
	}	

}
