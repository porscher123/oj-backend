package com.wxc.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.constant.CommonConstant;
import com.wxc.oj.constant.Level;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.mapper.ProblemMapper;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.po.Problem;
import com.wxc.oj.model.po.Tag;
import com.wxc.oj.model.po.User;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.service.TagService;
import com.wxc.oj.service.UserService;
import com.wxc.oj.utils.SqlUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
* @author 王新超
* @description 针对表【problem】的数据库操作Service实现
* @createDate 2024-02-28 14:24:47
*/
@Service
@Slf4j(topic = "ProblemServiceImpl🏍🏍🏍🏍🏍🏍🏍🏍🏍")
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

    @Resource
    private UserService userService;

    @Resource
    private TagService tagService;

    private boolean checkLevel(String level) {
        Set<String> levels = new HashSet<>();
        levels.add(Level.EASY);
        levels.add(Level.MEDIUM);
        levels.add(Level.HARD);
        return levels.contains(level);
    }

    /**
     * 校验题目是否合法
     * 题目的有些数据创建时可以省略,
     * 等待后期修改
     * @param problem
     * @param add
     */
    @Override
    public void validProblem(Problem problem, boolean add) {

    }

    /**
     * 根据请求的封装对象获取查询包装类
     * 前端会根据题目的标题,内容,难度,标签进行查询
     * @param problemQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest) {
        var queryWrapper = new QueryWrapper<Problem>();
        if (problemQueryRequest == null) {
            return queryWrapper.lambda();
        }
        String title = problemQueryRequest.getTitle();

        // 第1次查数据库,根据tags筛选ids
        List<String> tags = problemQueryRequest.getTags(); // 获取标签列表
        if (tags != null && !tags.isEmpty()) {
            List<Long> problemIds = tagService.getProblemIdsByTagNames(tags);
            queryWrapper.in(!problemIds.isEmpty() && problemIds != null, "id", problemIds);
        }

//        log.info("💕💕💕💕💕💕💕💕" + problemIds.toString() + "💕💕💕💕💕💕");

        Integer level = problemQueryRequest.getLevel();
        String sortField = problemQueryRequest.getSortField();
        String sortOrder = problemQueryRequest.getSortOrder();
        if (sortOrder == null) {
            sortOrder = CommonConstant.SORT_ORDER_ASC;
        }
        if (sortField == null) {
            sortField = "id";
        }
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title)
                .like(StringUtils.isNotBlank(title), "content", title)
                .eq(level != null && level != 6,"level", level);





//        queryWrapper.eq(ObjectUtils.isNotEmpty(id),"id", id);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper.lambda();
    }


    /**
     * 接受DTO对象, 查询满足请求的所有Problem对象,并封装成VO对象
     * @param problemQueryRequest
     * @return
     */
    public Page<ProblemVO> listProblemVO(ProblemQueryRequest problemQueryRequest) {
        int current = problemQueryRequest.getCurrent();
        int pageSize = problemQueryRequest.getPageSize();
        if (problemQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取查询条件
        LambdaQueryWrapper<Problem> queryWrapper = getQueryWrapper(problemQueryRequest);
        // 查询
        Page<Problem> problemPage = this.page(new Page<>(current, pageSize), queryWrapper);
        Page<ProblemVO> problemVOPage = this.getProblemVOPage(problemPage);
        // 返回
        return problemVOPage;
    }






    /**
     * 生成要返回给前端的VO对象
     * 进行了数据脱敏
     * 题目对应的用户信息和标签信息需要再次查询
     * @param problem
     * @return
     */
    @Override
    public ProblemVO getProblemVOWithoutContent(Problem problem) {
        // 将entity转为vo
        ProblemVO problemVO = ProblemVO.objToVoWithoutContent(problem);
        // 补充vo的信息
        Long userId = problem.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        List<Tag> tags = tagService.listTagsByProblemId(problem.getId());
        problemVO.setTags(tags);
        problemVO.setUserVO(userVO);
        return problemVO;
    }
    /**
     * 生成要返回给前端的VO对象
     * 进行了数据脱敏
     * 题目对应的用户信息和标签信息需要再次查询
     * @param problem
     * @return
     */
    @Override
    public ProblemVO getProblemVOWithContent(Problem problem) {
        // 将entity转为vo
        ProblemVO problemVO = ProblemVO.objToVoWithContent(problem);
        // 补充vo的信息
        Long userId = problem.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        List<Tag> tags = tagService.listTagsByProblemId(problem.getId());
        problemVO.setTags(tags);
        problemVO.setUserVO(userVO);
        return problemVO;
    }
    /**
     * 生成要返回给前端的VO对象
     * 进行了数据脱敏
     */
    @Override
    public List<ProblemVO> getProblemVO(List<Problem> problemList) {
        ArrayList<ProblemVO> problemVOList = new ArrayList<>();
        for (Problem problem : problemList) {
            ProblemVO problemVO = getProblemVOWithoutContent(problem);
            problemVOList.add(problemVO);
        }
        return problemVOList;
    }
    /**
     * 生成分页的VO对象
     * 主要是修改Page对象的records属性
     * records属性就是 List<Problem>
     * 将Page的records属性从List<Problem>修改为List<ProblemVO>
     * @param problemPage
     * @return
     */
    @Override
    public Page<ProblemVO> getProblemVOPage(Page<Problem> problemPage) {
        List<Problem> problemList = problemPage.getRecords();
        Page<ProblemVO> problemVOPage = new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        if (CollUtil.isEmpty(problemList)) {
            return problemVOPage;
        }
        List<ProblemVO> problemVOList = getProblemVO(problemList);
        problemVOPage.setRecords(problemVOList);
        return problemVOPage;
    }
}




