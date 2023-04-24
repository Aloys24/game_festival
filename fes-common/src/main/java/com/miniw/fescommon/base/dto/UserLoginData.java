package com.miniw.fescommon.base.dto;

import lombok.Data;

/**
 * 用户登录数据
 *
 * @author luoquan
 * @date 2021/08/12
 */
@Data
public class UserLoginData {


    /**
     * uin： 迷你号
     * langId：语言
     * ver： 版本
     * apiId： 渠道
     * country： 国家
     * ts： 时间戳
     *
     */
    private String uin;
    private String langId;
    private String ver;
    private String apiId;
    private String country;
    private long ts;


}
