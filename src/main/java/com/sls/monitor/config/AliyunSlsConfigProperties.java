package com.sls.monitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/15 16:19
 **/
@Data
@ConfigurationProperties(prefix = "aliyun.sls")
public class AliyunSlsConfigProperties {
    private String project;
    private String logStore;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
}
