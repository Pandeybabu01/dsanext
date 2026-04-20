package com.dsanext.dto.response;

import com.dsanext.domain.entity.UserSetting;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserSettingResponse {

    private UUID id;
    private String theme;
    private boolean notificationsEnabled;
    private boolean emailNotifications;
    private String lcUsername;
    private String cfUsername;
    private String hrUsername;
    private String ibUsername;
    private Instant updatedAt;

    public static UserSettingResponse from(UserSetting s) {
        return UserSettingResponse.builder()
                .id(s.getId())
                .theme(s.getTheme())
                .notificationsEnabled(s.isNotificationsEnabled())
                .emailNotifications(s.isEmailNotifications())
                .lcUsername(s.getLcUsername())
                .cfUsername(s.getCfUsername())
                .hrUsername(s.getHrUsername())
                .ibUsername(s.getIbUsername())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
