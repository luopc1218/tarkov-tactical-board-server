package com.tarkov.board.mapintel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tarkov_map_loot")
public class TarkovMapLootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "map_name_zh", nullable = false, length = 128, unique = true)
    private String mapNameZh;

    @Column(name = "version", nullable = false, length = 64)
    private String version;

    @Lob
    @Column(name = "loot_json", nullable = false, columnDefinition = "LONGTEXT")
    private String lootJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TarkovMapLootEntity() {
    }

    public TarkovMapLootEntity(String mapNameZh, String version, String lootJson, Instant updatedAt) {
        this.mapNameZh = mapNameZh;
        this.version = version;
        this.lootJson = lootJson;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getMapNameZh() {
        return mapNameZh;
    }

    public void setMapNameZh(String mapNameZh) {
        this.mapNameZh = mapNameZh;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLootJson() {
        return lootJson;
    }

    public void setLootJson(String lootJson) {
        this.lootJson = lootJson;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
