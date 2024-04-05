package org.example.utils;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component // 限流工具类
public class FlowUtils {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public boolean limitOnceCheck(String key, int blockTime){
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            return false;  // 包含key就返回false
        } else {  // 不包含就让你进入冷却时间
            stringRedisTemplate.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS);
            return true;
        }
    }
}
