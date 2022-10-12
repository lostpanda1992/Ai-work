package com.xxxxxx.hotel.imageanalysis.web.service.qconfig;

import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qconfig.client.spring.QConfig;
import xxxxxx.tc.qconfig.client.spring.QConfigLogLevel;

import java.util.Map;


@Service
public class SwitchConfigService {
    private static Logger LOGGER = LoggerFactory.getLogger(SwitchConfigService.class);

    // 是否打印日志
    private volatile boolean printRecallRankLog = false;

    // 是否打印使用时间日志
    private volatile boolean printUseTimeLog = false;

    private volatile boolean searchRedisSwitch = false;
    // 只能搜索帖子的状态是0(已审帖子)开关
    private volatile boolean searchItemStatusOnly0 = false;


    @QConfig(value = "switch.properties", logLevel = QConfigLogLevel.high)
    public void qconfigOnChanged(Map<String, String> config) {
        try {
            Boolean printRecallRankLogNew = Boolean.valueOf(config.get("printRecallRankLog"));
            printRecallRankLog = printRecallRankLogNew;
            Boolean printUseTimeLogNew = Boolean.valueOf(config.get("printUseTimeLog"));
            printUseTimeLog = printUseTimeLogNew;
            Boolean searchRedisSwitchNew = Boolean.valueOf(config.get("searchRedisSwitch"));
            searchRedisSwitch = searchRedisSwitchNew;
            Boolean searchItemStatusOnly0New = Boolean.valueOf(config.get("searchItemStatusOnly0"));
            searchItemStatusOnly0 = searchItemStatusOnly0New;

        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.QCONFIG_LOAD_ERROR);
            LOGGER.error("[qconfig] switch.properties 配置错误.  异常. {}={}", "error", e);
        }
    }


    public boolean getPrintRecallRankLogSwitch() {
        return printRecallRankLog;
    }

    public boolean getPrintUseTimeLogSwitch() {
        return printUseTimeLog;
    }

    public boolean getSearchRedisSwitch() {
        return searchRedisSwitch;
    }

    public boolean getSearchItemStatusOnly0Switch() {
        return searchItemStatusOnly0;
    }


}
