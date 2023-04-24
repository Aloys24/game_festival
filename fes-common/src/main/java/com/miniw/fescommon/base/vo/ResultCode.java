package com.miniw.fescommon.base.vo;

/**
 * 结果代码
 * 消息状态枚举
 *
 * @author luoquan
 * @date 2021/08/18
 */
public enum ResultCode {

    /*系统相关*/
    SYS_SUCCESS         (0 , "操作成功"),
    SYS_ERROR           (400,"操作失败"),
    SYS_NEED_LOGIN      (401,"请登录"),
    SYS_NOT_OPEN        (402,"活动未开启"),
    SYS_IS_END          (403,"活动已结束"),
    SYS_LIMIT           (404,"超过次数限制"),
    SYS_UNKNOWN         (405,"未知错误"),
    SYS_PARAM_ERROR     (406,"请求参数异常"),
    SYS_ILLEGAL_PARAM(407, "API请求失败"),
    SYS_MISMATCH(408, "数据不匹配"),
    SYS_NON_EXIST(409, "数据不存在"),
    SYS_DISCONTENT(410, "未达到领取条件"),
    SYS_BAN(411, "禁止操作"),
    SYS_BUSY(444, "系统繁忙"),
    SYS_ACCOUNT_ERROR(445, "用户名密码不匹配"),


    /* 0909创收节相关 */
    FES_PLEASE_WAIT(446, "未到售卖期，敬请期待"),
    FES_SKIN_NOT_EXIST(447, "暂未获取到装扮信息，请稍后重试"),
    FES_ALREADY_OWN(448, "已经拥有该礼包"),
    FES_COIN_NOT_ENOUGH(449, "您的迷你币不足，无法购买！请先充值哦"),
    FES_COIN_QUERY_FAILED(450, "查询迷你币库存繁忙，请稍后重试"),
    FES_COIN_CONSUME_FAILED(451, "活动太火爆啦，请稍后重试"),
    FES_SEND_EMAIL_FAILED(452, "活动太火爆啦，道具将在5分钟之后下发"),
    FES_SKIN_QUERY_FAILED(453, "获取装扮信息繁忙，请稍后重试"),
    FES_RANDOM_MISMATCH(454, "无法获取对应事件信息，请稍后重试"),
    FES__MISMATCH(455, "无法获取对应事件信息，请稍后重试"),
    FES_UNIVERSAL_DICE_NON(456, "万能骰子不足，请前往购买"),
    FES_COMMON_DICE_NON(457, "骰子不足，请前往领取"),
    FES_GET_COMMON_LIMIT(458, "今天已经领取过啦"),
    FES_GET_UNIVERSAL_LIMIT(459, "您已经领取过该奖励"),
    FES_BUY_ORIGIN_FIRST(460, "需先拥有装扮柳仙儿才可购买换装礼包！前往游戏商店-扭蛋获得柳仙儿哦"),
    ;

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static ResultCode valueOf(Integer code) {
        if (code == null || code < 0)
            return SYS_UNKNOWN;
        for (ResultCode resultCode : values()) {
            if (resultCode.code.equals(code))
                return resultCode;
        }
        return SYS_UNKNOWN;
    }
}
