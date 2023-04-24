package com.miniw.fesweb.params.base;

import lombok.Getter;

import javax.validation.ValidationException;

/**
 * 印章base
 *
 * @author luoquan
 * @date 2021/08/25
 */
@Getter
public class MoreAwardBase {

    private final Long uid;

    public MoreAwardBase(Long uid){
        if(null == uid){
            throw new ValidationException("uid 不能为空");
        }
        this.uid = uid;
    }

}
