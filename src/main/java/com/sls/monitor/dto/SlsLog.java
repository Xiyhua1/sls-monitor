package com.sls.monitor.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author Luowenjian
 * @Description
 * @Date 2023/8/15 17:15
 **/
@Data
@Accessors(chain = true)
public class SlsLog {
    private String level;
    private String code;
    private String data;
    private String description;
    private String message;
    private String url;
}
