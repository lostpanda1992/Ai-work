package com.xxxxxx.hotel.imageanalysis.web.service.qmq;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.IMAGE_ANALYSIS_PLATFORM_SEND_QMQ;
import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.IMAGE_ANALYSIS_PLATFORM_SUBJECT;

import com.xxxxxx.flight.qmonitor.QMonitor;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qmq.Message;
import xxxxxx.tc.qmq.MessageProducer;

/**
 * QMQ消息发送服务
 *
 */
@Slf4j
@Service
public class XuexinOcrQmqSendService {

    private static Logger LOGGER = LoggerFactory.getLogger(XuexinOcrQmqSendService.class);

    @Resource
    private MessageProducer messageProducer;


    public void sendXuexinQmqMsg (String traceId, String appcode, String subject, String paramJson) {
        Message message = messageProducer.generateMessage(subject);
        message.setProperty("appid", appcode);
        message.setProperty("ocrInfos", paramJson);
        message.setProperty("subject", subject);

        QMonitor.recordOne(IMAGE_ANALYSIS_PLATFORM_SEND_QMQ);
        LOGGER.info("sendXuexinQmqMsg subject:{}, traceId:{}", subject, traceId);
        messageProducer.sendMessage(message);
    }
}
