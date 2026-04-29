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

    public RisingSectionServiceImpl(RisingSectionRepository repository) {
        this.repository = repository;
    }

    private RisingSectionDTO toDto(RisingSection e) {
        RisingSectionDTO dto = new RisingSectionDTO();
        dto.setId(e.getId());
        dto.setPlantNo(e.getPlantNo());
        dto.setBatchNo(e.getBatchNo());
        dto.setRisingTime(e.getRisingTime());
        dto.setRisingTempC(e.getRisingTempC());
        dto.setMoldPenetration(e.getMoldPenetration());
        dto.setBallTest(e.getBallTest());
        dto.setRemark(e.getRemark());
        dto.setShift(e.getShift());
        dto.setPlantName(e.getPlantName());
        dto.setUserId(e.getUserId());
        dto.setBranchId(e.getBranchId());
        dto.setOrgId(e.getOrgId());
        dto.setCreatedDate(e.getCreatedDate());
        return dto;
    }

    private void applyDto(RisingSection e, RisingSectionDTO dto) {
        e.setPlantNo(dto.getPlantNo());
        e.setBatchNo(dto.getBatchNo());
        e.setRisingTime(dto.getRisingTime());
        e.setRisingTempC(dto.getRisingTempC());
        e.setMoldPenetration(dto.getMoldPenetration());
        e.setBallTest(dto.getBallTest());
        e.setRemark(dto.getRemark());
        e.setShift(dto.getShift());
        e.setPlantName(dto.getPlantName());
        e.setUserId(dto.getUserId());
        e.setBranchId(dto.getBranchId());
        e.setOrgId(dto.getOrgId());
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
