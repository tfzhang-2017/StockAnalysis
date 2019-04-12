DROP DATABASE IF EXISTS `gp_datacenter`
/
CREATE DATABASE `gp_datacenter` DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

--根据cma统计股票收益数据
--dataType数据类型：日，周，月
CREATE TABLE IF NOT EXISTS `tbl_stockShouYi_data`(
   `code` VARCHAR(10) NOT NULL,
   `shouYiLv` float(8,2) DEFAULT 0,
   `isZheng` boolean DEFAULT 0,
   `maiRuJia` float(8,2) DEFAULT 0,
   `maiChuJia` float(8,2) DEFAULT 0,
   `startDate` VARCHAR(20) DEFAULT null,
   `endDate` VARCHAR(20) DEFAULT null,
   `dateCount` int(11) DEFAULT 0,
   PRIMARY KEY (`code`,`startDate`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
--创建资金分析视图
DROP VIEW IF EXISTS `view_zjAnalysis`
go
CREATE VIEW `view_zjAnalysis` AS (
select *,chg*100/rate zjrate from (
SELECT b.code,b.open,b.close,(b.close-b.open)*100/b.open chg,zj.zhuInflows*100/b.amount rate,zj.zhuInflows,b.amount 
FROM tbl_trade_base_data_2017 b
right join tbl_trade_zjls_data_2017 zj on b.code=zj.code and b.tradeDate=zj.tradeDate ) t where t.code!=''
);

--股票研报评级数据
--dataType数据类型：日，周，月
CREATE TABLE IF NOT EXISTS `tbl_stockCT_data`(
   `code` VARCHAR(10) NOT NULL,
   `yanbaoshu` int(11) DEFAULT 0,
   `mairu` int(11) DEFAULT 0,
   `zengchi` int(11) DEFAULT 0,
   `zhongxing` int(11) DEFAULT 0,
   `jianchi` int(11) DEFAULT 0,
   `maichu` int(11) DEFAULT 0,
   PRIMARY KEY (`code`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
--股票分析用数据
--dataType数据类型：日，周，月
CREATE TABLE IF NOT EXISTS `tbl_stockAnalysis_data`(
   `code` VARCHAR(10) NOT NULL,
   `isZheng` boolean DEFAULT false,
   `dateList` VARCHAR(526) DEFAULT NULL,
   `dateCount` int(11) DEFAULT 0,
   `ma5Changes` VARCHAR(526) DEFAULT NULL,
   `changeFu` float(10,3) DEFAULT NULL,
   `dataType` VARCHAR(11) NOT NULL,
   `startDate` VARCHAR(11) DEFAULT NULL,
   `endDate` VARCHAR(11) DEFAULT NULL,
   PRIMARY KEY (`code`,`dataType`,`startDate`)
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

--股票交易基本数据,BOLL数据
CREATE TABLE IF NOT EXISTS `tbl_trade_boll_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dataType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `mb` float(10,3) NOT NULL,
   `up` float(10,3) NOT NULL,
   `dn` float(10,3) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dataType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据,MACD数据
CREATE TABLE IF NOT EXISTS `tbl_trade_macd_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dataType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `diff` float(10,3) NOT NULL,
   `dea` float(10,3) NOT NULL,
   `macd` float(10,3) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dataType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票实时交易基本数据
CREATE TABLE IF NOT EXISTS `tbl_trade_real_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dataType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `DIFF` float(10,3) NOT NULL,
   `DEA` float(10,3) NOT NULL,
   `MACD` float(10,3) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dataType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据,CMA数据
CREATE TABLE IF NOT EXISTS `tbl_trade_cma_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dataType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `ma5` float(10,3) NOT NULL,
   `ma10` float(10,3) NOT NULL,
   `ma20` float(10,3) NOT NULL,
   `ma30` float(10,3) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dataType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
--股票资金历史数据
--股票代码，交易日，净流入额，净流入占比（主力：超大单：大单：中单：小单），收盘价，涨跌幅，第几周
--code,2017-05-09,1187.8688,2.74%,-162.4556,-0.37%,1350.3244,3.11%,-277.3582,-0.64%,-910.5106,-2.1%,13.73,1.70%
CREATE TABLE IF NOT EXISTS `tbl_trade_zjls_data`(
   `code` VARCHAR(10) NOT NULL,
   `tradeDate` VARCHAR(11) NOT NULL,
   `dataType` VARCHAR(11) NOT NULL,
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
   `ws` tinyint,
   PRIMARY KEY ( `code`,`tradeDate`,`dataType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据(老表，暂时不用)
--股票代码，交易日，开盘价，收盘价，最高价，最低价，成交量，成交额
--code,2017-09-22,9.04,8.99,9.05,8.92,3717111,33369518
CREATE TABLE IF NOT EXISTS `tbl_trade_base_data`(
   `code` VARCHAR(10) NOT NULL,
   `tradeDate` VARCHAR(11) NOT NULL,
   `dataType` VARCHAR(11) NOT NULL,
   `opened` float(8,2) NOT NULL,
   `closing` float(8,2) NOT NULL,
   `maximum` float(8,2) NOT NULL,
   `minimum` float(8,2) NOT NULL,
   `volume` bigint(20) NOT NULL,
   `turnVolume` bigint(20) NOT NULL,
   PRIMARY KEY ( `code`,`tradeDate`,`dataType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--股票交易基本数据
--{"time":"2010-11-18","open":"5.27","close":"4.92",
--"high":"5.39","low":"4.86","volume":"69760",
--"amount":"2.42亿","amplitude":"9.78%"}
CREATE TABLE IF NOT EXISTS `tbl_trade_base_data`(
   `code` VARCHAR(10) NOT NULL,--股票代码
   `tradeDate` VARCHAR(11) NOT NULL,--交易日期
   `dataType` VARCHAR(11) NOT NULL,--数据类型：日，周，月
   `open` float(8,2) NOT NULL,--开盘价
   `close` float(8,2) NOT NULL,--收盘价
   `high` float(8,2) NOT NULL,--最高价
   `low` float(8,2) NOT NULL,--最低价
   `volume` int(11) NOT NULL,--成交量
   `amount` float(11,1) NOT NULL,--成交额
   `amplitude` float(8,2) NOT NULL,--振幅
   PRIMARY KEY ( `code`,`tradeDate`,`dataType`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_stock_code`(
   `code` VARCHAR(10) NOT NULL,
   `zhName` VARCHAR(11) NOT NULL,
   PRIMARY KEY ( `code`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop procedure if exists proc_calZJdata
go
create procedure proc_calZJdata(in tCode varchar(10))
begin
    DECLARE  affectRows  INT  DEFAULT 0 ; 
    --计算资金流
    UPDATE tbl_trade_zjls_data SET zjflows=zhuInflows+chaoInflows+daInflows+zhongInflows+xiaoInflows where code=tCode;
    Set affectRows = affectRows + row_count();
    --计算周资金流
    INSERT INTO tbl_trade_zjls_data(SELECT code, max(tradeDate),'WK' dataType, sum(zjflows), sum(zhuInflows), 0, sum(chaoInflows), 0, sum(daInflows), 0, sum(zhongInflows), 0, sum(xiaoInflows), 0, closing, 0, ws 
        FROM tbl_trade_zjls_data where code=tCode and dataType='K' group by code,ws order by tradeDate desc)
        ON DUPLICATE KEY UPDATE zjflows=values(zjflows), zhuInflows=values(zhuInflows), zhuInflowsRatio=values(zhuInflowsRatio), chaoInflows=values(chaoInflows), chaoInflowsRatio=values(chaoInflowsRatio), daInflows=values(daInflows), daInflowsRatio=values(daInflowsRatio), zhongInflows=values(zhongInflows), zhongInflowsRatio=values(zhongInflowsRatio), xiaoInflows=values(xiaoInflows), xiaoInflowsRatio=values(xiaoInflowsRatio), closing=values(closing), chg=values(chg), ws=values(ws);
    Set affectRows = affectRows + row_count();
    --计算月资金流
    INSERT INTO tbl_trade_zjls_data(SELECT code, max(tradeDate), 'MK' dataType, sum(zjflows), sum(zhuInflows), 0, sum(chaoInflows), 0, sum(daInflows), 0, sum(zhongInflows), 0, sum(xiaoInflows), 0, closing, 0, 0 
        FROM (select code, tradeDate, left(tradeDate,7) dd, dataType, zjflows, zhuInflows, zhuInflowsRatio, chaoInflows, chaoInflowsRatio, daInflows, daInflowsRatio, zhongInflows, zhongInflowsRatio, xiaoInflows, xiaoInflowsRatio, closing, chg, ws from tbl_trade_zjls_data ) t where code=tCode and dataType='K' group by code,dd order by tradeDate desc)
        ON DUPLICATE KEY UPDATE zjflows=values(zjflows), zhuInflows=values(zhuInflows), zhuInflowsRatio=values(zhuInflowsRatio), chaoInflows=values(chaoInflows), chaoInflowsRatio=values(chaoInflowsRatio), daInflows=values(daInflows), daInflowsRatio=values(daInflowsRatio), zhongInflows=values(zhongInflows), zhongInflowsRatio=values(zhongInflowsRatio), xiaoInflows=values(xiaoInflows), xiaoInflowsRatio=values(xiaoInflowsRatio), closing=values(closing), chg=values(chg), ws=values(ws);
    Set affectRows = affectRows + row_count();
    select affectRows;
end

call proc_calZJdata('601628')





