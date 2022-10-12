package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.image.ImageButtonDetectService;
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

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.BUTTON_DETECT_HTTP_ERROR;

@Controller
@RequestMapping("/image")
public class ImageButtonDetectController {
    @Resource
    ImageButtonDetectService imageButtonDetectService;

    private static Logger LOGGER = LoggerFactory.getLogger(ImageButtonDetectController.class);
    private static String BUTTON_IMAGE_DETECT = "button_detect";
    private static String BUTTON_IMAGE_DETECT_URL = "http://l-ml10.wap.beta.cn0:8088/halgo/image/buttonImageDetect";


    @RequestMapping(value = "/appButtonDetect", method = RequestMethod.POST)
    @JsonBody
    public JSONObject analysis(@RequestBody String jsonParam) {

        long start_time = System.currentTimeMillis();

        QMonitor.recordOne(WatcherConstant.BUTTON_DETECT_TOTAL_NUM);
        JSONObject result = new JSONObject();

        if (JsonUtils.isJsonString(jsonParam)) {
            JSONObject param = JSONObject.parseObject(jsonParam);
            String appcode = param.getString("app_code");
            String traceId = param.getString("trace_id");
            String service_name = param.getString("service_name");
            String imgType = param.getString("img_type");
            JSONArray imgInfo = param.getJSONArray("img_info");

            if(service_name == null || service_name.isEmpty()){
                service_name = "button_detect";
            }
            if(imgType ==null || imgType.isEmpty()){
                imgType = "img_base64";
            }

            LOGGER
                    .info("OcrController　analysis　入口参数: appcode={}, traceId={},source={},imgType={}", appcode,
                            traceId, service_name, imgType);


            if (BUTTON_IMAGE_DETECT.equals(service_name)) {

                QMonitor.recordOne(WatcherConstant.BUTTON_DETECT_NUM);
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
                    paramObj.put("img_value", imgInfo.getJSONObject(0).getString("img_value"));
                    paramObj.put("img_id", imgInfo.getJSONObject(0).getString("img_id"));

                    result = imageButtonDetectService.getButtonDetectPyResult(paramObj, result, traceId);

                } catch (Exception e) {
                    QMonitor.recordOne(BUTTON_DETECT_HTTP_ERROR);
                    QMonitor.recordOne(WatcherConstant.BUTTON_DETECT_ERROR_NUM);
                    LOGGER.error("开关按钮检测接口异常, url={}, params={}, error:{}",
                            BUTTON_IMAGE_DETECT_URL, paramObj, e);
                    result.put("msg", "BUTTON_DETECT_HTTP_ERROR！");
                    result.put("status", 1);
                }
            }

        } else {
            QMonitor.recordOne(WatcherConstant.BUTTON_DETECT_ERROR_NUM);
            LOGGER.info("image button controller　analysis　参数非json:{}", jsonParam);
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
