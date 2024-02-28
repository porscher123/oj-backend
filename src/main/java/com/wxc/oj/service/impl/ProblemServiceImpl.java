package com.wxc.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.exception.ThrowUtils;
import com.wxc.oj.mapper.ProblemMapper;
import com.wxc.oj.model.dto.problem.ProblemQueryRequest;
import com.wxc.oj.model.pojo.Problem;
import com.wxc.oj.model.pojo.User;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 王新超
* @description 针对表【problem】的数据库操作Service实现
* @createDate 2024-02-28 14:24:47
*/
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {@Autowired

    @Resource
    private UserService userService;


    /**
     * 校验题目是否合法
     * 题目的有些数据创建时可以省略,
     * 等待后期修改
     * @param problem
     * @param add
     */
    @Override
    public void validProblem(Problem problem, boolean add) {
        problem
    }

    /**
     * 获取查询包装类
     *
     * @param problemQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        if (problemQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = problemQueryRequest.getSearchText();
        String sortField = problemQueryRequest.getSortField();
        String sortOrder = problemQueryRequest.getSortOrder();
        Long id = problemQueryRequest.getId();
        String title = problemQueryRequest.getTitle();
        String content = problemQueryRequest.getContent();
        List<String> tagList = problemQueryRequest.getTags();
        Long userId = problemQueryRequest.getUserId();
        Long notId = problemQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Problem> searchFromEs(ProblemQueryRequest problemQueryRequest) {
        Long id = problemQueryRequest.getId();
        Long notId = problemQueryRequest.getNotId();
        String searchText = problemQueryRequest.getSearchText();
        String title = problemQueryRequest.getTitle();
        String content = problemQueryRequest.getContent();
        List<String> tagList = problemQueryRequest.getTags();
        List<String> orTagList = problemQueryRequest.getOrTags();
        Long userId = problemQueryRequest.getUserId();
        // es 起始页为 0
        long current = problemQueryRequest.getCurrent() - 1;
        long pageSize = problemQueryRequest.getPageSize();
        String sortField = problemQueryRequest.getSortField();
        String sortOrder = problemQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 包含任何一个标签即可
        if (CollUtil.isNotEmpty(orTagList)) {
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<ProblemEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, ProblemEsDTO.class);
        Page<Problem> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Problem> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<ProblemEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> problemIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Problem> problemList = baseMapper.selectBatchIds(problemIdList);
            if (problemList != null) {
                Map<Long, List<Problem>> idProblemMap = problemList.stream().collect(Collectors.groupingBy(Problem::getId));
                problemIdList.forEach(problemId -> {
                    if (idProblemMap.containsKey(problemId)) {
                        resourceList.add(idProblemMap.get(problemId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(problemId), ProblemEsDTO.class);
                        log.info("delete problem {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public ProblemVO getProblemVO(Problem problem, HttpServletRequest request) {
        ProblemVO problemVO = ProblemVO.objToVo(problem);
        long problemId = problem.getId();
        // 1. 关联查询用户信息
        Long userId = problem.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        problemVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<ProblemThumb> problemThumbQueryWrapper = new QueryWrapper<>();
            problemThumbQueryWrapper.in("problemId", problemId);
            problemThumbQueryWrapper.eq("userId", loginUser.getId());
            ProblemThumb problemThumb = problemThumbMapper.selectOne(problemThumbQueryWrapper);
            problemVO.setHasThumb(problemThumb != null);
            // 获取收藏
            QueryWrapper<ProblemFavour> problemFavourQueryWrapper = new QueryWrapper<>();
            problemFavourQueryWrapper.in("problemId", problemId);
            problemFavourQueryWrapper.eq("userId", loginUser.getId());
            ProblemFavour problemFavour = problemFavourMapper.selectOne(problemFavourQueryWrapper);
            problemVO.setHasFavour(problemFavour != null);
        }
        return problemVO;
    }

    @Override
    public Page<ProblemVO> getProblemVOPage(Page<Problem> problemPage, HttpServletRequest request) {
        List<Problem> problemList = problemPage.getRecords();
        Page<ProblemVO> problemVOPage = new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        if (CollUtil.isEmpty(problemList)) {
            return problemVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = problemList.stream().map(Problem::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> problemIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> problemIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> problemIdSet = problemList.stream().map(Problem::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<ProblemThumb> problemThumbQueryWrapper = new QueryWrapper<>();
            problemThumbQueryWrapper.in("problemId", problemIdSet);
            problemThumbQueryWrapper.eq("userId", loginUser.getId());
            List<ProblemThumb> problemProblemThumbList = problemThumbMapper.selectList(problemThumbQueryWrapper);
            problemProblemThumbList.forEach(problemProblemThumb -> problemIdHasThumbMap.put(problemProblemThumb.getProblemId(), true));
            // 获取收藏
            QueryWrapper<ProblemFavour> problemFavourQueryWrapper = new QueryWrapper<>();
            problemFavourQueryWrapper.in("problemId", problemIdSet);
            problemFavourQueryWrapper.eq("userId", loginUser.getId());
            List<ProblemFavour> problemFavourList = problemFavourMapper.selectList(problemFavourQueryWrapper);
            problemFavourList.forEach(problemFavour -> problemIdHasFavourMap.put(problemFavour.getProblemId(), true));
        }
        // 填充信息
        List<ProblemVO> problemVOList = problemList.stream().map(problem -> {
            ProblemVO problemVO = ProblemVO.objToVo(problem);
            Long userId = problem.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            problemVO.setUser(userService.getUserVO(user));
            problemVO.setHasThumb(problemIdHasThumbMap.getOrDefault(problem.getId(), false));
            problemVO.setHasFavour(problemIdHasFavourMap.getOrDefault(problem.getId(), false));
            return problemVO;
        }).collect(Collectors.toList());
        problemVOPage.setRecords(problemVOList);
        return problemVOPage;
    }
}




