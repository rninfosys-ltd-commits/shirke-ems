package com.schoolapp.entity;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rejection_data")
public class RejectionDataEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "report_date")
	@Temporal(TemporalType.DATE)
	private Date date;

	@Column(name = "batch_no")
	private String batchNo;

	@Column(name = "block_size")
	private String blockSize;

	private Integer qty;

	@Column(name = "shift")
	private String shift;

	@Column(name = "plant_name", length = 50)
	private String plantName;

	// ===== Existing breakage fields (kept for backward compat) =====
	@Column(name = "corner_damage")
	private Integer cornerDamage;

	@Column(name = "eruption_type")
	private Integer eruptionType;

	@Column(name = "top_side_damages")
	private Integer topSideDamages;

	@Column(name = "side_crack_thermal_crack")
	private Integer sideCrackThermalCrack;

	@Column(name = "rising_crack")
	private Integer risingCrack;

	@Column(name = "centre_crack")
	private Integer centreCrack;

	@Column(name = "bottom_uncut_blocks")
	private Integer bottomUncutBlocks;

	@Column(name = "total_breakages")
	private Integer totalBreakages;

	// ===== NEW fields =====
	@Column(name = "autoclave_damage")
	private Integer autoclaveDamage;
	@Column(name = "crane_damage")
	private Integer craneDamage;
	@Column(name = "collapse")
	private Integer collapse;
	@Column(name = "unrise")
	private Integer unrise;
	@Column(name = "unsize")
	private Integer unsize;
	@Column(name = "uncut")
	private Integer uncut;
	@Column(name = "chipping")
	private Integer chipping;

	// ===== NEW rejection category fields =====
	@Column(name = "crack_rejection")
	private Integer crackRejection;

	@Column(name = "dimension_failure")
	private Integer dimensionFailure;

	@Column(name = "density_failure")
	private Integer densityFailure;

	@Column(name = "strength_failure")
	private Integer strengthFailure;

	@Column(name = "other_rejection")
	private Integer otherRejection;

	/** Auto-calculated: sum of all rejection category fields */
	@Column(name = "total_rejection")
	private Integer totalRejection;

	@Column(name = "remarks", length = 500)
	private String remarks;

	// ===== Workflow FK to Cube Test =====
	@Column(name = "cube_test_id")
	private Long cubeTestId;

	// ===== Common Required Fields =====
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "branch_id")
	private Integer branchId;

	@Column(name = "org_id")
	private Integer orgId;

	@Column(name = "created_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name = "updated_by")
	private Integer updatedBy;

	@Column(name = "updated_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;

	@Column(name = "is_active")
	private Integer isActive;
}
