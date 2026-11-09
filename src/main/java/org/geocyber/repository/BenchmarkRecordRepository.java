package org.geocyber.repository;

import org.geocyber.domain.BenchmarkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BenchmarkRecordRepository extends JpaRepository<BenchmarkRecord, Long> {
    List<BenchmarkRecord> findByModelNameOrderByRunTimestampDesc(String modelName);
    List<BenchmarkRecord> findTop20ByOrderByRunTimestampDesc();
}
