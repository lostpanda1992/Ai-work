package com.xxxxxx.hotel.imageanalysis.web.controller;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_XUEXIN_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.XUEXIN_OCR_SUBJECT;

import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.OcrXuexinService;
import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
public class OcrXuexinController {

  @Resource
  OcrXuexinService ocrXuexinService;

  @Resource
  QmqSendService qmqSendService;

  private static Logger LOGGER = LoggerFactory.getLogger(OcrXuexinController.class);
  private static String XUEXIN_QMQ_KEY = "xuexin_qmq_key";

  @RequestMapping(value = "/xuexin", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysis(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.XUEXIN_TOTAL_NUM);
    JSONObject result = getPageRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      JSONObject data = param.getJSONObject("data");
      String taskId = data.getString("task_id");

      String sync = param.getString("sync");

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "传入APPCODE不可为空！");
        return result;
      }
      LOGGER.info("OcrXuexinController　analysis　入口参数: appcode={}, traceId={}", appcode, taskId);

      // 构造ocr服务接收参数
      JSONObject paramObj = new JSONObject();
      paramObj.put("app_code", appcode);
      paramObj.put("data", data);

      // 判断同步异步
      if ("true".equals(sync) || "True".equals(sync)) {

        // 调用python服务
        try {
          result = ocrXuexinService.getOcrXuexinPyResult(paramObj, result, taskId);
        } catch (Exception e) {
          QMonitor.recordOne(OCR_XUEXIN_HTTP_ERROR);
          LOGGER.error("学信网ocr接口异常, params={}, error:{}", paramObj, e);
        }

      } else {
        // send qmq
        paramObj.put("taskId", taskId);
        qmqSendService
            .sendImageQmqMsg(taskId, XUEXIN_OCR_SUBJECT, XUEXIN_QMQ_KEY, paramObj.toJSONString());
        result.put("status", 0);
        result.put("msg", "学信网ocr服务调用成功");
      }


    } else {
      LOGGER.info("OcrXuexinController　analysis　参数非json:{}", jsonParam);
    }

    return result;
  }

  private JSONObject getPageRes() {
    JSONObject result = new JSONObject();

    result.put("status", 1);
    result.put("msg", "学信网ocr服务调用失败");

    return result;
  }

}
