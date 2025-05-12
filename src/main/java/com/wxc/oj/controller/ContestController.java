package com.wxc.oj.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.wxc.oj.annotation.AuthCheck;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.PageRequest;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.enums.contest.ContestEnum;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.dto.contest.*;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.dto.submission.SubmissionQueryDTO;
import com.wxc.oj.model.po.Contest;
import com.wxc.oj.model.po.ContestProblem;
import com.wxc.oj.model.po.ContestSubmission;
import com.wxc.oj.model.vo.*;
import com.wxc.oj.model.vo.rank.RankListVO;
import com.wxc.oj.service.ContestProblemService;
import com.wxc.oj.service.ContestService;
import com.wxc.oj.service.ContestSubmissionService;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wxc.oj.enums.UserRoleEnum.ADMIN;
import static org.springframework.beans.BeanUtils.copyProperties;
@RestController
@RequestMapping("contest")
@Slf4j(topic = "ContestController🤣🤣🤣🤣🤣")
public class ContestController {

    @Getter
    @Resource
    ContestService contestService;


    @Resource
    ContestProblemService contestProblemService;


    @Resource
    ContestSubmissionService contestSubmissionService;


    /**
     * 创建比赛并从题库选择题目添加到比赛中
     * @param contestAddRequest
     * @return
     */
    @PostMapping("add")
    @AuthCheck(mustRole = ADMIN)
    public BaseResponse<ContestVO> addContest(HttpServletRequest request,
                                              @RequestBody ContestAddRequest contestAddRequest) {
        Contest contest = new Contest();
        copyProperties(contestAddRequest, contest);
        contest.setStatus(0);
        contestService.contestInStatus_0(request,contest);
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
        ContestVO contestVO = contestService.getContestVOWithProblemListByContest(contest);
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
    public BaseResponse<?> register(@RequestBody RegisterDTO registerDTO) {
        boolean register = contestService.register(registerDTO.getUserId(), registerDTO.getContestId());
        return ResultUtils.success(register);
    }

    @DeleteMapping("cancelReg")
    public BaseResponse<?> cancelReg(@RequestBody RegisterDTO registerDTO) {
        boolean register = contestService.cancelRegistration(registerDTO.getUserId(), registerDTO.getContestId());
        return ResultUtils.success(register);
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

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ContestVO>> listProblemVOByPage(@RequestBody PageRequest pageRequest) {
        Page<ContestVO> contestVOPage = contestService.getContestVOPage(pageRequest);
        return ResultUtils.success(contestVOPage);
    }

    @GetMapping("/get/vo")
    public BaseResponse<ContestVO> getContestVOById(@RequestParam("id") Long contestId) {
        ContestVO contestVO = contestService.getContestVOByContestId(contestId);
        return ResultUtils.success(contestVO);
    }

    @PostMapping("user/isReg")
    public BaseResponse<Boolean> isReg(@RequestBody RegisterDTO registerDTO) {
        boolean reg = contestService.findUserInContest(registerDTO.getUserId(),  registerDTO.getContestId());
        return ResultUtils.success(reg);
    }
    @PostMapping("user/canReg")
    public BaseResponse<Boolean> canReg(@RequestBody RegisterDTO registerDTO) {
        boolean reg = contestService.canRegister(registerDTO.getUserId(),  registerDTO.getContestId());
        return ResultUtils.success(reg);
    }


    /**
     * 获取某个contest下的所有题目
     * @param contestId
     * @return
     */
    @GetMapping("problems")
    public BaseResponse<List<ContestProblemVO>> getContestProblems(@RequestParam Long contestId, @RequestParam Long userId) {
        List<ContestProblemVO> problemVOListByContestId
                = contestService.getContestProblemVOListByContestId(contestId, userId);
        return ResultUtils.success(problemVOListByContestId);
    }



    @GetMapping("problem/get")
    public BaseResponse<ContestProblemVO> getContestProblemByIndex(@RequestParam Long contestId,
                                                            @RequestParam Integer index) {
        ContestProblemVO contestProblemVO = contestService.getContestProblemByIndex(contestId, index);
        return ResultUtils.success(contestProblemVO);
    }






    /**
     * 返回用户对于比赛的权限
     * @param contestId
     * @param userId
     * @return
     */
    @GetMapping("auth")
    public BaseResponse<UserAuthInContestVO> getUserAuthInContest(@RequestParam Long contestId, @RequestBody Long userId) {
        return null;
    }


    /**
     * 在比赛中提交代码
     * todo:
     *  1. 只有在比赛进行中才能提交代码
     */
    @PostMapping("problem/submit")
    public BaseResponse<ContestSubmissionVO> submit(@RequestBody SubmitInContestDTO submitInContestDTO) {
//        Long contestId = submitInContestDTO.getContestId();
//        Contest contest = contestService.getById(contestId);
//        if (contest.getStatus() != ContestEnum.RUNNING.getCode()) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR);
//        }
        ContestSubmissionVO contestSubmissionVO = contestSubmissionService.submitCode(submitInContestDTO);
        return ResultUtils.success(contestSubmissionVO);
    }



    /**
     * 获取一个contest的所有submission
     * @param
     * @return
     */
    @PostMapping("submissions")
    public BaseResponse<Page<ContestSubmissionVO>> getContestSubmissions(
            @RequestBody ContestSubmissionListDTO contestSubmissionListDTO) {
        Page<ContestSubmissionVO> ans = contestSubmissionService.listSubmissions(contestSubmissionListDTO);
        return ResultUtils.success(ans);
    }

    @GetMapping("submission/get")
    public BaseResponse<ContestSubmissionVO> getContestSubmissionById(@RequestParam Long id) {
        ContestSubmissionVO contestSubmissionVO = contestSubmissionService.getContestSubmissionById(id);
        return ResultUtils.success(contestSubmissionVO);
    }


    @GetMapping("rank")
    public BaseResponse<RankListVO> getContestRank(@RequestParam Long contestId) {
        RankListVO rankList = contestService.getRankList(contestId);
        return ResultUtils.success(rankList);
    }
}


