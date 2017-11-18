
--股票分析用数据
--dateType数据类型：日，周，月
CREATE TABLE IF NOT EXISTS `tbl_stockAnalysis_data`(
   `code` VARCHAR(10) NOT NULL,
   `isZheng` boolean DEFAULT false,
   `dateList` VARCHAR(526) DEFAULT NULL,
   `ma5Changes` VARCHAR(526) DEFAULT NULL,
   `changeFu` float(10,3) DEFAULT NULL,
   `dateType` VARCHAR(11) NOT NULL,
   `startDate` VARCHAR(11) DEFAULT NULL,
   `endDate` VARCHAR(11) DEFAULT NULL,
   PRIMARY KEY (`code`,`dateType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


--行业股票列表数据
CREATE TABLE IF NOT EXISTS `tbl_proxy_data`(
   `ip` VARCHAR(20) NOT NULL,
   `port` int(6) NOT NULL,
   `type` VARCHAR(11) DEFAULT NULL,
   `speed` VARCHAR(10) DEFAULT NULL,
   `timeout` VARCHAR(10) DEFAULT NULL,
   `live` VARCHAR(10) DEFAULT NULL,
   PRIMARY KEY (`ip`,`port`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--行业股票列表数据
CREATE TABLE IF NOT EXISTS `tbl_trade_HYStock_data`(
   `hyCode` VARCHAR(10) NOT NULL,
   `code` VARCHAR(10) NOT NULL,
   `zhName` VARCHAR(11) NOT NULL,
   `market` VARCHAR(2) NOT NULL,
   PRIMARY KEY (`hyCode`,`code`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--每日行业数据
CREATE TABLE IF NOT EXISTS `tbl_trade_MRHY_data`(
   `code` VARCHAR(10) NOT NULL,
   `zhName` VARCHAR(11) NOT NULL,
   `chg` float(10,3) NOT NULL,
   `value` bigint NOT NULL,
   `rate` float(10,3) NOT NULL,
   `up` int(11) NOT NULL,
   `down` int(11) NOT NULL,
   PRIMARY KEY ( `code`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据,MACD数据
CREATE TABLE IF NOT EXISTS `tbl_trade_macd_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dateType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `DIFF` float(10,3) NOT NULL,
   `DEA` float(10,3) NOT NULL,
   `MACD` float(10,3) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dateType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票实时交易基本数据
CREATE TABLE IF NOT EXISTS `tbl_trade_real_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dateType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `DIFF` float(10,3) NOT NULL,
   `DEA` float(10,3) NOT NULL,
   `MACD` float(10,3) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dateType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据,CMA数据
CREATE TABLE IF NOT EXISTS `tbl_trade_cma_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dateType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `MA5` float(10,3) NOT NULL,
   `MA10` float(10,3) NOT NULL,
   `MA20` float(10,3) NOT NULL,
   `MA30` float(10,3) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dateType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
--股票资金历史数据
--股票代码，交易日，净流入额，净流入占比（主力：超大单：大单：中单：小单），收盘价，涨跌幅
--code,2017-05-09,1187.8688,2.74%,-162.4556,-0.37%,1350.3244,3.11%,-277.3582,-0.64%,-910.5106,-2.1%,13.73,1.70%
CREATE TABLE IF NOT EXISTS `tbl_trade_zjls_data`(
   `code` VARCHAR(10) NOT NULL,
   `tradeDate` VARCHAR(11) NOT NULL,
   `dateType` VARCHAR(11) NOT NULL,
   `zhuInflows` float(8,2) NOT NULL,
   `zhuInflowsRatio` float(8,2) NOT NULL,
   `chaoInflows` float(8,2) NOT NULL,
   `chaoInflowsRatio` float(8,2) NOT NULL,
   `daInflows` float(8,2) NOT NULL,
   `daInflowsRatio` float(8,2) NOT NULL,
   `zhongInflows` float(8,2) NOT NULL,
   `zhongInflowsRatio` float(8,2) NOT NULL,
   `xiaoInflows` float(8,2) NOT NULL,
   `xiaoInflowsRatio` float(8,2) NOT NULL,
   `closing` float(8,2) NOT NULL,
   `chg` float(8,2) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dateType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据(老表，暂时不用)
--股票代码，交易日，开盘价，收盘价，最高价，最低价，成交量，成交额
--code,2017-09-22,9.04,8.99,9.05,8.92,3717111,33369518
CREATE TABLE IF NOT EXISTS `tbl_trade_base_data`(
   `code` VARCHAR(10) NOT NULL,
   `tradeDate` VARCHAR(11) NOT NULL,
   `dateType` VARCHAR(11) NOT NULL,
   `opened` float(8,2) NOT NULL,
   `closing` float(8,2) NOT NULL,
   `maximum` float(8,2) NOT NULL,
   `minimum` float(8,2) NOT NULL,
   `volume` bigint(20) NOT NULL,
   `turnVolume` bigint(20) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dateType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据
--{"time":"2010-11-18","open":"5.27","close":"4.92",
--"high":"5.39","low":"4.86","volume":"69760",
--"amount":"2.42亿","amplitude":"9.78%"}
CREATE TABLE IF NOT EXISTS `tbl_trade_base_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dateType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `open` float(8,2) NOT NULL,--开盘价
   `close` float(8,2) NOT NULL,--收盘价
   `high` float(8,2) NOT NULL,--最高价
   `low` float(8,2) NOT NULL,--最低价
   `volume` int(11) NOT NULL,--成交量
   `amount` float(11,1) NOT NULL,--成交额
   `amplitude` float(8,2) NOT NULL,--振幅
   PRIMARY KEY ( `code`,`tradeDate`,`dateType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_stock_code`(
   `code` VARCHAR(10) NOT NULL,
   `zhName` VARCHAR(11) NOT NULL,
   PRIMARY KEY ( `code`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;