package com.schoolapp.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockSeparatingDTO {

    private Long id;

    private String batchNumber;
    private Date castingDate;
    private String blockSize;

    /** Legacy time - kept for backward compat */
    private String time;

    // ===== NEW FIELDS =====
    private String startTime;
    private String endTime;
    /** Auto-calculated: endTime - startTime (HH:MM) */
    private String duration;
    private String operator;

    // ===== Common =====
    private String shift;
    private String remark;
    private String remarks;
    private String plantName;
    private Date reportDate;

    // ===== Workflow FK =====
    private Long autoclaveId;

    // ===== NEW BREAKAGE FIELDS =====
    private Integer middleCrack;
    private Integer risingCrack;
    private Integer cornerDamage;
    private Integer bottomLineMiddleCrack;
    private Integer upperLineCrack;
    private Integer autoclaveDamage;
    private Integer sideCrack;
    private Integer chipping;
    private Integer craneDamage;
    private Integer unrise;
    private Integer unsize;
    private Integer uncut;
    private Integer collapse;

    private Integer totalBreakage;
    private Integer totalPcs;
    private Double breakagePercent;

    private int userId;
    private int branchId;
    private int orgId;

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public Date getCastingDate() { return castingDate; }
    public void setCastingDate(Date castingDate) { this.castingDate = castingDate; }

    public String getBlockSize() { return blockSize; }
    public void setBlockSize(String blockSize) { this.blockSize = blockSize; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }

    public Long getAutoclaveId() { return autoclaveId; }
    public void setAutoclaveId(Long autoclaveId) { this.autoclaveId = autoclaveId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }

    public Integer getMiddleCrack() { return middleCrack; }
    public void setMiddleCrack(Integer middleCrack) { this.middleCrack = middleCrack; }
    public Integer getRisingCrack() { return risingCrack; }
    public void setRisingCrack(Integer risingCrack) { this.risingCrack = risingCrack; }
    public Integer getCornerDamage() { return cornerDamage; }
    public void setCornerDamage(Integer cornerDamage) { this.cornerDamage = cornerDamage; }
    public Integer getBottomLineMiddleCrack() { return bottomLineMiddleCrack; }
    public void setBottomLineMiddleCrack(Integer bottomLineMiddleCrack) { this.bottomLineMiddleCrack = bottomLineMiddleCrack; }
    public Integer getUpperLineCrack() { return upperLineCrack; }
    public void setUpperLineCrack(Integer upperLineCrack) { this.upperLineCrack = upperLineCrack; }
    public Integer getAutoclaveDamage() { return autoclaveDamage; }
    public void setAutoclaveDamage(Integer autoclaveDamage) { this.autoclaveDamage = autoclaveDamage; }
    public Integer getSideCrack() { return sideCrack; }
    public void setSideCrack(Integer sideCrack) { this.sideCrack = sideCrack; }
    public Integer getChipping() { return chipping; }
    public void setChipping(Integer chipping) { this.chipping = chipping; }
    public Integer getCraneDamage() { return craneDamage; }
    public void setCraneDamage(Integer craneDamage) { this.craneDamage = craneDamage; }
    public Integer getUnrise() { return unrise; }
    public void setUnrise(Integer unrise) { this.unrise = unrise; }
    public Integer getUnsize() { return unsize; }
    public void setUnsize(Integer unsize) { this.unsize = unsize; }
    public Integer getUncut() { return uncut; }
    public void setUncut(Integer uncut) { this.uncut = uncut; }
    public Integer getCollapse() { return collapse; }
    public void setCollapse(Integer collapse) { this.collapse = collapse; }
    public Integer getTotalBreakage() { return totalBreakage; }
    public void setTotalBreakage(Integer totalBreakage) { this.totalBreakage = totalBreakage; }
    public Integer getTotalPcs() { return totalPcs; }
    public void setTotalPcs(Integer totalPcs) { this.totalPcs = totalPcs; }
    public Double getBreakagePercent() { return breakagePercent; }
    public void setBreakagePercent(Double breakagePercent) { this.breakagePercent = breakagePercent; }
}
