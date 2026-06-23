package com.schoolapp.dao;

import java.util.List;

public class ProductionEntryRequest {

	public String shift;
	public String plantName;
	public java.time.LocalDate productionDate;

	public String siloNo1;
	public Double faSolid1;

	// Legacy material fields (kept for backward compatibility)
	public Double waterLiter;
	public Double cementKg;
	public Double limeKg;
	public Double gypsumKg;
	public Double solOilKg;
	public Double tempC;
	public Double faDensity;
	public Double excessDensity;
	public Double excessSolid;
	public Double cbmVolume;
	public Double faSlurryQty;
	public Double excessSlurryQty;
	public Double surfactant;
	public Double aluminumPowderKg;
	public Double dcmrt;
	public Integer mixingTime;

	public String castingTime;
	public String productionTime;
	public String productionRemark;

	public Long batcherId;
	public String batcherName;

	public int userId;
	public int branchId;
	public int orgId;

	// ===== DYNAMIC MATERIALS =====
	public List<MaterialValueDTO> materials;

	// ===== GETTERS & SETTERS =====

	public String getShift() {
		return shift;
	}

	public void setShift(String shift) {
		this.shift = shift;
	}

	public String getPlantName() {
		return plantName;
	}

	public void setPlantName(String plantName) {
		this.plantName = plantName;
	}

	public java.time.LocalDate getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(java.time.LocalDate productionDate) {
		this.productionDate = productionDate;
	}

	public String getSiloNo1() {
		return siloNo1;
	}

	public void setSiloNo1(String siloNo1) {
		this.siloNo1 = siloNo1;
	}

	public Double getFaSolid1() {
		return faSolid1;
	}

	public void setFaSolid1(Double faSolid1) {
		this.faSolid1 = faSolid1;
	}

	public Double getWaterLiter() {
		return waterLiter;
	}

	public void setWaterLiter(Double waterLiter) {
		this.waterLiter = waterLiter;
	}

	public Double getCementKg() {
		return cementKg;
	}

	public void setCementKg(Double cementKg) {
		this.cementKg = cementKg;
	}

	public Double getLimeKg() {
		return limeKg;
	}

	public void setLimeKg(Double limeKg) {
		this.limeKg = limeKg;
	}

	public Double getGypsumKg() {
		return gypsumKg;
	}

	public void setGypsumKg(Double gypsumKg) {
		this.gypsumKg = gypsumKg;
	}

	public Double getSolOilKg() {
		return solOilKg;
	}

	public void setSolOilKg(Double solOilKg) {
		this.solOilKg = solOilKg;
	}

	public Double getFaDensity() {
		return faDensity;
	}

	public void setFaDensity(Double faDensity) {
		this.faDensity = faDensity;
	}

	public Double getExcessDensity() {
		return excessDensity;
	}

	public void setExcessDensity(Double excessDensity) {
		this.excessDensity = excessDensity;
	}

	public Double getExcessSolid() {
		return excessSolid;
	}

	public void setExcessSolid(Double excessSolid) {
		this.excessSolid = excessSolid;
	}

	public Double getCbmVolume() {
		return cbmVolume;
	}

	public void setCbmVolume(Double cbmVolume) {
		this.cbmVolume = cbmVolume;
	}

	public Double getTempC() {
		return tempC;
	}

	public void setTempC(Double tempC) {
		this.tempC = tempC;
	}

	public String getCastingTime() {
		return castingTime;
	}

	public void setCastingTime(String castingTime) {
		this.castingTime = castingTime;
	}

	public String getProductionTime() {
		return productionTime;
	}

	public void setProductionTime(String productionTime) {
		this.productionTime = productionTime;
	}

	public String getProductionRemark() {
		return productionRemark;
	}

	public void setProductionRemark(String productionRemark) {
		this.productionRemark = productionRemark;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getBranchId() {
		return branchId;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}

	public int getOrgId() {
		return orgId;
	}

	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}

	public List<MaterialValueDTO> getMaterials() {
		return materials;
	}

	public void setMaterials(List<MaterialValueDTO> materials) {
		this.materials = materials;
	}

	public Double getFaSlurryQty() {
		return faSlurryQty;
	}

	public void setFaSlurryQty(Double faSlurryQty) {
		this.faSlurryQty = faSlurryQty;
	}

	public Double getExcessSlurryQty() {
		return excessSlurryQty;
	}

	public void setExcessSlurryQty(Double excessSlurryQty) {
		this.excessSlurryQty = excessSlurryQty;
	}

	public Double getSurfactant() {
		return surfactant;
	}

	public void setSurfactant(Double surfactant) {
		this.surfactant = surfactant;
	}

	public Double getAluminumPowderKg() {
		return aluminumPowderKg;
	}

	public void setAluminumPowderKg(Double aluminumPowderKg) {
		this.aluminumPowderKg = aluminumPowderKg;
	}

	public Long getBatcherId() {
		return batcherId;
	}

	public void setBatcherId(Long batcherId) {
		this.batcherId = batcherId;
	}

	public String getBatcherName() {
		return batcherName;
	}

	public void setBatcherName(String batcherName) {
		this.batcherName = batcherName;
	}

	public Double getDcmrt() {
		return dcmrt;
	}

	public void setDcmrt(Double dcmrt) {
		this.dcmrt = dcmrt;
	}

	public Integer getMixingTime() {
		return mixingTime;
	}

	public void setMixingTime(Integer mixingTime) {
		this.mixingTime = mixingTime;
	}
}
