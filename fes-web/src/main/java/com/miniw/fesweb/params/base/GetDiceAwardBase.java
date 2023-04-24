package com.miniw.fesweb.params.base;

import com.miniw.fescommon.constant.FestivalConstant;
import lombok.Getter;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;

/**
 * 获取骰子base
 *
 * @author luoquan
 * @date 2021/08/30
 */
@Getter
public class GetDiceAwardBase {
    private final Long uid;
    private final String type;

    /**
     * login:  每日登录活动页领取 普通骰子+1
     * invite：每日邀请好友参与活动 普通骰+1（仅包好微信、QQ
     * online： 每日邀请参与联机游戏 普通骰+1
     * liuxianer: 购买柳仙儿礼包可领取 万能骰子+3
     * yuewushuang: 购买月无双可领取 万能骰子+3
     */
    private static final List<String> TYPE_LIST = Arrays.asList(
            FestivalConstant.DICE_GET_TYPE_LOGIN,
            FestivalConstant.DICE_GET_TYPE_INVITE,
            FestivalConstant.DICE_GET_TYPE_ONLINE,
            FestivalConstant.DICE_GET_TYPE_LIUXIANER,
            FestivalConstant.DICE_GET_TYPE_YUEWUSHUANG
    );

    public GetDiceAwardBase(Long uid, String type){
        if(null == uid){
            throw new ValidationException("uid 不能为空");
        }

        if(null == type){
            throw new ValidationException("type 不能为空");
        }else{
            if(!TYPE_LIST.contains(type)){
                throw new ValidationException("type 类型有误");
            }
        }

        this.uid = uid;
        this.type = type;
    }

    public static boolean isGetCommonDice(String type) {
        final List<String> commonList = Arrays.asList(
                FestivalConstant.DICE_GET_TYPE_LOGIN,
                FestivalConstant.DICE_GET_TYPE_INVITE,
                FestivalConstant.DICE_GET_TYPE_ONLINE);
        return commonList.contains(type);
    }


    public static List<String> getCtypeList() {
        return Arrays.asList(
                FestivalConstant.DICE_GET_TYPE_LOGIN,
                FestivalConstant.DICE_GET_TYPE_INVITE,
                FestivalConstant.DICE_GET_TYPE_ONLINE
        );
    }

    public static List<String> getUtypeList() {
        return Arrays.asList(
                FestivalConstant.DICE_GET_TYPE_LIUXIANER,
                FestivalConstant.DICE_GET_TYPE_YUEWUSHUANG
        );
    }

    public static Integer matchV6(String type) {
        switch (type) {
            case FestivalConstant.DICE_GET_TYPE_LOGIN:
                return 1;
            case FestivalConstant.DICE_GET_TYPE_INVITE:
                return 2;
            case FestivalConstant.DICE_GET_TYPE_ONLINE:
                return 3;
            case FestivalConstant.DICE_GET_TYPE_LIUXIANER:
                return 4;
            case FestivalConstant.DICE_GET_TYPE_YUEWUSHUANG:
                return 5;
            default:
                return 0;
        }
    }
}
