package com.schoolapp.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CubeTestDTO {

    private Long id;
    private String batchNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date castDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date testingDate;

    private String shift;
    private String plantName;

    // ===== Legacy single-sample fields (kept for backward compat) =====
    private String cubeDimensionImmediate;
    private String cubeDimensionOverDry;
    private String weightImmediateKg;
    private String weightOverDryKg;
    private Double weightWithMoistureKg;
    private Double loadOverDryTonn;
    private Double loadMoistureTonn;
    private Double compStrengthOverDry;
    private Double compStrengthMoisture;
    private Double densityKgM3;

    // ===== NEW FIELDS =====
    private Double demouldDensity;
    private Double wetDensity;
    private Double wetStrength;
    private Double dryDensity;
    private Double dryStrength;

    // ===== Workflow FK =====
    private Long blockSeparatingId;

    // ===== NEW: Cube Size =====
    private String cubeSize; // "150x150x150" or "100x100x100"

    // ===== NEW: Post Cutting (Wet) - Top / Mid / Bottom =====
    private Double wetWeightTop; private Double wetWeightMid; private Double wetWeightBtm;
    private Double wetDensityTop; private Double wetDensityMid; private Double wetDensityBtm;
    private Double wetStrengthTop; private Double wetStrengthMid; private Double wetStrengthBtm;

    // ===== NEW: Dry Oven - Top / Mid / Bottom =====
    private Double dryWeightTop; private Double dryWeightMid; private Double dryWeightBtm;
    private Double dryDensityTop; private Double dryDensityMid; private Double dryDensityBtm;
    private Double dryStrengthTop; private Double dryStrengthMid; private Double dryStrengthBtm;

    // ===== Multi-sample details =====
    private List<CubeTestDetailDto> details;

    private int isActive;
    private Integer userId;
    private Integer branchId;
    private Integer orgId;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Date getCastDate() { return castDate; }
    public void setCastDate(Date castDate) { this.castDate = castDate; }

    public Date getTestingDate() { return testingDate; }
    public void setTestingDate(Date testingDate) { this.testingDate = testingDate; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }

    public String getCubeDimensionImmediate() { return cubeDimensionImmediate; }
    public void setCubeDimensionImmediate(String v) { this.cubeDimensionImmediate = v; }

    public String getCubeDimensionOverDry() { return cubeDimensionOverDry; }
    public void setCubeDimensionOverDry(String v) { this.cubeDimensionOverDry = v; }

    public String getWeightImmediateKg() { return weightImmediateKg; }
    public void setWeightImmediateKg(String v) { this.weightImmediateKg = v; }

    public String getWeightOverDryKg() { return weightOverDryKg; }
    public void setWeightOverDryKg(String v) { this.weightOverDryKg = v; }

    public Double getWeightWithMoistureKg() { return weightWithMoistureKg; }
    public void setWeightWithMoistureKg(Double v) { this.weightWithMoistureKg = v; }

    public Double getLoadOverDryTonn() { return loadOverDryTonn; }
    public void setLoadOverDryTonn(Double v) { this.loadOverDryTonn = v; }

    public Double getLoadMoistureTonn() { return loadMoistureTonn; }
    public void setLoadMoistureTonn(Double v) { this.loadMoistureTonn = v; }

    public Double getCompStrengthOverDry() { return compStrengthOverDry; }
    public void setCompStrengthOverDry(Double v) { this.compStrengthOverDry = v; }

    public Double getCompStrengthMoisture() { return compStrengthMoisture; }
    public void setCompStrengthMoisture(Double v) { this.compStrengthMoisture = v; }

    public Double getDensityKgM3() { return densityKgM3; }
    public void setDensityKgM3(Double v) { this.densityKgM3 = v; }

    public Double getDemouldDensity() { return demouldDensity; }
    public void setDemouldDensity(Double demouldDensity) { this.demouldDensity = demouldDensity; }
    public Double getWetDensity() { return wetDensity; }
    public void setWetDensity(Double wetDensity) { this.wetDensity = wetDensity; }
    public Double getWetStrength() { return wetStrength; }
    public void setWetStrength(Double wetStrength) { this.wetStrength = wetStrength; }
    public Double getDryDensity() { return dryDensity; }
    public void setDryDensity(Double dryDensity) { this.dryDensity = dryDensity; }
    public Double getDryStrength() { return dryStrength; }
    public void setDryStrength(Double dryStrength) { this.dryStrength = dryStrength; }

    public Long getBlockSeparatingId() { return blockSeparatingId; }
    public void setBlockSeparatingId(Long blockSeparatingId) { this.blockSeparatingId = blockSeparatingId; }

    public String getCubeSize() { return cubeSize; }
    public void setCubeSize(String cubeSize) { this.cubeSize = cubeSize; }

    // Wet getters/setters
    public Double getWetWeightTop() { return wetWeightTop; } public void setWetWeightTop(Double v) { wetWeightTop = v; }
    public Double getWetWeightMid() { return wetWeightMid; } public void setWetWeightMid(Double v) { wetWeightMid = v; }
    public Double getWetWeightBtm() { return wetWeightBtm; } public void setWetWeightBtm(Double v) { wetWeightBtm = v; }
    public Double getWetDensityTop() { return wetDensityTop; } public void setWetDensityTop(Double v) { wetDensityTop = v; }
    public Double getWetDensityMid() { return wetDensityMid; } public void setWetDensityMid(Double v) { wetDensityMid = v; }
    public Double getWetDensityBtm() { return wetDensityBtm; } public void setWetDensityBtm(Double v) { wetDensityBtm = v; }
    public Double getWetStrengthTop() { return wetStrengthTop; } public void setWetStrengthTop(Double v) { wetStrengthTop = v; }
    public Double getWetStrengthMid() { return wetStrengthMid; } public void setWetStrengthMid(Double v) { wetStrengthMid = v; }
    public Double getWetStrengthBtm() { return wetStrengthBtm; } public void setWetStrengthBtm(Double v) { wetStrengthBtm = v; }

    // Dry getters/setters
    public Double getDryWeightTop() { return dryWeightTop; } public void setDryWeightTop(Double v) { dryWeightTop = v; }
    public Double getDryWeightMid() { return dryWeightMid; } public void setDryWeightMid(Double v) { dryWeightMid = v; }
    public Double getDryWeightBtm() { return dryWeightBtm; } public void setDryWeightBtm(Double v) { dryWeightBtm = v; }
    public Double getDryDensityTop() { return dryDensityTop; } public void setDryDensityTop(Double v) { dryDensityTop = v; }
    public Double getDryDensityMid() { return dryDensityMid; } public void setDryDensityMid(Double v) { dryDensityMid = v; }
    public Double getDryDensityBtm() { return dryDensityBtm; } public void setDryDensityBtm(Double v) { dryDensityBtm = v; }
    public Double getDryStrengthTop() { return dryStrengthTop; } public void setDryStrengthTop(Double v) { dryStrengthTop = v; }
    public Double getDryStrengthMid() { return dryStrengthMid; } public void setDryStrengthMid(Double v) { dryStrengthMid = v; }
    public Double getDryStrengthBtm() { return dryStrengthBtm; } public void setDryStrengthBtm(Double v) { dryStrengthBtm = v; }

    public List<CubeTestDetailDto> getDetails() { return details; }
    public void setDetails(List<CubeTestDetailDto> details) { this.details = details; }

    public int getIsActive() { return isActive; }
    public void setIsActive(int isActive) { this.isActive = isActive; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }

    public Integer getOrgId() { return orgId; }
    public void setOrgId(Integer orgId) { this.orgId = orgId; }
}
