package com.xxxxxx.hotel.imageanalysis.web.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;
import com.xxxxxx.hotel.imageanalysis.web.service.billOCR.BillOcrWithTableProducer;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.BillOcrWithTableService;
import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
import com.xxxxxx.hotel.imageanalysis.web.service.billOCR.BillOcrProducer;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xxxxxx.web.spring.annotation.JsonBody;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/image")
public class BillOcrWithTableController {

    private static Logger LOGGER = LoggerFactory.getLogger(BillOcrWithTableController.class);

    @Resource
    BillOcrWithTableProducer billOcrWithTableProducer;

    @Resource
    BillOcrWithTableService billOcrWithTableService;


    @RequestMapping(value = "/tableBillOcr", method = RequestMethod.POST)
    @JsonBody
    public JSONObject billOcr(@RequestBody String jsonParam) {

        QMonitor.recordOne(WatcherConstant.BILL_OCR_TOTAL_NUM);
        LOGGER.info("[table_bill_ocr]billOcrController　入口参数:{}", jsonParam);
        jsonParam = jsonParam.replaceAll("pdf_id","img_id");
        jsonParam = jsonParam.replaceAll("pdf_url","img_url");

        JSONObject param = new JSONObject();
        String post_method = null;
        String appcode = null;
        String traceId = null;

        if(JsonUtils.isJsonString(jsonParam)) {
            param = JSONObject.parseObject(jsonParam);
            appcode = param.getString("app_code");
            traceId = param.getString("trace_id");
//            JSONArray serviceArray = param.getJSONArray("service");
            post_method = param.getString("post_method");

//            billOcrWithTableProducer.sendBillOcrMsg(param);

        } else {
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            LOGGER.info("[table_bill_ocr]billOcrController　参数非json:{}", jsonParam);
            JSONObject result = new JSONObject();
            result.put("status", -1);
            result.put("info", "参数非json");
            return result;
        }

        LOGGER.info("[table_bill_ocr][trace_id={}]tableBillOcrController 入口参数.appcode:{},param:{}", traceId,
                appcode, jsonParam);

        if(post_method == null || !post_method.equals("syn")){
            post_method = "asyn";
        }

        // 异步执行请求
        if(post_method.equals("asyn")){
            billOcrWithTableProducer.sendBillOcrMsg(param);
            JSONObject result = new JSONObject();
            result.put("status", 0);
            return result;
        }

        // 同步执行请求
        JSONArray serviceArray = param.getJSONArray("service");
        JSONObject input_Json = serviceArray.getJSONObject(0);
        String service_name =  input_Json.getString("service_name");
        String callback_url = input_Json.getString("callback_url");

        JSONArray img_info_array = input_Json.getJSONArray("params");
        String img_url = img_info_array.getJSONObject(0).getString("img_url");
        String img_id = img_info_array.getJSONObject(0).getString("img_id");

        Stopwatch start = Stopwatch.createStarted();
        JSONObject resultObj = new JSONObject();
        try {
            resultObj = billOcrWithTableService.getBillOcrPyResult(img_url,
                    appcode,traceId,"img_url",img_id,null,null,null);


        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            LOGGER.error("[table_bill_ocr][trace_id={}]table_bill_ocr接口异常, appcode={}, params={}, error:{}",
                    traceId,appcode, jsonParam, e);
            resultObj.put("status", 1);
        }
        long elapsed = start.elapsed(TimeUnit.MILLISECONDS);
        QMonitor.recordOne(WatcherConstant.BILL_OCR_TOTAL_TIME, elapsed);

        resultObj = getJavaBillOcrResultParam(resultObj);
        return resultObj;
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
