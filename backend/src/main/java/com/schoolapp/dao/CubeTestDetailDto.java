package com.schoolapp.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class CubeTestDetailDto {

    private Long id;
    private Long productionId;
    private Integer sampleNo;
    private Double density;
    private Double dryDensity;
    private Double moisture;
    private Double compressiveStrength;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date testDate;

    private String remarks;

    // ─── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductionId() { return productionId; }
    public void setProductionId(Long productionId) { this.productionId = productionId; }

    public Integer getSampleNo() { return sampleNo; }
    public void setSampleNo(Integer sampleNo) { this.sampleNo = sampleNo; }

    public Double getDensity() { return density; }
    public void setDensity(Double density) { this.density = density; }

    public Double getDryDensity() { return dryDensity; }
    public void setDryDensity(Double dryDensity) { this.dryDensity = dryDensity; }

    public Double getMoisture() { return moisture; }
    public void setMoisture(Double moisture) { this.moisture = moisture; }

    public Double getCompressiveStrength() { return compressiveStrength; }
    public void setCompressiveStrength(Double compressiveStrength) { this.compressiveStrength = compressiveStrength; }

    public Date getTestDate() { return testDate; }
    public void setTestDate(Date testDate) { this.testDate = testDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
