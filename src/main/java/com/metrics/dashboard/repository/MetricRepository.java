package com.metrics.dashboard.repository;

import com.metrics.dashboard.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    List<Metric> findTop10ByOrderByTimestampDesc();

    @Query("SELECT AVG(m.value) FROM Metric m")
    Double findAverageValue();

    @Query("SELECT COUNT(m) FROM Metric m")
    Long countAll();
}
