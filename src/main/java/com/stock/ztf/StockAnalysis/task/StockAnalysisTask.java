package com.stock.ztf.StockAnalysis.task;

import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stock.ztf.StockAnalysis.business.StockAnalysis;
import com.stock.ztf.StockAnalysis.mappers.StockDataAnalysisMapper;
import com.stock.ztf.StockAnalysis.utils.FileUtils;
import com.stock.ztf.StockAnalysis.utils.SendMail;

@Service
public class StockAnalysisTask {

	private final static Logger logger = LoggerFactory.getLogger(StockAnalysisTask.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;

	@Autowired
	private StockDataAnalysisMapper stockDataAnalysisMapper;
	
	@Autowired
	private StockAnalysis stockAnalysis;

	/**
	 * 打印分析结果
	 * @param year
	 * @param dType
	 * @return String
	 */
//	@Scheduled(initialDelay = 5 * oneSecond, fixedRate = 3000 * oneMinute)
	public String stockCMAAnalysis() {
		String retStr = "";
//		List<Map<String, Object>> codeDatas = stockDataAnalysisMapper.getStockCodeData("^0|^6");
		List<Map<String, Object>> codeDatas = stockDataAnalysisMapper.getHYStockCodeData("^0|^6");
		for (Map<String, Object> map : codeDatas) {
			String code = map.get("code").toString();
			logger.debug("analysis " + code + " trade data start");
//			stockAnalysis.macdAnalysis("2016,2017", "day", code);
			stockAnalysis.maAnalysis("2016,2017", "day", code);
			logger.debug("analysis " + code + " trade data end");
		}
		return retStr;
	}
	
	/**
	 * 打印分析结果
	 * @param year
	 * @param dType
	 * @return String
	 */
	//@Scheduled(initialDelay = 5 * oneSecond, fixedRate = 3000 * oneMinute)
	public String cal_stockShouYiByCma() {
		String retStr = "";
		List<Map<String, Object>> codeDatas = stockDataAnalysisMapper.getHYStockCodeData("^0|^6");
		for (Map<String, Object> map : codeDatas) {
			String code = map.get("code").toString();//code="002800";
			logger.debug("analysis " + code + " trade data start");
			stockAnalysis.cal_stockShouYiByCma("2016,2017", "day", code);
			logger.debug("analysis " + code + " trade data end");
		}
		return retStr;
	}
	
	/**
	 * 打印行业分析结果
	 * @param year
	 * @param dType
	 * @return String
	 */
//	@Scheduled(initialDelay = 5 * oneSecond, fixedRate = 3000 * oneMinute)
	public String HYCMAAnalysis() {
		String retStr = "";
		List<String> codeDatas = stockDataAnalysisMapper.getHYCodeData();
		for (String hyCode : codeDatas) {
			logger.debug("analysis " + hyCode + " trade data start");
//			stockAnalysis.macdAnalysis("2016,2017", "day", code);
			stockAnalysis.maAnalysis("2016,2017", "day", hyCode);
			logger.debug("analysis " + hyCode + " trade data end");
		}
		return retStr;
	}

	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 3000 * oneMinute)
//	@Scheduled(cron = "0 30 9-15 * * ?")
	public void baseAnalysisTask() {

		List<Map<String, Object>> codeDatas = stockDataAnalysisMapper.getStockCodeData("^0|^6");
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : codeDatas) {
			String code = map.get("code").toString();
			logger.debug("analysis " + code + " trade data start");

			try {
				Map<String, Object> stockAnalysisResult = stockAnalysis.macdAnalysis("2017", "day", code);
				logger.debug("analysis result:" + stockAnalysisResult);
				stockAnalysisResult.put("code", code);
				stockAnalysisResult.put("zhName", map.get("zhName").toString());
				results.add(stockAnalysisResult);
			} catch (Exception e) {
				logger.error("baseAnalysisTask error:" + e.getMessage(), e);
			}
			logger.debug("analysis " + code + " trade data end");
		}
		/**
		 * 根据区间涨跌幅排序
		 */
		Collections.sort(results, new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Boolean isZheng1 = (boolean) (o1.get("isZheng"));
				Boolean isZheng2 = (boolean) (o2.get("isZheng"));
				Integer dateCount1 = (int) (o1.get("dateCount"));
				Integer dateCount2 = (int) (o2.get("dateCount"));
				int isZhengRe = isZheng1.compareTo(isZheng2);
				if (isZhengRe == 0) {
					return dateCount1.compareTo(dateCount2);
				}
				return isZhengRe;
			}
		});
		String retStr = "";
		List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		for (Map<String, Object> map : results) {
			Map<String, String> dataMap = new HashMap<String, String>();
			List<String> dateList = (List<String>) map.get("dateList");
			if ((boolean) map.get("isZheng")) {
				retStr += "↑↑↑:";
				dataMap.put("status", "↑");

			} else {
				retStr += "↓↓↓:";
				dataMap.put("status", "↓");
			}
			retStr += "code=" + map.get("code") + ", zhName=" + map.get("zhName") + ", changeFu=" + map.get("changeFu")
					+ ", dateCount=" + dateList.size() + ", dateList=" + dateList + "<br/>";
			DecimalFormat df = new DecimalFormat("#.##");
			dataMap.put("code", "" + map.get("code"));
			dataMap.put("zhName", "" + map.get("zhName"));
			dataMap.put("changeFu", df.format((float) (map.get("changeFu"))));
			dataMap.put("dateCount", "" + dateList.size());
			dataMap.put("date", dateList.get(dateList.size() - 1));
			dataMap.put("dateList", "" + dateList);

			retList.add(dataMap);
		}
		List<String> trList = new ArrayList<String>();
		for (Map<String, String> map : retList) {
			trList.add("<tr>" + "<td style='width:6.0cm;border:solid black 1.0pt; padding:0cm 5.4pt 0cm 5.4pt'>"
					+ map.get("status")
					+ "</td><td style='width:2.0cm;border:solid black 1.0pt; border-left:none;padding:0cm 5.4pt 0cm 5.4pt'>"
					+ map.get("code") + "<br/>" +
					// "</td><td style='width:6.0cm;border:solid black 1.0pt;
					// border-left:none;padding:0cm 5.4pt 0cm 5.4pt'>" +
					map.get("zhName")
					+ "</td><td style='width:6.0cm;border:solid black 1.0pt; border-left:none;padding:0cm 5.4pt 0cm 5.4pt'>"
					+ "F:" + map.get("changeFu") + "<br/>" +
					// "</td><td style='width:6.0cm;border:solid black 1.0pt;
					// border-left:none;padding:0cm 5.4pt 0cm 5.4pt'>" +
					"C:" + map.get("dateCount") + "<br/>" +
					// "</td><td style='width:6.0cm;border:solid black 1.0pt;
					// border-left:none;padding:0cm 5.4pt 0cm 5.4pt'>" +
					"D:" + map.get("date") + "</td><tr/>");
		}
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath:mail_table.html");
			if (resources != null && resources.length > 0) {
				InputStreamReader mail_table = new InputStreamReader(resources[0].getInputStream());
				// File file =
				// ResourceUtils.getFile("classpath:mail_table.html");
				List<String> stockCodeDatas = FileUtils.readline(mail_table);
				String mailHtml = "".join("", stockCodeDatas);
				// logger.debug(retStr);
				SendMail.sendMail(mailHtml.replace("@MAIL", "".join("", trList)));
				logger.debug("sendMail OK");
			}
		} catch (Exception e) {
			logger.error("SendMail error: " + e.getMessage(), e);
		}
	}	
}
