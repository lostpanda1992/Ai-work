package com.xxxxxx.hotel.imageanalysis.web.controller;

//public class BillOcrController {
//}


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.enums.ServiceName;
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

@Controller
@RequestMapping("/image")
public class BillOcrController {

    private static Logger LOGGER = LoggerFactory.getLogger(BillOcrController.class);

    @Resource
    BillOcrProducer billOcrProducer;


    @RequestMapping(value = "/billOcr", method = RequestMethod.POST)
    @JsonBody
    public JSONObject billOcr(@RequestBody String jsonParam) {

        QMonitor.recordOne(WatcherConstant.BILL_OCR_TOTAL_NUM);
        LOGGER.info("[bill_ocr]billOcrController　入口参数:{}", jsonParam);
        jsonParam = jsonParam.replaceAll("pdf_id","img_id");
        jsonParam = jsonParam.replaceAll("pdf_url","img_url");


        if(JsonUtils.isJsonString(jsonParam)) {
            JSONObject param = JSONObject.parseObject(jsonParam);
            String appcode = param.getString("app_code");
            String traceId = param.getString("trace_id");
            JSONArray serviceArray = param.getJSONArray("service");

            billOcrProducer.sendBillOcrMsg(param);



        } else {
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            LOGGER.info("[bill_ocr]billOcrController　参数非json:{}", jsonParam);
            JSONObject result = new JSONObject();
            result.put("status", -1);
            result.put("info", "参数非json");
            return result;
        }

        JSONObject result = new JSONObject();
        result.put("status", 0);
        return result;
    }

}

