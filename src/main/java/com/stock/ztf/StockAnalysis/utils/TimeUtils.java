package com.stock.ztf.StockAnalysis.utils;

import java.util.Date;

public class TimeUtils {

	/**
	 * 获取时间戳
	 * @return
	 */
	public static int getTimeFloor(){
		return (int)Math.floor((new Date().getTime()) / 30000);
	}
	
	public static long getTime(){
		return new Date().getTime();
	}
	
	public static void main(String[] args) {
		System.out.println("([\"2,002502,骅威文化,9.03,0.12,1.35,53101,47586667,1.80,8.91,8.89,9.05,8.89,1.03,0.86,25.45\"])".replaceAll("[\\]\\[)(\"]", "").trim());
	}
}
