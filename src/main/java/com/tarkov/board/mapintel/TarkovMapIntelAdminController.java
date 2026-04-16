package com.tarkov.board.mapintel;

import com.tarkov.board.map.TarkovMapEntity;
import com.tarkov.board.map.TarkovMapRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/admin/maps/{mapId}/high-value-loot")
@Tag(name = "Admin Map Intel", description = "管理端地图情报接口")
@SecurityRequirement(name = "BearerAuth")
public class TarkovMapIntelAdminController {

    private final TarkovMapRepository mapRepository;
    private final TarkovMapLootService mapLootService;

    public TarkovMapIntelAdminController(TarkovMapRepository mapRepository, TarkovMapLootService mapLootService) {
        this.mapRepository = mapRepository;
        this.mapLootService = mapLootService;
    }

    @GetMapping
    @Operation(summary = "管理端获取地图高级物资点")
    public TarkovMapHighValueLootAdminResponse getHighValueLoot(@PathVariable Long mapId) {
        return mapLootService.getAdminLootInfo(getMapOrThrow(mapId));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "管理端更新地图高级物资点")
    public TarkovMapHighValueLootAdminResponse updateHighValueLoot(@PathVariable Long mapId,
                                                                   @Valid @RequestBody TarkovMapHighValueLootUpsertRequest request) {
        return mapLootService.saveAdminLootInfo(getMapOrThrow(mapId), request);
    }

    private TarkovMapEntity getMapOrThrow(Long mapId) {
        return mapRepository.findById(mapId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Map not found"));
    }
}
