package com.wxc.oj.mapper;

import com.wxc.oj.model.po.ContestSubmission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 王新超
* @description 针对表【contest_submission】的数据库操作Mapper
* @createDate 2025-05-06 14:30:36
* @Entity com.wxc.oj.model.entity.ContestSubmission
*/
public interface ContestSubmissionMapper extends BaseMapper<ContestSubmission> {
    /**
     * 获取指定比赛和题目中每个用户的最高得分提交记录
     * @param contestId 比赛ID
     * @param problemId 题目ID
     * @return 最高得分提交列表
     */
    List<ContestSubmission> selectMaxScoreSubmissionsByContest(
            @Param("contestId") Long contestId
    );
}




