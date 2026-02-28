package com.tarkov.board.whiteboard;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class WhiteboardChatService {

    private static final int DEFAULT_HISTORY_LIMIT = 100;
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_SENDER_NAME_LENGTH = 64;
    private static final String DEFAULT_SENDER_NAME = "Anonymous";

    private final WhiteboardInstanceRepository instanceRepository;
    private final WhiteboardChatMessageRepository messageRepository;

    public WhiteboardChatService(WhiteboardInstanceRepository instanceRepository,
                                 WhiteboardChatMessageRepository messageRepository) {
        this.instanceRepository = instanceRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public List<WhiteboardChatMessageResponse> listRecentMessages(String instanceId) {
        touchInstanceTtlOrThrow(instanceId);
        List<WhiteboardChatMessageEntity> entities = messageRepository.findByInstanceIdOrderByCreatedAtDesc(
                instanceId,
                PageRequest.of(0, DEFAULT_HISTORY_LIMIT)
        );

        return entities.stream()
                .sorted(Comparator.comparing(WhiteboardChatMessageEntity::getCreatedAt))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WhiteboardChatMessageResponse appendMessage(String instanceId, String senderName, String content) {
        touchInstanceTtlOrThrow(instanceId);

        String normalizedContent = normalizeContent(content);
        String normalizedSenderName = normalizeSenderName(senderName);
        Instant now = Instant.now();
        WhiteboardChatMessageEntity saved = messageRepository.save(
                new WhiteboardChatMessageEntity(instanceId, normalizedSenderName, normalizedContent, now)
        );
        return toResponse(saved);
    }

    private void touchInstanceTtlOrThrow(String instanceId) {
        Instant now = Instant.now();
        int touched = instanceRepository.touchExpireAtIfActive(
                instanceId,
                now,
                now.plus(WhiteboardRetention.INSTANCE_TTL)
        );
        if (touched == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Whiteboard instance not found or expired");
        }
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Message content is required");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new ResponseStatusException(BAD_REQUEST, "Message content is too long");
        }
        return trimmed;
    }

    private String normalizeSenderName(String senderName) {
        if (senderName == null || senderName.isBlank()) {
            return DEFAULT_SENDER_NAME;
        }
        String trimmed = senderName.trim();
        if (trimmed.length() > MAX_SENDER_NAME_LENGTH) {
            return trimmed.substring(0, MAX_SENDER_NAME_LENGTH);
        }
        return trimmed;
    }

    private WhiteboardChatMessageResponse toResponse(WhiteboardChatMessageEntity entity) {
        return new WhiteboardChatMessageResponse(
                entity.getId(),
                entity.getInstanceId(),
                entity.getSenderName(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
