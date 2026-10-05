package com.habittracker.stats.dto;

import java.time.LocalDate;

public record TrendPointResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        long completedCount,
        long possibleCount,
        double completionRate
) {
}
