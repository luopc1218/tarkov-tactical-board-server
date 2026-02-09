package com.tarkov.board.whiteboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkov.board.map.TarkovMapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class WhiteboardInstanceService {

    private static final String WS_PATH_PREFIX = "/ws/whiteboard/";
    private static final Duration RETENTION_DURATION = Duration.ofHours(72);
    private static final Set<String> SNAPSHOT_TYPES = Set.of("snapshot", "state_snapshot", "full_state");

    private final WhiteboardInstanceRepository repository;
    private final TarkovMapRepository mapRepository;
    private final ObjectMapper objectMapper;

    public WhiteboardInstanceService(WhiteboardInstanceRepository repository,
                                     TarkovMapRepository mapRepository,
                                     ObjectMapper objectMapper) {
        this.repository = repository;
        this.mapRepository = mapRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WhiteboardInstanceResponse createInstance(Long mapId) {
        cleanupExpiredInstances();
        validateMapExists(mapId);

        Instant now = Instant.now();
        String instanceId = UUID.randomUUID().toString();
        WhiteboardInstanceEntity entity = new WhiteboardInstanceEntity(
                instanceId, mapId, null, now, now, now.plus(RETENTION_DURATION));
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public WhiteboardInstanceResponse getInstance(String instanceId) {
        return toResponse(getActiveEntityOrThrow(instanceId));
    }

    @Transactional(readOnly = true)
    public boolean isInstanceActive(String instanceId) {
        return repository.existsByInstanceIdAndExpireAtAfter(instanceId, Instant.now());
    }

    @Transactional(readOnly = true)
    public WhiteboardStateResponse getState(String instanceId) {
        WhiteboardInstanceEntity entity = getActiveEntityOrThrow(instanceId);
        return new WhiteboardStateResponse(
                entity.getInstanceId(),
                parseState(entity.getStateJson()),
                entity.getUpdatedAt(),
                entity.getExpireAt()
        );
    }

    @Transactional
    public WhiteboardStateResponse saveState(String instanceId, JsonNode state) {
        WhiteboardInstanceEntity entity = getActiveEntityOrThrow(instanceId);
        saveStateInternal(entity, state);
        repository.save(entity);
        return new WhiteboardStateResponse(
                entity.getInstanceId(),
                parseState(entity.getStateJson()),
                entity.getUpdatedAt(),
                entity.getExpireAt()
        );
    }

    @Transactional
    public void persistSnapshotFromWebSocket(String instanceId, String payload) {
        WhiteboardInstanceEntity entity = repository.findByInstanceIdAndExpireAtAfter(instanceId, Instant.now())
                .orElse(null);
        if (entity == null) {
            return;
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (JsonProcessingException ignored) {
            return;
        }

        if (!root.isObject()) {
            return;
        }

        String messageType = root.path("type").asText("").toLowerCase(Locale.ROOT);
        if (!SNAPSHOT_TYPES.contains(messageType)) {
            return;
        }

        JsonNode state = root.hasNonNull("state") ? root.get("state") : root.path("data");
        if (state == null || state.isMissingNode() || state.isNull()) {
            return;
        }

        saveStateInternal(entity, state);
        repository.save(entity);
    }

    @Transactional
    public void cleanupExpiredInstances() {
        repository.deleteExpired(Instant.now());
    }

    private void saveStateInternal(WhiteboardInstanceEntity entity, JsonNode state) {
        Instant now = Instant.now();
        entity.setStateJson(writeState(state));
        entity.setUpdatedAt(now);
        entity.setExpireAt(now.plus(RETENTION_DURATION));
    }

    private JsonNode parseState(String stateJson) {
        if (stateJson == null || stateJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(stateJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored whiteboard state is invalid JSON", e);
        }
    }

    private String writeState(JsonNode state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid whiteboard state JSON", e);
        }
    }

    private WhiteboardInstanceEntity getActiveEntityOrThrow(String instanceId) {
        return repository.findByInstanceIdAndExpireAtAfter(instanceId, Instant.now())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Whiteboard instance not found or expired"));
    }

    private WhiteboardInstanceResponse toResponse(WhiteboardInstanceEntity entity) {
        return new WhiteboardInstanceResponse(
                entity.getInstanceId(),
                entity.getMapId(),
                WS_PATH_PREFIX + entity.getInstanceId(),
                entity.getCreatedAt(),
                entity.getExpireAt()
        );
    }

    private void validateMapExists(Long mapId) {
        if (!mapRepository.existsById(mapId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Map not found");
        }
    }
}
