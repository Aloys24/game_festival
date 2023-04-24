package com.miniw.fesweb.params.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 随机事件-回退
 *
 * @author luoquan
 * @date 2021/08/31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackRandomDto {

    /**
     * actStep: 计算之后的倒退实际落点位置
     * returnStartFlag： 是否倒退回起点 true/false
     */
    private Integer actStep;
    private boolean returnStartFlag;


}
