package com.tarkov.board.map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TarkovMapUpsertRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 128) String nameZh,
        @NotBlank @Size(max = 128) String nameEn,
        @NotBlank @Size(max = 255) @Pattern(regexp = "(?i).+\\.png$", message = "bannerObjectName must end with .png") String bannerObjectName,
        @NotBlank @Size(max = 255) @Pattern(regexp = "(?i).+\\.png$", message = "mapObjectName must end with .png") String mapObjectName
) {
}
