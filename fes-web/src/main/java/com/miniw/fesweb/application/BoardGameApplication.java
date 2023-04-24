package com.miniw.fesweb.application;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fesweb.params.base.*;
import com.miniw.fesweb.params.vo.*;
import com.miniw.gameapi.exception.GameApiException;
import com.miniw.gameapi.pojo.dto.UInfoDTO;

import java.util.List;

/**
 * 棋盘游戏相关逻辑
 *
 * @author luoquan
 * @date 2021/08/27
 */
public interface BoardGameApplication {

    /**
     * 使用骰子
     *
     * @param boardBase 棋盘base
     * @return {@link Result}<{@link BoardVo}>
     * @throws InterruptedException 中断异常
     */
    Result<BoardVo> useDice(BoardBase boardBase) throws InterruptedException;


    /**
     * 获奖记录
     *
     * @param recordBase 记录base
     * @return {@link Result}<{@link AwardRecordVo}>
     */
    Result<AwardRecordVo> list(RecordBase recordBase);

    /**
     * 领取骰子
     *
     * @param getDiceAwardBase 获取骰子base
     * @return {@link Result}<{@link GetDiceVo}>
     * @throws InterruptedException 中断异常
     */
    Result<GetDiceVo> getDice(GetDiceAwardBase getDiceAwardBase) throws InterruptedException;


    /**
     * 骰子领取详情列表
     *
     * @param diceDetailBase 骰子详情base
     * @return {@link Result}<{@link DiceDetailVo}>
     */
    Result<DiceDetailVo> listDiceDetail(DiceDetailBase diceDetailBase);


    /**
     * 购买万能骰子
     *
     * @param buyUniversalBase 购买万能骰子base
     * @return {@link Result}<{@link BuyUniversalVo}>
     * @throws GameApiException     游戏api异常
     * @throws InterruptedException 中断异常
     */
    Result<BuyUniversalVo> buyUniversal(BuyUniversalBase buyUniversalBase) throws GameApiException, InterruptedException;


    /**
     * 获取好友列表
     *
     * @param getFriendsBase 获取好友base
     * @return {@link Result}<{@link List}<{@link UInfoDTO}>>
     */
    Result<List<UInfoDTO>> getFriends(GetFriendsBase getFriendsBase);


    /**
     * 分享给好友
     *
     * @param shareFriendsBase 分享朋友base
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> shareWithFriends(ShareFriendsBase shareFriendsBase);


    /**
     * 分享任务回调
     *
     * @param callBackBase 回调base
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> taskCallBack(CallBackBase callBackBase);
}
