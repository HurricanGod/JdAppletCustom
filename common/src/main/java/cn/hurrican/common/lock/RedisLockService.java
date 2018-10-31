package cn.hurrican.common.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by HUFENG on 2016/6/6 0006.
 * redis 实现分布式锁
 */
public interface RedisLockService {

    /**
     * 获取锁  如果锁可用   立即返回true，  否则返回false
     *
     * @param key
     * @return
     */
    boolean tryLock(String key);

    /**
     * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false
     *
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    boolean tryLock(String key, long timeout, TimeUnit unit);

    /**
     * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false
     * 如果加锁成功，则回销最大时长为 lockMaxTime
     *
     * @param key
     * @param timeout
     * @param unit
     * @param lockMaxTime 加锁最大时长
     * @param maxTimeUnit
     * @return
     */
    boolean tryLock(String key, long timeout, TimeUnit unit, long lockMaxTime, TimeUnit maxTimeUnit);

    /**
     * 如果锁空闲立即返回   获取失败 一直等待
     *
     * @param key
     * @return
     */
    boolean lock(String key);

    /**
     * 释放锁
     *
     * @param key
     */
    void unLock(String key);

    /**
     * 批量获取锁  如果全部获取   立即返回true, 部分获取失败 返回false
     *
     * @param keyList
     * @return
     */
    boolean tryLock(List<String> keyList);

    /**
     * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false
     *
     * @param keyList
     * @param timeout
     * @param unit
     * @return
     */
    boolean tryLock(List<String> keyList, long timeout, TimeUnit unit);

    /**
     * 批量释放锁
     *
     * @param keyList
     */
    void unLock(List<String> keyList);
}
