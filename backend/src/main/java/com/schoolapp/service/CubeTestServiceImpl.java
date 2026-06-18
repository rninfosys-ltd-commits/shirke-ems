package com.schoolapp.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolapp.dao.CubeTestDTO;
import com.schoolapp.dao.CubeTestDetailDto;
import com.schoolapp.entity.CubeTestDetail;
import com.schoolapp.entity.CubeTestEntity;
import com.schoolapp.repository.CubeTestRepository;

@Service
public class CubeTestServiceImpl implements CubeTestService {

    private final CubeTestRepository repo;

    public CubeTestServiceImpl(CubeTestRepository repo) {
        this.repo = repo;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private CubeTestDetail toDetailEntity(CubeTestDetailDto dto, CubeTestEntity parent) {
        CubeTestDetail d = new CubeTestDetail();
        d.setCubeTest(parent);
        d.setProductionId(dto.getProductionId());
        d.setSampleNo(dto.getSampleNo());
        d.setDensity(dto.getDensity());
        d.setDryDensity(dto.getDryDensity());
        d.setMoisture(dto.getMoisture());
        d.setCompressiveStrength(dto.getCompressiveStrength());
        d.setTestDate(dto.getTestDate());
        d.setRemarks(dto.getRemarks());
        return d;
    }

    private CubeTestDetailDto toDetailDto(CubeTestDetail d) {
        CubeTestDetailDto dto = new CubeTestDetailDto();
        dto.setId(d.getId());
        dto.setProductionId(d.getProductionId());
        dto.setSampleNo(d.getSampleNo());
        dto.setDensity(d.getDensity());
        dto.setDryDensity(d.getDryDensity());
        dto.setMoisture(d.getMoisture());
        dto.setCompressiveStrength(d.getCompressiveStrength());
        dto.setTestDate(d.getTestDate());
        dto.setRemarks(d.getRemarks());
        return dto;
    }

    private CubeTestDTO toDTO(CubeTestEntity e) {
        CubeTestDTO dto = new CubeTestDTO();
        dto.setId(e.getId());
        dto.setBatchNo(e.getBatchNo());
        dto.setCastDate(e.getCastDate());
        dto.setTestingDate(e.getTestingDate());
        dto.setShift(e.getShift());
        dto.setPlantName(e.getPlantName());
        dto.setCubeDimensionImmediate(e.getCubeDimensionImmediate());
        dto.setCubeDimensionOverDry(e.getCubeDimensionOverDry());
        dto.setWeightImmediateKg(e.getWeightImmediateKg());
        dto.setWeightOverDryKg(e.getWeightOverDryKg());
        dto.setWeightWithMoistureKg(e.getWeightWithMoistureKg());
        dto.setLoadOverDryTonn(e.getLoadOverDryTonn());
        dto.setLoadMoistureTonn(e.getLoadMoistureTonn());
        dto.setCompStrengthOverDry(e.getCompStrengthOverDry());
        dto.setCompStrengthMoisture(e.getCompStrengthMoisture());
        dto.setDensityKgM3(e.getDensityKgM3());
        
        dto.setDemouldDensity(e.getDemouldDensity());
        dto.setWetDensity(e.getWetDensity());
        dto.setWetStrength(e.getWetStrength());
        dto.setDryDensity(e.getDryDensity());
        dto.setDryStrength(e.getDryStrength());

        dto.setBlockSeparatingId(e.getBlockSeparatingId());
        dto.setIsActive(e.getIsActive());
        dto.setUserId(e.getUserId());
        dto.setBranchId(e.getBranchId());
        dto.setOrgId(e.getOrgId());
        if (e.getDetails() != null) {
            dto.setDetails(e.getDetails().stream().map(this::toDetailDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private void applyFields(CubeTestEntity e, CubeTestDTO dto) {
        e.setBatchNo(dto.getBatchNo());
        e.setCastDate(dto.getCastDate());
        e.setTestingDate(dto.getTestingDate());
        e.setShift(dto.getShift());
        e.setPlantName(dto.getPlantName());
        e.setCubeDimensionImmediate(dto.getCubeDimensionImmediate());
        e.setCubeDimensionOverDry(dto.getCubeDimensionOverDry());
        e.setWeightImmediateKg(dto.getWeightImmediateKg());
        e.setWeightOverDryKg(dto.getWeightOverDryKg());
        e.setWeightWithMoistureKg(dto.getWeightWithMoistureKg());
        e.setLoadOverDryTonn(dto.getLoadOverDryTonn());
        e.setLoadMoistureTonn(dto.getLoadMoistureTonn());
        e.setCompStrengthOverDry(dto.getCompStrengthOverDry());
        e.setCompStrengthMoisture(dto.getCompStrengthMoisture());
        e.setDensityKgM3(dto.getDensityKgM3());

        e.setDemouldDensity(dto.getDemouldDensity());
        e.setWetDensity(dto.getWetDensity());
        e.setWetStrength(dto.getWetStrength());
        e.setDryDensity(dto.getDryDensity());
        e.setDryStrength(dto.getDryStrength());

        e.setBlockSeparatingId(dto.getBlockSeparatingId());
    }

    // ─── CRUD ───────────────────────────────────────────────────────────────────

    @Override
    public CubeTestEntity save(CubeTestEntity cubeTest) {
        cubeTest.setCreatedDate(new Date());
        cubeTest.setIsActive(1);
        cubeTest.setApprovalStage("NONE");
        return repo.save(cubeTest);
    }

    /** DTO-based save — preferred for new records with details */
    public CubeTestDTO saveDto(CubeTestDTO dto) {
        CubeTestEntity e = new CubeTestEntity();
        applyFields(e, dto);
        e.setUserId(dto.getUserId() != null ? dto.getUserId() : 0);
        e.setBranchId(dto.getBranchId() != null ? dto.getBranchId() : 0);
        e.setOrgId(dto.getOrgId() != null ? dto.getOrgId() : 0);
        e.setCreatedDate(new Date());
        e.setIsActive(1);
        e.setApprovalStage("NONE");

        // Build details
        List<CubeTestDetail> details = new ArrayList<>();
        if (dto.getDetails() != null) {
            for (CubeTestDetailDto dd : dto.getDetails()) {
                details.add(toDetailEntity(dd, e));
            }
        }
        e.setDetails(details);
        repo.save(e);
        return toDTO(e);
    }

    @Override
    public CubeTestEntity update(Long id, CubeTestEntity cubeTest) {
        CubeTestEntity db = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cube Test not found"));
        db.setBatchNo(cubeTest.getBatchNo());
        db.setCastDate(cubeTest.getCastDate());
        db.setTestingDate(cubeTest.getTestingDate());
        db.setShift(cubeTest.getShift());
        db.setCubeDimensionImmediate(cubeTest.getCubeDimensionImmediate());
        db.setCubeDimensionOverDry(cubeTest.getCubeDimensionOverDry());
        db.setWeightImmediateKg(cubeTest.getWeightImmediateKg());
        db.setWeightOverDryKg(cubeTest.getWeightOverDryKg());
        db.setWeightWithMoistureKg(cubeTest.getWeightWithMoistureKg());
        db.setLoadOverDryTonn(cubeTest.getLoadOverDryTonn());
        db.setLoadMoistureTonn(cubeTest.getLoadMoistureTonn());
        db.setCompStrengthOverDry(cubeTest.getCompStrengthOverDry());
        db.setCompStrengthMoisture(cubeTest.getCompStrengthMoisture());
        db.setDensityKgM3(cubeTest.getDensityKgM3());
        
        db.setDemouldDensity(cubeTest.getDemouldDensity());
        db.setWetDensity(cubeTest.getWetDensity());
        db.setWetStrength(cubeTest.getWetStrength());
        db.setDryDensity(cubeTest.getDryDensity());
        db.setDryStrength(cubeTest.getDryStrength());

        db.setBlockSeparatingId(cubeTest.getBlockSeparatingId());
        db.setUpdatedBy(cubeTest.getUpdatedBy());
        db.setUpdatedDate(new Date());
        db.setIsActive(cubeTest.getIsActive());
        return repo.save(db);
    }

    /** DTO-based update — preferred for records with details */
    public CubeTestDTO updateDto(Long id, CubeTestDTO dto) {
        CubeTestEntity e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cube Test not found"));
        applyFields(e, dto);
        e.setUpdatedBy(dto.getUserId() != null ? dto.getUserId() : 0);
        e.setUpdatedDate(new Date());

        // orphanRemoval-safe details update
        if (e.getDetails() != null) e.getDetails().clear();
        if (dto.getDetails() != null) {
            for (CubeTestDetailDto dd : dto.getDetails()) {
                if (e.getDetails() == null) e.setDetails(new ArrayList<>());
                e.getDetails().add(toDetailEntity(dd, e));
            }
        }
        return toDTO(repo.save(e));
    }

    @Override
    public List<CubeTestEntity> findAll() {
        return repo.findAll();
    }

    @Override
    public CubeTestEntity findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Cube Test not found"));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public CubeTestEntity approve(Long id, Long userId, String role) {
        CubeTestEntity entry = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cube Test not found"));
        String stage = entry.getApprovalStage();
        if (stage == null) stage = "NONE";
        switch (stage) {
            case "NONE":
                entry.setApprovedByL1(String.valueOf(userId));
                entry.setApprovalStage("L1");
                break;
            case "L1":
                entry.setApprovedByL2(String.valueOf(userId));
                entry.setApprovalStage("L2");
                break;
            case "L2":
                entry.setApprovedByL3(String.valueOf(userId));
                entry.setApprovalStage("L3");
                break;
            default:
                throw new RuntimeException("Already approved");
        }
        return repo.save(entry);
    }

    @Override
    public CubeTestEntity reject(Long cubeId, String reason, Long userId, String role) {
        CubeTestEntity entry = repo.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube Test not found"));
        entry.setApprovalStage("NONE");
        entry.setApprovedByL1(null);
        entry.setApprovedByL2(null);
        entry.setApprovedByL3(null);
        entry.setRejectedBy(String.valueOf(userId));
        entry.setRejectReason(reason);
        return repo.save(entry);
    }
}
