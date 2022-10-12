package com.xxxxxx.hotel.imageanalysis.web.utils;

/**
 * 浮点类型的特征处理的工具类
 */
public class FloatFeatureUtils {
    // 浮点精度
    private static final double EPSILON = 0.00001;

    /**
     * 比较两个浮点数是否相等，如果差异<EPSILON，则认为相等
     *
     * @param a 浮点数1
     * @param b 浮点数2
     * @return 是否相等
     */
    public static boolean doubleEquals(double a, double b) {
        if (Math.abs(a - b) < EPSILON) {
            return true;
        }

        return false;
    }
}
