package com.stock.ztf.StockAnalysis.beans;

public enum ZhiBiaoEnum {
	cma,macd;

    public static ZhiBiaoEnum getZhiBiaoEnum(String zhiBiao){  
       return valueOf(zhiBiao.toLowerCase());  
    }
}
