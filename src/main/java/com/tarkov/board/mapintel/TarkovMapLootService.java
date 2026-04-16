package com.tarkov.board.mapintel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkov.board.map.TarkovMapEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TarkovMapLootService {

    private static final Map<String, List<String>> MAP_NAME_ALIASES = Map.of(
            "立交桥", List.of("商场"),
            "塔科夫街区", List.of("街区")
    );

    private final TarkovMapLootRepository repository;
    private final ObjectMapper objectMapper;

    public TarkovMapLootService(TarkovMapLootRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Optional<WhiteboardMapIntelResponse.HighValueLootInfo> getLootInfo(TarkovMapEntity map) {
        Optional<TarkovMapLootEntity> exactMatch = repository.findByMapNameZh(map.getNameZh());
        if (exactMatch.isPresent()) {
            return exactMatch.map(this::toLootInfo);
        }

        LinkedHashSet<String> candidateNames = new LinkedHashSet<>();
        candidateNames.addAll(MAP_NAME_ALIASES.getOrDefault(map.getNameZh(), List.of()));

        return repository.findFirstByMapNameZhIn(candidateNames)
                .map(this::toLootInfo);
    }

    @Transactional(readOnly = true)
    public TarkovMapHighValueLootAdminResponse getAdminLootInfo(TarkovMapEntity map) {
        return findManagedLootEntity(map)
                .map(entity -> toAdminResponse(map, entity))
                .orElseGet(() -> new TarkovMapHighValueLootAdminResponse(
                        map.getId(),
                        map.getNameZh(),
                        map.getNameEn(),
                        null,
                        null,
                        List.of()
                ));
    }

    @Transactional
    public TarkovMapHighValueLootAdminResponse saveAdminLootInfo(TarkovMapEntity map,
                                                                 TarkovMapHighValueLootUpsertRequest request) {
        Instant now = Instant.now();
        TarkovMapLootEntity entity = findManagedLootEntity(map)
                .orElseGet(() -> new TarkovMapLootEntity(map.getNameZh(), "", "[]", now));

        if (!map.getNameZh().equals(entity.getMapNameZh())) {
            entity.setMapNameZh(map.getNameZh());
        }

        entity.setVersion(buildAdminVersion(now));
        entity.setLootJson(writeLootJson(request.points()));
        entity.setUpdatedAt(now);
        repository.save(entity);
        return toAdminResponse(map, entity);
    }

    @Transactional
    public void renameMapLootIfNeeded(String oldMapNameZh, String newMapNameZh) {
        if (oldMapNameZh == null || newMapNameZh == null || oldMapNameZh.equals(newMapNameZh)) {
            return;
        }
        repository.findByMapNameZh(oldMapNameZh).ifPresent(entity -> {
            entity.setMapNameZh(newMapNameZh);
            repository.save(entity);
        });
    }

    @Transactional
    public void deleteMapLoot(String mapNameZh) {
        repository.findByMapNameZh(mapNameZh).ifPresent(entity -> repository.delete(entity));
    }

    private Optional<TarkovMapLootEntity> findManagedLootEntity(TarkovMapEntity map) {
        Optional<TarkovMapLootEntity> exactMatch = repository.findByMapNameZh(map.getNameZh());
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        LinkedHashSet<String> candidateNames = new LinkedHashSet<>();
        candidateNames.addAll(MAP_NAME_ALIASES.getOrDefault(map.getNameZh(), List.of()));
        if (candidateNames.isEmpty()) {
            return Optional.empty();
        }
        return repository.findFirstByMapNameZhIn(candidateNames);
    }

    private WhiteboardMapIntelResponse.HighValueLootInfo toLootInfo(TarkovMapLootEntity entity) {
        return new WhiteboardMapIntelResponse.HighValueLootInfo(
                entity.getVersion(),
                parseLootPoints(entity.getLootJson())
        );
    }

    private TarkovMapHighValueLootAdminResponse toAdminResponse(TarkovMapEntity map, TarkovMapLootEntity entity) {
        return new TarkovMapHighValueLootAdminResponse(
                map.getId(),
                map.getNameZh(),
                map.getNameEn(),
                entity.getVersion(),
                entity.getUpdatedAt(),
                parseLootPoints(entity.getLootJson())
        );
    }

    private List<WhiteboardMapIntelResponse.HighValueLootPoint> parseLootPoints(String lootJson) {
        try {
            List<TarkovMapLootInitializer.LootSeedPointItem> items = objectMapper.readValue(
                    lootJson,
                    new TypeReference<>() {
                    }
            );
            List<WhiteboardMapIntelResponse.HighValueLootPoint> points = new ArrayList<>();
            for (TarkovMapLootInitializer.LootSeedPointItem item : items) {
                points.add(new WhiteboardMapIntelResponse.HighValueLootPoint(
                        item.area(),
                        item.location(),
                        item.items() == null ? List.of() : item.items(),
                        normalizeKeys(item.key()),
                        item.priority()
                ));
            }
            return points;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse stored loot JSON", e);
        }
    }

    private String writeLootJson(List<TarkovMapHighValueLootPointUpsertRequest> points) {
        List<TarkovMapLootInitializer.LootSeedPointItem> normalized = new ArrayList<>();
        for (TarkovMapHighValueLootPointUpsertRequest point : points) {
            normalized.add(new TarkovMapLootInitializer.LootSeedPointItem(
                    point.area(),
                    point.location(),
                    point.items(),
                    point.keys(),
                    point.priority()
            ));
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write loot JSON", e);
        }
    }

    private String buildAdminVersion(Instant updatedAt) {
        return "admin-" + updatedAt.toString();
    }

    private List<String> normalizeKeys(Object rawKey) {
        if (rawKey == null) {
            return List.of();
        }
        if (rawKey instanceof String key) {
            return key.isBlank() ? List.of() : List.of(key);
        }
        if (rawKey instanceof List<?> keys) {
            List<String> normalized = new ArrayList<>();
            for (Object value : keys) {
                if (value instanceof String key && !key.isBlank()) {
                    normalized.add(key);
                }
            }
            return normalized;
        }
        return List.of(String.valueOf(rawKey));
    }
}
