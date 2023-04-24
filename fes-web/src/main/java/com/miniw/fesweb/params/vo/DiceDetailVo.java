package com.miniw.fesweb.params.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 骰子领取详情
 *
 * @author luoquan
 * @date 2021/08/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiceDetailVo {

    /**
     * 前端要求的数据返回格式 -。-
     */
    Map<String, Object> result;

}
