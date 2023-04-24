package com.miniw.fesweb.params.base;

import lombok.Getter;
import org.springframework.util.CollectionUtils;

import javax.validation.ValidationException;
import java.util.Collection;

/**
 * 分享给好友base
 *
 * @author luoquan
 * @date 2021/09/01
 */
@Getter
public class ShareFriendsBase {

    /**
     * fIds：好友迷你号id列表
     * uid: 当前玩家迷你号
     */
    private final Collection<Long> fIds;
    private final Long uid;

    public ShareFriendsBase(Collection<Long> fIds, Long uid) {

        if (null == uid) {
            throw new ValidationException("uid 不能为空");
        }

        if (CollectionUtils.isEmpty(fIds)) {
            throw new ValidationException("fIds 不能为空");
        }

        this.fIds = fIds;
        this.uid = uid;
    }

}
