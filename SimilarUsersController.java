package com.example.analytics.controller;

import com.example.analytics.constants.AppConstants;
import com.example.analytics.dto.ApiResponse;
import com.example.analytics.dto.SimilarUserResponse;
import com.example.analytics.service.SimilarUsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Similar Users", description = "Find users with similar behavioral patterns")
public class SimilarUsersController {

    private final SimilarUsersService similarUsersService;

    @Operation(summary = "Find similar users",
            description = "Builds a behavioral centroid embedding per user and ranks others by cosine similarity.")
    @GetMapping("/similar-users")
    public ResponseEntity<ApiResponse<List<SimilarUserResponse>>> similarUsers(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "" + AppConstants.DEFAULT_TOP_K) int topK) {
        log.info("GET /similar-users called with userId={} topK={}", userId, topK);
        List<SimilarUserResponse> results = similarUsersService.findSimilarUsers(userId, topK);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.SUCCESS_MESSAGE_SIMILAR_USERS, results));
    }
}
