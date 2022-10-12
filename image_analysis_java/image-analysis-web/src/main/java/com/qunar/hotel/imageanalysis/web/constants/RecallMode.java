package com.xxxxxx.hotel.feedsearch.web.constants;

public enum RecallMode {

    COMMON_RECALL("通用 召回"),

    CITY_RECALL("city 召回"),
    POI_RECALL("poi 召回"),
    LABEL_RECALL("label 召回"),
    TITLE_RECALL("title 召回"),
    CATEGORY_RECALL("category 召回"),
    HOTPOI_RECALL("hotpoi 召回"),
    CONTENT_RECALL("content 召回"),


    FUZZY_POI_RECALL("poi 模糊 召回"),
    FUZZY_TITLE_RECALL("title 模糊 召回"),


    CITY_CATEGORY_RECALL("city_category 召回"),
    POI_CATEGORY_RECALL("poi_category 召回"),
    LABEL_CATEGORY_RECALL("label_category 召回"),
    TITLE_CATEGORY_RECALL("title_category 召回"),

    POI_LABEL_TITLE_CATEGORY_RECALL("poi_label_title_category 召回"),

    CITY_POI_CATEGORY_RECALL("city_poi_category 召回"),
    CITY_TITLE_CATEGORY_RECALL("city_title_category 召回"),
    CITY_LABEL_CATEGORY_RECALL("city_label_category 召回"),
    POI_LABEL_CATEGORY_RECALL("poi_label_category 召回"),
    POI_TITLE_CATEGORY_RECALL("poi_title_category 召回"),
    LABEL_TITLE_CATEGORY_RECALL("label_title_category 召回"),

    CITY_POI_LABEL_CATEGORY_RECALL("city_poi_label_category 召回"),
    CITY_POI_TITLE_CATEGORY_RECALL("city_poi_title_category 召回"),

    CITY_LABEL_TITLE_CATEGORY_RECALL("city_label_title_category 召回"),
    CITY_POI_LABEL_TITLE_CATEGORY_RECALL("city_poi_label_category 召回"),

    TITLE_SCENARIO_STRATEGY_RECALL("title scenario strategy 召回"),
    TITLE_SCENARIO_HOTEL_RECALL("title scenario hotel 召回"),
    TITLE_SCENARIO_SIGHT_RECALL("title scenario sight 召回"),
    TITLE_SCENARIO_FOOD_RECALL("title scenario food 召回"),
    TITLE_SCENARIO_OTHER_RECALL("title scenario other 召回"),
    ;


    private String desc;

    RecallMode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    // 日志收集依赖这个格式,请使用json样式
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"desc\":")
                .append(desc).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
