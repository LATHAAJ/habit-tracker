package com.habittracker.ai;

import com.habittracker.ai.dto.HabitPlanRequest;
import com.habittracker.ai.dto.HabitPlanResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiHabitPlanService aiHabitPlanService;

    public AiController(AiHabitPlanService aiHabitPlanService) {
        this.aiHabitPlanService = aiHabitPlanService;
    }

    @PostMapping("/habit-plan")
    public HabitPlanResponse generateHabitPlan(@Valid @RequestBody HabitPlanRequest request) {
        return aiHabitPlanService.generatePlan(request.goal());
    }
}
