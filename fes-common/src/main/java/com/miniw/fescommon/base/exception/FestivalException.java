package com.miniw.fescommon.base.exception;

import com.miniw.fescommon.base.vo.Result;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 自定义业务异常
 *
 * @author luoquan
 * @date 2021/08/19
 */
@ToString
@Getter
@Setter
public class FestivalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected Integer errorCode;
    protected String errorMsg;

    public FestivalException(Result result) {
        super(result.getMsg());
        this.errorCode = result.getCode();
        this.errorMsg = result.getMsg();
    }

    public FestivalException(String errorMsg, Integer code) {
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.errorCode  = code;
    }


}
