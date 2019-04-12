package com.stock.ztf.StockAnalysis.beans;

public class TradeBollDataInfo extends ZhiBiaoData {
	
	private float mb;
	private float up;
	private float dn;
	
	public float getMb() {
		return mb;
	}

	public void setMb(float mb) {
		this.mb = mb;
	}

	public float getUp() {
		return up;
	}

	public void setUp(float up) {
		this.up = up;
	}

	public float getDn() {
		return dn;
	}

	public void setDn(float dn) {
		this.dn = dn;
	}

	@Override
	public ZhiBiaoData build(String code, String tradeDate, String dataType, String dataStr) {
		TradeBollDataInfo tradeBollDataInfo = new TradeBollDataInfo();
		String[] bolls = dataStr.replaceAll("[^\\d\\.\\-,]", "").replaceAll("-", "0").split(",");
		tradeBollDataInfo.setCode(code);
		tradeBollDataInfo.setTradeDate(tradeDate);
		tradeBollDataInfo.setDataType(dataType);
		tradeBollDataInfo.setMb( Float.parseFloat(bolls[0]));
		tradeBollDataInfo.setUp( Float.parseFloat(bolls[1]));
		tradeBollDataInfo.setDn( Float.parseFloat(bolls[2]));
		return tradeBollDataInfo;
	}
	
	
}
