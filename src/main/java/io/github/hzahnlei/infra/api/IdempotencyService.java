package io.github.hzahnlei.infra.api;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory idempotency store keyed by the Idempotency-Key header value.
 * First call with a given key executes the operation and caches the result;
 * subsequent calls with the same key return the cached result without re-executing.
 */
@Service
public class IdempotencyService {

    private final Map<String, List<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Optional<List<T>> get(String key) {
        return Optional.ofNullable((List<T>) cache.get(key));
    }

    public void store(String key, List<?> result) {
        cache.putIfAbsent(key, result);
    }
}
