package com.xxxxxx.hotel.imageanalysis.web.service.screenShotOcr;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qmq.Message;
import xxxxxx.tc.qmq.MessageProducer;

import javax.annotation.Resource;

//import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.IMAGE_ANALYSIS_PLATFORM_SEND_QMQ;
//import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.IMAGE_ANALYSIS_PLATFORM_SUBJECT;


import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;

/**
 * QMQ消息发送服务
 *
 */
@Slf4j
@Service
public class ScreenShotProducer {

    private static Logger LOGGER = LoggerFactory.getLogger(com.xxxxxx.hotel.imageanalysis.web.service.screenShotOcr.ScreenShotProducer.class);

    @Resource
    private MessageProducer messageProducer;

//    public void sendImageAnalysisPlatformMsg(String appcode, String traceId, String serviceJson) {
    public void sendImageAnalysisPlatformMsg(JSONObject param) {
        Message message = messageProducer.generateMessage(OCR_SCREEN_SHOT_SUBJECT);
        String appcode = param.getString("app_code");
        String traceId = param.getString("trace_id");
        String task_id = param.getString("task_id");
        String source = param.getString("source");
        JSONArray serviceArray = param.getJSONArray("service");
        String serviceJsonStr = serviceArray.toJSONString();
        message.setProperty("appcode", appcode);
        message.setProperty("traceId", traceId);
        message.setProperty("taskId", task_id);
        message.setProperty("source", source);
        message.setProperty("serviceJson", serviceJsonStr);
        QMonitor.recordOne(OCR_SCREEN_SHOT_SEND_QMQ);
        LOGGER.info("sendScreenShotOcrmMsg QMQ appcode:{}, traceId:{}, serviceJson:{}", appcode, traceId, serviceJsonStr);
        messageProducer.sendMessage(message);
    }
}
