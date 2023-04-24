package com.miniw.fesweb.ctl;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fesweb.application.SealApplication;
import com.miniw.fesweb.params.base.MoreAwardBase;
import com.miniw.fesweb.params.base.ReceiveAwardBase;
import com.miniw.fesweb.params.base.SealHomeBase;
import com.miniw.fesweb.params.vo.ReceiveAwardVo;
import com.miniw.fesweb.params.vo.SealAwardVo;
import com.miniw.fesweb.params.vo.SealHomeVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 月兔印章控制层
 *
 * @author luoquan
 * @date 2021/08/25
 */
@RestController
@RequestMapping("/v1/festival")
public class SealController {

    @Resource
    private SealApplication sealApplication;


    /**
     * 获取首页数据
     *
     * @param uid uid 玩家迷你号
     * @return {@link Result}<{@link SealHomeVo}>
     */
    @GetMapping("/home/{uid}")
    public Result<SealHomeVo> home(@PathVariable(required = false) Long uid){
        return sealApplication.home(new SealHomeBase(uid));
    }


    /**
     * 获得更多的奖励
     *
     * @param uid uid 玩家迷你号
     * @return {@link Result}<{@link SealAwardVo}>
     */
    @GetMapping("/getMoreAward/{uid}")
    public Result<SealAwardVo> getMoreAward(@PathVariable(required = false) Long uid){
        return sealApplication.getMoreAward(new MoreAwardBase(uid));
    }


    /**
     * 领取月兔印章奖励
     *
     * @return {@link Result}<{@link ReceiveAwardVo}>
     * @throws InterruptedException 中断异常
     */
    @PostMapping("/receiveAward")
    public Result<ReceiveAwardVo> receiveAward(@RequestBody ReceiveAwardBase receiveAwardBase) throws InterruptedException {
        return sealApplication.receiveAward(receiveAwardBase);
    }

    /**
     * 是否存在未领取的节点奖励
     *
     * @param uid uid 迷你号
     * @return {@link Result}<{@link Boolean}>
     */
    @GetMapping("/unReceive/{uid}")
    public Result<Boolean> unReceive(@PathVariable(required = false) Long uid) {
        return sealApplication.unReceive(new SealHomeBase(uid));
    }


}
