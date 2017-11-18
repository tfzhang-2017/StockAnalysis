package com.stock.ztf.StockAnalysis.business;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stock.ztf.StockAnalysis.mappers.StockDataAnalysisMapper;
import com.stock.ztf.StockAnalysis.utils.FileUtils;
import com.stock.ztf.StockAnalysis.utils.SendMail;

@RestController
@RequestMapping("/analysis")
public class StockAnalysisTask {

	private final static Logger logger = LoggerFactory.getLogger(StockAnalysisTask.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;

	@Autowired
	private StockDataAnalysisMapper stockDataAnalysisMapper;

	@RequestMapping(value = "/baseAnalysis", method = RequestMethod.GET)
	public String baseAnalysis(String year, String dType) {
		String retStr = "";
		List<Map<String, Object>> codeDatas = stockDataAnalysisMapper.getStockCodeData("^0|^6");
		for (Map<String, Object> map : codeDatas) {
			String code = map.get("code").toString();
			logger.debug("analysis " + code + " trade data start");
			retStr += "analysis " + code + " trade data start<br/>";
			retStr += macdAnalysis("2016,2017", "day", code) + "<br/>";
			retStr += "analysis " + code + " trade data end<br/>";
			logger.debug("analysis " + code + " trade data end");
		}
		return retStr;
	}

	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 3000 * oneMinute)
	@Scheduled(cron = "0 30 9-15 * * ?")
	public void baseAnalysisTask() {

		List<Map<String, Object>> codeDatas = stockDataAnalysisMapper.getStockCodeData("^0|^6");
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : codeDatas) {
			String code = map.get("code").toString();
			logger.debug("analysis " + code + " trade data start");

			try {
				Map<String, Object> stockAnalysisResult = macdAnalysis("2017", "day", code);
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

	@RequestMapping(value = "/macdAnalysis", method = RequestMethod.GET)
	public Map<String, Object> macdAnalysis(String year, String dType, String code) {
		String retStr = "";
		String retStatus = "";
		List<Map<String, Object>> stockDatas = new ArrayList<Map<String, Object>>();
		String[] years = year.split(",");
		for (String y : years) {
			stockDatas.addAll(stockDataAnalysisMapper.getTradeData(y, dType, code));
		}
		/**
		 * 计算每日股价涨跌幅、MA5的涨跌幅
		 */
		for (int i = 0; i < stockDatas.size(); i++) {
			Map<String, Object> stockData = stockDatas.get(i);
			if (i != 0) {
				Map<String, Object> upStockData = stockDatas.get(i - 1);
				/**
				 * 计算股价涨跌幅
				 */
				float change = ((float) stockData.get("close") - (float) upStockData.get("close")) * 100
						/ (float) upStockData.get("close");
				/**
				 * 计算MA5的涨跌幅
				 */
				float ma5Change = ((float) stockData.get("MA5") - (float) upStockData.get("MA5")) * 100
						/ (float) upStockData.get("MA5");
				stockData.put("change", change);
				stockData.put("ma5Change", ma5Change);
			} else {
				stockData.put("change", 0.0f);
				stockData.put("ma5Change", 0.0f);
			}
			/**
			 * 计算DIFF和DEA的差值
			 */
			// float
			// ddChange=(float)stockData.get("DIFF")-(float)stockData.get("DEA");
			// stockData.put("ddChange", ddChange);
		}
		/**
		 * 根据MA5变化率统计股价涨跌幅
		 */
		List<Map<String, Object>> tongjiMA5Change = new ArrayList<Map<String, Object>>();
		Map<String, Object> changeMap = new HashMap<String, Object>();
		boolean isZheng = true;
		for (Map<String, Object> stockData : stockDatas) {
			if (changeMap.isEmpty()) {
				isZheng = ((float) stockData.get("ma5Change")) > 0;
				changeMap.put("isZheng", isZheng);
				changeMap.put("dateList", new ArrayList<String>() {
					{
						add(stockData.get("tradeDate").toString());
					}
				});
				changeMap.put("ma5Changes", new ArrayList<Float>() {
					{
						add((float) stockData.get("ma5Change"));
					}
				});
				changeMap.put("changeFu", (float) stockData.get("change"));
			} else {
				/**
				 * 同向涨跌幅相加
				 */
				if ((((float) stockData.get("ma5Change")) > 0) == isZheng) {
					((List<String>) (changeMap.get("dateList"))).add(stockData.get("tradeDate").toString());
					((List<Float>) (changeMap.get("ma5Changes"))).add((float) stockData.get("ma5Change"));
					changeMap.put("changeFu", (float) (changeMap.get("changeFu")) + (float) stockData.get("change"));
				} else {
					tongjiMA5Change.add(changeMap);
					changeMap = new HashMap<String, Object>();
					isZheng = ((float) stockData.get("ma5Change")) > 0;
					changeMap.put("isZheng", isZheng);
					changeMap.put("dateList", new ArrayList<String>() {
						{
							add(stockData.get("tradeDate").toString());
						}
					});
					changeMap.put("ma5Changes", new ArrayList<Float>() {
						{
							add((float) stockData.get("ma5Change"));
						}
					});
					changeMap.put("changeFu", (float) stockData.get("change"));
				}
			}
		}
		tongjiMA5Change.add(changeMap);
		/**
		 * 根据区间涨跌幅排序
		 */
		// Collections.sort(tongjiMA5Change, new Comparator<Map<String,
		// Object>>() {
		// public int compare(Map<String, Object> o1, Map<String, Object> o2) {
		// Float changeFu1 = Float.valueOf(o1.get("changeFu").toString()) ;
		// Float changeFu2 = Float.valueOf(o2.get("changeFu").toString()) ;
		// return changeFu1.compareTo(changeFu2);
		// }
		// });
		/**
		 * 打印统计结果
		 */
		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";
		float totalFu = 0f;
		int count = 0;
		for (Map<String, Object> map : tongjiMA5Change) {
			if ((boolean) map.get("isZheng")) {
				// System.out.println("↑↑↑:changeFu="+map.get("changeFu")+
				// ", dateList="+map.get("dateList")+
				// ", ma5Changes="+map.get("ma5Changes")+
				// ", isZheng="+map.get("isZheng"));
				List<String> dateList = (List<String>) map.get("dateList");
				retStr += "↑↑↑:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
						+ "<br/>";
				totalFu += (float) map.get("changeFu");
				count += 1;
			}
		}
		retStr += "↑↑↑:count:" + count + ", totalFu:" + totalFu + "<br/>";
		totalFu = 0f;
		count = 0;
		for (Map<String, Object> map : tongjiMA5Change) {
			if (!(boolean) map.get("isZheng")) {
				// System.out.println("↓↓↓:changeFu="+map.get("changeFu")+
				// ", dateList="+map.get("dateList")+
				// ", ma5Changes="+map.get("ma5Changes")+
				// ", isZheng="+map.get("isZheng"));
				List<String> dateList = (List<String>) map.get("dateList");
				retStr += "↓↓↓:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
						+ "<br/>";
				totalFu += (float) map.get("changeFu");
				count += 1;
			}
		}
		retStr += "↓↓↓:count:" + count + ", totalFu:" + totalFu + "<br/>";
		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";

		Map<String, Object> zuihouData = tongjiMA5Change.get(tongjiMA5Change.size() - 1);
		List<String> tdateList = (List<String>) zuihouData.get("dateList");
		zuihouData.put("dateCount", tdateList.size());
		return zuihouData;
		// System.out.println("------------------------------根据MA5变化率统计------------------------------");

		/**
		 * 根据DIFF和DEA的差值统计股价涨跌幅
		 */
		//
		// List<Map<String, Object>> tongjiDDChange=new ArrayList<Map<String,
		// Object>>();
		// changeMap=new HashMap<String, Object>();
		// isZheng=true;
		// for (Map<String, Object> stockData : stockDatas) {
		// if (changeMap.isEmpty()) {
		// isZheng=((float)stockData.get("ddChange"))>0;
		// changeMap.put("isZheng", isZheng);
		// changeMap.put("dateList", new
		// ArrayList<String>(){{add(stockData.get("tradeDate").toString());}});
		// changeMap.put("ddChanges", new
		// ArrayList<Float>(){{add((float)stockData.get("ddChange"));}});
		// changeMap.put("changeFu", (float)stockData.get("change"));
		// }else{
		// /**
		// * 同向涨跌幅相加
		// */
		// if ((((float)stockData.get("ddChange"))>0)==isZheng) {
		// ((List<String>)(changeMap.get("dateList"))).add(stockData.get("tradeDate").toString());
		// ((List<Float>)(changeMap.get("ddChanges"))).add((float)stockData.get("ddChange"));
		// changeMap.put("changeFu",
		// (float)(changeMap.get("changeFu"))+(float)stockData.get("change"));
		// } else {
		// tongjiDDChange.add(changeMap);
		// changeMap=new HashMap<String, Object>();
		// isZheng=((float)stockData.get("ddChange"))>0;
		// changeMap.put("isZheng", isZheng);
		// changeMap.put("dateList", new
		// ArrayList<String>(){{add(stockData.get("tradeDate").toString());}});
		// changeMap.put("ddChanges", new
		// ArrayList<Float>(){{add((float)stockData.get("ddChange"));}});
		// changeMap.put("changeFu", (float)stockData.get("change"));
		// }
		// }
		// }
		// tongjiDDChange.add(changeMap);
		// /**
		// * 根据区间涨跌幅排序
		// */
		//// Collections.sort(tongjiDDChange, new Comparator<Map<String,
		// Object>>() {
		//// public int compare(Map<String, Object> o1, Map<String, Object> o2)
		// {
		//// Float changeFu1 = Float.valueOf(o1.get("changeFu").toString()) ;
		//// Float changeFu2 = Float.valueOf(o2.get("changeFu").toString()) ;
		//// return changeFu1.compareTo(changeFu2);
		//// }
		//// });
		// /**
		// * 打印统计结果
		// */
		//// System.out.println("------------------------------根据DIFF和DEA差值统计------------------------------");
		// retStr+="------------------------------根据DIFF和DEA差值统计------------------------------<br/>";
		// totalFu=0f;
		// count=0;
		// for (Map<String, Object> map : tongjiDDChange) {
		// if ((boolean)map.get("isZheng")) {
		//// System.out.println("↑↑↑:changeFu="+map.get("changeFu")+
		//// ", dateList="+map.get("dateList")+
		//// ", ddChanges="+map.get("ddChanges")+
		//// ", isZheng="+map.get("isZheng"));
		// List<String> dateList=(List<String>) map.get("dateList");
		// retStr+="↑↑↑:changeFu="+map.get("changeFu")+
		// ", dateCount="+dateList.size()+
		// ", dateList="+dateList+
		// ", ddChanges="+map.get("ddChanges")+
		// ", isZheng="+map.get("isZheng")+"<br/>";
		// totalFu+=(float)map.get("changeFu");
		// count+=1;
		// }
		// }
		// retStr+="↑↑↑:count:"+count+", totalFu:"+totalFu+"<br/>";
		//// System.out.println("count:"+count+", totalFu:"+totalFu);
		// totalFu=0f;
		// count=0;
		// for (Map<String, Object> map : tongjiDDChange) {
		// if (!(boolean)map.get("isZheng")) {
		//// System.out.println("↓↓↓:changeFu="+map.get("changeFu")+
		//// ", dateList="+map.get("dateList")+
		//// ", ddChanges="+map.get("ddChanges")+
		//// ", isZheng="+map.get("isZheng"));
		// List<String> dateList=(List<String>) map.get("dateList");
		// retStr+="↓↓↓:changeFu="+map.get("changeFu")+
		// ", dateCount="+dateList.size()+
		// ", dateList="+dateList+
		// ", ddChanges="+map.get("ddChanges")+
		// ", isZheng="+map.get("isZheng")+"<br/>";
		// totalFu+=(float)map.get("changeFu");
		// count+=1;
		// }
		// }
		//// System.out.println("count:"+count+", totalFu:"+totalFu);
		// retStr+="↓↓↓:count:"+count+", totalFu:"+totalFu+"<br/>";
		// retStr+="------------------------------根据DIFF和DEA差值统计------------------------------<br/>";
		//// System.out.println("------------------------------根据DIFF和DEA差值统计------------------------------");
		// return null;
	}

	/**
	 * MA分析
	 * 
	 * @param year
	 * @param dType
	 * @param code
	 * @return Map<String, Object>
	 */
	public Map<String, Object> maAnalysis(String year, String dType, String code) {
		String retStr = "";
		String retStatus = "";
		List<Map<String, Object>> stockDatas = new ArrayList<Map<String, Object>>();
		String[] years = year.split(",");
		for (String y : years) {
			stockDatas.addAll(stockDataAnalysisMapper.getTradeData(y, dType, code));
		}
		/**
		 * 计算每日股价涨跌幅、MA5的涨跌幅
		 */
		for (int i = 0; i < stockDatas.size(); i++) {
			Map<String, Object> nowStockData = stockDatas.get(i);
			if (i != 0) {
				Map<String, Object> prevStockData = stockDatas.get(i - 1);
				/**
				 * 计算股价涨跌幅
				 */
				float change = ((float) nowStockData.get("close") - (float) prevStockData.get("close")) * 100
						/ (float) prevStockData.get("close");
				/**
				 * 计算MA5的涨跌幅
				 */
				float ma5Change = ((float) nowStockData.get("MA5") - (float) prevStockData.get("MA5")) * 100
						/ (float) prevStockData.get("MA5");
				nowStockData.put("change", change);
				nowStockData.put("ma5Change", ma5Change);
			} else {
				/*
				 * 处理上市当天数据
				 */
				nowStockData.put("change", 0.0f);
				nowStockData.put("ma5Change", 0.0f);
			}

		}
		/**
		 * 根据MA5涨跌幅统计股价涨跌幅
		 */
		List<Map<String, Object>> tongjiMA5Change = new ArrayList<Map<String, Object>>();
		Map<String, Object> changeMap = new HashMap<String, Object>();
		boolean isZheng = true;
		boolean ma5Zheng = true;
		for (Map<String, Object> stockData : stockDatas) {
			if (changeMap.isEmpty()) {
				/*
				 * 处理第一个数据
				 */
				isZheng = ((float) stockData.get("ma5Change")) > 0;
				changeMap.put("isZheng", isZheng);
				changeMap.put("dateList", new ArrayList<String>() {
					{
						add(stockData.get("tradeDate").toString());
					}
				});
				changeMap.put("ma5Changes", new ArrayList<Float>() {
					{
						add((float) stockData.get("ma5Change"));
					}
				});
				changeMap.put("changeFu", (float) stockData.get("change"));
				changeMap.put("startDate", (String) stockData.get("tradeDate"));
				changeMap.put("endDate", (String) stockData.get("tradeDate"));
			} else {

				ma5Zheng = (float) stockData.get("ma5Change") > 0;
				if (ma5Zheng == isZheng) {
					/**
					 * 同向涨跌幅相加
					 */
					((List<String>) (changeMap.get("dateList"))).add(stockData.get("tradeDate").toString());
					((List<Float>) (changeMap.get("ma5Changes"))).add((float) stockData.get("ma5Change"));
					changeMap.put("changeFu", (float) (changeMap.get("changeFu")) + (float) stockData.get("change"));
					changeMap.replace("endDate", (String) stockData.get("tradeDate"));
				} else {
					tongjiMA5Change.add(changeMap);
					changeMap = new HashMap<String, Object>();
					isZheng = ((float) stockData.get("ma5Change")) > 0;
					changeMap.put("isZheng", isZheng);
					changeMap.put("dateList", new ArrayList<String>() {
						{
							add(stockData.get("tradeDate").toString());
						}
					});
					changeMap.put("ma5Changes", new ArrayList<Float>() {
						{
							add((float) stockData.get("ma5Change"));
						}
					});
					changeMap.put("changeFu", (float) stockData.get("change"));
					changeMap.put("startDate", (String) stockData.get("tradeDate"));
					changeMap.put("endDate", (String) stockData.get("tradeDate"));
				}
			}
		}
		tongjiMA5Change.add(changeMap);
		/*
		 * 分析结果记入数据库
		 */
		stockDataAnalysisMapper.insertOrUpdateAnalysisData(tongjiMA5Change);
		
		/**
		 * 根据区间涨跌幅排序
		 */
		// Collections.sort(tongjiMA5Change, new Comparator<Map<String,
		// Object>>() {
		// public int compare(Map<String, Object> o1, Map<String, Object> o2) {
		// Float changeFu1 = Float.valueOf(o1.get("changeFu").toString()) ;
		// Float changeFu2 = Float.valueOf(o2.get("changeFu").toString()) ;
		// return changeFu1.compareTo(changeFu2);
		// }
		// });
		/**
		 * 打印统计结果
		 */
		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";
		for (Map<String, Object> map : tongjiMA5Change) {
			if (!(boolean) map.get("isZheng")) {
				System.out.println("↓↓↓:changeFu=" + map.get("changeFu") + ", startDate=" + map.get("startDate")
						+ ", endDate=" + map.get("endDate") + ", ma5Changes=" + map.get("ma5Changes"));
			}
		}
		for (Map<String, Object> map : tongjiMA5Change) {

			if ((boolean) map.get("isZheng")) {
				System.out.println("↑↑↑:changeFu=" + map.get("changeFu") + ", startDate=" + map.get("startDate")
						+ ", endDate=" + map.get("endDate") + ", ma5Changes=" + map.get("ma5Changes"));
			}
		}
		float totalFu = 0f;
		int count = 0;
		for (Map<String, Object> map : tongjiMA5Change) {
			/*
			 * 打印上涨数据
			 */
			if ((boolean) map.get("isZheng")) {
				List<String> dateList = (List<String>) map.get("dateList");
				retStr += "↑↑↑:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
						+ "<br/>";
				totalFu += (float) map.get("changeFu");
				count += 1;
			}
		}
		retStr += "↑↑↑:count:" + count + ", totalFu:" + totalFu + "<br/>";
		totalFu = 0f;
		count = 0;
		for (Map<String, Object> map : tongjiMA5Change) {
			/*
			 * 打印下跌数据
			 */
			if (!(boolean) map.get("isZheng")) {
				List<String> dateList = (List<String>) map.get("dateList");
				retStr += "↓↓↓:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
						+ "<br/>";
				totalFu += (float) map.get("changeFu");
				count += 1;
			}
		}
		retStr += "↓↓↓:count:" + count + ", totalFu:" + totalFu + "<br/>";
		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";

		Map<String, Object> zuihouData = tongjiMA5Change.get(tongjiMA5Change.size() - 1);
		List<String> tdateList = (List<String>) zuihouData.get("dateList");
		zuihouData.put("dateCount", tdateList.size());
		return zuihouData;
		// System.out.println("------------------------------根据MA5变化率统计------------------------------");
	}
}
