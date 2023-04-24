package com.miniw.fesweb.ctl;

import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.constant.RedisKeyConstant;
import com.miniw.fescommon.utils.RedisUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;


/**
 * 导出数据控制器
 *
 * @author luoquan
 * @date 2021/09/22
 */
@RestController
@RequestMapping("/exportExcel")
public class ExportDataController {

    @Resource
    private RedisUtil redisUtil;


    @GetMapping("/getAllYuewushuang")
    public Result<Boolean> getAllYuewushuang() throws IOException, ParseException {
        redisUtil.hScan(RedisKeyConstant.BUY_SUCCESS_TEMP, 100);

        return Result.success(true);
    }



}
