package com.xxxxxx.hotel.imageanalysis.web.service.ocr;

//public class BillOcrService {
//}


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
public class BillOcrService {
    // qconfig 配置
    private int httpConnTimeout = 50000;
    private int httpReadTimeout = 50000;

    @Resource
    HTTPConfigService httpConfigService;

    @Value("${bill_ocr_url}")
    private String BILL_OCR_URL;

    private static Logger LOGGER = LoggerFactory.getLogger(BillOcrService.class);
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


    public JSONObject getBillOcrPyResult(JSONObject paramObj, JSONObject result, String traceId) {

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");

        Stopwatch httpRequestStart = Stopwatch.createStarted();

        String postResult = HttpUtils
                .postHttpSync(httpClient, BILL_OCR_URL, paramObj, headerMap);
        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("[bill_ocr][trace_id={}]请求 bill_ocr_url done ,bill_ocr_url :{}, result:{}, time:{}", traceId,BILL_OCR_URL,
                postResult, httpEndTime);

        if (postResult != null) {
            JSONObject res = JSONObject.parseObject(postResult);
            JSONArray ocrRes = res.getJSONArray("result");
            String type = res.getString("img_type");
            int status = res.getInteger("status");
            result.put("status", 0);
            result.put("msg", "bill ocr调用成功");
            result.put("res", ocrRes);
            result.put("type", type);

        } else {
            QMonitor.recordOne(OCR_SCREEN_SHOT_HTTP_RESULT_EMPTY);
            LOGGER.error("[bill_ocr][trace_id={}]bill ocr python接口返回为空 url:{},postResult:{}", traceId,BILL_OCR_URL,postResult);
            result.put("msg", "bill ocr无结果");
        }

        return result;
    }
}
