package com.wxc.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.common.ErrorCode;
import com.wxc.oj.common.PageRequest;
import com.wxc.oj.enums.JudgeResultEnum;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.mapper.ContestMapper;
import com.wxc.oj.mapper.ContestProblemMapper;
import com.wxc.oj.mapper.ContestRegistrationMapper;
import com.wxc.oj.model.po.*;
import com.wxc.oj.model.submission.SubmissionResult;
import com.wxc.oj.model.vo.*;
import com.wxc.oj.model.queueMessage.ContestMessage;
import com.wxc.oj.model.vo.rank.RankListVO;
import com.wxc.oj.model.vo.rank.RankProblemVO;
import com.wxc.oj.model.vo.rank.RankItem;
import com.wxc.oj.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * @author 王新超
 * @description 针对表【contest】的数据库操作Service实现
 * @createDate 2025-03-22 22:31:00
 */
@Service
@Slf4j(topic = "ContestServiceImpl💕💕💕💕")
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest>
        implements ContestService{

    @Resource
    RabbitTemplate rabbitTemplate;


    @Resource
    ContestRegistrationMapper contestRegistrationMapper;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    ProblemService problemService;

    @Resource
    ContestProblemMapper contestProblemMapper;


    @Resource
    ContestProblemService contestProblemService;


    @Resource
    ContestRegistrationService contestRegistrationService;

    @Resource
    UserService userService;


    @Resource
    ContestSubmissionService contestSubmissionService;

    /**
     * contest在状态0时的操作:
     * 1. 保存contest到数据库
     *
     * @return
     */
    @Override
    public void contestInStatus_0(HttpServletRequest request, Contest contest) {
        Date startTime = contest.getStartTime();
        Date currentDate = new Date();
        // 比赛时间不能早于当前时间
        if (startTime.getTime() < currentDate.getTime()) {
            throw new BusinessException(400, "比赛时间不能早于当前时间");
        }
        log.info(contest.toString());
        contest.setHostId(userService.getLoginUser(request).getId());
        // 保存contest到数据库
        boolean save = this.save(contest);
        if (!save) {
            throw new BusinessException(400, "比赛发布失败");
        }
        currentDate = new Date();
        long timeDifferenceInMillis = startTime.getTime() - currentDate.getTime();
        log.info("距离"+timeDifferenceInMillis+"ms比赛就业开始");
        // 创建消息, 保存contest ID
        ContestMessage contestMessage = new ContestMessage();
        contestMessage.setId(contest.getId());
        // 发送消息到延迟交换机, 转发到timePublish队列
        rabbitTemplate.convertAndSend("delayExchange", "timePublish", contestMessage,message -> {
            MessageProperties properties = message.getMessageProperties();
            properties.setDelay(Integer.valueOf((int)timeDifferenceInMillis));
            return message;
        });

        log.info("比赛已经发布😊😊😊😊😊");
    }


    /**
     * contest在状态1的操作
     * 从timePublish队列收到消息后, 再次发送一个消息到延迟交换机
     * 延迟duration后转发到timeFinish队列进行结束处理
     * @param
     * @return
     */
    @RabbitListener(queues = "timePublish", messageConverter = "jacksonConverter")
    public void contestInStatus_1(ContestMessage contestMessage) {
        Long id = contestMessage.getId();
        Contest contest = this.getById(id);
        contest.setStatus(Integer.valueOf(1));
        boolean updated = this.updateById(contest);
        if (!updated) {
            throw new RuntimeException("更新失败");
        }
        Integer duration = contest.getDuration();
        ContestMessage contestMessage2 = new ContestMessage();
        contestMessage2.setId(contest.getId());
        // todo: 将当前contest下的所有题目缓存到redis
        LambdaQueryWrapper<ContestProblem> problemQueryWrapper = new LambdaQueryWrapper<>();
        problemQueryWrapper.eq(ContestProblem::getContestId, id);
        List<ContestProblem> contestProblems = contestProblemMapper.selectList(problemQueryWrapper);
        for (ContestProblem contestProblem : contestProblems) {
            log.info("contestProblem:"+contestProblem);
            Long problemId = contestProblem.getProblemId();
            Problem problem = problemService.getById(problemId);
            redisTemplate.opsForValue().set("problem:"+problemId, problem);
        }
        rabbitTemplate.convertAndSend("delayExchange", "timeFinish", contestMessage2, m -> {
            m.getMessageProperties().setDelay(duration);
            return m;
        });
        log.info("比赛正在进行🤷‍♀️🤷‍♀️🤷‍♀️🤷‍♀️🤷‍♀️");
    }

    /**
     * 从timeFinish队列收到消息
     * 修改消息体中指定的id对应的contest的状态为2
     * @param
     * @return
     */
    @RabbitListener(queues = "timeFinish", messageConverter = "jacksonConverter")
    public void contestInStatus_2(ContestMessage contestMessage) {
        Long id = contestMessage.getId();
        Contest contest = this.getById(id);
        contest.setStatus(Integer.valueOf(2));
        boolean save = this.updateById(contest);
        if (!save) {
            throw new RuntimeException("更新失败");
        }
        log.info("比赛结束✔✔✔✔");
    }



    /**
     * ProblemList页面使用
     * 将contest转为contestVO
     * 设置比赛的所有题目
     * 包括比赛报名人数
     * @param contest
     * @return
     */
    @Override
    public ContestVO getContestVOWithoutProblemListByContest(Contest contest) {
        ContestVO contestVO = new ContestVO();
        copyProperties(contest, contestVO);
        Integer playerCount = this.getContestPlayerCount(contest.getId());
        contestVO.setPlayerCount(playerCount);
        log.info("getHostId"+contest.getHostId());
        Long hostId = contest.getHostId();
        User host = userService.getById(hostId);
        if (host == null) {
            throw new BusinessException(400, "获取比赛发布者失败信息失败");
        }
        contestVO.setHostName(host.getUserName());
        contestVO.setHostId(contest.getHostId());
        // 以秒为单位返回比赛持续时间
        contestVO.setDuration(Integer.valueOf(contest.getDuration() / 1000));
        contestVO.setEndTime(new Date(contest.getStartTime().getTime() + contest.getDuration()));
        return contestVO;
    }

    /**
     * ProblemDetail页面使用
     * @param contest
     * @return
     */
    @Override
    public ContestVO getContestVOWithProblemListByContest(Contest contest) {
        ContestVO contestVO = new ContestVO();
        copyProperties(contest, contestVO);
        Long contestId = contest.getId();
        Integer playerCount = this.getContestPlayerCount(contest.getId());
        contestVO.setPlayerCount(playerCount);
        List<ProblemVO> problemVOList = this.getProblemVOListByContestId(contestId);
        contestVO.setProblemVOList(problemVOList);
        // 以秒为单位返回比赛持续时间
        contestVO.setDuration(Integer.valueOf(contest.getDuration() / 1000));
        contestVO.setEndTime(new Date(contest.getStartTime().getTime() + contest.getDuration()));

//        contestVO.setRegistered(K);
//        contestVO.setCanRegistration(this.canRegister(contest.getId(), ));
        return contestVO;
    }
    /**
     * 将contestList转为contestVOList
     * @param contestList
     * @return
     */
    private List<ContestVO> getContestVOListByContestList(List<Contest> contestList) {
        ArrayList<ContestVO> contestVOList = new ArrayList<>();
        for (Contest contest : contestList) {
            ContestVO contestVO = getContestVOWithoutProblemListByContest(contest);
            contestVOList.add(contestVO);
        }
        return contestVOList;
    }
    private Page<ContestVO> getContestVOPageByContestPage(Page<Contest> contestPage) {
        List<Contest> contestList = contestPage.getRecords();
        Page<ContestVO> contestVOPage = new Page<>(contestPage.getCurrent(),
                contestPage.getSize(),  contestPage.getTotal());
        if (CollUtil.isEmpty(contestList)) {
            return contestVOPage;
        }
        List<ContestVO> problemVOList = getContestVOListByContestList(contestList);
        contestVOPage.setRecords(problemVOList);
        return contestVOPage;
    }

    @Override
    public Page<ContestVO> getContestVOPage(PageRequest pageRequest) {
        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();
        Page<Contest> contestPage = this.page(new Page<>(current, pageSize),
                new QueryWrapper<Contest>().orderByDesc("start_time"));
        Page<ContestVO> contestVOPageByContestPage = getContestVOPageByContestPage(contestPage);
        return contestVOPageByContestPage;
    }


    /**
     * 获取某个比赛下的所有题目
     * 为了封装ContestVO准备
     * 查询contestProblem表，找到contestId对应的所有problemId
     * 再在problem表里找到对应的problemVO
     * @param contestId
     * @return
     */
    @Override
    public List<ProblemVO> getProblemVOListByContestId(Long contestId) {
        LambdaQueryWrapper<ContestProblem> problemQueryWrapper = new LambdaQueryWrapper<>();
        problemQueryWrapper.eq(ContestProblem::getContestId, contestId)
                .select(ContestProblem::getProblemId);
        List<Long> problemIds =  contestProblemMapper.selectObjs(problemQueryWrapper);
        // 有的比赛可能没有题目
        if (CollUtil.isEmpty(problemIds)) {
            return new ArrayList<>();
        }
        List<Problem> problems = problemService.listByIds(problemIds);
        List<ProblemVO> problemVOListByProblemList = problemService.getProblemVOListByProblemList(problems);
        return problemVOListByProblemList;
    }


    /**
     * 查询单个比赛信息，包含题目VO列表对象
     * @param contestId
     * @return
     */
    @Override
    public ContestVO getContestVOByContestId(Long contestId) {
        Contest contest = this.getById(contestId);
        ContestVO contestVO = getContestVOWithProblemListByContest(contest);
        return contestVO;
    }



    /**
     * 查询某个Content有多少用户报名了
     * 使用Redis 缓存优化
     * @param contestId
     * @return
     */
    private Integer getContestPlayerCount(Long contestId) {
        String key = "contest:" + contestId + ":playerCount";
        Integer playerCount = (Integer) redisTemplate.opsForValue().get(key);
        if (playerCount == null) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("contest_id", contestId);
            Long l = contestRegistrationMapper.selectCount(queryWrapper);
            log.info("contest:" + contestId + ":playerCount" + playerCount);
            playerCount = l.intValue();
            redisTemplate.opsForValue().set(key, playerCount);
        }
        return playerCount;
    }


    /**
     * 用户报名比赛功能
     * todo:
     *  校验比赛状态,只有当比赛处于status = 0的时候才能报名
     *  防止用户重复报名
     * @param contestId
     * @return
     */
    @Override
    public boolean register(Long userId, Long contestId) {
        // 校验比赛的状态
        Contest contest = this.getById(contestId);
        if (contest.getStatus() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛已开始");
        }
        // 判断是否重复报名：根据（userId, contestId）查询，查询到则重复报名。
        LambdaQueryWrapper<ContestRegistration> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ContestRegistration::getContestId, contestId)
                .eq(ContestRegistration::getUserId, userId);
        ContestRegistration one = contestRegistrationService.getOne(lambdaQueryWrapper);
        if (one != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重复报名");
        }
        // 保存报名信息到数据库
        ContestRegistration contestRegistration = new ContestRegistration();
        contestRegistration.setContestId(contestId);
        contestRegistration.setUserId(userId);
        boolean save = contestRegistrationService.save(contestRegistration);
        // 比赛的报名人数改变, 删除缓存,让下一次查询使用数据库
        if (save) {
            redisTemplate.delete("contest:" + contestId + ":playerCount");
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重复报名");
        }
        return true;
    }

    /**
     * 取消报名
     * todo:
     *  1. 只有当比赛status = 0时才可以取消报名
     *
     * @param request
     * @param contestId
     * @return
     */
    @Override
    public boolean cancelRegistration(Long userId, Long contestId) {
        LambdaQueryWrapper<ContestRegistration> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ContestRegistration::getContestId, contestId)
                .eq(ContestRegistration::getUserId, userId);
        ContestRegistration contestRegistration =
                contestRegistrationService.getOne(lambdaQueryWrapper);
        if (contestRegistration == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该比赛不存在");
        }
        boolean r = contestRegistrationService.removeById(contestRegistration.getId());
        return r;
    }


    /**
     * 查找用户是否报名了某个比赛
     * 使用Redis缓存优化
     * @param userId
     * @param contestId
     * @return
     */
    @Override
    public boolean findUserInContest(Long userId, Long contestId) {
        String key = "contest:" + contestId + "user:" + userId;
        if (redisTemplate.hasKey(key)) {
            return true;
        }
        LambdaQueryWrapper<ContestRegistration> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ContestRegistration::getContestId, contestId)
                .eq(ContestRegistration::getUserId, userId);
        ContestRegistration contestRegistration =
                contestRegistrationService.getOne(lambdaQueryWrapper);
        if (contestRegistration == null) {
            return false;
        }
        redisTemplate.opsForValue().set("contest:" + contestId + "user:" + userId, 1);
        return true;
    }

    /**
     * 判断用户是否可以报名
     * todo:
     *  1.用户不能重复报名
     *  2.比赛状态必须是0，才能报名
     *  3.比赛本身不允许别人自主报名
     * @param userId
     * @param contestId
     * @return
     */
    @Override
    public boolean canRegister(Long userId, Long contestId) {
        Contest contest = this.getById(contestId);
        if (contest.getStatus() != 0) {
            return false;
        }
        if (contest.getCanRegister() == 0) {
            return false;
        }
        return true;
    }

//    @Override
//    public List<ProblemVO> getProblemVOListByContestId(Long contestId) {
//        List<ProblemVO> problemVOList = new ArrayList<>();
//        QueryWrapper<ContestProblem> queryWrapper
//                = new QueryWrapper<ContestProblem>().eq("contest_id", contestId);
//        List<ContestProblem> contestProblemList = contestProblemService.list();
//        for (ContestProblem contestProblem : contestProblemList) {
//            Long problemId = contestProblem.getProblemId();
//            Problem problem = problemService.getById(problemId);
//            ProblemVO problemVO = problemService.getProblemVOWithoutContent(problem);
//            problemVOList.add(problemVO);
//        }
//        return problemVOList;
//    }


    @Override
    public ContestProblemVO contestProblemToVO(ContestProblem contestProblem) {
        Long problemId = contestProblem.getProblemId();
        Problem problem = problemService.getById(problemId);
        if (problem == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目不存在");
        }
        ProblemVO problemVO = problemService.getProblemVOWithoutContent(problem);
        ContestProblemVO contestProblemVO = new ContestProblemVO();
        BeanUtils.copyProperties(problemVO, contestProblemVO);
        contestProblemVO.setPindex(contestProblem.getPindex());
        contestProblemVO.setFullScore(contestProblem.getFullScore());
        return contestProblemVO;
    }



    @Override
    public List<ContestProblemVO> getContestProblemVOListByContestId(Long contestId, Long userId) {
        List<ContestProblemVO> contestProblemVOList = new ArrayList<>();
        QueryWrapper<ContestProblem> queryWrapper
                = new QueryWrapper<ContestProblem>().eq("contest_id", contestId);
        List<ContestProblem> contestProblemList = contestProblemService.list(queryWrapper);
        // 有的比赛可能没有题目
        if (CollUtil.isEmpty(contestProblemList)) {
            return new ArrayList<>();
        }
        for (ContestProblem contestProblem : contestProblemList) {
            ContestProblemVO contestProblemVO = this.contestProblemToVO(contestProblem);
            Long problemId = contestProblem.getProblemId();
            LambdaQueryWrapper<ContestSubmission> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(ContestSubmission::getContestId, contestId)
                    .eq(ContestSubmission::getUserId, userId)
                    .eq(ContestSubmission::getProblemId, problemId);
            List<ContestSubmission> contestSubmissionList = contestSubmissionService.list(queryWrapper1);
            int maxScore = 0;
            if (CollUtil.isNotEmpty(contestSubmissionList)) {
                for (ContestSubmission contestSubmission : contestSubmissionList) {
                    ContestSubmissionVO contestSubmissionVO = contestSubmissionService.contestSubmissionToVO(contestSubmission);
                    maxScore  = Math.max(maxScore, contestSubmissionVO.getSubmissionResult().getScore());
                }
                contestProblemVO.setGainScore(maxScore * contestProblemVO.getFullScore() / 100);
            }
            contestProblemVOList.add(contestProblemVO);
        }

        return contestProblemVOList;
    }




    @Override
    public ContestProblemVO getContestProblemByIndex(Long contestId, Integer index) {
        QueryWrapper<ContestProblem> queryWrapper
                = new QueryWrapper<ContestProblem>().eq("contest_id", contestId)
                .eq("pindex", index);
        ContestProblem contestProblem = contestProblemService.getOne(queryWrapper);
        if (contestProblem == null) {
            return null;
        }
        Long problemId = contestProblem.getProblemId();
        Problem problem = problemService.getById(problemId);
        if (problem == null) {
            return null;
        }
        ProblemVO problemVO = problemService.getProblemVOWithContent(problem);
        ContestProblemVO contestProblemVO = new ContestProblemVO();
        BeanUtils.copyProperties(problemVO, contestProblemVO);
        contestProblemVO.setPindex(contestProblem.getPindex());
        contestProblemVO.setFullScore(contestProblem.getFullScore());
        return contestProblemVO;
    }


    public List<UserVO>  getUserVOListByContestId(Long contestId) {
        List<UserVO> userVOList = new ArrayList<>();
        LambdaQueryWrapper<ContestRegistration> queryWrapper
                = new LambdaQueryWrapper<ContestRegistration>()
                .eq(ContestRegistration::getContestId, contestId);
        List<ContestRegistration> contestRegistrationList = contestRegistrationService.list(queryWrapper);
        if (CollUtil.isEmpty(contestRegistrationList)) {
            return new ArrayList<>();
        }
        for (ContestRegistration contestRegistration : contestRegistrationList) {
            Long userId = contestRegistration.getUserId();
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            userVOList.add(userVO);
        }
        return userVOList;
      }


    public List<ContestProblem> getContestProblemListByContestId(Long contestId) {
        QueryWrapper<ContestProblem> queryWrapper
                = new QueryWrapper<ContestProblem>().eq("contest_id", contestId);
        List<ContestProblem> contestProblemList = contestProblemService.list(queryWrapper);
        return contestProblemList;
    }

    @Override
    public RankListVO getRankList(Long contestId) {
        List<ContestProblem> contestProblems = this.getContestProblemListByContestId(contestId);
        Map<Long, ContestProblem> problemMap = contestProblems.stream()
                .collect(Collectors.toMap(ContestProblem::getProblemId, problem -> problem));



        Contest contest = this.getById(contestId);
        List<UserVO> userVOList = this.getUserVOListByContestId(contestId);
        if (CollUtil.isEmpty(userVOList)) {
            return new RankListVO();
        }
        // 每个报名用户对应一条排名记录
        // userID -> UserRankVO
        Map<Long, RankItem> rankMap = new HashMap<>();
        userVOList.forEach(user -> {
            RankItem rankVO = new RankItem();
            rankVO.setUserVO(user);
            rankVO.setTotalScore(Integer.valueOf(0)); // 所有题目总得分
            rankVO.setUsedTime(Long.valueOf(0L)); // 所有题目总用时
            rankVO.setSubmitted(Boolean.valueOf(false)); // 是否提交过
            rankVO.setProblemDetails(new HashMap<>()); // 每个题目的具体得分和用时
            rankMap.put(user.getId(), rankVO);
        });
        // 不用返回一个比赛下的所有提交
        // 只保留一个用户对一个题目最高得分的提交
        List<ContestSubmissionVO> submissions
                = contestSubmissionService.listSubmissionsByContestId(contestId);

        // 遍历所有提交记录，更新排名记录
        for (ContestSubmissionVO submission : submissions) {
            Long userId = submission.getUserId();
            RankItem rankItem = rankMap.get(userId);
            // 获取提交信息
            ContestProblem contestProblem = problemMap.get(submission.getProblemId());


            // 更新提交对应的题目的排名信息
            RankProblemVO rankProblemVO = new RankProblemVO();
            Integer score = submission.getSubmissionResult().getScore();
            Integer fullScore = contestProblem.getFullScore();
            rankProblemVO.setGainScore(Integer.valueOf(score * fullScore / 100));
            rankProblemVO.setFullScore(contestProblem.getFullScore());

            rankItem.getProblemDetails().put(contestProblem.getId(), rankProblemVO);
            rankItem.setTotalScore(rankItem.getTotalScore() + rankProblemVO.getGainScore());
            if (submission.getSubmissionResult().getScore() > 0) {
                rankItem.setUsedTime(rankItem.getUsedTime() + submission.getSubmissionResult().getTotalTime());
            }
            rankItem.setSubmitted(true);
        }
        List<RankItem> rankList = new ArrayList<>(rankMap.values());
        RankListVO result = new RankListVO();
        result.setData(rankList);
        Map<Long, Integer> problemInfo = new HashMap<>();
        contestProblems.forEach(problem -> problemInfo.put(problem.getId(), problem.getFullScore()));
        result.setProblem(problemInfo);
        return result;

    }
}




