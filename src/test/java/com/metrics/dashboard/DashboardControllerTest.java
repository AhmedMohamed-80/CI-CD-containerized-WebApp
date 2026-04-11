package com.metrics.dashboard;

import com.metrics.dashboard.service.MetricService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricService metricService;

    @Test
    void dashboard_shouldReturn200() throws Exception {
        when(metricService.getTotalCount()).thenReturn(5L);
        when(metricService.getAverageValue()).thenReturn(42.0);
        when(metricService.getRecentMetrics()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
               .andExpect(status().isOk())
               .andExpect(view().name("dashboard"));
    }

    @Test
    void apiMetrics_shouldReturnJson() throws Exception {
        when(metricService.getRecentMetrics()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/metrics"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void addMetric_shouldReturnSuccess() throws Exception {
        var metric = new com.metrics.dashboard.entity.Metric(
            "cpu_usage", 55.5, "WARNING", java.time.LocalDateTime.now());
        when(metricService.addSampleMetric()).thenReturn(metric);
        when(metricService.getTotalCount()).thenReturn(1L);
        when(metricService.getAverageValue()).thenReturn(55.5);

        mockMvc.perform(post("/api/metrics/add"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.success").value(true));
    }
}
