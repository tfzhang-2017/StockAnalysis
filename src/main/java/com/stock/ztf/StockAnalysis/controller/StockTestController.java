package com.stock.ztf.StockAnalysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stock.ztf.StockAnalysis.business.StockDataPicker;

@RestController
@RequestMapping(value = "/test")
public class StockTestController {

	@Autowired
	private StockDataPicker stockDataPicker;

    @RequestMapping(value="/pickerStockBaseData")
    public String pickerStockBaseData(String c, String mkt){
    	stockDataPicker.pickerStockBaseData(c, mkt);
        return "OK";
    }
}
