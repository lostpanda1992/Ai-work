package com.xxxxxx.hotel.imageanalysis.web.service.image;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.WHITE_DETECT_HTTP_RESULT_EMPTY;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.HotelPriceService;
import com.xxxxxx.hotel.imageanalysis.web.service.qconfig.HTTPConfigService;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhiteDetectService {
    // qconfig 配置
    private int httpConnTimeout = 50000;
    private int httpReadTimeout = 50000;

    @Resource
    HTTPConfigService httpConfigService;

    @Value("${white_image_detect_url}")
    private String WHITE_IMAGE_DETECT_URL;

    private static Logger LOGGER = LoggerFactory.getLogger(HotelPriceService.class);
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


    public JSONObject getWhiteDetectPyResult(JSONObject paramObj, JSONObject result, String traceId) {

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");

        Stopwatch httpRequestStart = Stopwatch.createStarted();
        LOGGER.info("white_image_detect_url :{},httpReadTime:{}", WHITE_IMAGE_DETECT_URL,httpReadTimeout);

        String postResult = HttpUtils
                .postHttpSync(httpClient, WHITE_IMAGE_DETECT_URL, paramObj, headerMap);
        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("请求 white_image_detect_url done trace_id:{}, result:{}, time:{}", traceId,
                postResult, httpEndTime);

        if (postResult != null) {
            JSONObject res = JSONObject.parseObject(postResult);
            int status = res.getInteger("status");
            if(status ==0) {
                res.put("msg", "白屏检测调用成功");
                result = res;
//                Boolean is_white = res.getBoolean("is_white");
//                result.put("status", 0);
//                result.put("msg", "白屏检测调用成功");
//                result.put("is_white", is_white);
            }else{
//                result.put("status", 1);
                res.put("msg", "白屏检测失败！");
                result = res;
            }

        } else {
            QMonitor.recordOne(WHITE_DETECT_HTTP_RESULT_EMPTY);
            LOGGER.error("白屏检测 python接口返回为空 url:{}, traceId={}", WHITE_IMAGE_DETECT_URL, traceId);
            result.put("msg", "白屏检测无结果");
            result.put("status", 1);
            String img_id = paramObj.getString("img_id");
            if( img_id != null && !img_id.isEmpty()){
                result.put("img_id", img_id);
            }

        }

        return result;
    }

}
