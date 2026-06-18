package com.schoolapp.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoclaveWagonDTO {

    public Integer batchNo;
    public Integer size;

    // Morning batch slot
    public Integer mBatch;
    public Integer mSize;

    // Evening batch slot
    public Integer eBatch;
    public Integer eSize;

    // West batch slot
    public Integer wBatch;
    public Integer wSize;
}
