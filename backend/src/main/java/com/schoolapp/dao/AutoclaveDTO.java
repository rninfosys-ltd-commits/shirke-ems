package com.schoolapp.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoclaveDTO {

    public Long id;

    public String autoclaveNo;
    public Integer autoclaveNumber; // 1-6 for Plant 1, 1-5 for Plant 2
    public String autoclaveCycleNumber;
    public String startedAt;
    public Date startedDate;
    public String completedAt;
    public Date completedDate;
    public String remarks;
    public String shift;
    public String plantName;
    public String batchNo;

    // ===== STATUS =====
    public String currentStatus;

    // ===== BATCH COUNTS & OUTPUT =====
    public Integer plant1BatchCount;
    public Integer plant2BatchCount;
    public String transferStartTime;
    public Integer transferredToAutoclaveNo;
    public String transferEndTime;
    public String releaseStartTime;
    public String releaseEndTime;
    public String doorOpenTime;

    // ===== NEW CYCLE TRACKING FIELDS =====
    public String doorCloseTime;
    public String vacuumStartTime;
    public String autoclaveRisingStartTime;
    public String autoclaveRisingCloseTime;
    public Double totalPressureAfterRisingClose;
    public Double pressureAfterDoorOpen;

    // ===== PRESSURE READINGS =====
    public Double pressure1Hr;
    public Double pressure2Hr;
    public Double pressure3Hr;
    public Double pressureRelease;

    public int userId;
    public int branchId;
    public int orgId;

    public List<AutoclaveWagonDTO> wagons;
}
