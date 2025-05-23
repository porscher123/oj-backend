package com.wxc.oj.model.vo.rank;

import com.wxc.oj.model.vo.UserVO;
import lombok.Data;
import java.util.Map;

@Data
public class RankItem {
    private Long userId;
    private String userName;
    private int totalScore;
    private long usedTime;
    private Boolean submitted;
    private Map<Integer, RankProblemVO> problemDetails; // 题目ID -> 题目详情
}
