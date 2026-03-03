package com.tarkov.board.map;

public record TarkovMapResponse(Long id,
                                String nameZh,
                                String nameEn,
                                Integer sortOrder,
                                String bannerPath,
                                String mapPath) {
}
