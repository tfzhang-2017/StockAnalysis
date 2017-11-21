package com.stock.ztf.StockAnalysis.mappers;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface StockDataAnalysisMapper {

	List<Map<String, Object>> getTradeCMAData(@Param("year") String year,@Param("dtype") String dtype,@Param("code") String code);

	List<Map<String, Object>> getStockCodeData(@Param("condition") String condition);

	int getStockTradeTblCount(@Param("tblName") String tblName);
	
	List<Map<String, Object>> getHYStockCodeData(@Param("condition") String condition);
	
	List<String> getHYCodeData();
	
	int insertOrUpdateAnalysisData(Map<String, Object> datas);
	
	int insertOrUpdateAnalysisDatas(@Param("datas") List<Map<String, Object>> datas);

}
