package com.wxc.oj.mysandbox.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {
    @Bean
    public DockerClient dockerClient() {
        return DockerClientBuilder.getInstance("tcp://123.249.0.179:2375").build();
    }
}