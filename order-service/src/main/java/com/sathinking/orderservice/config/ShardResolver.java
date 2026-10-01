package com.sathinking.orderservice.config;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

@Component
public class ShardResolver {

    private static final int NUM_SHARDS = 3;

    public String resolveShard(Long customerId) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md5.digest(("customer_" + customerId).getBytes(StandardCharsets.UTF_8));
            BigInteger hashValue = new BigInteger(1, hashBytes); // unsigned
            int shardIndex = hashValue.mod(BigInteger.valueOf(NUM_SHARDS)).intValue();
            return "shard-" + shardIndex;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}