package com.example.analytics.service;

import com.example.analytics.dto.EventResponse;
import com.example.analytics.dto.TrackEventRequest;

public interface TrackingService {

    EventResponse trackEvent(TrackEventRequest request);
}
