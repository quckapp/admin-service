package com.quckapp.admin.controller;

import com.quckapp.admin.dto.AdminDtos.ApiResponse;
import com.quckapp.admin.dto.VersionDtos.PublicVersionMapResponse;
import com.quckapp.admin.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Tag(name = "Public Config", description = "Public configuration endpoints for client applications")
public class PublicConfigController {

    private final VersionService versionService;

    @Value("${app.base-url:https://api.quckapp.io}")
    private String baseUrl;

    @GetMapping("/versions")
    @Operation(summary = "Get active version map for client applications")
    public ResponseEntity<ApiResponse<PublicVersionMapResponse>> getVersionMap(
            @RequestParam String environment) {
        return ResponseEntity.ok(ApiResponse.success(versionService.getPublicVersionMap(environment)));
    }
}
