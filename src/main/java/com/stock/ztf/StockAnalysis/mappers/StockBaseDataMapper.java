package com.stock.ztf.StockAnalysis.mappers;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.stock.ztf.StockAnalysis.beans.TradeBaseDataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeCMADataInfo;
import com.stock.ztf.StockAnalysis.beans.TradeZJLSDataInfo;

public interface StockBaseDataMapper {

	List<Map<String, Object>> getDayTradeBaseData();

	List<String> getStockCodeData(@Param("condition") String condition);
	
	List<Map<String, Object>> getHYStockCodeData(@Param("condition") String condition);

	int getStockTradeTblCount(@Param("tblName") String tblName);

	int insertOrUpdateTradeBaseData(@Param("tblName") String tblName, @Param("datas") List<TradeBaseDataInfo> datas);

	int insertOrUpdateStockCode(@Param("code") String code, @Param("zhName") String zhName);

	int createStockTradeTbl(@Param("tblName") String tblName, @Param("year") String year);

	int insertOrUpdateTradeZJLSData(@Param("tblName") String tblName, @Param("datas") List<TradeZJLSDataInfo> datas);
	
	int insertOrUpdateTradeCMAData(@Param("tblName") String tblName, @Param("datas") List<?> datas);
	
	int insertOrUpdateTradeMACDData(@Param("tblName") String tblName, @Param("datas") List<?> datas);
	
	int insertOrUpdateTradeMRHYData(@Param("datas") List<?> datas);
	
	int insertOrUpdateTradeHYStockData(@Param("datas") List<?> datas);
	
	List<String> getHYCode();
	
	int insertOrUpdateStockCTData(@Param("datas") List<?> datas);

}
