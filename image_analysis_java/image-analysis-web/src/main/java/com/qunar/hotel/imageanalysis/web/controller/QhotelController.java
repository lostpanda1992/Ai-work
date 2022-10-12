package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.xxxxxx.base.meerkat.http.xxxxxxHttpClient;
import com.xxxxxx.hotel.imageanalysis.web.utils.HttpUtils;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/qhotel")
public class QhotelController {


  @GetMapping("/img")
  @ResponseBody
  public Object scenario(
      @RequestParam(value = "id", required = true) String id
  ) throws Exception {
    return analysis(id);
  }


  public String analysis(String id) {

    xxxxxxHttpClient httpClient = xxxxxxHttpClient.createDefaultClient(10000, 10000, 2000, 500);
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

    return imgs.toString();
  }


}
