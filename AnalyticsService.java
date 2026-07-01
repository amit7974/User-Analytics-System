package com.example.analytics.service;

import com.example.analytics.dto.AnalyticsResponse;

import java.time.LocalDate;

public interface AnalyticsService {

    AnalyticsResponse getAnalytics(String event, LocalDate from, LocalDate to);
}
