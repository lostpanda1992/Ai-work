package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.image.WhiteDetectService;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.HotelPriceService;
import com.xxxxxx.hotel.imageanalysis.web.utils.JsonUtils;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xxxxxx.web.spring.annotation.JsonBody;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.WHITE_DETECT_HTTP_ERROR;

@Controller
@RequestMapping("/image")
public class WhiteImageDetectController {
    @Resource
    WhiteDetectService whiteDetectService;

    private static Logger LOGGER = LoggerFactory.getLogger(OcrController.class);
    private static String WHITE_IMAGE_DETECT = "whitedetect";
    private static String WHITE_IMAGE_DETECT_URL = "http://l-ml10.wap.beta.cn0:8088/halgo/image/whiteImageDetect";


    @RequestMapping(value = "/whiteImageDetect", method = RequestMethod.POST)
    @JsonBody
    public JSONObject analysis(@RequestBody String jsonParam) {

        long start_time = System.currentTimeMillis();

        QMonitor.recordOne(WatcherConstant.WHITE_DETECT_TOTAL_NUM);
        JSONObject result = new JSONObject();

        if (JsonUtils.isJsonString(jsonParam)) {
            JSONObject param = JSONObject.parseObject(jsonParam);
            String appcode = param.getString("app_code");
            String traceId = param.getString("trace_id");
            String service_name = param.getString("service_name");
            String imgType = param.getString("img_type");
            JSONArray imgInfo = param.getJSONArray("img_info");

            if(service_name == null || service_name.isEmpty()){
                service_name = "whitedetect";
            }
            if(imgType ==null || imgType.isEmpty()){
                imgType = "img_base64";
            }

            LOGGER
                    .info("OcrController　analysis　入口参数: appcode={}, traceId={},source={},imgType={}", appcode,
                            traceId, service_name, imgType);


            if (WHITE_IMAGE_DETECT.equals(service_name)) {

                QMonitor.recordOne(WatcherConstant.WHITE_DETECT_NUM);
                if (!"img_base64".equals(imgType)) {
                    result.put("msg", "传入图片格式有误，需要base64！");
                    result.put("status", 1);
                    return result;
                }

                if (imgInfo.size() > 1) {
                    result.put("msg", "传入图片数量不可大于1！");
                    result.put("status", 1);
                    return result;
                }

                // 调用python服务
                JSONObject paramObj = new JSONObject();
                try {

                    paramObj.put("app_code", appcode);
                    paramObj.put("trace_id", traceId);
                    paramObj.put("img_base64", imgInfo.getJSONObject(0).getString("img_value"));
                    paramObj.put("img_id", imgInfo.getJSONObject(0).getString("img_id"));

                    result = whiteDetectService.getWhiteDetectPyResult(paramObj, result, traceId);

                } catch (Exception e) {
                    QMonitor.recordOne(WHITE_DETECT_HTTP_ERROR);
                    QMonitor.recordOne(WatcherConstant.WHITE_DETECT_ERROR_NUM);
                    LOGGER.error("酒店比价ocr接口异常, url={}, params={}, error:{}",
                            WHITE_IMAGE_DETECT_URL, paramObj, e);
                    result.put("msg", "WHITE_DETECT_HTTP_ERROR！");
                    result.put("status", 1);
                }
            }

        } else {
            QMonitor.recordOne(WatcherConstant.WHITE_DETECT_ERROR_NUM);
            LOGGER.info("OcrController　analysis　参数非json:{}", jsonParam);
            result.put("msg", "参数非json！");
            result.put("status", 1);
        }
        long end_time = System.currentTimeMillis();
        int status = result.getInteger("status");
        LOGGER.info("status:{},timeConsuming:{}", status,end_time-start_time);

        JSONArray results_array = new JSONArray();
        results_array.add(result);
        JSONObject results_object = new JSONObject();
        results_object.put("results",results_array);

        LOGGER.info("results_object:{}", results_object.toJSONString());


        return results_object;
    }
}
