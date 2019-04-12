package com.stock.ztf.StockAnalysis.beans;

public class TradeCMADataInfo extends ZhiBiaoData {

	private float MA5;
	private float MA10;
	private float MA20;
	private float MA30;
	
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
	
	@Override
	public ZhiBiaoData build(String code, String tradeDate, String dataType, String dataStr) {
		TradeCMADataInfo tradeDataInfo = new TradeCMADataInfo();
		String[] cmas = dataStr.replaceAll("[^\\d\\.\\-,]", "").replaceAll("-", "0").split(",");
		tradeDataInfo.setCode(code);
		tradeDataInfo.setTradeDate(tradeDate);
		tradeDataInfo.setDataType(dataType);
		tradeDataInfo.setMA5( Float.parseFloat(cmas[0]));
		tradeDataInfo.setMA10( Float.parseFloat(cmas[1]));
		tradeDataInfo.setMA20( Float.parseFloat(cmas[2]));
		tradeDataInfo.setMA30( Float.parseFloat(cmas[3]));
		return tradeDataInfo;
	}	
		
}
