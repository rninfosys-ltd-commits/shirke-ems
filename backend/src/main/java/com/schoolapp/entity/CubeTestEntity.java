package com.schoolapp.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cube_test")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CubeTestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "cast_date")
    @Temporal(TemporalType.DATE)
    private Date castDate;

    @Column(name = "testing_date")
    @Temporal(TemporalType.DATE)
    private Date testingDate;

    @Column(name = "shift")
    private String shift;

    @Column(name = "plant_name", length = 50)
    private String plantName;

    // ===== Legacy single-sample fields (kept for backward compat) =====
    @Column(name = "cube_dimension_immediate")
    private String cubeDimensionImmediate;

    @Column(name = "cube_dimension_over_dry")
    private String cubeDimensionOverDry;

    @Column(name = "weight_immediate_kg")
    private String weightImmediateKg;

    @Column(name = "weight_over_dry_kg")
    private String weightOverDryKg;

    @Column(name = "weight_with_moisture_kg")
    private Double weightWithMoistureKg;

    @Column(name = "load_over_dry_tonn")
    private Double loadOverDryTonn;

    @Column(name = "load_moisture_tonn")
    private Double loadMoistureTonn;

    @Column(name = "comp_strength_over_dry")
    private Double compStrengthOverDry;

    @Column(name = "comp_strength_moisture")
    private Double compStrengthMoisture;

    @Column(name = "density_kgm3")
    private Double densityKgM3;

    // ===== NEW FIELDS =====
    @Column(name = "demould_density")
    private Double demouldDensity;
    @Column(name = "wet_density")
    private Double wetDensity;
    @Column(name = "wet_strength")
    private Double wetStrength;
    @Column(name = "dry_density")
    private Double dryDensity;
    @Column(name = "dry_strength")
    private Double dryStrength;

    // ===== Workflow FK to Block Separating =====
    @Column(name = "block_separating_id")
    private Long blockSeparatingId;

    // ===== NEW FIELDS: Cube Size & Top/Mid/Bottom =====
    @Column(name = "cube_size")
    private String cubeSize; // "150x150x150" or "100x100x100"

    // Post Cutting (Wet) - Top/Mid/Bottom
    @Column(name = "wet_weight_top") private Double wetWeightTop;
    @Column(name = "wet_weight_mid") private Double wetWeightMid;
    @Column(name = "wet_weight_btm") private Double wetWeightBtm;
    @Column(name = "wet_density_top") private Double wetDensityTop;
    @Column(name = "wet_density_mid") private Double wetDensityMid;
    @Column(name = "wet_density_btm") private Double wetDensityBtm;
    @Column(name = "wet_strength_top") private Double wetStrengthTop;
    @Column(name = "wet_strength_mid") private Double wetStrengthMid;
    @Column(name = "wet_strength_btm") private Double wetStrengthBtm;

    // Dry Oven - Top/Mid/Bottom
    @Column(name = "dry_weight_top") private Double dryWeightTop;
    @Column(name = "dry_weight_mid") private Double dryWeightMid;
    @Column(name = "dry_weight_btm") private Double dryWeightBtm;
    @Column(name = "dry_density_top") private Double dryDensityTop;
    @Column(name = "dry_density_mid") private Double dryDensityMid;
    @Column(name = "dry_density_btm") private Double dryDensityBtm;
    @Column(name = "dry_strength_top") private Double dryStrengthTop;
    @Column(name = "dry_strength_mid") private Double dryStrengthMid;
    @Column(name = "dry_strength_btm") private Double dryStrengthBtm;

    // ===== Multi-sample details =====
    @OneToMany(mappedBy = "cubeTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CubeTestDetail> details;

    // ===== Approval Workflow =====
    @Column(name = "approved_by_l1")
    private String approvedByL1;

    @Column(name = "approved_by_l2")
    private String approvedByL2;

    @Column(name = "approved_by_l3")
    private String approvedByL3;

    @Column(name = "approval_stage")
    private String approvalStage = "NONE";

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "reject_reason")
    private String rejectReason;

    // ===== Common Required Fields =====
    @Column(name = "user_id")
    private int userId;

    @Column(name = "branch_id")
    private int branchId;

    @Column(name = "org_id")
    private int orgId;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "updated_by")
    private int updatedBy;

    @Column(name = "updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @Column(name = "is_active")
    private int isActive;
}
