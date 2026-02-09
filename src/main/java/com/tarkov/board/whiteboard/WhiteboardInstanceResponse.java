package com.tarkov.board.whiteboard;

import java.time.Instant;

public record WhiteboardInstanceResponse(
        String instanceId,
        Long mapId,
        String wsPath,
        Instant createdAt,
        Instant expireAt
) {
}
