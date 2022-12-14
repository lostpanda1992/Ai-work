package com.xxxxxx.hotel.imageanalysis.web.controller;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_DETAIL_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_IMAGE_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_POPUP_HTTP_ERROR;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.UI_TEST_LOADING_HTTP_ERROR;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.UiTestService;
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
public class UiTestController {

  @Resource
  UiTestService uiTestService;

  private static Logger LOGGER = LoggerFactory.getLogger(UiTestController.class);

  @RequestMapping(value = "/uiTest", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysis(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UI_TEST_TOTAL_NUM);
    JSONObject result = geUiTestRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      JSONArray text = param.getJSONArray("text");
      String imgType = param.getString("img_type");
      String imgValue = param.getString("img_value");

      LOGGER
          .info("uiTest ????????????: appcode={}, traceId={},text={},imgType={}", appcode,
              traceId, text, imgType);

      if (text.isEmpty()) {
        result.put("msg", "???????????????????????????");
        return result;
      }

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ??????python??????
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("text", text);
        paramObj.put("img_type", imgType);
        paramObj.put("img_value", imgValue);

        result = uiTestService.getUiTestPyResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(UI_TEST_HTTP_ERROR);
        LOGGER.error("uiTest????????????, traceId={}, error:{}", traceId, e);
      }


    } else {
      result.put("msg", "?????????json???");
      LOGGER.info("uiTest????????????json:{}", jsonParam);
    }

    return result;
  }


  @RequestMapping(value = "/uiTestDetail", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysisDetail(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UI_TEST_DETAIL_TOTAL_NUM);
    JSONObject result = geUiTestRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String imgType = param.getString("img_type");
      String imgValue = param.getString("img_value");

      LOGGER
          .info("uiTest ????????????: appcode={}, traceId={},imgType={}", appcode,
              traceId, imgType);

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ??????python??????
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("img_type", imgType);
        paramObj.put("img_value", imgValue);

        result = uiTestService.getUiTestPyDetailResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(UI_TEST_DETAIL_HTTP_ERROR);
        LOGGER.error("uiTest????????????, traceId={}, error:{}", traceId, e);
      }


    } else {
      result.put("msg", "?????????json???");
      LOGGER.info("uiTestDetail????????????json:{}", jsonParam);
    }

    return result;
  }


  @RequestMapping(value = "/uiTestImage", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysisImage(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UI_TEST_IMAGE_TOTAL_NUM);
    JSONObject result = geUiTestRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String imgValueScreenhot = param.getString("img_value_screenhot");
      String imgValueTemplate = param.getString("img_value_template");

      LOGGER
          .info("uiTestImage ????????????: appcode={}, traceId={}", appcode, traceId);

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ??????python??????
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("img_value_screenhot", imgValueScreenhot);
        paramObj.put("img_value_template", imgValueTemplate);

        result = uiTestService.getUiTestPyImageResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(UI_TEST_IMAGE_HTTP_ERROR);
        LOGGER.error("uiTest????????????, traceId={}, error:{}", traceId, e);
      }


    } else {
      result.put("msg", "?????????json???");
      LOGGER.info("uiTestImage????????????json:{}", jsonParam);
    }

    return result;
  }


  @RequestMapping(value = "/uiTestPopup", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysisPopup(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UI_TEST_POPUP_TOTAL_NUM);
    JSONObject result = geUiTestRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String imgValue = param.getString("img_value");

      LOGGER
          .info("uiTestPopup ????????????: appcode={}, traceId={}", appcode, traceId);

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ??????python??????
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("img_value", imgValue);

        result = uiTestService.getUiTestPyPopupResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(UI_TEST_POPUP_HTTP_ERROR);
        LOGGER.error("uiTestPopup????????????, traceId={}, error:{}", traceId, e);
      }

    } else {
      result.put("msg", "?????????json???");
      LOGGER.info("uiTestPopup????????????json:{}", jsonParam);
    }

    return result;
  }


  @RequestMapping(value = "/uiTestBlock", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysisBlock(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UI_TEST_BLOCK_TOTAL_NUM);
    JSONObject result = geUiTestRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String text = param.getString("text");
      String imgValue = param.getString("img_value");

      LOGGER
          .info("uiTestBlock ????????????: appcode={}, traceId={},text={}", appcode,
              traceId, text);

      if (text.isEmpty()) {
        result.put("msg", "???????????????????????????");
        return result;
      }

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ??????python??????
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("text", text);
        paramObj.put("img_value", imgValue);

        result = uiTestService.getUiTestPyBlockResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(UI_TEST_HTTP_ERROR);
        LOGGER.error("uiTest????????????, traceId={}, error:{}", traceId, e);
      }


    } else {
      result.put("msg", "?????????json???");
      LOGGER.info("uiTestBlock????????????json:{}", jsonParam);
    }

    return result;
  }


  @RequestMapping(value = "/uiTestSlide", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysisSlide(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UI_TEST_SLIDE_TOTAL_NUM);
    JSONObject result = geUiTestRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String imgValue = param.getString("img_value");

      LOGGER
          .info("uiTestSlide ????????????: appcode={}, traceId={}", appcode,
              traceId);

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ??????python??????
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("img_value", imgValue);

        result = uiTestService.getUiTestPySlideResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(UI_TEST_HTTP_ERROR);
        LOGGER.error("uiTest????????????, traceId={}, error:{}", traceId, e);
      }


    } else {
      result.put("msg", "?????????json???");
      LOGGER.info("uiTestSlide????????????json:{}", jsonParam);
    }

    return result;
  }



  @RequestMapping(value = "/uiTestLoading", method = RequestMethod.POST)
  @JsonBody
  public JSONObject analysisLoading(@RequestBody String jsonParam) {

    QMonitor.recordOne(WatcherConstant.UI_TEST_LOADING_TOTAL_NUM);
    JSONObject result = geUiTestRes();

    if (JsonUtils.isJsonString(jsonParam)) {
      JSONObject param = JSONObject.parseObject(jsonParam);
      String appcode = param.getString("app_code");
      String traceId = param.getString("trace_id");
      String imgValue = param.getString("img_value");

      LOGGER
          .info("uiTestLoading ????????????: appcode={}, traceId={}", appcode, traceId);

      if (StringUtils.isEmpty(appcode)) {
        result.put("msg", "??????APPCODE???????????????");
        return result;
      }

      // ??????python??????
      JSONObject paramObj = new JSONObject();
      try {

        paramObj.put("app_code", appcode);
        paramObj.put("trace_id", traceId);
        paramObj.put("img_value", imgValue);

        result = uiTestService.getUiTestPyLoadingResult(paramObj, result, traceId);

      } catch (Exception e) {
        QMonitor.recordOne(UI_TEST_LOADING_HTTP_ERROR);
        LOGGER.error("uiTestLoading????????????, traceId={}, error:{}", traceId, e);
      }

    } else {
      result.put("msg", "?????????json???");
      LOGGER.info("uiTestLoading????????????json:{}", jsonParam);
    }

    return result;
  }



  private JSONObject geUiTestRes() {
    JSONObject result = new JSONObject();

    // result.put("status", 1);
    result.put("msg", "");
    result.put("res", "");
    return result;
  }

}
