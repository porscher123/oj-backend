package com.wxc.oj.model.vo.rank;

import com.wxc.oj.model.vo.ContestProblemVO;
import com.wxc.oj.model.vo.ProblemVO;
import lombok.Data;

@Data
public class RankProblemVO extends ContestProblemVO {
    private boolean firstBlood;
}
