package com.wxc.oj;

import cn.hutool.json.JSONUtil;
import com.wxc.oj.model.entity.Contest;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

@SpringBootTest
class OjApplicationTests {


    @Autowired
    RedisTemplate<String, Object> redisTemplate;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void test() {
//        Contest contest = new Contest();
//        contest.setCid(1);
//        contest.setTitle("dasdasd");
//        contest.setStart(new Date());
//        contest.setLength(50000000);
//        contest.setType(2);
//        contest.setIspublic(1);
//        contest.setDescription("dsadasdasdasdsadas");
//        contest.setDone(0);
//        String jsonStr = JSONUtil.toJsonStr(contest);
//        redisTemplate.opsForValue().set("contest:3", jsonStr);
    }

    @Test
    public void testRabbit() {
        rabbitTemplate.convertAndSend("delayExchange", "delayKey", "message", m -> {
            m.getMessageProperties().setDelay(5_000);
            return m;
        });
    }

}
