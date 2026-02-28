package com.tarkov.board.map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TarkovMapService {

    private final TarkovMapRepository repository;

    public TarkovMapService(TarkovMapRepository repository) {
        this.repository = repository;
    }

    public List<TarkovMapResponse> listMaps() {
        return repository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TarkovMapResponse create(TarkovMapUpsertRequest request) {
        TarkovMapEntity entity = new TarkovMapEntity(
                request.nameZh(), request.nameEn(), request.bannerPath(), request.mapPath());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public TarkovMapResponse update(Long id, TarkovMapUpsertRequest request) {
        TarkovMapEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Map not found"));

        entity.setNameZh(request.nameZh());
        entity.setNameEn(request.nameEn());
        entity.setBannerPath(request.bannerPath());
        entity.setMapPath(request.mapPath());

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
        return new TarkovMapResponse(
                entity.getId(),
                entity.getNameZh(),
                entity.getNameEn(),
                entity.getBannerPath(),
                entity.getMapPath()
        );
    }
}
