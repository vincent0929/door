package com.vc.door.core.configuration.druid;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.sql.SQLException;

@Configuration
@ConfigurationProperties(prefix = DruidConfiguration.DATA_SOURCE_PREFIX)
@Import(DruidProperties.class)
public class DruidConfiguration {

    static final String DATA_SOURCE_PREFIX = "spring.datasource";

    @Autowired
    @NestedConfigurationProperty
    private DruidProperties druidProperties;

    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String password;

    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setFilters(druidProperties.getFilters());
        dataSource.setMaxActive(druidProperties.getMaxActive());
        dataSource.setInitialSize(druidProperties.getInitialSize());
        dataSource.setMaxWait(druidProperties.getMaxWait());
        dataSource.setMinIdle(druidProperties.getMinIdle());
        dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        dataSource.setTestWhileIdle(druidProperties.getTestWhileIdle());
        dataSource.setTestOnBorrow(druidProperties.getTestOnBorrow());
        dataSource.setTestOnReturn(druidProperties.getTestOnReturn());
        dataSource.setPoolPreparedStatements(druidProperties.getPoolPreparedStatements());
        dataSource.setMaxOpenPreparedStatements(druidProperties.getMaxOpenPreparedStatements());
        dataSource.setAsyncInit(druidProperties.getAsyncInit());
        return dataSource;
    }
}
