package com.schoolapp.dao;

import lombok.Data;

import java.util.Date;

@Data
public class RisingSectionDTO {

    private Long id;

    private String plantNo;
    private String batchNo;
    private String risingTime;
    private Double risingTempC;
    private Double moldPenetration;
    private String ballTest;
    private String remark;
    private String shift;
    private String plantName;

    private int userId;
    private int branchId;
    private int orgId;

    private Date createdDate;
}
