package com.miniw.fesweb.params.base;

import com.miniw.fescommon.constant.RandomEventEnum;
import lombok.Getter;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.List;

/**
 * 棋盘base
 *
 * @author luoquan
 * @date 2021/08/27
 */
@Getter
public class BoardBase {

    private final Long uid;
    private final Integer step;
    private final Integer eventId;

    private final List<Integer> eventList = Arrays.asList(RandomEventEnum.RANDOM_2_2001.getEventId(),
            RandomEventEnum.RANDOM_2_2002.getEventId());


    public BoardBase(Long uid, Integer step, Integer eventId){
        if(null == uid){
            throw new ValidationException("uid 不能为空");
        }

        if(eventId != 0 && !eventList.contains(eventId)){
            throw new ValidationException("eventId 参数非法");
        }

        if(null != step && step < 0){
            throw new ValidationException("step 参数非法");
        }

        this.uid = uid;
        this.step = step;
        this.eventId = eventId;
    }


}
