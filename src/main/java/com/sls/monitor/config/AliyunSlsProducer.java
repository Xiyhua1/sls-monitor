package com.sls.monitor.config;

import ch.qos.logback.classic.spi.CallerData;
import cn.hutool.core.date.DateUtil;
import com.aliyun.openservices.aliyun.log.producer.*;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogItem;
import com.google.common.util.concurrent.ListenableFuture;
import com.sls.monitor.dto.SlsLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.context.Lifecycle;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/15 16:37
 **/

@Slf4j
public class AliyunSlsProducer implements Lifecycle {

    private final Producer producer;
    private final AtomicBoolean isAlive;

    private final String project;

    private final String logStore;

    private final String appName;

    public AliyunSlsProducer(ProjectConfig projectConfig, String logStore, String appName) {
        this.producer = new LogProducer(new ProducerConfig());
        this.producer.putProjectConfig(projectConfig);
        this.isAlive = new AtomicBoolean(true);
        this.project = projectConfig.getProject();
        this.logStore = logStore;
        this.appName = appName;
    }

    public ListenableFuture<Result> log(SlsLog slsLog) throws ProducerException, InterruptedException {
        return log(slsLog,null);
    }

    public ListenableFuture<Result> log(SlsLog slsLog, Throwable e) throws ProducerException, InterruptedException {
        return log(null, slsLog, e);
    }

    ListenableFuture<Result> log(Method method, SlsLog slsLog) throws ProducerException, InterruptedException {
        return log(method, slsLog, null);
    }

    ListenableFuture<Result> log(Method method, SlsLog slsLog, Throwable e) throws ProducerException, InterruptedException {
        return this.producer.send(project, logStore, null, null, toLogItem(slsLog, method, e));
    }

    @Override
    public void start() {
        log.debug("Aliyun sls producer start success.");
    }

    @Override
    public void stop() {
        try {
            this.producer.close();
            log.debug("Aliyun sls producer stopped.");
        } catch (InterruptedException | ProducerException e) {
            throw new RuntimeException("close aliyun sls producer failed:", e);
        } finally {
            this.isAlive.set(false);
        }
    }

    public LogItem toLogItem(SlsLog slsLog, Method method, Throwable throwable) {
        LogItem logItem = new LogItem();
        fillSlsLogItem(logItem, slsLog);
        fillClassLogItem(logItem, method, throwable);
        fillBaseInfo(logItem);
        if (throwable != null) {
            fillExceptionLogItem(logItem, throwable);
        }
        return logItem;
    }

    private void fillSlsLogItem(LogItem logItem, SlsLog slsLog) {
        logItem.PushBack("level", slsLog.getLevel());
        if (slsLog.getData() != null) {
            logItem.PushBack("data", slsLog.getData());
        }
        logItem.PushBack("message", slsLog.getMessage());
        logItem.PushBack("description", slsLog.getDescription());
        logItem.PushBack("code", slsLog.getCode());
        logItem.PushBack("url", slsLog.getUrl());
    }


    private void fillBaseInfo(LogItem logItem) {
        logItem.PushBack("appName", this.appName);
        logItem.PushBack("time", DateUtil.formatDateTime(new Date()));
        logItem.PushBack("traceId", TraceContext.traceId());
        logItem.PushBack("thread", Thread.currentThread().getName());
    }

    private void fillClassLogItem(LogItem logItem, Method method, Throwable throwable) {
        if (method != null) {
            logItem.PushBack("class", method.getDeclaringClass().getName());
            logItem.PushBack("methodName", method.getName());
            logItem.PushBack("line", getTargetMethodLineNumber(method, throwable));
        } else {
            StackTraceElement[] extract = throwable == null ? CallerData.extract(new Throwable(), this.getClass().getName(), 8, new ArrayList<>()) : throwable.getStackTrace();
            logItem.PushBack("class", extract[0].getClassName());
            String methodName = extract[0].getMethodName();
            if (methodName.contains("$")) {
                logItem.PushBack("methodName", methodName.split("\\$")[0]);
            } else {
                logItem.PushBack("methodName", methodName);
            }
            logItem.PushBack("line", String.valueOf(extract[0].getLineNumber()));
        }
    }


    private void fillExceptionLogItem(LogItem logItem, Throwable throwable) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(128);
             PrintStream printStream = new PrintStream(out, true, "UTF-8")) {
            throwable.printStackTrace(printStream);
            logItem.PushBack("exception", out.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTargetMethodLineNumber(Method method, Throwable throwable) {
        StackTraceElement[] stackTrace = throwable == null ? new Throwable().getStackTrace() : throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().startsWith(method.getDeclaringClass().getName())) {
                return String.valueOf(stackTraceElement.getLineNumber());
            }
        }
        return "-1";
    }

    @Override
    public boolean isRunning() {
        return this.isAlive.get();
    }
}
