package com.miniw.fesweb.params.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 购买礼包发送邮件失败dto
 *
 * @author luoquan
 * @date 2021/08/24
 */
@Data
@AllArgsConstructor
public class EmailFailedDto {

    /**
     * uid: 当前迷你号
     * type：礼包类型 柳仙儿正式购买：liuxianer_official 月无双正式购买：yuewushuang_official
     * skinQuery:  需要查询的skinId集
     * v6: 打点类型
     * attachMap： 奖励的道具id及数量
     * avatarName:  道具名称
     * avatarNum： 道具奖励
     */
    private Long uid;
    private Map<Integer, Integer> attachMap;
    private String type;
    private int v6;
    Map<Integer, Boolean> skinQuery;
    private String avatarName;
    private Integer avatarNum;

}
