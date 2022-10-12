package com.xxxxxx.hotel.imageanalysis.web.service.qmq;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_XUEXIN_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.XUEXIN_OCR_SUBJECT;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.OcrXuexinService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qmq.Message;
import xxxxxx.tc.qmq.consumer.annotation.QmqConsumer;


/**
 * QMQ消费服务
 */
@Slf4j
@Service
public class OcrXuexinQmqConsumerService {

  private static Logger LOGGER = LoggerFactory.getLogger(QmqSendService.class);

  public static final String XUEXIN_GROUP = "xuexin_ocr_subject_group";

  private xxxxxxHttpClient httpClient;

  private volatile int httpTimeout = 50000;

  @Resource
  OcrXuexinService ocrXuexinService;

  @Resource
  XuexinOcrQmqSendService xuexinOcrQmqSendService;

  @PostConstruct
  private void init() {
    httpClient = xxxxxxHttpClient.createDefaultClient(httpTimeout, httpTimeout, 2000, 2000);
  }

  @PreDestroy
  private void shutdown() {
    if (httpClient != null) {
      httpClient.close();
    }
  }


  @QmqConsumer(prefix = XUEXIN_OCR_SUBJECT, consumerGroup = XUEXIN_GROUP)
  public void xuexinOcrConsumer(final Message message) {
    Stopwatch start = Stopwatch.createStarted();
    LOGGER.info("消费QMQ OcrXuexinQmqConsumer, message:{}", message);

    JSONObject pyResult = new JSONObject();
    JSONObject serviceJsonObject = JSONObject
        .parseObject(message.getStringProperty("xuexin_qmq_key"));
    String appcode = serviceJsonObject.getString("app_code");
    JSONObject dataObj = serviceJsonObject.getJSONObject("data");

    String taskId = dataObj.getString("task_id");
    String source = dataObj.getString("source");
    JSONObject callback = dataObj.getJSONObject("callback");
    String type = callback.getString("type");
    String value = callback.getString("value");

    JSONObject paramObj = new JSONObject();
    paramObj.put("app_code", appcode);
    paramObj.put("data", dataObj);

    // 调用python服务
    try {
      pyResult = ocrXuexinService.getOcrXuexinPyResult(paramObj, pyResult, taskId);
    } catch (Exception e) {
      QMonitor.recordOne(OCR_XUEXIN_HTTP_ERROR);
      LOGGER.error("学信网ocr接口异常, params={}, error:{}", paramObj, e);
    }

    JSONArray ocrInfos = pyResult.getJSONArray("ocrInfos");

    // python结果回调
    JSONObject resObj = new JSONObject();
    resObj.put("taskId", taskId);
    resObj.put("source", source);
    resObj.put("ocrInfos", ocrInfos);

    // 发送qmq
    if ("qmq".equals(type)) {
      xuexinOcrQmqSendService.sendXuexinQmqMsg(taskId, appcode, value, ocrInfos.toJSONString());
    }
    // 回调callback url
    if ("url".equals(type)) {
      ocrXuexinService.xuexinCallbackByUrl(value, resObj, taskId);
    }
  }



  @QmqConsumer(prefix = "mp_xuexin_ocr_image", consumerGroup = XUEXIN_GROUP)
  public void xuexinConsumer1(final Message message) {

    LOGGER.info("【】【】【】【】【】" + message.getStringProperty("ocrInfos"));

  }


  @QmqConsumer(prefix = "mp_xuexin_ocr_image_old", consumerGroup = XUEXIN_GROUP)
  public void xuexinConsumer2(final Message message) {

    LOGGER.info("【】【】【】【】【】" + message.getStringProperty("ocrInfos"));

  }

}