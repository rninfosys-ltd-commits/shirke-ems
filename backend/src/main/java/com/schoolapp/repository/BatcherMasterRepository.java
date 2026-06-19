package com.schoolapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolapp.entity.BatcherMaster;

@Repository
public interface BatcherMasterRepository extends JpaRepository<BatcherMaster, Long> {

}
