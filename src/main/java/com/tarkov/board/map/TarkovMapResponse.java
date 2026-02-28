package com.tarkov.board.map;

public record TarkovMapResponse(Long id,
                                String nameZh,
                                String nameEn,
                                String bannerPath,
                                String mapPath) {
}
