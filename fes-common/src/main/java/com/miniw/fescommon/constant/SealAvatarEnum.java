package com.miniw.fescommon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 印章对应奖励枚举
 *
 * @author luoquan
 * @date 2021/08/25
 */
@AllArgsConstructor
@Getter
public enum SealAvatarEnum {

    /**
     *
     */
    NUM_10(10, 12948, "原始人礼包", 1),
    NUM_30(30, 10011, "果实券", 2),
    NUM_60(60, 37175, "黄昏靴", 1),
    NUM_90(90, 10010, "扭蛋券", 3),
    NUM_120(120, 37177, "手摘星辰", 1),
    NUM_150(150, 10012, "活动券", 3),
    NUM_180(180, 37173, "惊喜星眸", 1),
    NUM_210(210, 49005, "碎片自选礼包", 10),
    NUM_240(240, 37174, "星环头冠", 1),
    NUM_270(270, 37176, "星带披风", 1),
    NUM_300(300, 20280, "海焰幻境", 1),
    NUM_350(350, 49005, "碎片自选礼包", 10),
    NUM_400(400, 37178, "芸芸星辰", 1),
    NUM_450(450, 10012, "活动券", 5),
    NUM_500(500, 10010, "扭蛋券", 5),

    ;

    private final Integer sealNum;
    private final Integer avatarId;
    private final String avatarName;
    private final Integer avatarNum;

}
