package com.sathinking.orderservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShardDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.shard0")
    public DataSource shard0DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.shard1")
    public DataSource shard1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.shard2")
    public DataSource shard2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean
    public DataSource shardRoutingDataSource(DataSource shard0DataSource, DataSource shard1DataSource, DataSource shard2DataSource) {
        ShardRoutingDataSource routingDataSource = new ShardRoutingDataSource();

        Map<Object, Object> targets = new HashMap<>();
        targets.put("shard-0", shard0DataSource);
        targets.put("shard-1", shard1DataSource);
        targets.put("shard-2", shard2DataSource);

        routingDataSource.setTargetDataSources(targets);
        routingDataSource.setDefaultTargetDataSource(shard0DataSource);
        routingDataSource.afterPropertiesSet();

        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}