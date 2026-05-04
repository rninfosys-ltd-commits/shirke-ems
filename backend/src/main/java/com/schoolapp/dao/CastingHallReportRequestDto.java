package com.schoolapp.dao;

import java.util.Date;

public class CastingHallReportRequestDto {

    private Long id;
    private String batchNo;
    private Date createdDate;

    private Integer createdById;
    private String createdByName;

    private String approvalStage;

    private Integer approvedByL1;
    private String approvedByL1Name;

    private Integer approvedByL2;
    private String approvedByL2Name;

    private Integer approvedByL3;
    private String approvedByL3Name;

    private String rejectionReason;

    // casting fields
    private String height;
    private int mouldNo;
    private int flowInCm;
    private int castingTempC;
    private String remark;
    private int userId;
    
    private int branchId;
    private int orgId;
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

    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Integer getCreatedById() {
		return createdById;
	}
	public void setCreatedById(Integer createdById) {
		this.createdById = createdById;
	}
	public String getCreatedByName() {
		return createdByName;
	}
	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}
	public String getApprovalStage() {
		return approvalStage;
	}
	public void setApprovalStage(String approvalStage) {
		this.approvalStage = approvalStage;
	}
	public Integer getApprovedByL1() {
		return approvedByL1;
	}
	public void setApprovedByL1(Integer approvedByL1) {
		this.approvedByL1 = approvedByL1;
	}
	public String getApprovedByL1Name() {
		return approvedByL1Name;
	}
	public void setApprovedByL1Name(String approvedByL1Name) {
		this.approvedByL1Name = approvedByL1Name;
	}
	public Integer getApprovedByL2() {
		return approvedByL2;
	}
	public void setApprovedByL2(Integer approvedByL2) {
		this.approvedByL2 = approvedByL2;
	}
	public String getApprovedByL2Name() {
		return approvedByL2Name;
	}
	public void setApprovedByL2Name(String approvedByL2Name) {
		this.approvedByL2Name = approvedByL2Name;
	}
	public Integer getApprovedByL3() {
		return approvedByL3;
	}
	public void setApprovedByL3(Integer approvedByL3) {
		this.approvedByL3 = approvedByL3;
	}
	public String getApprovedByL3Name() {
		return approvedByL3Name;
	}
	public void setApprovedByL3Name(String approvedByL3Name) {
		this.approvedByL3Name = approvedByL3Name;
	}
	public String getRejectionReason() {
		return rejectionReason;
	}
	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public int getMouldNo() {
		return mouldNo;
	}
	public void setMouldNo(int mouldNo) {
		this.mouldNo = mouldNo;
	}
	public int getFlowInCm() {
		return flowInCm;
	}
	public void setFlowInCm(int flowInCm) {
		this.flowInCm = flowInCm;
	}
	public int getCastingTempC() {
		return castingTempC;
	}
	public void setCastingTempC(int castingTempC) {
		this.castingTempC = castingTempC;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
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
}
