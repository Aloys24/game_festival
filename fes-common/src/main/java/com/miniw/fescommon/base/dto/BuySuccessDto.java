package com.miniw.fescommon.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 购买成功dto
 *
 * @author luoquan
 * @date 2021/08/23
 */
@Data
@AllArgsConstructor
public class BuySuccessDto implements Serializable {

    /**
     * uid：当前迷你号
     * type：购买类型（liuxianer_presale、 liuxianer_official、yuewushuang_presale、yuewushuang_official
     * date：购买日期
     * attachMap: 邮件附件id及数量
     * newSkinMap：换装装扮道具id及数量
     */
    private Long uid;
    private String type;
    private Date date;
    private Map<Integer, Integer> attachMap;
    private Map<Integer, Integer> newSkinMap;


}
