package com.vc.door.core.configuration.druid;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = DruidProperties.DRUID_CONFIG_PREFIX)
public class DruidProperties {
    static final String DRUID_CONFIG_PREFIX = "spring.datasource.druid";

    private String filters;
    private Integer maxActive;
    private Integer initialSize;
    private Integer maxWait;
    private Integer minIdle;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer minEvictableIdleTimeMillis;
    private Boolean testWhileIdle;
    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Boolean poolPreparedStatements;
    private int maxOpenPreparedStatements;
    private Boolean asyncInit;
}
