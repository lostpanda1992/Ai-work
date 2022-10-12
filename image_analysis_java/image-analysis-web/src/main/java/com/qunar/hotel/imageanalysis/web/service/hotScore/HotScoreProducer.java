package com.xxxxxx.hotel.imageanalysis.web.service.hotScore;

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

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;

/**
 * QMQ消息发送服务
 *
 */
@Slf4j
@Service
public class HotScoreProducer {
    private static Logger LOGGER = LoggerFactory.getLogger(com.xxxxxx.hotel.imageanalysis.web.service.hotScore.HotScoreProducer.class);

    @Resource
    private MessageProducer messageProducer;

    public void sendImageAnalysisPlatformMsg(JSONObject param) {
        Message message = messageProducer.generateMessage(IMAGE_ANALYSIS_PLATFORM_SUBJECT);
        String appcode = param.getString("app_code");
        String traceId = param.getString("trace_id");
        String imgType = (String) param.get("img_type");
        JSONArray serviceArray = param.getJSONArray("service");
        String serviceJsonStr = serviceArray.toJSONString();
        message.setProperty("appcode", appcode);
        message.setProperty("traceId", traceId);
        message.setProperty("imgType", imgType);
        message.setProperty("serviceJson", serviceJsonStr);
        QMonitor.recordOne(IMAGE_ANALYSIS_PLATFORM_SEND_QMQ);
        LOGGER.info("sendImageAnalysisPlatformMsg imageHotScore QMQ appcode:{}, traceId:{}, imgType:{}, serviceJson:{}", appcode, traceId, imgType, serviceJsonStr);
        messageProducer.sendMessage(message);
    }
}
