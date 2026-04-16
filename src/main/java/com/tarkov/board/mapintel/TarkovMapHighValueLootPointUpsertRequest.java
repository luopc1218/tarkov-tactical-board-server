package com.tarkov.board.mapintel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TarkovMapHighValueLootPointUpsertRequest(
        @NotBlank @Size(max = 255) String area,
        @NotBlank @Size(max = 255) String location,
        @NotNull List<@NotBlank @Size(max = 255) String> items,
        @NotNull List<@NotBlank @Size(max = 255) String> keys,
        @NotBlank @Size(max = 64) String priority
) {
}
