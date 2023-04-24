package com.miniw.fesexternal.client.openlogs;


import com.miniw.fesexternal.client.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 迷你玩大数据打点服务
 *
 * @author luoquan
 * @date 2021/07/30
 */
@FeignClient(name = "openLogsClient", configuration = FeignConfig.class, url = "https://tj2.mini1.cn")
public interface OpenLogsClient {

    /**
     * 数据打点
     *
     * @param k       打点id
     * @param uin     迷你号
     * @param apiId   渠道
     * @param version 版本
     * @param country 国家
     * @param lang    语言
     * @param v6      业务字段
     * @param v7      业务字段
     * @param v8      业务字段
     */
    @GetMapping("/miniworld")
    void openLog(@RequestParam("k") String k,
                 @RequestParam("v1") String uin,
                 @RequestParam("v2") String apiId,
                 @RequestParam("v3") String version,
                 @RequestParam("v4") String country,
                 @RequestParam("v5") String lang,
                 @RequestParam("v6") int v6,
                 @RequestParam("v7") Integer v7,
                 @RequestParam("v8") Integer v8
    );


    /*
     * 可查看文档：https://mini1.feishu.cn/sheets/shtcnmhICaSgGUtodCa0aQmMQYb
     */


}
