//package com.wxc.oj.advice;
//
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//@Slf4j
//public class CodeSandboxAdvice {
//
////    /**
////     * 在日志中打印判题的信息
////     * @param joinPoint
////     */
////    @Before("execution(* com.wxc.oj.judge.codesandbox.CodeSandbox.executeCode(..))")
////    public void before(JoinPoint joinPoint) {
////        log.info("执行代码的请求类: ");
////        Object[] args = joinPoint.getArgs();
////        for (Object arg : args) {
////            log.info(arg.toString());
////        }
////        log.info("开启执行代码");
////    }
////
////    /**
////     * 在日志中打印代码执行结果
////     * @param result
////     */
////    @AfterReturning(value = "execution(* com.wxc.oj.judge.codesandbox.CodeSandbox.executeCode(..))", returning = "result")
////    public void after(Object result) {
////        log.info("代码执行结果: ");
////        log.info(result.toString());
////        log.info("执行结束");
////    }
//}
