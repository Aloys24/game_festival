package com.miniw.fesweb.params.vo;


import com.miniw.fescommon.constant.FestivalConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 预售期价格（扣除后）、原装扮是否拥有
 *
 * @author luoquan
 * @date 2021/08/20
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Slf4j
public class SkinVo {

    /**
     * uin: 当前玩家uin
     * liuXianEr：柳仙儿预售礼包返回参数集
     * yueWuShuang: 月无双预售礼包返回参数集
     */
    private Long uin;
    private LiuXianErPackage liuXianEr;
    private YueWuShuangPackage yueWuShuang;

    @Data
    public static class LiuXianErPackage {
        /**
         * originSkinAvatarId：原装扮道具id
         * newSkinAvatarId：换装装扮道具id
         * awardAvatarId：奖励道具id
         * avatarName: 奖励道具名称
         * haveOriginFlag：是否拥有原装扮
         * haveNewFlag：是否拥有换装装扮
         * price：最终价格
         */
        private Integer originSkinAvatarId;
        private Integer newSkinAvatarId;
        private Integer awardAvatarId;
        private String avatarName;
        private boolean haveOriginFlag;
        private boolean haveNewFlag;
        private Integer price;
    }


    @Data
    public static class YueWuShuangPackage {
        /**
         * originSkinAvatarId：原装扮道具id
         * newSkinAvatarId：换装装扮道具id
         * awardAvatarId：奖励道具id
         * avatarName:奖励道具名称
         * haveOriginFlag：是否拥有原装扮
         * haveNewFlag：是否拥有换装装扮
         * price：最终价格
         */
        private Integer originSkinAvatarId;
        private Integer newSkinAvatarId;
        private Integer awardAvatarId;
        private String avatarName;
        private boolean haveOriginFlag;
        private boolean haveNewFlag;
        private Integer price;
    }


    /**
     * 返回预售页面所需vo
     *
     * @param result 结果
     * @return {@link SkinVo}
     */
    public static SkinVo convertToVO(Map<Integer, Boolean> result) {
        if (MapUtils.isEmpty(result)) {
            return new SkinVo();
        }
        SkinVo vo = new SkinVo();
        LiuXianErPackage liuXianEr = new LiuXianErPackage();
        YueWuShuangPackage yueWuShuang = new YueWuShuangPackage();

        // 固定信息
        liuXianEr.setOriginSkinAvatarId(FestivalConstant.LIUXIANER_ORIGIN_SKIN_AVATAR_ID);
        liuXianEr.setNewSkinAvatarId(FestivalConstant.LIUXIANER_NEW_SKIN_AVATAR_ID);
        liuXianEr.setAwardAvatarId(FestivalConstant.LIUXIANER_AWARD_AVATAR_ID);
        liuXianEr.setAvatarName(FestivalConstant.LIUXIANER_AWARD_AVATAR_NAME);

        yueWuShuang.setOriginSkinAvatarId(FestivalConstant.YUEWUSHUNG_ORIGIN_SKIN_AVATAR_ID);
        yueWuShuang.setNewSkinAvatarId(FestivalConstant.YUEWUSHUNG_NEW_SKIN_AVATAR_ID);
        yueWuShuang.setAwardAvatarId(FestivalConstant.YUEWUSHUNG_AWARD_AVATAR_ID);
        yueWuShuang.setAvatarName(FestivalConstant.YUEWUSHUNG_AWARD_AVATAR_NAME);

        // 筛选判断皮肤是否拥有
        result.forEach((key, value) -> {
            if (key.equals(FestivalConstant.LIUXIANER_ORIGN_SKIN)) {
                liuXianEr.setHaveOriginFlag(value);
            } else if (key.equals(FestivalConstant.LIUXIANER_NEW_SKIN)) {
                liuXianEr.setHaveNewFlag(value);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_ORIGN_SKIN)) {
                yueWuShuang.setHaveOriginFlag(value);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_NEW_SKIN)) {
                yueWuShuang.setHaveNewFlag(value);
            }
        });

        liuXianEr.setPrice(FestivalConstant.ALL_LIUXIANER_PRICE);
        yueWuShuang.setPrice(FestivalConstant.ALL_YUEWUSHUANG_PRICE);

        if(liuXianEr.haveOriginFlag)
            liuXianEr.setPrice(FestivalConstant.NEW_LIUXIANER_PRICE);
        if(yueWuShuang.haveOriginFlag)
            yueWuShuang.setPrice(FestivalConstant.NEW_YUEWUSHUNG_PRICE);

        vo.setLiuXianEr(liuXianEr);
        vo.setYueWuShuang(yueWuShuang);
        return vo;
    }


    /**
     * 计算最终价格
     *
     * @param resultMap 皮肤返回
     * @return {@link AtomicInteger}
     */
    public static Integer computePrice(Map<Integer, Boolean> resultMap) {
        AtomicInteger price = new AtomicInteger();
        resultMap.forEach((key, value) -> {
            if (key.equals(FestivalConstant.LIUXIANER_ORIGN_SKIN)) {
                if (value) {
                    price.getAndSet(FestivalConstant.NEW_LIUXIANER_PRICE);
                }
                price.getAndSet(FestivalConstant.ALL_LIUXIANER_PRICE);
            }
            if (key.equals(FestivalConstant.YUEWUSHUNG_ORIGN_SKIN)) {
                if (value) {
                    price.getAndSet(FestivalConstant.NEW_YUEWUSHUNG_PRICE);
                }
                price.getAndSet(FestivalConstant.ALL_YUEWUSHUANG_PRICE);
            }
        });
        return price.get();
    }


    /**
     * 更新缓存
     *
     * @param vo        签证官
     * @param skinQuery 皮肤查询
     * @return {@link SkinVo}
     */
    public static SkinVo updateRecord(SkinVo vo, Map<Integer, Boolean> skinQuery) {
        final LiuXianErPackage liuXianEr = vo.getLiuXianEr();
        final YueWuShuangPackage yueWuShuang = vo.getYueWuShuang();
        skinQuery.forEach((key, value) -> {
            if (key.equals(FestivalConstant.LIUXIANER_ORIGN_SKIN)) {
                liuXianEr.setHaveOriginFlag(value);
            } else if (key.equals(FestivalConstant.LIUXIANER_NEW_SKIN)) {
                liuXianEr.setHaveNewFlag(value);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_ORIGN_SKIN)) {
                yueWuShuang.setHaveOriginFlag(value);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_NEW_SKIN)) {
                yueWuShuang.setHaveNewFlag(value);
            }
        });
        vo.setLiuXianEr(liuXianEr);
        vo.setYueWuShuang(yueWuShuang);
        return vo;
    }

    /**
     * 判断礼包皮肤是否都拥有
     *
     * @return boolean
     */
    public static boolean isAll(SkinVo vo, String type){
        AtomicBoolean isAll = new AtomicBoolean(false);
        if(type.equals(FestivalConstant.LIUXIANER_PRESALE_TYPE)){
            final LiuXianErPackage liuXianEr = vo.getLiuXianEr();
            if(liuXianEr.haveOriginFlag && liuXianEr.haveNewFlag){
                isAll.getAndSet(true);
                return isAll.get();
            }
        }

        if(type.equals(FestivalConstant.YUEWUSHUANG_PRESALE_TYPE)){
            final YueWuShuangPackage yueWuShuang = vo.getYueWuShuang();
            if (yueWuShuang.haveOriginFlag && yueWuShuang.haveNewFlag) {
                isAll.getAndSet(true);
                return isAll.get();
            }
        }
        return isAll.get();
    }


    /**
     * 仅更新skin为true
     *
     * @param vo         缓存
     * @param skinResult 游戏服查询结果
     * @return {@link SkinVo}
     */
    public static SkinVo updateIsTrue(SkinVo vo, Map<Integer, Boolean> skinResult) {
//        final Integer liuxianerPrice = vo.getLiuXianEr().getPrice();
//        final Integer yuewushaungPrice = vo.getYueWuShuang().getPrice();

        skinResult.forEach((key, value) -> {
            if (key.equals(FestivalConstant.LIUXIANER_ORIGN_SKIN) && value) {
                vo.getLiuXianEr().setHaveOriginFlag(true);
            }

            if (key.equals(FestivalConstant.LIUXIANER_NEW_SKIN) && value) {
                vo.getLiuXianEr().setHaveNewFlag(true);
//                vo.getYueWuShuang().setPrice(liuxianerPrice - FestivalConstant.NEW_LIUXIANER_PRICE);
            }

            if (key.equals(FestivalConstant.YUEWUSHUNG_ORIGN_SKIN) && value) {
                vo.getYueWuShuang().setHaveOriginFlag(true);
            }

            if (key.equals(FestivalConstant.YUEWUSHUNG_NEW_SKIN) && value) {
                vo.getYueWuShuang().setHaveNewFlag(true);
            }
        });

        // 更新价格
        if (vo.getLiuXianEr().isHaveOriginFlag()) {
            vo.getLiuXianEr().setPrice(FestivalConstant.NEW_LIUXIANER_PRICE);
        }
        if (vo.getYueWuShuang().isHaveOriginFlag()) {
            vo.getYueWuShuang().setPrice(FestivalConstant.NEW_YUEWUSHUNG_PRICE);
        }
        return vo;
    }

}
