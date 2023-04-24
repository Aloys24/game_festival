package com.miniw.fesweb.params.vo;

import com.miniw.fescommon.constant.SealAvatarEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 棋盘首页数据vo
 *
 * @author luoquan
 * @date 2021/08/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SealHomeVo {

    /**
     * alreadyCount: 玩家已经收集的月兔印章总数
     * recentSeal: 距离当前最近的节点值及奖励
     * lastPosition：上一次落点详细
     * commonDiceNum：当前拥有普通骰子数量
     * universalDiceNum：当前拥有万能骰子数量
     */
    private Integer alreadyCount;
    private LastBoard lastBoard;
    private Integer commonDiceNum;
    private Integer universalDiceNum;
    private RecentSeal recentSeal;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LastBoard {
        /**
         * lastPosition：上一次落点位置
         * avatarId：奖励道具id
         * randomEvent: 随机事件
         */
        private Integer lastPosition;
        private Integer avatarId;
        private RandomEvent randomEvent;


        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class RandomEvent{
            private Integer eventId;
            private String content;
        }

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentSeal {
        /**
         * sealNum：节点目标值
         * avatarId：奖励道具id
         * receiveFlag: 是否已领取
         */
        private Integer sealNum;
        private Integer avatarId;
        private boolean receiveFlag;
    }


    public static SealHomeVo matchRecent(Integer alreadyCount, List<SealAwardVo.SealAward> awardList) {
        SealHomeVo sealHomeVo = new SealHomeVo();
        sealHomeVo.setAlreadyCount(alreadyCount);
        // 获取最有一个节点值备用
        final List<SealAwardVo.SealAward> finalSeal = awardList.stream().filter(sealAward -> sealAward.getSealNum() == 500).collect(Collectors.toList());
        // 先按节点值目标数进行升序
        awardList.sort(Comparator.comparingInt(SealAwardVo.SealAward::getSealNum));
        // 匹配距离最近且未领取的节点值
        final Optional<SealAwardVo.SealAward> match = awardList.stream().filter(sealAward ->
                sealAward.getSealNum().compareTo(alreadyCount) >= 0 && !sealAward.isReceiveFlag()).findFirst();
        RecentSeal seal;
        // 返回第一个节点值（如果没有匹配的 返回最后一个节点
        seal = match.map(sealAward -> new RecentSeal(sealAward.getSealNum(), sealAward.getAvatarId(), sealAward.isReceiveFlag()))
                .orElseGet(() -> new RecentSeal(SealAvatarEnum.NUM_500.getSealNum(), SealAvatarEnum.NUM_500.getAvatarId(), finalSeal.get(0).isReceiveFlag()));
        sealHomeVo.setRecentSeal(seal);
        return sealHomeVo;
    }

    public static SealHomeVo addBoard(Integer commonDiceNum, Integer universalDiceNum,
                                      SealHomeVo sealHomeVo, BoardVo boardRecord) {
        LastBoard lastBoard = new LastBoard();
        LastBoard.RandomEvent randomEvent = new LastBoard.RandomEvent();

        lastBoard.setLastPosition(boardRecord.getPosition());
        lastBoard.setAvatarId(boardRecord.getBoardAvatar().getAvatarId());
        final BoardVo.BoardAvatar.RandomEvent event = boardRecord.getBoardAvatar().getRandomEvent();
        if(null != event){
            // 如果存在随机事件
            randomEvent.setEventId(event.getEventId());
            randomEvent.setContent(event.getContent());
        }

        lastBoard.setRandomEvent(randomEvent);
        sealHomeVo.setLastBoard(lastBoard);
        sealHomeVo.setCommonDiceNum(commonDiceNum);
        sealHomeVo.setUniversalDiceNum(universalDiceNum);
        return sealHomeVo;
    }


}
