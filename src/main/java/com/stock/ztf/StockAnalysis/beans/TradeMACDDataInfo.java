package com.stock.ztf.StockAnalysis.beans;

public class TradeMACDDataInfo {

	private String code;
	private String tradeDate;
	private String dateType;
	private float DIFF;
	private float DEA;
	private float MACD;
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
	public String getDateType() {
		return dateType;
	}
	public void setDateType(String dateType) {
		this.dateType = dateType;
	}
	public float getDIFF() {
		return DIFF;
	}
	public void setDIFF(float dIFF) {
		DIFF = dIFF;
	}
	public float getDEA() {
		return DEA;
	}
	public void setDEA(float dEA) {
		DEA = dEA;
	}
	public float getMACD() {
		return MACD;
	}
	public void setMACD(float mACD) {
		MACD = mACD;
	}
}
