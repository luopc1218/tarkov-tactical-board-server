package com.tarkov.board.map;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TarkovMapInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final TarkovMapRepository repository;

    public TarkovMapInitializer(JdbcTemplate jdbcTemplate, TarkovMapRepository repository) {
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        createTableIfNeeded();
        seedDefaultMapsIfEmpty();
        normalizeSortOrder();
    }

    private void createTableIfNeeded() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tarkov_map (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name_zh VARCHAR(128) NOT NULL,
                    name_en VARCHAR(128) NOT NULL,
                    banner_file_name VARCHAR(255) NULL,
                    map_file_name VARCHAR(255) NULL,
                    sort_order INT NOT NULL DEFAULT 0
                )
                """);

        renameOldColumnsIfPresent();
        ensureNewColumnsPresent();
        migrateAndDropCodeColumnIfPresent();
    }

    private void renameOldColumnsIfPresent() {
        Integer oldBannerColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'banner_object_name'
                """,
                Integer.class
        );
        Integer currentBannerColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'banner_file_name'
                        """,
                Integer.class
        );
        Integer legacyBannerPathColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'banner_path'
                        """,
                Integer.class
        );

        if (oldBannerColumnCount != null && oldBannerColumnCount > 0) {
            if (currentBannerColumnCount != null && currentBannerColumnCount > 0) {
                jdbcTemplate.update("""
                        UPDATE tarkov_map
                        SET banner_file_name = COALESCE(NULLIF(banner_file_name, ''), banner_object_name)
                        WHERE banner_object_name IS NOT NULL AND banner_object_name <> ''
                        """);
                jdbcTemplate.execute("ALTER TABLE tarkov_map DROP COLUMN banner_object_name");
            } else {
                jdbcTemplate.execute("ALTER TABLE tarkov_map CHANGE COLUMN banner_object_name banner_file_name VARCHAR(255) NULL");
            }
        }

        if (legacyBannerPathColumnCount != null && legacyBannerPathColumnCount > 0) {
            if (currentBannerColumnCount != null && currentBannerColumnCount > 0) {
                jdbcTemplate.update("""
                        UPDATE tarkov_map
                        SET banner_file_name = COALESCE(NULLIF(banner_file_name, ''), banner_path)
                        WHERE banner_path IS NOT NULL AND banner_path <> ''
                        """);
                jdbcTemplate.execute("ALTER TABLE tarkov_map DROP COLUMN banner_path");
            } else {
                jdbcTemplate.execute("ALTER TABLE tarkov_map CHANGE COLUMN banner_path banner_file_name VARCHAR(255) NULL");
            }
        }

        Integer oldMapColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'map_object_name'
                """,
                Integer.class
        );
        Integer currentMapColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'map_file_name'
                        """,
                Integer.class
        );
        Integer legacyMapPathColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'map_path'
                        """,
                Integer.class
        );

        if (oldMapColumnCount != null && oldMapColumnCount > 0) {
            if (currentMapColumnCount != null && currentMapColumnCount > 0) {
                jdbcTemplate.update("""
                        UPDATE tarkov_map
                        SET map_file_name = COALESCE(NULLIF(map_file_name, ''), map_object_name)
                        WHERE map_object_name IS NOT NULL AND map_object_name <> ''
                        """);
                jdbcTemplate.execute("ALTER TABLE tarkov_map DROP COLUMN map_object_name");
            } else {
                jdbcTemplate.execute("ALTER TABLE tarkov_map CHANGE COLUMN map_object_name map_file_name VARCHAR(255) NULL");
            }
        }

        if (legacyMapPathColumnCount != null && legacyMapPathColumnCount > 0) {
            if (currentMapColumnCount != null && currentMapColumnCount > 0) {
                jdbcTemplate.update("""
                        UPDATE tarkov_map
                        SET map_file_name = COALESCE(NULLIF(map_file_name, ''), map_path)
                        WHERE map_path IS NOT NULL AND map_path <> ''
                        """);
                jdbcTemplate.execute("ALTER TABLE tarkov_map DROP COLUMN map_path");
            } else {
                jdbcTemplate.execute("ALTER TABLE tarkov_map CHANGE COLUMN map_path map_file_name VARCHAR(255) NULL");
            }
        }
    }

    private void ensureNewColumnsPresent() {
        Integer bannerColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'banner_file_name'
                        """,
                Integer.class
        );
        if (bannerColumnCount != null && bannerColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE tarkov_map ADD COLUMN banner_file_name VARCHAR(255) NULL");
        }

        Integer mapColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'map_file_name'
                        """,
                Integer.class
        );
        if (mapColumnCount != null && mapColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE tarkov_map ADD COLUMN map_file_name VARCHAR(255) NULL");
        }

        Integer sortColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'sort_order'
                        """,
                Integer.class
        );
        if (sortColumnCount != null && sortColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE tarkov_map ADD COLUMN sort_order INT NOT NULL DEFAULT 0");
        }
    }

    private void migrateAndDropCodeColumnIfPresent() {
        Integer codeColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'code'
                        """,
                Integer.class
        );
        if (codeColumnCount == null || codeColumnCount == 0) {
            return;
        }

        jdbcTemplate.update("""
                UPDATE tarkov_map
                SET banner_file_name = CONCAT('maps/banners/', code, '.png')
                WHERE (banner_file_name IS NULL OR banner_file_name = '')
                  AND code IS NOT NULL AND code <> ''
                """);
        jdbcTemplate.update("""
                UPDATE tarkov_map
                SET map_file_name = CONCAT('maps/bodies/', code, '.png')
                WHERE (map_file_name IS NULL OR map_file_name = '')
                  AND code IS NOT NULL AND code <> ''
                """);

        jdbcTemplate.execute("ALTER TABLE tarkov_map DROP COLUMN code");
    }

    private void seedDefaultMapsIfEmpty() {
        if (repository.count() > 0) {
            return;
        }

        List<TarkovMapEntity> defaults = List.of(
                new TarkovMapEntity("零地", "Ground Zero", "maps/banners/ground-zero.png", "maps/bodies/ground-zero.png"),
                new TarkovMapEntity("工厂", "Factory", "maps/banners/factory.png", "maps/bodies/factory.png"),
                new TarkovMapEntity("海关", "Customs", "maps/banners/customs.png", "maps/bodies/customs.png"),
                new TarkovMapEntity("森林", "Woods", "maps/banners/woods.png", "maps/bodies/woods.png"),
                new TarkovMapEntity("海岸线", "Shoreline", "maps/banners/shoreline.png", "maps/bodies/shoreline.png"),
                new TarkovMapEntity("立交桥", "Interchange", "maps/banners/interchange.png", "maps/bodies/interchange.png"),
                new TarkovMapEntity("储备站", "Reserve", "maps/banners/reserve.png", "maps/bodies/reserve.png"),
                new TarkovMapEntity("灯塔", "Lighthouse", "maps/banners/lighthouse.png", "maps/bodies/lighthouse.png"),
                new TarkovMapEntity("塔科夫街区", "Streets of Tarkov", "maps/banners/streets-of-tarkov.png", "maps/bodies/streets-of-tarkov.png"),
                new TarkovMapEntity("实验室", "The Lab", "maps/banners/the-lab.png", "maps/bodies/the-lab.png")
        );

        repository.saveAll(defaults);
    }

    private void normalizeSortOrder() {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM tarkov_map ORDER BY sort_order ASC, id ASC",
                Long.class
        );
        for (int i = 0; i < ids.size(); i++) {
            jdbcTemplate.update("UPDATE tarkov_map SET sort_order = ? WHERE id = ?", i + 1, ids.get(i));
        }
    }
}
