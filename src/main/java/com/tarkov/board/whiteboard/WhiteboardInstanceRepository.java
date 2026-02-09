package com.tarkov.board.whiteboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface WhiteboardInstanceRepository extends JpaRepository<WhiteboardInstanceEntity, String> {

    Optional<WhiteboardInstanceEntity> findByInstanceIdAndExpireAtAfter(String instanceId, Instant now);

    boolean existsByInstanceIdAndExpireAtAfter(String instanceId, Instant now);

    @Modifying
    @Query("DELETE FROM WhiteboardInstanceEntity i WHERE i.expireAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
