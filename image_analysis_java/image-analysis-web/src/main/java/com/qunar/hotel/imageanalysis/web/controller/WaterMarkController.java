package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
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
import com.xxxxxx.hotel.imageanalysis.web.service.watermark.WaterMarkService;
import java.util.Map;

@Controller
@RequestMapping("/image")
public class WaterMarkController {

    private static Logger LOGGER = LoggerFactory.getLogger(WaterMarkController.class);

    @Resource
    QmqSendService qmqSendService;

    @Resource
    WaterMarkService waterMarkService;

    @RequestMapping(value = "/analysis", method = RequestMethod.POST)
    @JsonBody
    public String analysis(@RequestBody String jsonParam) {

        QMonitor.recordOne(WatcherConstant.IMAGE_ANALYSIS_NUM);
        LOGGER.info("ImageAnalysisController　analysis　入口参数:{}", jsonParam);


        if(JsonUtils.isJsonString(jsonParam)) {
            JSONObject param = JSONObject.parseObject(jsonParam);
            String appCode = param.getString("app_code");
            String traceId = param.getString("trace_id");
            String asynchronous = param.getString("asynchronous");
            if (asynchronous == null){asynchronous = "yes";}
            JSONArray serviceArray = param.getJSONArray("service");

            if ("no".equals(asynchronous)) { // 判断是否为异步，这里是同步流程
                LOGGER.info("ImageAnalysisController　analysis　服务不走qmq, 直接走python");
                JSONObject paramObj = new JSONObject();
                paramObj.put("app_code", appCode);
                paramObj.put("trace_id", traceId);
                String result = null;

                for(int i = 0; i < serviceArray.size(); i++) {
                    JSONObject service = serviceArray.getJSONObject(i);
                    String serviceName = service.getString("service_name");
                    paramObj.put("params", service.getJSONArray("params"));

                    Map<String, String> headerMap = Maps.newHashMap();
                    headerMap.put("Content-Type", "application/json");

                    JSONObject resultObj = waterMarkService.getWaterMarkPyResult(service, serviceName, paramObj, result, traceId, headerMap);
                    return resultObj.toJSONString();
                }

            } else {
                qmqSendService.sendImageAnalysisPlatformMsg(appCode, traceId, serviceArray.toJSONString());
            }
        } else {
            QMonitor.recordOne(WatcherConstant.IMAGE_ANALYSIS_NUM);
            LOGGER.info("ImageAnalysisController　analysis　参数非json:{}", jsonParam);
        }

        JSONObject result = new JSONObject();
        result.put("status", 0);

        return result.toJSONString();
    }

}

