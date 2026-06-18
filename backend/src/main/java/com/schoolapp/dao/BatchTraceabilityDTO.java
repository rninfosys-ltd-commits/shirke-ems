package com.schoolapp.dao;

import lombok.Data;

@Data
public class BatchTraceabilityDTO {
    private Object production;
    private Object casting;
    private Object rising;
    private Object cutting;
    private Object autoclave;
    private Object separating;
    private Object cubeTest;
    private Object rejection;
    private java.util.Map<String, Object> sharedFields;
}
