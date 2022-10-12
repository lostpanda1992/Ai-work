package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xxxxxx.web.spring.annotation.JsonBody;

import javax.annotation.Resource;
import com.xxxxxx.hotel.imageanalysis.web.service.hotScore.HotScoreProducer;
import com.xxxxxx.hotel.imageanalysis.web.service.hotScore.HotScoreService;
import com.xxxxxx.hotel.imageanalysis.web.utils.ImageParamsBuildUtils;

@Controller
@RequestMapping("/image")
public class ImageHotScoreController {

    private static Logger LOGGER = LoggerFactory.getLogger(ImageHotScoreController.class);

    @Resource
    HotScoreService hotScoreService;

    @Resource
    HotScoreProducer hotScoreProducer;


    @RequestMapping(value = "/hotScore", method = RequestMethod.POST)
    @JsonBody
    public JSONObject analysis(@RequestBody String jsonParam) {

        QMonitor.recordOne(WatcherConstant.IMAGE_HOT_SCORE_NUM);
        LOGGER.info("imageHotScoreController　hotScore　入口参数:{}", jsonParam);
        JSONObject result = new JSONObject();

        if (JsonUtils.isJsonString(jsonParam)) {
            JSONObject param = JSONObject.parseObject(jsonParam);
            String sync = (String) param.get("sync");
            if ("true".equals(sync) || "True".equals(sync)) {
                QMonitor.recordOne(WatcherConstant.IMAGE_HOT_SCORE_SYNC_NUM);

                JSONObject paramObj = new JSONObject();
                JSONArray params = new JSONArray();

                paramObj.put("app_code", param.get("app_code"));
                String traceId = (String) param.get("trace_id");
                paramObj.put("trace_id", traceId);
                String imgType = (String) param.get("img_type");
                JSONArray serviceArray = param.getJSONArray("service");

                JSONObject paramPyObj = ImageParamsBuildUtils.getImageParams(serviceArray, imgType, paramObj, params);

                LOGGER.info("imageHotScoreController　paramObj 参数:{}", JsonUtils.jsonObjLimitMaxVal(paramPyObj, 100));
                try {
                    result = hotScoreService.getImageHotScorePyResult(paramPyObj, result, traceId);

                } catch (Exception e) {
                    QMonitor.recordOne(WatcherConstant.IMAGE_HOT_SCORE_SYNC_ERROR_NUM);
                    LOGGER.error("image hotscore接口异常, params={}, error:{}", JsonUtils.jsonObjLimitMaxVal(paramPyObj, 100), e);
                    result.put("status", 1);
                }

                return result;
            } else {
                hotScoreProducer.sendImageAnalysisPlatformMsg(param);
                QMonitor.recordOne(WatcherConstant.IMAGE_HOT_SCORE_TOTAL_ASYNC_NUM);
                result.put("status", 0);
                return result;
            }

        } else {
            QMonitor.recordOne(WatcherConstant.IMAGE_HOT_SCORE_ERROR_NUM);
            LOGGER.info("imageHotScoreController　参数非json:{}", jsonParam);
            result.put("status", 1);
            return result;
        }
    }

}