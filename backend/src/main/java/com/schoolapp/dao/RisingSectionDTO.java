package com.schoolapp.dao;

import lombok.Data;

import java.util.Date;

@Data
public class RisingSectionDTO {

    private Long id;

    private String plantNo;
    private String batchNo;
    private String risingStartTime;
    private String dischargeTime;     // discharge = rising end time
    private String risingEndTime;     // kept for backward compat
    private String risingTime;        // HH:MM duration string
    private Integer totalRisingTimeMin; // computed minutes
    private String mouldNo;
    private Double mouldHeight;
    private Double mouldFlow;
    private Double risingTemperature;
    private Double risingTempC;
    private String remark;
    private String remarks;
    private String shift;
    private String plantName;

    private Long castingId;

    private int userId;
    private int branchId;
    private int orgId;

    private Date createdDate;
}
