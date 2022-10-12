package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.service.ocr.BillOcrWithTableService;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_SCREEN_SHOT_HTTP_RESULT_EMPTY;

public class test {
    private static Logger LOGGER = LoggerFactory.getLogger(OcrController.class);

    @Resource
    public static BillOcrWithTableService billOcrWithTableService;

    public static void main(String[] args) {
        LOGGER.info("TEST");

        String img_url = "http://pf-impic-storecp.xxxxxxzz.com/pf_impic_store_mpic/4c63932558593d2b582e51e88c3cb931.pdf";
        String img_id = "http://pf-impic-storecp.xxxxxxzz.com/pf_impic_store_mpic/4c63932558593d2b582e51e88c3cb931.pdf";
        String appcode = "xxxxxxxx";
        String traceId= "xxxxxxx";
        String BILL_TABLE_DET_URL= "http://l-feedmedia12.c.cn6:9017/algo/ocr/tableDetect" ;
        String BILL_BOX_DET_URL= "http://l-feedmedia12.c.cn6:9017/algo/ocr/textdet";
        String BILL_REC_URL= "http://l-feedmedia12.c.cn6:9017/algo/ocr/billTextRec";



        JSONObject resultObj = new JSONObject();
        try {
            resultObj = billOcrWithTableService.getBillOcrPyResult(img_url,
                    appcode,traceId,"img_url",img_id,BILL_TABLE_DET_URL,BILL_BOX_DET_URL,BILL_REC_URL);

        } catch (Exception e) {
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
//            LOGGER.error("[table_bill_ocr][trace_id={}]table_bill_ocr接口异常, table_url={},box_url={},rec_url={}, params={}, error:{}",
//                    traceId,BILL_TABLE_DET_URL,BILL_BOX_DET_URL,BILL_REC_URL, paramObj, e);
            resultObj.put("status", 1);
        }

    }

    public static void main_F(String[] args) {
        LOGGER.info("TEST");
//        String filePath = "D:\\AE\\python\\ocr\\chineseocr\\master\\chineseocr-master\\chineseocr-master\\ocrResultTest\\onlineImages";
        String filePath ="D:/AE/python/ocr/chineseocr/master/chineseocr-master/chineseocr-master/ocrResultTest/pure_url.txt";
        List<String> urls = getFileLines(filePath);
        LOGGER.info("urls_size:{}",urls.size());
//        String url = "http://qimgs.xxxxxxzz.com/h_crawl_scheduler_plugin_market_01/ocr_47080_07150250158193.jpg";
        long start_time = System.currentTimeMillis();
        long detect_time  = 0;
        long ocr_time  = 0;
        for(int i = 0; i < urls.size();i++) {
            if(i >=1) break;
            String url = urls.get(i);
            LOGGER.info("url:{}",url);
            JSONObject paramobj = getScrrenShotOcrPyResult(url);
            JSONObject result = getScreenShotPyResult(paramobj);
            JSONObject res = result.getJSONArray("res").getJSONObject(0);
            JSONArray time_consume = res.getJSONArray("time_consume");
            LOGGER.info("time_consume:{}",time_consume);
            detect_time += time_consume.getLong(0);
            ocr_time += time_consume.getLong(1);

            LOGGER.info("++++++++++++++++++++++++++++++++++++++++++");
            LOGGER.info("i:{}",i);
            LOGGER.info("aver_time:{}",((float)(System.currentTimeMillis()-start_time))/(i+1));
            LOGGER.info("aver_detect_time_time:{}",((float)(detect_time))/(i+1));
            LOGGER.info("aver_ocr_time_time:{}",((float)(ocr_time))/(i+1));
            LOGGER.info("++++++++++++++++++++++++++++++++++++++++++");
        }
        long end_time = System.currentTimeMillis();
        LOGGER.info("aver_time:{}",((float)(end_time-start_time))/urls.size());




    }

    public static void main_2(String[] args) {
        LOGGER.info("TEST");
//        String filePath = "D:\\AE\\python\\ocr\\chineseocr\\master\\chineseocr-master\\chineseocr-master\\ocrResultTest\\onlineImages";
        String filePath ="D:/AE/python/ocr/chineseocr/master/chineseocr-master/chineseocr-master/ocrResultTest/pure_url.txt";
        List<String> urls = getFileLines(filePath);
        LOGGER.info("urls_size:{}",urls.size());
//        String url = "http://qimgs.xxxxxxzz.com/h_crawl_scheduler_plugin_market_01/ocr_47080_07150250158193.jpg";
        long start_time = System.currentTimeMillis();
        long detect_time  = 0;
        long ocr_time  = 0;
        for(int i = 0; i < urls.size();i++) {
            if(i >=1) break;
            String url = urls.get(i);
            LOGGER.info("url:{}",url);
            JSONObject paramobj = getScrrenShotOcrPyResult(url);
            JSONObject result = getScreenShotPyResult(paramobj);
        }
        long end_time = System.currentTimeMillis();
        LOGGER.info("aver_time:{}",((float)(end_time-start_time))/urls.size());




    }

    public static List<String>  getFileLines(String filepath){
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            File file = new File(filepath);
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file),"UTF-8");
            BufferedReader bf = new BufferedReader(inputReader);
            // 按行读取字符串
            String str;

            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static  List<String> getFileName(String filePath){
        List<String> names = new ArrayList<String>();
        if(null!=filePath && !"".equals(filePath)){
            File file = new File(filePath);
            //判断文件或目录是否存在
//            List<String> names = new ArrayList<String>();
            if(!file.exists()){
                LOGGER.info("【"+filePath + " not exists】");
            }
            //获取该文件夹下所有的文件
            File[] fileArray= file.listFiles();
            File fileName = null;
            for(int i =0;i<fileArray.length;i++){
//                if(i >=1) break;
                fileName = fileArray[i];
                //判断此文件是否存在
                if(fileName.isDirectory()){
                    LOGGER.info("【目录："+fileName.getName()+"】");
                }else{
                    LOGGER.info(fileName.getName());
                    names.add(fileName.getName());
                }
            }
        }

        return names;
    }

    public static JSONObject getScrrenShotOcrPyResult(String url){
        JSONObject paramObj = new JSONObject();
        paramObj.put("app_code", "text_pj");
        paramObj.put("trace_id", "text_pj");
        paramObj.put("img_type", "img_url");
        paramObj.put("old_ocr", 0);
        paramObj.put("show_time", 1);

        JSONObject one_img_json = new JSONObject();
        one_img_json.put("img_value",url);
        one_img_json.put("img_id","100000000001");

        JSONArray one_array_json = new JSONArray();
        one_array_json.add(one_img_json);

        paramObj.put("img_info", one_array_json);
        return paramObj;
    }

    public static JSONObject getScreenShotPyResult(JSONObject paramObj) {

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");

        JSONObject result = new JSONObject(0);

        Stopwatch httpRequestStart = Stopwatch.createStarted();
//        LOGGER.info("crawl_screen_shot_url :{}", OCR_SCREEN_SHOT_URL);

        String OCR_SCREEN_SHOT_URL = "http://algo.ocr.hotelprice.corp.xxxxxx.com/algo/ocr/crawlScreenShot";
//        String OCR_SCREEN_SHOT_URL = "http://l-ml10.wap.beta.cn0:9017/algo/ocr/crawlScreenShot";

        xxxxxxHttpClient httpClient = xxxxxxHttpClient
                .createDefaultClient(50000, 50000, 2000, 2000);
        String postResult = HttpUtils
                .postHttpSync(httpClient, OCR_SCREEN_SHOT_URL, paramObj, headerMap);
        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("请求 crawl_screen_shot_url done , result:{}, time:{}",
                postResult, httpEndTime);

        if (postResult != null) {
            JSONObject res = JSONObject.parseObject(postResult);
            JSONArray ocrRes = res.getJSONArray("result");
            String type = res.getString("img_type");
            int status = res.getInteger("status");
            result.put("status", -1);
            result.put("msg", "screen shot ocr调用成功");
            result.put("res", ocrRes);
            result.put("type", type);

        } else {
            QMonitor.recordOne(OCR_SCREEN_SHOT_HTTP_RESULT_EMPTY);
            LOGGER.error("screen shot ocr python接口返回为空 url:{}, traceId={}", OCR_SCREEN_SHOT_URL);
            result.put("msg", "screen shot ocr无结果");
        }
        httpClient.close();

        return result;
    }


}
