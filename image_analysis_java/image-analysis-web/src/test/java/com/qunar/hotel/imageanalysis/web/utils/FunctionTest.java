package com.xxxxxx.hotel.feedsearch.web.utils;

import com.google.common.base.Splitter;
import org.junit.Test;

import java.util.List;

/**
 * Pack:       com.xxxxxx.hotel.hotelfeedstream.web.utils
 * File:       FunctionTest
 * Desc:
 * User:       chuangfeng.wang
 * CreateTime: 2019-10-10 10:24
 */
public class FunctionTest {

    @Test
    public void stringSplit() throws Exception {
        String str = "";
        List<String> idList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
        System.out.println(idList);
    }

}
