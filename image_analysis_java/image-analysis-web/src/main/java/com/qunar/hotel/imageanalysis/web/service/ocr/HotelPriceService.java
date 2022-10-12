package com.xxxxxx.hotel.imageanalysis.web.service.ocr;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_HOTEL_PRICE_HTTP_RESULT_EMPTY;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_HOTEL_PAGE_HTTP_RESULT_EMPTY;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class HotelPriceService {

  // qconfig 配置
  private int httpConnTimeout = 5000;
  private int httpReadTimeout = 5000;

  @Resource
  HTTPConfigService httpConfigService;

  @Value("${ocr_hotel_price_url}")
  private String OCR_HOTEL_PRICE_URL;

  @Value("${ocr_hotel_page_url}")
  private String OCR_HOTEL_PAGE_URL;

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


  public JSONObject getHotelPricePyResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("hotel_price_ocr_url :{}", OCR_HOTEL_PRICE_URL);

    String postResult = HttpUtils
        .postHttpSync(httpClient, OCR_HOTEL_PRICE_URL, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 hotel_price_ocr_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      String ocrRes = res.getString("ocr_result");
      String type = res.getString("img_type");
      int status = res.getInteger("status");
      result.put("status", 0);
      result.put("msg", "酒店比价ocr调用成功");
      result.put("res", ocrRes);
      result.put("type", type);

    } else {
      QMonitor.recordOne(OCR_HOTEL_PRICE_HTTP_RESULT_EMPTY);
      LOGGER.error("酒店比价ocr python接口返回为空 url:{}, traceId={}", OCR_HOTEL_PRICE_URL, traceId);
      result.put("msg", "酒店比价ocr无结果");
    }

    return result;
  }


  public JSONObject getHotelPagePyResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ocr_hotel_page_url :{}", OCR_HOTEL_PAGE_URL);

    String postResult = HttpUtils
        .postHttpSync(httpClient, OCR_HOTEL_PAGE_URL, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 hotel_page_ocr_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      String type = res.getString("img_type");
      result.put("status", 0);
      result.put("msg", "酒店页面识别服务调用成功");
      result.put("type", type);
    } else {
      QMonitor.recordOne(OCR_HOTEL_PAGE_HTTP_RESULT_EMPTY);
      LOGGER.error("酒店页面识别 python接口返回为空 url:{}, traceId={}", OCR_HOTEL_PAGE_URL, traceId);
      result.put("msg", "酒店页面识别无结果");
    }

    return result;
  }

}
