package com.xxxxxx.hotel.imageanalysis.web.service.billOCR;

//public class BIllOcrWithTableConsumer {
//}

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.image.WhiteDetectService;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.BillOcrService;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.BillOcrWithTableService;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.ScreenShotService;
import com.xxxxxx.hotel.imageanalysis.web.service.qconfig.HTTPConfigService;
import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qconfig.client.spring.QConfig;
import xxxxxx.tc.qconfig.client.spring.QConfigLogLevel;
import xxxxxx.tc.qmq.Message;
import xxxxxx.tc.qmq.consumer.annotation.QmqConsumer;
import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.*;


/**
 * QMQ消费服务
 *
 */
@Slf4j
@Service
public class BillOcrWithTableConsumer {

    private static Logger LOGGER = LoggerFactory.getLogger(com.xxxxxx.hotel.imageanalysis.web.service.billOCR.BillOcrWithTableConsumer.class);

    public static final String GROUP = "table_bill_ocr_subject_group";

    private xxxxxxHttpClient httpClient;

    @Resource
    BillOcrWithTableService billOcrWithTableService;


    //    private volatile int imageAnalysisPlatformServiceSleepMs= 1000;
    private volatile int httpTimeout = 50000;

    @QConfig(value = "http.properties", logLevel = QConfigLogLevel.high)
    public void qconfigOnChanged(Map<String, String> config) {
        try {
            init();
        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.QCONFIG_LOAD_ERROR);
            LOGGER.error("[qconfig] http.properties 配置错误.  异常. {}={}", "error", e);
        }
    }

//    @Value("${bill_ocr_url}")
//    private String BILL_OCR_URL;

    @Value("${bill_table_det_url}")
    private String BILL_TABLE_DET_URL;
    @Value("${bill_box_det_url}")
    private String BILL_BOX_DET_URL;
    @Value("${bill_rec_url}")
    private String BILL_REC_URL;

//    private String BILL_TABLE_DET_URL= "http://l-feedmedia12.c.cn6:9017/algo/ocr/tableDetect" ;
//    private String BILL_BOX_DET_URL= "http://l-feedmedia12.c.cn6:9017/algo/ocr/textdet";
//    private String BILL_REC_URL= "http://l-feedmedia12.c.cn6:9017/algo/ocr/billTextRec";

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

    @QmqConsumer(prefix = TABLE_BILL_OCR_SUBJECT, consumerGroup = GROUP)
    public void billOcrConsumer(final Message message) {
        Stopwatch start = Stopwatch.createStarted();
        LOGGER.info("[bill_ocr]消费QMQ BILL_OCRConsumer, message:{}", message);

        String traceId = message.getStringProperty("traceId");
        String appcode = message.getStringProperty("appcode");
        String serviceJson = message.getStringProperty("serviceJson");

        JSONObject paramObj = new JSONObject();
        paramObj.put("trace_id", traceId);
        paramObj.put("app_code", appcode);

        JSONArray serviceArray = JsonUtils.fromJson(serviceJson, JSONArray.class);
        JSONObject input_Json = serviceArray.getJSONObject(0);
        String service_name =  input_Json.getString("service_name");
        JSONArray img_info_array = input_Json.getJSONArray("params");
        String callback_url = input_Json.getString("callback_url");

        String img_url = img_info_array.getJSONObject(0).getString("img_url");
        String img_id = img_info_array.getJSONObject(0).getString("img_id");

        QMonitor.recordOne(WatcherConstant.BILL_OCR_CONSUME_NUM);

        long time_1 = System.currentTimeMillis();
        // 调用python服务
        JSONObject resultObj = new JSONObject();
        try {
            BILL_TABLE_DET_URL="http://l-feedmedia12.c.cn6:9017/algo/ocr/tableDetect";
            BILL_BOX_DET_URL="http://l-feedmedia11.c.cn6:9017/algo/ocr/textdet";
            BILL_REC_URL="http://l-feedmedia10.c.cn6:9017/algo/ocr/billTextRec";
//            resultObj = billOcrWithTableService.getBillOcrPyResult(img_url,
//                    appcode,traceId,"img_url",img_id,BILL_TABLE_DET_URL,BILL_BOX_DET_URL,BILL_REC_URL);
            resultObj = billOcrWithTableService.getBillOcrPyResult(img_url,
                    appcode,traceId,"img_url",img_id,BILL_TABLE_DET_URL,BILL_BOX_DET_URL,BILL_REC_URL);


        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            LOGGER.error("[table_bill_ocr][trace_id={}]table_bill_ocr接口异常, table_url={},box_url={},rec_url={}, params={}, error:{}",
                    traceId,BILL_TABLE_DET_URL,BILL_BOX_DET_URL,BILL_REC_URL, paramObj, e);
            resultObj.put("status", 1);
        }
//        long time_2 = System.currentTimeMillis();
        long elapsed = start.elapsed(TimeUnit.MILLISECONDS);
        QMonitor.recordOne(WatcherConstant.BILL_OCR_TOTAL_TIME, elapsed);
//        QMonitor.recordOne(WatcherConstant.BILL_OCR_TOTAL_TIME,time_2-time_1);

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");
        String callbackResult = "";
        try {
            resultObj = getJavaBillOcrResultParam(resultObj);
            callbackResult = HttpUtils.postHttpSync(httpClient, callback_url, resultObj, headerMap);
        }catch(Exception e){
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            LOGGER.error("[bill_ocr][trace_id={}]call back fail.callback_url:{},serviceJson,{},resultObj:{},e:{}",traceId,callback_url,serviceJson,resultObj.toJSONString(),e);
        }
        LOGGER.info("[bill_ocr][trace_id={}]回调url done url:{}, ocr_time:{}.result:{}", traceId,callback_url,elapsed, callbackResult);


    }

    public JSONArray getPyBillOcrInputParam(JSONArray inputParam){
        JSONArray rtnParam = new JSONArray();

        for(int i =0; i< inputParam.size();i++){
            JSONObject tmp_param = new JSONObject();
            tmp_param.put("img_value",inputParam.getJSONObject(i).getString("img_url"));
            tmp_param.put("img_id",inputParam.getJSONObject(i).getString("img_id"));
            rtnParam.add(tmp_param);
        }

        return rtnParam;
    }

    public JSONObject getJavaBillOcrResultParam(JSONObject pyParam){
        JSONObject rtnParam = new JSONObject(0);

        rtnParam.put("service_name","bill_ocr");

        JSONArray data = new JSONArray();
        int status = pyParam.getInteger("status");
        if(status != 0){
            rtnParam.put("status",1);
            return rtnParam;
        }
        rtnParam.put("status",0);
        String pyResultStr = pyParam.getJSONArray("result").toJSONString();
        pyResultStr = pyResultStr.replaceAll("ocr_result","ocr_infos");
        pyResultStr = pyResultStr.replaceAll("img_id","pdf_id");
        data = JSON.parseArray(pyResultStr);
        rtnParam.put("data",data);

        return rtnParam;
    }


}
