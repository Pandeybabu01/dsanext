package com.dsanext.dto.response;

import com.dsanext.domain.entity.AppSetting;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AppSettingResponse {

    private UUID id;
    private String settingKey;
    private String settingValue;
    private String dataType;
    private String description;
    private boolean isPublic;
    private Instant updatedAt;

    public static AppSettingResponse from(AppSetting s) {
        return AppSettingResponse.builder()
                .id(s.getId())
                .settingKey(s.getSettingKey())
                .settingValue(s.getSettingValue())
                .dataType(s.getDataType())
                .description(s.getDescription())
                .isPublic(s.isPublic())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
