package com.tarkov.board.map;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarkovMapRepository extends JpaRepository<TarkovMapEntity, Long> {

    List<TarkovMapEntity> findAllByOrderByIdAsc();

    Optional<TarkovMapEntity> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
