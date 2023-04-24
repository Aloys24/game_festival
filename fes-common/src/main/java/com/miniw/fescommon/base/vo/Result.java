package com.miniw.fescommon.base.vo;


import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 通用返回结果
 *
 * @author luoquan
 * @date 2021/08/18
 */
@Data
@ToString
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    /**
     * 请求成功统一处理
     *
     * @param data 返回结果集
     * @return {@link Result}
     */
    public static <T> Result<T> success(T data){
        return new Result<T>(ResultCode.SYS_SUCCESS.getCode(), ResultCode.SYS_SUCCESS.getMsg(), data);
    }


    /**
     * 请求失败统一处理
     *
     * @param data 数据
     * @return {@link Result}
     */
    public static <T> Result<T> error(T data){
        return new Result<T>(ResultCode.SYS_ERROR.getCode(), ResultCode.SYS_ERROR.getMsg(), data);
    }

}
