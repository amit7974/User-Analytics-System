package com.example.analytics.service;

import com.example.analytics.dto.SearchResultResponse;

import java.util.List;

public interface SearchService {

    List<SearchResultResponse> search(String query, int topK);
}
