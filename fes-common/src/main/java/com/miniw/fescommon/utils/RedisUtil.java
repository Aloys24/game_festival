package com.miniw.fescommon.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.miniw.fescommon.base.dto.BuySuccessDto;
import com.miniw.fescommon.constant.FestivalConstant;
import com.miniw.fescommon.constant.RedisKeyConstant;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 *
 * @author luoquan
 * @date 2021/08/20
 */
@Component
public class RedisUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取key的过期时间 单位：秒
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 设置key的生命周期
     * @param key      key
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void expireKey(String key, long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 获取key值内容
     * @param key 键
     */
    public String sGet(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * String 自增长(带过期时间)
     * @param key    key
     * @param expire 过期时间
     */
    public long sIncr(String key, long expire) {
        Long incr = stringRedisTemplate.opsForValue().increment(key);
        if (incr != null && incr == 1 && expire > 0) {
            stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
        return incr == null ? 0 : incr;

    }


    /**
     * 设置key值及过期时间
     *
     * @param key    key
     * @param value  value
     * @param expire 过期时间
     * @param unit   单位
     */
    public void sPut(String key, Object value, long expire, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value));
        expireKey(key, expire, unit);
    }


    /**
     * 设置key值
     *
     * @param key    key
     * @param value  value
     * @param expire 过期时间 单位：天
     */
    public void sPutExDay(String key, Object value, long expire) {
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value));
        expireKey(key, expire, TimeUnit.DAYS);
    }

    /**
     * 删除key
     */
    public boolean sDelete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 获取Hash所有给定字段的值
     * @param key       key
     * @param hashKeys  字段
     */
    public List<String> hMultiGet(String key, List<String> hashKeys) {
        if (hashKeys == null || hashKeys.isEmpty()) {
            return new ArrayList<>();
        }
        HashOperations<String, String, String> operations = stringRedisTemplate.opsForHash();
        return operations.multiGet(key, hashKeys);
    }

    /**
     * 递增Hash键指定字段的值和过期时间
     *
     * @param key     key
     * @param hashKey 字段
     * @param delta   增量
     * @param date    日期
     */
    public void hIncrementAndEx(String key, String hashKey, int delta, Date date) {
        stringRedisTemplate.opsForHash().increment(key, hashKey, delta);
        stringRedisTemplate.expireAt(key, date);
    }

    public void hScan(String key, int count) throws IOException, ParseException {
        Cursor<Map.Entry<Object, Object>> cursor = stringRedisTemplate.opsForHash().scan(
                String.format("%s%s", RedisKeyConstant.BUY_SUCCESS_TEMP, "yuewushuang_presale"),
                ScanOptions.scanOptions().count(100).match("*").build()
        );
        while (cursor.hasNext()){
            BuySuccessDto buySuccessDto = JSON.parseObject(cursor.next().getValue().toString(), BuySuccessDto.class);
            final DateTime date = DateUtil.date(buySuccessDto.getDate());

            Date startTime = DateUtils.parseDate(FestivalConstant.YUEWUSHUANG_PRESALE_START, "yyyy-MM-dd HH:mm:ss");
            Date endTime = DateUtils.parseDate(FestivalConstant.YUEWUSHUANG_PRESALE_END, "yyyy-MM-dd HH:mm:ss");
//            buySuccessDto.getAttachMap().containsKey(13043)

            if(DateUtil.isIn(date, startTime, endTime) && buySuccessDto.getNewSkinMap().containsKey(13043)){
                final String format = String.format("%s  %s  %s", buySuccessDto.getUid(), buySuccessDto.getAttachMap(), date);
                System.out.println(format);
            }
        }
        //关闭cursor
        cursor.close();
    }


    /**
     * 递增Hash键指定字段的值
     *
     * @param key     key
     * @param hashKey 字段
     * @param delta   增量
     * @return {@link Long}
     */
    public Long hIncrement(String key, String hashKey, int delta) {
        return stringRedisTemplate.opsForHash().increment(key, hashKey, delta);
    }


    /**
     * 获取Hash键指定的字段值
     * @param key       key
     * @param hashKey   字段
     * @return Object
     */
    public Object hGet(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().get(key, hashKey);
    }


    /**
     * 获取Hash键指定的字段值
     * @param key       key
     * @param hashKey   字段
     * @return int
     */
    public int hGetInt(String key, String hashKey) {
        Object obj = stringRedisTemplate.opsForHash().get(key, hashKey);
        return obj != null ? Integer.parseInt(String.valueOf(obj)) : 0;
    }

    /**
     * Hash键指定的字段值是否存在
     * @param key       key
     * @param hashKey   字段
     */
    public boolean hHasKey(String key, Object hashKey) {
        return stringRedisTemplate.opsForHash().hasKey(key, String.valueOf(hashKey));
    }

    /**
     * 删除指定的Hash key
     *
     * @param key     关键
     * @param hashKey 散列键
     * @return {@link Long}
     */
    public Long hDelKey(String key, Object hashKey){
        return stringRedisTemplate.opsForHash().delete(key, hashKey);
    }

    /**
     * 设置Hash键指定字段值
     * @param key       key
     * @param hashKey   字段
     * @param value     value
     */
    public void hPut(String key, Object hashKey, Object value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, JSON.toJSONString(value));
    }

    /**
     * 设置Hash键指定字段并设置过期时间
     *
     * @param key     key
     * @param hashKey 字段
     * @param value   value
     * @param date    日期
     */
    public void hPutAndEx(String key, Object hashKey, Object value, Date date){
        stringRedisTemplate.executePipelined((RedisCallback<String>) connection ->{
            connection.hSet(key.getBytes(), String.valueOf(hashKey).getBytes(), JSON.toJSONBytes(value));
            connection.pExpireAt(key.getBytes(), date.getTime());
            return null;
        });
    }


    /**
     * 设置集合(Set)键值
     * @param key   key
     * @param value value
     */
    public Long sAdd(String key, Object value) {
        return stringRedisTemplate.opsForSet().add(key, JSON.toJSONString(value));
    }

    /**
     * 获取集合(Set)大小
     * @param key   key
     */
    public Long sSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    /**
     * 获取集合length个随机
     * @param key       key
     * @param length    随机数量
     */
    public List<String> sRandMembers(String key, int length) {
        List<String> members = stringRedisTemplate.opsForSet().randomMembers(key, length);
        return members == null ? new ArrayList<>() : members;
    }


    /**
     *  range list
     *
     * @param key   key
     * @param start 起始索引
     * @param end   结束索引
     * @return {@link List}<{@link String}>
     */
    public List<String> lGet(String key, long start, long end){
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 从右入队
     *
     * @param key  key
     * @param value 值
     */
    public void lRightPush(String key, Object value) {
        stringRedisTemplate.opsForList().rightPush(key, JSON.toJSONString(value));
    }

    /**
     * 从右移除并返回列表key的头元素
     *
     * @param key  key
     */
    public Object lRightPop(String key){
        return stringRedisTemplate.opsForList().rightPop(key);
    }


    /**
     * 从左入队
     *
     * @param key   key
     * @param value 值
     */
    public void lLeftPush(String key, Object value){
        stringRedisTemplate.opsForList().leftPush(key, JSON.toJSONString(value));
    }


    /**
     * 从左移除并返回列表key的头元素
     *
     * @param key key
     * @return {@link String}
     */
    public String lLeftPop(String key){
        return stringRedisTemplate.opsForList().rightPop(key);
    }


    public void lSetIfPresent(String key, Object value){
        stringRedisTemplate.opsForList().leftPushIfPresent(key, JSON.toJSONString(value));
    }



    public boolean hasKey(String key){
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 获取list的长度
     *
     * @param key key
     * @return long
     */
    public long lGetListSize(String key) { return stringRedisTemplate.opsForList().size(key); }

    /**
     * zSet 删除元素
     * @param key   key
     * @param value 元素
     */
    public Long zDelete(String key, String value) {
        return stringRedisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * zSet 添加元素及时间戳
     *
     * @param key       key
     * @param timeStamp 时间戳
     * @param value     value
     */
    public void zAdd(String key, String value, long timeStamp){
        stringRedisTemplate.opsForZSet().add(key, value, timeStamp);
    }


    /**
     * 返回集合内元素在指定分数范围内的排名（从小到大）带偏移量
     *
     * @param key   key
     * @param start 开始
     * @param end   结束
     * @param begin 开始
     * @param size  大小
     * @return {@link Set}<{@link String}>
     */
    public Set<String> zRangeByScore(String key, long start, long end, long begin, long size){
        return stringRedisTemplate.opsForZSet().rangeByScore(key, start, end, begin, size);
    }


    /**
     * 删除指定集合内指定元素
     *
     * @param key   关键
     * @param value value
     * @return {@link Long}
     */
    public Long zRem(String key, String value) {
        return stringRedisTemplate.opsForZSet().remove(key, value);
    }


}
