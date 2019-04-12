package com.stock.ztf.StockAnalysis.beans;

public abstract class ZhiBiaoData {

	private String code;
	private String tradeDate;
	private String dataType;
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getTradeDate() {
		return tradeDate;
	}
	
	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	

	public abstract ZhiBiaoData build(String code,String tradeDate,String dataType,String dataStr);
	
}
