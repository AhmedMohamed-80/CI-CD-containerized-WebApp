package com.metrics.dashboard.controller;

import com.metrics.dashboard.entity.Metric;
import com.metrics.dashboard.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final MetricService metricService;

    public DashboardController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalCount", metricService.getTotalCount());
        model.addAttribute("averageValue", metricService.getAverageValue());
        model.addAttribute("recentMetrics", metricService.getRecentMetrics());
        return "dashboard";
    }

    @GetMapping("/api/metrics")
    @ResponseBody
    public ResponseEntity<List<Metric>> getMetrics() {
        return ResponseEntity.ok(metricService.getRecentMetrics());
    }

    @PostMapping("/api/metrics/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addMetric() {
        Metric metric = metricService.addSampleMetric();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("metric", metric);
        response.put("totalCount", metricService.getTotalCount());
        response.put("averageValue", metricService.getAverageValue());
        return ResponseEntity.ok(response);
    }
}
