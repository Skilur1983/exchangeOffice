package org.example.repository.specification;

import lombok.extern.slf4j.Slf4j;
import org.example.exceptions.FilterIllegalArgumentException;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class SpecificationManager<T> {

    private final Map<String, SpecificationProvider<T>> providerMap;

    public SpecificationManager(List<SpecificationProvider<T>> specificationProviders) {
        this.providerMap = specificationProviders.stream()
                .collect(Collectors.toMap(
                        SpecificationProvider::getFilterKey,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("Duplicate filter key found: {}. Using first registered provider.",
                                    existing.getFilterKey());
                            return existing;
                        }
                ));

        log.info("Registered {} specification providers", providerMap.size());
        if (log.isDebugEnabled()) {
            providerMap.keySet().forEach(key -> log.debug("  - {}", key));
        }
    }

    public Specification<T> get(String filterKey, String[] params) {
        if (!providerMap.containsKey(filterKey)) {
            throw new FilterIllegalArgumentException(
                    "Filter key '" + filterKey + "' is not supported. Available keys: " +
                            String.join(", ", providerMap.keySet())
            );
        }

        if (params == null || params.length == 0 || (params.length == 1 && params[0].isEmpty())) {
            log.debug("Empty params for filter key: {}", filterKey);
            return null;
        }

        return providerMap.get(filterKey).getSpecification(params);
    }

    public boolean supports(String filterKey) {
        return providerMap.containsKey(filterKey);
    }

    public java.util.Set<String> getSupportedKeys() {
        return providerMap.keySet();
    }
}
