package com.xxxxxx.hotel.imageanalysis.web.service.screenShotOcr;

//public class ScreenShotConsumer {
//}

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.image.WhiteDetectService;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.BillOcrService;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.ScreenShotService;
import com.xxxxxx.hotel.imageanalysis.web.service.qconfig.HTTPConfigService;
import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qconfig.client.spring.QConfig;
import xxxxxx.tc.qconfig.client.spring.QConfigLogLevel;
import xxxxxx.tc.qmq.Message;
import xxxxxx.tc.qmq.consumer.annotation.QmqConsumer;
import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;


/**
 * QMQ消费服务
 *
 */
@Slf4j
@Service
public class ScreenShotConsumer {

    private static Logger LOGGER = LoggerFactory.getLogger(ScreenShotConsumer.class);

    public static final String GROUP = "ocr_screen_shot_subject_group";

    private xxxxxxHttpClient httpClient;

    @Resource
    ScreenShotService screenShotService;


    private volatile int imageAnalysisPlatformServiceSleepMs= 1000;
//    private volatile int ocrScreenShotServiceSleepMs = 10000;
    private volatile int httpTimeout = 50000;

    @QConfig(value = "http.properties", logLevel = QConfigLogLevel.high)
    public void qconfigOnChanged(Map<String, String> config) {
        try {
//            ocrScreenShotServiceSleepMs = Integer.valueOf(config.get("imageAnalysisPlatformServiceSleepMs"));
//            imageAnalysisPlatformServiceSleepMs = imageAnalysisPlatformServiceSleepMsNew;

//            httpTimeout = Integer.valueOf(config.get("httpTimeout"));
//            httpTimeout = httpTimeoutNew;

            init();

        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.QCONFIG_LOAD_ERROR);
            LOGGER.error("[qconfig] http.properties 配置错误.  异常. {}={}", "error", e);
        }
    }



    @Value("${ocr_screen_shot_url}")
    private String OCR_SCREEN_SHOT_URL;



    @PostConstruct
    private void init() {
        httpClient = xxxxxxHttpClient.createDefaultClient(httpTimeout, httpTimeout, 2000, 2000);
//        httpWaterMarkVideoClient = xxxxxxHttpClient.createDefaultClient(waterMarkVideoDetectTimeout, waterMarkVideoDetectTimeout, 2000, 2000);
    }

    @PreDestroy
    private void shutdown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }



    @QmqConsumer(prefix = OCR_SCREEN_SHOT_SUBJECT, consumerGroup = GROUP)
    public void screenShotOcrConsumer(final Message message) {
        Stopwatch start = Stopwatch.createStarted();
//        LOGGER.info("消费QMQ imageWaterMarkDetectConsumer, message:{}", message);
        LOGGER.info("消费QMQ ocrScreenShotConsumer, message:{}", message);

//        String result = null;


        JSONObject paramObj = new JSONObject();
        paramObj.put("app_code", message.getStringProperty("appcode"));

        String traceId = message.getStringProperty("traceId");
        String source = message.getStringProperty("source");
        String taskId = message.getStringProperty("taskId");
        paramObj.put("trace_id", traceId);
        LOGGER.info("trace_id:{}", traceId);

        String serviceJson = message.getStringProperty("serviceJson");
        LOGGER.info("serviceJson:{}", serviceJson);

        JSONArray serviceArray = JsonUtils.fromJson(serviceJson, JSONArray.class);

        JSONObject input_Json = serviceArray.getJSONObject(0);
        String service_name =  input_Json.getString("service_name");
        JSONArray img_info_array = input_Json.getJSONArray("params");
        String callback_url = input_Json.getString("callback_url");

        paramObj.put("img_info",img_info_array);
//        if (WHITE_IMAGE_DETECT.equals(service_name)) {

            QMonitor.recordOne(WatcherConstant.WHITE_DETECT_NUM);

            // 调用python服务
        JSONObject result = new JSONObject();
//        paramObj = new JSONObject();
//        paramObj.put("trace_id", traceId);
//        paramObj.put("app_code", message.getStringProperty("appcode"));
//        paramObj.put("service_name", "screen_shot _ocr");
//        paramObj.put("img_type", "img_base64");
//        paramObj.put("img_url", "http://qimgs.xxxxxxzz.com/h_crawl_scheduler_plugin_market_01/ocr_47080_07150250158193.jpg");
        LOGGER.info("paramObj:{}",paramObj.toJSONString());
        try {
            result = screenShotService.getScreenShotPyResult(paramObj, result, traceId);

        } catch (Exception e) {
            QMonitor.recordOne(OCR_SCREEN_SHOT_HTTP_ERROR);
            QMonitor.recordOne(WatcherConstant.SCREEN_SHOT_OCR_ERROR_NUM);
            LOGGER.error("screen shotocr接口异常, url={}, params={}, error:{}",
                    OCR_SCREEN_SHOT_URL, paramObj, e);
//            result.put("msg", "WHITE_DETECT_HTTP_ERROR！");
            result.put("status", 1);
        }
//        }
        JSONObject resultObj = result;
//        resultObj = getJavaSreenOcrResultParam(result, taskId, source);

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");
        String callbackResult = "";
        try {
            resultObj = screenShotService.getJavaSreenOcrResultParam(result, taskId, source);
            callbackResult = HttpUtils.postHttpSync(httpClient, callback_url, resultObj, headerMap);
        }catch(Exception e){
            LOGGER.info("call back fail.e:{}",e);

        }
        LOGGER.info("回调url done url:{}, result:{}", callback_url, callbackResult);


    }




}