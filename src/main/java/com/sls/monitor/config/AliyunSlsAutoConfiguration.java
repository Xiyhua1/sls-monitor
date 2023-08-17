package com.sls.monitor.config;

import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/15 16:20
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliyunSlsConfigProperties.class)
public class AliyunSlsAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "aliyun.sls", name = {"project", "logStore", "endpoint", "accessKeyId", "accessKeySecret"})
    public AliyunSlsProducer aliyunSlsProducer(AliyunSlsConfigProperties aliyunSlsConfigProperties, @Value("${spring.application.name}") String appName) {
        ProjectConfig projectConfig = new ProjectConfig(aliyunSlsConfigProperties.getProject(), aliyunSlsConfigProperties.getEndpoint()
                , aliyunSlsConfigProperties.getAccessKeyId(), aliyunSlsConfigProperties.getAccessKeySecret());
        return new AliyunSlsProducer(projectConfig, aliyunSlsConfigProperties.getLogStore(), appName);
    }

    @Bean
    @ConditionalOnBean(AliyunSlsProducer.class)
    public ApiMonitorAnnotationBeanPostProcessor apiMonitorAnnotationBeanPostProcessor(AliyunSlsProducer aliyunSlsProducer, HttpServletRequest request){
        return new ApiMonitorAnnotationBeanPostProcessor(aliyunSlsProducer,request);
    }

}
