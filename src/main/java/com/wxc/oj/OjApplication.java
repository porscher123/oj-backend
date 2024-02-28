package com.wxc.oj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wxc.oj.mapper")
public class OjApplication {

	public static void main(String[] args) {
		SpringApplication.run(OjApplication.class, args);
	}

}
