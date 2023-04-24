package com.miniw.fesweb.application.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miniw.fescommon.base.dto.UserLoginData;
import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.base.vo.ResultCode;
import com.miniw.fescommon.constant.FestivalConstant;
import com.miniw.fescommon.constant.RedisKeyConstant;
import com.miniw.fescommon.constant.SendEmailConstant;
import com.miniw.fescommon.utils.RedisUtil;
import com.miniw.fesexternal.client.openlogs.OpenLogsClient;
import com.miniw.fesexternal.client.openlogs.params.OpenLogsConstant;
import com.miniw.fesweb.application.BuyApplication;
import com.miniw.fesweb.params.base.BuyBase;
import com.miniw.fesweb.params.dto.BuySuccessDto;
import com.miniw.fesweb.params.dto.EmailFailedDto;
import com.miniw.fesweb.params.dto.GetDiceDto;
import com.miniw.fesweb.params.vo.BuySuccessVo;
import com.miniw.fesweb.params.vo.SkinVo;
import com.miniw.gameapi.api.EmailApi;
import com.miniw.gameapi.api.MiniCoinApi;
import com.miniw.gameapi.api.SkinApi;
import com.miniw.gameapi.exception.GameApiException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.time.DateUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 购买相关逻辑
 *
 * @author luoquan
 * @date 2021/08/21
 */
@Slf4j
@Service
public class BuyApplicationImpl implements BuyApplication {

    @Resource
    private Redisson redisson;
    @Resource
    private SkinApi skinApi;
    @Resource
    private MiniCoinApi miniCoinApi;
    @Resource
    private EmailApi emailApi;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private OpenLogsClient openLogsClient;


    /**
     * 预售期间购买礼包：
     *
     * @param buyBase 购买base
     * @return {@link Result}<{@link BuySuccessVo}>
     * @throws InterruptedException 中断异常
     * @throws ParseException       解析异常
     * @throws GameApiException     游戏api例外
     */
    @Override
    public Result<BuySuccessVo> buyOnPreSale(BuyBase buyBase) throws InterruptedException, ParseException, GameApiException {
        final Long uid = buyBase.getUid();
        final String type = buyBase.getType();

        val lockKey = String.format("%s%s", RedisKeyConstant.BUY_PRESALE_LOCK_KEY, uid);
        final RLock lock = redisson.getLock(lockKey);
        if (!lock.tryLock(0, 10, TimeUnit.SECONDS)) {
            log.error("迷你号{}预售期购买正在处理", uid);
            throw new InterruptedException("购买正在处理，请稍后再试");
        }

        try {
            // 获取缓存中的登录态
            final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
            if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
                return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
            }
            UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);


            if (!redisUtil.hHasKey(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid))) {
                return new Result<>(ResultCode.FES_SKIN_NOT_EXIST.getCode(), ResultCode.FES_SKIN_NOT_EXIST.getMsg());
            }
            final Object packageStatus = redisUtil.hGet(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid));
            log.debug("预售购买查询 当前redis缓存为：{}", packageStatus.toString());
            SkinVo vo = JSONObject.parseObject(packageStatus.toString(), SkinVo.class);


            // 前置条件判断
            final Result<Object> objectResult = this.conOfBuy(type, vo);
            if (!objectResult.getCode().equals(ResultCode.SYS_SUCCESS.getCode())) {
                // 如果前置条件不满足 直接返回
                return new Result<>(objectResult.getCode(), objectResult.getMsg());
            }
            // 处理购买逻辑
            return this.buyOfPreSale(uid, Integer.parseInt(String.valueOf(objectResult.getData())), type, vo, loginData);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    /**
     * 购买礼包-正式期
     *
     * @param buyBase 购买base
     * @return {@link Result}<{@link BuySuccessVo}>
     */
    @Override
    public Result<BuySuccessVo> buyOnOfficial(BuyBase buyBase) throws InterruptedException, GameApiException, ParseException {
        final Long uid = buyBase.getUid();
        final String type = buyBase.getType();
        log.debug("{} - 正在进行正式购买", uid);

        val lockKey = String.format("%s%s", RedisKeyConstant.BUY_OFFICIAL_LOCK_KEY, uid);
        final RLock lock = redisson.getLock(lockKey);
        if (!lock.tryLock(0, 10, TimeUnit.SECONDS)) {
            log.error("迷你号{}正式售卖期购买正在处理", uid);
            throw new InterruptedException("购买正在处理，请稍后再试");
        }

        try {
            // 获取缓存中的登录态
            final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
            if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
                return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
            }
            UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);


            if (!redisUtil.hHasKey(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid))) {
                return new Result<>(ResultCode.FES_SKIN_NOT_EXIST.getCode(), ResultCode.FES_SKIN_NOT_EXIST.getMsg());
            }
            final Object packageStatus = redisUtil.hGet(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid));
            log.debug("正式购买查询 当前redis缓存为：{}", packageStatus.toString());
            SkinVo vo = JSONObject.parseObject(packageStatus.toString(), SkinVo.class);


            // 前置条件判断
            final Result<Object> objectResult = this.conOfBuy(type, vo);
            if (!objectResult.getCode().equals(ResultCode.SYS_SUCCESS.getCode())) {
                // 如果前置条件不满足 直接返回
                return new Result<>(objectResult.getCode(), objectResult.getMsg());
            }
            // 处理购买逻辑
            return this.buyOfOfficial(uid, Integer.parseInt(String.valueOf(objectResult.getData())), type, vo, loginData);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }


    /**
     * 礼包正式售卖期逻辑处理：
     * <p>
     * 1. 判断迷你币是否充足；<br/>
     * 2. 扣除迷你币，若扣除失败，不予发放，返回错误信息；<br/>
     * 3. 扣除迷你币成功，立即下发装扮。若失败，则存入redis中进行重试；<br/>
     * 4. 下发成功，记录购买数据（防止恶意请求及重复购买<br/>
     * </p>
     *
     * @param uid       uid
     * @param price     最终价格
     * @param type      类型
     * @param vo        皮肤缓存
     * @param loginData 登录数据
     * @return {@link Result}<{@link BuySuccessVo}>
     * @throws GameApiException 游戏api例外
     */
    private Result<BuySuccessVo> buyOfOfficial(Long uid, Integer price, String type, SkinVo vo, UserLoginData loginData) throws GameApiException {
        final Integer num = miniCoinApi.queryMiniCoin(uid);
        if (num < price) {
            // 迷你币不足
            return new Result<>(ResultCode.FES_COIN_NOT_ENOUGH.getCode(), ResultCode.FES_COIN_NOT_ENOUGH.getMsg());
        }

        /*
            why:扣除迷你币接口参数
            originSkinAvatarId：原装扮道具id
            newSkinAvatarId: 新装扮道具id
            allPrice：礼包价格（用于判断是否发放原装扮
            skinIdList: 需要查询的skinId集
            skinQuery: 皮肤是否拥有query
            v6: 打点装扮id
            packageAwardId: 礼包自带的道具id
         */
        String why = null, DICE_GET_TYP = null, PACKAGE_EMAIL_BODY = null;
        int originSkinAvatarId = 0, allPrice = 0, newSkinAvatarId = 0, v6 = 0, packageAwardId = 0;
        Map<Integer, Boolean> skinQuery = new HashMap<>(2);
        // 是否是购买柳仙儿礼包，如果是 增加一处原装扮的校验
        AtomicBoolean isLiuXianEr = new AtomicBoolean(false);

        if (type.equals(FestivalConstant.LIUXIANER_OFFICIAL_TYPE)) {
            why = FestivalConstant.COUSUME_COIN_WHY_LIUXIANER;
            originSkinAvatarId = FestivalConstant.LIUXIANER_ORIGIN_SKIN_AVATAR_ID;
            allPrice = FestivalConstant.ALL_LIUXIANER_PRICE;
            newSkinAvatarId = FestivalConstant.LIUXIANER_NEW_SKIN_AVATAR_ID;
//            skinQuery.put(FestivalConstant.LIUXIANER_ORIGN_SKIN, true);
            skinQuery.put(FestivalConstant.LIUXIANER_NEW_SKIN, true);

            isLiuXianEr.getAndSet(true);

            v6 = 1;
            packageAwardId = FestivalConstant.LIUXIANER_PACKAGE_AVATAR_ID;
            DICE_GET_TYP = FestivalConstant.DICE_GET_TYPE_LIUXIANER;
            PACKAGE_EMAIL_BODY = SendEmailConstant.PACKAGE_LIUXIANER_EMAIL_BODY;
        } else if (type.equals(FestivalConstant.YUEWUSHUANG_OFFICIAL_TYPE)) {
            why = FestivalConstant.COUSUME_COIN_WHY_YUEWUSHUANG;
            originSkinAvatarId = FestivalConstant.YUEWUSHUNG_ORIGIN_SKIN_AVATAR_ID;
            allPrice = FestivalConstant.ALL_YUEWUSHUANG_PRICE;
            newSkinAvatarId = FestivalConstant.YUEWUSHUNG_NEW_SKIN_AVATAR_ID;
            skinQuery.put(FestivalConstant.YUEWUSHUNG_ORIGN_SKIN, true);
            skinQuery.put(FestivalConstant.YUEWUSHUNG_NEW_SKIN, true);

            v6 = 2;
            packageAwardId = FestivalConstant.YUEWUSHUNG_PACKAGE_AVATAR_ID;
            DICE_GET_TYP = FestivalConstant.DICE_GET_TYPE_YUEWUSHUANG;
            PACKAGE_EMAIL_BODY = SendEmailConstant.PACKAGE_YUEWUSHUANG_EMAIL_BODY;
        }

        // 扣除迷你币（异常无需特殊处理 在advice中进行统一处理
        miniCoinApi.consumeMiniCoin(uid, price, why, null);

        // 立即下发换装装扮(根据价格来判断是否需要发放原装扮
        Map<Integer, Integer> attachMap = new HashMap<>(2);
        attachMap.put(newSkinAvatarId, 1);
        attachMap.put(packageAwardId, 1);
        // 最终价格与礼包总价匹配，发放原装扮
        if (price.compareTo(allPrice) == 0) {
            // （20210903产品需求调整：柳仙儿原装扮不参与售卖）
            if (!isLiuXianEr.get()) {
                attachMap.put(originSkinAvatarId, 1);
            }
        }

        try {
            emailApi.sendEmail(uid, SendEmailConstant.PACKAGE_EMAIL_TITLE,
                    PACKAGE_EMAIL_BODY,
                    SendEmailConstant.PACKAGE_EMAIL_ACT_URL,
                    attachMap, null);
            log.debug("{} - 邮件已发送", uid);
        } catch (GameApiException e) {
            log.error("uid - {}，正式售卖期购买发送道具邮件失败，已入队 - {}", uid, RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST);
            redisUtil.lRightPush(RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST, new EmailFailedDto(uid, attachMap, type, v6, skinQuery, null, null));
            return new Result<>(ResultCode.SYS_BUSY.getCode(), "当前活动太火爆啦，五分钟之后发放奖励");
        }

        // 记录订购数据
        BuySuccessDto successDto = new BuySuccessDto(uid, type, new Date(), attachMap, null);
        String successTempKey = String.format("%s%s", RedisKeyConstant.BUY_SUCCESS_TEMP, type);
        redisUtil.hPut(successTempKey, Long.toString(uid), successDto);

        // 维持一份是否购买过（包含正式、预售购买)的埋点，用于之后的领取万能骰子操作
        String key = String.format("%s%s", RedisKeyConstant.GET_UNIVERSAL_LIMIT, uid);
        redisUtil.hPut(key, DICE_GET_TYP, new GetDiceDto(DICE_GET_TYP, false, true));

        // 更新礼包查询缓存（包含未发放的换装装扮、已发放的奖励道具及原装扮
        if (redisUtil.hHasKey(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid))) {
            final SkinVo result = SkinVo.updateRecord(vo, skinQuery);
            redisUtil.hPut(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid), result);
        }

        // 数据打点(v7: 1 购买预售 2 直接购买
        openLogsClient.openLog(OpenLogsConstant.BUY_ID, Long.toString(uid),
                loginData.getApiId(), loginData.getVer(),
                loginData.getCountry(), loginData.getLangId(),
                v6, 2, null
        );
        log.debug("{} - 正式购买已完成", uid);
        return Result.success(BuySuccessVo.convertTo(attachMap, type));
    }

    /**
     * 礼包预售购买逻辑处理：
     * <p>
     * 1. 判断迷你币是否充足；<br/>
     * 2. 扣除迷你币，若扣除失败，不予发放，返回错误信息；<br/>
     * 3. 扣除迷你币成功，下发原装扮及额外道具。若失败，则存入redis中进行重试；<br/>
     * 4. 下发成功，记录购买数据（防止恶意请求及重复购买），并记录一份数据，0916 10点之后进行发放<br/>
     * </p>
     *
     * @param uid       uid
     * @param price     最终价格
     * @param type      类型
     * @param vo        皮肤缓存
     * @param loginData 登录数据
     * @return {@link Result}<{@link BuySuccessVo}>
     * @throws GameApiException 游戏api异常
     */
    private Result<BuySuccessVo> buyOfPreSale(Long uid, Integer price, String type, SkinVo vo, UserLoginData loginData) throws GameApiException {
        log.debug("{} - 正在进行预售购买", uid);
        final Integer num = miniCoinApi.queryMiniCoin(uid);
        if (num < price) {
            // 迷你币不足
            return new Result<>(ResultCode.FES_COIN_NOT_ENOUGH.getCode(), ResultCode.FES_COIN_NOT_ENOUGH.getMsg());
        }

        /*
            why:扣除迷你币接口参数
            zSet：延时发放礼包zSet集
            originSkinAvatarId：原装扮道具id
            avatarId：奖励道具id
            allPrice：礼包价格（用于判断是否发放原装扮
            newSkinMap: 未发放的换装道具id及数量
            skinQuery: 皮肤是否拥有query
            v6: 打点装扮id
         */
        String why = null, zSet = null, DICE_GET_TYP = null, PACKAGE_EMAIL_BODY = null;
        int originSkinAvatarId = 0, avatarId = 0, allPrice = 0, v6 = 0;
        Map<Integer, Integer> newSkinAvatarMap = new HashMap<>(2);
        Map<Integer, Boolean> skinQuery = new HashMap<>(2);
        // 是否是购买柳仙儿礼包，如果是 增加一处原装扮的校验
        AtomicBoolean isLiuXianEr = new AtomicBoolean(false);

        if (type.equals(FestivalConstant.LIUXIANER_PRESALE_TYPE)) {
            why = FestivalConstant.COUSUME_COIN_WHY_LIUXIANER;
            originSkinAvatarId = FestivalConstant.LIUXIANER_ORIGIN_SKIN_AVATAR_ID;
            avatarId = FestivalConstant.LIUXIANER_AWARD_AVATAR_ID;
            allPrice = FestivalConstant.ALL_LIUXIANER_PRICE;
            zSet = RedisKeyConstant.LIUXIANER_PRESALE_DELAY;
            newSkinAvatarMap.put(FestivalConstant.LIUXIANER_NEW_SKIN_AVATAR_ID, 1);
            newSkinAvatarMap.put(FestivalConstant.LIUXIANER_PACKAGE_AVATAR_ID, 1);

            skinQuery.put(FestivalConstant.LIUXIANER_ORIGN_SKIN, true);
            skinQuery.put(FestivalConstant.LIUXIANER_NEW_SKIN, true);

            isLiuXianEr.getAndSet(true);
            DICE_GET_TYP = FestivalConstant.DICE_GET_TYPE_LIUXIANER;
            PACKAGE_EMAIL_BODY = SendEmailConstant.PACKAGE_LIUXIANER_EMAIL_BODY;
            v6 = 1;
        } else if (type.equals(FestivalConstant.YUEWUSHUANG_PRESALE_TYPE)) {
            why = FestivalConstant.COUSUME_COIN_WHY_YUEWUSHUANG;
            originSkinAvatarId = FestivalConstant.YUEWUSHUNG_ORIGIN_SKIN_AVATAR_ID;
            avatarId = FestivalConstant.YUEWUSHUNG_AWARD_AVATAR_ID;
            allPrice = FestivalConstant.ALL_YUEWUSHUANG_PRICE;
            zSet = RedisKeyConstant.YUEWUSHUANG_PRESALE_DELAY;
            newSkinAvatarMap.put(FestivalConstant.YUEWUSHUNG_NEW_SKIN_AVATAR_ID, 1);
            newSkinAvatarMap.put(FestivalConstant.YUEWUSHUNG_PACKAGE_AVATAR_ID, 1);

            skinQuery.put(FestivalConstant.YUEWUSHUNG_ORIGN_SKIN, true);
            skinQuery.put(FestivalConstant.YUEWUSHUNG_NEW_SKIN, true);
            DICE_GET_TYP = FestivalConstant.DICE_GET_TYPE_YUEWUSHUANG;
            PACKAGE_EMAIL_BODY = SendEmailConstant.PACKAGE_YUEWUSHUANG_EMAIL_BODY;
            v6 = 2;
        }

        // 扣除迷你币（异常无需特殊处理 在advice中进行统一处理
        miniCoinApi.consumeMiniCoin(uid, price, why, null);

        // 下发装扮及额外道具(根据价格来判断是否需要发放原装扮
        Map<Integer, Integer> attachMap = new HashMap<>(2);
        attachMap.put(avatarId, 1);
        // 最终价格与礼包总价匹配，发放原装扮
        if (price.compareTo(allPrice) == 0) {
            // （20210903产品需求调整：柳仙儿原装扮不参与售卖）
            if (!isLiuXianEr.get()) {
                attachMap.put(originSkinAvatarId, 1);
            }
        }

        try {
            emailApi.sendEmail(uid, SendEmailConstant.PACKAGE_EMAIL_TITLE,
                    PACKAGE_EMAIL_BODY,
                    SendEmailConstant.PACKAGE_EMAIL_ACT_URL,
                    attachMap, null);
            log.debug("{} - 邮件已发送", uid);
        } catch (GameApiException e) {
            log.error("uid - {}，预售购买发送道具邮件失败，已入队 - {}", uid, RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST);
            redisUtil.lRightPush(RedisKeyConstant.EMAIL_PACKAGE_FAILED_LIST, new EmailFailedDto(uid, attachMap, type, v6, skinQuery, null, null));
            return new Result<>(ResultCode.SYS_BUSY.getCode(), "当前活动太火爆啦，五分钟之后发放奖励");
        }

        // 记录订购数据（包含未发放的换装装扮、已发放的奖励道具及原装扮
        BuySuccessDto successDto = new BuySuccessDto(uid, type, new Date(), attachMap, newSkinAvatarMap);
        String successTempKey = String.format("%s%s", RedisKeyConstant.BUY_SUCCESS_TEMP, type);
        redisUtil.hPut(successTempKey, Long.toString(uid), successDto);

        // 维持一份是否购买过（包含正式、预售购买)的埋点，用于之后的领取万能骰子操作
        String key = String.format("%s%s", RedisKeyConstant.GET_UNIVERSAL_LIMIT, uid);
        redisUtil.hPut(key, DICE_GET_TYP, new GetDiceDto(DICE_GET_TYP, false, true));

        // 预售礼包延时发放
        redisUtil.zAdd(zSet, JSON.toJSONString(successDto), System.currentTimeMillis());

        // 更新skin查询缓存（不再调用充值服接口去获取皮肤拥有状态，活动这边默认为发送完邮件即已拥有
        if (redisUtil.hHasKey(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid))) {
            final SkinVo result = SkinVo.updateRecord(vo, skinQuery);
            redisUtil.hPut(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid), result);
        }

        // 数据打点(v7: 1 购买预售 2 直接购买
        openLogsClient.openLog(OpenLogsConstant.BUY_ID, Long.toString(uid),
                loginData.getApiId(), loginData.getVer(),
                loginData.getCountry(), loginData.getLangId(),
                v6, 1, null
        );
        log.debug("{} - 预售购买已完成", uid);
        return Result.success(BuySuccessVo.convertTo(attachMap, type));
    }

    /**
     * 礼包购买：前置条件判断
     *
     * <p>
     * 1. 判断是否在有效期<br/>
     * 2. 判断是否拥有原装扮跟换装装扮，有的话不能再次购买<br/>
     * 4. 计算并返回最终价格<br/>
     * </p>
     *
     * @param type 类型( 预售、正式售卖
     * @param vo   皮肤查询缓存
     * @return {@link Result}<{@link Object}>
     * @throws ParseException 解析异常
     */
    private Result<Object> conOfBuy(String type, SkinVo vo) throws ParseException {
        // 预售开始、结束时间
        Date start = null, end = null;
        // 最终价格
        Integer price = null;
        // 是否是购买柳仙儿礼包，如果是 增加一处原装扮的校验
        AtomicBoolean isLiuXianEr = new AtomicBoolean(false);

        switch (type) {
            case FestivalConstant.LIUXIANER_PRESALE_TYPE:
                start = DateUtils.parseDate(FestivalConstant.LIUXIANER_PRESALE_START, "yyyy-MM-dd HH:mm:ss");
                end = DateUtils.parseDate(FestivalConstant.LIUXIANER_PRESALE_END, "yyyy-MM-dd HH:mm:ss");
                price = vo.getLiuXianEr().getPrice();
                isLiuXianEr.getAndSet(true);
                break;
            case FestivalConstant.YUEWUSHUANG_PRESALE_TYPE:
                start = DateUtils.parseDate(FestivalConstant.YUEWUSHUANG_PRESALE_START, "yyyy-MM-dd HH:mm:ss");
                end = DateUtils.parseDate(FestivalConstant.YUEWUSHUANG_PRESALE_END, "yyyy-MM-dd HH:mm:ss");
                price = vo.getYueWuShuang().getPrice();
                break;
            case FestivalConstant.LIUXIANER_OFFICIAL_TYPE:
                start = DateUtils.parseDate(FestivalConstant.LIUXIANER_OFFICIAL_START, "yyyy-MM-dd HH:mm:ss");
                end = DateUtils.parseDate(FestivalConstant.LIUXIANER_OFFICIAL_END, "yyyy-MM-dd HH:mm:ss");
                price = vo.getLiuXianEr().getPrice();
                isLiuXianEr.getAndSet(true);
                break;
            case FestivalConstant.YUEWUSHUANG_OFFICIAL_TYPE:
                start = DateUtils.parseDate(FestivalConstant.YUEWUSHUANG_OFFICIAL_START, "yyyy-MM-dd HH:mm:ss");
                end = DateUtils.parseDate(FestivalConstant.YUEWUSHUANG_OFFICIAL_END, "yyyy-MM-dd HH:mm:ss");
                price = vo.getYueWuShuang().getPrice();
                break;
            default:
        }

        // 是否在有效时间内
        if (!DateUtil.isIn(new Date(), start, end)) {
            return new Result<>(ResultCode.FES_PLEASE_WAIT.getCode(), ResultCode.FES_PLEASE_WAIT.getMsg());
        }

        // （20210903产品需求调整：柳仙儿原装扮不参与售卖）
        if (isLiuXianEr.get()) {
            // 判断是否拥有柳仙儿原装扮
            if (!vo.getLiuXianEr().isHaveOriginFlag())
                return new Result<>(ResultCode.FES_BUY_ORIGIN_FIRST.getCode(), ResultCode.FES_BUY_ORIGIN_FIRST.getMsg());
        }


        // 通过缓存去判断是否拥有礼包内所有装扮（活动这边默认为发送完邮件即已拥有
        if (SkinVo.isAll(vo, type)) {
            // 都拥有 不可购买
            return new Result<>(ResultCode.FES_ALREADY_OWN.getCode(), ResultCode.FES_ALREADY_OWN.getMsg());
        }
        return Result.success(price);
    }
}
