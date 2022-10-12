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
public class HTTPConfigService {
    private static Logger LOGGER = LoggerFactory.getLogger(HTTPConfigService.class);

//    private volatile int waterMarkDetectServiceSleepMs = 1;
    private volatile int imageAnalysisPlatformServiceSleepMs= 1;
    private volatile int waterMarkVideoDetectTimeout = 10000;

    @QConfig(value = "http.properties", logLevel = QConfigLogLevel.high)
    public void qconfigOnChanged(Map<String, String> config) {
        try {
            Integer imageAnalysisPlatformServiceSleepMsNew = Integer.valueOf(config.get("imageAnalysisPlatformServiceSleepMs"));
            imageAnalysisPlatformServiceSleepMs = imageAnalysisPlatformServiceSleepMsNew;

            Integer waterMarkVideoDetectTimeoutNew = Integer.valueOf(config.get("waterMarkVideoDetectTimeout"));
            waterMarkVideoDetectTimeout = waterMarkVideoDetectTimeoutNew;

        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.QCONFIG_LOAD_ERROR);
            LOGGER.error("[qconfig] http.properties 配置错误.  异常. {}={}", "error", e);
        }
    }


//    public int getWaterMarkDetectServiceSleepMs() { return waterMarkDetectServiceSleepMs; }
    public int getWaterMarkDetectServiceSleepMs() { return imageAnalysisPlatformServiceSleepMs; }
}
