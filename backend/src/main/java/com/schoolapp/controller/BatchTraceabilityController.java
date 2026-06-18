package com.schoolapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolapp.service.BatchTraceabilityService;

@RestController
@CrossOrigin(origins = "*") // Based on common pattern in this project
public class BatchTraceabilityController {

    @Autowired
    private BatchTraceabilityService batchTraceabilityService;

    /**
     * Used for full traceability view.
     */
    @GetMapping("/api/batch-traceability/{batchNo}")
    public ResponseEntity<?> getBatchTraceability(@PathVariable String batchNo) {
        return ResponseEntity.ok(batchTraceabilityService.getTraceabilityByBatch(batchNo));
    }

    /**
     * Used by forms to auto-populate previous data.
     */
    @GetMapping("/api/production/batch/{batchNo}")
    public ResponseEntity<?> getBatchLookup(@PathVariable String batchNo) {
        return ResponseEntity.ok(batchTraceabilityService.getTraceabilityByBatch(batchNo));
    }
}
