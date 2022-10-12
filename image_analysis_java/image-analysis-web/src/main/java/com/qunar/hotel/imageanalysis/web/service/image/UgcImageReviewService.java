package com.xxxxxx.hotel.imageanalysis.web.service.image;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UGC_HTTP_RESULT_EMPTY;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.service.qconfig.HTTPConfigService;
import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
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
public class UgcImageReviewService {

  // qconfig 配置
  private int httpConnTimeout = 600000; // 10min
  private int httpReadTimeout = 600000; // 10min

  @Resource
  HTTPConfigService httpConfigService;

  @Resource
  QmqSendService qmqSendService;
  @Value("${ugc_img_review_url}")
  private String UGC_IMG_REVIEW_URL;


  private static Logger LOGGER = LoggerFactory.getLogger(UgcImageReviewService.class);
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


  public JSONObject getUgcReviewResPyResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ugc_img_review_url :{}", UGC_IMG_REVIEW_URL);

    String postResult = HttpUtils
        .postHttpSync(httpClient, UGC_IMG_REVIEW_URL, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ugc_img_review_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      result.put("status", 0);
      result.put("msg", "ugc图像审核服务调用成功");
      result.put("data", res);

    } else {
      QMonitor.recordOne(UGC_HTTP_RESULT_EMPTY);
      LOGGER.error("ugc图像审核 python接口返回为空 url:{}, traceId={}", UGC_IMG_REVIEW_URL, traceId);
      result.put("msg", "ugc图像审核无结果");
    }

    return result;
  }


  public void ugcCallbackByUrl(String url, JSONObject paramObj, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ugc_callback_url :{}", url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ugc_callback_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

  }


}
