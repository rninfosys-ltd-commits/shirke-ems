package com.schoolapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "rising_section")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RisingSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plantNo;
    private String batchNo;

    private String risingStartTime;  // when rising begins
    private String dischargeTime;    // when rising ends / discharge (was risingEndTime)
    private String risingEndTime;    // kept for backward compat
    private String risingTime;       // auto-calculated duration
    private Integer totalRisingTimeMin; // total rising time in minutes (computed)
    private Double risingTempC; // kept for legacy if any, mapped as well
    private String mouldNo;
    private Double mouldHeight;
    private Double mouldFlow;
    private Double risingTemperature;
    private String remark;
    private String remarks;

    private String shift;

    @Column(name = "plant_name", length = 50)
    private String plantName;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "casting_id")
    private CastingHallReport castingReport;

    private int userId;
    private int branchId;
    private int orgId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    private int updatedBy;
    private int isActive;

    @PrePersist
    protected void onCreate() {
        this.createdDate = new Date();
        this.isActive = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Date();
    }
}
