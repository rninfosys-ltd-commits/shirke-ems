package com.schoolapp.repository;

import com.schoolapp.entity.RisingSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Sort;
import java.util.Date;
import java.util.List;

@Repository
public interface RisingSectionRepository extends JpaRepository<RisingSection, Long> {
    List<RisingSection> findByIsActive(int isActive, Sort sort);
    List<RisingSection> findByPlantName(String plantName);
    List<RisingSection> findByCreatedDateBetween(Date start, Date end);
    List<RisingSection> findByCreatedDateBetweenAndPlantName(Date start, Date end, String plantName);
    List<RisingSection> findByBatchNo(String batchNo);
    List<RisingSection> findByBatchNoAndShift(String batchNo, String shift);
    boolean existsByBatchNo(String batchNo);
}
