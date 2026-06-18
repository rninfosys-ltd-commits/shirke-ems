package com.schoolapp.service;

import com.schoolapp.dao.AutoclaveDTO;
import com.schoolapp.entity.AutoclaveCycle;
import com.schoolapp.repository.AutoclaveRepository;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class AutoclaveServiceImpl implements AutoclaveService {

    private final AutoclaveRepository repo;

    public AutoclaveServiceImpl(AutoclaveRepository repo) {
        this.repo = repo;
    }

    @Override
    public AutoclaveDTO save(AutoclaveDTO dto) {
        AutoclaveCycle a = new AutoclaveCycle();
        a.setAutoclaveNo(generateAutoclaveNo());
        mapDtoToEntity(dto, a);
        validate(a, dto);
        if (a.getAutoclaveCycleNumber() == null || a.getAutoclaveCycleNumber().isEmpty()) {
            a.setAutoclaveCycleNumber(generateAutoclaveCycleNumber());
        }
        a.setCreatedDate(new Date());
        a.setIsActive(1);

        repo.save(a);
        dto.id = a.getId();
        return dto;
    }

    @Override
    public List<AutoclaveDTO> getAll() {
        return repo.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AutoclaveDTO getById(Long id) {
        AutoclaveCycle a = repo.findById(id).orElseThrow();
        return mapEntityToDto(a);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public AutoclaveDTO update(Long id, AutoclaveDTO dto) {
        AutoclaveCycle a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Autoclave not found"));

        // DO NOT touch autoclaveNo
        mapDtoToEntity(dto, a);
        validate(a, dto);
        a.setUpdatedDate(new Date());
        a.setUpdatedBy(dto.userId);

        repo.save(a);
        dto.autoclaveNo = a.getAutoclaveNo();
        return dto;
    }

    // ── Shared mapping helpers ──────────────────────────────────────────────

    private void mapDtoToEntity(AutoclaveDTO dto, AutoclaveCycle a) {
        a.setAutoclaveCycleNumber(dto.autoclaveCycleNumber);
        a.setStartedAt(dto.startedAt);
        a.setStartedDate(dto.startedDate);
        a.setCompletedAt(dto.completedAt);
        a.setCompletedDate(dto.completedDate);
        a.setRemarks(dto.remarks);
        a.setShift(dto.shift);
        a.setPlantName(dto.plantName);
        a.setCurrentStatus(dto.currentStatus);
        a.setPlant1BatchCount(dto.plant1BatchCount);
        a.setPlant2BatchCount(dto.plant2BatchCount);
        a.setTransferStartTime(dto.transferStartTime);
        a.setTransferredToAutoclaveNo(dto.transferredToAutoclaveNo);
        a.setTransferEndTime(dto.transferEndTime);
        a.setReleaseStartTime(dto.releaseStartTime);
        a.setReleaseEndTime(dto.releaseEndTime);
        a.setDoorOpenTime(dto.doorOpenTime);
        a.setPressure1Hr(dto.pressure1Hr);
        a.setPressure2Hr(dto.pressure2Hr);
        a.setPressure3Hr(dto.pressure3Hr);
        a.setPressureRelease(dto.pressureRelease);
        a.setUserId(dto.userId);
        a.setBranchId(dto.branchId);
        a.setOrgId(dto.orgId);
        a.setBatchNo(dto.batchNo);
    }

    private AutoclaveDTO mapEntityToDto(AutoclaveCycle a) {
        AutoclaveDTO d = new AutoclaveDTO();
        d.id = a.getId();
        d.autoclaveNo = a.getAutoclaveNo();
        d.autoclaveCycleNumber = a.getAutoclaveCycleNumber();
        d.startedAt = a.getStartedAt();
        d.startedDate = a.getStartedDate();
        d.completedAt = a.getCompletedAt();
        d.completedDate = a.getCompletedDate();
        d.remarks = a.getRemarks();
        d.shift = a.getShift();
        d.plantName = a.getPlantName();
        d.currentStatus = a.getCurrentStatus();
        d.plant1BatchCount = a.getPlant1BatchCount();
        d.plant2BatchCount = a.getPlant2BatchCount();
        d.transferStartTime = a.getTransferStartTime();
        d.transferredToAutoclaveNo = a.getTransferredToAutoclaveNo();
        d.transferEndTime = a.getTransferEndTime();
        d.releaseStartTime = a.getReleaseStartTime();
        d.releaseEndTime = a.getReleaseEndTime();
        d.doorOpenTime = a.getDoorOpenTime();
        d.pressure1Hr = a.getPressure1Hr();
        d.pressure2Hr = a.getPressure2Hr();
        d.pressure3Hr = a.getPressure3Hr();
        d.pressureRelease = a.getPressureRelease();
        d.userId = a.getUserId();
        d.branchId = a.getBranchId();
        d.orgId = a.getOrgId();
        d.batchNo = a.getBatchNo();
        d.wagons = new ArrayList<>(); // always empty now — wagons removed
        return d;
    }

    private void validate(AutoclaveCycle a, AutoclaveDTO dto) {
        if (a.getPlantName() == null || a.getPlantName().isBlank()) {
            throw new RuntimeException("Plant is required");
        }

        int batchCount = countBatches(dto.batchNo);
        if ("Plant 1".equalsIgnoreCase(a.getPlantName())) {
            if (batchCount < 15 || batchCount > 18) {
                throw new RuntimeException("Plant 1 batch count must be between 15 and 18");
            }
        } else if ("Plant 2".equalsIgnoreCase(a.getPlantName())) {
            if (batchCount < 1 || batchCount > 14) {
                throw new RuntimeException("Plant 2 batch count must be between 1 and 14");
            }
        }

        if (a.getAutoclaveNumber() == null) {
            throw new RuntimeException("Autoclave number is required");
        }

        List<Integer> validNumbers = getAvailableAutoclaveNumbers(a.getPlantName());
        if (!validNumbers.contains(a.getAutoclaveNumber())) {
            throw new RuntimeException("Invalid autoclave number for selected plant");
        }

        if (a.getTransferredToAutoclaveNo() != null && a.getTransferredToAutoclaveNo().equals(a.getAutoclaveNumber())) {
            throw new RuntimeException("Transferred autoclave cannot be the current autoclave");
        }

        if (a.getTransferredToAutoclaveNo() != null && !validNumbers.contains(a.getTransferredToAutoclaveNo())) {
            throw new RuntimeException("Invalid transferred autoclave for selected plant");
        }
    }

    private int countBatches(String batchNo) {
        if (batchNo == null || batchNo.isBlank()) {
            return 0;
        }
        return (int) Arrays.stream(batchNo.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .count();
    }

    private List<Integer> getAvailableAutoclaveNumbers(String plantName) {
        if ("Plant 1".equalsIgnoreCase(plantName)) {
            return Arrays.asList(1, 2, 3, 4, 5, 6);
        }
        if ("Plant 2".equalsIgnoreCase(plantName)) {
            return Arrays.asList(1, 2, 3, 4, 5);
        }
        return Collections.emptyList();
    }

    private String generateAutoclaveNo() {
        String lastNo = repo.findLastAutoclaveNo();
        if (lastNo == null)
            return "AUTO-0001";
        int num = Integer.parseInt(lastNo.replace("AUTO-", ""));
        return String.format("AUTO-%04d", num + 1);
    }

    private String generateAutoclaveCycleNumber() {
        String lastNo = repo.findLastAutoclaveCycleNumber();
        if (lastNo == null || lastNo.trim().isEmpty()) {
            return "1";
        }
        try {
            int num = Integer.parseInt(lastNo.trim());
            return String.valueOf(num + 1);
        } catch (NumberFormatException e) {
            return lastNo + "-1";
        }
    }
}
