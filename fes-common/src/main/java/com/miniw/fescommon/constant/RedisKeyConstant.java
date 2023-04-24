package com.miniw.fescommon.constant;


/**
 * redis常用key常量类
 *
 * @author luoquan
 * @date 2021/08/20
 */
public class RedisKeyConstant {

    /**
     * PACKAGE_STATUS_RECORD: 首页礼包拥有状态等记录
     * SEAL_RECORD：印章节点状态记录
     * EMAIL_PACKAGE_FAILED_LIST: 礼包发送邮件道具失败list
     * EMAIL_ACTIVITY_FAILED_LIST：活动发送邮件道具失败list
     * BUY_SUCCESS_TEMP: 订购信息备份
     */
    public static final String PACKAGE_STATUS_RECORD = "PACKAGE_STATUS_RECORD:";
    public static final String SEAL_STATUS_RECORD = "SEAL_STATUS_RECORD:";

    public static final String EMAIL_PACKAGE_FAILED_LIST = "EMAIL_PACKAGE_FAILED_LIST:";
    public static final String EMAIL_ACTIVITY_FAILED_LIST = "EMAIL_ACTIVITY_FAILED_LIST:";
    public static final String BUY_SUCCESS_TEMP = "BUY_SUCCESS_TEMP:";

    /**
     * LIUXIANER_PRESALE_DELAY: 柳仙儿预售礼包延时zset
     * YUEWUSHUANG_PRESALE_DELAY：月无双预售礼包延时zset
     */
    public static final String LIUXIANER_PRESALE_DELAY = "LIUXIANER_PRESALE_DELAY:";
    public static final String YUEWUSHUANG_PRESALE_DELAY = "YUEWUSHUANG_PRESALE_DELAY:";


    /**
     * BUY_PRESALE_LOCK_KEY: 预售期购买lock key
     * BUY_OFFICIAL_LOCK_KEY：正式期购买lock key
     * SEAL_AWARD_LOCK_KEY: 印章奖励值领取lock key
     * USE_DICE_LOCK_KEY: 走格子 lock key
     * GET_COMMON_LOCK_KEY: 领取普通骰子 lock key
     * BUY_UNIVERSAL_LOCK_KEY：购买万能骰子key
     */
    public static final String BUY_PRESALE_LOCK_KEY = "BUY_PRESALE_LOCK_KEY:";
    public static final String BUY_OFFICIAL_LOCK_KEY = "BUY_OFFICIAL_LOCK_KEY:";
    public static final String SEAL_AWARD_LOCK_KEY = "SEAL_AWARD_LOCK_KEY:";
    public static final String USE_DICE_LOCK_KEY = "USE_DICE_LOCK_KEY:";
    public static final String GET_COMMON_LOCK_KEY = "GET_COMMON_LOCK_KEY:";
    public static final String BUY_UNIVERSAL_LOCK_KEY = "BUY_UNIVERSAL_LOCK_KEY:";


    /**
     * RABBIT_SEAL_COUNT: 玩家月兔印章数量
     */
    public static final String RABBIT_SEAL_COUNT = "RABBIT_SEAL_COUNT:";


    /**
     * COMMON_DICE_NUM: 玩家拥有普通骰子数量
     * UNIVERSAL_DICE_NUM: 玩家拥有万能骰子数量
     */
    public static final String COMMON_DICE_NUM = "COMMON_DICE_NUM:";
    public static final String UNIVERSAL_DICE_NUM = "UNIVERSAL_DICE_NUM:";

    /**
     * BOARD_POSITION_RECORD: 当前棋盘位置记录
     */
    public static final String BOARD_POSITION_RECORD = "BOARD_POSITION_RECORD:";

    /**
     * GET_COMMON_LIMIT: 普通骰领取每日限制
     * GET_UNIVERSAL_LIMIT：万能骰子领取限制
     */
    public static final String GET_COMMON_LIMIT = "GET_COMMON_LIMIT:";
    public static final String GET_UNIVERSAL_LIMIT = "GET_UNIVERSAL_LIMIT:";

    /**
     * USER_LOGIN_DATA: 用户登录态
     */
    public static final String USER_LOGIN_DATA = "USER_LOGIN_DATA:";

    /**
     * SEAL_NUM_REACH_COUNT: 各节点达成人数
     * SEAL_NUM_RECEIVE_COUNT: 各节点领取人数
     */
    public static final String SEAL_NUM_REACH_COUNT = "SEAL_NUM_REACH_COUNT:";
    public static final String SEAL_NUM_RECEIVE_COUNT = "SEAL_NUM_RECEIVE_COUNT:";


}
