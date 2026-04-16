package com.tarkov.board.mapintel;

import java.time.Instant;
import java.util.List;

public record TarkovMapHighValueLootAdminResponse(
        Long mapId,
        String mapNameZh,
        String mapNameEn,
        String version,
        Instant updatedAt,
        List<WhiteboardMapIntelResponse.HighValueLootPoint> points
) {
}
