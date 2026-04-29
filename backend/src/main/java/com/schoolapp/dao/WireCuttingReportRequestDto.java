package com.schoolapp.dao;

import java.util.Date;

public class WireCuttingReportRequestDto {

    private String batchNo;
    private Date cuttingDate;

    private int mouldNo;
    private String size;
    private int ballTestMm;

    // Length 100 fields
    private Integer len100Qty;
    private Integer len100TotalQty;
    private Integer len100Breakage;
    private Integer len100NetQty;

    // Length 150 fields
    private Integer len150Qty;
    private Integer len150TotalQty;
    private Integer len150Breakage;
    private Integer len150NetQty;

    private Integer totalItem;
    private String remark;
    private String time;

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
    public Integer getLen100Qty() {
        return len100Qty;
    }
    public void setLen100Qty(Integer len100Qty) {
        this.len100Qty = len100Qty;
    }
    public Integer getLen100TotalQty() {
        return len100TotalQty;
    }
    public void setLen100TotalQty(Integer len100TotalQty) {
        this.len100TotalQty = len100TotalQty;
    }
    public Integer getLen100Breakage() {
        return len100Breakage;
    }
    public void setLen100Breakage(Integer len100Breakage) {
        this.len100Breakage = len100Breakage;
    }
    public Integer getLen100NetQty() {
        return len100NetQty;
    }
    public void setLen100NetQty(Integer len100NetQty) {
        this.len100NetQty = len100NetQty;
    }
    public Integer getLen150Qty() {
        return len150Qty;
    }
    public void setLen150Qty(Integer len150Qty) {
        this.len150Qty = len150Qty;
    }
    public Integer getLen150TotalQty() {
        return len150TotalQty;
    }
    public void setLen150TotalQty(Integer len150TotalQty) {
        this.len150TotalQty = len150TotalQty;
    }
    public Integer getLen150Breakage() {
        return len150Breakage;
    }
    public void setLen150Breakage(Integer len150Breakage) {
        this.len150Breakage = len150Breakage;
    }
    public Integer getLen150NetQty() {
        return len150NetQty;
    }
    public void setLen150NetQty(Integer len150NetQty) {
        this.len150NetQty = len150NetQty;
    }
    public Integer getTotalItem() {
        return totalItem;
    }
    public void setTotalItem(Integer totalItem) {
        this.totalItem = totalItem;
    }

    // 👉 Generate getters & setters
}
