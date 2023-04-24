package com.miniw.fesweb.params.base;

import com.miniw.fescommon.constant.FestivalConstant;
import lombok.Getter;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;

/**
 * 回调base
 *
 * @author luoquan
 * @date 2021/09/02
 */
@Getter
public class CallBackBase {

    /**
     * 回调type：wx/微信  qq/QQ  online/联机
     */
    private final String type;
    private final Long uid;
    private static final List<String> TYPE_LIST = Arrays.asList(
            FestivalConstant.CALLBACK_TYPE_WX,
            FestivalConstant.CALLBACK_TYPE_QQ,
            FestivalConstant.CALLBACK_TYPE_ONLINE
    );

    public CallBackBase(Long uid, String type) {
        if (null == uid) {
            throw new ValidationException("uid 不能为空");
        }

        if (!TYPE_LIST.contains(type)) {
            throw new ValidationException("type 类型不正确");
        }

        this.uid = uid;
        this.type = type;
    }


    public static Integer matchV6(String type) {
        switch (type) {
            case FestivalConstant.CALLBACK_TYPE_WX:
            case FestivalConstant.CALLBACK_TYPE_QQ:
                return 2;
            case FestivalConstant.CALLBACK_TYPE_ONLINE:
                return 3;
            default:
                return 0;
        }
    }

    public static String convertToInvite(String type) {
        switch (type) {
            case FestivalConstant.CALLBACK_TYPE_WX:
            case FestivalConstant.CALLBACK_TYPE_QQ:
                return FestivalConstant.DICE_GET_TYPE_INVITE;

            default:
                return FestivalConstant.DICE_GET_TYPE_ONLINE;
        }
    }
}
