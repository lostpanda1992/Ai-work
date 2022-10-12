package com.xxxxxx.hotel.imageanalysis.web.utils;

import java.util.UUID;

/**
 * Pack:       com.xxxxxx.hotel.hotelfeedstream.web.utils
 * File:       UUIDUtil
 * Desc:
 * User:       chuangfeng.wang
 * CreateTime: 2019-07-16 16:23
 */
public class UUIDUtil {
    /**
     * 得到一个 uuid, -分隔各段, 字母为小写
     * @return 格式(长度36): 4b5b64cb-e428-412b-af7d-0912ea944f8d
     */
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
