package com.healthcare.alertservice.controller;

import com.healthcare.alertservice.dto.AlertRuleRequest;
import com.healthcare.alertservice.dto.AlertRuleResponse;
import com.healthcare.alertservice.service.AlertRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @PostMapping
    public ResponseEntity<AlertRuleResponse> createRule(@Valid @RequestBody AlertRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertRuleService.createRule(request));
    }

    @GetMapping
    public ResponseEntity<List<AlertRuleResponse>> getAllRules() {
        return ResponseEntity.ok(alertRuleService.getAllRules());
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<AlertRuleResponse> getRuleById(@PathVariable Long ruleId) {
        return ResponseEntity.ok(alertRuleService.getRuleById(ruleId));
    }

    @GetMapping("/vital-type/{vitalType}")
    public ResponseEntity<AlertRuleResponse> getRuleByVitalType(@PathVariable String vitalType) {
        return ResponseEntity.ok(alertRuleService.getRuleByVitalType(vitalType));
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<AlertRuleResponse> updateRule(@PathVariable Long ruleId,
                                                        @Valid @RequestBody AlertRuleRequest request) {
        return ResponseEntity.ok(alertRuleService.updateRule(ruleId, request));
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
        alertRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}
