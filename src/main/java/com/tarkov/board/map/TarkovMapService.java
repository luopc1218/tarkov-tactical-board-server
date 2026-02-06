package com.tarkov.board.map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TarkovMapService {

    private final TarkovMapRepository repository;
    private final MapAssetResolver assetResolver;

    public TarkovMapService(TarkovMapRepository repository, MapAssetResolver assetResolver) {
        this.repository = repository;
        this.assetResolver = assetResolver;
    }

    public List<TarkovMapResponse> listMaps() {
        return repository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TarkovMapResponse create(TarkovMapUpsertRequest request) {
        repository.findByCode(request.code()).ifPresent(map -> {
            throw new ResponseStatusException(BAD_REQUEST, "Map code already exists");
        });

        TarkovMapEntity entity = new TarkovMapEntity(
                request.code(), request.nameZh(), request.nameEn(), request.bannerObjectName(), request.mapObjectName());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public TarkovMapResponse update(Long id, TarkovMapUpsertRequest request) {
        TarkovMapEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Map not found"));

        if (repository.existsByCodeAndIdNot(request.code(), id)) {
            throw new ResponseStatusException(BAD_REQUEST, "Map code already exists");
        }

        entity.setCode(request.code());
        entity.setNameZh(request.nameZh());
        entity.setNameEn(request.nameEn());
        entity.setBannerObjectName(request.bannerObjectName());
        entity.setMapObjectName(request.mapObjectName());

        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Map not found");
        }
        repository.deleteById(id);
    }

    private TarkovMapResponse toResponse(TarkovMapEntity entity) {
        MapAssetResolver.AssetResolved banner = assetResolver.resolveBanner(entity);
        MapAssetResolver.AssetResolved mapBody = assetResolver.resolveMapBody(entity);
        return new TarkovMapResponse(
                entity.getId(),
                entity.getCode(),
                entity.getNameZh(),
                entity.getNameEn(),
                banner.objectName(),
                banner.url(),
                mapBody.objectName(),
                mapBody.url()
        );
    }
}
