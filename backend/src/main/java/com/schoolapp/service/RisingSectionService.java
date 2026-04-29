package com.schoolapp.service;

import com.schoolapp.dao.RisingSectionDTO;

import java.util.List;

public interface RisingSectionService {
    RisingSectionDTO create(RisingSectionDTO dto);
    RisingSectionDTO update(Long id, RisingSectionDTO dto);
    List<RisingSectionDTO> getAll();
    RisingSectionDTO getById(Long id);
    void delete(Long id);
}
