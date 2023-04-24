package com.miniw.fesweb.params.base;

import com.miniw.fescommon.constant.FestivalConstant;
import lombok.Getter;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.Collection;

/**
 * uid
 *
 * @author luoquan
 * @date 2021/08/20
 */
@Getter
public class SkinQueryBase {

    /**
     * uid: 当前迷你号
     * skinIds: 礼包查询skinId集
     */
    private final Long uid;
    private final Collection<Integer> skinIds = Arrays.asList(FestivalConstant.LIUXIANER_ORIGN_SKIN,
            FestivalConstant.LIUXIANER_NEW_SKIN,
            FestivalConstant.YUEWUSHUNG_ORIGN_SKIN,
            FestivalConstant.YUEWUSHUNG_NEW_SKIN
    );

    public SkinQueryBase(Long uid) {
        if (null == uid) {
            throw new ValidationException("uid 不能为空");
        }
        this.uid = uid;
    }
}
