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

    private String risingTime;
    private Double risingTempC;

    private Double moldPenetration;
    private String ballTest;
    private String remark;

    private String shift;

    @Column(name = "plant_name", length = 50)
    private String plantName;

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
