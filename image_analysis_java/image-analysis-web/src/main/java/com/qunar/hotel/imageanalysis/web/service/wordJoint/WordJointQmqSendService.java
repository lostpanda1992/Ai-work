package com.xxxxxx.hotel.imageanalysis.web.service.wordJoint;

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
public class WordJointQmqSendService {
    private static Logger LOGGER = LoggerFactory.getLogger(WordJointQmqSendService.class);

    @Resource
    private MessageProducer messageProducer;

    public void sendFirstImageWordJointformMsg(JSONObject param) {
        Message message = messageProducer.generateMessage(IMAGE_WORD_JOINT_SUBJECT);
        String appcode = param.getString("app_code");
        String traceId = param.getString("trace_id");
        JSONArray serviceArray = param.getJSONArray("service");
        String serviceJsonStr = serviceArray.toJSONString();

        message.setProperty("appcode", appcode);
        message.setProperty("traceId", traceId);
        message.setProperty("serviceJson", serviceJsonStr);
        QMonitor.recordOne(IMAGE_ANALYSIS_PLATFORM_SEND_QMQ);
        LOGGER.info("sendImageWordJointformMsg ImageWordJoint QMQ appcode:{}, traceId:{}, serviceJson:{}", appcode, traceId, serviceJsonStr);
        messageProducer.sendMessage(message);
    }
}
