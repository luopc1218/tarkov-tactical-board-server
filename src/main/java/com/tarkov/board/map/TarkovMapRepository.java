package com.tarkov.board.map;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarkovMapRepository extends JpaRepository<TarkovMapEntity, Long> {

    List<TarkovMapEntity> findAllByOrderByIdAsc();
}
