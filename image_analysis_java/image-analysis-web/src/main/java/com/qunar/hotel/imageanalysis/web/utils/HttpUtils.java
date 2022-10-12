package com.xxxxxx.hotel.imageanalysis.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.base.meerkat.http.data.PostParameter;
import com.xxxxxx.base.meerkat.http.data.xxxxxxHttpGet;
import com.xxxxxx.base.meerkat.http.data.xxxxxxHttpPost;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;


public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    // 同步get请求
    public static String getHttpSync(final xxxxxxHttpClient clientSync, String url, Map<String, Object> paramsMap) {
        String fullUrl = createUrl(url, paramsMap);
        String result = null;
        try {
            result = clientSync.httpGet(fullUrl);
        } catch (IOException e) {
            LOGGER.error("Http request error, url:{}, e:{}", url, e);
            Throwables.propagate(e);
        }
        return result;
    }

    // 同步post请求
    public static String postHttpSync(final xxxxxxHttpClient clientSync, String url, Map<String, Object> paramsMap) {
        String fullUrl = url;
        String result = null;
        try {
            PostParameter postParameter = new PostParameter();
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                String valueJson = JSON.toJSONString(entry.getValue());
                if (JsonUtils.isJsonString(valueJson)) {
                    postParameter.put(entry.getKey(), valueJson);
                } else {
                    postParameter.put(entry.getKey(), entry.getValue().toString());
                }
            }
            result = clientSync.httpPost(fullUrl, postParameter);

        } catch (IOException e) {
            LOGGER.error("Http request error, url:{}, e:{}", url, e);
            Throwables.propagate(e);
        }

        return result;
    }

    // 同步post请求
    public static String postHttpSync(final xxxxxxHttpClient httpClient, String url, JSONObject paramObj, Map<String, String > hearderMap) {
        String fullUrl = url;
        String result = null;
        try {

            xxxxxxHttpPost post = new xxxxxxHttpPost(url);
            for (Map.Entry<String, String> entry : hearderMap.entrySet()) {
                post.setHeader(entry.getKey(), entry.getValue());
            }
            post.setEntity(new StringEntity(paramObj.toJSONString(), "UTF-8"));
            com.xxxxxx.base.meerkat.http.data.HttpResult httpRequest = httpClient.httpExecute(post);
            result = httpRequest.getContent();
        } catch (IOException e) {
            LOGGER.error("Http request error, url:{}, e:{}", url, e);
            Throwables.propagate(e);
        }

        return result;
    }

    // 同步get请求
    public static String getHttp(final xxxxxxHttpClient httpClient, String url, Map<String, String > hearderMap) {
        String fullUrl = url;
        String result = null;
        try {

            xxxxxxHttpGet get = new xxxxxxHttpGet(url);
            for (Map.Entry<String, String> entry : hearderMap.entrySet()) {
                get.setHeader(entry.getKey(), entry.getValue());
            }
            com.xxxxxx.base.meerkat.http.data.HttpResult httpRequest = httpClient.httpExecute(get);
            result = httpRequest.getContent();
        } catch (IOException e) {
            LOGGER.error("Http request getHttpSync error, url:{}, e:{}", url, e);
            Throwables.propagate(e);
        }

        return result;
    }


    // 同步get请求
    public static String getHttpSync(final xxxxxxHttpClient clientSync, String url, Object para) {
        Map<String, Object> paramsMap = objectToMap(para);
        return getHttpSync(clientSync, url, paramsMap);
    }

    // 同步post请求
    public static String postHttpSync(final xxxxxxHttpClient clientSync, String url, Object para) {
        Map<String, Object> paramsMap = objectToMap(para);
        return postHttpSync(clientSync, url, paramsMap);
    }

    private static Map<String, Object> objectToMap(Object para) {
        String jsonString = JSON.toJSONString(para);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Map<String, Object> paramsMap = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            paramsMap.put(entry.getKey(), entry.getValue());
        }
        return paramsMap;
    }

    /**
     * 自动拼接url和参数值
     *
     * @param url
     * @param paramsMap
     * @return
     */
    private static String createUrl(String url, Map<String, Object> paramsMap) {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url必填");
        }

        if (paramsMap == null || paramsMap.size() <= 0) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        if (!url.endsWith("?")) {
            sb.append("?");
        }

        try {
            for (String key : paramsMap.keySet()) {
                sb.append(key).append("=");
                Object value = paramsMap.get(key);
                if (value != null) {
                    sb.append(URLEncoder.encode(value.toString(), "UTF-8"));
                }
                sb.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("[url编码异常]. {}={}", "error", e);
        }

        String fullUrl = sb.toString();
        return fullUrl.substring(0, fullUrl.length() - 1);
    }

    public static void main(String[] args) {
        xxxxxxHttpClient httpClient = xxxxxxHttpClient.createDefaultClient(10000, 10000, 2000, 500);

        String id = "649550342";
        String url = "http://reviewaudit.corp.xxxxxx.com/comment/search?searchType=cid&keyword=" + id
                + "&gradeLevel=all&commentType=all&auditStatus=all&source=all&modStartDate=2021-03-01&modEndDate=2021-07-30&orderBy=modtime&order=desc&pageNum=1&pageSize=10&platform=1&_time=1627616111591";

        Map<String, String> hearderMap = Maps.newHashMap();
        hearderMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
        hearderMap.put("Accept-Encoding", "gzip, deflate");
        hearderMap.put("Accept-Language", "zh-CN,zh;q=0.9");
        hearderMap.put("Connection", "keep-alive");
        hearderMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        hearderMap.put("Cookie",
                "QN601=c398eec895ed2686b29d4d15aa9dec8c; QN99=8529; QN300=organic; xxxxxxGlobal=10.86.213.150_47afbcc5_16c8636ec39_-87c|1565620530725; _i=RBTKSocwSHTVc-8x6C8wlVC4uvMx; QN48=20191316-9b98-4038-99dc-79778b99033d; fid=4112dc47-2c7d-4232-bcc5-b1c5399587b1; _bfa=1.1566811760486.22elnx.1.1567134236765.1567407968059.5.17; _ga=GA1.2.352939633.1565620534; _RSG=Gfq952c2VQ8gUzvr7gemZB; _RDG=28e9dfbfb571e22075288c4216ec00198d; _RGUID=869f0a60-8527-4a50-b8f0-a2a1f5e7d7aa; __utma=183398822.352939633.1565620534.1573695481.1577946108.12; _jzqa=1.3854466050938921000.1565620535.1573695484.1577946109.12; SC1=aa644e1f3768e141f1b21c5f8b5b69f2; SC18=; _RF1=183.84.2.208; un=01b4ef1e998d877460843eea0b9a73a41598267710708; new_dubai_user=\"2|1:0|10:1615880762|14:new_dubai_user|216:eyJqb2JfY29kZSI6ICJSRCIsICJxX2RvbWFpbl9uYW1lIjogInpoYW5neXV6eXkuemhhbmciLCAicV9hY2NvdW50X25hbWUiOiAi5byg5a6Henl5IiwgIm1hbmFnZXJfdXNlcm5hbWUiOiAieml5YW4uamlhIiwgIm1hbmFnZXIiOiAi6LS+6Ieq6ImzIiwgImlzX2RiYSI6IGZhbHNlfQ==|e31aa73b848bb3222b3b22d09abfb9d964e6b315ef8acb4293231c33866ec5db\"; QN169=6c01f82e-43cf-44c1-b328-57de0e168804-9f992f90; qoa-ret=https%3A//oa.corp.xxxxxx.com/webapp/index.html%23/cooperate/detail/8adad8d879e454ee017a136923502fa2; csrfToken=gSJB2bTyI1WqMnNiwbEymD5amZN71yui; QN271=f9ae9e32-39e5-44ba-944f-190271767784; QN1=00003a002834349ff018fbbd; l-pswebapp1-8000-PORTAL-PSJSESSIONID=O9yKbUGHiMhCwDROoDsh6aax6qvmKmbj!-177876636; console_record=2|1:0|10:1626093531|14:console_record|92:W3siaXBfcG9ydCI6ICIxOTIuMTY4LjIyNC4yMDdfMzMwOSIsICJwYXNzd29yZCI6ICJvWTdCcU4wdXo2aUtzUHBPIn1d|8835fa0982ccf2cd1574213a46be94175baf7d29756acde37d6315deaf710cd1; _audit_q=\"zhangyuzyy.zhang@xxxxxxman\"; _audit_v=G7GnzdDNXs1qndLP46lEytRC449fF0SmQt2XYbppqK5boXNVYtnUMVEdByZxdGADFw; HN1=v1688530a33e1329822328fc0277debdf0; HN2=qusgnkzqlqzsg; QN267=06397299662c980722; QN269=F723FBC3BD9811E9A454FA163E7BCC04; _vi=HbZTj-1g7ZSNyBSNKPT83Ow3pPyQ5tTGEA0qPKXMUQZYEgbYy0D0NIMpDTyOkeVuLQdDjYg-lYqeQxb-Il0FB8yb4iPKDTEFTsDC0d4QIDJ_G0ndNB2sWDhgrtFUbO-WCUJZh3AHuC6rdDYVEFGUl9UbEW1HtV1pziF71yUzj-Rt; JSESSIONID=67266B3560C68A5EBAB3BD53872D4069");
        hearderMap.put("Host", "reviewaudit.corp.xxxxxx.com");
        hearderMap.put("Referer", "http://reviewaudit.corp.xxxxxx.com/pages/auditcomment/commentList");
        hearderMap.put("User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.75 Safari/537.36");
        hearderMap.put("X-Requested-With", "XMLHttpRequest");

        String result = HttpUtils.getHttp(httpClient, url, hearderMap);
        JSONObject jsonObject = JSONObject.parseObject(result);
        Object id_trans = jsonObject.getJSONArray("data").getJSONObject(0).get("id");

        String url2 =
                "http://reviewaudit.corp.xxxxxx.com/comment/search/queryDetail?id=" + id_trans.toString()
                        + "&_time=1627611021703";
        String result2 = HttpUtils.getHttp(httpClient, url2, hearderMap);

        JSONObject jsonObject2 = JSONObject.parseObject(result2);
        Object imgs = jsonObject2.getJSONObject("data").getJSONObject("imageInfo").get("images");
        System.out.println(imgs);



//        Map<String, Object> dataParam = Maps.newHashMap();
//        // dataParam.put("b", "{\"city\":\"北京\",\"input\":\"故宫\"}");
//        dataParam.put("b", "{\"input\":\"香山附近网红打卡景点 \"}");
//        dataParam.put("c", "{\"uid\":\"00000002623880C3C3C3\",\"vid\":\"60001200\",\"gid\":\"00000002623880C3C3C3\",\"pid\":10010,\"catom\":\"com.mxxxxxx.atom.voice_42\"}");
//        String  resp = null;
//        try {
//            resp = HttpUtils.postHttpSync(httpClient, "http://l-suggestion2.wap.cn5.xxxxxx.com:8080/suggestion/queryanalyze", dataParam);
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(resp);
    }
}