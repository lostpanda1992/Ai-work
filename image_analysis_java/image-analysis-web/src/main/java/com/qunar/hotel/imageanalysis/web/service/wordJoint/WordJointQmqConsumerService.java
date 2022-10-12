package com.xxxxxx.hotel.imageanalysis.web.service.wordJoint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import xxxxxx.tc.qmq.Message;
import xxxxxx.tc.qmq.consumer.annotation.QmqConsumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;

/**
 * QMQ消费服务
 *
 */
@Slf4j
@Service
public class WordJointQmqConsumerService {

    private static Logger LOGGER = LoggerFactory.getLogger(WordJointQmqConsumerService.class);

    public static final String GROUP = "image_word_joint_subject_group";

    private xxxxxxHttpClient httpClient;

    private volatile int httpTimeout = 50000;

    @Resource
    WordJointService wordJointService;

    @PostConstruct
    private void init() {
        httpClient = xxxxxxHttpClient.createDefaultClient(httpTimeout, httpTimeout, 2000, 2000);
    }

    @PreDestroy
    private void shutdown() {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    @QmqConsumer(prefix = IMAGE_WORD_JOINT_SUBJECT, consumerGroup = GROUP)
    public void firstImageWordJointConsumer(final Message message) {
        Stopwatch start = Stopwatch.createStarted();
        LOGGER.info("消费QMQ firstImageWordJointConsumer, message:{}", message);

        JSONObject resultObject = new JSONObject();

        JSONObject paramObj = new JSONObject();
        paramObj.put("app_code", message.getStringProperty("appcode"));

        String traceId = message.getStringProperty("traceId");
        paramObj.put("trace_id", traceId);

        String serviceJson = message.getStringProperty("serviceJson");
        JSONArray serviceArray = JsonUtils.fromJson(serviceJson, JSONArray.class);


        for(int i = 0; i < serviceArray.size(); i++) {
            JSONObject service = serviceArray.getJSONObject(i);
            JSONArray params = service.getJSONArray("params");
            String callbackUrl = service.getString("callback_url");
            paramObj.put("params", params);

            Map<String, String> headerMap = Maps.newHashMap();
            headerMap.put("Content-Type", "application/json");

            resultObject = wordJointService.getFirstImageWordJointPyResult(paramObj,resultObject,traceId);
            LOGGER.info("头图压字和图片拼接 java端最终处理结果 resultObject:{}, traceid:{}", resultObject, traceId);

            String callbackResultFirstImageWordJoint = HttpUtils.postHttpSync(httpClient, callbackUrl, resultObject, headerMap);
            LOGGER.info("头图压字和图片拼接 callback url done traceid:{}, callbackurl:{}, result:{}", traceId, callbackUrl, callbackResultFirstImageWordJoint);
        }
    }
}
