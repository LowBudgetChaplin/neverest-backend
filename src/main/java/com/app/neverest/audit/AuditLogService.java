package com.app.neverest.audit;

import com.app.neverest.persistence.entity.AuditLogEntity;
import com.app.neverest.persistence.repository.AuditLogRepository;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 500;
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String action, String actor, boolean success, String message, Map<String, String> metadata) {
        AuditLogEntity entry = new AuditLogEntity(
                UUID.randomUUID(),
                Instant.now(),
                action,
                actor,
                success,
                message,
                encodeMetadata(metadata)
        );

        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLogEntry> getEntries(Integer limit, String action, String actor, Boolean success) {
        int normalizedLimit = normalizeLimit(limit);
        Specification<AuditLogEntity> specification = (root, query, builder) -> builder.conjunction();

        if (action != null && !action.isBlank()) {
            String actionFilter = action.trim();
            specification = specification.and(
                    (root, query, builder) -> builder.equal(builder.lower(root.get("action")), actionFilter.toLowerCase())
            );
        }
        if (actor != null && !actor.isBlank()) {
            String actorFilter = actor.trim();
            specification = specification.and(
                    (root, query, builder) -> builder.equal(builder.lower(root.get("actor")), actorFilter.toLowerCase())
            );
        }
        if (success != null) {
            specification = specification.and(
                    (root, query, builder) -> builder.equal(root.get("success"), success)
            );
        }

        return auditLogRepository.findAll(
                        specification,
                        PageRequest.of(0, normalizedLimit, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .getContent()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private int normalizeLimit(Integer rawLimit) {
        if (rawLimit == null) {
            return DEFAULT_LIMIT;
        }
        if (rawLimit <= 0) {
            return 1;
        }
        return Math.min(rawLimit, MAX_LIMIT);
    }

    private AuditLogEntry toDomain(AuditLogEntity entity) {
        return new AuditLogEntry(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getAction(),
                entity.getActor(),
                entity.isSuccess(),
                entity.getMessage(),
                decodeMetadata(entity.getMetadataEncoded())
        );
    }

    private String encodeMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }

        return metadata.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private Map<String, String> decodeMetadata(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Map.of();
        }

        return java.util.Arrays.stream(encoded.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(
                        java.util.stream.Collectors.toMap(
                                parts -> decode(parts[0]),
                                parts -> decode(parts[1]),
                                (left, right) -> right
                        )
                );
    }

    private String encode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
