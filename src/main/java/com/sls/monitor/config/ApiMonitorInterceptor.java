package com.sls.monitor.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sls.monitor.annotation.ApiMonitor;
import com.sls.monitor.dto.SlsLog;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/16 15:16
 **/

public class ApiMonitorInterceptor implements MethodInterceptor {
    private final AliyunSlsProducer aliyunSlsProducer;
    private final HttpServletRequest httpServletRequest;

    public ApiMonitorInterceptor(AliyunSlsProducer aliyunSlsProducer, HttpServletRequest request) {
        this.aliyunSlsProducer = aliyunSlsProducer;
        this.httpServletRequest = request;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        ApiMonitor apiMonitor = AnnotatedElementUtils.findMergedAnnotation(method, ApiMonitor.class);
        if (apiMonitor == null) {
            return invocation.proceed();
        }
        try {
            Object result = invocation.proceed();
            try {
                JSONObject resultJson = JSON.parseObject(JSON.toJSONString(result));
                aliyunSlsProducer.log(invocation.getMethod(), new SlsLog().setLevel("200".equals(resultJson.getString("errCode")) ? "INFO" : "ERROR")
                        .setUrl(httpServletRequest.getRequestURI())
                        .setCode(resultJson.getString("errCode"))
                        .setData(JSON.toJSONString(resultJson.get("data")))
                        .setDescription(apiMonitor.description())
                        .setMessage(resultJson.getString("errMsg")));
            } catch (Exception e) {
                //don't do anything
            }
            return result;
        } catch (Throwable e) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(128);
                 PrintStream printStream = new PrintStream(out, true, "UTF-8")) {
                e.printStackTrace(printStream);
                aliyunSlsProducer.log(invocation.getMethod(), new SlsLog().setLevel("ERROR")
                        .setUrl(httpServletRequest.getRequestURI())
                        .setCode("500")
                        .setDescription(apiMonitor.description())
                        .setMessage("exception"),e);
            } catch (Exception ex) {
                //don't do anything
            }
            throw e;
        }
    }
}
