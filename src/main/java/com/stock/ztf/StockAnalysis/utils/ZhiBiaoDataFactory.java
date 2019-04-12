package com.stock.ztf.StockAnalysis.utils;

import com.stock.ztf.StockAnalysis.beans.TradeBollDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeCMADataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeMACDDataInfo;
import com.stock.ztf.StockAnalysis.beans.ZhiBiaoData;
import com.stock.ztf.StockAnalysis.beans.ZhiBiaoEnum;

public class ZhiBiaoDataFactory {
	
	public static void insertZhiBiaoDataToDB(){
		
	}
	
	public static ZhiBiaoData build(String extend){
		ZhiBiaoData zhiBiaoData = null;
		switch (ZhiBiaoEnum.getZhiBiaoEnum(extend)) {
		case cma:
			zhiBiaoData = new TradeCMADataInfo();
			break;
		case macd:
			zhiBiaoData = new TradeMACDDataInfo();
			break;
		case boll:
			zhiBiaoData = new TradeBollDataInfo();
			break;
		default:
			break;
		}
		
		return zhiBiaoData;
	}

}
