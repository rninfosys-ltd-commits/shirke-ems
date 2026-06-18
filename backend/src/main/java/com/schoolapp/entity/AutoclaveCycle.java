package com.schoolapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "autoclave_cycle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoclaveCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String autoclaveNo;
    private Integer autoclaveNumber; // NEW: 1-6 for Plant 1, 1-5 for Plant 2
    private String autoclaveCycleNumber;
    private String startedAt;
    private Date startedDate;
    private String completedAt;
    private Date completedDate;

    private String remarks;
    private String shift;
    private String plantName;

    // ===== STATUS =====
    @Column(name = "current_status", length = 50)
    private String currentStatus;

    @Column(name = "batch_no")
    private String batchNo;

    // ===== BATCH COUNTS =====
    @Column(name = "plant1_batch_count")
    private Integer plant1BatchCount;

    @Column(name = "plant2_batch_count")
    private Integer plant2BatchCount;

    @Column(name = "transfer_start_time")
    private String transferStartTime;

    @Column(name = "transferred_to_autoclave_no")
    private Integer transferredToAutoclaveNo;

    @Column(name = "transfer_end_time")
    private String transferEndTime;

    @Column(name = "release_start_time")
    private String releaseStartTime;

    @Column(name = "release_end_time")
    private String releaseEndTime;

    @Column(name = "door_open_time")
    private String doorOpenTime;

    // ===== NEW LIFECYCLE FIELDS (Autoclave Cycle Tracking) =====
    @Column(name = "door_close_time")
    private String doorCloseTime;

    @Column(name = "vacuum_start_time")
    private String vacuumStartTime;

    @Column(name = "autoclave_rising_start_time")
    private String autoclaveRisingStartTime;

    @Column(name = "autoclave_rising_close_time")
    private String autoclaveRisingCloseTime;

    @Column(name = "total_pressure_after_rising_close")
    private Double totalPressureAfterRisingClose;

    @Column(name = "pressure_after_door_open")
    private Double pressureAfterDoorOpen;

    // ===== PRESSURE READINGS =====
    @Column(name = "pressure1_hr")
    private Double pressure1Hr;

    @Column(name = "pressure2_hr")
    private Double pressure2Hr;

    @Column(name = "pressure3_hr")
    private Double pressure3Hr;

    @Column(name = "pressure_release")
    private Double pressureRelease;

    // 🔥 REQUIRED FIELDS
    private int userId;
    private int branchId;
    private int orgId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    private int updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    private int isActive = 1;

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }
}
