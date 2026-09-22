package com.smartspace.entry.dto;

import lombok.Data;
import java.util.List;

@Data
public class OfflineSyncRequest {
    private Long hallId;
    private List<OfflineLog> logs;
    
    @Data
    public static class OfflineLog {
        private String clientEventId;
        private String jti;
        private String eventType;
        private String verdict;
        private String reasonCode;
        private String identityMethod;
        private String deviceId;
        private Integer headcount;
        private Long occurredAtEpochMs;
    }
}
