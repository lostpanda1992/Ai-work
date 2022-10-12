package com.xxxxxx.hotel.imageanalysis.web.service.ocr;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_HTTP_RESULT_EMPTY;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_BLOCK_HTTP_RESULT_EMPTY;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_SLIDE_HTTP_RESULT_EMPTY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
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
public class UiTestService {

  // qconfig 配置
  private int httpConnTimeout = 250000;
  private int httpReadTimeout = 250000;

  @Resource
  HTTPConfigService httpConfigService;

  @Value("${ui_test_url}")
  private String ui_test_url;

  @Value("${ui_test_detail_url}")
  private String ui_test_detail_url;

  @Value("${ui_test_image_url}")
  private String ui_test_image_url;

  @Value("${ui_test_popup_url}")
  private String ui_test_popup_url;

  @Value("${ui_test_loading_url}")
  private String ui_test_loading_url;

  @Value("${ui_test_block_url}")
  private String ui_test_block_url;

  @Value("${ui_test_slide_url}")
  private String ui_test_slide_url;

  private static Logger LOGGER = LoggerFactory.getLogger(UiTestService.class);
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


  public JSONObject getUiTestPyResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ui_test_url :{}", ui_test_url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, ui_test_url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ui_test_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      JSONArray boxRes = res.getJSONArray("find_box");
      int status = res.getInteger("status");

      if (status == 0) {
        result.put("msg", "ui图片测试服务调用成功");
        result.put("res", boxRes);
        QMonitor.recordOne(WatcherConstant.UI_TEST_SUCCEED_NUM);
      } else {
        result.put("msg", "ui图片测试服务调用失败");
      }

    } else {
      QMonitor.recordOne(UI_TEST_HTTP_RESULT_EMPTY);
      LOGGER.error("uiTest接口返回为空 url:{}, traceId={}", ui_test_url, traceId);
      result.put("msg", "ui图片测试无结果");
    }

    return result;
  }



  public JSONObject getUiTestPyDetailResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ui_test_detail_url :{}", ui_test_detail_url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, ui_test_detail_url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ui_test_detail_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      JSONArray bosRes = res.getJSONArray("ocr_result");
      int status = res.getInteger("status");

      if (status == 0) {
        result.put("msg", "ui图片测试服务调用成功");
        result.put("res", bosRes);
        QMonitor.recordOne(WatcherConstant.UI_TEST_DETAIL_SUCCEED_NUM);
      } else {
        result.put("msg", "ui图片测试服务调用失败");
      }

    } else {
      QMonitor.recordOne(UI_TEST_HTTP_RESULT_EMPTY);
      LOGGER.error("uiTestDetail接口返回为空 url:{}, traceId={}", ui_test_detail_url, traceId);
      result.put("msg", "ui图片测试无结果");
    }

    return result;
  }


  public JSONObject getUiTestPyImageResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ui_test_image_url :{}", ui_test_image_url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, ui_test_image_url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ui_test_image_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      JSONArray bosRes = res.getJSONArray("match_result");
      int status = res.getInteger("status");

      if (status == 0) {
        result.put("msg", "ui图片测试服务调用成功");
        result.put("res", bosRes);
        QMonitor.recordOne(WatcherConstant.UI_TEST_IMAGE_SUCCEED_NUM);
      } else {
        result.put("msg", "ui图片测试服务调用失败");
      }

    } else {
      QMonitor.recordOne(UI_TEST_HTTP_RESULT_EMPTY);
      LOGGER.error("uiTestImage接口返回为空 url:{}, traceId={}", ui_test_image_url, traceId);
      result.put("msg", "ui图片测试无结果");
    }

    return result;
  }


  public JSONObject getUiTestPyPopupResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ui_test_popup_url :{}", ui_test_popup_url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, ui_test_popup_url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ui_test_popup_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      JSONArray bosRes = res.getJSONArray("close_box");
      int is_popup = res.getInteger("is_popup");

      JSONObject popRes = new JSONObject();
      popRes.put("is_popup", is_popup);
      popRes.put("close_box", bosRes);

      result.put("msg", "ui图片测试服务调用成功");
      result.put("res", popRes);


    } else {
      QMonitor.recordOne(UI_TEST_HTTP_RESULT_EMPTY);
      LOGGER.error("uiTestPopup接口返回为空 url:{}, traceId={}", ui_test_popup_url, traceId);
      result.put("msg", "ui图片测试无结果");
    }

    return result;
  }


  public JSONObject getUiTestPyLoadingResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ui_test_loading_url :{}", ui_test_loading_url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, ui_test_loading_url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ui_test_loading_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);

      int isLoading = res.getInteger("is_loading");

      JSONObject popRes = new JSONObject();
      popRes.put("is_loading", isLoading);


      result.put("msg", "ui图片测试服务调用成功");
      result.put("res", popRes);


    } else {
      QMonitor.recordOne(UI_TEST_HTTP_RESULT_EMPTY);
      LOGGER.error("uiTestLoading接口返回为空 url:{}, traceId={}", ui_test_loading_url, traceId);
      result.put("msg", "ui图片测试无结果");
    }

    return result;
  }

  public JSONObject getUiTestPyBlockResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ui_test_block_url :{}", ui_test_block_url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, ui_test_block_url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ui_test_block_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      JSONArray boxRes = res.getJSONArray("find_box");
      int status = res.getInteger("status");

      if (status == 0) {
        result.put("msg", "ui图片测试服务调用成功");
        result.put("res", boxRes);
        QMonitor.recordOne(WatcherConstant.UI_TEST_BLOCK_SUCCEED_NUM);
      } else {
        result.put("msg", "ui图片测试服务调用失败");
      }

    } else {
      QMonitor.recordOne(UI_TEST_BLOCK_HTTP_RESULT_EMPTY);
      LOGGER.error("uiTestBlock接口返回为空 url:{}, traceId={}", ui_test_block_url, traceId);
      result.put("msg", "ui图片测试无结果");
    }

    return result;
  }

  public JSONObject getUiTestPySlideResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("ui_test_slide_url :{}", ui_test_slide_url);

    String postResult = HttpUtils
        .postHttpSync(httpClient, ui_test_slide_url, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 ui_test_slide_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      JSONArray boxRes = res.getJSONArray("find_box");
      result.put("msg", "ui图片测试服务调用成功");
      result.put("res", boxRes);
      QMonitor.recordOne(WatcherConstant.UI_TEST_SLIDE_SUCCEED_NUM);

    } else {
      QMonitor.recordOne(UI_TEST_SLIDE_HTTP_RESULT_EMPTY);
      LOGGER.error("uiTestSlide接口返回为空 url:{}, traceId={}", ui_test_slide_url, traceId);
      result.put("msg", "ui图片测试无结果");
    }

    return result;
  }
}
