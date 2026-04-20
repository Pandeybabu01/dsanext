package com.dsanext.dto.response;

import com.dsanext.domain.entity.Platform;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PlatformResponse {

    private UUID id;
    private String name;
    private String baseUrl;
    private String iconUrl;
    private boolean isActive;

    public static PlatformResponse from(Platform platform) {
        return PlatformResponse.builder()
                .id(platform.getId())
                .name(platform.getName())
                .baseUrl(platform.getBaseUrl())
                .iconUrl(platform.getIconUrl())
                .isActive(platform.isActive())
                .build();
    }
}
