package com.habittracker.habit;

import com.habittracker.habit.dto.HabitRequest;
import com.habittracker.habit.dto.HabitResponse;
import com.habittracker.user.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public List<HabitResponse> list(@AuthenticationPrincipal User owner) {
        return habitService.listHabits(owner);
    }

    @PostMapping
    public ResponseEntity<HabitResponse> create(@AuthenticationPrincipal User owner, @Valid @RequestBody HabitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitService.createHabit(owner, request));
    }

    @PutMapping("/{id}")
    public HabitResponse update(@AuthenticationPrincipal User owner, @PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        return habitService.updateHabit(owner, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User owner, @PathVariable Long id) {
        habitService.deleteHabit(owner, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    public HabitResponse toggle(
            @AuthenticationPrincipal User owner,
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return habitService.toggleCompletion(owner, id, date != null ? date : LocalDate.now());
    }

    @GetMapping("/{id}/logs")
    public List<String> logs(
            @AuthenticationPrincipal User owner,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return habitService.getCompletedDates(owner, id, from, to);
    }
}
