package com.xxxxxx.hotel.imageanalysis.web.controller;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UGC_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UGC_SUBJECT;

import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.image.UgcImageReviewService;
import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xxxxxx.web.spring.annotation.JsonBody;

@Controller
@Component
@RequestMapping("/image")
public class UgcImageReviewController {

  @Resource
  QmqSendService qmqSendService;
  @Resource
  UgcImageReviewService ugcImageReviewService;


  private static Logger LOGGER = LoggerFactory.getLogger(UgcImageReviewController.class);
  private static Logger REQUEST_LOGGER = LoggerFactory.getLogger("request_result");
  private static String UGC_QMQ_KEY = "ugc_qmq_key";

  @PostConstruct
  private void warmup(){
    String warmupStr = "{\"app_code\":\"algo_warmup\", \"trace_id\":\"qtraceid\",\"comm_id\":\"test\",\"callback_url\":\"callback_url\",\"comm_img_info\": [{\"img_id\": \"11\",\"img_url\": \"http://ugcimg.xxxxxxzz.com/imgs/202108/01/rMz5CU2rhdfnqJmp8720.jpg\"}],\"hotel_img_info\": [\"http://ugcimg.xxxxxxzz.com/imgs/202108/01/rMz5CU2rhdfnqJmp8720.jpg\"]}\n";
    JSONObject serviceJsonObject = JSONObject.parseObject(warmupStr);

    JSONObject pyResult = new JSONObject();
    pyResult.put("status", 1);
    pyResult.put("msg", "ugc??????????????????????????????");
    pyResult.put("data", Maps.newHashMap());

    String traceId = serviceJsonObject.getString("trace_id");

    // ??????python??????
    try {
      for (int i = 0;i <= 16; i++) {
        Stopwatch httpRequestStart = Stopwatch.createStarted();

        pyResult = ugcImageReviewService.getUgcReviewResPyResult(serviceJsonObject, pyResult, traceId);
        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("ugc???????????????warmup num:{}, res ={}???time={}", i, pyResult, httpEndTime);
      }
    } catch (Exception e) {
      QMonitor.recordOne(UGC_HTTP_ERROR);
      LOGGER.error("ugc????????????????????????, params={}, error:{}", serviceJsonObject, e);
    }
  }

  @RequestMapping(value = "/review", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysis(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UGC_TOTAL_NUM);
    JSONObject result = getPageRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String commId = param.getString("comm_id");
      String callbackUrl = param.getString("callback_url");

      JSONArray commImgInfo = param.getJSONArray("comm_img_info");
      JSONArray hotelImgInfo = param.getJSONArray("hotel_img_info");


      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ???????????????
      REQUEST_LOGGER.info("service={}, appcode={}, traceId={}, params={}", ServiceName.UGC_IMAGE_REVIEW, appcode, traceId, jsonParam);

      // ??????ugc??????????????????????????????
      JSONObject paramObj = new JSONObject();
      paramObj.put("app_code", appcode);
      paramObj.put("trace_id", traceId);
      paramObj.put("comm_id", commId);
      paramObj.put("callback_url", callbackUrl);
      paramObj.put("comm_img_info", commImgInfo);
      paramObj.put("hotel_img_info", hotelImgInfo);


      // send qmq
      paramObj.put("trace_id", traceId);
      qmqSendService
          .sendImageQmqMsg(traceId, UGC_SUBJECT, UGC_QMQ_KEY, paramObj.toJSONString());
      result.put("status", 0);
      result.put("msg", "ugc??????????????????????????????");


    } else {
      LOGGER.info("UgcImageReviewController???analysis????????????json:{}", jsonParam);
    }

    return result;
  }

  private JSONObject getPageRes() {
    JSONObject result = new JSONObject();

    result.put("status", 1);
    result.put("msg", "ugc??????????????????????????????");

    return result;
  }


  @Test
  public void test(){

   String str = "{\"app_code\":\"algo_warmup\", \"trace_id\":\"qtraceid\",\"comm_id\":\"test\",\"callback_url\":\"callback_url\",\"comm_img_info\": [{\"img_id\": \"11\",\"img_url\": \"http://ugcimg.xxxxxxzz.com/imgs/202108/01/rMz5CU2rhdfnqJmp8720.jpg\"}],\"hotel_img_info\": [\"http://ugcimg.xxxxxxzz.com/imgs/202108/01/rMz5CU2rhdfnqJmp8720.jpg\"]}\n";
    //System.out.println(JSONObject.parseObject(str));

    System.out.println(ServiceName.UGC_IMAGE_REVIEW + "????????????");

  }

}
