package com.miniw.fesweb.application.impl;

import com.alibaba.fastjson.JSONObject;
import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.constant.RedisKeyConstant;
import com.miniw.fescommon.utils.RedisUtil;
import com.miniw.fesweb.application.SkinApplication;
import com.miniw.fesweb.params.base.SkinQueryBase;
import com.miniw.fesweb.params.vo.SkinVo;
import com.miniw.gameapi.api.SkinApi;
import com.miniw.gameapi.exception.GameApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;

/**
 * 包含：皮肤、装扮
 *
 * @author luoquan
 * @date 2021/08/20
 */
@Slf4j
@Service
public class SkinApplicationImpl implements SkinApplication {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private SkinApi skinApi;


    /**
     * 首页-获取预售期价格（扣除后）、道具等
     *
     * @param skinQueryBase skinQueryBase
     * @return {@link Result}
     */
    @Override
    public Result<SkinVo> skinIsOwned(SkinQueryBase skinQueryBase) throws GameApiException {
        final Long uid = skinQueryBase.getUid();
        final Collection<Integer> skinIdList = skinQueryBase.getSkinIds();

        // 尝试从缓存中获取
        if (redisUtil.hHasKey(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid))) {
            final Object o = redisUtil.hGet(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid));
            log.debug("当前redis缓存为：{}", o.toString());
            SkinVo vo = JSONObject.parseObject(o.toString(), SkinVo.class);
            // 添加一层游戏服获取皮肤记录的逻辑，防止用户在活动外进行了购买 缓存没有更新
            final Map<Integer, Boolean> skinResult = skinApi.querySkinList(uid, skinIdList);
            if (!MapUtils.isEmpty(skinResult)) {
                // 查询结果为true的更新 false的以活动缓存记录为准
                SkinVo.updateIsTrue(vo, skinResult);
            }
            redisUtil.hPut(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid), vo);
            return Result.success(vo);
        }

        // 缓存中没有 从游戏服获取
        final Map<Integer, Boolean> skinResult = skinApi.querySkinList(uid, skinIdList);
        if (MapUtils.isEmpty(skinResult)) {
            final SkinVo data = new SkinVo(uid, null, null);
            redisUtil.hPut(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid), data);
            return Result.success(data);
        }
        SkinVo result = SkinVo.convertToVO(skinResult);
        result.setUin(uid);
        redisUtil.hPut(RedisKeyConstant.PACKAGE_STATUS_RECORD, Long.toString(uid), result);
        return Result.success(result);
    }
}
