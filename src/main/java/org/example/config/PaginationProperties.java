package org.example.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@ConfigurationProperties(prefix = "pagination")
@Validated
public class PaginationProperties {
    @Value("${page}")
    private int page;

    @Value("${pageSize}")
    private int pageSize;

    @Value("${maxSize}")
    private int maxSize;

    @Value("${sortBy}")
    private String sortBy;

    @Value("${sortOrder}")
    private String sortOrder;
}
