package com.wxc.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.constant.CommonConstant;
import com.wxc.oj.enums.SubmissionLanguageEnum;
import com.wxc.oj.enums.SubmissionStatusEnum;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.dto.submission.SubmissionQueryDTO;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.entity.Submission;
import com.wxc.oj.model.entity.User;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.model.vo.SubmissionVO;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.SubmissionService;
import com.wxc.oj.mapper.SubmissionMapper;
import com.wxc.oj.service.UserService;
import com.wxc.oj.utils.SqlUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 王新超
* @description 针对表【submission】的数据库操作Service实现
* @createDate 2024-02-28 10:33:17
*/
@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {



    @Resource
    private ProblemService problemService;


    @Resource
    private UserService userService;
    /**
     * 提交代码
     * @param submissionAddRequest
     * @param loginUser
     * @return 插入的submission的id
     */
    @Override
    public Submission submitCode(SubmissionAddRequest submissionAddRequest, User loginUser) {
        if (submissionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查编程语言是否存在
        String language = submissionAddRequest.getLanguage();
        List<String> submissionLanguages = SubmissionLanguageEnum.getValues();
        if (!submissionLanguages.contains(language)) {
            throw new BusinessException(ErrorCode.LANGUAGE_NOT_SUPPORTED);
        }
        // 判断提交的题目是否存在
        Long problemId = submissionAddRequest.getProblemId();
        Problem problem = problemService.getById(problemId);
        if (problem == null) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_EXIST);
        }
        Submission submission = new Submission();
        // 创建并初始化待插入数据库的submission
        submission.setProblemId(problemId);
        submission.setUserId(loginUser.getId());
        submission.setSourceCode(submissionAddRequest.getSourceCode());
        submission.setLanguage(submissionAddRequest.getLanguage());
        // 初始化判题状态为 waiting
        submission.setStatus(SubmissionStatusEnum.WAITING.getValue());
        submission.setJudgeInfo("{}");
        // 保存到数据库
        boolean save = this.save(submission);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Submission submission1 = this.getById(submission.getId());
        // 返回submission的id
        return submission1;
    }


    /**
     * 根据Submission的DTO获取LambdaQueryWrapper
     * @return
     */
    @Override
    public LambdaQueryWrapper<Submission> getQueryWrapper(SubmissionQueryDTO submissionQueryDTO) {
        var queryWrapper = new QueryWrapper<Submission>();
        if (submissionQueryDTO == null) {
            return queryWrapper.lambda();
        }
        Long problemId = submissionQueryDTO.getProblemId();
        Long userId = submissionQueryDTO.getUserId();
        String language = submissionQueryDTO.getLanguage();
        String sortField = submissionQueryDTO.getSortField();
        String sortOrder = submissionQueryDTO.getSortOrder();
        if (sortOrder == null) {
            sortOrder = CommonConstant.SORT_ORDER_ASC;
        }
        String judgeResult = submissionQueryDTO.getJudgeResult();

        queryWrapper.eq(ObjectUtils.isNotEmpty(problemId), "problemId", problemId)
                    .eq(ObjectUtils.isNotEmpty(userId), "userId", userId)
                    .eq(StringUtils.isNotBlank(language), "language", language)
                    .like(StringUtils.isNotBlank(judgeResult), "judgeInfo", judgeResult);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper.lambda();
    }
    /**
     *
     */
    @Override
    public SubmissionVO getSubmissionVO(Submission submission) {
        // 将entity转为vo
        SubmissionVO submissionVO = SubmissionVO.objToVo(submission);
        // 先不进行实际脱敏
        return submissionVO;
    }

    /**
     * 生成分页的VO对象
     * @return
     */
    @Override
    public Page<SubmissionVO> getSubmissionVOPage(Page<Submission> submissionPage) {
        // 获取当前页面的所有submission
        List<Submission> submissionList = submissionPage.getRecords();
        // 创建一个空的页面 存储 submissionVO
        Page<SubmissionVO> submissionVOPage = new Page<>(submissionPage.getCurrent(),
                submissionPage.getSize(), submissionPage.getTotal());
        // 不进行用户关联
        List<SubmissionVO> submissionVOList = submissionList.stream().map(submission -> {
            return getSubmissionVO(submission);
        }).collect(Collectors.toList());
//        if (CollUtil.isEmpty(submissionList)) {
//            return submissionVOPage;
//        }
//        // 1. 根据当前页面所有的submission查询所有的user id
//        Set<Long> userIdSet = submissionList.stream().map(Submission::getUserId).collect(Collectors.toSet());
//        // listByIds 根据多个id查询用户
//        // 形成 userId -> User 的Map
//        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
//                .collect(Collectors.groupingBy(User::getId));
//        // 2. 已登录，获取用户点赞、收藏状态
//        // 填充信息
//        List<SubmissionVO> submissionVOList = submissionList.stream().map(submission -> {
//            SubmissionVO submissionVO = SubmissionVO.objToVo(submission);
//            Long userId = submission.getUserId();
//            User user = null;
//            if (userIdUserListMap.containsKey(userId)) {
//                user = userIdUserListMap.get(userId).get(0);
//            }
//            submissionVO.setUserVO(userService.getUserVO(user));
//            return submissionVO;
//        }).collect(Collectors.toList());
        submissionVOPage.setRecords(submissionVOList);
        return submissionVOPage;
    }
}




