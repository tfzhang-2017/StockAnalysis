package com.stock.ztf.StockAnalysis.beans;

public class TradeMACDDataInfo extends ZhiBiaoData {
	
	private float diff;
	private float dea;
	private float macd;
	
	public float getDiff() {
		return diff;
	}
	public void setDiff(float diff) {
		this.diff = diff;
	}
	public float getDea() {
		return dea;
	}
	public void setDea(float dea) {
		this.dea = dea;
	}
	public float getMacd() {
		return macd;
	}
	public void setMacd(float macd) {
		this.macd = macd;
	}
	
	@Override
	public ZhiBiaoData build(String code, String tradeDate, String dataType, String dataStr) {
		TradeMACDDataInfo tradeMACDDataInfo = new TradeMACDDataInfo();
		String[] macds = dataStr.replaceAll("[^\\d\\.\\-,]", "").split(",");
		tradeMACDDataInfo.setCode(code);
		tradeMACDDataInfo.setTradeDate(tradeDate);
		tradeMACDDataInfo.setDataType(dataType);
		tradeMACDDataInfo.setDiff( Float.parseFloat(macds[0]));
		tradeMACDDataInfo.setDea( Float.parseFloat(macds[1]));
		tradeMACDDataInfo.setMacd( Float.parseFloat(macds[2]));
		return tradeMACDDataInfo;
	}
	
	
}
