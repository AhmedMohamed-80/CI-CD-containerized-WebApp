package com.metrics.dashboard.service;

import com.metrics.dashboard.entity.Metric;
import com.metrics.dashboard.repository.MetricRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class MetricService {

    private final MetricRepository metricRepository;
    private final Random random = new Random();

    private static final String[] METRIC_NAMES = {
        "cpu_usage", "memory_usage", "disk_io", "network_latency",
        "request_rate", "error_rate", "response_time", "throughput",
        "cache_hit_ratio", "db_query_time"
    };

    private static final String[] STATUSES = {"OK", "WARNING", "CRITICAL"};

    public MetricService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    public List<Metric> getRecentMetrics() {
        return metricRepository.findTop10ByOrderByTimestampDesc();
    }

    public List<Metric> getAllMetrics() {
        return metricRepository.findAll();
    }

    public Long getTotalCount() {
        return metricRepository.countAll();
    }

    public Double getAverageValue() {
        Double avg = metricRepository.findAverageValue();
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    public Metric addSampleMetric() {
        String name = METRIC_NAMES[random.nextInt(METRIC_NAMES.length)];
        double value = Math.round(random.nextDouble() * 10000.0) / 100.0;
        String status;
        if (value < 50) {
            status = "OK";
        } else if (value < 80) {
            status = "WARNING";
        } else {
            status = "CRITICAL";
        }
        Metric metric = new Metric(name, value, status, LocalDateTime.now());
        return metricRepository.save(metric);
    }
}
