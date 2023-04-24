package com.miniw.fesweb.params.base;

import lombok.Getter;

import javax.validation.ValidationException;

/**
 * 获奖记录基础
 *
 * @author luoquan
 * @date 2021/08/28
 */
@Getter
public class RecordBase {
    private final Long uid;
    private final Integer pageIndex;
    private final Integer pageSize;

    public RecordBase(Long uid, Integer pageIndex, Integer pageSize){
        if(null == uid){
            throw new ValidationException("uid 不能为空");
        }

        if(null == pageIndex){
            pageIndex = 1;
        }

        if(null == pageSize){
            pageSize = 9;
        }

        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.uid = uid;
    }

}
