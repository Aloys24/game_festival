package com.miniw.fesweb.application;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fesweb.params.base.MoreAwardBase;
import com.miniw.fesweb.params.base.ReceiveAwardBase;
import com.miniw.fesweb.params.base.SealHomeBase;
import com.miniw.fesweb.params.vo.ReceiveAwardVo;
import com.miniw.fesweb.params.vo.SealAwardVo;
import com.miniw.fesweb.params.vo.SealHomeVo;

/**
 * 月兔印章
 *
 * @author luoquan
 * @date 2021/08/25
 */
public interface SealApplication {

    /**
     * 获得更多奖励
     *
     * @param moreAwardBase 印章base
     * @return {@link Result}<{@link SealAwardVo}>
     */
    Result<SealAwardVo> getMoreAward(MoreAwardBase moreAwardBase);


    /**
     * 领取印章奖励
     *
     * @param receiveAwardBase 领取奖励base
     * @return {@link Result}<{@link ReceiveAwardVo}>
     * @throws InterruptedException 中断异常
     */
    Result<ReceiveAwardVo> receiveAward(ReceiveAwardBase receiveAwardBase) throws InterruptedException;


    /**
     * 获取首页数据
     *
     * @param sealHomeBase 首页base
     * @return {@link Result}<{@link SealHomeVo}>
     */
    Result<SealHomeVo> home(SealHomeBase sealHomeBase);

    /**
     * 是否存在未领取的节点奖励
     *
     * @param sealHomeBase 首页base
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> unReceive(SealHomeBase sealHomeBase);
}
