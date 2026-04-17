package com.tarkov.board.mapintel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Component
@DependsOn("tarkovMapInitializer")
public class TarkovMapLootInitializer {

    private static final String SEED_RESOURCE = "seeds/tarkov-map-loot-info.json";

    private final JdbcTemplate jdbcTemplate;
    private final TarkovMapLootRepository repository;
    private final ObjectMapper objectMapper;

    public TarkovMapLootInitializer(JdbcTemplate jdbcTemplate,
                                    TarkovMapLootRepository repository,
                                    ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        createTableIfNeeded();
        upsertSeedData();
    }

    private void createTableIfNeeded() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tarkov_map_loot (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    map_name_zh VARCHAR(128) NOT NULL,
                    version VARCHAR(64) NOT NULL,
                    loot_json LONGTEXT NOT NULL,
                    updated_at DATETIME(3) NOT NULL,
                    UNIQUE KEY uk_tarkov_map_loot_map_name_zh (map_name_zh)
                )
                """);
    }

    private void upsertSeedData() {
        LootSeedRoot root = loadSeedData();
        Instant now = Instant.now();
        for (LootSeedMapItem item : root.maps()) {
            String seedLootJson = writeLootJson(item.points());
            TarkovMapLootEntity entity = repository.findByMapNameZh(item.mapName())
                    .orElseGet(() -> new TarkovMapLootEntity(item.mapName(), root.version(), seedLootJson, now));

            if (entity.getId() == null || shouldRefreshFromSeed(entity, root.version())) {
                entity.setVersion(root.version());
                entity.setLootJson(seedLootJson);
                entity.setUpdatedAt(now);
                repository.save(entity);
            }
        }
    }

    private boolean shouldRefreshFromSeed(TarkovMapLootEntity entity, String seedVersion) {
        if (isBlankLootJson(entity.getLootJson())) {
            return true;
        }
        String version = entity.getVersion();
        if (version == null || version.isBlank()) {
            return true;
        }
        if (version.startsWith("admin-")) {
            return false;
        }
        return !seedVersion.equals(version);
    }

    private boolean isBlankLootJson(String lootJson) {
        return lootJson == null || lootJson.isBlank() || "[]".equals(lootJson.trim());
    }

    private LootSeedRoot loadSeedData() {
        ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, LootSeedRoot.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load map loot seed data", e);
        }
    }

    private String writeLootJson(List<LootSeedPointItem> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write loot JSON", e);
        }
    }

    public record LootSeedRoot(@JsonProperty("版本") String version,
                               @JsonProperty("地图列表") List<LootSeedMapItem> maps) {
    }

    public record LootSeedMapItem(@JsonProperty("地图") String mapName,
                                  @JsonProperty("资源点") List<LootSeedPointItem> points) {
    }

    public record LootSeedPointItem(@JsonProperty("区域") String area,
                                    @JsonProperty("位置") String location,
                                    @JsonProperty("物资") List<String> items,
                                    @JsonProperty("钥匙") Object key,
                                    @JsonProperty("优先级") String priority) {
    }
}
