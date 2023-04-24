package com.miniw.fesweb.params.vo;

import com.miniw.fescommon.constant.SealAvatarEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 印章奖励vo
 *
 * @author luoquan
 * @date 2021/08/25
 */
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SealAwardVo {

    /**
     * alreadyCount: 玩家已经收集的月兔印章数
     * awardList：各节点值list
     */
    private Integer alreadyCount;
    private List<SealAward> awardList;

    @Data
    @AllArgsConstructor
    public static class SealAward {

        /**
         * sealNum：节点值目标数
         * avatarId：奖励道具id
         * avatarName：奖励道具名称
         * avatarNum: 奖励道具数量
         * reachFlag：是否达到 true/false
         * receiveFlag: 是否已领取 true/false
         * openLogFlag1: 大数据打点标识1(是否达成
         * openLogFlag2：大数据打点标识2（是否领取
         */
        private Integer sealNum;
        private Integer avatarId;
        private String avatarName;
        private Integer avatarNum;
        private boolean reachFlag;
        private boolean receiveFlag;
        private boolean openLogFlag1;
        private boolean openLogFlag2;
    }

    public static void matchSealNum(Integer alreadyCount, SealAwardVo vo){
        List<SealAward> awardList = vo.getAwardList();
        awardList.forEach(sealAward -> {
            // 如果当前印章数大于或等于 节点值目标数，则将当前节点标识设置为true
            if(alreadyCount.compareTo(sealAward.getSealNum()) >= 0){
                sealAward.setReachFlag(true);
            }
        });
        vo.setAwardList(awardList);
    }


    public static SealAwardVo getInstance(){
        return SealAwardVo.InitSealAward.SEAL_AWARD;
    }

    private static class InitSealAward{
        private static final SealAwardVo SEAL_AWARD = new SealAwardVo(0, new ArrayList<>(Arrays.asList(
                new SealAward(SealAvatarEnum.NUM_10.getSealNum(),
                        SealAvatarEnum.NUM_10.getAvatarId(),
                        SealAvatarEnum.NUM_10.getAvatarName(),
                        SealAvatarEnum.NUM_10.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_30.getSealNum(),
                        SealAvatarEnum.NUM_30.getAvatarId(),
                        SealAvatarEnum.NUM_30.getAvatarName(),
                        SealAvatarEnum.NUM_30.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_60.getSealNum(),
                        SealAvatarEnum.NUM_60.getAvatarId(),
                        SealAvatarEnum.NUM_60.getAvatarName(),
                        SealAvatarEnum.NUM_60.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_90.getSealNum(),
                        SealAvatarEnum.NUM_90.getAvatarId(),
                        SealAvatarEnum.NUM_90.getAvatarName(),
                        SealAvatarEnum.NUM_90.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_120.getSealNum(),
                        SealAvatarEnum.NUM_120.getAvatarId(),
                        SealAvatarEnum.NUM_120.getAvatarName(),
                        SealAvatarEnum.NUM_120.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_150.getSealNum(),
                        SealAvatarEnum.NUM_150.getAvatarId(),
                        SealAvatarEnum.NUM_150.getAvatarName(),
                        SealAvatarEnum.NUM_150.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_180.getSealNum(),
                        SealAvatarEnum.NUM_180.getAvatarId(),
                        SealAvatarEnum.NUM_180.getAvatarName(),
                        SealAvatarEnum.NUM_180.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_210.getSealNum(),
                        SealAvatarEnum.NUM_210.getAvatarId(),
                        SealAvatarEnum.NUM_210.getAvatarName(),
                        SealAvatarEnum.NUM_210.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_240.getSealNum(),
                        SealAvatarEnum.NUM_240.getAvatarId(),
                        SealAvatarEnum.NUM_240.getAvatarName(),
                        SealAvatarEnum.NUM_240.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_270.getSealNum(),
                        SealAvatarEnum.NUM_270.getAvatarId(),
                        SealAvatarEnum.NUM_270.getAvatarName(),
                        SealAvatarEnum.NUM_270.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_300.getSealNum(),
                        SealAvatarEnum.NUM_300.getAvatarId(),
                        SealAvatarEnum.NUM_300.getAvatarName(),
                        SealAvatarEnum.NUM_300.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_350.getSealNum(),
                        SealAvatarEnum.NUM_350.getAvatarId(),
                        SealAvatarEnum.NUM_350.getAvatarName(),
                        SealAvatarEnum.NUM_350.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_400.getSealNum(),
                        SealAvatarEnum.NUM_400.getAvatarId(),
                        SealAvatarEnum.NUM_400.getAvatarName(),
                        SealAvatarEnum.NUM_400.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_450.getSealNum(),
                        SealAvatarEnum.NUM_450.getAvatarId(),
                        SealAvatarEnum.NUM_450.getAvatarName(),
                        SealAvatarEnum.NUM_450.getAvatarNum(),
                        false, false, false, false),
                new SealAward(SealAvatarEnum.NUM_500.getSealNum(),
                        SealAvatarEnum.NUM_500.getAvatarId(),
                        SealAvatarEnum.NUM_500.getAvatarName(),
                        SealAvatarEnum.NUM_500.getAvatarNum(),
                        false, false, false, false)
        )));
    }

}