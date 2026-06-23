package com.schoolapp.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "production_entry")
public class ProductionEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ---------- PRODUCTION DATA ----------
	private String shift;
	private String batchNo;

	@Column(name = "plant_name", length = 50)
	private String plantName;

	private String siloNo1;
	private Double faSolid1;

	private Double totalSolid;
	private Double faSlurryQty;
	private Double excessSlurryQty;
	private Double surfactant;

	@Column(name = "dc_chemical")
	private Double aluminumPowderKg;

	private Double dcmrt;

	// ---------- BATCHER SELECTION ----------
	private Long batcherId;
	private String batcherName;

	private java.time.LocalDate productionDate;

	// ---------- ADDED CALCULATIONS ----------
	private Double cbmVolume;
	private Double totalSolidsPerCbm;
	private Double totalBindersPerCbm;
	private Double totalWaterPerCbm;
	private Double waterSolidRatio;

	private Double faDensity;
	private Double excessDensity;
	private Double excessSolid;

	// ---------- LEGACY MATERIAL COLUMNS (kept for backward compatibility)
	// ----------
	private Double waterLiter;
	private Double cementKg;
	private Double limeKg;
	private Double gypsumKg;
	private Double solOilKg;
	private Double tempC;
	private Integer mixingTime;
	private String approvalTimeL1;
	private String approvalTimeL2;
	private String approvalTimeL3;

	private String castingTime;
	private String productionTime;

	@Column(length = 500)
	private String productionRemark;
	// ---------- APPROVAL WORKFLOW ----------

	private String approvedByL1;
	private String approvedByL2;
	private String approvedByL3;

	@Column(length = 20)
	@Builder.Default
	private String approvalStage = "NONE";

	private String rejectedBy;
	private String rejectReason;

	// ---------- DYNAMIC MATERIALS ----------
	@OneToMany(mappedBy = "productionEntry", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@Builder.Default
	private List<ProductionMaterial> materials = new ArrayList<>();

	// ---------- SYSTEM FIELDS ----------
	private int userId;
	private int branchId;
	private int orgId;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	private int updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;

	private int isActive;

	// ---------- AUTO SET ----------
	@PrePersist
	public void onCreate() {
		this.createdDate = new Date();
		this.isActive = 1;
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedDate = new Date();
	}
}
