package com.schoolapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolapp.entity.BatcherMaster;
import com.schoolapp.service.BatcherMasterService;

@RestController
@RequestMapping("/api/batchers")
public class BatcherMasterController {

    @Autowired
    private BatcherMasterService service;

    @PostMapping
    public BatcherMaster createBatcher(@RequestBody BatcherMaster batcher) {
        return service.saveBatcher(batcher);
    }

    @GetMapping
    public List<BatcherMaster> getAllBatchers() {
        return service.getAllBatchers();
    }

    @GetMapping("/{id}")
    public BatcherMaster getBatcherById(@PathVariable Long id) {
        return service.getBatcherById(id);
    }

    @PutMapping("/{id}")
    public BatcherMaster updateBatcher(@PathVariable Long id, @RequestBody BatcherMaster batcher) {
        return service.updateBatcher(id, batcher);
    }

    @DeleteMapping("/{id}")
    public String deleteBatcher(@PathVariable Long id) {
        service.deleteBatcher(id);
        return "Deleted Successfully";
    }
}
