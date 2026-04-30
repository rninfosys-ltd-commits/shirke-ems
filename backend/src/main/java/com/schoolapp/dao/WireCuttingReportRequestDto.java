package com.schoolapp.dao;

import java.util.Date;

public class WireCuttingReportRequestDto {

    private String batchNo;
    private Date cuttingDate;

    private int mouldNo;
    private String size;
    private int ballTestMm;


    private String remark;
    private String time;

    // Table Fields for Len 100
    private int qty100;
    private double quantityTotal100;
    private int breakage100;
    private double netQty100;

    // Table Fields for Len 150
    private int qty150;
    private double quantityTotal150;
    private int breakage150;
    private double netQty150;

    private double totalItem;

    // Getters and Setters for new fields
    public int getQty100() { return qty100; }
    public void setQty100(int qty100) { this.qty100 = qty100; }
    public double getQuantityTotal100() { return quantityTotal100; }
    public void setQuantityTotal100(double quantityTotal100) { this.quantityTotal100 = quantityTotal100; }
    public int getBreakage100() { return breakage100; }
    public void setBreakage100(int breakage100) { this.breakage100 = breakage100; }
    public double getNetQty100() { return netQty100; }
    public void setNetQty100(double netQty100) { this.netQty100 = netQty100; }

    public int getQty150() { return qty150; }
    public void setQty150(int qty150) { this.qty150 = qty150; }
    public double getQuantityTotal150() { return quantityTotal150; }
    public void setQuantityTotal150(double quantityTotal150) { this.quantityTotal150 = quantityTotal150; }
    public int getBreakage150() { return breakage150; }
    public void setBreakage150(int breakage150) { this.breakage150 = breakage150; }
    public double getNetQty150() { return netQty150; }
    public void setNetQty150(double netQty150) { this.netQty150 = netQty150; }

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


    // 👉 Generate getters & setters
}
