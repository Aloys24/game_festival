package com.miniw.fesweb.params.base;

import lombok.Getter;

import javax.validation.ValidationException;

/**
 * 漫步月乐园首页base
 *
 * @author luoquan
 * @date 2021/08/27
 */
@Getter
public class SealHomeBase {

    private final Long uid;

    public SealHomeBase(Long uid){
        if(null == uid){
            throw new ValidationException("uid 不能为空");
        }
        this.uid = uid;
    }
}
