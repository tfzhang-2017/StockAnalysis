package com.stock.ztf.StockAnalysis.beans;

public class TradeMRHYDataInfo {

	private String code;
	private String zhName;
	private float chg;
	private long value;
	private float rate;
	private int up;
	private int down;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getZhName() {
		return zhName;
	}
	public void setZhName(String zhName) {
		this.zhName = zhName;
	}
	public float getChg() {
		return chg;
	}
	public void setChg(float chg) {
		this.chg = chg;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public float getRate() {
		return rate;
	}
	public void setRate(float rate) {
		this.rate = rate;
	}
	public int getUp() {
		return up;
	}
	public void setUp(int up) {
		this.up = up;
	}
	public int getDown() {
		return down;
	}
	public void setDown(int down) {
		this.down = down;
	}	
}
