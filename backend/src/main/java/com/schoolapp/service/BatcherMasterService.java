package com.schoolapp.service;

import java.util.List;

import com.schoolapp.entity.BatcherMaster;

public interface BatcherMasterService {
    BatcherMaster saveBatcher(BatcherMaster batcher);
    List<BatcherMaster> getAllBatchers();
    BatcherMaster getBatcherById(Long id);
    BatcherMaster updateBatcher(Long id, BatcherMaster batcher);
    void deleteBatcher(Long id);
}
