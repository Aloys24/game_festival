package com.miniw.fesweb.task;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miniw.fescommon.base.dto.UserLoginData;
import com.miniw.fescommon.constant.FestivalConstant;
import com.miniw.fescommon.constant.RedisKeyConstant;
import com.miniw.fescommon.constant.SendEmailConstant;
import com.miniw.fescommon.utils.RedisUtil;
import com.miniw.fescommon.utils.ThreadPoolUtil;
import com.miniw.fesexternal.client.openlogs.OpenLogsClient;
import com.miniw.fesexternal.client.openlogs.params.OpenLogsConstant;
import com.miniw.fespersistence.model.TAwardRecord;
import com.miniw.fesweb.params.dto.BuySuccessDto;
import com.miniw.fesweb.params.dto.EmailFailedDto;
import com.miniw.fesweb.params.vo.SkinVo;
import com.miniw.gameapi.api.EmailApi;
import com.miniw.gameapi.exception.GameApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 计划任务
 *
 * @author luoquan
 * @date 2021/08/23
 */
@Component
@Slf4j
public class ScheduledTask {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private EmailApi emailApi;
    @Resource
    private OpenLogsClient openLogsClient;

    private static final Collection<Integer> SEAL_NUM_LIST = Arrays.asList(10, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 350, 400, 450, 500);


    /**
     * 礼包-邮件发送失败补偿任务
     * （每1分钟执行一次
     */
    @Async
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void packageEmailFailedTask() {
//        log.debug("礼包-邮件发送补偿任务执行 - {}", LocalDateTime.now());
        AtomicInteger count = new AtomicInteger(0);

        do {
            if (!redisUtil.hasKey(RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST)) {
                break;
            }
            EmailFailedDto failedDto = JSONObject.parseObject(
                    String.valueOf(redisUtil.lRightPop(RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST)), EmailFailedDto.class);
            final Long uid = failedDto.getUid();
            final String type = failedDto.getType();
            // 获取缓存中的登录态
            final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
            if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
                return;
            }
            UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);

            log.info("正在对uid - {}进行道具奖励邮件补发 - {}", uid, LocalDateTime.now());

            String PACKAGE_EMAIL_BODY = null;
            if (type.equals(FestivalConstant.LIUXIANER_PRESALE_TYPE) ||
                    type.equals(FestivalConstant.LIUXIANER_OFFICIAL_TYPE)) {
                PACKAGE_EMAIL_BODY = SendEmailConstant.PACKAGE_LIUXIANER_EMAIL_BODY;
            }

            if (type.equals(FestivalConstant.YUEWUSHUANG_PRESALE_TYPE) ||
                    type.equals(FestivalConstant.YUEWUSHUANG_OFFICIAL_TYPE)) {
                PACKAGE_EMAIL_BODY = SendEmailConstant.PACKAGE_YUEWUSHUANG_EMAIL_BODY;
            }

            try {
                emailApi.sendEmail(uid, SendEmailConstant.PACKAGE_EMAIL_TITLE,
                        PACKAGE_EMAIL_BODY,
                        SendEmailConstant.PACKAGE_EMAIL_ACT_URL,
                        failedDto.getAttachMap(), null);
            } catch (GameApiException e) {
                log.error("uid - {}，礼包-道具奖励邮件补发失败，已重新入队 - {}", uid, RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST);
                redisUtil.lRightPush(RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST, failedDto);
            }
            // 记录订购数据
            BuySuccessDto successDto = new BuySuccessDto(uid, failedDto.getType(), new Date(), failedDto.getAttachMap(), null);
            String successTempKey = String.format("%s%s", RedisKeyConstant.BUY_SUCCESS_TEMP, type);
            redisUtil.hPut(successTempKey, Long.toString(uid), successDto);

            final Object packageStatus = redisUtil.hGet(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid));
            SkinVo vo = JSONObject.parseObject(packageStatus.toString(), SkinVo.class);

            // 更新礼包查询缓存（包含未发放的换装装扮、已发放的奖励道具及原装扮
            if (redisUtil.hHasKey(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid))) {
                final SkinVo result = SkinVo.updateRecord(vo, failedDto.getSkinQuery());
                redisUtil.hPut(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid), result);
            }

            // 数据打点(v7: 1 购买预售 2 直接购买
            openLogsClient.openLog(OpenLogsConstant.BUY_ID, Long.toString(uid),
                    loginData.getApiId(), loginData.getVer(),
                    loginData.getCountry(), loginData.getLangId(),
                    failedDto.getV6(), 2, null
            );

            count.incrementAndGet();
            log.info("uid - {}，礼包-道具奖励邮件补发成功：{}", uid, LocalDateTime.now());
        } while (count.get() <= 50);

    }


    /**
     * 活动-邮件发送失败补偿任务
     * （每1分钟执行一次
     */
    @Async
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void activityEmailFailedTask() {
//        log.debug("活动-邮件发送补偿任务执行 - {}", LocalDateTime.now());
        AtomicInteger count = new AtomicInteger(0);

        do {
            if (!redisUtil.hasKey(RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST)) {
                break;
            }
            EmailFailedDto failedDto = JSONObject.parseObject(
                    String.valueOf(redisUtil.lRightPop(RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST)), EmailFailedDto.class);
            final Long uid = failedDto.getUid();
            log.info("正在对uid - {}进行道具奖励邮件补发 - {}", uid, LocalDateTime.now());

            try {
                emailApi.sendEmail(uid, SendEmailConstant.YUELEYUAN_EMAIL_TITLE,
                        SendEmailConstant.YUELEYUAN_EMAIL_BODY,
                        SendEmailConstant.YUELEYUAN_EMAIL_ACT_URL,
                        failedDto.getAttachMap(), null);
            } catch (GameApiException e) {
                log.error("uid - {}，活动-道具奖励邮件补发失败，已重新入队 - {}", uid, RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST);
                redisUtil.lRightPush(RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST, failedDto);
            }

            //  记录奖励
            TAwardRecord tAwardRecord = new TAwardRecord();
            tAwardRecord.setUid(uid);
            tAwardRecord.setContent(String.format("%s*%s", failedDto.getAvatarName(), failedDto.getAvatarNum()));
            tAwardRecord.setTime(DateUtil.format(new Date(), "MM-dd HH:mm:ss"));

            count.incrementAndGet();
            log.info("uid - {}，活动-道具奖励邮件补发成功：{}", uid, LocalDateTime.now());
        }while (count.get() <= 50);

    }


    /**
     *  柳仙儿礼包预售期邮件发送
     * （从2021-09-16 10：00开始 每分钟执行一次
     *
     */
    @Async
//    @Scheduled(cron = "0 0/1 10 16 9 ? ")
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void preSaleOfLiuXianEr() {
        ThreadPoolUtil.ThreadPollProxy threadPollProxy = ThreadPoolUtil.getThreadPollProxy();

        log.info("time - {}, threadName - {}正在执行柳仙儿预售发放礼包任务", LocalDateTime.now(), Thread.currentThread().getName());
        // 每次拉取500条
        final Set<String> set = redisUtil.zRangeByScore(RedisKeyConstant.LIUXIANER_PRESALE_DELAY, 0, System.currentTimeMillis(), 0, 500);
        if (CollectionUtils.isEmpty(set)) {
            return;
        }
        for (String json : set) {
            threadPollProxy.execute(() -> {
                final BuySuccessDto successDto = JSONObject.parseObject(json, BuySuccessDto.class);
                if (null != successDto) {
                    final String uid = String.valueOf(successDto.getUid());
                    // 只拿未发放的新装扮
                    final Map<Integer, Integer> newSkinMap = successDto.getNewSkinMap();
                        final Long aLong = redisUtil.zRem(RedisKeyConstant.LIUXIANER_PRESALE_DELAY, json);
                        // 只有删除成功了才进行业务逻辑操作
                        if (aLong != null && aLong == 1) {
                            try {
                                emailApi.sendEmail(Long.valueOf(uid), SendEmailConstant.PACKAGE_EMAIL_TITLE,
                                        SendEmailConstant.PACKAGE_LIUXIANER_EMAIL_BODY,
                                        SendEmailConstant.PACKAGE_EMAIL_ACT_URL,
                                        newSkinMap, null);
                            } catch (GameApiException e) {
                                log.error("{}:预售礼包发放失败:{}，已重新入队", uid, e.getMessage());
                                redisUtil.zAdd(RedisKeyConstant.LIUXIANER_PRESALE_DELAY, JSON.toJSONString(successDto), System.currentTimeMillis());
                            }
                            log.info("{}:柳仙儿预售礼包已经发送", uid);
                        }
                    }
                });
            }
    }


    /**
     *  月无双礼包预售期邮件发送
     * （从2021-09-27 10：00开始 每分钟执行一次
     *
     */
    @Async
//    @Scheduled(cron = "0 0/1 10 26 9 ? ")
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void preSaleOfYueWuShuang() {
        ThreadPoolUtil.ThreadPollProxy threadPollProxy = ThreadPoolUtil.getThreadPollProxy();

        log.info("time - {}, threadName - {}正在执行月无双预售发放礼包任务", LocalDateTime.now(), Thread.currentThread().getName());
        // 每次拉取500条
        final Set<String> set = redisUtil.zRangeByScore(RedisKeyConstant.YUEWUSHUANG_PRESALE_DELAY, 0, System.currentTimeMillis(), 0, 500);
        if (CollectionUtils.isEmpty(set)) {
            return;
        }
        for (String json : set) {
            threadPollProxy.execute(() -> {
                final BuySuccessDto successDto = JSONObject.parseObject(json, BuySuccessDto.class);
                if (null != successDto) {
                    final String uid = String.valueOf(successDto.getUid());
                    // 只拿未发放的新装扮
                    final Map<Integer, Integer> newSkinMap = successDto.getNewSkinMap();
                        final Long aLong = redisUtil.zRem(RedisKeyConstant.YUEWUSHUANG_PRESALE_DELAY, json);
                        // 只有删除成功了才进行业务逻辑操作
                        if (aLong != null && aLong == 1) {
                            try {
                                emailApi.sendEmail(Long.valueOf(uid), SendEmailConstant.PACKAGE_EMAIL_TITLE,
                                        SendEmailConstant.PACKAGE_YUEWUSHUANG_EMAIL_BODY,
                                        SendEmailConstant.PACKAGE_EMAIL_ACT_URL,
                                        newSkinMap, null);
                            } catch (GameApiException e) {
                                log.error("{}:月无双预售礼包发放失败:{}，已重新入队", uid, e.getMessage());
                                redisUtil.zAdd(RedisKeyConstant.YUEWUSHUANG_PRESALE_DELAY, JSON.toJSONString(successDto), System.currentTimeMillis());
                            }
                            log.info("{}:月无双预售礼包已经发送", uid);
                        }
                    }
                });
            }
    }


    /**
     * 累计积分奖励产出情况
     * （每天凌晨执行一次
     */
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    public void sealNumAccountTask() {
        log.info("累计积分奖励产出情况任务执行 - {}", LocalDateTime.now());
        SEAL_NUM_LIST.forEach(num -> {
            int reachCount = redisUtil.hGetInt(RedisKeyConstant.SEAL_NUM_REACH_COUNT, Long.toString(num));
            int receiveCount = redisUtil.hGetInt(RedisKeyConstant.SEAL_NUM_REACH_COUNT, Long.toString(num));

            log.info("{} - 当前达到人数：{}, 当前领取人数：{}", LocalDateTime.now(), reachCount, receiveCount);

            // 达成人数、领取人数数据打点
            openLogsClient.openLog(OpenLogsConstant.SEAL_NUM_ID, " ",
                    "999", "0.43.0",
                    "CN", "0",
                    num, reachCount, receiveCount
            );

        });


    }


}
