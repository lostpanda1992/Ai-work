package com.xxxxxx.hotel.imageanalysis.web.controller;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_HOTEL_PRICE_HTTP_ERROR;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.HotelPriceService;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import javax.annotation.Resource;
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
public class OcrController {

  @Resource
  HotelPriceService hotelPriceService;

  private static Logger LOGGER = LoggerFactory.getLogger(OcrController.class);
  private static String SOURCE_HOTEL_PRICE = "hotelprice";
  private static String OCR_HOTEL_PRICE_URL = "http://l-ml10.wap.beta.cn0:9017/algo/ocr/hotelPrice";


  @RequestMapping(value = "/ocr", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysis(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.OCR_TOTAL_NUM);
    JSONObject result = getHotelPriceRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String source = param.getString("source");
      String imgType = param.getString("img_type");
      JSONArray imgInfo = param.getJSONArray("img_info");

      LOGGER
          .info("OcrController　analysis　入口参数: appcode={}, traceId={},source={},imgType={}", appcode,
              traceId, source, imgType);

      // 酒店比价ocr
      if (SOURCE_HOTEL_PRICE.equals(source)) {

        QMonitor.recordOne(WatcherConstant.OCR_HOTEL_PRICE_NUM);
        if (!"img_base64".equals(imgType)) {
          result.put("msg", "传入图片格式有误，需要base64！");
          return result;
        }

        if (imgInfo.size() > 1) {
          result.put("msg", "传入图片数量不可大于1！");
          return result;
        }

        // 调用python服务
        JSONObject paramObj = new JSONObject();
        try {

          paramObj.put("app_code", appcode);
          paramObj.put("trace_id", traceId);
          paramObj.put("img_base64", imgInfo.getJSONObject(0).getString("img_value"));

          result = hotelPriceService.getHotelPricePyResult(paramObj, result, traceId);

        } catch (Exception e) {
          QMonitor.recordOne(OCR_HOTEL_PRICE_HTTP_ERROR);
          LOGGER.error("酒店比价ocr接口异常, url={}, params={}, error:{}",
              OCR_HOTEL_PRICE_URL, paramObj, e);
        }
      }

    } else {
      QMonitor.recordOne(WatcherConstant.OCR_TOTAL_NUM);
      LOGGER.info("OcrController　analysis　参数非json:{}", jsonParam);
    }

    return result;
  }

  private JSONObject getHotelPriceRes() {
    JSONObject result = new JSONObject();

    result.put("status", 1);
    result.put("msg", "");
    result.put("res", "");
    result.put("type", "other");

    return result;
  }

}
