package com.sls.monitor.config;

import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/16 15:11
 **/

public class ApiMonitorAnnotationBeanPostProcessor extends AbstractAdvisingBeanPostProcessor {
    public ApiMonitorAnnotationBeanPostProcessor(AliyunSlsProducer aliyunSlsProducer, HttpServletRequest request) {
        this.advisor = new ApiMonitorAnnotationAdvisor(aliyunSlsProducer, request);
    }
}
