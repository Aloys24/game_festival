package com.miniw.fesweb.params.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 骰子dto
 *
 * @author luoquan
 * @date 2021/08/28
 */
@Data
@AllArgsConstructor
public class DiceDto {

    private AtomicInteger faceValue;


    public static DiceDto getInstance(){
        return DiceDto.InitDice.FACE_VALUE;
    }

    private static class InitDice{
        private static final DiceDto FACE_VALUE = new DiceDto(new AtomicInteger());
    }


}
