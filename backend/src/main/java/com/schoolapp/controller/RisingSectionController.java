package com.schoolapp.controller;

import com.schoolapp.dao.RisingSectionDTO;
import com.schoolapp.service.RisingSectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rising-section")
public class RisingSectionController {

    private final RisingSectionService service;

    public RisingSectionController(RisingSectionService service) {
        this.service = service;
    }

    @PostMapping
    public RisingSectionDTO create(@RequestBody RisingSectionDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public RisingSectionDTO update(@PathVariable Long id, @RequestBody RisingSectionDTO dto) {
        return service.update(id, dto);
    }

    @GetMapping
    public List<RisingSectionDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RisingSectionDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
