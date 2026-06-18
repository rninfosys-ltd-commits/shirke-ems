package com.schoolapp.service.export.impl;

import com.schoolapp.entity.ProductionEntry;
import com.schoolapp.repository.ProductionEntryRepository;
import com.schoolapp.service.export.StageReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionExportServiceImpl implements StageReportService {

    @Autowired
    private ProductionEntryRepository repository;

    @Override
    public String getStageName() {
        return "PRODUCTION";
    }

    @Override
    public String[] getHeaders() {
        return new String[] {
                "Batch No", "Shift", "Date", "Silo 1", "FA Solid 1",
                "Total Solid",
                "FA Slurry Qty", "Excess Slurry Qty", "Surfactant", "Water (L)", "Cement (kg)", "Lime (kg)", "Gypsum (kg)", "Sol Oil (kg)",
                "Aluminum Powder (kg)", "DC MRT", "Mixing Time", "Temperature", "Production Time", "Remark"
        };
    }

    @Override
    public List<Map<String, Object>> getData(Date fromDate, Date toDate) {
        List<ProductionEntry> entries = repository.findByCreatedDateBetween(fromDate, toDate);
        return entries.stream().map(e -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("Batch No", e.getBatchNo());
            map.put("Shift", e.getShift());
            map.put("Date", e.getCreatedDate());
            map.put("Silo 1", e.getSiloNo1());
            map.put("FA Solid 1", e.getFaSolid1());
            map.put("Total Solid", e.getTotalSolid());
            map.put("FA Slurry Qty", e.getFaSlurryQty());
            map.put("Excess Slurry Qty", e.getExcessSlurryQty());
            map.put("Surfactant", e.getSurfactant());
            map.put("Water (L)", e.getWaterLiter());
            map.put("Cement (kg)", e.getCementKg());
            map.put("Lime (kg)", e.getLimeKg());
            map.put("Gypsum (kg)", e.getGypsumKg());
            map.put("Sol Oil (kg)", e.getSolOilKg());
            map.put("Aluminum Powder (kg)", e.getAluminumPowderKg());
            map.put("DC MRT", e.getDcmrt());
            map.put("Mixing Time", e.getMixingTime());
            map.put("Temperature", e.getTempC());
            map.put("Production Time", e.getProductionTime());
            map.put("Remark", e.getProductionRemark());
            return map;
        }).collect(Collectors.toList());
    }
}
