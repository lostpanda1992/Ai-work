package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.wordJoint.WordJointQmqSendService;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xxxxxx.web.spring.annotation.JsonBody;

import javax.annotation.Resource;
import java.util.Map;

import com.xxxxxx.hotel.imageanalysis.web.service.wordJoint.WordJointService;

@Controller
@RequestMapping("/image")
public class ImageWordJointController {
    private static Logger LOGGER = LoggerFactory.getLogger(ImageWordJointController.class);

    @Resource
    WordJointQmqSendService wordJointQmqSendService;

    @Resource
    WordJointService wordJointService;

    @RequestMapping(value = "/wordJoint", method = RequestMethod.POST)
    @JsonBody
    public String analysis(@RequestBody String jsonParam) {

        QMonitor.recordOne(WatcherConstant.IMAGE_WORD_JOINT_TOTAL_NUM);
        LOGGER.info("ImageWordJointController　analysis　入口参数:{}", jsonParam);

        if(JsonUtils.isJsonString(jsonParam)) {
            JSONObject param = JSONObject.parseObject(jsonParam);
            String asynchronous = param.getString("sync");
            if (asynchronous == null){asynchronous = "yes";}

            if ("no".equals(asynchronous)) { // 判断是否为异步，这里是同步流程

                JSONArray serviceArray = param.getJSONArray("service");
                String appCode = param.getString("app_code");
                String traceId = param.getString("trace_id");

                LOGGER.info("ImageWordJointController　analysis　服务不走qmq, 直接走python");
                JSONObject paramObj = new JSONObject();
                paramObj.put("app_code", appCode);
                paramObj.put("trace_id", traceId);
                JSONObject result = new JSONObject();

                for(int i = 0; i < serviceArray.size(); i++) {
                    JSONObject service = serviceArray.getJSONObject(i);
                    paramObj.put("params", service.getJSONArray("params"));

                    Map<String, String> headerMap = Maps.newHashMap();
                    headerMap.put("Content-Type", "application/json");

                    JSONObject resultObj = wordJointService.getFirstImageWordJointPyResult(paramObj, result, traceId);
                    return resultObj.toJSONString();
                }

            } else {
                wordJointQmqSendService.sendFirstImageWordJointformMsg(param);
            }
        } else {
            QMonitor.recordOne(WatcherConstant.IMAGE_WORD_JOINT_ERROR_RECIVE_NUM);
            LOGGER.info("ImageWordJointController　analysis　参数非json:{}", jsonParam);
        }

        JSONObject result = new JSONObject();
        result.put("status", 0);

        return result.toJSONString();
    }
}
