package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
//import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxxxxx.tc.qtracer.QTracer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

//import static com.xxxxxx.hotel.imageanalysis.web.constants.ServiceNameConstant.OCR_SCREEN_SHOT_HTTP_RESULT_EMPTY;

public class testBillOcr {

    private static Logger LOGGER = LoggerFactory.getLogger(OcrController.class);

    private static ListeningExecutorService executorService = MoreExecutors.listeningDecorator(QTracer.wrap(Executors.newCachedThreadPool()));



    public static void main_1(String[] args) {
        LOGGER.info("TEST");
        String path = "D:/AE/python/ocr/paddle/PaddleOCR/train_data/small_images/011002000711_99754305.pdf0.png";
        String im_base64 = getImgBase64(path);

        String api_url_tabel_det = "http://l-ml10.wap.beta.cn0:9017/algo/ocr/tableDetect";
        String api_url_box_det = "http://l-ml10.wap.beta.cn0:9017/algo/ocr/textdet";
        String api_url_bill_rec = "http://l-ml10.wap.beta.cn0:9017/algo/ocr/billTextRec";


//        get_ocr_post_body_py(String img_base64, List<Object> img_boxes, List<Object> table_boxes)
        JSONObject post_params = get_ocr_post_body_py(im_base64, null,null );

        JSONObject result_table_boxes = getTableBillOcrPyResult(post_params,api_url_tabel_det);
        JSONArray table_boxes = result_table_boxes.getJSONArray("result").getJSONObject(0).getJSONArray("table_detect_result");
        JSONObject result_det_boxes = getTableBillOcrPyResult(post_params,api_url_box_det);
        JSONArray det_boxes = result_det_boxes.getJSONArray("result").getJSONObject(0).getJSONArray("text_detect_result");

        post_params = get_ocr_post_body_py(im_base64, det_boxes, table_boxes);
        JSONObject result_bill_rec = getTableBillOcrPyResult(post_params,api_url_bill_rec);
        LOGGER.info("result_bill_rec:{}",result_bill_rec.toJSONString());



    }

    public static void main_2(String[] args) {
        LOGGER.info("TEST");
        String path = "D:/AE/python/ocr/paddle/PaddleOCR/train_data/small_images/011002000711_99754305.pdf0.png";
        String im_base64 = getImgBase64(path);

        String api_url_box_det = "http://l-ml10.wap.beta.cn0:9017/algo/ocr/textdet";


//        get_ocr_post_body_py(String img_base64, List<Object> img_boxes, List<Object> table_boxes)
        JSONObject post_params = get_ocr_post_body_py(im_base64, null,null );

//        JSONObject result_det_boxes = new JSONObject();
       JSONObject result_det_boxes = getTableBillOcrPyResult(post_params,api_url_box_det);
        JSONArray det_boxes = result_det_boxes.getJSONArray("result").getJSONObject(0).getJSONArray("text_detect_result");

       LOGGER.info("result_det_boxes:{}",result_det_boxes.toJSONString());



    }



    public static void main(String[] args) {
        LOGGER.info("TEST");
        String path = "D:/AE/python/ocr/paddle/PaddleOCR/train_data/small_images/011002000711_99754305.pdf0.png";
        String im_base64 = getImgBase64(path);

        JSONObject post_params = get_ocr_post_body_py(im_base64, null,null );

        Stopwatch start = Stopwatch.createStarted();
//        OutFeatures outFeatures = new OutFeatures();

        String api_url_tabel_det = "http://l-feedmedia12.c.cn6:9017/algo/ocr/tableDetect";
        String api_url_box_det = "http://l-feedmedia12.c.cn6:9017/algo/ocr/textdet";

        String table_boxes_r = null, det_boxes_r = null;

        try {
//            ListeningExecutorService executorService = MoreExecutors.listeningDecorator(QTracer.wrap(Executors.newCachedThreadPool()));
//            ListeningExecutorService executorService = MoreExecutors.listeningDecorator(QTracer.wrap(Executors.newCachedThreadPool()));

            List<ListenableFuture<Object>> futureList = Lists.newArrayList();

            // 获取订单城市
            ListenableFuture<Object> orderCityFuture = executorService.submit(
                    new Callable<Object>() {
                        @Override
                        public String call() throws Exception {
                            // 最近未入住酒店订单的城市 (dubbo)
//                            HotelRecentOrder hotelRecentOrder = userBehaviorAgent.queryUserHistoryInfo(param);
                            JSONObject table_boxes_result = tabel_det_request(post_params,api_url_tabel_det);
                            if (table_boxes_result != null) {
                                return table_boxes_result.toJSONString();
                            }
                            return "";
                        }
                    }
            );
            futureList.add(orderCityFuture);


            // 获取常驻地城市
            ListenableFuture<Object> residenceCityFuture = executorService.submit(
                    new Callable<Object>() {
                        @Override
                        public String call() throws Exception {
                            // 获取常住地 (http)
                            JSONObject det_boxes_result = bill_det_request(post_params,api_url_box_det);
                            if (det_boxes_result != null) {
                                return det_boxes_result.toJSONString();
                            }
                            return "";
                        }
                    }
            );
            futureList.add(residenceCityFuture);


            // 线程结果聚合
//            String table_boxes_r = null, det_boxes_r = null;

            final ListenableFuture<List<Object>> futureResultCompose = Futures.successfulAsList(futureList);
            List<Object> futureResultList;

            try {
                long threadPoolTimeOut = 50000;
                futureResultList = futureResultCompose.get(threadPoolTimeOut, TimeUnit.MILLISECONDS);  // 等待所有线程完成

                table_boxes_r = (String) futureResultList.get(0);
                det_boxes_r = (String) futureResultList.get(1);

            } catch (InterruptedException e) {
                LOGGER.error("fetchOutFeatures 线程被中断, param:{}, error:", post_params, e);
//                QMonitor.recordOne(WatcherConstant.OUT_FEATURE_INTERRUPTED_ERROR);
            } catch (TimeoutException e) {
                LOGGER.error("fetchOutFeatures 线程执行超时, param:{}, error:", post_params, e);
//                QMonitor.recordOne(WatcherConstant.OUT_FEATURE_TIMEOUT_ERROR);

                // 把没超时的那个取回来
                table_boxes_r = (String)getDoneFuture(futureList, 0);
                det_boxes_r = (String)getDoneFuture(futureList, 1);

            } catch (Exception e) {
                LOGGER.error("fetchOutFeatures 线程执行异常, param:{}, error:", post_params, e);
//                QMonitor.recordOne(WatcherConstant.OUT_FEATURE_ERROR);
            }


        } catch (Exception e) {
            LOGGER.error("fetchOutFeatures error, param:{}, error:", post_params, e);
//            QMonitor.recordOne(WatcherConstant.OUT_FEATURE_ERROR);
        }

        LOGGER.info("table_boxes_r:{}",table_boxes_r);
        LOGGER.info("det_boxes_r:{}",det_boxes_r);




    }

    private static Object getDoneFuture(List<ListenableFuture<Object>> futureList, int idx) {
        Object obj = null;
        try {
            if (futureList.get(idx).isDone()) {
                obj = futureList.get(idx).get();
            }
        } catch (InterruptedException | ExecutionException e2) {
            // ignore
        }
        return obj;
    }



    public static JSONObject tabel_det_request(JSONObject post_params,String api_url_tabel_det){
//        String api_url_tabel_det = "http://l-feedmedia12.c.cn6:9017/algo/ocr/tableDetect";
        JSONObject result_table_boxes = getTableBillOcrPyResult(post_params,api_url_tabel_det);
        return result_table_boxes;
    }

    public static JSONObject bill_det_request(JSONObject post_params,String api_url_box_det){
//        String api_url_box_det = "http://l-feedmedia12.c.cn6:9017/algo/ocr/textdet";
        JSONObject result_det_boxes = getTableBillOcrPyResult(post_params,api_url_box_det);
        return result_det_boxes;
    }


    public static JSONObject get_ocr_post_body_py(String img_base64, JSONArray img_boxes, JSONArray table_boxes) {
        JSONObject paramObj = new JSONObject();
        paramObj.put("app_code", "text_pj");
        paramObj.put("trace_id", "text_pj");
        paramObj.put("img_type", "img_base64");
        paramObj.put("show_time", 1);

        JSONObject one_img_json = new JSONObject();
        one_img_json.put("img_value", img_base64);
        one_img_json.put("img_id", "100000000001");
        one_img_json.put("boxes", img_boxes);
        one_img_json.put("table_boxes", table_boxes);
        one_img_json.put("table_iter", "");
        one_img_json.put("is_full", true);

        JSONArray one_array_json = new JSONArray();
        one_array_json.add(one_img_json);

        paramObj.put("img_info", one_array_json);
        return paramObj;
    }

    public static JSONObject getTableBillOcrPyResult(JSONObject paramObj,String url) {

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Content-Type", "application/json");
//        headerMap.put("Connection", "close");

        JSONObject result = new JSONObject(0);

        Stopwatch httpRequestStart = Stopwatch.createStarted();

        xxxxxxHttpClient httpClient = xxxxxxHttpClient
                .createDefaultClient(50000, 50000, 2000, 2000);
        String postResult = HttpUtils
                .postHttpSync(httpClient, url, paramObj, headerMap);

        long httpEndTime = httpRequestStart.elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("请求 table_bill_ocr_url done , result:{}, time:{}",
                postResult, httpEndTime);
        httpClient.close();
        JSONObject res = JSONObject.parseObject(postResult);

        return res;
    }

    public static String getImgBase64(String imgFile) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理

        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64String(data);
    }

}
