package com.xxxxxx.hotel.imageanalysis.web.service.billOCR;

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
public class BillOcrProducer {

    private static Logger LOGGER = LoggerFactory.getLogger(com.xxxxxx.hotel.imageanalysis.web.service.billOCR.BillOcrProducer.class);

    @Resource
    private MessageProducer messageProducer;

    public void sendBillOcrMsg(JSONObject param) {
        Message message = messageProducer.generateMessage(BILL_OCR_SUBJECT);
        String appcode = param.getString("app_code");
        String traceId = param.getString("trace_id");
        JSONArray serviceArray = param.getJSONArray("service");
        String serviceJsonStr = serviceArray.toJSONString();
        message.setProperty("appcode", appcode);
        message.setProperty("traceId", traceId);
        message.setProperty("serviceJson", serviceJsonStr);
        QMonitor.recordOne(BILL_OCR_SEND_QMQ);
        LOGGER.info("[bill_ocr][traceId={}]send BillOcrmMsg QMQ. appcode:{},, serviceJson:{}", traceId, appcode, serviceJsonStr);
        messageProducer.sendMessage(message);
    }
}
