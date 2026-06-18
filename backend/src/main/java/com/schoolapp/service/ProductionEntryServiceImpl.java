package com.schoolapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schoolapp.dao.MaterialValueDTO;
import com.schoolapp.dao.ProductionEntryRequest;
import com.schoolapp.dao.ProductionImportRequest;
import com.schoolapp.dao.ProductionImportResponse;
import com.schoolapp.dao.ProductionImportResult;
import com.schoolapp.entity.ProductionEntry;
import com.schoolapp.entity.ProductionMaterial;
import com.schoolapp.repository.CastingHallReportRepository;
import com.schoolapp.repository.ProductionEntryRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductionEntryServiceImpl implements ProductionEntryService {

    private final ProductionEntryRepository repository;
    private final CastingHallReportRepository castingHallReportRepository;

    public ProductionEntryServiceImpl(
            ProductionEntryRepository repository,
            CastingHallReportRepository castingHallReportRepository) {
        this.repository = repository;
        this.castingHallReportRepository = castingHallReportRepository;
    }

    @Override
    @Transactional
    public ProductionEntry save(ProductionEntryRequest r) {

        ProductionEntry e = new ProductionEntry();
        mapRequestToEntity(r, e);
        e.setApprovalStage("NONE");

        // 1️⃣ Save first to generate ID
        ProductionEntry saved = repository.save(e);

        // 2️⃣ Set batchNo = id
        saved.setBatchNo(String.valueOf(saved.getId()));

        // 3️⃣ Save dynamic materials
        saveMaterials(saved, r.getMaterials());

        // 4️⃣ Save again
        return repository.save(saved);
    }

    @Override
    @Transactional
    public ProductionEntry update(Long id, ProductionEntryRequest r) {

        ProductionEntry e = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production entry not found"));

        mapRequestToEntity(r, e);

        // Update dynamic materials — clear old, insert new
        e.getMaterials().clear();
        repository.save(e); // flush orphan removal

        saveMaterials(e, r.getMaterials());

        return repository.save(e);
    }

    private void saveMaterials(ProductionEntry entry, List<MaterialValueDTO> materials) {
        if (materials == null || materials.isEmpty())
            return;

        List<ProductionMaterial> list = new ArrayList<>();
        for (MaterialValueDTO dto : materials) {
            ProductionMaterial pm = ProductionMaterial.builder()
                    .productionEntry(entry)
                    .materialMasterId(dto.getMaterialMasterId())
                    .materialName(dto.getMaterialName())
                    .unit(dto.getUnit())
                    .value(dto.getValue())
                    .displayOrder(dto.getDisplayOrder())
                    .build();
            list.add(pm);
        }
        entry.getMaterials().addAll(list);
    }

    private void mapRequestToEntity(ProductionEntryRequest r, ProductionEntry e) {

        e.setShift(r.shift);
        e.setPlantName(r.plantName);

        e.setSiloNo1(r.siloNo1);
        e.setFaSolid1(r.faSolid1);

        // 🔥 AUTO TOTAL SOLID
        double totalSolid = (r.faSolid1 != null ? r.faSolid1 : 0);
        e.setTotalSolid(totalSolid);

        // Legacy material fields — still mapped
        e.setWaterLiter(r.waterLiter);
        e.setCementKg(r.cementKg);
        e.setLimeKg(r.limeKg);
        e.setGypsumKg(r.gypsumKg);
        e.setSolOilKg(r.solOilKg);
        e.setTempC(r.tempC);

        e.setFaDensity(r.faDensity);
        e.setExcessDensity(r.excessDensity);
        e.setExcessSolid(r.excessSolid);
        e.setCbmVolume(r.cbmVolume);

        e.setFaSlurryQty(r.faSlurryQty);
        e.setExcessSlurryQty(r.excessSlurryQty);
        e.setSurfactant(r.surfactant);
        e.setAluminumPowderKg(r.aluminumPowderKg);
        e.setDcmrt(r.dcmrt);
        e.setMixingTime(r.mixingTime);

        e.setCastingTime(r.castingTime);
        e.setProductionTime(r.productionTime);
        e.setProductionRemark(r.productionRemark);

        e.setUserId(r.userId);
        e.setBranchId(r.branchId);
        e.setOrgId(r.orgId);

        // Map Batcher
        e.setBatcherId(r.batcherId);
        e.setBatcherName(r.batcherName);

        // Perform Advanced Calculations
        double flyAshVal = totalSolid;
        double cementVal = r.cementKg != null ? r.cementKg : 0.0;
        double limeVal = r.limeKg != null ? r.limeKg : 0.0;
        double gypsumVal = r.gypsumKg != null ? r.gypsumKg : 0.0;
        double alumVal = r.aluminumPowderKg != null ? r.aluminumPowderKg : 0.0;
        double calcTotalSolids = flyAshVal + cementVal + limeVal + gypsumVal + alumVal;
        
        double batchVolume = r.cbmVolume != null && r.cbmVolume > 0 ? r.cbmVolume : ("Plant 1".equalsIgnoreCase(r.plantName) ? 3.744 : 1.0);
        
        double solidsPerCbm = calcTotalSolids / batchVolume;
        e.setTotalSolidsPerCbm(Math.round(solidsPerCbm * 100.0) / 100.0);
        
        double bindersPerCbm = (cementVal + limeVal + gypsumVal) / batchVolume;
        e.setTotalBindersPerCbm(Math.round(bindersPerCbm * 100.0) / 100.0);
        
        double waterVal = r.waterLiter != null ? r.waterLiter : 0.0;
        double waterPerCbm = waterVal / batchVolume;
        e.setTotalWaterPerCbm(Math.round(waterPerCbm * 100.0) / 100.0);
        
        double wsRatio = calcTotalSolids > 0 ? (waterVal / calcTotalSolids) : 0.0;
        e.setWaterSolidRatio(Math.round(wsRatio * 10000.0) / 10000.0);
    }

    @Override
    public List<ProductionEntry> getAll() {
        return repository.findAll();
    }

    @Override
    public ProductionEntry getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production entry not found"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        castingHallReportRepository.deleteByProductionEntry_Id(id);
        repository.deleteById(id);
    }

    @Override
    public ProductionEntry approve(Long productionId, Long userId, String role) {

        ProductionEntry entry = repository.findById(productionId)
                .orElseThrow(() -> new RuntimeException("Production entry not found"));

        String stage = entry.getApprovalStage();
        if (stage == null)
            stage = "NONE";

        switch (stage) {

            case "NONE":
                if (!"ROLE_DIRECTOR".equals(role))
                    throw new RuntimeException("Only L1 can approve");

                entry.setApprovedByL1(String.valueOf(userId));
                entry.setApprovalTimeL1(now());
                entry.setApprovalStage("L1");
                break;

            case "L1":
                if (!"ROLE_MANAGER".equals(role))
                    throw new RuntimeException("Only L2 can approve");

                entry.setApprovedByL2(String.valueOf(userId));
                entry.setApprovalTimeL2(now());
                entry.setApprovalStage("L2");
                break;

            case "L2":
                if (!"ROLE_SUPERVISOR".equals(role))
                    throw new RuntimeException("Only L3 can approve");

                entry.setApprovedByL3(String.valueOf(userId));
                entry.setApprovalTimeL3(now());
                entry.setApprovalStage("L3");
                break;

            default:
                throw new RuntimeException("Already approved");
        }

        return repository.save(entry);
    }

    @Override
    public ProductionEntry reject(Long productionId, Long userId, String role, String reason) {

        ProductionEntry entry = repository.findById(productionId)
                .orElseThrow(() -> new RuntimeException("Production entry not found"));

        String stage = entry.getApprovalStage();
        if (stage == null)
            stage = "NONE";

        if ("L3".equals(stage))
            throw new RuntimeException("Final approved cannot be rejected");

        if (("NONE".equals(stage) && !"ROLE_DIRECTOR".equals(role)) ||
                ("L1".equals(stage) && !"ROLE_MANAGER".equals(role)) ||
                ("L2".equals(stage) && !"ROLE_SUPERVISOR".equals(role))) {
            throw new RuntimeException("No authority to reject");
        }

        entry.setApprovalStage("NONE");
        entry.setApprovedByL1(null);
        entry.setApprovedByL2(null);
        entry.setApprovedByL3(null);
        entry.setRejectedBy(String.valueOf(userId));
        entry.setRejectReason(reason);

        return repository.save(entry);
    }

    @Override
    @Transactional
    public ProductionImportResponse importProduction(
            ProductionImportRequest request) {

        int saved = 0;
        int failed = 0;

        List<ProductionImportResult> results = new ArrayList<>();
        if (request.getProductions() == null || request.getProductions().isEmpty()) {
            throw new RuntimeException("No production data received");
        }
        for (ProductionEntryRequest r : request.getProductions()) {
            try {

                r.setUserId(request.getUploadedBy());
                r.setBranchId(request.getBranchId());
                r.setOrgId(request.getOrgId());

                ProductionEntry savedEntry = this.save(r);

                results.add(
                        new ProductionImportResult(
                                savedEntry.getBatchNo(),
                                "SUCCESS",
                                null));
                saved++;

            } catch (Exception ex) {

                results.add(
                        new ProductionImportResult(
                                r.getShift(),
                                "FAILED",
                                ex.getMessage()));
                failed++;
            }
        }

        return new ProductionImportResponse(saved, failed, results);
    }

    @Override
    public Map<String, Object> importExcel(Map<String, Object> body) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) body.get("rows");

        int saved = 0;
        int failed = 0;
        List<Map<String, String>> results = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            try {
                ProductionEntry e = new ProductionEntry();
                e.setShift(get(row, "Shift"));
                e.setBatchNo(get(row, "Batch No"));

                Double faSolid1Import = toDouble(row.get("FA Solid 1"));
                double totalSolid = (faSolid1Import != null ? faSolid1Import : 0);
                e.setFaSolid1(faSolid1Import);
                e.setTotalSolid(totalSolid);
 
                Double waterLit = toDouble(row.get("Water Liter"));
                e.setWaterLiter(waterLit);
                Double cementKgVal = toDouble(row.get("Cement Kg"));
                e.setCementKg(cementKgVal);
                Double limeKgVal = toDouble(row.get("Lime Kg"));
                e.setLimeKg(limeKgVal);
                Double gypsumKgVal = toDouble(row.get("Gypsum Kg"));
                e.setGypsumKg(gypsumKgVal);
                e.setSolOilKg(toDouble(row.get("Sol Oil Kg")));
                e.setTempC(toDouble(row.get("Temperature")));
                e.setFaSlurryQty(toDouble(row.get("FA Slurry Qty")));
                e.setExcessSlurryQty(toDouble(row.get("Excess Slurry Qty")));
                e.setSurfactant(toDouble(row.get("Surfactant")));
                e.setFaDensity(toDouble(row.get("FA Density")));
                e.setExcessDensity(toDouble(row.get("Excess Density")));
                e.setExcessSolid(toDouble(row.get("Excess Solid")));
                
                Double cbmVolVal = toDouble(row.get("Cbm Volume"));
                if (cbmVolVal == null) {
                    cbmVolVal = toDouble(row.get("CBM Volume"));
                }
                e.setCbmVolume(cbmVolVal);
                
                Double alumVal = toDouble(row.get("Aluminum Powder (kg)")) != null ? toDouble(row.get("Aluminum Powder (kg)")) : toDouble(row.get("DC Chemical"));
                e.setAluminumPowderKg(alumVal);
                e.setDcmrt(toDouble(row.get("DC MRT")));
                e.setMixingTime(toInt(row.get("Mixing Time")));

                e.setBatcherName(get(row, "Batcher"));

                // Advanced calculations
                double cementVal = cementKgVal != null ? cementKgVal : 0.0;
                double limeVal = limeKgVal != null ? limeKgVal : 0.0;
                double gypsumVal = gypsumKgVal != null ? gypsumKgVal : 0.0;
                double alumP = alumVal != null ? alumVal : 0.0;
                double calcTotalSolids = totalSolid + cementVal + limeVal + gypsumVal + alumP;
                
                double batchVol = e.getCbmVolume() != null && e.getCbmVolume() > 0 ? e.getCbmVolume() : ("Plant 1".equalsIgnoreCase(e.getPlantName()) ? 3.744 : 1.0);
                
                double solidsPerCbm = calcTotalSolids / batchVol;
                e.setTotalSolidsPerCbm(Math.round(solidsPerCbm * 100.0) / 100.0);
                
                double bindersPerCbm = (cementVal + limeVal + gypsumVal) / batchVol;
                e.setTotalBindersPerCbm(Math.round(bindersPerCbm * 100.0) / 100.0);
                
                double waterVal = waterLit != null ? waterLit : 0.0;
                double waterPerCbm = waterVal / batchVol;
                e.setTotalWaterPerCbm(Math.round(waterPerCbm * 100.0) / 100.0);
                
                double wsRatio = calcTotalSolids > 0 ? (waterVal / calcTotalSolids) : 0.0;
                e.setWaterSolidRatio(Math.round(wsRatio * 10000.0) / 10000.0);

                e.setCastingTime(get(row, "Casting Time"));
                e.setProductionTime(get(row, "Production Time"));
                e.setProductionRemark(get(row, "Production Remark"));

                repository.save(e);
                saved++;

                results.add(Map.of("status", "SUCCESS"));

            } catch (Exception ex) {
                failed++;
                results.add(Map.of(
                        "status", "FAILED",
                        "error", ex.getMessage()));
            }
        }

        return Map.of(
                "saved", saved,
                "failed", failed,
                "results", results);
    }

    private String get(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? null : v.toString();
    }

    private Double toDouble(Object v) {
        if (v == null || v.toString().isEmpty())
            return null;
        return Double.parseDouble(v.toString());
    }

    private Integer toInt(Object v) {
        if (v == null || v.toString().isEmpty())
            return null;
        return (int) Double.parseDouble(v.toString());
    }

    private String now() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

}
