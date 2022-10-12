package com.xxxxxx.hotel.imageanalysis.web.service.ocr;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import com.xxxxxx.hotel.imageanalysis.web.controller.OcrController;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
//import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xxxxxx.tc.qconfig.client.spring.QConfig;
import xxxxxx.tc.qconfig.client.spring.QConfigLogLevel;
import xxxxxx.tc.qtracer.QTracer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


@Service
public class BillOcrWithTableService {

    private static Logger LOGGER = LoggerFactory.getLogger(BillOcrWithTableService.class);

    private static ListeningExecutorService executorService = MoreExecutors.listeningDecorator(QTracer.wrap(Executors.newCachedThreadPool()));

    @Value("${bill_table_det_url}")
    private  String BILL_TABLE_DET_URL;
    @Value("${bill_box_det_url}")
    private  String BILL_BOX_DET_URL;
    @Value("${bill_rec_url}")
    private  String BILL_REC_URL;

    // qconfig 配置的
    private static Integer threadPoolTimeOut = 50000;  // 线程池等待时间

//    @QConfig(value = "http.properties", logLevel = QConfigLogLevel.high)
//    public void qconfigOnChanged(Map<String, String> config) {
//        try {
//            Integer threadPoolTimeOutTemp = Integer.valueOf(config.get("threadPoolTimeOut"));
//            threadPoolTimeOut = threadPoolTimeOutTemp;
//        } catch (NumberFormatException | NullPointerException e) {
//            LOGGER.error("[qconfig] http.properties 配置错误. threadPoolTimeOut 值异常. error=", e);
//            QMonitor.recordOne(WatcherConstant.QCONFIG_LOAD_ERROR);
//            Throwables.propagate(e);
//        }
//    }

    public static void main(String[] args) {
//        String path = "D:/AE/python/ocr/paddle/PaddleOCR/train_data/small_images/011002000711_99754305.pdf0.png";
//        String im_value = getImgBase64(path);

        String img_value = "http://pf-impic-storecp.xxxxxxzz.com/pf_impic_store_mpic/4c63932558593d2b582e51e88c3cb931.pdf";
        String img_id = "http://pf-impic-storecp.xxxxxxzz.com/pf_impic_store_mpic/4c63932558593d2b582e51e88c3cb931.pdf";
        String app_code = "xxxxxxxx";
        String trace_id= "xxxxxxx";
        String api_url_tabel_det= "http://l-feedmedia12.c.cn6:9017/algo/ocr/tableDetect" ;
        String api_url_box_det= "http://l-feedmedia12.c.cn6:9017/algo/ocr/textdet";
        String api_url_bill_rec= "http://l-feedmedia12.c.cn6:9017/algo/ocr/billTextRec";
        String img_type= "img_url";

        JSONObject post_params = get_ocr_post_body_py(img_value, null,null ,app_code,trace_id,img_type,img_id);
        LOGGER.info("tabel_det_url:{},box_det_url:{}rec_url:{},post_params={}",api_url_tabel_det,api_url_box_det,api_url_bill_rec,post_params);


        Stopwatch start = Stopwatch.createStarted();
        String table_boxes_r = null, det_boxes_r = null;

//        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(QTracer.wrap(Executors.newCachedThreadPool()));
        try {
            List<ListenableFuture<Object>> futureList = Lists.newArrayList();
            // 获取订单城市
            ListenableFuture<Object> tableDetFuture = executorService.submit(
                    new Callable<Object>() {
                        @Override
                        public String call() throws Exception {
                            // 最近未入住酒店订单的城市 (dubbo)
                            JSONObject table_boxes_result = tabel_det_request(post_params,null);
                            if (table_boxes_result != null) {
                                return table_boxes_result.toJSONString();
                            }
                            return "";
                        }
                    }
            );
            futureList.add(tableDetFuture);

            // 获取常驻地城市
            ListenableFuture<Object> boxDetFuture = executorService.submit(
                    new Callable<Object>() {
                        @Override
                        public String call() throws Exception {
                            // 获取常住地 (http)
                            JSONObject det_boxes_result = bill_det_request(post_params,null);
                            if (det_boxes_result != null) {
                                return det_boxes_result.toJSONString();
                            }
                            return "";
                        }
                    }
            );
            futureList.add(boxDetFuture);

            final ListenableFuture<List<Object>> futureResultCompose = Futures.successfulAsList(futureList);
            List<Object> futureResultList;

            try {
                long threadPoolTimeOut = 50000;
                futureResultList = futureResultCompose.get(threadPoolTimeOut, TimeUnit.MILLISECONDS);  // 等待所有线程完成

                table_boxes_r = (String) futureResultList.get(0);
                det_boxes_r = (String) futureResultList.get(1);

            } catch (InterruptedException e) {
                LOGGER.error("[table_bill_ocr][trace_id={}] 线程被中断, table_url={},box_url={},rec_url={}, params={}, error:{}",
                        trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
                QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            } catch (TimeoutException e) {
                LOGGER.error("[table_bill_ocr][trace_id={}] 线程执行超时, table_url={},box_url={},rec_url={}, params={}, error:{}",
                        trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
                QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);

                // 把没超时的那个取回来
                table_boxes_r = (String)getDoneFuture(futureList, 0);
                det_boxes_r = (String)getDoneFuture(futureList, 1);

            } catch (Exception e) {
                LOGGER.error("[table_bill_ocr][trace_id={}] 线程执行异常, table_url={},box_url={},rec_url={}, params={}, error:{}",
                        trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
                QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            }


        } catch (Exception e) {
            LOGGER.error("[table_bill_ocr][trace_id={}] error, table_url={},box_url={},rec_url={}, params={}, error:{}",
                    trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
        }

        LOGGER.info("table_boxes_r:{}",table_boxes_r);
        LOGGER.info("det_boxes_r:{}",det_boxes_r);
        JSONObject result_table_boxes = JSONObject.parseObject(table_boxes_r);
        JSONArray table_boxes = result_table_boxes.getJSONArray("result").getJSONObject(0).getJSONArray("table_detect_result");

        JSONObject result_det_boxes = JSONObject.parseObject(det_boxes_r);
        JSONArray det_boxes = result_det_boxes.getJSONArray("result").getJSONObject(0).getJSONArray("text_detect_result");


        JSONObject post_params_rec = get_ocr_post_body_py(img_value, det_boxes, table_boxes, app_code,trace_id,img_type,img_id);
        JSONObject result_bill_rec = getTableBillOcrPyResult(post_params_rec,api_url_bill_rec);
        LOGGER.info("result_bill_rec:{}",result_bill_rec.toJSONString());




    }

    public   JSONObject getBillOcrPyResult(String img_value,
                                                String app_code,String trace_id,String img_type,String img_id,
                                                String api_url_tabel_det,String api_url_box_det,String api_url_bill_rec) {
//        String path = "D:/AE/python/ocr/paddle/PaddleOCR/train_data/small_images/011002000711_99754305.pdf0.png";
//        String im_value = getImgBase64(path);

        JSONObject post_params = get_ocr_post_body_py(img_value, null,null ,app_code,trace_id,img_type,img_id);
//        LOGGER.info("tabel_det_url:{},box_det_url:{}rec_url:{},post_params={}",api_url_tabel_det,api_url_box_det,api_url_bill_rec,post_params);
        LOGGER.info("tabel_det_url:{},box_det_url:{},rec_url:{},post_params={}",BILL_TABLE_DET_URL,BILL_BOX_DET_URL,BILL_REC_URL,post_params);



        Stopwatch start = Stopwatch.createStarted();
        String table_boxes_r = null, det_boxes_r = null;

//        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(QTracer.wrap(Executors.newCachedThreadPool()));
        try {
            List<ListenableFuture<Object>> futureList = Lists.newArrayList();
            // 获取订单城市
            ListenableFuture<Object> tableDetFuture = executorService.submit(
                    new Callable<Object>() {
                        @Override
                        public String call() throws Exception {
                            // 最近未入住酒店订单的城市 (dubbo)
//                            JSONObject table_boxes_result = tabel_det_request(post_params,api_url_tabel_det);
                            JSONObject table_boxes_result = tabel_det_request(post_params,BILL_TABLE_DET_URL);
                            if (table_boxes_result != null) {
                                return table_boxes_result.toJSONString();
                            }
                            return "";
                        }
                    }
            );
            futureList.add(tableDetFuture);

            // 获取常驻地城市
            ListenableFuture<Object> boxDetFuture = executorService.submit(
                    new Callable<Object>() {
                        @Override
                        public String call() throws Exception {
                            // 获取常住地 (http)
//                            JSONObject det_boxes_result = bill_det_request(post_params,api_url_box_det);
                            JSONObject det_boxes_result = bill_det_request(post_params,BILL_BOX_DET_URL);
                            if (det_boxes_result != null) {
                                return det_boxes_result.toJSONString();
                            }
                            return "";
                        }
                    }
            );
            futureList.add(boxDetFuture);

            final ListenableFuture<List<Object>> futureResultCompose = Futures.successfulAsList(futureList);
            List<Object> futureResultList;

            try {
                long threadPoolTimeOut = 50000;
                futureResultList = futureResultCompose.get(threadPoolTimeOut, TimeUnit.MILLISECONDS);  // 等待所有线程完成


                LOGGER.info("get result.");
                table_boxes_r = (String) futureResultList.get(0);
                det_boxes_r = (String) futureResultList.get(1);

            } catch (InterruptedException e) {
                LOGGER.error("[table_bill_ocr][trace_id={}] 线程被中断, table_url={},box_url={},rec_url={}, params={}, error:{}",
                        trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
                QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            } catch (TimeoutException e) {
                LOGGER.error("[table_bill_ocr][trace_id={}] 线程执行超时, table_url={},box_url={},rec_url={}, params={}, error:{}",
                        trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
                QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);

                // 把没超时的那个取回来
                table_boxes_r = (String)getDoneFuture(futureList, 0);
                det_boxes_r = (String)getDoneFuture(futureList, 1);

            } catch (Exception e) {
                LOGGER.error("[table_bill_ocr][trace_id={}] 线程执行异常, table_url={},box_url={},rec_url={}, params={}, error:{}",
                        trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
                QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
            }


        } catch (Exception e) {
            LOGGER.error("[table_bill_ocr][trace_id={}] error, table_url={},box_url={},rec_url={}, params={}, error:{}",
                    trace_id,api_url_tabel_det,api_url_box_det,api_url_bill_rec, post_params, e);
            QMonitor.recordOne(WatcherConstant.BILL_OCR_ERROR_NUM);
        }

        LOGGER.info("table_boxes_r:{}",table_boxes_r);
        LOGGER.info("det_boxes_r:{}",det_boxes_r);
        JSONObject result_table_boxes = JSONObject.parseObject(table_boxes_r);
        JSONArray table_boxes = result_table_boxes.getJSONArray("result").getJSONObject(0).getJSONArray("table_detect_result");

        JSONObject result_det_boxes = JSONObject.parseObject(det_boxes_r);
        JSONArray det_boxes = result_det_boxes.getJSONArray("result").getJSONObject(0).getJSONArray("text_detect_result");


        JSONObject post_params_rec = get_ocr_post_body_py(img_value, det_boxes, table_boxes, app_code,trace_id,img_type,img_id);
//        JSONObject result_bill_rec = getTableBillOcrPyResult(post_params_rec,api_url_bill_rec);
        JSONObject result_bill_rec = getTableBillOcrPyResult(post_params_rec,BILL_REC_URL);
        LOGGER.info("result_bill_rec:{}",result_bill_rec.toJSONString());

        return result_bill_rec;


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


    public static JSONObject get_ocr_post_body_py(String img_value, JSONArray img_boxes, JSONArray table_boxes,
                                                  String app_code,String trace_id,String img_type,String img_id) {
        JSONObject paramObj = new JSONObject();
        paramObj.put("app_code", app_code);
        paramObj.put("trace_id", trace_id);
        paramObj.put("img_type", img_type);
        paramObj.put("show_time", 1);

        JSONObject one_img_json = new JSONObject();
        one_img_json.put("img_value", img_value);
        one_img_json.put("img_id", img_id);
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
