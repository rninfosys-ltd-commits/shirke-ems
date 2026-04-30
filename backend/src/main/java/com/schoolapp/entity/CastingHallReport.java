package com.schoolapp.entity;

import jakarta.persistence.*;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "casting_hall_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CastingHallReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String batchNo;

    private String size; // Changed to String for consistency with WireCutting
    private int mouldNo;

    private int flowInCm;
    private int castingTempC;

    private String remark;

    private String shift; // Morning / Afternoon / Night

    @Column(name = "plant_name", length = 50)
    private String plantName;

    // ================= APPROVAL FLOW =================

    @Column(name = "approval_stage")
    private String approvalStage;

    @Column(name = "approved_by_l1")
    private String approvedByL1;

    @Column(name = "approved_by_l2")
    private String approvedByL2;

    @Column(name = "approved_by_l3")
    private String approvedByL3;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    private int userId;
    private int branchId;
    private int orgId;

    // ✅ AUTO DATE (VERY IMPORTANT)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    private int updatedBy;
    private int isActive;

    // ================= LIFECYCLE =================

    @PrePersist
    protected void onCreate() {
        this.createdDate = new Date(); // 🔥 auto insert date
        this.isActive = 1;
        if (this.approvalStage == null) {
            this.approvalStage = "NONE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Date(); // 🔥 auto update date
    }

}
