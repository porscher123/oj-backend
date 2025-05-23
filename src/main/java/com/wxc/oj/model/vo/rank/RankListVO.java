package com.wxc.oj.model.vo.rank;

import com.wxc.oj.model.vo.contest.ContestProblemVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RankListVO {
    private List<RankItem> data;
    private Map<Integer, Integer> problem;
}
