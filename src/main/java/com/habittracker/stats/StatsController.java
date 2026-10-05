package com.habittracker.stats;

import com.habittracker.stats.dto.HabitStatRow;
import com.habittracker.stats.dto.TrendPointResponse;
import com.habittracker.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/trend")
    public List<TrendPointResponse> trend(
            @AuthenticationPrincipal User owner,
            @RequestParam(required = false, defaultValue = "WEEK") String granularity,
            @RequestParam(required = false, defaultValue = "12") int periods) {
        Granularity parsed;
        try {
            parsed = Granularity.valueOf(granularity.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidStatsRangeException("granularity must be WEEK or MONTH");
        }
        return statsService.trend(owner, parsed, periods);
    }

    @GetMapping("/habits")
    public List<HabitStatRow> habits(
            @AuthenticationPrincipal User owner,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statsService.rankHabits(owner, from, to);
    }
}
