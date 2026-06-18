package com.schoolapp.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RejectionDataDTO {

	private Long id;
	private Date date;
	private String batchNo;
	private String blockSize;
	private Integer qty;
	private String shift;
	private String plantName;

	// ===== Existing breakage fields =====
	private Integer cornerDamage;
	private Integer eruptionType;
	private Integer topSideDamages;
	private Integer sideCrackThermalCrack;
	private Integer risingCrack;
	private Integer centreCrack;
	private Integer bottomUncutBlocks;
	private Integer totalBreakages;

	// ===== NEW fields =====
	private Integer autoclaveDamage;
	private Integer craneDamage;
	private Integer collapse;
	private Integer unrise;
	private Integer unsize;
	private Integer uncut;
	private Integer chipping;

	// ===== NEW rejection category fields =====
	private Integer crackRejection;
	private Integer dimensionFailure;
	private Integer densityFailure;
	private Integer strengthFailure;
	private Integer otherRejection;

	/** Auto-calculated by service: sum of all rejection categories */
	private Integer totalRejection;

	private String remarks;

	// ===== Workflow FK =====
	private Long cubeTestId;

	// ===== Common =====
	private Integer userId;
	private Integer branchId;
	private Integer orgId;
	private Date createdDate;
	private Integer updatedBy;
	private Date updatedDate;
	private Integer isActive;
}
