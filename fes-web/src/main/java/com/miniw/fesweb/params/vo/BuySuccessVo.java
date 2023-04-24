package com.miniw.fesweb.params.vo;

import com.miniw.fescommon.constant.FestivalConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 预售购买成功vo
 *
 * @author luoquan
 * @date 2021/08/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuySuccessVo {
    /**
     * avatarList：奖励道具列表
     *
     */
    private List<Avatar> avatarList;

    @Data
    public static class Avatar{
        /**
         * isSend： true/false
         * avatarId: 奖励道具id
         * avatarName:奖励道具名称
         * count: 道具数量
         */
        private boolean isSend;
        private Integer avatarId;
        private String avatarName;
        private Integer count;

    }


    public static BuySuccessVo convertTo(Map<Integer, Integer> attachMap, String type){
        BuySuccessVo vo = new BuySuccessVo();
        List<Avatar> avatarList = new ArrayList<>();
        attachMap.forEach((key, value) ->{
            BuySuccessVo.Avatar avatar = new BuySuccessVo.Avatar();
            avatar.setAvatarId(key);
            avatar.setCount(value);
            avatar.setSend(true);


            if (key.equals(FestivalConstant.LIUXIANER_ORIGIN_SKIN_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.LIUXIANER_ORIGIN_SKIN_AVATAR_NAME);
            } else if (key.equals(FestivalConstant.LIUXIANER_AWARD_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.LIUXIANER_AWARD_AVATAR_NAME);
            } else if (key.equals(FestivalConstant.LIUXIANER_PACKAGE_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.LIUXIANER_PACKAGE_AVATAR_NAME);
            } else if (key.equals(FestivalConstant.LIUXIANER_NEW_SKIN_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.LIUXIANER_NEW_AVATAR_NAME);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_ORIGIN_SKIN_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.YUEWUSHUNG_ORIGIN_SKIN_AVATAR_NAME);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_AWARD_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.YUEWUSHUNG_AWARD_AVATAR_NAME);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_PACKAGE_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.YUEWUSHUNG_PACKAGE_AVATAR_NAME);
            } else if (key.equals(FestivalConstant.YUEWUSHUNG_NEW_SKIN_AVATAR_ID)) {
                avatar.setAvatarName(FestivalConstant.YUEWUSHUNG_NEW_AVATAR_NAME);
            }
            avatarList.add(avatar);
        });

        // 预售换装装扮
        BuySuccessVo.Avatar avatar1 = new BuySuccessVo.Avatar();
        BuySuccessVo.Avatar avatar2 = new BuySuccessVo.Avatar();
        if (type.equals(FestivalConstant.LIUXIANER_PRESALE_TYPE)) {
            // 新装扮
            avatar1.setAvatarId(FestivalConstant.LIUXIANER_NEW_SKIN_AVATAR_ID);
            avatar1.setAvatarName(FestivalConstant.LIUXIANER_NEW_AVATAR_NAME);
            avatar1.setCount(1);
            avatar1.setSend(false);

            //  额外赠送的头像框
            avatar2.setAvatarId(FestivalConstant.LIUXIANER_PACKAGE_AVATAR_ID);
            avatar2.setAvatarName(FestivalConstant.LIUXIANER_PACKAGE_AVATAR_NAME);
            avatar2.setCount(1);
            avatar2.setSend(false);

            avatarList.add(avatar1);
            avatarList.add(avatar2);
        }else if(type.equals(FestivalConstant.YUEWUSHUANG_PRESALE_TYPE)) {
            avatar1.setAvatarId(FestivalConstant.YUEWUSHUNG_NEW_SKIN_AVATAR_ID);
            avatar1.setAvatarName(FestivalConstant.YUEWUSHUNG_NEW_AVATAR_NAME);
            avatar1.setCount(1);
            avatar1.setSend(false);

            //  额外赠送的头像框
            avatar2.setAvatarId(FestivalConstant.YUEWUSHUNG_PACKAGE_AVATAR_ID);
            avatar2.setAvatarName(FestivalConstant.YUEWUSHUNG_PACKAGE_AVATAR_NAME);
            avatar2.setCount(1);
            avatar2.setSend(false);

            avatarList.add(avatar1);
            avatarList.add(avatar2);
        }
        vo.setAvatarList(avatarList);
        return vo;
    }


}
