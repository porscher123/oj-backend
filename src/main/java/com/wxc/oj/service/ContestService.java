package com.wxc.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.common.PageRequest;
import com.wxc.oj.model.po.Contest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxc.oj.model.vo.ContestProblemVO;
import com.wxc.oj.model.vo.ContestVO;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.queueMessage.ContestMessage;
import io.lettuce.core.output.ListOfGenericMapsOutput;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 王新超
* @description 针对表【contest】的数据库操作Service
* @createDate 2025-03-24 21:58:05
*/
public interface ContestService extends IService<Contest> {
    void contestInStatus_0(HttpServletRequest request, Contest contest);
    void contestInStatus_1(ContestMessage contest);
    void contestInStatus_2(ContestMessage contest);
    ContestVO getContestVOWithoutProblemListByContest(Contest contest);
    ContestVO getContestVOWithProblemListByContest(Contest contest);
    Page<ContestVO> getContestVOPage(PageRequest pageRequest);

    ContestVO getContestVOByContestId(Long contestId);


    boolean register(Long userId, Long contestId);

    boolean cancelRegistration(Long userId, Long contestId);

    boolean findUserInContest(Long userId, Long contestId);

    boolean canRegister(Long userId, Long contestId);

    List<ProblemVO> getProblemVOListByContestId(Long contestId);

    List<ContestProblemVO> getContestProblemVOListByContestId(Long contestId);
}
