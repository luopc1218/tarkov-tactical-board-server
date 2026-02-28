package com.tarkov.board.whiteboard;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WhiteboardChatMessageRepository extends JpaRepository<WhiteboardChatMessageEntity, Long> {

    List<WhiteboardChatMessageEntity> findByInstanceIdOrderByCreatedAtDesc(String instanceId, Pageable pageable);

    @Modifying
    void deleteByInstanceId(String instanceId);

    @Modifying
    @Query(value = """
            DELETE m
            FROM whiteboard_chat_message m
            LEFT JOIN whiteboard_instance i ON i.instance_id = m.instance_id
            WHERE i.instance_id IS NULL
            """, nativeQuery = true)
    int deleteOrphanMessages();
}
