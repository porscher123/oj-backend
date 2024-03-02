package com.wxc.oj.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.entity.Problem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxc.oj.model.vo.ProblemVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 王新超
* @description 针对表【problem】的数据库操作Service
* @createDate 2024-02-28 14:24:47
*/
public interface ProblemService extends IService<Problem> {
    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validProblem(Problem post, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    LambdaQueryWrapper<Problem> getQueryWrapper(ProblemQueryRequest postQueryRequest);



    /**
     * 获取帖子封装
     *
     * @param post
     * @return
     */
    ProblemVO getProblemVO(Problem post);

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @param request
     * @return
     */
    Page<ProblemVO> getProblemVOPage(Page<Problem> postPage, HttpServletRequest request);
}
