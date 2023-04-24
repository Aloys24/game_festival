package com.miniw.fescommon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 棋盘游戏枚举
 *
 * @author luoquan
 * @date 2021/08/27
 */
@AllArgsConstructor
@Getter
public enum BoardGameEnum {
    /**
     *
     */
    BOARD_1(1, "B", 10000, "迷你豆", 18),
    BOARD_2(2, "S", 10011, "果实券", 1),
    BOARD_3(3, "C", 5, "月兔印章", 3),
    BOARD_4(4, "B", 20036, "小白象碎片", 5),
    BOARD_5(5, "A", 1, "普通骰", 1),
    BOARD_6(6, "B", 20038, "小海豹碎片", 5),
    BOARD_7(7, "S", 10012, "活动券", 1),
    BOARD_8(8, "S", 10010, "扭蛋券", 1),
    BOARD_9(9, "C", 5, "月兔印章", 5),
    BOARD_10(10, "A", 2, "随机事件-1", 1),
    BOARD_11(11, "B", 12948, "原始人礼包", 1),
    BOARD_12(12, "B", 10000, "迷你豆", 30),
    BOARD_13(13, "S", 10010, "扭蛋券", 1),
    BOARD_14(14, "B", 12988, "装扮体验礼包", 3),
    BOARD_15(15, "A", 3, "随机事件-2", 1),
    BOARD_16(16, "B", 20036, "小白象碎片", 5),
    BOARD_17(17, "S", 10012, "活动券", 1),
    BOARD_18(18, "C", 5, "月兔印章", 2),
    BOARD_19(19, "S", 10011, "果实券", 1),
    BOARD_20(20, "A", 4, "万能骰", 1),

;



    private final int boardId;
    private final String boardType;
    private final int avatarId;
    private final String avatarName;
    private final int avatarNum;
}
