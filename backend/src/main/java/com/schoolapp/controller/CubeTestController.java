package com.schoolapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.schoolapp.dao.CubeTestDTO;
import com.schoolapp.entity.CubeTestEntity;
import com.schoolapp.service.CubeTestService;
import com.schoolapp.service.CubeTestServiceImpl;

@RestController
@RequestMapping("/api/cube-test")
public class CubeTestController {

    private final CubeTestService service;
    private final CubeTestServiceImpl serviceImpl;

    public CubeTestController(CubeTestService service, CubeTestServiceImpl serviceImpl) {
        this.service = service;
        this.serviceImpl = serviceImpl;
    }

    /** POST /api/cube-test — create with multi-sample details */
    @PostMapping
    public Object save(@RequestBody CubeTestDTO dto) {
        // If details are provided use DTO path, otherwise fall back to legacy BeanUtils
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            return serviceImpl.saveDto(dto);
        }
        CubeTestEntity e = new CubeTestEntity();
        BeanUtils.copyProperties(dto, e, "details");
        return service.save(e);
    }

    /** GET /api/cube-test — list all */
    @GetMapping
    public List<CubeTestEntity> list() {
        return service.findAll();
    }

    /** GET /api/cube-test/{id} */
    @GetMapping("/{id}")
    public CubeTestEntity getById(@PathVariable Long id) {
        return service.findById(id);
    }

    /** PUT /api/cube-test/{id} — update with multi-sample details */
    @PutMapping("/{id}")
    public Object update(@PathVariable Long id, @RequestBody CubeTestDTO dto) {
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            return serviceImpl.updateDto(id, dto);
        }
        CubeTestEntity e = new CubeTestEntity();
        BeanUtils.copyProperties(dto, e, "details");
        return service.update(id, e);
    }

    /** DELETE /api/cube-test/{id} */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** POST /api/cube-test/{id}/approve */
    @PostMapping("/{id}/approve")
    public CubeTestEntity approve(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String role) {
        return service.approve(id, userId, role);
    }

    /** POST /api/cube-test/{id}/reject */
    @PostMapping("/{id}/reject")
    public CubeTestEntity reject(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String role,
            @RequestBody Map<String, String> body) {
        return service.reject(id, body.get("reason"), userId, role);
    }
}
