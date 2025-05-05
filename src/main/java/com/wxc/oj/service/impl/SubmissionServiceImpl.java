package com.wxc.oj.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.constant.CommonConstant;
import com.wxc.oj.enums.submission.SubmissionLanguageEnum;
import com.wxc.oj.enums.submission.SubmissionStatus;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.mapper.SubmissionMapper;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.queueMessage.SubmissionMessage;
import com.wxc.oj.model.dto.submission.SubmissionAddRequest;
import com.wxc.oj.model.dto.submission.SubmissionQueryDTO;
import com.wxc.oj.model.po.Problem;
import com.wxc.oj.model.po.Submission;
import com.wxc.oj.model.po.User;
import com.wxc.oj.service.SubmissionService;
import com.wxc.oj.model.vo.ProblemVO;
import com.wxc.oj.model.vo.SubmissionVO;
import com.wxc.oj.model.vo.UserVO;
import com.wxc.oj.service.ProblemService;
import com.wxc.oj.service.UserService;
import com.wxc.oj.utils.SqlUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 王新超
* @description 针对表【submission】的数据库操作Service实现
* @createDate 2024-02-28 10:33:17
*/
@Service
@Slf4j(topic = "SubmissionServiceImpl")
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {


    @Resource
    private RabbitTemplate rabbitTemplate;

    public static final String ROUTING_KEY = "submission";
    /**
     * 默认的直连交换机
     */
    public static final String EXCHANGE = "amq.direct";

    @Resource
    private ProblemService problemService;
    @Resource
    private UserService userService;

    /**
     * 提交代码
     * 并生成submission到rocketmq
     * 因为用户提交代码后, 后端异步地调用判题服务, 所以此时给用户返回地判题结果为空
     * @param submissionAddRequest
     * @param loginUser
     * @return 插入的submission的id
     */
    @Override
    public SubmissionVO submitCode(SubmissionAddRequest submissionAddRequest, User loginUser) {
        if (submissionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(submissionAddRequest.getSourceCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查编程语言是否存在
        String language = submissionAddRequest.getLanguage();
        log.info("language = " + language);
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

        // 创建并初始化待插入数据库的submission和submissionResult
        Submission submission = new Submission();
        SubmissionResult submissionResult = new SubmissionResult();

        submission.setProblemId(problemId);
        submission.setUserId(loginUser.getId());
        submission.setSourceCode(submissionAddRequest.getSourceCode());
        submission.setLanguage(submissionAddRequest.getLanguage());

        // 初始化判题状态为 NOT_SUBMITTED
//        submission.setStatus(SubmissionStatus.SUBMITTED.getStatus());

        submissionResult.setStatus(SubmissionStatus.SUBMITTED.getStatus());
        submissionResult.setStatusDescription(SubmissionStatus.SUBMITTED.getDescription());
        submissionResult.setScore(0);

        submission.setSubmissionResult(JSONUtil.toJsonStr(submissionResult));

        // 初始submission保存到数据库
        boolean save = this.save(submission);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }

        // 获取插入数据库后的submissionID
        Submission submission1 = this.getById(submission.getId());
        // submission发送到消息队列后，submission的状态为 PENDING
        // ❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗❗
        Long id = submission1.getId();
        if (id != null) {
            SubmissionMessage submissionMessage = new SubmissionMessage();
            submissionMessage.setId(id);
            log.info("发送submissionId: " + id);
            //
            // 使用convertAndSend方法一步到位，参数基本和之前是一样的
            //最后一个消息本体可以是Object类型，真是大大的方便
            // 发送消息到直连交换机, 指定路由键
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, submissionMessage);
//            submission1.setStatus(SubmissionStatus.PENDING.getStatus());
            submissionResult.setStatus(SubmissionStatus.PENDING.getStatus());
            submissionResult.setStatusDescription(SubmissionStatus.PENDING.getDescription());
        }
        this.updateById(submission1);
        // 异步化了, 所以返回的还是刚开始的初始的submission
        Submission submission2 = this.getById(submission.getId());
        SubmissionVO submissionVO = this.submissionToVO(submission2);
        return submissionVO;
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

//        String sortField = "createTime";
//        String sortOrder = CommonConstant.SORT_ORDER_DESC;

        String judgeResult = submissionQueryDTO.getJudgeResult();

        queryWrapper.eq(ObjectUtils.isNotEmpty(problemId), "problemId", problemId)
                    .eq(ObjectUtils.isNotEmpty(userId), "userId", userId)
                    .eq(StringUtils.isNotBlank(language), "language", language)
                    .like(StringUtils.isNotBlank(judgeResult), "judgeInfo", judgeResult);
        return queryWrapper.lambda();
    }
    /**
     * 从数据库中得submission
     * 进行数据脱敏和数据库id扩展
     */
    @Override
    public SubmissionVO submissionToVO(Submission submission) {
        // pojo的基础数据映射
        SubmissionVO submissionVO = SubmissionVO.objToVo(submission);
        // 解析pojo对象的JSON字符串为对象
        String submissionResultStr = submission.getSubmissionResult();
        SubmissionResult submissionResult = JSONUtil.toBean(submissionResultStr, SubmissionResult.class);
        submissionVO.setSubmissionResult(submissionResult);

        // 设置submission的题目具体信息
        Problem byId = problemService.getById(submissionVO.getProblemId());
        ProblemVO problemVO = problemService.getProblemVOWithoutContent(byId);
        submissionVO.setProblemId(problemVO.getId());
        submissionVO.setProblemTitle(problemVO.getTitle());

        // 设置submission的提交User信息
        User user = userService.getById(submissionVO.getUserId());
        UserVO userVO = UserVO.objToVo(user);
        submissionVO.setUserId(userVO.getId());
        submissionVO.setUserAccount(userVO.getUserAccount());

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
            return submissionToVO(submission);
        }).collect(Collectors.toList());
        submissionVOPage.setRecords(submissionVOList);
        return submissionVOPage;
    }
}




