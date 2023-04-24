package com.miniw.fesweb.ctl;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.utils.CurrentTimeMillisUtil;
import com.miniw.fesweb.application.BuyApplication;
import com.miniw.fesweb.params.base.BuyBase;
import com.miniw.fesweb.params.vo.BuySuccessVo;
import com.miniw.gameapi.exception.GameApiException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;

/**
 * 购买相关逻辑
 *
 * @author luoquan
 * @date 2021/08/21
 */
@RestController
@RequestMapping("/v1/festival")
public class BuyController {

    @Resource
    private BuyApplication buyApplication;


    /**
     * 购买礼包-预售期
     *
     * @param buyBase 购买base
     * @return {@link Result}<{@link BuySuccessVo}>
     * @throws InterruptedException 中断异常
     * @throws GameApiException     游戏api异常
     * @throws ParseException       解析异常
     */
    @PostMapping("/buyOnPreSale")
    public Result<BuySuccessVo> buyOnPreSale(@RequestBody BuyBase buyBase) throws InterruptedException, GameApiException, ParseException {
        return buyApplication.buyOnPreSale(buyBase);
    }

    /**
     * 购买礼包-正式售卖期
     *
     * @param buyBase 购买base
     * @return {@link Result}<{@link BuySuccessVo}>
     * @throws InterruptedException 中断异常
     * @throws GameApiException     游戏api异常
     * @throws ParseException       解析异常
     */
    @PostMapping("/buyOnOfficial")
    public Result<BuySuccessVo> buyOnOfficial(@RequestBody BuyBase buyBase) throws InterruptedException, GameApiException, ParseException {
        return buyApplication.buyOnOfficial(buyBase);
    }

    /**
     * 获取时间戳
     *
     * @return {@link Result}<{@link Long}>
     */
    @GetMapping("/getTimestamp")
    public Result<Long> getTimestamp() {
        return Result.success(CurrentTimeMillisUtil.getInstance().now());
    }


}
