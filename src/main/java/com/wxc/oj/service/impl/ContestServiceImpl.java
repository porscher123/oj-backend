package com.wxc.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxc.oj.exception.BusinessException;
import com.wxc.oj.mapper.ContestMapper;
import com.wxc.oj.mapper.ContestProblemMapper;
import com.wxc.oj.model.entity.Contest;
import com.wxc.oj.model.entity.ContestProblem;
import com.wxc.oj.model.entity.Problem;
import com.wxc.oj.model.vo.ContestVO;
import com.wxc.oj.queueMessage.ContestMessage;
import com.wxc.oj.service.ContestService;
import com.wxc.oj.service.ProblemService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    RedisTemplate redisTemplate;

    @Resource
    ProblemService problemService;

    @Resource
    ContestProblemMapper contestProblemMapper;

    /**
     * contest在状态0时的操作:
     * 1. 保存contest到数据库
     *
     * @return
     */
    public void contestInStatus_0(Contest contest) {
        Date startTime = contest.getStartTime();
        Date currentDate = new Date();
        // 比赛时间不能早于当前时间
        if (startTime.getTime() < currentDate.getTime()) {
            throw new BusinessException(400, "比赛时间不能早于当前时间");
        }
        log.info(contest.toString());
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
     * 将contest转为contestVo
     */
    public ContestVO getContestVO(Contest contest) {
        ContestVO contestVO = new ContestVO();
        copyProperties(contest, contestVO);
        return contestVO;
    }
}




