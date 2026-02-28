package com.tarkov.board.whiteboard;

import java.time.Instant;

public record WhiteboardChatMessageResponse(
        Long id,
        String instanceId,
        String senderName,
        String content,
        Instant createdAt
) {
}
