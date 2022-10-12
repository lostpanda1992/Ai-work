package com.xxxxxx.hotel.imageanalysis.web.service.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Pack:       com.xxxxxx.hotel.feedstream.web.service
 * File:       LogService
 * Desc:       日志服务类
 * User:       zhangyuzyy.zhang
 */
@Service
public class LogService {
    private static Logger LOGGER = LoggerFactory.getLogger(LogService.class);

//    @Resource
//    SwitchConfigService switchConfigService;

//    /**
//     * 输出召回参数
//     *
//     * @param log
//     * @param format
//     * @param reqParam
//     */
//    public void printRecallParam(Logger log, String format, ReqParam reqParam) {
//        if (switchConfigService.getPrintRecallRankLogSwitch()) {
//            log.info(format, reqParam);
//        }
//    }
//
//    /**
//     * 打印使用时间日志
//     *
//     * @param log
//     * @param format
//     * @param reqParam
//     * @param userTime
//     */
//    public void printUseTime(Logger log, String format, ReqParam reqParam, long userTime, Object... param) {
//        if (switchConfigService.getPrintUseTimeLogSwitch()) {
//            log.info(format, reqParam, userTime, param);
//        }
//    }
//
//
//    /**
//     * 输出召回结果
//     *
//     * @param log
//     * @param format
//     * @param reqParam
//     * @param recallResult
//     * @param userTime
//     */
//    public void printRecallResult(Logger log, String format, ReqParam reqParam, Map<RecallMode, List<ContentItem>> recallResult, long userTime) {
//        if (switchConfigService.getPrintRecallRankLogSwitch()) {
//            log.info(format, reqParam, JsonUtils.toJson(recallResult), userTime);
//        }
//    }
//
//    /**
//     * 输出rank参数
//     *
//     * @param log
//     * @param format
//     * @param reqParam
//     */
//    public void printRankParam(Logger log, String format, ReqParam reqParam) {
//        if (switchConfigService.getPrintRecallRankLogSwitch()) {
//            log.info(format, reqParam);
//        }
//    }
//
//    /**
//     * 输出rank结果
//     *
//     * @param log
//     * @param format
//     * @param reqParam
//     * @param rankItems
//     * @param userTime
//     */
//    public void printRankResult(Logger log, String format, ReqParam reqParam, List<ContentItem> rankItems, long userTime) {
//        if (switchConfigService.getPrintRecallRankLogSwitch()) {
//            log.info(format, reqParam, JsonUtils.toJson(rankItems), userTime);
//        }
//    }

}
