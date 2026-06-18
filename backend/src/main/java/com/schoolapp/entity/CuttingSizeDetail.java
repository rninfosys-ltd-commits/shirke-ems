package com.schoolapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cutting_size_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuttingSizeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cutting_id")
    private WireCuttingReport cuttingReport;

    private Double length;
    private Double height;
    private Double width;
    private Integer quantity;
    private Integer breakage;
    private Integer netQuantity;
}
