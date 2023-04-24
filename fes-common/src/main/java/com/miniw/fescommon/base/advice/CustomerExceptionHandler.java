package com.miniw.fescommon.base.advice;

import com.miniw.fescommon.base.exception.FestivalException;
import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.base.vo.ResultCode;
import com.miniw.gameapi.exception.GameApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;

/**
 * 业务异常统一处理
 *
 * @author luoquan
 * @date 2021/08/18
 */
@Slf4j
@RestControllerAdvice
public class CustomerExceptionHandler<T> {

    @ExceptionHandler(Exception.class)
    public Result<T> exception(HttpServletRequest request, Exception e) {
        if (e instanceof GameApiException) {
            // 处理GameApiException
            log.error("接口：{}出现调用异常：{}", request.getRequestURI(), e.getMessage());
            return this.handlerGameApiException(request, (GameApiException) e);
        }

        if (e instanceof ResourceAccessException) {
            log.error("接口：{}出现调用异常：{}", request.getRequestURI(), e.getMessage());
            return new Result<>(ResultCode.SYS_BUSY.getCode(), "活动太火爆啦，请稍后再试");
        }

        log.error("接口：{}出现调用异常：{}", request.getRequestURI(), e.getMessage());
        return new Result<>(ResultCode.SYS_BUSY.getCode(), ResultCode.SYS_BUSY.getMsg());
    }

    @ExceptionHandler(NullPointerException.class)
    public Result<T> nullPointerException(HttpServletRequest request, NullPointerException e){
        log.error("接口：{}当前发生空指针异常 - {}", request.getRequestURI(), e);
        return new Result<>(ResultCode.SYS_ERROR.getCode(), ResultCode.SYS_ERROR.getMsg());
    }


    @ExceptionHandler(FestivalException.class)
    public Result<T> festivalException(HttpServletRequest request, FestivalException e){
        log.error("接口：{}发生业务异常：{}", request.getRequestURI(), e.getMessage());
        return new Result<>(e.getErrorCode(), e.getErrorMsg());
    }

    @ExceptionHandler(ValidationException.class)
    public Result<T> validationException(HttpServletRequest request, ValidationException e){
        log.error("接口：{}发生异常：{}", request.getRequestURI(), e.getMessage());
        return new Result<>(ResultCode.SYS_PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(InterruptedException.class)
    public Result<T> interruptedException(HttpServletRequest request, InterruptedException e){
        log.error("接口：{}请求被拦截：{}", request.getRequestURI(), e.getMessage());
        return new Result<>(ResultCode.SYS_BUSY.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<T> notReadableException(HttpServletRequest request, HttpMessageNotReadableException e){
        log.error("接口：{}时间转换异常：{}", request.getRequestURI(), e.getMessage());
        return new Result<>(ResultCode.SYS_PARAM_ERROR.getCode(), ResultCode.SYS_PARAM_ERROR.getMsg());
    }



    /**
     * 游戏api异常处理：
     * 包含：查询迷你币库存、活动扣除迷你币、发送邮件
     *
     * @param e       e
     * @param request 请求
     * @return {@link Result}<{@link T}>
     */
    private Result<T> handlerGameApiException(HttpServletRequest request, GameApiException e) {
        Result<T> result;
        log.error("接口：{}调用游戏服发生异常：{}", request.getRequestURI(), e.getMessage());
        final List<String> errorMsg = Arrays.asList(e.getMessage().split("\\|"));
        if (CollectionUtils.isEmpty(errorMsg)) {
            return new Result<>(ResultCode.SYS_BUSY.getCode(), e.getMessage());
        }
        //  通过act判断抛出的接口类型
        switch (errorMsg.get(1)) {
            case "查询迷你币":
                result = new Result<>(ResultCode.FES_COIN_QUERY_FAILED.getCode(), ResultCode.FES_COIN_QUERY_FAILED.getMsg());
                break;
            case "消耗迷你币":
                result = new Result<>(ResultCode.FES_COIN_CONSUME_FAILED.getCode(), ResultCode.FES_COIN_CONSUME_FAILED.getMsg());
                break;
            case "批量查询是否拥有皮肤":
                result = new Result<>(ResultCode.FES_SKIN_QUERY_FAILED.getCode(), ResultCode.FES_SKIN_QUERY_FAILED.getMsg());
                break;
            default:
                result =  new Result<>(ResultCode.SYS_BUSY.getCode(), e.getMessage());
        }
        return result;
    }

}
