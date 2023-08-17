package com.sls.monitor.annotation;


import java.lang.annotation.*;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/16 10:56
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiMonitor {
    String description();
}
