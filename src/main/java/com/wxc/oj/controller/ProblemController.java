package com.wxc.oj.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.annotation.AuthCheck;
import com.wxc.oj.common.BaseResponse;
import com.wxc.oj.common.DeleteRequest;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.ResultUtils;
import com.wxc.oj.constant.UserConstant;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.exception.ThrowUtils;
import com.wxc.oj.model.dto.problem.ProblemAddRequest;
import com.wxc.oj.model.dto.problem.ProblemEditRequest;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.dto.problem.ProblemUpdateRequest;
import com.wxc.oj.model.pojo.Problem;
import com.wxc.oj.model.pojo.User;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * 帖子接口
 *

 */
@RestController
@RequestMapping("problem")
@Slf4j
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("add")
    public BaseResponse<Long> addProblem(@RequestBody ProblemAddRequest problemAddRequest, HttpServletRequest request) {
        if (problemAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem problem = new Problem();
        copyProperties(problemAddRequest, problem);
        List<String> tags = problemAddRequest.getTags();
        if (tags != null) {
            problem.setTags(JSONUtil.toJsonStr(tags));
        }
        problemService.validProblem(problem, true);
        // 获取当前用户
        User loginUser = userService.getLoginUser(request.getHeader("token"));
        problem.setUserId(loginUser.getId());
        problem.setFavorNum(0);
        problem.setThumbNum(0);
        boolean result = problemService.save(problem);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newProblemId = problem.getId();
        return ResultUtils.success(newProblemId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteProblem(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request.getHeader("token"));
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
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateProblem(@RequestBody ProblemUpdateRequest problemUpdateRequest) {
        if (problemUpdateRequest == null || problemUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem problem = new Problem();
        copyProperties(problemUpdateRequest, problem);
        List<String> tags = problemUpdateRequest.getTags();
        if (tags != null) {
            problem.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        problemService.validProblem(problem, false);
        Long id = problemUpdateRequest.getId();
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = problemService.updateById(problem);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ProblemVO> getProblemVOById(Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem Problem = problemService.getById(id);
        if (Problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(problemService.getProblemVO(Problem, request));
    }

    /**
     * 分页获取列表（仅管理员）
     * @param
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Problem>> listProblemByPage(@RequestBody ProblemQueryRequest problemQueryRequest) {
        long current = problemQueryRequest.getCurrent();
        long size = problemQueryRequest.getPageSize();
        Page<Problem> ProblemPage = problemService.page(new Page<>(current, size),
                problemService.getQueryWrapper(problemQueryRequest));
        return ResultUtils.success(ProblemPage);
    }

    /**
     * 分页获取列表（封装类）
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ProblemVO>> listProblemVOByPage(@RequestBody ProblemQueryRequest problemQueryRequest,
            HttpServletRequest request) {
        long current = problemQueryRequest.getCurrent();
        long size = problemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Problem> ProblemPage = problemService.page(new Page<>(current, size),
                problemService.getQueryWrapper(problemQueryRequest));
        return ResultUtils.success(problemService.getProblemVOPage(ProblemPage, request));
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
        User loginUser = userService.getLoginUser(request.getHeader("token"));
        problemQueryRequest.setUserId(loginUser.getId());
        long current = problemQueryRequest.getCurrent();
        long size = problemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Problem> ProblemPage = problemService.page(new Page<>(current, size),
                problemService.getQueryWrapper(problemQueryRequest));
        return ResultUtils.success(problemService.getProblemVOPage(ProblemPage, request));
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
        User loginUser = userService.getLoginUser(request.getHeader("token"));
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
