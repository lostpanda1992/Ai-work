package com.xxxxxx.hotel.feedsearch.web.service.abtest;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xxxxxx.abtest.ABSlot;
import com.xxxxxx.abtest.ABTests;
import com.xxxxxx.datateam.abtest.abclient4j.ABTestHelper;
import com.xxxxxx.flight.qmonitor.QMonitor;
import com.xxxxxx.hotel.imageanalysis.web.constants.WatcherConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xxxxxx.agile.Conf;
import xxxxxx.tc.qconfig.client.spring.QConfig;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
public class ABManage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABManage.class);
    private static final Gson gson = new Gson();



    // qconfig 控制的
    private String expIds = "";                         // 实验id
    private Map<String, String> whiteMap = Maps.newConcurrentMap();       // 在qconfig中配置的白名单. uid->slotId
    private String defaultSlotId = "default";                             // 默认分桶


    @QConfig("abtest.properties")
    public void onChanged(Map<String, String> map) {
        try {

            Conf conf = Conf.fromMap(map);

            Set<String> aSlotClients;       // Qconfig中配置指定进入A桶的uid
            Set<String> bSlotClients;       // Qconfig中配置指定进入B桶的uid
            Set<String> cSlotClients;       // Qconfig中配置指定进入C桶的uid
            Set<String> dSlotClients;       // Qconfig中配置指定进入D桶的uid
            Set<String> eSlotClients;       // Qconfig中配置指定进入E桶的uid
            Set<String> fSlotClients;       // Qconfig中配置指定进入F桶的uid
            Set<String> gSlotClients;       // Qconfig中配置指定进入G桶的uid

            // 实验白名单, 配置一些uid进入特定的桶
            String aSlot = conf.getString("A", "[]");
            aSlotClients = gson.fromJson(aSlot, new TypeToken<Set<String>>() {
            }.getType());
            for (String uid : aSlotClients) {
                whiteMap.put(uid, "A");
            }

            String bSlot = conf.getString("B", "[]");
            bSlotClients = gson.fromJson(bSlot, new TypeToken<Set<String>>() {
            }.getType());
            for (String uid : bSlotClients) {
                whiteMap.put(uid, "B");
            }

            String cSlot = conf.getString("C", "[]");
            cSlotClients = gson.fromJson(cSlot, new TypeToken<Set<String>>() {
            }.getType());
            for (String uid : cSlotClients) {
                whiteMap.put(uid, "C");
            }

            String dSlot = conf.getString("D", "[]");
            dSlotClients = gson.fromJson(dSlot, new TypeToken<Set<String>>() {
            }.getType());
            for (String uid : dSlotClients) {
                whiteMap.put(uid, "D");
            }

            String eSlot = conf.getString("E", "[]");
            eSlotClients = gson.fromJson(eSlot, new TypeToken<Set<String>>() {
            }.getType());
            for (String uid : eSlotClients) {
                whiteMap.put(uid, "E");
            }

            String fSlot = conf.getString("F", "[]");
            fSlotClients = gson.fromJson(fSlot, new TypeToken<Set<String>>() {
            }.getType());
            for (String uid : fSlotClients) {
                whiteMap.put(uid, "F");
            }

            String gSlot = conf.getString("G", "[]");
            gSlotClients = gson.fromJson(gSlot, new TypeToken<Set<String>>() {
            }.getType());
            for (String uid : gSlotClients) {
                whiteMap.put(uid, "G");
            }


            expIds = conf.getString("expId", "");
            defaultSlotId = conf.getString("defaultSlotId", "default");

            LOGGER.info("ABtest white map: {}", JSON.toJSONString(whiteMap));
            LOGGER.info("expId: {}", expIds);
        } catch (Exception e) {
            LOGGER.error("[qconfig] abtest.properties 配置错误. 值异常. error=", e);
            QMonitor.recordOne(WatcherConstant.QCONFIG_LOAD_ERROR);
            Throwables.propagate(e);
        }
    }

    /**
     * 在创建 ABService 对象之前，初始化ABTest实验环境
     */
    @PostConstruct
    private void init() {
        //主动初始化实验配置，这个方法在服务启动时调用一次即可。如果不调用该方法，配置信息会在第一次请求分流1min后初始化完成，在此之前获取不到分流结果。
        ABTestHelper.cachePreload(ImmutableList.of(expIds));
        LOGGER.info("Init ABTest:  expIds:{}", expIds);
    }


    /**
     * 根据客户端信息，进行分桶
     *
     * @param expId 使用的分桶实验编号
     * @return 分桶的结果
     */
    private String getSlotId(String expId, String abBy) {
        if (StringUtils.isEmpty(abBy) || StringUtils.isEmpty(expId)) {
            return "";
        }

        ABSlot slot= ABTests.ab(expId,abBy);
        if(slot.isInExp()){//为真表明请求在实验中，slot.isInExp()为假，代码外层一定要加上默认版本逻辑兜底，保证项目健壮
            return slot.getSlotId();
        } else {
            return "";
        }

    }


}
