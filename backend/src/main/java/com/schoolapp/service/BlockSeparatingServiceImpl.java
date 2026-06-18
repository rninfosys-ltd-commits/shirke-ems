package com.schoolapp.service;

import org.springframework.stereotype.Service;

import com.schoolapp.dao.BlockSeparatingDTO;
import com.schoolapp.entity.BlockSeparating;
import com.schoolapp.repository.BlockSeparatingRepository;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlockSeparatingServiceImpl implements BlockSeparatingService {

    private final BlockSeparatingRepository repository;

    public BlockSeparatingServiceImpl(BlockSeparatingRepository repository) {
        this.repository = repository;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    /** Auto-calculate duration "HH:MM" from HH:MM strings. */
    private String calculateDuration(String startTime, String endTime) {
        if (startTime == null || endTime == null) return null;
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end   = LocalTime.parse(endTime);
            Duration dur = Duration.between(start, end);
            if (dur.isNegative()) dur = dur.plusHours(24);
            return String.format("%02d:%02d", dur.toHours(), dur.toMinutesPart());
        } catch (Exception e) {
            return null;
        }
    }

    /** Maps entity → DTO */
    private BlockSeparatingDTO toDTO(BlockSeparating e) {
        BlockSeparatingDTO dto = new BlockSeparatingDTO();
        dto.setId(e.getId());
        dto.setBatchNumber(e.getBatchNumber());
        dto.setCastingDate(e.getCastingDate());
        dto.setBlockSize(e.getBlockSize());
        dto.setTime(e.getTime());
        dto.setStartTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setDuration(e.getDuration());
        dto.setOperator(e.getOperator());
        dto.setShift(e.getShift());
        dto.setRemark(e.getRemark());
        dto.setRemarks(e.getRemarks());
        dto.setPlantName(e.getPlantName());
        dto.setReportDate(e.getReportDate());
        dto.setAutoclaveId(e.getAutoclaveId());
        dto.setUserId(e.getUserId());
        dto.setBranchId(e.getBranchId());
        dto.setOrgId(e.getOrgId());

        dto.setMiddleCrack(e.getMiddleCrack());
        dto.setRisingCrack(e.getRisingCrack());
        dto.setCornerDamage(e.getCornerDamage());
        dto.setBottomLineMiddleCrack(e.getBottomLineMiddleCrack());
        dto.setUpperLineCrack(e.getUpperLineCrack());
        dto.setAutoclaveDamage(e.getAutoclaveDamage());
        dto.setSideCrack(e.getSideCrack());
        dto.setChipping(e.getChipping());
        dto.setCraneDamage(e.getCraneDamage());
        dto.setUnrise(e.getUnrise());
        dto.setUnsize(e.getUnsize());
        dto.setUncut(e.getUncut());
        dto.setCollapse(e.getCollapse());
        dto.setTotalBreakage(e.getTotalBreakage());
        dto.setTotalPcs(e.getTotalPcs());
        dto.setBreakagePercent(e.getBreakagePercent());

        return dto;
    }

    /** Maps DTO → entity fields (does not touch id / createdDate) */
    private void applyFields(BlockSeparating e, BlockSeparatingDTO dto) {
        e.setBatchNumber(dto.getBatchNumber());
        e.setCastingDate(dto.getCastingDate());
        e.setBlockSize(dto.getBlockSize());
        e.setTime(dto.getTime());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setDuration(calculateDuration(dto.getStartTime(), dto.getEndTime()));
        e.setOperator(dto.getOperator());
        if (dto.getShift() != null) {
            e.setShift(dto.getShift().split(" ")[0]);
        } else {
            e.setShift(null);
        }
        e.setRemark(dto.getRemark());
        e.setRemarks(dto.getRemarks());
        e.setPlantName(dto.getPlantName());
        e.setReportDate(dto.getReportDate());
        e.setAutoclaveId(dto.getAutoclaveId());

        e.setMiddleCrack(dto.getMiddleCrack());
        e.setRisingCrack(dto.getRisingCrack());
        e.setCornerDamage(dto.getCornerDamage());
        e.setBottomLineMiddleCrack(dto.getBottomLineMiddleCrack());
        e.setUpperLineCrack(dto.getUpperLineCrack());
        e.setAutoclaveDamage(dto.getAutoclaveDamage());
        e.setSideCrack(dto.getSideCrack());
        e.setChipping(dto.getChipping());
        e.setCraneDamage(dto.getCraneDamage());
        e.setUnrise(dto.getUnrise());
        e.setUnsize(dto.getUnsize());
        e.setUncut(dto.getUncut());
        e.setCollapse(dto.getCollapse());
        e.setTotalPcs(dto.getTotalPcs());
    }

    // ─── CRUD ───────────────────────────────────────────────────────────────────

    @Override
    public BlockSeparatingDTO create(BlockSeparatingDTO dto) {
        BlockSeparating e = new BlockSeparating();
        applyFields(e, dto);
        e.setUserId(dto.getUserId());
        e.setBranchId(dto.getBranchId());
        e.setOrgId(dto.getOrgId());
        e.setCreatedDate(new Date());
        e.setUpdatedDate(new Date());
        e.setUpdatedBy(dto.getUserId());
        e.setIsActive(1);
        repository.save(e);
        dto.setId(e.getId());
        return dto;
    }

    @Override
    public BlockSeparatingDTO update(Long id, BlockSeparatingDTO dto) {
        BlockSeparating e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        applyFields(e, dto);
        e.setUpdatedBy(dto.getUserId());
        e.setUpdatedDate(new Date());
        repository.save(e);
        dto.setId(id);
        return dto;
    }

    @Override
    public List<BlockSeparatingDTO> getAll() {
        return repository.findByIsActive(1)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BlockSeparatingDTO getById(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found")));
    }

    @Override
    public void delete(Long id) {
        BlockSeparating e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        e.setIsActive(0);
        e.setUpdatedDate(new Date());
        repository.save(e);
    }
}
