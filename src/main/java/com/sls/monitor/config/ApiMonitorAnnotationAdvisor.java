package com.sls.monitor.config;

import org.aopalliance.aop.Advice;
import com.sls.monitor.annotation.ApiMonitor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/16 15:12
 **/

public class ApiMonitorAnnotationAdvisor extends AbstractPointcutAdvisor {

    private final Pointcut pointcut;
    private final Advice advice;

    public ApiMonitorAnnotationAdvisor(AliyunSlsProducer aliyunSlsProducer, HttpServletRequest request) {
        this.advice = new ApiMonitorInterceptor(aliyunSlsProducer, request);
        this.pointcut = new ComposablePointcut(new AnnotationMatchingPointcut(null, ApiMonitor.class, true));
    }


    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }
}
