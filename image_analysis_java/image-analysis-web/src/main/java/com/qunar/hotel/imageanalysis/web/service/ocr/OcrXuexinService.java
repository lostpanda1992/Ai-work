package com.xxxxxx.hotel.imageanalysis.web.service.ocr;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_XUEXIN_HTTP_RESULT_EMPTY;

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
public class OcrXuexinService {

  // qconfig 配置
  private int httpConnTimeout = 15000;
  private int httpReadTimeout = 15000;

  @Resource
  HTTPConfigService httpConfigService;

  @Resource
  QmqSendService qmqSendService;
  @Value("${ocr_xuexin_url}")
  private String OCR_XUEXIN_URL;


  private static Logger LOGGER = LoggerFactory.getLogger(OcrXuexinService.class);
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


  public JSONObject getOcrXuexinPyResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ocr_xuexin_url :{}", OCR_XUEXIN_URL);

    String postResult = HttpUtils
        .postHttpSync(httpClient, OCR_XUEXIN_URL, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ocr_xuexin_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      String taskId = res.getString("taskId");
      String source = res.getString("source");
      JSONArray ocrInfos = res.getJSONArray("ocrInfos");
      result.put("status", 0);
      result.put("msg", "学信网ocr服务调用成功");
      result.put("ocrInfos", ocrInfos);

    } else {
      QMonitor.recordOne(OCR_XUEXIN_HTTP_RESULT_EMPTY);
      LOGGER.error("学信网ocr python接口返回为空 url:{}, traceId={}", OCR_XUEXIN_URL, traceId);
      result.put("msg", "学信网ocr无结果");
    }

    return result;
  }


  public void xuexinCallbackByUrl(String url, JSONObject paramObj, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ocr_xuexin_callback_url :{}", url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ocr_xuexin_callback_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

  }


}
