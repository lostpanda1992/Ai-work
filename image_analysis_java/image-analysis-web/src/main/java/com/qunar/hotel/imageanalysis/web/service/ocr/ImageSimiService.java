package com.xxxxxx.hotel.imageanalysis.web.service.ocr;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.IMAGE_SIMILARITY_HTTP_RESULT_EMPTY;


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
public class ImageSimiService {

  // qconfig 配置
  private int httpConnTimeout = 5000;
  private int httpReadTimeout = 5000;

  @Resource
  HTTPConfigService httpConfigService;

  @Value("${image_simi_url}")
  private String IMAGE_SIMI_URL;


  private static Logger LOGGER = LoggerFactory.getLogger(ImageSimiService.class);
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


  public JSONObject getImageSimiPyResult(JSONObject paramObj, JSONObject result, String traceId) {

    Map<String, String> headerMap = Maps.newHashMap();
    headerMap.put("Content-Type", "application/json");

    Stopwatch httpRequestStart = Stopwatch.createStarted();
    LOGGER.info("image_simi_url :{}", IMAGE_SIMI_URL);

    String postResult = HttpUtils
        .postHttpSync(httpClient, IMAGE_SIMI_URL, paramObj, headerMap);
    long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
    LOGGER.info("请求 image_simi_url done trace_id:{}, result:{}, time:{}", traceId,
        postResult, httpEndTime);

    if (postResult != null) {
      JSONObject res = JSONObject.parseObject(postResult);
      double similarity = res.getDouble("similarity");
      result.put("status", 0);
      result.put("msg", "图像相似度服务调用成功");
      result.put("similarity", similarity);
    } else {
      QMonitor.recordOne(IMAGE_SIMILARITY_HTTP_RESULT_EMPTY);
      LOGGER.error("图片相似度 python接口返回为空 url:{}, traceId={}", IMAGE_SIMI_URL, traceId);
      result.put("msg", "图片相似度无结果");
    }

    return result;
  }

}
