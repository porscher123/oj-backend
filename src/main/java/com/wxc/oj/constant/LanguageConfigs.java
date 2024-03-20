package com.wxc.oj.constant;

import com.wxc.oj.sandbox.model.LanguageConfig;

import java.util.Arrays;

public interface LanguageConfigs {


    LanguageConfig CPP = LanguageConfig.builder()
            .cmpArgs(Arrays.asList("/usr/bin/g++", "main.cpp", "-o", "main"))
            .exeArgs(Arrays.asList("main"))
            .envs(Arrays.asList("PATH=/usr/bin:/bin"))
            .exeFileName("main")
            .sourceFileName("main.cpp").build();
}
