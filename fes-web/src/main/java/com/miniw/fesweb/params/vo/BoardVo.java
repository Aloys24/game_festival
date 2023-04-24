package com.miniw.fesweb.params.vo;

import com.miniw.fescommon.constant.BoardGameEnum;
import com.miniw.fescommon.constant.FestivalConstant;
import com.miniw.fescommon.constant.RandomEventEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 棋盘返回vo
 *
 * @author luoquan
 * @date 2021/08/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardVo {

    /**
     * position: 当前位置
     * backBeforePosition: 如果存在倒退事件，记录倒退之前棋盘位置
     * diceNum：本次摇骰子结果
     * boardAvatar：落点对应道具详情
     */
    private Integer position;
    private Integer diceNum;
    private Integer backBeforePosition;
    private BoardAvatar boardAvatar;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoardAvatar {
        /**
         * avatarType：道具类别
         * avatarId：道具id
         * avatarName：道具名称
         * avatarNum：道具数量
         * randomEvent: 随机事件内容
         * returnStartAvatar: 重回起点额外奖励
         */
        private String avatarType;
        private Integer avatarId;
        private String avatarName;
        private Integer avatarNum;
        private RandomEvent randomEvent;
        private ReturnStartAvatar returnStartAvatar;


        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ReturnStartAvatar {
            private Integer avatarId;
            private String avatarName;
            private Integer avatarNum;
        }


        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class RandomEvent {
            private Integer eventId;
            private String content;
            private Integer avatarId;
            private String avatarName;
            private Integer avatarNum;
        }
    }


    /**
     * 根据最终步数匹配相应奖励
     *
     * @param finalStep       最终步数
     * @param returnStartFlag 返回起点标志
     * @return {@link BoardAvatar}
     */
    public static BoardAvatar matchAward(int finalStep, boolean returnStartFlag) {
        if (finalStep == 0) {
            finalStep += 1;
        }

        final BoardAvatar boardAvatar = new BoardAvatar();
        //  重回起点
        if (returnStartFlag) {
            BoardAvatar.ReturnStartAvatar returnStartAvatar = new BoardAvatar.ReturnStartAvatar();
            returnStartAvatar.setAvatarId(FestivalConstant.RABBIT_SEAL_AVATAR_ID);
            returnStartAvatar.setAvatarNum(FestivalConstant.RABBIT_SEAL_AVATAR_NUM);
            returnStartAvatar.setAvatarName(FestivalConstant.RABBIT_SEAL_AVATAR_NAME);
            boardAvatar.setReturnStartAvatar(returnStartAvatar);
        }
        switch (finalStep) {
            case 1:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_1.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_1.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_1.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_1.getBoardType());
                break;
            case 2:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_2.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_2.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_2.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_2.getBoardType());
                break;
            case 3:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_3.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_3.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_3.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_3.getBoardType());
                break;
            case 4:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_4.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_4.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_4.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_4.getBoardType());
                break;
            case 5:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_5.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_5.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_5.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_5.getBoardType());
                break;
            case 6:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_6.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_6.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_6.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_6.getBoardType());
                break;
            case 7:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_7.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_7.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_7.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_7.getBoardType());
                break;
            case 8:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_8.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_8.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_8.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_8.getBoardType());
                break;
            case 9:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_9.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_9.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_9.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_9.getBoardType());
                break;
            case 10:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_10.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_10.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_10.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_10.getBoardType());
                // 触发随机事件（平概率
                List<BoardAvatar.RandomEvent> listRandom1 =  Arrays.asList(
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_1_1001.getEventId(),
                                RandomEventEnum.RANDOM_1_1001.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_1_1002.getEventId(),
                                RandomEventEnum.RANDOM_1_1002.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_1_1003.getEventId(),
                                RandomEventEnum.RANDOM_1_1003.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_1_1004.getEventId(),
                                RandomEventEnum.RANDOM_1_1004.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_1_1005.getEventId(),
                                RandomEventEnum.RANDOM_1_1005.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_1_1006.getEventId(),
                                RandomEventEnum.RANDOM_1_1006.getContent(), null, null, null)
                );
                Collections.shuffle(listRandom1);
                final BoardAvatar.RandomEvent event1 = listRandom1.get((int) (Math.random() * listRandom1.size()));
                boardAvatar.setRandomEvent(event1);
                break;
            case 11:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_11.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_11.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_11.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_11.getBoardType());
                break;
            case 12:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_12.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_12.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_12.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_12.getBoardType());
                break;
            case 13:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_13.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_13.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_13.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_13.getBoardType());
                break;
            case 14:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_14.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_14.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_14.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_14.getBoardType());
                break;
            case 15:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_15.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_15.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_15.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_15.getBoardType());
                // 触发随机事件（平概率
                List<BoardAvatar.RandomEvent> listRandom2 =  Arrays.asList(
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_2_2001.getEventId(),
                                RandomEventEnum.RANDOM_2_2001.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_2_2002.getEventId(),
                                RandomEventEnum.RANDOM_2_2002.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_2_2003.getEventId(),
                                RandomEventEnum.RANDOM_2_2003.getContent(), null, null, null),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_2_2004.getEventId(),
                                RandomEventEnum.RANDOM_2_2004.getContent(), 5, FestivalConstant.RABBIT_SEAL_AVATAR_NAME, 3),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_2_2005.getEventId(),
                                RandomEventEnum.RANDOM_2_2005.getContent(), 5, FestivalConstant.RABBIT_SEAL_AVATAR_NAME, 5),
                        new BoardAvatar.RandomEvent(RandomEventEnum.RANDOM_2_2006.getEventId(),
                                RandomEventEnum.RANDOM_2_2006.getContent(), 12928, "一袋迷你豆", 1)
                );
                Collections.shuffle(listRandom2);
                final BoardAvatar.RandomEvent event = listRandom2.get((int) (Math.random() * listRandom2.size()));
                boardAvatar.setRandomEvent(event);
                break;
            case 16:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_16.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_16.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_16.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_16.getBoardType());
                break;
            case 17:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_17.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_17.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_17.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_17.getBoardType());
                break;
            case 18:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_18.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_18.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_18.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_18.getBoardType());
                break;
            case 19:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_19.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_19.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_19.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_19.getBoardType());
                break;
            case 20:
                boardAvatar.setAvatarId(BoardGameEnum.BOARD_20.getAvatarId());
                boardAvatar.setAvatarName(BoardGameEnum.BOARD_20.getAvatarName());
                boardAvatar.setAvatarNum(BoardGameEnum.BOARD_20.getAvatarNum());
                boardAvatar.setAvatarType(BoardGameEnum.BOARD_20.getBoardType());
                break;
            default:
        }
        return boardAvatar;
    }

    /**
     *
     */
    public static BoardVo getInstance(){
        return InitBoard.BOARD;
    }

    private static class InitBoard{
        private static final BoardVo BOARD = new BoardVo(
                BoardGameEnum.BOARD_1.getBoardId(),
                null,
                null,
                new BoardAvatar(BoardGameEnum.BOARD_1.getBoardType(),
                        BoardGameEnum.BOARD_1.getAvatarId(),
                        BoardGameEnum.BOARD_1.getAvatarName(),
                        BoardGameEnum.BOARD_1.getAvatarNum(),
                        null,
                        null
                )
        );
    }


}
