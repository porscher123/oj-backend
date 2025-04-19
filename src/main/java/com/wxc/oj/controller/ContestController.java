package com.wxc.oj.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxc.oj.annotation.AuthCheck;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.model.dto.contest.ContestAddRequest;
import com.wxc.oj.model.dto.contest.ContestProblemAddRequest;
import com.wxc.oj.model.entity.Contest;
import com.wxc.oj.model.entity.ContestProblem;
import com.wxc.oj.model.vo.ContestVO;
import com.wxc.oj.service.ContestProblemService;
import com.wxc.oj.service.ContestService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wxc.oj.enums.UserRoleEnum.ADMIN;
import static org.springframework.beans.BeanUtils.copyProperties;
@RestController
@RequestMapping("contest")
@Slf4j(topic = "ContestController🤣🤣🤣🤣🤣")
public class ContestController {

    @Resource
    ContestService contestService;


    @Resource
    ContestProblemService contestProblemService;


    /**
     * 创建比赛并从题库选择题目添加到比赛中
     * @param contestAddRequest
     * @return
     */
    @PostMapping("add")
    @AuthCheck(mustRole = ADMIN)
    public BaseResponse<ContestVO> addContest(@RequestBody ContestAddRequest contestAddRequest) {
        Contest contest = new Contest();
        copyProperties(contestAddRequest, contest);
        contest.setStatus(0);
        contestService.contestInStatus_0(contest);
        Long contestId = contest.getId();
        List<Long> problems = contestAddRequest.getProblems();
        int idx = 0;
        for (Long problemId : problems) {
            ContestProblem contestProblem = new ContestProblem();
            contestProblem.setContestId(contestId);
            contestProblem.setProblemId(problemId);
            contestProblem.setPindex(idx++);
            contestProblemService.save(contestProblem);
        }
        ContestVO contestVO = contestService.getContestVO(contest);
        return ResultUtils.success(contestVO);
    }
    @PostMapping("addContestProblem")
    @AuthCheck(mustRole = ADMIN)
    public BaseResponse addProblemToContest(@RequestBody ContestProblemAddRequest contestProblemAddRequest) {

        ContestProblem contestProblem = new ContestProblem();
        copyProperties(contestProblemAddRequest, contestProblem);
        boolean save = contestProblemService.save(contestProblem);
        if (!save) {
            return ResultUtils.error(500, "添加失败");
        }
        return ResultUtils.success(contestProblem);
    }

    @GetMapping("problems")
    @AuthCheck(mustRole = ADMIN)
    public BaseResponse<List<ContestProblem>> getProblemsByContestId(@RequestParam Long contestId) {
        LambdaQueryWrapper<ContestProblem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ContestProblem::getContestId, contestId).orderByAsc(ContestProblem::getPindex);
        List<ContestProblem> contestProblems = contestProblemService.list(lambdaQueryWrapper);
        return ResultUtils.success(contestProblems);
    }


    /**
     * 比赛报名接口
     * TODO:
     *  1. 验证contest的status == 0, 只能报名未开始的比赛
     *  2. 验证用户是否已经报名了该比赛, 避免重复报名
     *  3. 获取当前用户User的ID
     *  4. 创建比赛报名记录: ContestRegistration并插入数据库
     *  5. 返回ok
     *  用户比赛报名接口
     * @return
     */
    @PostMapping("register")
    public BaseResponse<?> register(@RequestParam Long contestId) {
//        contestService.register(contestId);
        return ResultUtils.success(null);
    }

    /**
     * 取消报名接口
     * todo:
     *
     * @param contestId
     * @return
     */
    @PostMapping("unregister")
    public BaseResponse<?> unregister(@RequestParam Long contestId) {
//        contestService.register(contestId);
        return ResultUtils.success(null);
    }
    /**
     * 获取当前正在进行/比赛结束的比赛的用户排名
     * todo:
     *  1. 限制contest的status = 1 或 2
     *
     * @param contestId
     * @return
     */
    @GetMapping("playersRank")
    public BaseResponse<?> getPlayersRank(@RequestParam Long contestId) {

        return ResultUtils.success(null);
    }



}


