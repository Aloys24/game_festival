package com.miniw.fesweb.params.base;

import com.miniw.fescommon.constant.SealAvatarEnum;
import lombok.Getter;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.Collection;


/**
 * 领取印章奖励base
 *
 * @author luoquan
 * @date 2021/08/26
 */
@Getter
public class ReceiveAwardBase {

    private final Long uid;
    private final Integer num;
    private final Collection<Integer> sealNumList = Arrays.asList(SealAvatarEnum.NUM_10.getSealNum(),
            SealAvatarEnum.NUM_30.getSealNum(),
            SealAvatarEnum.NUM_60.getSealNum(),
            SealAvatarEnum.NUM_90.getSealNum(),
            SealAvatarEnum.NUM_120.getSealNum(),
            SealAvatarEnum.NUM_150.getSealNum(),
            SealAvatarEnum.NUM_180.getSealNum(),
            SealAvatarEnum.NUM_210.getSealNum(),
            SealAvatarEnum.NUM_240.getSealNum(),
            SealAvatarEnum.NUM_270.getSealNum(),
            SealAvatarEnum.NUM_300.getSealNum(),
            SealAvatarEnum.NUM_350.getSealNum(),
            SealAvatarEnum.NUM_400.getSealNum(),
            SealAvatarEnum.NUM_450.getSealNum(),
            SealAvatarEnum.NUM_500.getSealNum()

    );

    public ReceiveAwardBase(Long uid, Integer num) {
        if (null == uid) {
            throw new ValidationException("uid 不能为空");
        }
        // 节点值是否合法
        if (0 != num) {
            if(!sealNumList.contains(num)){
                throw new ValidationException("num 找不到该节点值奖励");
            }
        }
        this.uid = uid;
        this.num = num;
    }

}
