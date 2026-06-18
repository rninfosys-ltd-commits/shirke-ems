package com.schoolapp.repository;

import com.schoolapp.entity.CubeTestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CubeTestDetailRepository extends JpaRepository<CubeTestDetail, Long> {
    List<CubeTestDetail> findByCubeTestId(Long cubeTestId);
}
