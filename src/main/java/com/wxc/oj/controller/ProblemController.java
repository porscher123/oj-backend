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
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子接口
 *

 */
@RestController
@RequestMapping("/problem")
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
     * @param ProblemAddRequest
     * @param request
     * @return
     */
    @PostMapping("add")
    public BaseResponse<Long> addProblem(@RequestBody ProblemAddRequest ProblemAddRequest, HttpServletRequest request) {
        if (ProblemAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem Problem = new Problem();
        BeanUtils.copyProperties(ProblemAddRequest, Problem);
        List<String> tags = ProblemAddRequest.getTags();
        if (tags != null) {
            Problem.setTags(JSONUtil.toJsonStr(tags));
        }
        ProblemService.validProblem(Problem, true);
        User loginUser = userService.getLoginUser(request);
        Problem.setUserId(loginUser.getId());
        Problem.setFavorNum(0);
        Problem.setThumbNum(0);
        boolean result = ProblemService.save(Problem);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newProblemId = Problem.getId();
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
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Problem oldProblem = ProblemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldProblem.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = ProblemService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param ProblemUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateProblem(@RequestBody ProblemUpdateRequest ProblemUpdateRequest) {
        if (ProblemUpdateRequest == null || ProblemUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem Problem = new Problem();
        BeanUtils.copyProperties(ProblemUpdateRequest, Problem);
        List<String> tags = ProblemUpdateRequest.getTags();
        if (tags != null) {
            Problem.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        ProblemService.validProblem(Problem, false);
        long id = ProblemUpdateRequest.getId();
        // 判断是否存在
        Problem oldProblem = ProblemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = ProblemService.updateById(Problem);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ProblemVO> getProblemVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem Problem = ProblemService.getById(id);
        if (Problem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(ProblemService.getProblemVO(Problem, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param ProblemQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Problem>> listProblemByPage(@RequestBody ProblemQueryRequest ProblemQueryRequest) {
        long current = ProblemQueryRequest.getCurrent();
        long size = ProblemQueryRequest.getPageSize();
        Page<Problem> ProblemPage = ProblemService.page(new Page<>(current, size),
                ProblemService.getQueryWrapper(ProblemQueryRequest));
        return ResultUtils.success(ProblemPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param ProblemQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ProblemVO>> listProblemVOByPage(@RequestBody ProblemQueryRequest ProblemQueryRequest,
            HttpServletRequest request) {
        long current = ProblemQueryRequest.getCurrent();
        long size = ProblemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Problem> ProblemPage = ProblemService.page(new Page<>(current, size),
                ProblemService.getQueryWrapper(ProblemQueryRequest));
        return ResultUtils.success(ProblemService.getProblemVOPage(ProblemPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     * @param ProblemQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ProblemVO>> listMyProblemVOByPage(@RequestBody ProblemQueryRequest ProblemQueryRequest,
            HttpServletRequest request) {
        if (ProblemQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ProblemQueryRequest.setUserId(loginUser.getId());
        long current = ProblemQueryRequest.getCurrent();
        long size = ProblemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Problem> ProblemPage = ProblemService.page(new Page<>(current, size),
                ProblemService.getQueryWrapper(ProblemQueryRequest));
        return ResultUtils.success(ProblemService.getProblemVOPage(ProblemPage, request));
    }



    /**
     * 编辑（用户）
     *
     * @param ProblemEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editProblem(@RequestBody ProblemEditRequest ProblemEditRequest, HttpServletRequest request) {
        if (ProblemEditRequest == null || ProblemEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Problem Problem = new Problem();
        BeanUtils.copyProperties(ProblemEditRequest, Problem);
        List<String> tags = ProblemEditRequest.getTags();
        if (tags != null) {
            Problem.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        ProblemService.validProblem(Problem, false);
        User loginUser = userService.getLoginUser(request);
        long id = ProblemEditRequest.getId();
        // 判断是否存在
        Problem oldProblem = ProblemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldProblem.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = ProblemService.updateById(Problem);
        return ResultUtils.success(result);
    }

}
