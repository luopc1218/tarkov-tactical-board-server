package com.tarkov.board.map;

import com.tarkov.board.service.MinioService;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MapAssetResolver {

    private static final Duration URL_EXPIRE = Duration.ofHours(12);

    private final MinioService minioService;

    public MapAssetResolver(MinioService minioService) {
        this.minioService = minioService;
    }

    public AssetResolved resolveBanner(TarkovMapEntity entity) {
        String objectName = firstPresent(
                entity.getBannerObjectName(),
                "maps/banners/" + entity.getCode() + ".png"
        );
        return buildResolved(objectName);
    }

    public AssetResolved resolveMapBody(TarkovMapEntity entity) {
        String objectName = firstPresent(
                entity.getMapObjectName(),
                "maps/bodies/" + entity.getCode() + ".png"
        );
        return buildResolved(objectName);
    }

    private AssetResolved buildResolved(String objectName) {
        if (objectName == null) {
            return new AssetResolved(null, null);
        }
        String url = minioService.objectExists(objectName)
                ? minioService.getPresignedDownloadUrl(objectName, URL_EXPIRE)
                : null;
        return new AssetResolved(objectName, url);
    }

    private String firstPresent(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }

    public record AssetResolved(String objectName, String url) {
    }
}
