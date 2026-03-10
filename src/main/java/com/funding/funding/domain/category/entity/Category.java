package com.funding.funding.domain.category.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "categories")
public class Category {

    // Jackson 응답을 위해 getter 필요 (없으면 data가 비거나 오류 날 수 있음)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // created_at은 생성 시점에만 세팅되고 수정되면 안 됨
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Category() {
        // JPA 기본 생성자(권장: protected)
    }

    // 이름으로 생성
    public Category(String name) {
        this.name = name;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 이름 수정
    public void updateName(String name) {
        this.name = name;
    }

}