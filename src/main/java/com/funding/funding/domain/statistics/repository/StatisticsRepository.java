package com.funding.funding.domain.statistics.repository;

import com.funding.funding.domain.statistics.entity.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<DailyStatistics, LocalDate> {

    // 최근 N일 통계 (날짜 역순)
    List<DailyStatistics> findTop30ByOrderByStatDateDesc();
}