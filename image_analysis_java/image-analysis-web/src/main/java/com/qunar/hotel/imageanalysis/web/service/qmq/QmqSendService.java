package com.xxxxxx.hotel.imageanalysis.web.service.qmq;

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
public class QmqSendService {

    private static Logger LOGGER = LoggerFactory.getLogger(QmqSendService.class);

    @Resource
    private MessageProducer messageProducer;

    public void sendImageAnalysisPlatformMsg (String appcode, String traceId, String serviceJson) {
        Message message = messageProducer.generateMessage(IMAGE_ANALYSIS_PLATFORM_SUBJECT);
        message.setProperty("appcode", appcode);
        message.setProperty("traceId", traceId);
        message.setProperty("serviceJson", serviceJson);
        QMonitor.recordOne(IMAGE_ANALYSIS_PLATFORM_SEND_QMQ);
        LOGGER.info("sendImageAnalysisPlatformMsg QMQ appcode:{}, traceId:{}, serviceJson:{}", appcode, traceId, serviceJson);
        messageProducer.sendMessage(message);
    }

    public void sendImageQmqMsg (String traceId, String subject, String key, String paramJson) {
        Message message = messageProducer.generateMessage(subject);
        message.setProperty(key, paramJson);
        QMonitor.recordOne(IMAGE_ANALYSIS_PLATFORM_SEND_QMQ);
        LOGGER.info("sendImageQmqMsg subject:{}, traceId:{}", subject, traceId);
        messageProducer.sendMessage(message);
    }
}
