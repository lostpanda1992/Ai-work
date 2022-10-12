package com.xxxxxx.hotel.imageanalysis.web.service.watermark;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;
import com.xxxxxx.hotel.imageanalysis.web.service.qconfig.HTTPConfigService;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qconfig.client.spring.QConfig;
import xxxxxx.tc.qconfig.client.spring.QConfigLogLevel;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;


@Service
public class WaterMarkService {

    @Resource
    HTTPConfigService httpConfigService;

    @Value("${water_mark_detect_url}")
    private String WATER_MARK_DETECT_URL;

    @Value("${water_mark_inpaint_url}")
    private String WATER_MARK_INPAINT_URL;

    @Value("${water_mark_detect_and_inpaint_url}")
    private String WATER_MARK_DETECT_AND_INPAINT_URL;

    @Value("${water_mark_video_detect_url}")
    private String WATER_MARK_VIDEO_DETECT_URL;

    private static Logger LOGGER = LoggerFactory.getLogger(WaterMarkService.class);

    private xxxxxxHttpClient httpClient;
    private xxxxxxHttpClient httpWaterMarkVideoClient;


    private volatile int imageAnalysisPlatformServiceSleepMs = 1;
    private volatile int waterMarkVideoDetectTimeout = 10000;
    private volatile int httpTimeout = 50000;

    // qconfig 配置
    private volatile int httpConnTimeout = 50000;
    private volatile int httpReadTimeout = 50000;

    @QConfig(value = "http.properties", logLevel = QConfigLogLevel.high)
    public void qconfigOnChanged(Map<String, String> config) {
        try {
            Integer imageAnalysisPlatformServiceSleepMsNew = Integer.valueOf(config.get("imageAnalysisPlatformServiceSleepMs"));
            imageAnalysisPlatformServiceSleepMs = imageAnalysisPlatformServiceSleepMsNew;

            Integer waterMarkVideoDetectTimeoutNew = Integer.valueOf(config.get("waterMarkVideoDetectTimeout"));
            waterMarkVideoDetectTimeout = waterMarkVideoDetectTimeoutNew;

            Integer httpTimeoutNew = Integer.valueOf(config.get("httpTimeout"));
            httpTimeout = httpTimeoutNew;

            Integer httpConnTimeoutNew = Integer.valueOf(config.get("httpConnTimeout"));
            httpConnTimeout = httpConnTimeoutNew;

            Integer httpReadTimeoutNew = Integer.valueOf(config.get("httpReadTimeout"));
            httpReadTimeout = httpReadTimeoutNew;

            init();

        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.QCONFIG_LOAD_ERROR);
            LOGGER.error("[qconfig] http.properties 配置错误.  异常. {}={}", "error", e);
        }
    }


    @PostConstruct
    private void init() {
        httpClient = xxxxxxHttpClient.createDefaultClient(httpConnTimeout, httpReadTimeout, 2000, 2000);
        httpWaterMarkVideoClient = xxxxxxHttpClient.createDefaultClient(waterMarkVideoDetectTimeout, waterMarkVideoDetectTimeout, 2000, 2000);
    }

    @PreDestroy
    private void shutdown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }


    public JSONObject getWaterMarkPyResult(JSONObject service, String serviceName, JSONObject paramObj, String result, String traceId, Map<String, String> hearderMap) {
        Stopwatch start = Stopwatch.createStarted();
        JSONObject resultObj = new JSONObject();
        switch (ServiceName.valueOf(serviceName)) {

            case watermark_detect_img:
                try {
                    Stopwatch httpRequestStart = Stopwatch.createStarted();
                    LOGGER.info("water_mark_detect_url param:{}", paramObj);
                    result = HttpUtils.postHttpSync(httpClient, WATER_MARK_DETECT_URL, paramObj, hearderMap);
                    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
                    LOGGER.info("请求 water_mark_detect_url trace_id:{}, time:{}", traceId, httpEndTime);
                    QMonitor.recordOne(IMAGE_WATER_MARK_DETECT_HTTP);
                } catch (Exception e) {
                    QMonitor.recordOne(IMAGE_WATER_MARK_DETECT_HTTP_ERROR);
                    LOGGER.error("请求水印检测接口异常 time:{}, url={}, params={}, error:{}",
                            start.elapsed(TimeUnit.MILLISECONDS), WATER_MARK_DETECT_URL, paramObj, e);
                }

                if (result != null) {
                    JSONArray jsonArray = JsonUtils.fromJson(result, JSONArray.class);
                    resultObj.put("service_name", serviceName);
                    resultObj.put("data", jsonArray);
                    resultObj.put("status", 0);
                    LOGGER.info("请求 water_mark_detect_url trace_id:{}, result:{}", traceId, result);
                    LOGGER.info("图片水印检测回调函数内容{}", resultObj);
                } else {
                    QMonitor.recordOne(IMAGE_WATER_MARK_DETECT_HTTP_RESULT_EMPTY);
                    LOGGER.error("请求图片水印检测python接口返回为空 url:{}, params={}", WATER_MARK_DETECT_URL, service.toJSONString());
                    resultObj.put("status", 1);
                }
                break;

            case watermark_inpaint_img:

                try {
                    Stopwatch httpRequestStart = Stopwatch.createStarted();
                    LOGGER.info("water_mark_inpaint_url param:{}", paramObj);
                    result = HttpUtils.postHttpSync(httpClient, WATER_MARK_INPAINT_URL, paramObj, hearderMap);
                    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
                    LOGGER.info("请求 water_mark_inpaint_url trace_id:{}, time:{}", traceId, httpEndTime);
                    QMonitor.recordOne(IMAGE_WATER_MARK_INPAINT_HTTP);
                } catch (Exception e) {
                    QMonitor.recordOne(IMAGE_WATER_MARK_INPAINT_HTTP_ERROR);
                    LOGGER.error("请求水印修复接口异常 time:{}, url={}, params={}, error:{}",
                            start.elapsed(TimeUnit.MILLISECONDS), WATER_MARK_INPAINT_URL, paramObj, e);
                }

                if (result != null) {
                    JSONArray jsonArray = JsonUtils.fromJson(result, JSONArray.class);
                    resultObj.put("service_name", serviceName);
                    resultObj.put("data", jsonArray);
                    resultObj.put("status", 0);
                    LOGGER.info("请求 water_mark_inpaint_url trace_id:{}, result:{}", traceId, result);
                    LOGGER.info("图片水印修复回调函数内容{}", resultObj);
                } else {
                    QMonitor.recordOne(IMAGE_WATER_MARK_INPAINT_HTTP_RESULT_EMPTY);
                    LOGGER.error("请求图片水印修复python接口返回为空 url:{}, params={}", WATER_MARK_INPAINT_URL, service.toJSONString());
                    resultObj.put("status", 1);
                }
                break;

            case watermark_detect_and_inpaint_img:

                try {
                    Stopwatch httpRequestStart = Stopwatch.createStarted();
                    LOGGER.info("water_mark_detect_and_inpaint_url param:{}", paramObj);
                    result = HttpUtils.postHttpSync(httpClient, WATER_MARK_DETECT_AND_INPAINT_URL, paramObj, hearderMap);
                    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
                    LOGGER.info("请求 water_mark_detect_and_inpaint_url trace_id:{}, time:{}", traceId, httpEndTime);
                    QMonitor.recordOne(IMAGE_WATER_MARK_DETECT_AND_INPAINT_HTTP);
                } catch (Exception e) {
                    QMonitor.recordOne(IMAGE_WATER_MARK_DETECT_AND_INPAINT_HTTP_ERROR);
                    LOGGER.error("请求水印检测并修复接口异常 time:{}, url={}, params={}, error:{}",
                            start.elapsed(TimeUnit.MILLISECONDS), WATER_MARK_DETECT_AND_INPAINT_URL, paramObj, e);
                }

                if (result != null) {
                    JSONArray jsonArray = JsonUtils.fromJson(result, JSONArray.class);
                    resultObj.put("service_name", serviceName);
                    resultObj.put("data", jsonArray);
                    resultObj.put("status", 0);
                    LOGGER.info("请求 water_mark_detect_and_inpaint_url trace_id:{}, result:{}", traceId, result);
                    LOGGER.info("图片水印检测并修复回调函数内容{}", resultObj);
                } else {
                    QMonitor.recordOne(IMAGE_WATER_MARK_DETECT_AND_INPAINT_HTTP_RESULT_EMPTY);
                    LOGGER.error("请求图片水印修复python接口返回为空 url:{}, params={}", WATER_MARK_DETECT_AND_INPAINT_URL, service.toJSONString());
                    resultObj.put("status", 1);
                }
                break;

            case watermark_video_detect:

                try {
                    Stopwatch httpRequestStart = Stopwatch.createStarted();
                    Thread.sleep(imageAnalysisPlatformServiceSleepMs);
                    LOGGER.info("water_mark_video_detect_url param:{}", paramObj);
                    result = HttpUtils.postHttpSync(httpWaterMarkVideoClient, WATER_MARK_VIDEO_DETECT_URL, paramObj, hearderMap);
                    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
                    LOGGER.info("请求 water_mark_video_detect_url done trace_id:{}, result:{}, time:{}", traceId, result, httpEndTime);
                    QMonitor.recordOne(VIDEO_WATER_MARK_DETECT_HTTP);

                } catch (Exception e) {
                    QMonitor.recordOne(VIDEO_WATER_MARK_DETECT_HTTP_ERROR);
                    LOGGER.error("请求视频水印检测接口异常 time:{}, url={}, params={}, error:{}",
                            start.elapsed(TimeUnit.MILLISECONDS), WATER_MARK_DETECT_URL, paramObj, e);
                }

                if (result != null) {
                    JSONArray jsonArray = JsonUtils.fromJson(result, JSONArray.class);
                    resultObj.put("detect_result", jsonArray);
                    resultObj.put("status", 0);
                } else {
                    QMonitor.recordOne(VIDEO_WATER_MARK_DETECT_HTTP_RESULT_EMPTY);
                    LOGGER.error("请求视频水印检测python接口返回为空 url:{}, params={}", WATER_MARK_DETECT_URL, service.toJSONString());
                    resultObj.put("status", 1);
                    resultObj.put("service_name", serviceName);
                    resultObj.put("param", paramObj);
                }
                break;

            default:
                LOGGER.info("service_name not found, service_name:{}, jsonParam:{}", serviceName, paramObj);
        }
        return resultObj;
    }

}


