package com.miniw.fesweb.application.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.miniw.fescommon.base.dto.UserLoginData;
import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.base.vo.ResultCode;
import com.miniw.fescommon.constant.*;
import com.miniw.fescommon.utils.RedisUtil;
import com.miniw.fesexternal.client.openlogs.OpenLogsClient;
import com.miniw.fesexternal.client.openlogs.params.OpenLogsConstant;
import com.miniw.fespersistence.model.TAwardRecord;
import com.miniw.fespersistence.service.TAwardRecordService;
import com.miniw.fesweb.application.BoardGameApplication;
import com.miniw.fesweb.params.base.*;
import com.miniw.fesweb.params.dto.BackRandomDto;
import com.miniw.fesweb.params.dto.EmailFailedDto;
import com.miniw.fesweb.params.dto.EventualityDto;
import com.miniw.fesweb.params.dto.GetDiceDto;
import com.miniw.fesweb.params.vo.*;
import com.miniw.gameapi.api.EmailApi;
import com.miniw.gameapi.api.FriendApi;
import com.miniw.gameapi.api.MiniCoinApi;
import com.miniw.gameapi.exception.GameApiException;
import com.miniw.gameapi.pojo.dto.UInfoDTO;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

/**
 * 棋盘游戏相关逻辑
 *
 * @author luoquan
 * @date 2021/08/27
 */
@Slf4j
@Service
public class BoardGameApplicationImpl implements BoardGameApplication {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private EmailApi emailApi;
    @Resource
    private MiniCoinApi miniCoinApi;
    @Resource
    private FriendApi friendApi;
    @Resource
    private Redisson redisson;
    @Resource
    private TAwardRecordService tAwardRecordService;
    @Resource
    private OpenLogsClient openLogsClient;


    /**
     * 使用骰子
     * <p>
     * 1. 优先判断是否携带随机事件（点数翻倍/点数减半）<br/>
     * 2. 普通骰子：根据概率计算步数; <br/>
     *    万能骰子：根据玩家选择的步数走动格子
     * </p>
     *
     * @param boardBase 棋盘base
     * @return {@link Result}<{@link BoardVo}>
     * @throws InterruptedException 中断异常
     */
    @Override
    public Result<BoardVo> useDice(BoardBase boardBase) throws InterruptedException {
        final Long uid = boardBase.getUid();
        final Integer step = boardBase.getStep();
        final Integer eventId = boardBase.getEventId();

        val lockKey = String.format("%s%s", RedisKeyConstant.USE_DICE_LOCK_KEY, uid);
        final RLock lock = redisson.getLock(lockKey);
        if (!lock.tryLock(0, 15, TimeUnit.SECONDS)) {
            log.error("{} - 当前有正在进行的游戏", uid);
            throw new InterruptedException("游戏正在处理，请稍后再试");
        }

        try {
            // 获取缓存中的登录态
            final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
            if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
                return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
            }
            UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);

            if (step != 0) {
                // 判断万能可用骰子点数
                if (redisUtil.hGetInt(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid)) <= 0)
                    return new Result<>(ResultCode.FES_UNIVERSAL_DICE_NON.getCode(), ResultCode.FES_UNIVERSAL_DICE_NON.getMsg());
            } else {
                if (redisUtil.hGetInt(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid)) <= 0)
                    return new Result<>(ResultCode.FES_COMMON_DICE_NON.getCode(), ResultCode.FES_COMMON_DICE_NON.getMsg());
            }

            final BoardVo boardRecord = JSON.parseObject(String.valueOf(redisUtil.hGet(RedisKeyConstant.BOARD_POSITION_RECORD, Long.toString(uid))), BoardVo.class);

            // 先清除之前的由于触发了倒退事件的backBeforePosition值
            boardRecord.setBackBeforePosition(null);

            // 判断缓存中是否携带随机事件2001、2002
            final List<Integer> eventList = Arrays.asList(RandomEventEnum.RANDOM_2_2001.getEventId(), RandomEventEnum.RANDOM_2_2002.getEventId());
            final BoardVo.BoardAvatar.RandomEvent randomEvent = boardRecord.getBoardAvatar().getRandomEvent();
            final Result<BoardVo> boardVoResult;
            if (null != randomEvent && eventList.contains(randomEvent.getEventId())) {
                // 如果携带随机事件且为2001、2002
                boardVoResult = this.handleRandom(uid, step, eventId, boardRecord, randomEvent);
                final BoardVo.BoardAvatar boardAvatar = boardVoResult.getData().getBoardAvatar();
                // 数据打点
                openLogsClient.openLog(OpenLogsConstant.BOARD_AWARD_ID, Long.toString(uid),
                        loginData.getApiId(), loginData.getVer(),
                        loginData.getCountry(), loginData.getLangId(),
                        boardAvatar.getAvatarId(), boardVoResult.getData().getPosition(), null
                );
            } else {
                // 正常投掷骰子
                boardVoResult = this.handleCommon(uid, step, boardRecord);
                final BoardVo.BoardAvatar boardAvatar = boardVoResult.getData().getBoardAvatar();
                // 数据打点
                openLogsClient.openLog(OpenLogsConstant.BOARD_AWARD_ID, Long.toString(uid),
                        loginData.getApiId(), loginData.getVer(),
                        loginData.getCountry(), loginData.getLangId(),
                        boardAvatar.getAvatarId(), boardVoResult.getData().getPosition(), null
                );
            }
            return boardVoResult;
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获奖记录
     *
     * @param recordBase 记录基础
     * @return {@link Result}<{@link AwardRecordVo}>
     */
    @Override
    public Result<AwardRecordVo> list(RecordBase recordBase) {
        final Long uid = recordBase.getUid();
        final Integer pageIndex = recordBase.getPageIndex();
        final Integer pageSize = recordBase.getPageSize();
        //  从db中获取
        TAwardRecord tAwardRecord = new TAwardRecord();
        tAwardRecord.setUid(uid);
        QueryWrapper<TAwardRecord> queryWrapper = new QueryWrapper<>(tAwardRecord);
        queryWrapper.orderByDesc("time");
        IPage<TAwardRecord> page = tAwardRecordService.page(new Page<>(pageIndex, pageSize), queryWrapper);
        List<AwardRecordVo.RecordVo> collect = page.getRecords().stream().map(AwardRecordVo::convert).collect(toList());
        if(CollectionUtils.isEmpty(collect)){
            return Result.success(new AwardRecordVo());
        }
        AwardRecordVo vo = new AwardRecordVo();
        vo.setRecordVoList(collect);
        vo.setTotal(page.getTotal());
        return Result.success(vo);
    }

    /**
     * 领取骰子
     *
     * @param getDiceAwardBase 领取骰子base
     * @return {@link Result}<{@link GetDiceVo}>
     * @throws InterruptedException 中断异常
     */
    @Override
    public Result<GetDiceVo> getDice(GetDiceAwardBase getDiceAwardBase) throws InterruptedException {
        final Long uid = getDiceAwardBase.getUid();
        final String type = getDiceAwardBase.getType();
        final int v6 = GetDiceAwardBase.matchV6(type);

        val lockKey = String.format("%s%s", RedisKeyConstant.GET_COMMON_LOCK_KEY, uid);
        final RLock lock = redisson.getLock(lockKey);
        if (!lock.tryLock(0, 10, TimeUnit.SECONDS)) {
            log.error("{} - 领取正在处理，请耐心等待", uid);
            throw new InterruptedException("领取正在处理，请稍后再试");
        }

        try {
            // 获取缓存中的登录态
            final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
            if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
                return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
            }
            UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);

            String key;
            // 获取普通骰子
            if (GetDiceAwardBase.isGetCommonDice(type)) {
                // 判断今日是否已经领取
                key = String.format("%s%s", RedisKeyConstant.GET_COMMON_LIMIT, uid);

                if (!redisUtil.hHasKey(key, type)) {
                    return new Result<>(ResultCode.SYS_BUSY.getCode(), "请刷新当前页面哦");
                }

                final GetDiceDto diceDto = JSON.parseObject(String.valueOf(redisUtil.hGet(key, type)), GetDiceDto.class);

                if (diceDto.isReceiveFlag()) {
                    return new Result<>(ResultCode.FES_GET_COMMON_LIMIT.getCode(), ResultCode.FES_GET_COMMON_LIMIT.getMsg());
                }

                // 普通骰子+1  添加每日限制
                redisUtil.hIncrement(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid), 1);
                redisUtil.hPutAndEx(key, type, new GetDiceDto(type, true, true), DateUtil.endOfDay(new Date()));

                // 数据打点
                openLogsClient.openLog(OpenLogsConstant.TASK_ID, Long.toString(uid),
                        loginData.getApiId(), loginData.getVer(),
                        loginData.getCountry(), loginData.getLangId(),
                        v6, 2, null
                );

                return Result.success(new GetDiceVo(FestivalConstant.GET_COMMON_DICE_TYPE, 1));
            }

            // 万能骰子（领取前判断是否已购买
            String uKey = String.format("%s%s", RedisKeyConstant.GET_UNIVERSAL_LIMIT, uid);
            if (!redisUtil.hHasKey(uKey, type)) {
                return new Result<>(ResultCode.SYS_BAN.getCode(), "请先去购买礼包哦");
            }

            final GetDiceDto diceDto = JSON.parseObject(String.valueOf(redisUtil.hGet(uKey, type)), GetDiceDto.class);

            if (diceDto.isReceiveFlag()) {
                return new Result<>(ResultCode.FES_GET_UNIVERSAL_LIMIT.getCode(), ResultCode.FES_GET_UNIVERSAL_LIMIT.getMsg());
            }

            // 万能骰子+3 没有过期时间
            redisUtil.hIncrement(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid), 3);
            redisUtil.hPut(uKey, type, new GetDiceDto(type, true, true));
            // 数据打点
            openLogsClient.openLog(OpenLogsConstant.TASK_ID, Long.toString(uid),
                    loginData.getApiId(), loginData.getVer(),
                    loginData.getCountry(), loginData.getLangId(),
                    v6, 2, null);

            return Result.success(new GetDiceVo(FestivalConstant.GET_UNIVERSAL_DICE_TYPE, 3));
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }

    }

    /**
     * 骰子领取详情列表
     *
     * @param diceDetailBase 骰子详情base
     * @return {@link Result}<{@link DiceDetailVo}>
     */
    @Override
    public Result<DiceDetailVo> listDiceDetail(DiceDetailBase diceDetailBase) {
        final Long uid = diceDetailBase.getUid();
        final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
        if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
            return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
        }
        UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);

        // 获取普通骰子任务初始化
        String key = String.format("%s%s", RedisKeyConstant.GET_COMMON_LIMIT, uid);
        if (!redisUtil.hHasKey(key, FestivalConstant.DICE_GET_TYPE_LOGIN)) {
            redisUtil.hPutAndEx(key, FestivalConstant.DICE_GET_TYPE_LOGIN,
                    new GetDiceDto(FestivalConstant.DICE_GET_TYPE_LOGIN, false, true),
                    DateUtil.endOfDay(new Date()));
            // 每日完成登录活动页数据打点
            openLogsClient.openLog(OpenLogsConstant.TASK_ID, Long.toString(uid),
                    loginData.getApiId(), loginData.getVer(),
                    loginData.getCountry(), loginData.getLangId(),
                    1, 1, null
            );
        }
        if (!redisUtil.hHasKey(key, FestivalConstant.DICE_GET_TYPE_INVITE)) {
            redisUtil.hPutAndEx(key, FestivalConstant.DICE_GET_TYPE_INVITE,
                    new GetDiceDto(FestivalConstant.DICE_GET_TYPE_INVITE, false, false),
                    DateUtil.endOfDay(new Date()));
        }
        if (!redisUtil.hHasKey(key, FestivalConstant.DICE_GET_TYPE_ONLINE)) {
            redisUtil.hPutAndEx(key, FestivalConstant.DICE_GET_TYPE_ONLINE,
                    new GetDiceDto(FestivalConstant.DICE_GET_TYPE_ONLINE, false, false),
                    DateUtil.endOfDay(new Date()));
        }

        // 从缓存中获取
        String common = String.format("%s%s", RedisKeyConstant.GET_COMMON_LIMIT, uid);
        String universal = String.format("%s%s", RedisKeyConstant.GET_UNIVERSAL_LIMIT, uid);
        Map<String, Object> result = new HashMap<>(5);
        // 普通骰子
        GetDiceAwardBase.getCtypeList().forEach(type -> {
            GetDiceDto getDiceDto = JSON.parseObject(String.valueOf(redisUtil.hGet(common, type)), GetDiceDto.class);

            // 如果任务已完成 且 未领取
            if (getDiceDto.isCompleteFlag() && !getDiceDto.isReceiveFlag()) {
                result.put(getDiceDto.getType(), false);
            }

            // 如果任务未完成
            if (!getDiceDto.isCompleteFlag() && !getDiceDto.isReceiveFlag()) {
                result.put(getDiceDto.getType(), "unComplete");
            }

            // 如果任务已完成 且 已领取
            if (getDiceDto.isCompleteFlag() && getDiceDto.isReceiveFlag()) {
                result.put(getDiceDto.getType(), getDiceDto.isReceiveFlag());
            }

        });

        // 万能骰子
        GetDiceAwardBase.getUtypeList().forEach(type -> {
            GetDiceDto getDiceDto = JSON.parseObject(String.valueOf(redisUtil.hGet(universal, type)), GetDiceDto.class);

            if (getDiceDto == null) {
                // 如果未购买
                result.put(type, "unbuy");
            } else {
                result.put(getDiceDto.getType(), getDiceDto.isReceiveFlag());
            }
        });
        return Result.success(new DiceDetailVo(result));
    }

    /**
     * 购买万能骰子
     *
     * @param buyUniversalBase 购买万能骰子base
     * @return {@link Result}<{@link BuyUniversalVo}>
     * @throws GameApiException     游戏api异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public Result<BuyUniversalVo> buyUniversal(BuyUniversalBase buyUniversalBase) throws GameApiException, InterruptedException {
        final Long uid = buyUniversalBase.getUid();
        final Integer diceNum = buyUniversalBase.getNum();
        final Integer price = buyUniversalBase.getPrice();
        final Integer v6 = BuyUniversalBase.matchV6(diceNum);
        val lockKey = String.format("%s%s", RedisKeyConstant.BUY_UNIVERSAL_LOCK_KEY, uid);
        final RLock lock = redisson.getLock(lockKey);
        if (!lock.tryLock(0, 10, TimeUnit.SECONDS)) {
            log.error("{} - 购买万能骰子正在处理，请耐心等待", uid);
            throw new InterruptedException("购买万能骰子正在处理，请稍后再试");
        }

        try {
            final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
            if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
                return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
            }
            UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);

            // 检测库存
            final Integer num = miniCoinApi.queryMiniCoin(uid);
            if (num < price) {
                // 迷你币不足
                return new Result<>(ResultCode.FES_COIN_NOT_ENOUGH.getCode(), ResultCode.FES_COIN_NOT_ENOUGH.getMsg());
            }

            // 扣除迷你币
            miniCoinApi.consumeMiniCoin(uid, price, FestivalConstant.COUSUME_COIN_WHY_DICE, null);

            // 发放万能骰子
            redisUtil.hIncrement(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid), diceNum);

            // 数据打点
            openLogsClient.openLog(OpenLogsConstant.BUY_DICE_ID, Long.toString(uid),
                    loginData.getApiId(), loginData.getVer(),
                    loginData.getCountry(), loginData.getLangId(),
                    v6, null, null
            );

            return Result.success(new BuyUniversalVo(diceNum));
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取好友列表
     *
     * @param getFriendsBase 获取好友base
     * @return {@link Result}<{@link UInfoDTO}>
     */
    @Override
    public Result<List<UInfoDTO>> getFriends(GetFriendsBase getFriendsBase) {
        final Long uid = getFriendsBase.getUid();
        try {
            final List<UInfoDTO> friends = friendApi.getFriends(uid);
            return Result.success(friends);
        } catch (GameApiException e) {
            log.error("{} - 获取好友列表失败 - {}", uid, e.getMessage());
            return new Result<>(ResultCode.SYS_BUSY.getCode(), e.getMessage());
        }
    }

    /**
     * 分享给好友
     *
     * @param shareFriendsBase 分享朋友base
     * @return {@link Result}<{@link Boolean}>
     */
    @Override
    public Result<Boolean> shareWithFriends(ShareFriendsBase shareFriendsBase) {
        final Collection<Long> fIds = shareFriendsBase.getFIds();
        final Long uid = shareFriendsBase.getUid();
        // 获取缓存中的登录态
        final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
        if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
            return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
        }
        UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);

        try {
            friendApi.shareFriend(uid, fIds, SendEmailConstant.SHARE_IMG, SendEmailConstant.SHARE_TITLE, SendEmailConstant.SHARE_CONTENT, SendEmailConstant.SHARE_ACT_URL);
            // 将任务状态标记为已完成未领取
            String key = String.format("%s%s", RedisKeyConstant.GET_COMMON_LIMIT, uid);
            redisUtil.hPutAndEx(key, FestivalConstant.DICE_GET_TYPE_INVITE,
                    new GetDiceDto(FestivalConstant.DICE_GET_TYPE_INVITE, false, true),
                    DateUtil.endOfDay(new Date()));

            // 数据打点
            openLogsClient.openLog(OpenLogsConstant.TASK_ID, Long.toString(uid),
                    loginData.getApiId(), loginData.getVer(),
                    loginData.getCountry(), loginData.getLangId(),
                    2, 1, null
            );

//            // 普通骰子+1
//            redisUtil.hIncrement(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid), 1);
        } catch (GameApiException e) {
            log.error("{} - 分享给好友- {}失败 - {}", uid, fIds, e.getMessage());
            return new Result<>(ResultCode.SYS_BUSY.getCode(), e.getMessage());
        }
        return Result.success(true);
    }

    /**
     * 分享任务回调
     *
     * @param callBackBase 回调base
     * @return {@link Result}<{@link Boolean}>
     */
    @Override
    public Result<Boolean> taskCallBack(CallBackBase callBackBase) {
        final Long uid = callBackBase.getUid();
        final String type = callBackBase.getType();
        final int v6 = CallBackBase.matchV6(type);
        // 获取缓存中的登录态
        final Object o = redisUtil.hGet(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid));
        if (!redisUtil.hHasKey(RedisKeyConstant.USER_LOGIN_DATA, Long.toString(uid)) || null == o) {
            return new Result<>(ResultCode.SYS_NEED_LOGIN.getCode(), "无法获取该迷你号的登录信息");
        }
        UserLoginData loginData = JSONObject.parseObject(String.valueOf(o), UserLoginData.class);

        // 添加任务完成状态
        String key = String.format("%s%s", RedisKeyConstant.GET_COMMON_LIMIT, uid);
        // wx qq回调统一处理为invite
        final String convert = CallBackBase.convertToInvite(type);

        redisUtil.hPutAndEx(key, convert,
                new GetDiceDto(convert, false, true),
                DateUtil.endOfDay(new Date()));
        // 数据打点
        openLogsClient.openLog(OpenLogsConstant.TASK_ID, Long.toString(uid),
                loginData.getApiId(), loginData.getVer(),
                loginData.getCountry(), loginData.getLangId(),
                v6, 1, null
        );
        return Result.success(true);
    }


    /**
     * 处理正常骰子
     *
     * @param uid         uid
     * @param step        万能骰子步数
     * @param boardRecord 棋盘记录
     */
    private Result<BoardVo> handleCommon(Long uid, Integer step, BoardVo boardRecord) {
        // 如果是万能骰子，则直接计算结果
        AtomicBoolean isUniversalDice = new AtomicBoolean(false);
        if(step != 0){
            isUniversalDice.getAndSet(true);
        }

        // 初始骰子点数
        AtomicInteger faceValue = new AtomicInteger();
        final Integer position = boardRecord.getPosition();
        if (isUniversalDice.get()) {
            //  万能骰子直接进行步数赋值
            faceValue.getAndSet(step);
        } else {
//            //  普通骰子
//            faceValue = DiceDto.getInstance().getFaceValue();
//            final int i = new Random().nextInt((6 - 1) + 1) + 1;
//            faceValue.getAndSet(i);
            faceValue.getAndSet(EventualityDto.countAward(position, null));
            if (faceValue.get() < 0)
                faceValue.getAndAdd(20);
        }
        boardRecord.setDiceNum(faceValue.get());

        log.debug("{} - 当前摇骰子结果 - {}", uid, faceValue.get());
        AtomicInteger diceNum = new AtomicInteger();
        // 计算出最后棋盘落点位置(上一次的落点位置 + 骰子的最终结果
        diceNum.getAndAdd(position);
        log.debug("{} - 上次落点位置 - {}", uid, diceNum.get());
        diceNum.getAndAdd(faceValue.get());
        log.debug("{} - 最终结果 - {}", uid, diceNum.get());

        // 实际落点位置为 i % n
        final int i = diceNum.get();
        int actStep = i % 21;
        log.debug("{} - 棋盘实际落点位置为 - {}", uid, actStep);
        //  重回起点标识
        AtomicBoolean returnStartFlag = new AtomicBoolean(false);
        if (i >= 21 && actStep >= 0) {
            log.debug("{} - 经过了起点！", uid);
            returnStartFlag.getAndSet(true);
            actStep += 1;
        }
        boardRecord.setPosition(actStep == 0 ? 1 : actStep);

        // 根据最后落点位置匹配奖励（重回起点需要额外奖励月兔印章*5
        final BoardVo.BoardAvatar boardAvatar = BoardVo.matchAward(actStep, returnStartFlag.get());
        boardRecord.setBoardAvatar(boardAvatar);

        // resultIsRandom这个标识如果触发了 则不发放任何奖励
        AtomicBoolean resultIsRandom = new AtomicBoolean(false);
        // 处理随机事件中包含倒退、重回起点的问题
        if (null != boardAvatar.getRandomEvent()) {
            BackRandomDto randomDto = this.resultIsRandomEvent(actStep, boardAvatar, uid);
            // 如果真的触发了倒退事件
            if (null != randomDto) {
                resultIsRandom.getAndSet(true);
                // 记录回退事件之后的位置
                boardRecord.setPosition(randomDto.getActStep());
                // 记录回退之前的位置
                boardRecord.setBackBeforePosition(actStep);
            }
        }

        // 更新缓存
        redisUtil.hPut(RedisKeyConstant.BOARD_POSITION_RECORD, Long.toString(uid), boardRecord);

        // 扣减骰子数量
        if (isUniversalDice.get()) {
            // 万能骰子-1
            redisUtil.hIncrement(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid), -1);
        } else {
            redisUtil.hIncrement(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid), -1);
        }

        // 如果触发过倒退事件，则没有任何奖励
        if (resultIsRandom.get()) {
            return Result.success(boardRecord);
        }

        //   处理重回起点奖励
        if (boardAvatar.getReturnStartAvatar() != null) {
            log.debug("{} -  重回起点奖励发放", uid);
            redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, Long.toString(uid), boardAvatar.getReturnStartAvatar().getAvatarNum());
        }


        // 处理奖励发放（筛选出月兔印章奖励、万能骰子、普通骰子
        if (boardAvatar.getAvatarId().equals(FestivalConstant.RABBIT_SEAL_AVATAR_ID)) {
            // 如果存在月兔印章奖励，更改缓存中的印章数量
            redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, Long.toString(uid), boardAvatar.getAvatarNum());
        } else if (boardAvatar.getAvatarId().equals(FestivalConstant.UNIVERSAL_DICE_AVATAR)) {
            // 如果万能骰子奖励，更改缓存中的数量
            redisUtil.hIncrement(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid), boardAvatar.getAvatarNum());
        } else if (boardAvatar.getAvatarId().equals(FestivalConstant.COMMON_DICE_AVATAR)) {
            // 如果普通骰子奖励，更改缓存中的数量
            redisUtil.hIncrement(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid), boardAvatar.getAvatarNum());
        } else if (!boardAvatar.getAvatarId().equals(BoardGameEnum.BOARD_15.getAvatarId())) {
            // 剔除掉随机事件格子本带的道具id（比如avatarId = 3 其实不属于道具发放范畴
            log.debug("{} - 走格子发放奖励邮件", uid);
            Map<Integer, Integer> attachMap = new HashMap<>(1);
            attachMap.put(boardAvatar.getAvatarId(), boardAvatar.getAvatarNum());
            try {
                emailApi.sendEmail(uid, SendEmailConstant.YUELEYUAN_EMAIL_TITLE,
                        SendEmailConstant.YUELEYUAN_EMAIL_BODY,
                        SendEmailConstant.YUELEYUAN_EMAIL_ACT_URL,
                        attachMap, null);
                log.debug("{} - 走格子发放奖励邮件成功", uid);
            } catch (GameApiException e) {
                log.error("uid - {}，漫步月乐园发送道具邮件失败 - {}，已入队 - {}", uid, e.getMessage(), RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST);
                redisUtil.lRightPush(RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST, new EmailFailedDto(uid, attachMap, null, 0, null, boardAvatar.getAvatarName(), boardAvatar.getAvatarNum()));
                return new Result<>(ResultCode.FES_SEND_EMAIL_FAILED.getCode(), ResultCode.FES_SEND_EMAIL_FAILED.getMsg());
            }
        }


        //  记录奖励
        TAwardRecord tAwardRecord = new TAwardRecord();
        tAwardRecord.setUid(uid);
        tAwardRecord.setContent(String.format("%s*%s", boardAvatar.getAvatarName(), boardAvatar.getAvatarNum()));
        // 随机事件2当中的奖励内容特殊处理一下
        if (null != boardAvatar.getRandomEvent()) {
            if (boardAvatar.getRandomEvent().getAvatarName() != null && boardAvatar.getRandomEvent().getAvatarId() != null) {
                tAwardRecord.setContent(String.format("%s*%s", boardAvatar.getRandomEvent().getAvatarName(), boardAvatar.getRandomEvent().getAvatarNum()));
            }
        }
        tAwardRecord.setTime(DateUtil.format(new Date(), "MM-dd HH:mm:ss"));
        tAwardRecordService.save(tAwardRecord);

        return Result.success(boardRecord);
    }


    /**
     * 处理携带随机事件的骰子
     *
     * @param uid         uid
     * @param boardRecord 棋盘记录
     * @param eventId     前端给到的事件id
     * @param randomEvent 随机事件缓存
     * @param step        万能骰子步数
     * @return {@link Result}<{@link BoardVo}>
     */
    private Result<BoardVo> handleRandom(Long uid, Integer step, Integer eventId, BoardVo boardRecord, BoardVo.BoardAvatar.RandomEvent randomEvent) {
        if(0 == eventId){
            // 如果前端给出的eventId为0
            return new Result<>(ResultCode.SYS_BAN.getCode(), "禁止操作，未匹配到随机事件");
        }

        if(!eventId.equals(randomEvent.getEventId())){
            // 如果与缓存中的随机事件不匹配
            return new Result<>(ResultCode.FES_RANDOM_MISMATCH.getCode(), ResultCode.FES_RANDOM_MISMATCH.getMsg());
        }

        // 如果是万能骰子，则直接进行随机事件处理
        AtomicBoolean isUniversalDice = new AtomicBoolean(false);
        if (step != 0) {
            isUniversalDice.getAndSet(true);
        }

        // 初始骰子点数
        AtomicInteger faceValue = new AtomicInteger();
        final Integer position = boardRecord.getPosition();


        if (isUniversalDice.get()) {
            //  万能骰子直接进行步数赋值
            faceValue.getAndSet(step);
        } else {
            //  普通骰子
//            faceValue = DiceDto.getInstance().getFaceValue();
//            final int i = new Random().nextInt((6 - 1) + 1) + 1;
//            faceValue.getAndSet(i);
            // 根据概率匹配奖品并计算出最终结果
            faceValue.getAndSet(EventualityDto.countAward(position, eventId));
            if (faceValue.get() < 0)
                faceValue.getAndAdd(20);
        }

        boardRecord.setDiceNum(faceValue.get());

        // 计算出最后棋盘落点位置(上一次的落点位置 + 骰子的最终结果
        AtomicInteger diceNum = new AtomicInteger();

        if (eventId.compareTo(RandomEventEnum.RANDOM_2_2001.getEventId()) == 0) {
            // 点数翻倍
            diceNum.getAndSet(faceValue.get() * 2);
        } else if (eventId.compareTo(RandomEventEnum.RANDOM_2_2002.getEventId()) == 0) {
            // 点数减半（向上取整数
            diceNum.getAndSet((int) Math.ceil(faceValue.doubleValue() / 2));
        }
        diceNum.getAndAdd(position);

        final int finalStep = diceNum.get();

        // 实际落点位置为 i % n
        int actStep = finalStep % 21;

        //  重回起点标识
        AtomicBoolean returnStartFlag = new AtomicBoolean(false);
        if (finalStep >= 21 && actStep >= 0) {
            log.debug("{} - 经过了起点！", uid);
            returnStartFlag.getAndSet(true);
            actStep += 1;
        }
        boardRecord.setPosition(actStep == 0 ? 1 : actStep);
        log.debug("{} - 棋盘实际落点位置为 - {}", uid, boardRecord.getPosition());
        // 根据最后落点位置匹配奖励（重回起点需要额外奖励月兔印章*5
        final BoardVo.BoardAvatar boardAvatar = BoardVo.matchAward(actStep, returnStartFlag.get());
        boardRecord.setBoardAvatar(boardAvatar);

        // resultIsRandom这个标识如果触发了 则不发放任何奖励
        AtomicBoolean resultIsRandom = new AtomicBoolean(false);
        // 处理随机事件中包含倒退、重回起点的问题
        if (null != boardAvatar.getRandomEvent()) {
            BackRandomDto randomDto = this.resultIsRandomEvent(actStep, boardAvatar, uid);
            // 如果真的触发了倒退事件
            if (null != randomDto) {
                resultIsRandom.getAndSet(true);
                // 记录回退事件之后的位置
                boardRecord.setPosition(randomDto.getActStep());
                // 记录回退之前的位置
                boardRecord.setBackBeforePosition(actStep);
            }
        }

        // 更新缓存
        redisUtil.hPut(RedisKeyConstant.BOARD_POSITION_RECORD, Long.toString(uid), boardRecord);

        // 扣减骰子数量
        if (isUniversalDice.get()) {
            // 万能骰子-1
            redisUtil.hIncrement(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid), -1);
        } else {
            redisUtil.hIncrement(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid), -1);
        }

        // 如果触发过倒退事件，则没有任何奖励
        if (resultIsRandom.get()) {
            return Result.success(boardRecord);
        }

        //   处理重回起点奖励
        if (boardAvatar.getReturnStartAvatar() != null) {
            log.debug("{} -  重回起点奖励发放", uid);
            redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, Long.toString(uid), boardAvatar.getReturnStartAvatar().getAvatarNum());
        }

        // 处理奖励发放（筛选出月兔印章奖励、万能骰子、普通骰子
        if (boardAvatar.getAvatarId().equals(FestivalConstant.RABBIT_SEAL_AVATAR_ID)) {
            // 如果存在月兔印章奖励，更改缓存中的印章数量
            redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, Long.toString(uid), boardAvatar.getAvatarNum());
        } else if (boardAvatar.getAvatarId().equals(FestivalConstant.UNIVERSAL_DICE_AVATAR)) {
            // 如果万能骰子奖励，更改缓存中的数量
            redisUtil.hIncrement(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid), boardAvatar.getAvatarNum());
        } else if (boardAvatar.getAvatarId().equals(FestivalConstant.COMMON_DICE_AVATAR)) {
            // 如果普通骰子奖励，更改缓存中的数量
            redisUtil.hIncrement(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid), boardAvatar.getAvatarNum());
        } else {
            log.debug("{} - 走格子发放奖励邮件", uid);
            Map<Integer, Integer> attachMap = new HashMap<>(1);
            attachMap.put(boardAvatar.getAvatarId(), boardAvatar.getAvatarNum());
            try {
                emailApi.sendEmail(uid, SendEmailConstant.YUELEYUAN_EMAIL_TITLE,
                        SendEmailConstant.YUELEYUAN_EMAIL_BODY,
                        SendEmailConstant.YUELEYUAN_EMAIL_ACT_URL,
                        attachMap, null);
                log.debug("{} - 走格子发放奖励邮件成功", uid);
            } catch (GameApiException e) {
                log.error("uid - {}，漫步月乐园发送道具邮件失败 - {}，已入队 - {}", uid, e.getMessage(), RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST);
                redisUtil.lRightPush(RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST, new EmailFailedDto(uid, attachMap, null, 0, null, boardAvatar.getAvatarName(), boardAvatar.getAvatarNum()));
                return new Result<>(ResultCode.FES_SEND_EMAIL_FAILED.getCode(), ResultCode.FES_SEND_EMAIL_FAILED.getMsg());
            }
        }

        //  记录奖励
        TAwardRecord tAwardRecord = new TAwardRecord();
        tAwardRecord.setUid(uid);
        tAwardRecord.setContent(String.format("%s*%s", boardAvatar.getAvatarName(), boardAvatar.getAvatarNum()));
        // 随机事件2当中的奖励内容特殊处理一下
        if (null != boardAvatar.getRandomEvent()) {
            if (boardAvatar.getRandomEvent().getAvatarName() != null && boardAvatar.getRandomEvent().getAvatarId() != null) {
                tAwardRecord.setContent(String.format("%s*%s", boardAvatar.getRandomEvent().getAvatarName(), boardAvatar.getRandomEvent().getAvatarNum()));
            }
        }

        tAwardRecord.setTime(DateUtil.format(new Date(), "MM-dd HH:mm:ss"));
        tAwardRecordService.save(tAwardRecord);

        return Result.success(boardRecord);
    }


    /**
     * 结果是随机事件
     *
     * @param boardAvatar 已匹配棋盘奖励列表
     * @param actStep     实际棋盘落点位置
     * @param uid         uid
     * @return {@link BackRandomDto}
     */
    private BackRandomDto resultIsRandomEvent(int actStep, BoardVo.BoardAvatar boardAvatar, Long uid) {
        BackRandomDto randomDto = new BackRandomDto();

        // 已经根据摇骰子匹配之后的随机事件结果
        final BoardVo.BoardAvatar.RandomEvent randomEvent = boardAvatar.getRandomEvent();
        //  随机事件1中的所有倒退事件id
        final List<Integer> backEventIdList = Arrays.asList(
                RandomEventEnum.RANDOM_1_1006.getEventId(),
                RandomEventEnum.RANDOM_1_1005.getEventId(),
                RandomEventEnum.RANDOM_1_1004.getEventId(),
                RandomEventEnum.RANDOM_1_1003.getEventId(),
                RandomEventEnum.RANDOM_1_1002.getEventId(),
                RandomEventEnum.RANDOM_1_1001.getEventId()
        );

        // returnStartFlag：是否倒退回起点
        final Integer eventId = randomEvent.getEventId();
        AtomicBoolean returnStartFlag = new AtomicBoolean(false);

        // 如果包含随机事件2中的月兔印章、迷你豆两个事件的话，进行发放
        if (eventId.equals(RandomEventEnum.RANDOM_2_2004.getEventId()) ||
                eventId.equals(RandomEventEnum.RANDOM_2_2005.getEventId())) {
            redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, Long.toString(uid), randomEvent.getAvatarNum());
        }

        if (eventId.equals(RandomEventEnum.RANDOM_2_2006.getEventId())) {
            log.debug("{} - 走格子发放奖励邮件", uid);
            Map<Integer, Integer> attachMap = new HashMap<>(1);
            attachMap.put(randomEvent.getAvatarId(), randomEvent.getAvatarNum());
            try {
                emailApi.sendEmail(uid, SendEmailConstant.YUELEYUAN_EMAIL_TITLE,
                        SendEmailConstant.YUELEYUAN_EMAIL_BODY,
                        SendEmailConstant.YUELEYUAN_EMAIL_ACT_URL,
                        attachMap, null);
                log.debug("{} - 走格子发放奖励邮件成功", uid);
            } catch (GameApiException e) {
                log.error("uid - {}，漫步月乐园发送道具邮件失败 - {}，已入队 - {}", uid, e.getMessage(), RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST);
                redisUtil.lRightPush(RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST, new EmailFailedDto(uid, attachMap, null, 0, null, boardAvatar.getAvatarName(), boardAvatar.getAvatarNum()));
            }
        }

        // 如果包含倒退事件
        if (backEventIdList.contains(eventId)) {
            int backNum = RandomEventEnum.returnBackStep(eventId);
            //  实际倒退落点位置
            actStep -= backNum;
            log.debug("倒退了 {} 步，最终结果为 {}", backNum, actStep);
            if (actStep < 0) {
                // 可能会存在负数的情况，此处进行处理
                actStep += 21;
                log.debug("负数规正，最终结果为 {}", actStep);
            }
            if (actStep == 1) {
                // 倒退回了起点 不予发放月兔奖章
                returnStartFlag.getAndSet(true);
                log.debug("退回了起点，记录flag {}", returnStartFlag.get());
            }
            randomDto.setActStep(actStep);
            randomDto.setReturnStartFlag(returnStartFlag.get());
            return randomDto;
        }

        if (eventId.equals(RandomEventEnum.RANDOM_2_2003.getEventId())) {
            returnStartFlag.getAndSet(true);
            // 如果触发了直接回到起点 直接将落点位置置为1
            randomDto.setActStep(1);
            randomDto.setReturnStartFlag(returnStartFlag.get());
            return randomDto;
        }

        return null;
    }

}
