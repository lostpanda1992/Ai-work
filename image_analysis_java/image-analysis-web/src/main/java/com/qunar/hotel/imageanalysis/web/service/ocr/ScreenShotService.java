package com.xxxxxx.hotel.imageanalysis.web.service.ocr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.service.qconfig.HTTPConfigService;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_SCREEN_SHOT_HTTP_RESULT_EMPTY;

@Service
public class ScreenShotService {
    // qconfig 配置
    private int httpConnTimeout = 50000;
    private int httpReadTimeout = 50000;

    @Resource
    HTTPConfigService httpConfigService;

    @Value("${ocr_screen_shot_url}")
    private String OCR_SCREEN_SHOT_URL;

    private static Logger LOGGER = LoggerFactory.getLogger(ScreenShotService.class);
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


    public JSONObject getScreenShotPyResult(JSONObject paramObj, JSONObject result, String traceId) {

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");

        Stopwatch httpRequestStart = Stopwatch.createStarted();
        LOGGER.info("crawl_screen_shot_url :{}", OCR_SCREEN_SHOT_URL);

//        OCR_SCREEN_SHOT_URL = "http://l-deeplearning2.h.cn2:9017/algo/ocr/crawlScreenShot";
        String postResult = HttpUtils
                .postHttpSync(httpClient, OCR_SCREEN_SHOT_URL, paramObj, headerMap);
        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("请求 crawl_screen_shot_url done trace_id:{}, result:{}, time:{}", traceId,
                postResult, httpEndTime);

        if (postResult != null) {
            JSONObject res = JSONObject.parseObject(postResult);
            JSONArray ocrRes = res.getJSONArray("result");
            String type = res.getString("img_type");
            int status = res.getInteger("status");
            result.put("status", 0);
            result.put("msg", "screen shot ocr调用成功");
            result.put("res", ocrRes);
            result.put("type", type);

        } else {
            QMonitor.recordOne(OCR_SCREEN_SHOT_HTTP_RESULT_EMPTY);
            LOGGER.error("screen shot ocr python接口返回为空 url:{}, traceId={}", OCR_SCREEN_SHOT_URL, traceId);
            result.put("msg", "screen shot ocr无结果");
        }

        return result;
    }

    public JSONObject getJavaSreenOcrResultParam(JSONObject pyParam,String taskId,String source){
        JSONObject rtnParam = new JSONObject(0);

        rtnParam.put("service_name","screen_shot_ocr");
        rtnParam.put("task_id",taskId);
        rtnParam.put("source",source);

        JSONArray data = new JSONArray();
        int status = pyParam.getInteger("status");
        if(status != 0){
            rtnParam.put("status",1);
            return rtnParam;
        }
        rtnParam.put("status",0);
        String pyResultStr = pyParam.getJSONArray("res").toJSONString();
        pyResultStr = pyResultStr.replaceAll("ocr_result","ocr_infos");
        data = JSON.parseArray(pyResultStr);
        rtnParam.put("data",data);

        return rtnParam;
    }
}
