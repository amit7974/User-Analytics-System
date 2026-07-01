package com.example.analytics.service;

import com.example.analytics.dto.SimilarUserResponse;

import java.util.List;

public interface SimilarUsersService {

    List<SimilarUserResponse> findSimilarUsers(String userId, int topK);
}
