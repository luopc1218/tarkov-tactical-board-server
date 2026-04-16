package com.tarkov.board.mapintel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface TarkovMapLootRepository extends JpaRepository<TarkovMapLootEntity, Long> {

    Optional<TarkovMapLootEntity> findByMapNameZh(String mapNameZh);

    Optional<TarkovMapLootEntity> findFirstByMapNameZhIn(Collection<String> mapNames);
}
