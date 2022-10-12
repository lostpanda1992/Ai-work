package com.xxxxxx.hotel.imageanalysis.web.service.qmq;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.internal.$Gson$Preconditions;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.hotel.imageanalysis.web.service.watermark.WaterMarkService;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import xxxxxx.tc.qmq.Message;
import xxxxxx.tc.qmq.consumer.annotation.QmqConsumer;
import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;
import com.xxxxxx.hotel.imageanalysis.web.utils.ImageParamsBuildUtils;
import com.xxxxxx.hotel.imageanalysis.web.service.hotScore.HotScoreService;
import com.xxxxxx.hotel.imageanalysis.web.service.qualityScore.QualityScoreService;

/**
 * QMQ消费服务
 *
 */
@Slf4j
@Service
public class QmqConsumerService {

    private static Logger LOGGER = LoggerFactory.getLogger(QmqConsumerService.class);

    public static final String GROUP = "image_analysis_platform_subject_group";

    private xxxxxxHttpClient httpClient;

    private volatile int httpTimeout = 50000;

    @Resource
    WaterMarkService waterMarkService;

    @Resource
    HotScoreService hotScoreService;

    @Resource
    QualityScoreService qualityScoreService;

    @PostConstruct
    private void init() {
        httpClient = xxxxxxHttpClient.createDefaultClient(httpTimeout, httpTimeout, 2000, 2000);
    }

    @PreDestroy
    private void shutdown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }



    @QmqConsumer(prefix = IMAGE_ANALYSIS_PLATFORM_SUBJECT, consumerGroup = GROUP)
    public void imageAnalysisPlatformConsumer(final Message message) {
        Stopwatch start = Stopwatch.createStarted();
        LOGGER.info("消费QMQ imageAnalysisPlatformConsumer, message:{}", message);

        String result = null;
        JSONObject resultObject = new JSONObject();

        JSONObject paramObj = new JSONObject();
        JSONArray params = new JSONArray();
        paramObj.put("app_code", message.getStringProperty("appcode"));

        String traceId = message.getStringProperty("traceId");
        paramObj.put("trace_id", traceId);

        String serviceJson = message.getStringProperty("serviceJson");
        JSONArray serviceArray = JsonUtils.fromJson(serviceJson, JSONArray.class);


        for(int i = 0; i < serviceArray.size(); i++) {
            JSONObject service = serviceArray.getJSONObject(i);
            String serviceName = service.getString("service_name");
            String callbackUrl = service.getString("callback_url");

            Map<String, String> headerMap = Maps.newHashMap();
            headerMap.put("Content-Type", "application/json");

            if ("image_hot_scores".equals(serviceName) || "quality".equals(serviceName) || "aesthetic".equals(serviceName)){
                String imgType= message.getStringProperty("imgType");

                JSONObject paramPyObj = ImageParamsBuildUtils.getImageParams(serviceArray, imgType, paramObj, params);
                if ("image_hot_scores".equals(serviceName)) {
                    resultObject = hotScoreService.getImageHotScorePyResult(paramPyObj, resultObject, traceId);
                } else {
                    resultObject = qualityScoreService.getImageQualityScorePyResult(paramPyObj, resultObject, traceId);
                }
                String callbackResultImgDetect = HttpUtils.postHttpSync(httpClient, callbackUrl, resultObject, headerMap);
                LOGGER.info("图片评分 url done serviceName:{}, url:{}, result:{}", serviceName, callbackUrl, callbackResultImgDetect);
            } else {
                paramObj.put("params", service.getJSONArray("params"));
                JSONObject resultObj = waterMarkService.getWaterMarkPyResult(service, serviceName, paramObj, result, traceId, headerMap);

                switch (ServiceName.valueOf(serviceName)) {

                    case watermark_detect_img:

                        String callbackResultImgDetect = HttpUtils.postHttpSync(httpClient, callbackUrl, resultObj, headerMap);
                        LOGGER.info("图片水印检测回调 url done url:{}, result:{}", callbackUrl, callbackResultImgDetect);

                        break;
                    case watermark_inpaint_img:

                        String callbackResultImgInpaint = HttpUtils.postHttpSync(httpClient, callbackUrl, resultObj, headerMap);
                        LOGGER.info("图片水印修复回调 url done url:{}, result:{}", callbackUrl, callbackResultImgInpaint);

                        break;
                    case watermark_detect_and_inpaint_img:

                        String callbackResultImgDetectInpaint = HttpUtils.postHttpSync(httpClient, callbackUrl, resultObj, headerMap);
                        LOGGER.info("图片水印检测并修复回调 url done url:{}, result:{}", callbackUrl, callbackResultImgDetectInpaint);

                        break;
                    case watermark_video_detect:

                        String callbackResult = HttpUtils.postHttpSync(httpClient, callbackUrl, resultObj, headerMap);
                        LOGGER.info("回调url done url:{}, result:{}", callbackUrl, callbackResult);

                        break;
                    default:
                        LOGGER.info("service_name not found, service_name:{}, jsonParam:{}", serviceName, paramObj);
                }
            }
        }
    }
}