package com.schoolapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolapp.entity.BatcherMaster;
import com.schoolapp.repository.BatcherMasterRepository;

@Service
public class BatcherMasterServiceImpl implements BatcherMasterService {

    @Autowired
    private BatcherMasterRepository repository;

    @Override
    public BatcherMaster saveBatcher(BatcherMaster batcher) {
        return repository.save(batcher);
    }

    @Override
    public List<BatcherMaster> getAllBatchers() {
        return repository.findAll();
    }

    @Override
    public BatcherMaster getBatcherById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public BatcherMaster updateBatcher(Long id, BatcherMaster batcher) {
        BatcherMaster existing = getBatcherById(id);
        if (existing != null) {
            existing.setName(batcher.getName());
            return repository.save(existing);
        }
        return null;
    }

    @Override
    public void deleteBatcher(Long id) {
        repository.deleteById(id);
    }
}
