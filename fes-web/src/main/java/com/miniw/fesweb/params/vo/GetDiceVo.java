package com.miniw.fesweb.params.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获得骰子返回vo
 *
 * @author luoquan
 * @date 2021/08/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetDiceVo {

    /**
     * diceType: 骰子类型： C/普通骰子 U/万能骰子
     * diceNum：获得的骰子数量
     *
     */
    private String diceType;
    private Integer diceNum;

}
