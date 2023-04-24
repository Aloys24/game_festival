package com.miniw.fesweb.application;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fesweb.params.base.BuyBase;
import com.miniw.fesweb.params.vo.BuySuccessVo;
import com.miniw.gameapi.exception.GameApiException;

import java.text.ParseException;

/**
 * 购买相关逻辑
 * @author luoquan
 * @date 2021/08/21
 */
public interface BuyApplication {


    /**
     * 购买礼包-预售期
     *
     * @param buyBase 购买base
     * @return {@link Result}<{@link BuySuccessVo}>
     * @throws InterruptedException 中断异常
     * @throws ParseException       解析异常
     * @throws GameApiException     游戏api异常
     */
    Result<BuySuccessVo> buyOnPreSale(BuyBase buyBase) throws InterruptedException, ParseException, GameApiException;


    /**
     * 购买礼包-正式期
     *
     * @param buyBase 购买base
     * @return {@link Result}<{@link BuySuccessVo}>
     * @throws InterruptedException 中断异常
     * @throws GameApiException     游戏api异常
     * @throws ParseException       解析异常
     */
    Result<BuySuccessVo> buyOnOfficial(BuyBase buyBase) throws InterruptedException, GameApiException, ParseException;
}
