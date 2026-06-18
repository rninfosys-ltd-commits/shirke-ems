package com.schoolapp.service.export.impl;

import com.schoolapp.entity.CastingHallReport;
import com.schoolapp.repository.CastingHallReportRepository;
import com.schoolapp.service.export.StageReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CastingExportServiceImpl implements StageReportService {

    @Autowired
    private CastingHallReportRepository repository;

    @Override
    public String getStageName() {
        return "CASTING";
    }

    @Override
    public String[] getHeaders() {
        return new String[] { "Batch No", "Height", "Mould No", "Flow (cm)",
                "Temp (C)", "Shift", "Date" };
    }

    @Override
    public List<Map<String, Object>> getData(Date fromDate, Date toDate) {
        List<CastingHallReport> reports = repository.findByCreatedDateBetween(fromDate, toDate);
        return reports.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("Batch No", r.getBatchNo());
            map.put("Height", r.getHeight());
            map.put("Mould No", r.getMouldNo());
            map.put("Flow (cm)", r.getMouldFlow() != null ? String.valueOf(r.getMouldFlow()) : "—");
            map.put("Temp (C)", r.getCastingTempC());
            map.put("Shift", r.getShift());
            map.put("Date", r.getCreatedDate());
            return map;
        }).collect(Collectors.toList());
    }
}
