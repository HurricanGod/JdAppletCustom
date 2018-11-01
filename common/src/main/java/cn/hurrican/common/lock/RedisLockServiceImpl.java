package cn.hurrican.common.lock;

import cn.hurrican.common.redis.JedisCallable;
import cn.hurrican.common.redis.RedisExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
public class RedisLockServiceImpl implements RedisLockService {

    private static final int DEFAULT_SINGLE_EXPIRE_TIME = 5;
    private static final int DEFAULT_BATCH_EXPIRE_TIME = 10;
    private static Logger LOGGER = LogManager.getLogger(RedisLockServiceImpl.class);

    private RedisExecutor redisExecutor;


    /**
     * 获取锁  如果锁可用   立即返回true，  否则返回false
     *
     * @param key
     * @return
     */

    public boolean tryLock(String key) {
        return tryLock(key, 0L, null);
    }

    /**
     * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false
     *
     * @param key
     * @param timeout
     * @param unit
     * @return
     */

    public boolean tryLock(final String key, final long timeout, final TimeUnit unit) {
        JedisCallable<Boolean> callable = new JedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis instance) throws Exception {
                long nano = System.nanoTime();
                do {
                    LOGGER.info("try lock key: " + key);
                    Long i = instance.setnx(key, key);
                    if (i == 1) {
                        instance.expire(key, DEFAULT_SINGLE_EXPIRE_TIME);
                        LOGGER.info("get lock, key: " + key + " , expire in " + DEFAULT_SINGLE_EXPIRE_TIME + " seconds.");
                        return Boolean.TRUE;
                    } else { // 存在锁
                        String desc = instance.get(key);
                        LOGGER.info("key: " + key + " locked by another business：" + desc);
                    }
                    if (timeout == 0) {
                        break;
                    }
                    Thread.sleep(300);
                } while ((System.nanoTime() - nano) < unit.toNanos(timeout));
                return Boolean.FALSE;
            }
        };
        return redisExecutor.doInRedis(callable);
    }

    public boolean tryLock(final String key, final long timeout, final TimeUnit unit, final long lockMaxTime, final TimeUnit maxTimeUnit) {
        JedisCallable<Boolean> callable = new JedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis instance) throws Exception {
                long nano = System.nanoTime();
                do {
                    LOGGER.info("try lock key: " + key);
                    Long i = instance.setnx(key, key);
                    if (i == 1) {
                        instance.expire(key, (int) maxTimeUnit.toSeconds(lockMaxTime));
                        LOGGER.info("get lock, key: " + key + " , expire in " + DEFAULT_SINGLE_EXPIRE_TIME + " seconds.");
                        return Boolean.TRUE;
                    } else { // 存在锁
                        String desc = instance.get(key);
                        LOGGER.info("key: " + key + " locked by another business：" + desc);
                    }
                    if (timeout == 0) {
                        break;
                    }
                    Thread.sleep(300);
                } while ((System.nanoTime() - nano) < unit.toNanos(timeout));
                return Boolean.FALSE;
            }
        };
        return redisExecutor.doInRedis(callable);
    }

    /**
     * 如果锁空闲立即返回   获取失败 一直等待
     *
     * @param key
     */

    public boolean lock(final String key) {
        JedisCallable<Boolean> callable = new JedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis instance) throws Exception {
                do {
                    LOGGER.info("lock key: " + key);
                    Long i = instance.setnx(key, key);
                    if (i == 1) {
                        instance.expire(key, DEFAULT_SINGLE_EXPIRE_TIME);
                        LOGGER.info("get lock, key: " + key + " , expire in " + DEFAULT_SINGLE_EXPIRE_TIME + " seconds.");
                        return Boolean.TRUE;
                    } else {
                        String desc = instance.get(key);
                        LOGGER.error("key: " + key + " locked by another business：" + desc);
                    }
                    Thread.sleep(300);
                } while (true);
            }
        };
        return redisExecutor.doInRedis(callable);
    }

    /**
     * 释放锁
     *
     * @param key
     * @author http://blog.csdn.net/java2000_wl
     */

    public void unLock(String key) {
        List<String> list = new ArrayList<>();
        list.add(key);
        unLock(list);
    }

    /**
     * 批量获取锁  如果全部获取   立即返回true, 部分获取失败 返回false
     *
     * @param keyList
     * @return
     */

    public boolean tryLock(List<String> keyList) {
        return tryLock(keyList, 0L, null);
    }

    /**
     * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false
     *
     * @param keyList
     * @param timeout
     * @param unit
     * @return
     */
    public boolean tryLock(final List<String> keyList, final long timeout, final TimeUnit unit) {
        JedisCallable<Boolean> callable = new JedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis instance) throws Exception {
                List<String> needLocking = new CopyOnWriteArrayList<>();
                List<String> locked = new CopyOnWriteArrayList<>();
                long nano = System.nanoTime();
                do {
                    // 构建pipeline，批量提交
                    Pipeline pipeline = instance.pipelined();
                    for (String identify : keyList) {
                        String key = identify;
                        needLocking.add(key);
                        pipeline.setnx(key, key);
                    }
                    LOGGER.debug("try lock keys: " + needLocking);
                    // 提交redis执行计数
                    List<Object> results = pipeline.syncAndReturnAll();
                    for (int i = 0; i < results.size(); ++i) {
                        Long result = (Long) results.get(i);
                        String key = needLocking.get(i);
                        if (result == 1) {  // setnx成功，获得锁
                            instance.expire(key, DEFAULT_BATCH_EXPIRE_TIME);
                            locked.add(key);
                        }
                    }
                    needLocking.removeAll(locked);  // 已锁定资源去除

                    if (CollectionUtils.isEmpty(needLocking)) {
                        return true;
                    } else {
                        // 部分资源未能锁住
                        LOGGER.debug("keys: " + needLocking + " locked by another business：");
                    }

                    if (timeout == 0) {
                        break;
                    }
                    Thread.sleep(500);
                } while ((System.nanoTime() - nano) < unit.toNanos(timeout));

                // 得不到锁，释放锁定的部分对象，并返回失败
                if (!CollectionUtils.isEmpty(locked)) {
                    instance.del(locked.toArray(new String[0]));
                }
                return false;
            }
        };
        return redisExecutor.doInRedis(callable);
    }

    /**
     * 批量释放锁
     *
     * @param keyList
     */

    public void unLock(final List<String> keyList) {
        JedisCallable<Boolean> callable = new JedisCallable<Boolean>() {
            @Override
            public Boolean call(Jedis instance) throws Exception {
                List<String> keys = new CopyOnWriteArrayList<String>();
                for (String key : keyList) {
                    keys.add(key);
                }
                try {
                    instance.del(keys.toArray(new String[0]));
                    LOGGER.debug("release lock, keys :" + keys);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return true;
            }
        };
        redisExecutor.doInRedis(callable);
    }

}

