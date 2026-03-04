package com.funding.funding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Spring이 Scheduled를 스캔하고 실행하도록 켜는 스위치
public class FundingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundingApplication.class, args);
	}

}

// 프로젝트 전체에서 스케줄링 기능을 켜놓음