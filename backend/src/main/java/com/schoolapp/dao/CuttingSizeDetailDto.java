package com.schoolapp.dao;

import lombok.Data;

@Data
public class CuttingSizeDetailDto {
    private Long id;
    private Double length;
    private Double height;
    private Double width;
    private Integer quantity;
    private Integer breakage;
    private Integer netQuantity;
}
