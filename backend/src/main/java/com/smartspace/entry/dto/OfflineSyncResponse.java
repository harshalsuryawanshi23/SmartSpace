package com.smartspace.entry.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OfflineSyncResponse {
    private int processedCount;
    private int conflictCount;
    private List<SyncConflict> conflicts;

    @Data
    @Builder
    public static class SyncConflict {
        private String clientEventId;
        private String reason;
    }
}
