package com.miniw.fesweb.params.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 领取印章节点奖励vo
 *
 * @author luoquan
 * @date 2021/08/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveAwardVo {

    private List<ReceiveResult> awardList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceiveResult{
        /**
         * avatarId: 道具id
         * avatarName：道具名称
         * num：道具数量
         */
        private Integer avatarId;
        private String avatarName;
        private Integer num;
    }


}
