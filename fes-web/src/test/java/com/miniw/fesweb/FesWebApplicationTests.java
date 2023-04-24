package com.miniw.fesweb;

import cn.hutool.core.date.DateUtil;
import com.miniw.fescommon.base.vo.Result;
import com.miniw.fescommon.constant.FestivalConstant;
import com.miniw.fescommon.constant.RandomEventEnum;
import com.miniw.fescommon.constant.RedisKeyConstant;
import com.miniw.fescommon.utils.RedisUtil;
import com.miniw.fesweb.params.base.SkinQueryBase;
import com.miniw.fesweb.params.dto.GetDiceDto;
import com.miniw.fesweb.params.vo.SealAwardVo;
import com.miniw.gameapi.api.EmailApi;
import com.miniw.gameapi.api.MiniCoinApi;
import com.miniw.gameapi.exception.GameApiException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
class FesWebApplicationTests {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private MiniCoinApi miniCoinApi;
    @Resource
    private EmailApi emailApi;


    @Test
    void contextLoads() {
    }

    @Test
    void testException() {
        System.out.println(Result.success("test success"));
//        throw new FestivalException("test festivalException", new Result(ResultCode.SYS_BUSY.getCode(), ResultCode.SYS_BUSY.getMsg()));
    }

    @Test
    void tetGetUidBase(){
        System.out.println(new SkinQueryBase(111L));
    }

    @Test
    void testGameApiException(){
        String msg = "1111|消耗迷你币|错误的返回码|FAIL";
        List<String> errorMsg = Arrays.asList(msg.split("\\|"));
        System.out.println(errorMsg.stream());
    }

    @Test
    void testSealAwardVo(){
        final SealAwardVo vo = new SealAwardVo();
        System.out.println(vo.getAwardList().size());
    }

    @Test
    void testAlready(){
        redisUtil.hIncrement(RedisKeyConstant.RABBIT_SEAL_COUNT, "1000039921", 10);
    }

    @Test
    void testMiniCoin(){

    }

//    void testSealRecord(){
//        String recordKey = String.format("%s%s", RedisKeyConstant.SEAL_RECORD, uid);
//        if(redisUtil.hasKey(recordKey)){
//            redisUtil.lGet(recordKey, 0, 14);
//        }
//
//
//    }

    @Test
    void testDice(){

    }

    @Test
    void testRandomEvent(){
        List<Integer> list =  Arrays.asList(RandomEventEnum.RANDOM_1_1001.getEventId(),
                RandomEventEnum.RANDOM_1_1002.getEventId(),
                RandomEventEnum.RANDOM_1_1003.getEventId(),
                RandomEventEnum.RANDOM_1_1004.getEventId(),
                RandomEventEnum.RANDOM_1_1005.getEventId(),
                RandomEventEnum.RANDOM_1_1006.getEventId()
        );
        Collections.shuffle(list);
        System.out.println(list.get((int) (Math.random() * list.size())));

    }

    @Test
    void testPosition() {
        int i = 25;
        int n = 20;

        System.out.println(25 % 20);
    }


    @Test
    void testEmail() throws GameApiException {
//        Map<Integer, Integer> attachMap = new HashMap<>(1);
//        attachMap.put(10000, 1);
        emailApi.sendEmail(995244089L, "活动测试",
                "这是一封测试邮件",
                "https://h5.mini1.cn/p/maf/?portrait=2&openBrowser=3",
                new HashMap<>(1), null);
    }

    @Test
    void testExpried() {
        String key = String.format("%s%s", RedisKeyConstant.GET_COMMON_LIMIT, 1000040751);


        redisUtil.hPutAndEx(key, FestivalConstant.DICE_GET_TYPE_LOGIN,
                new GetDiceDto(FestivalConstant.DICE_GET_TYPE_LOGIN, false, true),
                DateUtil.endOfDay(new Date()));

        redisUtil.hPutAndEx(key, FestivalConstant.DICE_GET_TYPE_INVITE,
                new GetDiceDto(FestivalConstant.DICE_GET_TYPE_INVITE, false, false),
                DateUtil.endOfDay(new Date()));

        redisUtil.hPutAndEx(key, FestivalConstant.DICE_GET_TYPE_ONLINE,
                new GetDiceDto(FestivalConstant.DICE_GET_TYPE_ONLINE, false, false),
                DateUtil.endOfDay(new Date()));
    }


}
