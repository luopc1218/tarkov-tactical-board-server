package com.tarkov.board.map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tarkov_map")
public class TarkovMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_zh", nullable = false, length = 128)
    private String nameZh;

    @Column(name = "name_en", nullable = false, length = 128)
    private String nameEn;

    @Column(name = "banner_path", length = 255)
    private String bannerPath;

    @Column(name = "map_path", length = 255)
    private String mapPath;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    protected TarkovMapEntity() {
    }

    public TarkovMapEntity(String nameZh, String nameEn, String bannerPath, String mapPath) {
        this(nameZh, nameEn, bannerPath, mapPath, 0);
    }

    public TarkovMapEntity(String nameZh, String nameEn, String bannerPath, String mapPath, Integer sortOrder) {
        this.nameZh = nameZh;
        this.nameEn = nameEn;
        this.bannerPath = bannerPath;
        this.mapPath = mapPath;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    public String getMapPath() {
        return mapPath;
    }

    public void setMapPath(String mapPath) {
        this.mapPath = mapPath;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
