package com.xxxxxx.hotel.imageanalysis.web.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class ImageParamsBuildUtils {
    public static JSONObject getImageParams(JSONArray serviceArray, String imgType, JSONObject paramObj, JSONArray params){
        for(int i =0; i< serviceArray.size();i++){
            JSONObject jsonHotScoreImageInfo = serviceArray.getJSONObject(i);
            String serviceName = jsonHotScoreImageInfo.getString("service_name");
            JSONArray jsonHotScoreImageParams = jsonHotScoreImageInfo.getJSONArray("params");

            for(int j =0; j< jsonHotScoreImageParams.size();j++){
                JSONObject singleParams = new JSONObject();
                JSONObject jsonSingleHotScoreImageInfo = jsonHotScoreImageParams.getJSONObject(i);
                String imageId = jsonSingleHotScoreImageInfo.getString("image_id");
                String imageValue = jsonSingleHotScoreImageInfo.getString("image_value");
                singleParams.put("image_id", imageId);
                singleParams.put("img_type", imgType);
                singleParams.put("image_value", imageValue);
                singleParams.put("service_name", serviceName);
                params.add(singleParams);
            }
            paramObj.put("params", params);
        }
        return paramObj;
    }
}
