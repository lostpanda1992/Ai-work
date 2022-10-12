package com.xxxxxx.hotel.imageanalysis.web.service.hotScore;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
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

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;


@Service
public class HotScoreService {
    // qconfig 配置
    private int httpConnTimeout = 50000;
    private int httpReadTimeout = 50000;

    @Resource
    HTTPConfigService httpConfigService;

    @Value("${image_hot_score_url}")
    private String IMAGE_HOT_SCORE_URL;

    private static Logger LOGGER = LoggerFactory.getLogger(HotScoreService.class);
    private xxxxxxHttpClient httpClient;


    @PostConstruct
    private void init() {
        httpClient = xxxxxxHttpClient
                .createDefaultClient(httpConnTimeout, httpReadTimeout, 2000, 2000);
    }

    @PreDestroy
    private void shutdown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }


    public JSONObject getImageHotScorePyResult(JSONObject paramObj, JSONObject result, String traceId) {

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");

        Stopwatch httpRequestStart = Stopwatch.createStarted();
        LOGGER.info("image_hot_score_url :{}", IMAGE_HOT_SCORE_URL);

        String imageHotScorePyResult = HttpUtils.postHttpSync(httpClient, IMAGE_HOT_SCORE_URL, paramObj, headerMap);
        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("请求 image_hot_score_url done trace_id:{}, result:{}, time:{}", traceId, imageHotScorePyResult, httpEndTime);

        if (imageHotScorePyResult != null) {
            JSONArray jsonHotScorePyResult = JsonUtils.fromJson(imageHotScorePyResult, JSONArray.class);
            for(int i =0; i< jsonHotScorePyResult.size();i++){
                JSONObject jsonSingleHotScorePyResult = jsonHotScorePyResult.getJSONObject(i);
                String calculateStatus=jsonSingleHotScorePyResult.getString("calculate_status");
                String serviceName=jsonSingleHotScorePyResult.getString("service_name");
                String imageId=jsonSingleHotScorePyResult.getString("image_id");
                String hotScore=jsonSingleHotScorePyResult.getString("hot_score");

                result.put("status", 0);
                result.put("calculateStatus", calculateStatus);
                result.put("serviceName", serviceName);
                result.put("imageId", imageId);
                result.put("hotScore", hotScore);
            }
        } else {
            QMonitor.recordOne(IMAGE_HOT_SCORE_HTTP_RESULT_EMPTY);
            LOGGER.error("image hot score python接口返回为空 url:{}, traceId={}", IMAGE_HOT_SCORE_URL, traceId);
            result.put("msg", "image hot score 无结果");
        }

        return result;
    }

}