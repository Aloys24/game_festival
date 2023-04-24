package com.miniw.fesweb.params.base;

import lombok.Getter;

import javax.validation.ValidationException;

/**
 * 骰子领取详情base
 *
 * @author luoquan
 * @date 2021/08/30
 */
@Getter
public class DiceDetailBase {
    private final Long uid;

    public DiceDetailBase(Long uid) {
        if (null == uid) {
            throw new ValidationException("uid 不能为空");
        }
        this.uid = uid;
    }
}
