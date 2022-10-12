package com.xxxxxx.hotel.imageanalysis.web.controller;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.IMAGE_SIMILARITY_HTTP_ERROR;

import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.ImageSimiService;
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
public class ImageSimiController {

  @Resource
  ImageSimiService imageSimiService;

  private static Logger LOGGER = LoggerFactory.getLogger(ImageSimiController.class);


  @RequestMapping(value = "/simi", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysis(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.PAGE_TOTAL_NUM);
    JSONObject result = getPageRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String imgInfo1 = param.getString("img_info_1");
      String imgInfo2 = param.getString("img_info_2");

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "传入APPCODE不可为空！");
        return result;
      }
      LOGGER.info("ImageSimiController　analysis　入口参数: appcode={}, traceId={}", appcode, traceId);

      // 调用python服务
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("img_info_1", imgInfo1);
        paramObj.put("img_info_2", imgInfo2);

        result = imageSimiService.getImageSimiPyResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(IMAGE_SIMILARITY_HTTP_ERROR);
        LOGGER.error("图片相似度接口异常, params={}, error:{}",paramObj, e);
      }


    } else {
      LOGGER.info("ImageSimiController　analysis　参数非json:{}", jsonParam);
    }

    return result;
  }

  private JSONObject getPageRes() {
    JSONObject result = new JSONObject();

    result.put("status", 1);
    result.put("msg", "");
    result.put("similarity", 0.0);

    return result;
  }

}
