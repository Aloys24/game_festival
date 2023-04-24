package com.miniw.fesweb.params.base;


import lombok.Getter;

import javax.validation.ValidationException;

/**
 * 获取好友base
 *
 * @author luoquan
 * @date 2021/09/01
 */
@Getter
public class GetFriendsBase {
    private final Long uid;

    public GetFriendsBase(Long uid) {
        if (null == uid) {
            throw new ValidationException("uid 不能为空");
        }
        this.uid = uid;
    }
}
