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
    }

    private void createTableIfNeeded() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tarkov_map (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(64) NOT NULL UNIQUE,
                    name_zh VARCHAR(128) NOT NULL,
                    name_en VARCHAR(128) NOT NULL,
                    banner_object_name VARCHAR(255) NULL,
                    map_object_name VARCHAR(255) NULL
                )
                """);

        Integer bannerColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'banner_object_name'
                        """,
                Integer.class
        );
        if (bannerColumnCount != null && bannerColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE tarkov_map ADD COLUMN banner_object_name VARCHAR(255) NULL");
        }

        Integer mapColumnCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'tarkov_map'
                          AND column_name = 'map_object_name'
                        """,
                Integer.class
        );
        if (mapColumnCount != null && mapColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE tarkov_map ADD COLUMN map_object_name VARCHAR(255) NULL");
        }

        // Backfill old rows with standard PNG asset naming.
        jdbcTemplate.update("""
                UPDATE tarkov_map
                SET banner_object_name = CONCAT('maps/banners/', code, '.png')
                WHERE banner_object_name IS NULL OR banner_object_name = ''
                """);
        jdbcTemplate.update("""
                UPDATE tarkov_map
                SET map_object_name = CONCAT('maps/bodies/', code, '.png')
                WHERE map_object_name IS NULL OR map_object_name = ''
                """);
    }

    private void seedDefaultMapsIfEmpty() {
        if (repository.count() > 0) {
            return;
        }

        List<TarkovMapEntity> defaults = List.of(
                new TarkovMapEntity("ground-zero", "零地", "Ground Zero", "maps/banners/ground-zero.png", "maps/bodies/ground-zero.png"),
                new TarkovMapEntity("factory", "工厂", "Factory", "maps/banners/factory.png", "maps/bodies/factory.png"),
                new TarkovMapEntity("customs", "海关", "Customs", "maps/banners/customs.png", "maps/bodies/customs.png"),
                new TarkovMapEntity("woods", "森林", "Woods", "maps/banners/woods.png", "maps/bodies/woods.png"),
                new TarkovMapEntity("shoreline", "海岸线", "Shoreline", "maps/banners/shoreline.png", "maps/bodies/shoreline.png"),
                new TarkovMapEntity("interchange", "立交桥", "Interchange", "maps/banners/interchange.png", "maps/bodies/interchange.png"),
                new TarkovMapEntity("reserve", "储备站", "Reserve", "maps/banners/reserve.png", "maps/bodies/reserve.png"),
                new TarkovMapEntity("lighthouse", "灯塔", "Lighthouse", "maps/banners/lighthouse.png", "maps/bodies/lighthouse.png"),
                new TarkovMapEntity("streets-of-tarkov", "塔科夫街区", "Streets of Tarkov", "maps/banners/streets-of-tarkov.png", "maps/bodies/streets-of-tarkov.png"),
                new TarkovMapEntity("the-lab", "实验室", "The Lab", "maps/banners/the-lab.png", "maps/bodies/the-lab.png")
        );

        repository.saveAll(defaults);
    }
}
