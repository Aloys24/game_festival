package com.miniw.fesweb.ctl;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fesweb.application.BoardGameApplication;
import com.miniw.fesweb.params.base.*;
import com.miniw.fesweb.params.vo.*;
import com.miniw.gameapi.exception.GameApiException;
import com.miniw.gameapi.pojo.dto.UInfoDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 棋盘游戏控制器
 *
 * @author luoquan
 * @date 2021/08/27
 */
@RestController
@RequestMapping("/v1/festival")
public class BoardGameController {

    @Resource
    private BoardGameApplication boardGameApplication;

    /**
     * 使用骰子
     *
     * @param boardBase 棋盘base
     * @return {@link Result}<{@link BoardVo}>
     * @throws InterruptedException 中断异常
     */
    @PostMapping("/useDice")
    public Result<BoardVo> useDice(@RequestBody BoardBase boardBase) throws InterruptedException {
        return boardGameApplication.useDice(boardBase);
    }

    /**
     * 获取获奖记录
     *
     * @param uid       uid 迷你号
     * @param pageIndex 页面索引
     * @param pageSize  页面大小
     * @return {@link Result}<{@link AwardRecordVo}>
     */
    @GetMapping("/record/list/{uid}")
    public Result<AwardRecordVo> list(@PathVariable(required = false) Long uid,
                                      @RequestParam("pageIndex") Integer pageIndex,
                                      @RequestParam("pageSize") Integer pageSize) {
        return boardGameApplication.list(new RecordBase(uid, pageIndex, pageSize));

    }

    /**
     * 领取骰子
     *
     * @param diceAwardBase 普通骰子base
     * @return {@link Result}<{@link GetDiceVo}>
     * @throws InterruptedException 中断异常
     */
    @PostMapping("/getDice")
    public Result<GetDiceVo> getDice(@RequestBody GetDiceAwardBase diceAwardBase) throws InterruptedException {
        return boardGameApplication.getDice(diceAwardBase);
    }

    /**
     * 骰子领取详情列表
     *
     * @param uid uid 迷你号
     * @return {@link Result}<{@link DiceDetailVo}>
     */
    @GetMapping("/listDiceDetail/{uid}")
    public Result<DiceDetailVo> listDiceDetail(@PathVariable(required = false) Long uid) {
        return boardGameApplication.listDiceDetail(new DiceDetailBase(uid));
    }

    /**
     * 购买万能骰子
     *
     * @param buyUniversalBase 购买万能骰子基础
     * @return {@link Result}<{@link BuyUniversalVo}>
     * @throws GameApiException     游戏api异常
     * @throws InterruptedException 中断异常
     */
    @PostMapping("/buyUniversal")
    public Result<BuyUniversalVo> buyUniversal(@RequestBody BuyUniversalBase buyUniversalBase) throws GameApiException, InterruptedException {
        return boardGameApplication.buyUniversal(buyUniversalBase);
    }

    /**
     * 获取好友列表
     *
     * @param uid uid
     * @return {@link Result}<{@link List}<{@link UInfoDTO}>>
     */
    @GetMapping("/getFriends/{uid}")
    public Result<List<UInfoDTO>> getFriends(@PathVariable(required = false) Long uid) {
        return boardGameApplication.getFriends(new GetFriendsBase(uid));
    }

    /**
     * 分享给游戏好友
     *
     * @param shareFriendsBase 分享朋友base
     * @return {@link Result}<{@link Boolean}>
     */
    @PostMapping("/shareWithFriends")
    public Result<Boolean> shareWithFriends(@RequestBody ShareFriendsBase shareFriendsBase) {
        return boardGameApplication.shareWithFriends(shareFriendsBase);
    }

    /**
     * 分享任务回调
     *
     * @param callBackBase 回调base
     * @return {@link Result}<{@link Boolean}>
     */
    @PostMapping("/taskCallBack")
    public Result<Boolean> taskCallBack(@RequestBody CallBackBase callBackBase) {
        return boardGameApplication.taskCallBack(callBackBase);
    }


}
