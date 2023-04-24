package com.miniw.fesweb.params.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领取骰子
 *
 * @author luoquan
 * @date 2021/08/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetDiceDto {
    /**
     * type: login:  每日登录活动页领取+1 | invite：每日邀请好友参与活动+1 | online： 每日邀请参与联机游戏+1
     * receiveFlag:  领取标识 true 已领取 false 未领取
     * completeFlag: 完成标识 true 已完成 false 未完成
     */
    private String type;
    private boolean receiveFlag;
    private boolean completeFlag;
}
