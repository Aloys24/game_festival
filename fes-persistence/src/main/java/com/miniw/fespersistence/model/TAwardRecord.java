package com.miniw.fespersistence.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * 棋盘落点奖励记录
 *
 * @author luoquan
 * @date 2021/08/30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TAwardRecord implements Serializable {
    private static final long serialVersionUID = -82678655075479967L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发放事件
     */
    @TableField("time")
    private String time;

    /**
     * 发放内容
     */
    @TableField("content")
    private String content;

    /**
     * 迷你号
     */
    @TableField("uid")
    private Long uid;

}