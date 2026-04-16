package com.tarkov.board.mapintel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TarkovMapHighValueLootUpsertRequest(
        @NotNull List<@Valid TarkovMapHighValueLootPointUpsertRequest> points
) {
}
