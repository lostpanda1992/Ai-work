package com.xxxxxx.hotel.imageanalysis.web.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.ScreenShotService;
import com.xxxxxx.hotel.imageanalysis.web.service.screenShotOcr.ScreenShotProducer;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xxxxxx.web.spring.annotation.JsonBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/image")
public class CrawlScreenShotOcrController {

    private static Logger LOGGER = LoggerFactory.getLogger(CrawlScreenShotOcrController.class);

    @Resource
    ScreenShotProducer screenShotProducer;

    @Resource
    ScreenShotService screenShotService;


    @RequestMapping(value = "/screenShotOcr", method = RequestMethod.POST)
    @JsonBody
    public JSONObject analysis(@RequestBody String jsonParam) {

        QMonitor.recordOne(WatcherConstant.SCREEN_SHOT_OCR_TOTAL_NUM);

        JSONObject result = new JSONObject();
        if(JsonUtils.isJsonString(jsonParam)) {



            JSONObject param = JSONObject.parseObject(jsonParam);
            LOGGER.info("screenShotOcrController　analysis　入口参数:{}", JsonUtils.jsonObjLimitMaxVal(param, 100));
            String sync = (String)param.get("sync");
            if ("true".equals(sync) || "True".equals(sync)) {

                QMonitor.recordOne(WatcherConstant.SCREEN_SHOT_OCR_TOTAL_SYNC_NUM);

                JSONObject paramObj = new JSONObject();
                paramObj.toString();
                paramObj.put("app_code", param.get("app_code"));

                String traceId = (String)param.get("trace_id");
                paramObj.put("trace_id", traceId);

                String imgType = (String)param.get("img_type");
                paramObj.put("img_type", imgType);

                JSONArray serviceArray = (JSONArray)param.get("service");

                JSONObject input_Json = serviceArray.getJSONObject(0);
                JSONArray img_info_array = input_Json.getJSONArray("params");

                paramObj.put("img_info",img_info_array);

                LOGGER.info("screenShotOcrController　paramObj 参数:{}", JsonUtils.jsonObjLimitMaxVal(paramObj, 100));
                try {
                    result = screenShotService.getScreenShotPyResult(paramObj, result, traceId);

                } catch (Exception e) {
                    QMonitor.recordOne(WatcherConstant.SCREEN_SHOT_OCR_ERROR_NUM);
                    LOGGER.error("screen shotocr接口异常, params={}, error:{}", JsonUtils.jsonObjLimitMaxVal(paramObj, 100), e);
                    result.put("status", 1);
                }

                return result;
            } else {
                screenShotProducer.sendImageAnalysisPlatformMsg(param);
                QMonitor.recordOne(WatcherConstant.SCREEN_SHOT_OCR_TOTAL_ASYNC_NUM);
                result.put("status", 0);
                return result;
            }

        } else {
            QMonitor.recordOne(WatcherConstant.SCREEN_SHOT_OCR_ERROR_NUM);
            LOGGER.info("screenShotOcrController　参数非json:{}", jsonParam);
            result.put("status", 1);
            return result;
        }



    }

}
