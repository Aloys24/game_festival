package com.miniw.fesweb.params.vo;

import com.miniw.fespersistence.model.TAwardRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 获奖记录vo
 *
 * @author luoquan
 * @date 2021/08/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AwardRecordVo {

    private List<RecordVo> recordVoList;
    private long total;

    @Data
    @NoArgsConstructor
    public static class RecordVo{
        /**
         * sendTime：发放时间
         * recordContent：发放内容（道具名*数量
         */
        private String sendTime;
        private String recordContent;
    }


    public static RecordVo convert(TAwardRecord tAwardRecord) {
        RecordVo recordVo = new RecordVo();
        recordVo.setSendTime(tAwardRecord.getTime());
        recordVo.setRecordContent(tAwardRecord.getContent());
        return recordVo;
    }


}
