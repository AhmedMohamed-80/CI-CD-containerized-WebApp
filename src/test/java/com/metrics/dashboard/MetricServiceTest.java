package com.metrics.dashboard;

import com.metrics.dashboard.entity.Metric;
import com.metrics.dashboard.repository.MetricRepository;
import com.metrics.dashboard.service.MetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(MetricService.class)
class MetricServiceTest {

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        metricRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    void addSampleMetric_shouldPersistAndReturnMetric() {
        Metric saved = metricService.addSampleMetric();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isNotBlank();
        assertThat(saved.getValue()).isGreaterThanOrEqualTo(0.0);
        assertThat(saved.getStatus()).isIn("OK", "WARNING", "CRITICAL");
        assertThat(saved.getTimestamp()).isNotNull();
    }

    @Test
    void getRecentMetrics_shouldReturnAtMostTen() {
        for (int i = 0; i < 15; i++) {
            metricService.addSampleMetric();
        }
        List<Metric> recent = metricService.getRecentMetrics();
        assertThat(recent).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void getTotalCount_shouldReflectInsertedMetrics() {
        metricService.addSampleMetric();
        metricService.addSampleMetric();
        assertThat(metricService.getTotalCount()).isEqualTo(2L);
    }

    @Test
    void getAverageValue_shouldReturnCorrectAverage() {
        metricRepository.save(new Metric("test_a", 10.0, "OK", LocalDateTime.now()));
        metricRepository.save(new Metric("test_b", 30.0, "OK", LocalDateTime.now()));
        entityManager.flush();

        double avg = metricService.getAverageValue();
        assertThat(avg).isEqualTo(20.0);
    }
}
