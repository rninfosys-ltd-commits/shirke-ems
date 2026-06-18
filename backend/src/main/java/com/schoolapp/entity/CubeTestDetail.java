package com.schoolapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "cube_test_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CubeTestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cube_test_id")
    private CubeTestEntity cubeTest;

    /** FK to production entry for traceability */
    @Column(name = "production_id")
    private Long productionId;

    @Column(name = "sample_no")
    private Integer sampleNo;

    @Column(name = "density")
    private Double density;

    @Column(name = "dry_density")
    private Double dryDensity;

    @Column(name = "moisture")
    private Double moisture;

    @Column(name = "compressive_strength")
    private Double compressiveStrength;

    @Temporal(TemporalType.DATE)
    @Column(name = "test_date")
    private Date testDate;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
