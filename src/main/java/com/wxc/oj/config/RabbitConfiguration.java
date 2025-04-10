package com.wxc.oj.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfiguration {
    @Bean("directExchange")  //定义交换机Bean，可以很多个
    public Exchange exchange(){
        return ExchangeBuilder.directExchange("amq.direct").build();
    }

    @Bean("submission")     //定义消息队列
    public Queue queue(){
        return QueueBuilder
          				.durable("submission")   //非持久化类型
          				.build();
    }

    @Bean("binding")
    public Binding binding(@Qualifier("directExchange") Exchange exchange,
                           @Qualifier("submission") Queue queue){
      	//将我们刚刚定义的交换机和队列进行绑定
        return BindingBuilder
                .bind(queue)   //绑定队列
                .to(exchange)  //到交换机
                .with("submission")   //使用自定义的routingKey
                .noargs();
    }

    /**
     * 延迟交换机
     * @return
     */
    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("delayExchange", "x-delayed-message", true, false, args);
    }

    /**
     * 延迟队列
     */
    @Bean("timePublish")
    public Queue delayQueue1() {
        return QueueBuilder.durable("timePublish").build();
    }

    @Bean("timeFinish")
    public Queue delayQueue2() {
        return QueueBuilder.durable("timeFinish").build();
    }


    /**
     * 绑定交换机和队列
     */
    @Bean
    public Binding delayBinding1() {
        return BindingBuilder
                .bind(delayQueue1()).to(delayExchange()).with("timePublish").noargs();
    }
    @Bean
    public Binding delayBinding2() {
        return BindingBuilder
                .bind(delayQueue2()).to(delayExchange()).with("timeFinish").noargs();
    }
    /**
     * 创建一个用于JSON转换的Bean
     * @return
     */
    @Bean("jacksonConverter")
    public Jackson2JsonMessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }
}