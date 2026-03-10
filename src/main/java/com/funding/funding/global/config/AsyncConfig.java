package com.funding.funding.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/*
  @Async 활성화
  EmailService의 비동기 메일 발송에 필요
  - 이메일 발송이 HTTP 응답을 블로킹하지 않음
  - 발송 실패해도 회원가입/비밀번호 찾기 응답에 영향 없음
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}