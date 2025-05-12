package com.wxc.oj.model.vo.rank;

import com.wxc.oj.model.vo.UserVO;
import lombok.Data;
import java.util.Map;

@Data
public class RankItem {
    private UserVO userVO;
    private int totalScore;
    private long usedTime;
    private boolean submitted;
    private Map<Long, RankProblemVO> problemDetails; // 题目ID -> 题目详情
}
