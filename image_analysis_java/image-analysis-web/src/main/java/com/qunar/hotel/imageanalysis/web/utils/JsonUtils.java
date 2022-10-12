package com.xxxxxx.hotel.imageanalysis.web.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author: hengyu.dai
 * @date: 2019/7/19
 */
public class JsonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.getFactory().enable(JsonFactory.Feature.INTERN_FIELD_NAMES);
        objectMapper.getFactory().enable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getObjectMapperInstance() {
        return objectMapper;
    }

    /**
     * 将对象序列化为Json字符串。Object可以是POJO，也可以是Collection或数组。
     * 该方法由调用方负责处理异常
     */
    public static String fromBean(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 将对象序列化为Json字符串。Object可以是POJO，也可以是Collection或数组。 如果对象为Null, 返回"null". 如果集合为空集合, 返回"[]".
     */
    public static String toJson(Object object) {
        try {
            if(object instanceof String){
                return (String) object;
            }
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            LOGGER.error("write to json string error:{}", object, e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * 反序列化POJO或简单Collection或Map.如List<String>, List<Object>, Map<String, String>, Map<String, Object>
     * 该方法由调用方负责处理异常
     */
    public static <T> T toBean(String jsonString, Class<T> clazz) throws IOException {
        return objectMapper.readValue(jsonString, clazz);
    }

    /**
     * 反序列化，出错时抛runtime exception
     *
     * @param jsonString
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toBeanQuietly(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new RuntimeException("deserialize error", e);
        }
    }

    /**
     * 反序列化复杂类型，如List<Bean>, Map<String, Bean>, Map<String, Map<String, Bean>>.
     * 利用buildType、buildParametricType、buildCollectionType、buildMapType等方法来构造JavaType, 然后调用本方法。
     * 该方法由调用方负责处理异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T toBean(String jsonString, JavaType javaType) throws IOException {
        return (T) objectMapper.readValue(jsonString, javaType);
    }

    public static <T> T toBeanQuietly(String jsonString, JavaType javaType) {
        try {
            return (T) objectMapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            throw new RuntimeException("deserialize error", e);
        }
    }

    /**
     * 反序列化POJO或简单Collection或Map.如List<String>, List<Object>, Map<String, String>, Map<String, Object>
     *
     * 如果JSON字符串为Null或"null"字符串, 返回Null. 如果JSON字符串为"[]", 返回空集合.
     *
     * 如需反序列化复杂Collection如List<MyBean>, 请使用fromJson(String, JavaType)
     *
     * @see #fromJson(String, JavaType)
     */
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            LOGGER.error("parse json string error:{}", jsonString, e);
            return null;
        }
    }

    /**
     * 反序列化复杂类型，如List<Bean>, Map<String, Bean>, Map<String, Map<String, Bean>>.
     * 利用buildType、buildParametricType、buildCollectionType、buildMapType等方法来构造类型, 然后调用本方法。
     */
    @SuppressWarnings("unchecked")
    public static  <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return (T) objectMapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            LOGGER.error("parse json string error:{}", jsonString, e);
            return null;
        }
    }

    /**
     * 构造类型，例如: JavaType stringType = JsonUtils.buildType(String.class)
     */
    public static JavaType buildType(Type type) {
        return objectMapper.constructType(type);
    }

    /**
     * 构造参数类型，例如：要反序列化成ApiResult<MemberLevel>类型的对象，示例代码如下：
     * ApiResult<MemberLevel> result = JsonUtils.toBean(jsonString, JsonUtils.buildParametricType(ApiResult.class, MemberLevel.class));
     */
    public static JavaType buildParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        return objectMapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    public static JavaType buildParametricType(Class<?> parametrized, JavaType... parameterTypes) {
        return objectMapper.getTypeFactory().constructParametricType(parametrized, parameterTypes);
    }

    /**
     * 构造Collection类型.
     */
    public static JavaType buildCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return objectMapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    public static JavaType buildCollectionType(Class<? extends Collection> collectionClass, JavaType elementType) {
        return objectMapper.getTypeFactory().constructCollectionType(collectionClass, elementType);
    }

    /**
     * 构造Map类型.
     */
    public static JavaType buildMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return objectMapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    public static JavaType buildMapType(Class<? extends Map> mapClass, JavaType keyType, JavaType valueType) {
        return objectMapper.getTypeFactory().constructMapType(mapClass, keyType, valueType);
    }

    /**
     * 判断给定的字符串是不是JSON字符串格式
     */
    public static boolean isJsonString(String content) {
        try {
            JSONObject.parseObject(content);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * jsonOjbect对象限制value了大长度，以解决打日志的时候toString的时候value太长的问题
     * @param srcJsonObject 原始jsonObject
     * @param maxValueLength 限制的value最大长度
     * @return
     */
    public static JSONObject jsonObjLimitMaxVal(JSONObject srcJsonObject,  int maxValueLength) {
        JSONObject newJsonObject = new JSONObject();
        for (String key : srcJsonObject.keySet()) {
            Object value = srcJsonObject.get(key);
            if (value != null) {
                if (value.getClass().toString().endsWith("JSONObject")) {
                    newJsonObject.put(key, jsonObjLimitMaxVal((JSONObject) value, maxValueLength));
                } else if (value.getClass().toString().endsWith("JSONArray")) {
                    newJsonObject.put(key, jsonArrLimitMaxVal((JSONArray) value, maxValueLength));
                } else {
                    String valueStr = value.toString();
                    if (valueStr.length() > maxValueLength) {
                        newJsonObject.put(key, valueStr.substring(0, maxValueLength));
                    } else {
                        newJsonObject.put(key, valueStr);
                    }
                }
            }

        }
        return newJsonObject;
    }

    private static JSONArray jsonArrLimitMaxVal(JSONArray srcJSONArray, int maxValueLength) {
        JSONArray newJSONArray = new JSONArray();
        for (int i = 0; i < srcJSONArray.size(); i++) {
            Object obj = srcJSONArray.get(i);
            if (obj.getClass().toString().endsWith("JSONObject")) {
                newJSONArray.add(jsonObjLimitMaxVal((JSONObject) obj, maxValueLength));
            } else if (obj.getClass().toString().endsWith("JSONArray")) {
                newJSONArray.add(jsonArrLimitMaxVal((JSONArray) obj, maxValueLength));
            }
        }
        return newJSONArray;
    }



    public static void main(String[] args) {
        JSONObject obj = new JSONObject();
        obj.put("a", "aaaaaaaaaaaaaa");

        JSONArray ja = new JSONArray();

        JSONObject obj2 = new JSONObject();
        obj2.put("b", "bbbbbbbbbbbbbbbbbb");
        ja.add(obj2);

        JSONObject obj3 = new JSONObject();
        obj3.put("c", "cccccccccccccccccccccccccc");
        ja.add(obj3);
        obj.put("array", ja);

        JSONArray j2 = new JSONArray();
        obj.put("j2", j2);
        j2.add(obj2);
        j2.add(obj3);

        ja.add(j2);


        System.out.println(obj);
        System.out.println(jsonObjLimitMaxVal(obj, 2));
    }
}
