package com.smartspace.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagingUtils {

    private PagingUtils() {
        // Utility class
    }

    public static Pageable createPageRequest(int page, int size, String sort, String dir) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
        
        if (sort != null && !sort.isEmpty()) {
            Sort.Direction direction = Sort.Direction.fromString(dir != null ? dir : "ASC");
            return PageRequest.of(page, size, Sort.by(direction, sort));
        }
        
        return PageRequest.of(page, size);
    }
}
