package com.miniw.fesweb.params.base;

import lombok.Getter;

import javax.validation.ValidationException;
import java.util.Arrays;

/**
 * 购买万能骰子base
 *
 * @author luoquan
 * @date 2021/08/30
 */
@Getter
public class BuyUniversalBase {

    private final Long uid;
    private final Integer num;
    private final Integer price;

    public BuyUniversalBase(Long uid, Integer num, Integer price) {

        if (null == uid) {
            throw new ValidationException("uid 不能为空");
        }

        if (null == num) {
            throw new ValidationException("num 不能为空");
        }

        if (null == price) {
            throw new ValidationException("price 不能为空");
        }

        if (!Arrays.asList(1, 5).contains(num)) {
            throw new ValidationException("num 格式有误");
        }

        if (!Arrays.asList(20, 90).contains(price)) {
            throw new ValidationException("price 格式有误");
        }

        this.uid = uid;
        this.num = num;
        this.price = price;
    }

    public static Integer matchV6(Integer diceNum) {
        /*
            v6=购买档位
            1：1个万能骰
            2：5个万能骰
         */
        switch (diceNum) {
            case 1:
                return 1;
            case 5:
                return 2;
            default:
                return 0;
        }
    }

}
