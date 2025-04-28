package com.wxc.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.constant.CommonConstant;
import com.wxc.oj.constant.Level;
import com.wxc.oj.mapper.ProblemMapper;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Tag;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.service.TagService;
import com.wxc.oj.service.UserService;
import com.wxc.oj.utils.SqlUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
     * @param problemQueryRequest
     * @return
     */
    @Override
    public LambdaQueryWrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest) {
        var queryWrapper = new QueryWrapper<Problem>();
        if (problemQueryRequest == null) {
            return queryWrapper.lambda();
        }
        Long id = problemQueryRequest.getId();
        String title = problemQueryRequest.getTitle();
        List<String> tags = problemQueryRequest.getTags();
        String level = problemQueryRequest.getLevel();
        String sortField = problemQueryRequest.getSortField();
        String sortOrder = problemQueryRequest.getSortOrder();
        if (sortOrder == null) {
            sortOrder = CommonConstant.SORT_ORDER_ASC;
        }

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title)
                .eq(StringUtils.isNotBlank(level) && checkLevel(level),"level", level);
        if (tags != null) {
            for (String tag : tags) {
                queryWrapper.like(StringUtils.isNotBlank(tag), "tags", tag);
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id),"id", id);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper.lambda();
    }

    /**
     * 生成要返回给前端的VO对象
     * 进行了数据脱敏
     * 题目对应的用户信息和标签信息需要再次查询
     * @param problem
     * @return
     */
    @Override
    public ProblemVO getProblemVO(Problem problem) {
        // 将entity转为vo
        ProblemVO problemVO = ProblemVO.objToVo(problem);
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
            ProblemVO problemVO = getProblemVO(problem);
            problemVOList.add(problemVO);
        }
        return problemVOList;
    }
    /**
     * 生成分页的VO对象
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
        // 1. 关联查询用户信息
//        Set<Long> userIdSet = problemList.stream().map(Problem::getUserId).collect(Collectors.toSet());
//        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
//                .collect(Collectors.groupingBy(User::getId));
//        // 2. 已登录，获取用户点赞、收藏状态
//        // 填充信息
//        List<ProblemVO> problemVOList = problemList.stream().map(problem -> {
//            ProblemVO problemVO = ProblemVO.objToVo(problem);
//            Long userId = problem.getUserId();
//            User user = null;
//            if (userIdUserListMap.containsKey(userId)) {
//                user = userIdUserListMap.get(userId).get(0);
//            }
//            problemVO.setUserVO(userService.getUserVO(user));
//
////            problemVO.setTags();
//            return problemVO;
//        }).collect(Collectors.toList());
        problemVOPage.setRecords(problemVOList);
        return problemVOPage;
    }
}




