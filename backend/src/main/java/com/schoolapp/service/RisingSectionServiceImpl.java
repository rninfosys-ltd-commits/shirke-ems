package com.schoolapp.service;

import com.schoolapp.dao.RisingSectionDTO;
import com.schoolapp.entity.RisingSection;
import com.schoolapp.repository.RisingSectionRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RisingSectionServiceImpl implements RisingSectionService {

    private final RisingSectionRepository repository;
    private final com.schoolapp.repository.CastingHallReportRepository castingRepo;

    public RisingSectionServiceImpl(RisingSectionRepository repository, com.schoolapp.repository.CastingHallReportRepository castingRepo) {
        this.repository = repository;
        this.castingRepo = castingRepo;
    }

    private RisingSectionDTO toDto(RisingSection e) {
        RisingSectionDTO dto = new RisingSectionDTO();
        dto.setId(e.getId());
        dto.setPlantNo(e.getPlantNo());
        dto.setBatchNo(e.getBatchNo());
        dto.setRisingStartTime(e.getRisingStartTime());
        dto.setDischargeTime(e.getDischargeTime());
        dto.setRisingEndTime(e.getRisingEndTime());
        dto.setRisingTime(e.getRisingTime());
        dto.setTotalRisingTimeMin(e.getTotalRisingTimeMin());
        dto.setRisingTempC(e.getRisingTempC());
        dto.setMouldNo(e.getMouldNo());
        dto.setMouldHeight(e.getMouldHeight());
        dto.setMouldFlow(e.getMouldFlow());
        dto.setRisingTemperature(e.getRisingTemperature());
        dto.setRemark(e.getRemark());
        dto.setRemarks(e.getRemarks());
        dto.setShift(e.getShift());
        dto.setPlantName(e.getPlantName());
        dto.setUserId(e.getUserId());
        dto.setBranchId(e.getBranchId());
        dto.setOrgId(e.getOrgId());
        dto.setCreatedDate(e.getCreatedDate());
        if (e.getCastingReport() != null) {
            dto.setCastingId(e.getCastingReport().getId());
        }
        return dto;
    }

    private void applyDto(RisingSection e, RisingSectionDTO dto) {
        e.setPlantNo(dto.getPlantNo());
        e.setBatchNo(dto.getBatchNo());
        e.setRisingStartTime(dto.getRisingStartTime());
        // dischargeTime is the new name for risingEndTime
        String dischargeTime = dto.getDischargeTime() != null ? dto.getDischargeTime() : dto.getRisingEndTime();
        e.setDischargeTime(dischargeTime);
        e.setRisingEndTime(dischargeTime); // keep compat
        // calculate duration and totalRisingTimeMin
        String duration = calculateDuration(dto.getRisingStartTime(), dischargeTime);
        e.setRisingTime(duration);
        e.setTotalRisingTimeMin(calcMinutes(dto.getRisingStartTime(), dischargeTime));
        e.setRisingTempC(dto.getRisingTempC());
        e.setMouldNo(dto.getMouldNo());
        e.setMouldHeight(dto.getMouldHeight());
        e.setMouldFlow(dto.getMouldFlow());
        e.setRisingTemperature(dto.getRisingTemperature());
        e.setRemark(dto.getRemark());
        e.setRemarks(dto.getRemarks());
        e.setShift(dto.getShift());
        e.setPlantName(dto.getPlantName());
        e.setUserId(dto.getUserId());
        e.setBranchId(dto.getBranchId());
        e.setOrgId(dto.getOrgId());

        if (dto.getBatchNo() != null) {
            List<com.schoolapp.entity.CastingHallReport> castList = castingRepo.findByBatchNo(dto.getBatchNo());
            if (!castList.isEmpty()) {
                e.setCastingReport(castList.get(0));
            }
        }
    }

    private String calculateDuration(String start, String end) {
        if (start == null || end == null || start.isEmpty() || end.isEmpty()) {
            return "0 Hours 0 Minutes";
        }
        try {
            String[] startParts = start.split(":");
            String[] endParts = end.split(":");
            int startMin = Integer.parseInt(startParts[0].trim()) * 60 + Integer.parseInt(startParts[1].trim());
            int endMin = Integer.parseInt(endParts[0].trim()) * 60 + Integer.parseInt(endParts[1].trim());
            int diff = endMin - startMin;
            if (diff < 0) diff += 24 * 60;
            int h = diff / 60;
            int m = diff % 60;
            return String.format("%d Hours %d Minutes", h, m);
        } catch (Exception ex) {
            return "0 Hours 0 Minutes";
        }
    }

    private Integer calcMinutes(String start, String end) {
        if (start == null || end == null || start.isEmpty() || end.isEmpty()) return 0;
        try {
            String[] s = start.split(":"); String[] e = end.split(":");
            int sm = Integer.parseInt(s[0].trim()) * 60 + Integer.parseInt(s[1].trim());
            int em = Integer.parseInt(e[0].trim()) * 60 + Integer.parseInt(e[1].trim());
            int diff = em - sm;
            if (diff < 0) diff += 24 * 60;
            return diff;
        } catch (Exception ex) { return 0; }
    }

    @Override
    public RisingSectionDTO create(RisingSectionDTO dto) {
        RisingSection e = new RisingSection();
        applyDto(e, dto);
        repository.save(e);
        dto.setId(e.getId());
        dto.setCreatedDate(e.getCreatedDate());
        return dto;
    }

    @Override
    public RisingSectionDTO update(Long id, RisingSectionDTO dto) {
        RisingSection e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rising section record not found: " + id));
        applyDto(e, dto);
        e.setUpdatedDate(new Date());
        e.setUpdatedBy(dto.getUserId());
        repository.save(e);
        dto.setId(id);
        return dto;
    }

    @Override
    public List<RisingSectionDTO> getAll() {
        return repository.findByIsActive(1)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RisingSectionDTO getById(Long id) {
        return toDto(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rising section record not found: " + id)));
    }

    @Override
    public void delete(Long id) {
        RisingSection e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rising section record not found: " + id));
        e.setIsActive(0);
        e.setUpdatedDate(new Date());
        repository.save(e);
    }
}
