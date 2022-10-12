package com.xxxxxx.hotel.imageanalysis.web.service.qmq;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UGC_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UGC_SUBJECT;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;
import com.xxxxxx.hotel.imageanalysis.web.service.image.UgcImageReviewService;
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
public class UgcImageReviewQmqConsumerService {

  private static Logger LOGGER = LoggerFactory.getLogger(QmqSendService.class);
  private static Logger REQUEST_LOGGER = LoggerFactory.getLogger("request_result");

  public static final String UGC_GROUP = "ugc_subject_group";

  private xxxxxxHttpClient httpClient;

  private volatile int httpTimeout = 600000; // 10min

  @Resource
  UgcImageReviewService ugcImageReviewService;

  @Resource
  UgcImageReviewQmqSendService ugcImageReviewQmqSendService;



  @QmqConsumer(prefix = UGC_SUBJECT, consumerGroup = UGC_GROUP)
  public void xuexinOcrConsumer(final Message message) {
    Stopwatch start = Stopwatch.createStarted();
    LOGGER.info("消费QMQ UgcImageReviewQmqConsumer, message:{}", message);

    JSONObject pyResult = new JSONObject();
    String paramStr = message.getStringProperty("ugc_qmq_key");
    JSONObject serviceJsonObject = JSONObject
        .parseObject(paramStr);

    JSONObject dataRes = new JSONObject();
    dataRes.put("comm_id", serviceJsonObject.getString("comm_id"));
    dataRes.put("review_result", Lists.newArrayList());

    pyResult.put("status", 1);
    pyResult.put("msg", "ugc图像审核服务调用失败");
    pyResult.put("data", dataRes);


    String traceId = serviceJsonObject.getString("trace_id");
    String appcode = serviceJsonObject.getString("app_code");
    String url = serviceJsonObject.getString("callback_url");

    // 消费打日志
    REQUEST_LOGGER.info("service={}, appcode={}, traceId={}, params={}", ServiceName.UGC_IMAGE_REVIEW_CONSUMER, appcode, traceId, paramStr);

    // 调用python服务
    try {
      pyResult = ugcImageReviewService.getUgcReviewResPyResult(serviceJsonObject, pyResult, traceId);
    } catch (Exception e) {
      QMonitor.recordOne(UGC_HTTP_ERROR);
      LOGGER.error("ugc图像审核接口异常, params={}, error:{}", serviceJsonObject, e);
    }

    // python结果回调
    // 发送qmq
//    if ("qmq".equals(type)) {
//      ugcImageReviewQmqSendService.sendUgcQmqMsg(taskId, appcode, value, resObj.toJSONString());
//    }
    // 回调callback url

    ugcImageReviewService.ugcCallbackByUrl(url, pyResult, traceId);

  }
}