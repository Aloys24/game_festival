package com.miniw.fesweb.application.impl;

import com.alibaba.fastjson.JSON;
import com.miniw.fescommon.base.exception.FestivalException;
import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.base.vo.ResultCode;
import com.miniw.fescommon.constant.RedisKeyConstant;
import com.miniw.fescommon.constant.SendEmailConstant;
import com.miniw.fescommon.utils.RedisUtil;
import com.miniw.fesexternal.client.openlogs.OpenLogsClient;
import com.miniw.fesweb.application.SealApplication;
import com.miniw.fesweb.params.base.MoreAwardBase;
import com.miniw.fesweb.params.base.ReceiveAwardBase;
import com.miniw.fesweb.params.base.SealHomeBase;
import com.miniw.fesweb.params.dto.EmailFailedDto;
import com.miniw.fesweb.params.dto.MyAttachMap;
import com.miniw.fesweb.params.vo.BoardVo;
import com.miniw.fesweb.params.vo.ReceiveAwardVo;
import com.miniw.fesweb.params.vo.SealAwardVo;
import com.miniw.fesweb.params.vo.SealHomeVo;
import com.miniw.gameapi.api.EmailApi;
import com.miniw.gameapi.exception.GameApiException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 月兔印章
 *
 * @author luoquan
 * @date 2021/08/25
 */
@Slf4j
@Service
public class SealApplicationImpl implements SealApplication {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private EmailApi emailApi;
    @Resource
    private Redisson redisson;
    @Resource
    private OpenLogsClient openLogsClient;


    /**
     * 获得更多奖励
     *
     * @param moreAwardBase 印章base
     * @return {@link Result}<{@link SealAwardVo}>
     */
    @Override
    public Result<SealAwardVo> getMoreAward(MoreAwardBase moreAwardBase) {
        final Long uid = moreAwardBase.getUid();

        // 从缓存中获取总印章数
        if (!redisUtil.hHasKey(RedisKeyConstant.RABBIT_SEAL_COUNT, String.valueOf(uid))) {
            redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, String.valueOf(uid), 0);
        }

        SealAwardVo vo;
//        // 尝试从缓存中获取
//        if (!redisUtil.hHasKey(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid))) {
//            // 初始化奖励队列
//            vo = SealAwardVo.getInstance();
////            throw new FestivalException("服务器繁忙，请重新进入棋盘首页", ResultCode.SYS_BUSY.getCode());
//        }
        final Object record = redisUtil.hGet(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid));
        vo = JSON.parseObject(String.valueOf(record), SealAwardVo.class);
        vo.getAwardList().sort(Comparator.comparing(SealAwardVo.SealAward::getSealNum));

        // 在此处进行统计当前阶段达成及领取人数的数据打点
        vo.getAwardList().forEach(sealAward -> {
            if (sealAward.isReachFlag() && !sealAward.isOpenLogFlag1()) {
                redisUtil.hIncrement(RedisKeyConstant.SEAL_NUM_REACH_COUNT, Long.toString(sealAward.getSealNum()), 1);
                sealAward.setOpenLogFlag1(true);
            }

            if (sealAward.isReceiveFlag() && !sealAward.isOpenLogFlag2()) {
                redisUtil.hIncrement(RedisKeyConstant.SEAL_NUM_RECEIVE_COUNT, Long.toString(sealAward.getSealNum()), 1);
                sealAward.setOpenLogFlag2(true);
            }
            // 更新打点标识
            redisUtil.hPut(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid), vo);
        });
        return Result.success(vo);
    }

    /**
     * 领取印章奖励
     *
     * @param receiveAwardBase 领取奖励base
     * @return {@link Result}<{@link ReceiveAwardVo}>
     */
    @Override
    public Result<ReceiveAwardVo> receiveAward(ReceiveAwardBase receiveAwardBase) throws InterruptedException {
        ReceiveAwardVo receiveAwardVo = new ReceiveAwardVo();
        final Long uid = receiveAwardBase.getUid();
        final Integer num = receiveAwardBase.getNum();

        val lockKey = String.format("%s%s", RedisKeyConstant.SEAL_AWARD_LOCK_KEY, uid);
        final RLock lock = redisson.getLock(lockKey);
        if (!lock.tryLock(0, 15, TimeUnit.SECONDS)) {
            log.error("迷你号{}领取印章奖励正在处理", uid);
            throw new InterruptedException("领取正在处理，请稍后再试");
        }

        try {
            // 获取当前节点值缓存
            final Object o = redisUtil.hGet(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid));
            if(null == o){
                throw new FestivalException("服务器繁忙，请重新进入该页面", ResultCode.SYS_BUSY.getCode());
            }
            final SealAwardVo vo = JSON.parseObject(String.valueOf(o), SealAwardVo.class);

            final List<ReceiveAwardVo.ReceiveResult> results;
            if(num == 0) {
                // 批量领取
                results = this.batchReceive(uid, vo);
                if (CollectionUtils.isEmpty(results)) {
                    return new Result<>(ResultCode.SYS_DISCONTENT.getCode(), "暂无可领取奖励");
                }
            } else {
                // 单个节点值奖励领取
                results = this.singleReceive(uid, num, vo);
                if (CollectionUtils.isEmpty(results)) {
                    return new Result<>(ResultCode.SYS_DISCONTENT.getCode(), "当前节点尚未达成");
                }
            }

            receiveAwardVo.setAwardList(results);
            return Result.success(receiveAwardVo);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取首页数据
     *
     * @param sealHomeBase 首页base
     * @return {@link Result}<{@link SealHomeVo}>
     */
    @Override
    public Result<SealHomeVo> home(SealHomeBase sealHomeBase) {
        final Long uid = sealHomeBase.getUid();
        // 尝试从缓存中获取
        if (!redisUtil.hHasKey(RedisKeyConstant.RABBIT_SEAL_COUNT, String.valueOf(uid))) {
            redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, String.valueOf(uid), 0);
        }

        // 印章记录 不存在则进行初始化
        SealAwardVo vo;
        if (redisUtil.hHasKey(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid))) {
            final Object o = redisUtil.hGet(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid));
            vo = JSON.parseObject(String.valueOf(o), SealAwardVo.class);
        } else {
            // 初始化奖励队列
            vo = SealAwardVo.getInstance();
        }

        // 根据已经收集的印章数更新各节点值情况
        final int alreadyCount = redisUtil.hGetInt(RedisKeyConstant.RABBIT_SEAL_COUNT, String.valueOf(uid));
        SealAwardVo.matchSealNum(alreadyCount, vo);
        vo.setAlreadyCount(alreadyCount);
        redisUtil.hPut(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid), vo);

        // 筛选出距离当前收集数最近的节点值
        SealHomeVo sealHomeVo = SealHomeVo.matchRecent(alreadyCount, vo.getAwardList());

        // 尝试从缓存中获取玩家棋盘位置记录
        if(!redisUtil.hHasKey(RedisKeyConstant.BOARD_POSITION_RECORD, Long.toString(uid))){
            // 初始化玩家棋盘位置记录
            redisUtil.hPut(RedisKeyConstant.BOARD_POSITION_RECORD, Long.toString(uid), BoardVo.getInstance());
        }
        final BoardVo boardRecord = JSON.parseObject(String.valueOf(redisUtil.hGet(RedisKeyConstant.BOARD_POSITION_RECORD, Long.toString(uid))), BoardVo.class);

        // 尝试从缓存中获取普通骰子数量
        if(!redisUtil.hHasKey(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid))){
            // 初始化玩家骰子数量为0
            redisUtil.hIncrement(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid), 0);
        }
        final Integer commonDiceNum = Integer.valueOf(String.valueOf(redisUtil.hGet(RedisKeyConstant.COMMON_DICE_NUM, Long.toString(uid))));

        // 尝试从缓存中获取万能骰子数量
        if(!redisUtil.hHasKey(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid))){
            // 初始化玩家骰子数量为0
            redisUtil.hIncrement(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid), 0);
        }
        final Integer universalDiceNum = Integer.valueOf(String.valueOf(redisUtil.hGet(RedisKeyConstant.UNIVERSAL_DICE_NUM, Long.toString(uid))));

        // 填充属性
        final SealHomeVo homeVo = SealHomeVo.addBoard(commonDiceNum, universalDiceNum, sealHomeVo, boardRecord);

        return Result.success(homeVo);
    }

    /**
     * 是否存在未领取的节点奖励
     *
     * @param sealHomeBase 首页base
     * @return {@link Result}<{@link Boolean}>
     */
    @Override
    public Result<Boolean> unReceive(SealHomeBase sealHomeBase) {
        final Long uid = sealHomeBase.getUid();
        // 印章记录 不存在则进行初始化
        SealAwardVo sealAwardVo;
        if (redisUtil.hHasKey(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid))) {
            final Object o = redisUtil.hGet(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid));
            sealAwardVo = JSON.parseObject(String.valueOf(o), SealAwardVo.class);
        } else {
            // 初始化奖励队列
            sealAwardVo = SealAwardVo.getInstance();
        }

        final List<SealAwardVo.SealAward> awardList = sealAwardVo.getAwardList();
        if (CollectionUtils.isEmpty(awardList)) {
            return new Result<>(ResultCode.SYS_NON_EXIST.getCode(), "暂未查询到该用户奖励列表记录，请重新登录");
        }
        // 所有已达到未领取的
        final List<SealAwardVo.SealAward> collect = awardList.stream().filter(sealAward -> !sealAward.isReceiveFlag() && sealAward.isReachFlag()).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return Result.success(false);
        }
        return Result.success(true);
    }

    /**
     * 批量领取印章奖励
     *
     * @param uid 迷你号
     * @param vo  当前缓存
     * @return {@link List}<{@link ReceiveAwardVo.ReceiveResult}>
     */
    private List<ReceiveAwardVo.ReceiveResult> batchReceive(Long uid, SealAwardVo vo) {
        // 筛选出当前已经达到且未领取的节点奖励列表
        final List<SealAwardVo.SealAward> awardList = vo.getAwardList();
        final List<SealAwardVo.SealAward> matchList = awardList.stream().filter(
                sealAward -> sealAward.isReachFlag() && !sealAward.isReceiveFlag()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(matchList)) {
            return new ArrayList<>();
        }

        List<ReceiveAwardVo.ReceiveResult> voList = new ArrayList<>(matchList.size());
        awardList.removeAll(matchList);

        // 汇总道具
//        Map<Integer, Integer> attachMap = new HashMap<>(matchList.size());
        MyAttachMap attachMap = new MyAttachMap();
        matchList.forEach(sealAward -> attachMap.put(sealAward.getAvatarId(), sealAward.getAvatarNum()));

        // 发放道具
        try {
            emailApi.sendEmail(uid, SendEmailConstant.YUELEYUAN_EMAIL_TITLE,
                    SendEmailConstant.YUELEYUAN_EMAIL_BODY,
                    SendEmailConstant.YUELEYUAN_EMAIL_ACT_URL,
                    attachMap, null);
        } catch (GameApiException e) {
            log.error("uid - {}，领取印章奖励邮件失败，已入队 - {}", uid, RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST);
            redisUtil.lRightPush(RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST, new EmailFailedDto(uid, attachMap, null, 0, null, null, null));
        }

        matchList.forEach(sealAward -> {
            ReceiveAwardVo.ReceiveResult receiveResult = new ReceiveAwardVo.ReceiveResult();
            // 更改节点值为已领取
            sealAward.setReceiveFlag(true);
            awardList.add(sealAward);
            vo.setAwardList(awardList);
            redisUtil.hPut(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid), vo);
            receiveResult.setAvatarId(sealAward.getAvatarId());
            receiveResult.setAvatarName(sealAward.getAvatarName());
            receiveResult.setNum(sealAward.getAvatarNum());
            voList.add(receiveResult);
        });
        return voList;
    }

    /**
     * 单个领取节点值奖励
     *
     * @param uid 当前迷你号
     * @param num 节点值
     * @param vo  当前缓存
     * @return {@link List}<{@link ReceiveAwardVo.ReceiveResult}>
     */
    private List<ReceiveAwardVo.ReceiveResult> singleReceive(Long uid, Integer num, SealAwardVo vo) {
        List<ReceiveAwardVo.ReceiveResult> voList = new ArrayList<>(1);
        // 匹配节点值
        final List<SealAwardVo.SealAward> awardList = vo.getAwardList();
        final List<SealAwardVo.SealAward> matchList = awardList.stream().filter(
                sealAward -> sealAward.getSealNum().compareTo(num) == 0 && sealAward.isReachFlag() && !sealAward.isReceiveFlag()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(matchList)) {
            return new ArrayList<>();
        }

        final SealAwardVo.SealAward sealAward = matchList.get(0);
        // 移除当前匹配节点记录
        awardList.removeAll(matchList);
        // 发送奖励
        Map<Integer, Integer> attachMap = new HashMap<>(1);
        attachMap.put(sealAward.getAvatarId(), sealAward.getAvatarNum());
        try {
            emailApi.sendEmail(uid, SendEmailConstant.YUELEYUAN_EMAIL_TITLE,
                    SendEmailConstant.YUELEYUAN_EMAIL_BODY,
                    SendEmailConstant.YUELEYUAN_EMAIL_ACT_URL,
                    attachMap, null);
            // 更改节点值为已领取
            sealAward.setReceiveFlag(true);
            // 将更新之后的数据set
            awardList.add(sealAward);
            vo.setAwardList(awardList);
            redisUtil.hPut(RedisKeyConstant.SEAL_STATUS_RECORD, Long.toString(uid), vo);
            // 返回信息
            voList.add(new ReceiveAwardVo.ReceiveResult(sealAward.getAvatarId(), sealAward.getAvatarName(), sealAward.getAvatarNum()));
        } catch (GameApiException e) {
            log.error("uid - {}，领取印章奖励邮件失败，已入队 - {}", uid, RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST);
            redisUtil.lRightPush(RedisKeyConstant.EMAIL_ACTIVITY_FAILED_LIST, new EmailFailedDto(uid, attachMap, null, 0, null, null, null));
        }
        return voList;
    }
}
