package com.schoolapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolapp.dao.BatchTraceabilityDTO;
import com.schoolapp.repository.*;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BatchTraceabilityService {

    @Autowired private ProductionEntryRepository productionRepo;
    @Autowired private CastingHallReportRepository castingRepo;
    @Autowired private RisingSectionRepository risingRepo;
    @Autowired private WireCuttingReportRepository cuttingRepo;
    @Autowired private AutoclaveRepository autoclaveRepo;
    @Autowired private BlockSeparatingRepository separatingRepo;
    @Autowired private CubeTestRepository cubeTestRepo;
    @Autowired private RejectionDataRepository rejectionRepo;

    public BatchTraceabilityDTO getTraceabilityByBatch(String batchNo) {
        BatchTraceabilityDTO dto = new BatchTraceabilityDTO();
        Map<String, Object> sharedFields = new LinkedHashMap<>();

        var prodList = productionRepo.findByBatchNo(batchNo);
        if (prodList != null && !prodList.isEmpty()) {
            var production = prodList.get(0);
            dto.setProduction(production);
            mergeSharedFields(sharedFields, production,
                "batchNo", "shift", "plantName", "createdDate", "productionTime", "castingTime", "siloNo1");
        }

        var castList = castingRepo.findByBatchNo(batchNo);
        if (castList != null && !castList.isEmpty()) {
            var casting = castList.get(0);
            dto.setCasting(casting);
            mergeSharedFields(sharedFields, casting,
                "batchNo", "shift", "plantName", "reportDate", "createdDate",
                "mouldNo", "mouldHeight", "mouldFlow", "height", "castingTempC", "remark", "bedNo");
        }

        var risingList = risingRepo.findByBatchNo(batchNo);
        if (risingList != null && !risingList.isEmpty()) {
            var rising = risingList.get(0);
            dto.setRising(rising);
            mergeSharedFields(sharedFields, rising,
                "batchNo", "shift", "plantName", "risingTime", "risingTempC",
                "mouldNo", "mouldHeight", "mouldFlow", "remark");
        }

        var cutList = cuttingRepo.findByBatchNo(batchNo);
        if (cutList != null && !cutList.isEmpty()) {
            var cutting = cutList.get(0);
            dto.setCutting(cutting);
            mergeSharedFields(sharedFields, cutting,
                "batchNo", "shift", "plantName", "cuttingDate", "mouldNo", "size",
                "ballTestMm", "time", "cuttingTempC", "remark");
        }

        var autoList = autoclaveRepo.findByBatchNo(batchNo);
        if (autoList != null && !autoList.isEmpty()) {
            var autoclave = autoList.get(0);
            dto.setAutoclave(autoclave);
            mergeSharedFields(sharedFields, autoclave,
                "batchNo", "shift", "plantName", "autoclaveNo", "runNo", "holdingHours");
        }

        var sepList = separatingRepo.findByBatchNumber(batchNo);
        if (sepList != null && !sepList.isEmpty()) {
            var separating = sepList.get(0);
            dto.setSeparating(separating);
            mergeSharedFields(sharedFields, separating,
                "batchNumber", "batchNo", "shift", "plantName", "blockSize");
        }

        var cubeList = cubeTestRepo.findByBatchNo(batchNo);
        if (cubeList != null && !cubeList.isEmpty()) {
            var cubeTest = cubeList.get(0);
            dto.setCubeTest(cubeTest);
            mergeSharedFields(sharedFields, cubeTest,
                "batchNo", "shift", "plantName", "castDate", "testingDate",
                "mouldNo", "blockSize", "remark",
                // include strengths/densities so lookup and reports can access saved values
                "dryStrength", "wetStrength", "dryDensity", "wetDensity", "demouldDensity",
                "compStrengthOverDry", "compStrengthMoisture", "densityKgM3", "cubeDimensionImmediate");
        }

        var rejList = rejectionRepo.findByBatchNo(batchNo);
        if (rejList != null && !rejList.isEmpty()) {
            var rejection = rejList.get(0);
            dto.setRejection(rejection);
            mergeSharedFields(sharedFields, rejection,
                "batchNo", "shift", "plantName", "blockSize", "remark");
        }

        dto.setSharedFields(sharedFields);

        return dto;
    }

    private void mergeSharedFields(Map<String, Object> sharedFields, Object source, String... fieldNames) {
        if (source == null) return;

        for (String fieldName : fieldNames) {
            if (sharedFields.containsKey(fieldName)) continue;
            Object value = readProperty(source, fieldName);
            if (value != null && !(value instanceof String s && s.isBlank())) {
                sharedFields.put(fieldName, value);
            }
        }
    }

    private Object readProperty(Object source, String fieldName) {
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (String prefix : new String[] { "get", "is" }) {
            try {
                Method method = source.getClass().getMethod(prefix + suffix);
                return method.invoke(source);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
