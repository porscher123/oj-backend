package com.wxc.oj.model.vo.rank;

import com.wxc.oj.model.vo.contest.ContestProblemVO;
import lombok.Data;

@Data
public class RankProblemVO extends ContestProblemVO {
    private boolean firstBlood;

    private Long timeUsed;
}
