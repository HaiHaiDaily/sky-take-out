package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    //配置文件 sky.alioss 封装类

    private String endpoint;
    //id
    private String accessKeyId;
    //AccessKey
    private String accessKeySecret;
    private String bucketName;

}
