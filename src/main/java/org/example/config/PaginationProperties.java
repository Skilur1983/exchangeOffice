package org.example.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class PaginationProperties {
    @Value("${page}")
    private int page;

    @Value("${pageSize}")
    private int pageSize;

    @Value("${sortBy}")
    private String sortBy;

    @Value("${sortOrder}")
    private String sortOrder;
}
