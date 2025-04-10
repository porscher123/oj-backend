package com.wxc.oj.mysandbox.controller;

import com.wxc.oj.mysandbox.dto.CodeExecutionRequest;
import com.wxc.oj.mysandbox.dto.CodeExecutionResponse;
import com.wxc.oj.mysandbox.service.CodeSandboxService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeExecutionController {
    @Resource
    private CodeSandboxService codeSandboxService;

    @PostMapping("/execute")
    public CodeExecutionResponse executeCode(@RequestBody CodeExecutionRequest request) {
        return codeSandboxService.executeCommand(request);
    }
}    