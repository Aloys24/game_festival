package com.miniw.fesweb.params.dto;

import com.miniw.fescommon.constant.BoardAwardRandomEnum;
import com.miniw.fescommon.constant.BoardGameEnum;
import com.miniw.fescommon.constant.RandomEventEnum;
import com.miniw.fescommon.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 奖励概率计算dto
 *
 * @author luoquan
 * @date 2021/09/10
 */
@Data
@AllArgsConstructor
@Getter
@Slf4j
public class EventualityDto {

    private Collection<Integer> boardId;

    /**
     * 奖品概率数组
     */
    private static List<Double> PROB_LIST = Arrays.asList(BoardAwardRandomEnum.AWARD_S.getEventuality(),
            BoardAwardRandomEnum.AWARD_A.getEventuality(),
            BoardAwardRandomEnum.AWARD_B.getEventuality(),
            BoardAwardRandomEnum.AWARD_C.getEventuality()
    );

    /**
     * 奖品type list
     */
    private static List<String> AWARD_TYPE_LIST = Arrays.asList(BoardAwardRandomEnum.AWARD_S.getRandomType(),
            BoardAwardRandomEnum.AWARD_A.getRandomType(),
            BoardAwardRandomEnum.AWARD_B.getRandomType(),
            BoardAwardRandomEnum.AWARD_C.getRandomType()
    );


    /**
     * S 奖励 棋盘id list
     */
    private static List<Integer> AWARD_S = Arrays.asList(BoardGameEnum.BOARD_2.getBoardId(),
            BoardGameEnum.BOARD_7.getBoardId(),
            BoardGameEnum.BOARD_8.getBoardId(),
            BoardGameEnum.BOARD_13.getBoardId(),
            BoardGameEnum.BOARD_17.getBoardId(),
            BoardGameEnum.BOARD_19.getBoardId()
    );

    /**
     * A 奖励 棋盘id list
     */
    private static List<Integer> AWARD_A = Arrays.asList(BoardGameEnum.BOARD_5.getBoardId(),
            BoardGameEnum.BOARD_10.getBoardId(),
            BoardGameEnum.BOARD_15.getBoardId(),
            BoardGameEnum.BOARD_20.getBoardId()
    );

    /**
     * B 奖励 棋盘id list
     */
    private static List<Integer> AWARD_B = Arrays.asList(BoardGameEnum.BOARD_1.getBoardId(),
            BoardGameEnum.BOARD_4.getBoardId(),
            BoardGameEnum.BOARD_6.getBoardId(),
            BoardGameEnum.BOARD_11.getBoardId(),
            BoardGameEnum.BOARD_12.getBoardId(),
            BoardGameEnum.BOARD_14.getBoardId(),
            BoardGameEnum.BOARD_16.getBoardId()
    );

    /**
     * C 奖励 棋盘id list
     */
    private static List<Integer> AWARD_C = Arrays.asList(BoardGameEnum.BOARD_3.getBoardId(),
            BoardGameEnum.BOARD_9.getBoardId(),
            BoardGameEnum.BOARD_18.getBoardId()
    );

    /**
     * eg：普通掷骰子结果： 1. 2. 3. 4. 5. 6
     * 翻倍掷骰子结果： 2. 4. 6. 8. 10. 12
     * 减半掷骰子结果： 1. 2. 3
     */
    private static Collection<Integer> commonDiceNum = Arrays.asList(1, 2, 3, 4, 5, 6);
    private static Collection<Integer> doubleDiceNum = Arrays.asList(2, 4, 6, 8, 10, 12);
    private static Collection<Integer> halfDiceNum = Arrays.asList(1, 2, 3);


    /**
     * 计算并判断是否能走到当前位置
     *
     * @param lastPosition 最后一次棋盘位置
     * @param eventId      可能影响结果的随机事件id（例如2001、2002）
     * @return {@link Integer}
     */
    public static Integer countAward(Integer lastPosition, Integer eventId) {

        // 构建一个比例区段组成的集合(避免概率和不为1)
        List<Double> sortRateList = new ArrayList<>();

        // 概率总和
        double sumRate = 0D;
        for (Double prob : PROB_LIST) {
            sumRate += prob;
        }

        // 概率所占比例
        double rate = 0D;
        for (Double prob : PROB_LIST) {
            rate += prob;
            sortRateList.add(rate / sumRate);
        }

        // Map<Integer, Integer> key为骰子数 value为结果
        Map<Integer, Integer> addResult;
        Collection<Integer> awardList;
        Collection<Integer> retainAll;
        do {
            // 随机生成一个随机数，并排序
            ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
            double random = threadLocalRandom.nextDouble(0, 1);
            sortRateList.add(random);
            Collections.sort(sortRateList);

            // 返回该随机数在比例集合中的索引
            final int indexOf = sortRateList.indexOf(random);
            log.debug(" 当前随机数索引 - {}", indexOf);
            final String awardType = AWARD_TYPE_LIST.get(indexOf);
            log.debug("随到的奖品为 - {}", awardType);

            addResult = new HashMap<>(6);
            awardList = new ArrayList<>();
            // 奖品区间判断
            switch (awardType) {
                case "S":
                    awardList = AWARD_S;
                    break;
                case "A":
                    awardList = AWARD_A;
                    break;
                case "B":
                    awardList = AWARD_B;
                    break;
                case "C":
                    awardList = AWARD_C;
                    break;
                default:
            }

            // 传入最后一次棋盘落点位置，判断有没有可能到达
            if (null == eventId) {
                // 普通
                addResult = CommonUtil.addDiceNum(commonDiceNum, lastPosition);
            } else if (eventId.compareTo(RandomEventEnum.RANDOM_2_2001.getEventId()) == 0) {
                // 翻倍
                addResult = CommonUtil.addDiceNum(doubleDiceNum, lastPosition);
            } else if (eventId.compareTo(RandomEventEnum.RANDOM_2_2002.getEventId()) == 0) {
                // 取半
                addResult = CommonUtil.addDiceNum(halfDiceNum, lastPosition);
            }

            // 将map中计算出得结果取出
            Collection<Integer> addListAll = new ArrayList<>(6);
            addResult.forEach((k, v) -> addListAll.add(v));
            // 交集
            retainAll = CollectionUtils.retainAll(addListAll, awardList);
            log.debug("跟奖品棋盘集 - {} 的交集为 - {}", awardList, retainAll);

            // 移除之前的随机数
            sortRateList.remove(random);
        } while (CollectionUtils.isEmpty(retainAll));

        // 从交集中平概率取出一个骰子数（假定有可能会发生多个结果集
        final List<Integer> retainAll1 = (List<Integer>) retainAll;
        Collections.shuffle(retainAll1);
        Integer retain = retainAll1.get((int) (Math.random() * retainAll1.size()));
        log.debug("交集最后落点位置 - {}", retain);

        // 获取结果值对应的骰子数
        final Integer i = CommonUtil.getKey(addResult, retain);

        // 随机事件类型需要重新计算骰子结果
        AtomicInteger diceNum = new AtomicInteger(i);
        if (eventId != null && eventId.compareTo(RandomEventEnum.RANDOM_2_2001.getEventId()) == 0) {
            // 如果是点数翻倍，则将骰子数减半
            diceNum.getAndSet(i / 2);
        }
        if (eventId != null && eventId.compareTo(RandomEventEnum.RANDOM_2_2002.getEventId()) == 0) {
            // 如果是点数减半, 则将骰子数向上取整
            diceNum.getAndSet((int) Math.floor(i.doubleValue() * 2));
        }
        log.debug("骰子结果为 - {}", diceNum.get());
        return diceNum.get();
    }

}
