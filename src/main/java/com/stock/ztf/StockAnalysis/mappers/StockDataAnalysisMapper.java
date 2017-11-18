package com.stock.ztf.StockAnalysis.mappers;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface StockDataAnalysisMapper {

	List<Map<String, Object>> getTradeData(@Param("year") String year,@Param("dtype") String dtype,@Param("code") String code);

	List<Map<String, Object>> getStockCodeData(@Param("condition") String condition);

	int getStockTradeTblCount(@Param("tblName") String tblName);
	
	List<String> getHYCode();
	
	int insertOrUpdateAnalysisData(@Param("datas") List<Map<String, Object>> datas);

}
