package com.miniw.fesweb.ctl;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fesweb.application.SkinApplication;
import com.miniw.fesweb.params.base.SkinQueryBase;
import com.miniw.fesweb.params.vo.SkinVo;
import com.miniw.gameapi.exception.GameApiException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 玩家皮肤相关
 *
 * @author luoquan
 * @date 2021/08/20
 */
@RestController
@RequestMapping("/v1/festival")
public class SkinController {

    @Resource
    private SkinApplication skinApplication;

    /**
     * 首页-获取预售期价格（扣除后）、道具等
     *
     * @param uid uid
     * @return {@link Result}<{@link SkinVo}>
     * @throws GameApiException 游戏api异常
     */
    @GetMapping("/skinQuery/{uid}")
    public Result<SkinVo> skinIsOwned(@PathVariable(required = false) Long uid) throws GameApiException {
        return skinApplication.skinIsOwned(new SkinQueryBase(uid));
    }

}
