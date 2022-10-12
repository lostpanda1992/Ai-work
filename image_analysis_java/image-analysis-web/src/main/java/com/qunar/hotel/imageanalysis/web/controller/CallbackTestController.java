package com.xxxxxx.hotel.imageanalysis.web.controller;

import com.xxxxxx.hotel.imageanalysis.web.service.qmq.QmqSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xxxxxx.web.spring.annotation.JsonBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/image")
public class CallbackTestController {

    private static Logger LOGGER = LoggerFactory.getLogger(CallbackTestController.class);

    @Resource
    QmqSendService qmqSendService;


    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    @JsonBody
    public String analysis(@RequestBody String jsonParam) {

        LOGGER.info("CallbackTestController  callback test　入口参数:{}", jsonParam);

        return jsonParam;
    }

}

