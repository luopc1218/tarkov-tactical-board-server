package com.tarkov.board.map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TarkovMapUpsertRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 128) String nameZh,
        @NotBlank @Size(max = 128) String nameEn,
        @NotBlank @Size(max = 255) String bannerPath,
        @NotBlank @Size(max = 255) String mapPath
) {
}
