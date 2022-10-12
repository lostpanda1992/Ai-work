package com.xxxxxx.hotel.imageanalysis.web.controller;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_HOTEL_PRICE_HTTP_ERROR;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.HotelPriceService;
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
public class PageController {

  @Resource
  HotelPriceService hotelPriceService;

  private static Logger LOGGER = LoggerFactory.getLogger(PageController.class);


  @RequestMapping(value = "/page", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysis(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.PAGE_TOTAL_NUM);
    JSONObject result = getPageRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String imgBase64 = param.getString("img_base64");

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "传入APPCODE不可为空！");
        return result;
      }
      LOGGER.info("PageController　analysis　入口参数: appcode={}, traceId={}", appcode, traceId);

      // 调用python服务
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("img_base64", imgBase64);

        result = hotelPriceService.getHotelPagePyResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(OCR_HOTEL_PRICE_HTTP_ERROR);
        LOGGER.error("酒店页面识别接口异常, params={}, error:{}",paramObj, e);
      }


    } else {
      LOGGER.info("OcrController　analysis　参数非json:{}", jsonParam);
    }

    return result;
  }

  private JSONObject getPageRes() {
    JSONObject result = new JSONObject();

    result.put("status", 1);
    result.put("msg", "");
    result.put("type", "other");

    return result;
  }

}
