package com.stock.ztf.StockAnalysis.beans;

import java.util.List;
import java.util.Map;

public class UrlDataInfo {

	private String name;
	private String code;
	private Map<String, String> info;
	private List<String> data;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Map<String, String> getInfo() {
		return info;
	}
	public void setInfo(Map<String, String> info) {
		this.info = info;
	}
	public List<String> getData() {
		return data;
	}
	public void setData(List<String> data) {
		this.data = data;
	}	
}
