package com.example.analytics.controller;

import com.example.analytics.constants.AppConstants;
import com.example.analytics.dto.ApiResponse;
import com.example.analytics.dto.SearchResultResponse;
import com.example.analytics.service.SearchService;
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
@Tag(name = "Search", description = "Semantic search over tracked events")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Semantic search", description = "Embeds the query and ranks events by cosine similarity.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "" + AppConstants.DEFAULT_TOP_K) int topK) {
        log.info("GET /search called with query='{}' topK={}", query, topK);
        List<SearchResultResponse> results = searchService.search(query, topK);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.SUCCESS_MESSAGE_SEARCH, results));
    }
}
