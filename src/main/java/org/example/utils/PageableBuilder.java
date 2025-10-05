package org.example.utils;

import lombok.RequiredArgsConstructor;
import org.example.config.PaginationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageableBuilder {

    private final PaginationProperties defaultProps;

    public Pageable build(Integer page, Integer size, String sortBy, String sortOrder) {
        int pageNum = (page != null) ? page : defaultProps.getPage();
        int pageSize = (size != null) ? size : defaultProps.getPageSize();
        String sortField = (sortBy != null) ? sortBy : defaultProps.getSortBy();
        String sortDir = (sortOrder != null) ? sortOrder : defaultProps.getSortOrder();

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        return PageRequest.of(pageNum, pageSize, sort);
    }
}
