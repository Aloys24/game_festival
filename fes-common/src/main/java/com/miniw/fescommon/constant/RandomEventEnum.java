package com.miniw.fescommon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 随机事件枚举
 *
 * @author luoquan
 * @date 2021/08/28
 */
@AllArgsConstructor
@Getter
public enum RandomEventEnum {

    /**
     *
     */
    RANDOM_1_1001(1001, "受到魔炎的攻击，倒退1格（倒退不会获得棋盘奖励）"),
    RANDOM_1_1002(1002, "受到魔炎的攻击，倒退2格（倒退不会获得棋盘奖励）"),
    RANDOM_1_1003(1003, "受到魔炎的攻击，倒退3格（倒退不会获得棋盘奖励）"),
    RANDOM_1_1004(1004, "受到魔炎的攻击，倒退4格（倒退不会获得棋盘奖励）"),
    RANDOM_1_1005(1005, "受到魔炎的攻击，倒退5格（倒退不会获得棋盘奖励）"),
    RANDOM_1_1006(1006, "受到魔炎的攻击，倒退6格（倒退不会获得棋盘奖励）"),

    RANDOM_2_2001(2001, "下次投掷点数翻倍"),
    RANDOM_2_2002(2002, "下次投掷点数减半（向上取整，如下次投掷点数为5，则前进3格）"),
    RANDOM_2_2003(2003, "前方事故维护，回到起点"),
    RANDOM_2_2004(2004, "受到月兔亲昵，获得月兔印章*3"),
    RANDOM_2_2005(2005, "受到月兔喜爱，获得大量月兔印章*5"),
    RANDOM_2_2006(2006, "挖掘到宝藏，获得一袋迷你豆*1"),
    ;

    private final int eventId;
    private final String content;


    public static int returnBackStep(Integer eventId) {
        if (eventId.equals(RandomEventEnum.RANDOM_1_1001.getEventId()))
            return 1;
        if (eventId.equals(RandomEventEnum.RANDOM_1_1002.getEventId()))
            return 2;
        if (eventId.equals(RandomEventEnum.RANDOM_1_1003.getEventId()))
            return 3;
        if (eventId.equals(RandomEventEnum.RANDOM_1_1004.getEventId()))
            return 4;
        if (eventId.equals(RandomEventEnum.RANDOM_1_1005.getEventId()))
            return 5;
        if (eventId.equals(RandomEventEnum.RANDOM_1_1006.getEventId()))
            return 6;
        return 0;
    }

}
