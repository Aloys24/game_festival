package com.miniw.fescommon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 棋盘奖励概率枚举
 *
 * @author luoquan
 * @date 2021/09/10
 */
@AllArgsConstructor
@Getter
public enum BoardAwardRandomEnum {

    /**
     *
     */
    AWARD_S("S", "抽奖券", 7.00),
    AWARD_A("A", "网页道具", 23.33),
    AWARD_B("B", "基础道具", 36.34),
    AWARD_C("C", "兑换积分", 33.33),

    ;

    private final String randomType;
    private final String name;
    private final Double eventuality;

}
