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

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name_zh", nullable = false, length = 128)
    private String nameZh;

    @Column(name = "name_en", nullable = false, length = 128)
    private String nameEn;

    @Column(name = "banner_object_name", length = 255)
    private String bannerObjectName;

    @Column(name = "map_object_name", length = 255)
    private String mapObjectName;

    protected TarkovMapEntity() {
    }

    public TarkovMapEntity(String code, String nameZh, String nameEn, String bannerObjectName, String mapObjectName) {
        this.code = code;
        this.nameZh = nameZh;
        this.nameEn = nameEn;
        this.bannerObjectName = bannerObjectName;
        this.mapObjectName = mapObjectName;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getBannerObjectName() {
        return bannerObjectName;
    }

    public void setBannerObjectName(String bannerObjectName) {
        this.bannerObjectName = bannerObjectName;
    }

    public String getMapObjectName() {
        return mapObjectName;
    }

    public void setMapObjectName(String mapObjectName) {
        this.mapObjectName = mapObjectName;
    }
}
