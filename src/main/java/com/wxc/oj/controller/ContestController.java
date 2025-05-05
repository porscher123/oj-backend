package com.wxc.oj.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.annotation.AuthCheck;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.PageRequest;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.model.dto.contest.ContestAddRequest;
import com.wxc.oj.model.dto.contest.ContestProblemAddRequest;
import com.wxc.oj.model.dto.contest.RegisterDTO;
import com.wxc.oj.model.po.Contest;
import com.wxc.oj.model.po.ContestProblem;
import com.wxc.oj.model.vo.ContestProblemVO;
import com.wxc.oj.model.vo.ContestVO;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.service.ContestProblemService;
import com.wxc.oj.service.ContestService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
    public BaseResponse<List<ContestProblemVO>> getContestProblems(@RequestParam Long contestId) {
        List<ContestProblemVO> problemVOListByContestId
                = contestService.getContestProblemVOListByContestId(contestId);
        return ResultUtils.success(problemVOListByContestId);
    }
}


