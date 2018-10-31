package cn.hurrican.common.lock;

import cn.hurrican.common.redis.RedisExecutor;

import java.util.Collections;


public class AutoReleaseLock implements AutoCloseable {

    private static final String LOCK_SUCCESS = "OK";

    private static final String SET_IF_NOT_EXIST = "NX";

    private static final String EXPIRE_TIME = "EX";

    private static final Long RELEASE_SUCCESS = 1L;

    private RedisExecutor executor;

    private String lockKey;

    private long expireTime;

    private String clientId;

    private boolean successLock;

    public AutoReleaseLock(RedisExecutor executor) {
        this.executor = executor;
    }

    public AutoReleaseLock(RedisExecutor executor, String lockKey, long expireTime, String clientId) {
        this.executor = executor;
        this.lockKey = lockKey;
        this.expireTime = expireTime;
        this.clientId = clientId;
    }

    @Override
    public void close() throws Exception {
        System.out.println("release distributed lock...");
        releaseDistributedLock();
    }


    public boolean tryGetDistributedLock() {
        if (executor != null) {
            return executor.doInRedis(instance -> {
                String response = instance.set(lockKey, clientId, SET_IF_NOT_EXIST, EXPIRE_TIME, expireTime);
                this.successLock = LOCK_SUCCESS.equals(response);
                return successLock;
            });
        }
        return false;
    }


    /**
     * 释放分布式锁
     *
     * @return 是否释放成功
     */
    public boolean releaseDistributedLock() {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        if (executor != null && successLock) {
            return executor.doInRedis(instance -> {
                Object result = instance.eval(script, Collections.singletonList(lockKey), Collections.singletonList(clientId));
                return RELEASE_SUCCESS.equals(result);
            });
        }
        return false;
    }


}

