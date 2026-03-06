package cn.edu.just.ytc.seems.utils;

import cn.edu.just.ytc.seems.pojo.entity.GPSLog;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  Redis 工具类
 * </p>
 *
 * @author Ya Shi
 * @since 2024/3/12 14:02
 */
@Component
public class RedisUtils {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long RELEASE_SUCCESS = 1L;
    private static final String RELEASE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else " +
            "return 0 " +
            "end";


    // 设置键值对
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }


    public <T> void add2List(String key, T value, int timeout, TimeUnit unit) {
        if (!hasKey(key)) {
            redisTemplate.opsForList().rightPush(key, value);
            redisTemplate.expire(key, timeout, unit);
        } else {
            redisTemplate.opsForList().rightPush(key, value);
        }

    }

    // 设置键值对并指定过期时间
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 设置键值对并指定过期时间
    public void set(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    // 获取值
    public <T> T get(String key, Class<T> clazz) {
        if (!hasKey(key)) {
            return null;
        }
        return JSONObject.parseObject(Objects.requireNonNull(redisTemplate.opsForValue().get(key)).toString(), clazz);
    }

    public <T> List<T> getList(String key) {
        if (!hasKey(key)) {
            return Collections.emptyList();
        }
        return (List<T>) redisTemplate.opsForList().range(key, 0, -1);
    }

    // 获取值
    public Object getString(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        return obj == null ? null : obj;
    }

    // 删除键
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // 判断键是否存在
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 如果不存在，则设置
    public Boolean setNx(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    // 如果不存在，则设置，附带过期时间
    public Boolean tryLock(String lockKey, String requestId, long seconds) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, seconds, TimeUnit.SECONDS);
    }

    // 如果不存在，则设置，附带过期时间
    public Boolean tryLock(String lockKey, String requestId, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, timeout, unit);
    }

    // 不存在返回true，存在则删除
    public Boolean releaseLock(String lockKey, String requestId){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RELEASE_SCRIPT);
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        return RELEASE_SUCCESS.equals(result);
    }

    public <T> void setList(String key, List<T> listValue, int i, TimeUnit timeUnit) {
        redisTemplate.opsForList().rightPushAll(key, listValue.toArray());
        redisTemplate.expire(key, i, timeUnit);
    }
}

