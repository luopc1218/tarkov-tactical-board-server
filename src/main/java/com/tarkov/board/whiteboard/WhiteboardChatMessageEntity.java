package com.tarkov.board.whiteboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "whiteboard_chat_message")
public class WhiteboardChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_id", nullable = false, length = 64)
    private String instanceId;

    @Column(name = "sender_name", nullable = false, length = 64)
    private String senderName;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WhiteboardChatMessageEntity() {
    }

    public WhiteboardChatMessageEntity(String instanceId, String senderName, String content, Instant createdAt) {
        this.instanceId = instanceId;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
