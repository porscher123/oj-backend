package com.wxc.oj.controller;

import com.wxc.oj.sandbox.dto.SandBoxResponse;
import com.wxc.oj.service.JudgeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("judge")
public class JudgeController {


    @Resource
    private JudgeService judgeService;

//    @RequestMapping("")
//    public SandBoxResponse judge() throws IOException {
//        SandBoxResponse response = judgeService.compileCode();
//        return response;
//    }
}
