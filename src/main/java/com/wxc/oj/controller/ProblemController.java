package com.wxc.oj.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.annotation.AuthCheck;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.DeleteRequest;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.constant.UserConstant;
import com.wxc.oj.enums.UserRoleEnum;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.exception.ThrowUtils;
import com.wxc.oj.model.dto.judge.JudgeCase;
import com.wxc.oj.model.dto.judge.JudgeConfig;
import com.wxc.oj.model.dto.problem.ProblemAddRequest;
import com.wxc.oj.model.dto.problem.ProblemEditRequest;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.dto.problem.ProblemUpdateRequest;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.beans.BeanUtils.resolveSignature;

/**
 * 题目
 */
@RestController
@RequestMapping("problem")
@Slf4j
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserService userService;



    /**
     * 创建题目
     */
    @PostMapping("add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Problem> addProblem(@RequestBody ProblemAddRequest problemAddRequest,
                                   HttpServletRequest request) {
        if (problemAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem problem = new Problem();
        copyProperties(problemAddRequest, problem);
        List<String> tags = problemAddRequest.getTags();
        if (tags != null) {
            problem.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCase = problemAddRequest.getJudgeCase();
        if (judgeCase != null) {
            problem.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        JudgeConfig judgeConfig = problemAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            problem.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        problemService.validProblem(problem, true);
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        problem.setUserId(loginUser.getId());
        problem.setFavorNum(0);
        problem.setThumbNum(0);
        problem.setSubmittedNum(0);
        problem.setAcceptedNum(0);
        // 保存答案
        boolean result = problemService.save(problem);
        // 添加失败
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newProblemId = problem.getId();

        Problem newProblem = problemService.getById(newProblemId);
        return ResultUtils.success(newProblem);
    }

    /**
     * 删除题目(逻辑删除)
     */
    @PostMapping("delete")
    public BaseResponse deleteProblem(@RequestBody DeleteRequest deleteRequest,
                                    HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldProblem.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = problemService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     */
    @PostMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ProblemVO> updateProblem(@RequestBody ProblemUpdateRequest problemUpdateRequest) {
        if (problemUpdateRequest == null || problemUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建要保存到数据库中的problem
        Problem problem = new Problem();
        copyProperties(problemUpdateRequest, problem);
        // 将对象转为json字符串存储
        List<String> tags = problemUpdateRequest.getTags();
        if (tags != null) {
            problem.setTags(JSONUtil.toJsonStr(tags));
        }
        List<JudgeCase> judgeCase = problemUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            problem.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        JudgeConfig judgeConfig = problemUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            problem.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        // 参数校验
        problemService.validProblem(problem, false);
        Long id = problemUpdateRequest.getId();
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        // 要更新的problem不存在
        ThrowUtils.throwIf(oldProblem == null, ErrorCode.NOT_FOUND_ERROR);
        // 执行更新操作
        problemService.updateById(problem);
        Problem newProblem = problemService.getById(oldProblem.getId());

        return ResultUtils.success(problemService.getProblemVO(newProblem));
    }

    /**
     * 根据 id 获取题目
     * GET方法
     */
    @GetMapping("/get/vo")
    public BaseResponse<ProblemVO> getProblemVOById(Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem problem = problemService.getById(id);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(problemService.getProblemVO(problem));
    }
    /**
     * 根据 id 获取题目
     * GET方法 不脱敏
     */
    @GetMapping("/get")
    public BaseResponse<Problem> getProblemById(Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem problem = problemService.getById(id);
        if (problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (!problem.getId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "不能查看其它用户的题目的全部信息");
        }
        return ResultUtils.success(problem);
    }

    /**
     * 分页获取题目列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Problem>> listProblemByPage(@RequestBody ProblemQueryRequest problemQueryRequest) {
        long current = problemQueryRequest.getCurrent();
        long size = problemQueryRequest.getPageSize();
        System.out.println("***************" + current + "       " +size);
        Page<Problem> problemPage = problemService.page(new Page<>(current, size),
                problemService.getQueryWrapper(problemQueryRequest));
        long total = problemPage.getTotal();
        System.out.println("************************" + total);
        return ResultUtils.success(problemPage);
    }

    /**
     * 分页获取列表（封装类）
     * 展示用户可见的部分(普通用户使用)
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse listProblemVOByPage(@RequestBody ProblemQueryRequest problemQueryRequest,
            HttpServletRequest request) {
        long current = problemQueryRequest.getCurrent();
        long size = problemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Problem> ProblemPage = problemService.page(new Page<>(current, size),
                problemService.getQueryWrapper(problemQueryRequest));
        return ResultUtils.success(problemService.getProblemVOPage(ProblemPage));
    }

    /**
     * 分页获取当前用户创建的资源列表
     * @param
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ProblemVO>> listMyProblemVOByPage(@RequestBody ProblemQueryRequest problemQueryRequest,
            HttpServletRequest request) {
        if (problemQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        problemQueryRequest.setUserId(loginUser.getId());
        long current = problemQueryRequest.getCurrent();
        long size = problemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Problem> ProblemPage = problemService.page(new Page<>(current, size),
                problemService.getQueryWrapper(problemQueryRequest));
        return ResultUtils.success(problemService.getProblemVOPage(ProblemPage));
    }



    /**
     * 编辑（用户）
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editProblem(@RequestBody ProblemEditRequest problemEditRequest, HttpServletRequest request) {
        if (problemEditRequest == null || problemEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem Problem = new Problem();
        copyProperties(problemEditRequest, Problem);
        List<String> tags = problemEditRequest.getTags();
        if (tags != null) {
            Problem.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        problemService.validProblem(Problem, false);
        User loginUser = userService.getLoginUser(request);
        Long id = problemEditRequest.getId();
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldProblem.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = problemService.updateById(Problem);
        return ResultUtils.success(result);
    }

}
