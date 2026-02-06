package com.tarkov.board.map;

public record TarkovMapResponse(Long id,
                                String code,
                                String nameZh,
                                String nameEn,
                                String bannerObjectName,
                                String bannerUrl,
                                String mapObjectName,
                                String mapUrl) {
}
