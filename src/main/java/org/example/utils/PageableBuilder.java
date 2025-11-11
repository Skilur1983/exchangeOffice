package org.example.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.PaginationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageableBuilder {

    private final PaginationProperties defaultProps;

    private static final String PAGE = "page";
    private static final String SIZE = "size";
    private static final String SORT_BY = "sortBy";
    private static final String SORT_ORDER = "sortOrder";

    public Pageable build(Integer page, Integer size, String sortBy, String sortOrder) {
        int pageNum = validateAndGetPage(page);
        int pageSize = validateAndGetSize(size);
        String sortField = isValid(sortBy) ? sortBy : defaultProps.getSortBy();
        String sortDir = isValid(sortOrder) ? sortOrder : defaultProps.getSortOrder();

        Sort sort = createSort(sortField, sortDir);
        return PageRequest.of(pageNum, pageSize, sort);
    }

    public Pageable buildFromFilters(Map<String, String> filters) {
        Integer page = parseAndRemove(filters, PAGE);
        Integer size = parseAndRemove(filters, SIZE);
        String sortBy = filters.remove(SORT_BY);
        String sortOrder = filters.remove(SORT_ORDER);

        return build(page, size, sortBy, sortOrder);
    }

    public Pageable buildFromFiltersNonDestructive(Map<String, String> filters) {
        Integer page = parseInteger(filters.get(PAGE));
        Integer size = parseInteger(filters.get(SIZE));
        String sortBy = filters.get(SORT_BY);
        String sortOrder = filters.get(SORT_ORDER);

        return build(page, size, sortBy, sortOrder);
    }

    public Pageable buildDefault() {
        return build(null, null, null, null);
    }

    public Pageable buildUnpaged() {
        return Pageable.unpaged();
    }

    private int validateAndGetPage(Integer page) {
        if (page == null) {
            return defaultProps.getPage();
        }
        if (page < 0) {
            log.warn("Invalid page number: {}. Using default: {}", page, defaultProps.getPage());
            return defaultProps.getPage();
        }
        return page;
    }

    private int validateAndGetSize(Integer size) {
        if (size == null) {
            return defaultProps.getPageSize();
        }
        if (size < 1) {
            log.warn("Invalid page size: {}. Using default: {}", size, defaultProps.getPageSize());
            return defaultProps.getPageSize();
        }
        if (size > defaultProps.getMaxSize()) {
            log.warn("Page size {} exceeds maximum {}. Using max.", size, defaultProps.getMaxSize());
            return defaultProps.getMaxSize();
        }
        return size;
    }

    private Sort createSort(String sortBy, String sortOrder) {
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortOrder);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid sort order: '{}'. Using default: '{}'", sortOrder, defaultProps.getSortOrder());
            direction = Sort.Direction.fromString(defaultProps.getSortOrder());
        }

        return Sort.by(direction, sortBy);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer from: '{}'", value);
            return null;
        }
    }

    private Integer parseAndRemove(Map<String, String> map, String key) {
        String value = map.remove(key);
        return parseInteger(value);
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }
}
