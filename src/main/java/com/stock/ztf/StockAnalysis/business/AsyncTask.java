package com.stock.ztf.StockAnalysis.business;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.scheduling.annotation.Async;  
import org.springframework.stereotype.Component;  
  
@Component    
public class AsyncTask {  
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());    
        
    @Async  //myTaskAsynPool即配置线程池的方法名，此处如果不写自定义线程池的方法名，会使用默认的线程池  
    public void doTask1(String i){    
        logger.info("Task"+i+" started.");    
    }    
}   
