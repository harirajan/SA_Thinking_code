package com.sathinking.orderservice.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> CURRENT_SHARD = new ThreadLocal<>();

    public static void setShard(String shardKey) {
        CURRENT_SHARD.set(shardKey);
    }

    public static void clearShard() {
        CURRENT_SHARD.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        //return CURRENT_SHARD.get();
        String shard = CURRENT_SHARD.get();
        System.out.println("Resolved shard for this connection: " + shard);
        return shard;
    }
}