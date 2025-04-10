package com.wxc.oj.mysandbox.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.wxc.oj.mysandbox.dto.CodeExecutionRequest;
import com.wxc.oj.mysandbox.dto.CodeExecutionResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
public class CodeSandboxService {

    @Resource
    private  DockerClient dockerClient;


    public String executeCommand(CodeExecutionRequest request) {
        String runCommand = request.getRunCommand();
        try {
            // 创建临时文件存储代码
            Path sourceFilePath = Files.createTempFile("source", ".cpp");
            String sourceCode = "#include <iostream>\nint main() { std::cout << \"Hello, C++!\"; return 0; }";
            Files.write(sourceFilePath, sourceCode.getBytes());

            // 创建容器配置，挂载外部环境目录
            HostConfig hostConfig = HostConfig.newHostConfig();
            // 挂载代码文件
            String hostCodePath = sourceFilePath.toString();
            String containerCodePath = "/app/source.cpp";
            hostConfig.withBinds(new Bind(hostCodePath, new Volume(containerCodePath)));

            // 挂载外部编译器目录（示例，根据实际情况修改）
            String compilerHostPath = "/usr/bin/g++";
            String compilerContainerPath = "/usr/bin/g++";
            hostConfig.withBinds(new Bind(compilerHostPath, new Volume(compilerContainerPath)));

            // 挂载外部库目录（示例，根据实际情况修改）
            String libHostPath = "/usr/lib";
            String libContainerPath = "/usr/lib";
            hostConfig.withBinds(new Bind(libHostPath, new Volume(libContainerPath)));

            // 创建容器
            CreateContainerResponse container = dockerClient.createContainerCmd("cpp-environment-with-mount:latest")
                    .withCmd("/bin/sh", "-c", runCommand)
                    .withHostConfig(hostConfig)
                    .exec();

            // 启动容器
            dockerClient.startContainerCmd(container.getId()).exec();

            // 等待容器执行完成
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(container.getId()).exec(waitCallback).awaitCompletion();

            // 获取容器输出
            LogContainerResultCallback logCallback = new LogContainerResultCallback();
            dockerClient.logContainerCmd(container.getId())
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(logCallback);
            String output = logCallback.toString();

            // 删除容器
            dockerClient.removeContainerCmd(container.getId()).exec();

            // 删除临时文件
            Files.deleteIfExists(sourceFilePath);

            return output;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private Path createTempFile(String prefix, String content) throws IOException {
        Path tempFile = Files.createTempFile(prefix, null);
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }
}    