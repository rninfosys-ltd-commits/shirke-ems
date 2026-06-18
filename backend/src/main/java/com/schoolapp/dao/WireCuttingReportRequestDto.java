package com.schoolapp.dao;

import java.util.Date;
import java.util.List;

public class WireCuttingReportRequestDto {

    private String batchNo;
    private Date cuttingDate;

    private int mouldNo;
    private String size;
    private int cuttingLength;
    private int ballTestMm;


    private String remark;
    private String time;
    private String cycleTime;

    // ===== NEW Cutting fields =====
    private Double cuttingHours;
    private Double cuttingTempC;
    private String cuttingTime;

    // ===== Workflow FK to Rising =====
    private Long risingId;

    // ===== Dynamic block size details =====
    private List<CuttingSizeDetailDto> sizeDetails;

    private double totalItem;

    public double getTotalItem() { return totalItem; }
    public void setTotalItem(double totalItem) { this.totalItem = totalItem; }

    private int userId;
    private int branchId;
    private int orgId;
    private int updatedBy;
    private String plantName;
    private String shift;

    public String getPlantName() {
        return plantName;
    }
    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }
    public String getShift() {
        return shift;
    }
    public void setShift(String shift) {
        this.shift = shift;
    }
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public Date getCuttingDate() {
		return cuttingDate;
	}
	public void setCuttingDate(Date cuttingDate) {
		this.cuttingDate = cuttingDate;
	}
	public int getMouldNo() {
		return mouldNo;
	}
	public void setMouldNo(int mouldNo) {
		this.mouldNo = mouldNo;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public int getCuttingLength() {
		return cuttingLength;
	}
	public void setCuttingLength(int cuttingLength) {
		this.cuttingLength = cuttingLength;
	}
	public int getBallTestMm() {
		return ballTestMm;
	}
	public void setBallTestMm(int ballTestMm) {
		this.ballTestMm = ballTestMm;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getCycleTime() {
		return cycleTime;
	}
	public void setCycleTime(String cycleTime) {
		this.cycleTime = cycleTime;
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
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

    public Double getCuttingHours() { return cuttingHours; }
    public void setCuttingHours(Double cuttingHours) { this.cuttingHours = cuttingHours; }

    public Double getCuttingTempC() { return cuttingTempC; }
    public void setCuttingTempC(Double cuttingTempC) { this.cuttingTempC = cuttingTempC; }

    public String getCuttingTime() { return cuttingTime; }
    public void setCuttingTime(String cuttingTime) { this.cuttingTime = cuttingTime; }

    public Long getRisingId() { return risingId; }
    public void setRisingId(Long risingId) { this.risingId = risingId; }

    public List<CuttingSizeDetailDto> getSizeDetails() { return sizeDetails; }
    public void setSizeDetails(List<CuttingSizeDetailDto> sizeDetails) { this.sizeDetails = sizeDetails; }
}
