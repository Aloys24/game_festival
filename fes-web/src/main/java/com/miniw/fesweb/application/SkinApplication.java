package com.miniw.fesweb.application;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fesweb.params.base.SkinQueryBase;
import com.miniw.fesweb.params.vo.SkinVo;
import com.miniw.gameapi.exception.GameApiException;

/**
 * 包含：皮肤、装扮
 *
 * @author luoquan
 * @date 2021/08/20
 */
public interface SkinApplication {

    /**
     * 首页-获取预售期价格（扣除后）、道具等
     *
     * @param skinQueryBase skinQueryBase
     * @return {@link Result}
     * @throws GameApiException 游戏api异常
     */
    Result<SkinVo> skinIsOwned(SkinQueryBase skinQueryBase) throws GameApiException;
}
