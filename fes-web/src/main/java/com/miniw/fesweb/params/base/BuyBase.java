package com.miniw.fesweb.params.base;

import com.miniw.fescommon.constant.FestivalConstant;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;
import java.util.Arrays;


/**
 * 预售请求base
 *
 * @author luoquan
 * @date 2021/08/21
 */
@Getter
public class BuyBase {

    /**
     * type：购买类型（ 柳仙儿：liuxianer  月无双：yuewushuang
     * uid：当前玩家迷你号
     */
    private final String type;
    private final Long uid;

    public BuyBase(String type, Long uid) {
        if(null == uid){
            throw new ValidationException("uid 不能为空");
        }

        if(StringUtils.isBlank(type)){
            throw new ValidationException("type 不能为空");
        }

        if(!Arrays.asList(FestivalConstant.LIUXIANER_PRESALE_TYPE,
                FestivalConstant.YUEWUSHUANG_PRESALE_TYPE,
                FestivalConstant.LIUXIANER_OFFICIAL_TYPE,
                FestivalConstant.YUEWUSHUANG_OFFICIAL_TYPE
                ).contains(type)){
            throw new ValidationException("type 参数不匹配");
        }

        this.type = type;
        this.uid = uid;
    }
}
