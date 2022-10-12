package com.xxxxxx.hotel.imageanalysis.web.service.wordJoint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import jdk.nashorn.internal.scripts.JO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;

@Service
public class WordJointService {

    @Value("${first_image_word_joint_url}")
    private String FIRST_IMAGE_WORD_JOINT_URL;

    private static Logger LOGGER = LoggerFactory.getLogger(WordJointService.class);
    private xxxxxxHttpClient httpClient;
    private volatile int httpTimeout = 50000;

    @PostConstruct
    private void init() {
        httpClient = xxxxxxHttpClient
                .createDefaultClient(httpTimeout, httpTimeout, 2000, 2000);
    }

    @PreDestroy
    private void shutdown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    public JSONObject getFirstImageWordJointPyResult(JSONObject paramObj, JSONObject result, String traceId) {

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");

        Stopwatch httpRequestStart = Stopwatch.createStarted();
        LOGGER.info("image_word_joint_url :{}", FIRST_IMAGE_WORD_JOINT_URL);

        LOGGER.info("image_word_joint_service 传入python端参数 :{}", paramObj);

        String firstImageWordJointPyResult = HttpUtils.postHttpSync(httpClient, FIRST_IMAGE_WORD_JOINT_URL, paramObj, headerMap);
        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("请求 image_word_joint_url done trace_id:{}, result:{}, time:{}", traceId, firstImageWordJointPyResult , httpEndTime);

        if (firstImageWordJointPyResult  != null) {
            JSONArray jsonImageWordJointPyResult = JsonUtils.fromJson(firstImageWordJointPyResult, JSONArray.class);
            result.put("python_data",jsonImageWordJointPyResult);
        } else {
            QMonitor.recordOne(IMAGE_WORD_JOINT_HTTP_RESULT_EMPTY);
            LOGGER.error("image word joint python接口返回为空 url:{}, traceId={}", FIRST_IMAGE_WORD_JOINT_URL, traceId);
            result.put("msg", "image word joint 无结果");
        }

        return result;
    }
}
