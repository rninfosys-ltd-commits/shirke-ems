package com.schoolapp.entity;

import jakarta.persistence.*;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "block_separating")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockSeparating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== FORM FIELDS =====
    @Column
    private String batchNumber;

    @Temporal(TemporalType.DATE)
    @Column
    private Date castingDate;

    @Column
    private String blockSize;

    /** Legacy time field. Kept for backward compatibility. */
    private String time;

    // ===== NEW FIELDS =====
    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    /** Auto-calculated: endTime - startTime (formatted HH:MM) */
    @Column(name = "duration")
    private String duration;

    @Column(name = "operator")
    private String operator;

    // ===== Common =====
    @Column(name = "shift", length = 100)
    private String shift;

    private String remark;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "plant_name", length = 50)
    private String plantName;

    @Temporal(TemporalType.DATE)
    @Column(name = "report_date")
    private Date reportDate;

    // ===== Workflow FK to Autoclave =====
    @Column(name = "autoclave_id")
    private Long autoclaveId;

    // ===== NEW BREAKAGE FIELDS =====
    private Integer middleCrack = 0;
    private Integer risingCrack = 0;
    private Integer cornerDamage = 0;
    private Integer bottomLineMiddleCrack = 0;
    private Integer upperLineCrack = 0;
    private Integer autoclaveDamage = 0;
    private Integer sideCrack = 0;
    private Integer chipping = 0;
    private Integer craneDamage = 0;
    private Integer unrise = 0;
    private Integer unsize = 0;
    private Integer uncut = 0;
    private Integer collapse = 0;

    private Integer totalBreakage = 0;
    private Integer totalPcs = 0;
    private Double breakagePercent = 0.0;

    // ===== Common Required Fields =====
    private int userId;
    private int branchId;
    private int orgId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    private int updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    private int isActive;

    @PrePersist
    @PreUpdate
    public void calculateBreakages() {
        this.totalBreakage = (middleCrack != null ? middleCrack : 0) +
                (risingCrack != null ? risingCrack : 0) +
                (cornerDamage != null ? cornerDamage : 0) +
                (bottomLineMiddleCrack != null ? bottomLineMiddleCrack : 0) +
                (upperLineCrack != null ? upperLineCrack : 0) +
                (autoclaveDamage != null ? autoclaveDamage : 0) +
                (sideCrack != null ? sideCrack : 0) +
                (chipping != null ? chipping : 0) +
                (craneDamage != null ? craneDamage : 0) +
                (unrise != null ? unrise : 0) +
                (unsize != null ? unsize : 0) +
                (uncut != null ? uncut : 0) +
                (collapse != null ? collapse : 0);

        if (this.totalPcs != null && this.totalPcs > 0) {
            this.breakagePercent = (this.totalBreakage * 100.0) / this.totalPcs;
        } else {
            this.breakagePercent = 0.0;
        }
    }
}
