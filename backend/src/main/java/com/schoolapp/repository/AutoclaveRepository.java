package com.schoolapp.repository;

//package com.Crmemp.repository;

//package com.Crmemp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.schoolapp.entity.AutoclaveCycle;

//import com.Crmemp.entity.AutoclaveCycle;

import java.util.Date;
import java.util.List;

public interface AutoclaveRepository extends JpaRepository<AutoclaveCycle, Long> {

  @Query(value = "SELECT autoclave_no FROM autoclave_cycle ORDER BY id DESC LIMIT 1", nativeQuery = true)
  String findLastAutoclaveNo();

  @Query(value = "SELECT autoclave_cycle_number FROM autoclave_cycle ORDER BY id DESC LIMIT 1", nativeQuery = true)
  String findLastAutoclaveCycleNumber();

  List<AutoclaveCycle> findByStartedDateBetween(Date start, Date end);

  @org.springframework.data.jpa.repository.Query("SELECT a FROM AutoclaveCycle a WHERE a.batchNo = :batchNo OR a.batchNo LIKE CONCAT(:batchNo, ', %') OR a.batchNo LIKE CONCAT('%, ', :batchNo) OR a.batchNo LIKE CONCAT('%, ', :batchNo, ', %')")
  List<AutoclaveCycle> findByBatchNo(@org.springframework.data.repository.query.Param("batchNo") String batchNo);

  @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) > 0 FROM AutoclaveCycle a WHERE a.batchNo = :batchNo OR a.batchNo LIKE CONCAT(:batchNo, ', %') OR a.batchNo LIKE CONCAT('%, ', :batchNo) OR a.batchNo LIKE CONCAT('%, ', :batchNo, ', %')")
  boolean existsByBatchNo(@org.springframework.data.repository.query.Param("batchNo") String batchNo);

  default boolean existsByWagonBatch(int batchNo) {
    return existsByBatchNo(String.valueOf(batchNo));
  }

  @org.springframework.data.jpa.repository.Query("SELECT a FROM AutoclaveCycle a WHERE a.plantName = :plantName")
  List<AutoclaveCycle> findByPlantName(@org.springframework.data.repository.query.Param("plantName") String plantName);

  @org.springframework.data.jpa.repository.Query("SELECT a FROM AutoclaveCycle a WHERE a.startedDate BETWEEN :start AND :end AND a.plantName = :plantName")
  List<AutoclaveCycle> findByStartedDateBetweenAndPlantName(
      @org.springframework.data.repository.query.Param("start") Date start,
      @org.springframework.data.repository.query.Param("end") Date end,
      @org.springframework.data.repository.query.Param("plantName") String plantName);
}
