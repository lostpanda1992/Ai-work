package com.xxxxxx.hotel.imageanalysis.web.service.qmq;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.IMAGE_ANALYSIS_PLATFORM_SEND_QMQ;

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
public class UgcImageReviewQmqSendService {

    private static Logger LOGGER = LoggerFactory.getLogger(UgcImageReviewQmqSendService.class);

    @Resource
    private MessageProducer messageProducer;


    public void sendUgcQmqMsg (String traceId, String appcode, String subject, String paramJson) {
        Message message = messageProducer.generateMessage(subject);
        message.setProperty("appid", appcode);
        message.setProperty("data", paramJson);
        message.setProperty("subject", subject);

        QMonitor.recordOne(IMAGE_ANALYSIS_PLATFORM_SEND_QMQ);
        LOGGER.info("sendUgcQmqMsg subject:{}, traceId:{}", subject, traceId);
        messageProducer.sendMessage(message);
    }
}
