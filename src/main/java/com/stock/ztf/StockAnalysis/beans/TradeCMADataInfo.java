package com.stock.ztf.StockAnalysis.beans;

public class TradeCMADataInfo {

	private String code;
	private String tradeDate;
	private String dateType;
	private float MA5;
	private float MA10;
	private float MA20;
	private float MA30;
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
	public float getMA5() {
		return MA5;
	}
	public void setMA5(float mA5) {
		MA5 = mA5;
	}
	public float getMA10() {
		return MA10;
	}
	public void setMA10(float mA10) {
		MA10 = mA10;
	}
	public float getMA20() {
		return MA20;
	}
	public void setMA20(float mA20) {
		MA20 = mA20;
	}
	public float getMA30() {
		return MA30;
	}
	public void setMA30(float mA30) {
		MA30 = mA30;
	}	
}
