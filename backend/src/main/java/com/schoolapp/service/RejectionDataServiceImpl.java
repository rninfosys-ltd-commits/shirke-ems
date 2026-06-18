package com.schoolapp.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolapp.dao.RejectionDataDTO;
import com.schoolapp.entity.RejectionDataEntity;
import com.schoolapp.repository.RejectionDataRepository;

@Service
public class RejectionDataServiceImpl implements RejectionDataService {

    @Autowired
    private RejectionDataRepository repo;

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private int computeTotalBreakages(RejectionDataDTO dto) {
        return safe(dto.getCornerDamage())
             + safe(dto.getEruptionType())
             + safe(dto.getTopSideDamages())
             + safe(dto.getSideCrackThermalCrack())
             + safe(dto.getRisingCrack())
             + safe(dto.getCentreCrack())
             + safe(dto.getBottomUncutBlocks())
             + safe(dto.getAutoclaveDamage())
             + safe(dto.getCraneDamage())
             + safe(dto.getCollapse())
             + safe(dto.getUnrise())
             + safe(dto.getUnsize())
             + safe(dto.getUncut())
             + safe(dto.getChipping());
    }

    /** Auto-calculates totalRejection as sum of all individual rejection fields */
    private int computeTotalRejection(RejectionDataDTO dto) {
        return safe(dto.getCrackRejection())
             + safe(dto.getCornerDamage())
             + safe(dto.getDimensionFailure())
             + safe(dto.getDensityFailure())
             + safe(dto.getStrengthFailure())
             + safe(dto.getOtherRejection())
             + safe(dto.getEruptionType())
             + safe(dto.getTopSideDamages())
             + safe(dto.getSideCrackThermalCrack())
             + safe(dto.getRisingCrack())
             + safe(dto.getCentreCrack())
             + safe(dto.getBottomUncutBlocks());
    }

    private int safe(Integer v) {
        return v != null ? v : 0;
    }

    private RejectionDataEntity toEntity(RejectionDataDTO dto) {
        return RejectionDataEntity.builder()
            .date(dto.getDate())
            .batchNo(dto.getBatchNo())
            .blockSize(dto.getBlockSize())
            .qty(dto.getQty())
            .shift(dto.getShift())
            .plantName(dto.getPlantName())
            // existing breakage fields
            .cornerDamage(dto.getCornerDamage())
            .eruptionType(dto.getEruptionType())
            .topSideDamages(dto.getTopSideDamages())
            .sideCrackThermalCrack(dto.getSideCrackThermalCrack())
            .risingCrack(dto.getRisingCrack())
            .centreCrack(dto.getCentreCrack())
            .bottomUncutBlocks(dto.getBottomUncutBlocks())
            .autoclaveDamage(dto.getAutoclaveDamage())
            .craneDamage(dto.getCraneDamage())
            .collapse(dto.getCollapse())
            .unrise(dto.getUnrise())
            .unsize(dto.getUnsize())
            .uncut(dto.getUncut())
            .chipping(dto.getChipping())
            .totalBreakages(computeTotalBreakages(dto))
            // new rejection category fields
            .crackRejection(dto.getCrackRejection())
            .dimensionFailure(dto.getDimensionFailure())
            .densityFailure(dto.getDensityFailure())
            .strengthFailure(dto.getStrengthFailure())
            .otherRejection(dto.getOtherRejection())
            .totalRejection(computeTotalRejection(dto))
            .remarks(dto.getRemarks())
            // workflow FK
            .cubeTestId(dto.getCubeTestId())
            // common
            .userId(dto.getUserId())
            .branchId(dto.getBranchId())
            .orgId(dto.getOrgId())
            .build();
    }

    private void applyFields(RejectionDataEntity entity, RejectionDataDTO dto) {
        entity.setDate(dto.getDate());
        entity.setBatchNo(dto.getBatchNo());
        entity.setBlockSize(dto.getBlockSize());
        entity.setQty(dto.getQty());
        entity.setShift(dto.getShift());
        entity.setPlantName(dto.getPlantName());
        // existing
        entity.setCornerDamage(dto.getCornerDamage());
        entity.setEruptionType(dto.getEruptionType());
        entity.setTopSideDamages(dto.getTopSideDamages());
        entity.setSideCrackThermalCrack(dto.getSideCrackThermalCrack());
        entity.setRisingCrack(dto.getRisingCrack());
        entity.setCentreCrack(dto.getCentreCrack());
        entity.setBottomUncutBlocks(dto.getBottomUncutBlocks());
        entity.setAutoclaveDamage(dto.getAutoclaveDamage());
        entity.setCraneDamage(dto.getCraneDamage());
        entity.setCollapse(dto.getCollapse());
        entity.setUnrise(dto.getUnrise());
        entity.setUnsize(dto.getUnsize());
        entity.setUncut(dto.getUncut());
        entity.setChipping(dto.getChipping());
        entity.setTotalBreakages(computeTotalBreakages(dto));
        // new fields
        entity.setCrackRejection(dto.getCrackRejection());
        entity.setDimensionFailure(dto.getDimensionFailure());
        entity.setDensityFailure(dto.getDensityFailure());
        entity.setStrengthFailure(dto.getStrengthFailure());
        entity.setOtherRejection(dto.getOtherRejection());
        entity.setTotalRejection(computeTotalRejection(dto));
        entity.setRemarks(dto.getRemarks());
        entity.setCubeTestId(dto.getCubeTestId());
    }

    // ─── CRUD ───────────────────────────────────────────────────────────────────

    @Override
    public Object create(RejectionDataDTO dto) {
        RejectionDataEntity entity = toEntity(dto);
        entity.setCreatedDate(new Date());
        entity.setUpdatedBy(dto.getUserId());
        entity.setIsActive(1);
        return repo.save(entity);
    }

    @Override
    public Object getAll() {
        return repo.findAll();
    }

    @Override
    public Object getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Object update(Long id, RejectionDataDTO dto) {
        RejectionDataEntity entity = repo.findById(id).orElse(null);
        if (entity == null) return null;

        applyFields(entity, dto);
        entity.setUpdatedBy(dto.getUserId());
        entity.setUpdatedDate(new Date());
        return repo.save(entity);
    }

    @Override
    public Object delete(Long id) {
        if (!repo.existsById(id)) return null;
        repo.deleteById(id);
        return "Deleted Successfully";
    }
}
